package models.igp;

import java.net.InetAddress;
import java.util.*;

public abstract class RoutingGraph {
    public Map<String, Set<String>> nodesToInterfaces; // routerIds to interfaceIds
    public Map<String, String> interfaceToNode; // interfaceIds to routerIds
    public Map<String, Set<String>> edges; // routerIds to routerIds
    public Map<String, Map<String, Float>> edgeCosts; // interfaceIds to interfaceIds to IGPMetric

    public RoutingGraph() {
        this.nodesToInterfaces = new HashMap<>();
        this.interfaceToNode = new HashMap<>();
        this.edges = new HashMap<>();
        this.edgeCosts = new HashMap<>();
    }

    public abstract void addNode(String nodeId);
    public abstract void addEdge(String srcId, InetAddress srcAddress, String destId, InetAddress destAddress, float metric);
    public abstract void removeNode(String nodeId);
    public abstract void removeEdge(String srcId, String destId);

    // ---------------------------------------------
    // Find shortest path functions
    // ---------------------------------------------
    public IGPPath findShortestPathBetweenNodes(String srcNodeId, String dstNodeId) {
        System.out.println("Finding shortest path: " + srcNodeId + " -> " + dstNodeId);
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
        distances.put(srcNodeId, 0f);

        queue.add(srcNodeId);

        while (!queue.isEmpty()) {
            String currentRouterId = queue.poll();
            System.out.println("Curr: "+ currentRouterId);
            visited.add(currentRouterId);

            // If the destination router is reached, reconstruct the path and return the total cost
            if (currentRouterId.equals(dstNodeId)) {
                ArrayList<String> path = reconstructPath(previous, srcNodeId, dstNodeId);
                return new IGPPath(srcNodeId, dstNodeId, path, distances.get(dstNodeId));
            }

            // Iterate through adjacent routers & interfaces, update distances if a shorter path is found
            for (String currentInterfaceId : nodesToInterfaces.get(currentRouterId)) {
                if (edgeCosts.get(currentInterfaceId) == null){
                    System.out.println(currentRouterId + " " + currentInterfaceId);
                    continue;
                }
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

    // helper function for building list of intermediate nodes
    private ArrayList<String> reconstructPath(HashMap<String, String> previous, String srcNodeId, String dstNodeId) {
        ArrayList<String> path = new ArrayList<>();
        String current = dstNodeId;
        while (!current.equals(srcNodeId)) {
            path.add(current);
            current = previous.get(current);
        }
        path.add(srcNodeId);
        Collections.reverse(path);
        return path;
    }

    public IGPShortestPathTree buildSpanningTree(String rootNodeId) {
        // TODO: we need to dynamically call this whenever the topology changes (as nodesToInterfaces + edges come in)
        IGPShortestPathTree spfTree = new IGPShortestPathTree(rootNodeId);

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
        costs.put(rootNodeId, 0f);
        queue.add(rootNodeId);

        // Dijkstra's algorithm to build the spanning tree
        while (!queue.isEmpty()) {
            String currentRouterId = queue.poll();
            visited.add(currentRouterId);

            // Process the interfaces of the current router
            for (String currentInterfaceId : nodesToInterfaces.get(currentRouterId)) {
                for (String nextInterfaceId : edgeCosts.get(currentInterfaceId).keySet()){
                    String nextRouterId = interfaceToNode.get(nextInterfaceId);
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
        return spfTree;
    }

}
