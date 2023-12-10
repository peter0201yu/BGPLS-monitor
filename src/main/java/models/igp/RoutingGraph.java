package models.igp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class RoutingGraph {
    public Set<String> nodes;
    public Map<String, Set<String>> edges;
    public RoutingGraph() {
        this.nodes = new HashSet<>();
        this.edges = new HashMap<>();
    }
    public abstract void addNode(String nodeId);
    public abstract void addEdge(String srcId, String destId);

    public abstract void removeNode(String nodeId);
    public abstract void removeEdge(String srcId, String destId);
}
