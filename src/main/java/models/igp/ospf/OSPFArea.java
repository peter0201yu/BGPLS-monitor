package models.igp.ospf;

import models.igp.IGPShortestPathTree;
import models.igp.RoutingGraph;
import util.Pair;

import java.net.InetAddress;
import java.util.*;

public class OSPFArea extends RoutingGraph {
    public String areaId;
    public boolean isBackbone;
    public HashMap<String, IGPShortestPathTree> areaBorderRouters;

    public OSPFArea(String _areaId, boolean _isBackbone) {
        super();
        areaId = _areaId;
        isBackbone = _isBackbone;
        areaBorderRouters = new HashMap<String, IGPShortestPathTree>();
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

    // compute the shortest paths from one abr to all other routers
    public void computePathsForABR(String abrId){
        areaBorderRouters.put(abrId, this.buildSpanningTree(abrId));
    }
}
