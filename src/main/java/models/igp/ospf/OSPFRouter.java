package models.igp.ospf;

import models.igp.IGPNode;
import util.Attribute;

import java.util.*;

public class OSPFRouter extends IGPNode {
    public Set<String> areaIds;
    public List<String> reachablePrefixes;

    public OSPFRouter(String routerId) {
        super(routerId);
        this.areaIds = new HashSet();
        this.reachablePrefixes = new ArrayList<>();
    }

    public void addArea(String areaId) {
        this.areaIds.add(areaId);
    }

    public void addReachablePrefix(String prefix) {
        this.reachablePrefixes.add(prefix);
    }

    public void removeReachablePrefix(String prefix) {
        this.reachablePrefixes.remove(prefix);
    }
}
