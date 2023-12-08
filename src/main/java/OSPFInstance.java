import com.fasterxml.jackson.databind.JsonNode;

import java.net.Inet4Address;
import java.util.HashMap;

public class OSPFInstance extends IGPInstance {

    HashMap<Integer, OSPFRouter> routers;
    HashMap<Integer, HashMap<Integer, OSPFLink>> links;
    HashMap<String, OSPFPrefix> prefixes;
    @Override
    public void readUpdateMessage(JsonNode message) {

    }

    private void addRouter(int routerID, int areaID, Attribute routerAttributes) {
        OSPFRouter router = routers.get(routerID);
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

    private void addLink(int srcID, int destID, Inet4Address srcInterface, Inet4Address destInterface, Attribute linkAttributes) {
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
