package models.igp.ospf;

import models.bgpls.LinkNLRI;
import models.bgpls.NLRI;
import models.bgpls.NodeNLRI;
import models.bgpls.PrefixNLRI;
import models.igp.IGPInstance;
import util.Attribute;
import util.Pair;

import java.util.HashMap;
import java.util.Map;

public class OSPFInstance extends IGPInstance {
    public Map<String, OSPFRouter> routers;
    public Map<Pair<String, String>, OSPFLink> links;
    public Map<String, OSPFPrefix> prefixes;
    public Map<String, OSPFArea> subgraphs;
    public OSPFInstance() {
        routers = new HashMap<>();
        links = new HashMap<>();
        prefixes = new HashMap<>();
        subgraphs = new HashMap<>();
    }

    @Override
    public void handleNLRI(Attribute attribute, NLRI nlri) {
        switch (nlri.type) {
            case "bgpls-node":
                addNode(attribute, (NodeNLRI) nlri);
                break;
            case "bgpls-link":
                addLink(attribute, (LinkNLRI) nlri);
                break;
            case "bgpls-prefix-v4":
                addPrefix(attribute, (PrefixNLRI) nlri);
                break;
            default:
                System.out.println("ERROR: OSFPInstance cannot parse this type of NLRI");
                return;
        }
        // TODO: handle unreach stuff
    }

    private void addNode(Attribute attributes, NodeNLRI nlri) {
        String routerId = nlri.descriptor.routerId;
        OSPFRouter router = routers.get(routerId);
        if (router == null) {
            router = new OSPFRouter(routerId);
            routers.put(routerId, router);
        }

        if (nlri.descriptor.ospfAreaId != null) {
            // Add areaId to list of areaIds in router
            String areaId = nlri.descriptor.ospfAreaId;
            router.addArea(areaId);

            // Add node to subgraph
            OSPFArea area = subgraphs.get(areaId);
            if (area == null) {
                area = new OSPFArea(areaId, false);
                subgraphs.put(areaId, area);
            }
            area.addNode(routerId);
        }
        router.setAttributes(attributes);
    }

    private void addLink(Attribute attributes, LinkNLRI nlri) {
        assert(attributes.containsKey("igp-metric"));
        Pair<String, String> linkKey = new Pair<>(nlri.local.routerId, nlri.remote.routerId);
        OSPFLink link = links.get(linkKey);
        if (link == null) {
            link = new OSPFLink(
                    nlri.local.routerId,
                    nlri.remote.routerId,
                    nlri.descriptor.interfaceAddress,
                    nlri.descriptor.neighborAddress
            );
            links.put(linkKey, link);
        }

        link.setAttributes(attributes);

        if (nlri.local.ospfAreaId != null && nlri.remote.ospfAreaId != null && nlri.local.ospfAreaId.equals(nlri.remote.ospfAreaId)) {
            // Add edge to subgraph
            String areaId = nlri.local.ospfAreaId;
            OSPFArea area = subgraphs.get(areaId);
            if (area != null) {
                area.addEdge(nlri.local.routerId, nlri.descriptor.interfaceAddress, nlri.remote.routerId, nlri.descriptor.neighborAddress, (Float) attributes.get("IGP-metric"));
            }
        }
    }

    private void addPrefix(Attribute attributes, PrefixNLRI nlri) {
        String prefixStr = nlri.descriptor.ipPrefix + '/' + nlri.descriptor.prefixLength;
        OSPFPrefix prefix = prefixes.get(prefixStr);
        if (prefix == null) {
            prefix = new OSPFPrefix();
            prefixes.put(prefixStr, prefix);
        }

        prefix.setAttributes(attributes);
        prefix.addRouter(nlri.local.routerId);

        // Keeping track of reachable prefixes in routerId
        OSPFRouter router = routers.get(nlri.local.routerId);
        assert(router != null);
        router.addReachablePrefix(prefixStr);

        // TODO: Equivalence Class Handling
    }

    private void removeNode(String routerId) {
        routers.remove(routerId);
    }

    private void removeLink(String srcId, String destId) {
        Pair<String, String> linkKey = new Pair<>(srcId, destId);
        links.remove(linkKey);
    }

    private void removePrefix(String prefix, String routerId) {
        Pair<String, String> prefixKey = new Pair<>(prefix, routerId);
        prefixes.remove(prefixKey);
    }
}
