package models.igp.ospf;

import models.igp.IGPPrefix;

import java.util.HashSet;
import java.util.Set;

public class OSPFPrefix extends IGPPrefix {
    public Set<String> routerIds;

    public OSPFPrefix() {
        this.routerIds = new HashSet<>();
    }

    public void addRouter(String routerId) {
        routerIds.add(routerId);
    }
}
