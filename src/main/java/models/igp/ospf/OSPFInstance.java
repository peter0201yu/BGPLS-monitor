package models.igp.ospf;

import models.igp.IGPInstance;
import util.Attribute;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.Map;

public class OSPFInstance extends IGPInstance {
    Map<String, OSPFArea> subgraphs;

    HashMap<String, OSPFRouter> routers;
    HashMap<Pair<String, String>, OSPFLink> links;
    HashMap<String, Map<String, IGPPrefix>> prefixes;

    @Override
    private void addRouter(IGPNode router) {
        String routerId = router.id;
        if (router == null) {
            // Router doesn't exist
            router = new OSPFRouter(routerID, areaID);
            routers.put(routerID, router);
        } else {
            // Router already exists
            router.attributes.putAll(routerAttributes);
            router.areaIDs.add(areaID);
        }

        // Update attributes
        if (routerAttributes != null) {
            router.attributes.putAll(routerAttributes);
        }
    }

    @Override
    public void addLink(int srcID, int destID, Inet4Address srcInterface, Inet4Address destInterface, Attribute linkAttributes) {
        HashMap<Integer, OSPFLink> srcNeighbors = links.get(srcID);
        OSPFLink link;
        if (srcNeighbors == null || srcNeighbors.get(destID) == null) {
            // Link doesn't exist

            // 1. Create new link
            link = new OSPFLink(srcID, destID, srcInterface, destInterface);

            // 2. Add link to links
            if (srcNeighbors == null) {
                srcNeighbors = new HashMap<Integer, OSPFLink>();
                links.put(srcID, srcNeighbors);
            }
            srcNeighbors.put(destID, link);
        } else {
            // Link already exists
            link = srcNeighbors.get(destID);
        }

        if (linkAttributes != null) {
            link.attributes.putAll(linkAttributes);
        }
    }

    @Override
    private void addPrefix(int routerID, int areaID, String prefixStr, Attribute attributes) {
        OSPFPrefix prefix = prefixes.get(prefixStr);
        if (prefix == null) {
            // Prefix doesn't exist

            prefix = new OSPFPrefix();
            prefixes.put(prefixStr, prefix);
        }

        prefix.routerIDToAttributes.put(routerID, attributes);
        addRouter(routerID, areaID, null); // Create router if doesn't exist
        routers.get(routerID).reachablePrefixes.add(prefixStr);

        // TODO: Equivalence Class Handling
    }

    private void removeRouter(int routerID) {
        // TODO 1: remove router from routers
        // TODO 2: remove router from links
    }


}
