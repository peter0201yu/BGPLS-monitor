package models.igp.ospf;

import models.igp.RoutingGraph;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class OSPFArea extends RoutingGraph {
    public String areaId;
    public boolean isBackbone;
    public HashMap<String, OSPFShortestPathTree> areaBorderRouters;

    // This stores the cost of each link:
    // srcInterfaceId => (dstInterfaceId => IGPMetric)
    public HashMap<String, HashMap<String, Float>> IGPMetricCosts;

    // Create these two maps for quick lookup
    public HashMap<String, ArrayList<String>> routerToInterfaces;
    public HashMap<String, String> interfaceToRouter;

    public OSPFArea(String _areaId, boolean _isBackbone) {
        super();
        areaId = _areaId;
        isBackbone = _isBackbone;
        areaBorderRouters = new HashMap<>();
    }

    // ---------------------------------------------
    // Build topology functions
    // ---------------------------------------------
    @Override
    public void addNode(String nodeId) {
        nodes.add(nodeId);
    }

    @Override
    public void addEdge(String srcId, InetAddress srcAddress, String destId, InetAddress destAddress) {
        assert(nodes.contains(srcId));
        assert(nodes.contains(destId));

        edges.putIfAbsent(srcId, new HashSet<>());
        Set<String> neighbors = edges.get(srcId);
        neighbors.add(destId);
    }

    @Override
    public void removeNode(String nodeId) {
        assert(nodes.contains(nodeId));

        nodes.remove(nodeId);

        // Remove outgoing edges
        edges.remove(nodeId);

        // Remove incoming edges
        for (String srcId : edges.keySet()) {
            Set<String> neighbors = edges.get(srcId);
            if (neighbors.contains(nodeId)) {
                neighbors.remove(nodeId);
            }
        }
    }

    @Override
    public void removeEdge(String srcId, String destId) {
        assert(nodes.contains(srcId));
        assert(nodes.contains(destId));

        Set<String> neighbors = edges.get(srcId);
        neighbors.remove(destId);
    }

    // ---------------------------------------------
    // Find shortest path functions
    // ---------------------------------------------
    public OSPFPath findShortestPathBetweenRouters(String srcRouterId, String dstRouterId) {

        HashSet<String> visited = new HashSet<>();
        // Router => distance from srcRouter
        HashMap<String, Float> distances = new HashMap<>();
        // Router => previous router on the way from srcRouter
        HashMap<String, String> previous = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>((router1, router2) -> {
            float distance1 = distances.getOrDefault(router1, Float.MAX_VALUE);
            float distance2 = distances.getOrDefault(router2, Float.MAX_VALUE);
            return Float.compare(distance1, distance2);
        });

        // Initialize distances
        for (String routerId : routerToInterfaces.keySet()) {
            distances.put(routerId, Float.MAX_VALUE);
            previous.put(routerId, null);
        }
        distances.put(srcRouterId, 0f);

        queue.add(srcRouterId);

        while (!queue.isEmpty()) {
            String currentRouterId = queue.poll();
            visited.add(currentRouterId);

            // If the destination router is reached, reconstruct the path and return the total cost
            if (currentRouterId.equals(dstRouterId)) {
                ArrayList<String> path = reconstructPath(previous, srcRouterId, dstRouterId);
                return new OSPFPath(srcRouterId, dstRouterId, path, distances.get(dstRouterId));
            }

            // Iterate through adjacent routers & interfaces, update distances if a shorter path is found
            for (String currentInterfaceId : routerToInterfaces.get(currentRouterId)) {
                for (String nextInterfaceId : IGPMetricCosts.get(currentInterfaceId).keySet()){
                    String nextRouterId = interfaceToRouter.get(nextInterfaceId);
                    if (visited.contains(nextRouterId)){
                        continue;
                    }
                    Float edgeCost = IGPMetricCosts.get(currentInterfaceId).getOrDefault(nextInterfaceId, Float.MAX_VALUE);
                    Float newDistance = distances.get(currentRouterId) + edgeCost;
                    if (newDistance < distances.get(nextRouterId)) {
                        distances.put(nextRouterId, newDistance);
                        previous.put(nextRouterId, currentRouterId);
                        if (!queue.contains(nextRouterId)){
                            queue.add(nextRouterId);
                        }
                    }
                }
            }
        }
        return null; // Destination router is unreachable
    }

    private ArrayList<String> reconstructPath(HashMap<String, String> previous, String srcRouterId, String dstRouterId) {
        ArrayList<String> path = new ArrayList<>();
        String current = dstRouterId;
        while (!current.equals(srcRouterId)) {
            path.add(current);
            current = previous.get(current);
        }
        path.add(srcRouterId);
        Collections.reverse(path);
        return path;
    }

    public void buildSpanningTree(String rootRouterId) {
        OSPFShortestPathTree spfTree = new OSPFShortestPathTree(rootRouterId);
        areaBorderRouters.put(rootRouterId, spfTree);

        HashSet<String> visited = new HashSet<>();
        HashMap<String, Float> costs = new HashMap<>();
        HashMap<String, String> parents = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>((router1, router2) -> {
            float distance1 = costs.getOrDefault(router1, Float.MAX_VALUE);
            float distance2 = costs.getOrDefault(router2, Float.MAX_VALUE);
            return Float.compare(distance1, distance2);
        });


        // Initialize the data structures
        for (String routerId : routerToInterfaces.keySet()) {
            costs.put(routerId, Float.MAX_VALUE);
            parents.put(routerId, null);
        }
        costs.put(rootRouterId, 0f);
        queue.add(rootRouterId);

        // Dijkstra's algorithm to build the spanning tree
        while (!queue.isEmpty()) {
            String currentRouterId = queue.poll();
            visited.add(currentRouterId);

            // Process the interfaces of the current router
            for (String currentInterfaceId : routerToInterfaces.get(currentRouterId)) {
                for (String nextInterfaceId : IGPMetricCosts.get(currentInterfaceId).keySet()){
                    String nextRouterId = interfaceToRouter.get(nextInterfaceId);
                    if (visited.contains(nextRouterId)){
                        continue;
                    }
                    Float edgeCost = IGPMetricCosts.get(currentInterfaceId).getOrDefault(nextInterfaceId, Float.MAX_VALUE);
                    Float newCost = costs.get(currentRouterId) + edgeCost;
                    if (newCost < costs.get(nextRouterId)) {
                        costs.put(nextRouterId, newCost);
                        parents.put(nextRouterId, currentRouterId);
                        if (!queue.contains(nextRouterId)) {
                            queue.add(nextRouterId);
                        }
                    }
                }
            }
        }

        // Update the spanning tree in OSPFShortestPathTree
        for (String childId : parents.keySet()) {
            spfTree.addToTree(childId, parents.get(childId), costs.get(childId));
        }
    }
}
