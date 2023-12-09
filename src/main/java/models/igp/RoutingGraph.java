package models.igp;

import java.util.Map;
import java.util.Set;

public abstract class RoutingGraph {
    Set<String> nodes;
    Map<String, Set<String>> edges;
}
