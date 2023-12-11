package models.igp.ospf;

import models.igp.RoutingGraph;
import util.Pair;

import java.net.InetAddress;
import java.util.*;

public class OSPFArea extends RoutingGraph {
    public String areaId;
    public boolean isBackbone;
    public HashMap<String, OSPFShortestPathTree> areaBorderRouters;

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
        nodesToInterfaces.putIfAbsent(nodeId, new HashSet<>());
    }

    @Override
    public void addEdge(String srcId, InetAddress srcAddress, String destId, InetAddress destAddress, float metric) {
        assert(nodesToInterfaces.containsKey(srcId));
        assert(nodesToInterfaces.containsKey(destId));

        String srcIp = srcAddress.getHostAddress();
        String destIp = destAddress.getHostAddress();

        // Keep track of a node's interfaces
        nodesToInterfaces.get(srcId).add(srcIp);
        nodesToInterfaces.get(destId).add(destIp);

        // Map interfaces to routers
        interfaceToRouter.put(srcIp, srcId);
        interfaceToRouter.put(destIp, destId);

        // Add edges
        edges.putIfAbsent(srcId, new HashSet<>());
        Set<String> neighbors = edges.get(srcId);
        neighbors.add(destId);

        // Add metric
        edgeCosts.putIfAbsent(srcIp, new HashMap<>());
        edgeCosts.putIfAbsent(destIp, new HashMap<>());
        Map<String, Float> costs = edgeCosts.get(srcIp);
        costs.put(destIp, metric);
    }

    @Override
    public void removeNode(String nodeId) {
        assert(nodesToInterfaces.containsKey(nodeId));

        // Remove mapping from its interfaces
        for (String interfaceId : nodesToInterfaces.get(nodeId)) {
            interfaceToRouter.remove(interfaceId);
        }

        // Remove edge costs of outgoing edges
        edgeCosts.remove(nodeId);
        // Remove outgoing edges
        edges.remove(nodeId);

        // Remove incoming edges and edge costs of incoming edges
        for (String srcId : edges.keySet()) {
            Set<String> neighbors = edges.get(srcId);
            neighbors.remove(nodeId);
            Map<String, Float> costs = edgeCosts.get(srcId);
            costs.remove(nodeId);
        }

        // Remove node
        nodesToInterfaces.remove(nodeId);

        // Remove if abr
        areaBorderRouters.remove(nodeId);
    }

    @Override
    public void removeEdge(String srcId, String destId) {
        assert(nodesToInterfaces.containsKey(srcId));
        assert(nodesToInterfaces.containsKey(destId));

        // Remove edge
        Set<String> neighbors = edges.get(srcId);
        neighbors.remove(destId);
        // Remove edge cost
        Map<String, Float> costs = edgeCosts.get(srcId);
        costs.remove(destId);
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
        for (String routerId : nodesToInterfaces.keySet()) {
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
            for (String currentInterfaceId : nodesToInterfaces.get(currentRouterId)) {
                for (String nextInterfaceId : edgeCosts.get(currentInterfaceId).keySet()){
                    String nextRouterId = interfaceToRouter.get(nextInterfaceId);
                    if (visited.contains(nextRouterId)){
                        continue;
                    }
                    Float edgeCost = edgeCosts.get(currentInterfaceId).getOrDefault(nextInterfaceId, Float.MAX_VALUE);
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
        // TODO: we need to dynamically call this whenever the topology changes (as nodesToInterfaces + edges come in)
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
        for (String routerId : nodesToInterfaces.keySet()) {
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
            for (String currentInterfaceId : nodesToInterfaces.get(currentRouterId)) {
                for (String nextInterfaceId : edgeCosts.get(currentInterfaceId).keySet()){
                    String nextRouterId = interfaceToRouter.get(nextInterfaceId);
                    if (visited.contains(nextRouterId)){
                        continue;
                    }
                    Float edgeCost = edgeCosts.get(currentInterfaceId).getOrDefault(nextInterfaceId, Float.MAX_VALUE);
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
