package models.igp.ospf;

import models.igp.RoutingGraph;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class OSPFArea extends RoutingGraph {
    public OSPFArea() {
        super();
    }

    @Override
    public void addNode(String nodeId) {
        nodes.add(nodeId);
    }

    @Override
    public void addEdge(String srcId, InetAddress srcAddress, String destId, InetAddress destAddress) {
        assert(nodes.contains(srcId));
        assert(nodes.contains(destId));

        edges.putIfAbsent(srcId, new HashSet());
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
}
