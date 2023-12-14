package models.igp.ospf;

import models.igp.IGPPath;
import models.igp.RoutingGraph;

import java.net.InetAddress;
import java.util.*;

public class OSPFSummaryGraph extends RoutingGraph {
    public OSPFSummaryGraph() {
        super();
    }

    @Override
    public void addNode(String nodeId) {
        nodesToInterfaces.putIfAbsent(nodeId, new HashSet<>());
    }

    @Override
    public void addEdge(String srcId, InetAddress srcAddress, String destId, InetAddress destAddress, float metric) {

    }

    public void addEdge(String srcId, String destId, float metric) {
        addNode(srcId);
        addNode(destId);

        String srcIp = srcId;
        String destIp = destId;

        // Keep track of a node's interfaces
        nodesToInterfaces.get(srcId).add("");
        nodesToInterfaces.get(destId).add("");

        // Map interfaces to routers
        interfaceToNode.put(srcIp, srcId);
        interfaceToNode.put(destIp, destId);

        // Add edges
        edges.putIfAbsent(srcId, new HashSet<>());
        edges.putIfAbsent(destId, new HashSet<>());
        Set<String> neighbors = edges.get(srcId);
        neighbors.add(destId);

        // Add metric
        edgeCosts.putIfAbsent(srcIp, new HashMap<>());
        edgeCosts.putIfAbsent(destIp, new HashMap<>());
        Map<String, Float> costs = edgeCosts.get(srcIp);
        costs.put(destIp, metric);
    }

    public void updateEdgeIfSmaller(String srcId, String destId, float metric) {
        addNode(srcId);
        addNode(destId);

        String srcIp = srcId;
        String destIp = destId;

        // Keep track of a node's interfaces
        nodesToInterfaces.get(srcId).add("");
        nodesToInterfaces.get(destId).add("");

        // Map interfaces to routers
        interfaceToNode.put(srcIp, srcId);
        interfaceToNode.put(destIp, destId);

        // Add edges
        edges.putIfAbsent(srcId, new HashSet<>());
        edges.putIfAbsent(destId, new HashSet<>());
        Set<String> neighbors = edges.get(srcId);
        neighbors.add(destId);

        // Add metric
        edgeCosts.putIfAbsent(srcIp, new HashMap<>());
        edgeCosts.putIfAbsent(destIp, new HashMap<>());
        Map<String, Float> costs = edgeCosts.get(srcIp);
        if (costs.containsKey(destIp)) {
            if (costs.get(destIp) > metric) {
                costs.put(destIp, metric);
            }
        } else {
            costs.put(destIp, metric);
        }
    }

    @Override
    public void removeNode(String nodeId) {
        assert(nodesToInterfaces.containsKey(nodeId));

        // Remove mapping from its interfaces
        for (String interfaceId : nodesToInterfaces.get(nodeId)) {
            interfaceToNode.remove(interfaceId);
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

    public IGPPath findShortestPathBetweenNodes(String srcRouterId, String dstRouterId) {

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
                return new IGPPath(srcRouterId, dstRouterId, path, distances.get(dstRouterId));
            }

            // Iterate through adjacent routers & interfaces, update distances if a shorter path is found
            for (String currentInterfaceId : nodesToInterfaces.get(currentRouterId)) {
                for (String nextInterfaceId : edgeCosts.get(currentInterfaceId).keySet()){
                    String nextRouterId = interfaceToNode.get(nextInterfaceId);
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
}
