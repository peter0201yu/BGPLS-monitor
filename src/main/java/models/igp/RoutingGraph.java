package models.igp;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class RoutingGraph {
    public Map<String, Set<String>> nodesToInterfaces; // routerIds to interfaceIds
    public Map<String, String> interfaceToRouter; // interfaceIds to routerIds
    public Map<String, Set<String>> edges; // routerIds to routerIds
    public Map<String, Map<String, Float>> edgeCosts; // interfaceIds to interfaceIds to IGPMetric

    public RoutingGraph() {
        this.nodesToInterfaces = new HashMap<>();
        this.interfaceToRouter = new HashMap<>();
        this.edges = new HashMap<>();
        this.edgeCosts = new HashMap<>();
    }

    public abstract void addNode(String nodeId);
    public abstract void addEdge(String srcId, InetAddress srcAddress, String destId, InetAddress destAddress, Float metric);
    public abstract void removeNode(String nodeId);
    public abstract void removeEdge(String srcId, String destId);
}
