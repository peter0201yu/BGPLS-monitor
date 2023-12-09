package models.igp;

import util.Pair;

import java.util.Map;

public abstract class IGPInstance {
    Map<String, RoutingGraph> subgraphs;
    Map<String, IGPNode> routers;
    Map<Pair<String, String>, IGPLink> links;
    Map<String, Map<String, IGPPrefix>> prefixes;
    public abstract void addNode(IGPNode router);
    public abstract void addLink(IGPLink router);
    public abstract void addPrefix(IGPPrefix prefix);
    public abstract void removeNode(String routerId);
    public abstract void removeLink(String srcId, String destId);
    public abstract void removePrefix(String prefix, String routerId);
}
