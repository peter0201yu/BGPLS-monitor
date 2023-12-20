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
        nodesToInterfaces.get(srcId).add(srcIp);
        nodesToInterfaces.get(destId).add(destIp);

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
}
