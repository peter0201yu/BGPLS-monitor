package models.igp;

import models.bgpls.LinkNLRI;
import models.bgpls.NLRI;
import models.bgpls.NodeNLRI;
import models.bgpls.PrefixNLRI;
import util.Attribute;
import util.Pair;

import java.util.Map;

public abstract class IGPInstance {
    public Map<String, IGPNode> routers;
    public Map<Pair<String, String>, IGPLink> links;
    public Map<Pair<String, String>, IGPPrefix> prefixes;
    public Map<String, RoutingGraph> subgraphs;
    public abstract void handleNLRI(Attribute attributes, NLRI nlri);
    public abstract void getShortestPath(String ingressIp, String egressIp);
}
