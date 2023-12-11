package models.igp.ospf;

import models.bgpls.LinkNLRI;
import models.bgpls.NLRI;
import models.bgpls.NodeNLRI;
import models.bgpls.PrefixNLRI;
import models.bgpls.descriptors.NodeDescriptor;
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
    public PrefixTrie prefixTrie;
    public OSPFInstance() {
        routers = new HashMap<>();
        links = new HashMap<>();
        subgraphs = new HashMap<>();
        prefixes = new HashMap<>();
        prefixTrie = new PrefixTrie();
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
            if (area == null) {
                area = new OSPFArea(areaId, false);
                subgraphs.put(areaId, area);
            }

            // add node first
            NodeNLRI nodeNLRI1 = new NodeNLRI();
            nodeNLRI1.descriptor = new NodeDescriptor();
            nodeNLRI1.descriptor.routerId = nlri.local.routerId;
            nodeNLRI1.descriptor.ospfAreaId = areaId;
            addNode(new Attribute(), nodeNLRI1);

            NodeNLRI nodeNLRI2 = new NodeNLRI();
            nodeNLRI2.descriptor = new NodeDescriptor();
            nodeNLRI2.descriptor.routerId = nlri.remote.routerId;
            nodeNLRI2.descriptor.ospfAreaId = areaId;
            addNode(new Attribute(), nodeNLRI2);

            float cost = (float) ((Integer) attributes.get("igp-metric"));

            area.addEdge(nlri.local.routerId, nlri.descriptor.interfaceAddress, nlri.remote.routerId,
                    nlri.descriptor.neighborAddress, cost);

        }
    }

    private void addPrefix(Attribute attributes, PrefixNLRI nlri) {
        String prefixStr = nlri.descriptor.ipPrefix + '/' + nlri.descriptor.prefixLength;
        OSPFPrefix prefix = prefixes.get(prefixStr);
        if (prefix == null) {
            prefix = new OSPFPrefix();
            prefixes.put(prefixStr, prefix);
        }

        String routerId = nlri.local.routerId;
        // Keeping track of connected routers and connection attributes of the prefix
        prefix.attributesForRouter.put(routerId, attributes);

        // Keeping track of reachable prefixes in routerId
        OSPFRouter router = routers.get(routerId);
        if (router == null) {
            // handle the case where the router is not added before the link
            NodeNLRI nodeNLRI = new NodeNLRI();
            nodeNLRI.descriptor = new NodeDescriptor();
            nodeNLRI.descriptor.routerId = routerId;
            nodeNLRI.descriptor.ospfAreaId = nlri.local.ospfAreaId;
            addNode(new Attribute(), nodeNLRI);
            router = routers.get(routerId);
        }
        assert(router != null);
        router.addReachablePrefix(prefixStr);

        // Add prefix to trie
        prefixTrie.insert(prefixStr);
    }

    private void removeNode(String routerId) {
        routers.remove(routerId);
    }

    private void removeLink(String srcId, String destId) {
        Pair<String, String> linkKey = new Pair<>(srcId, destId);
        links.remove(linkKey);
    }

    private void removePrefix(String prefixStr, String routerId) {
        // go into prefix to remove router
        OSPFPrefix prefix = prefixes.get(prefixStr);
        prefix.attributesForRouter.remove(routerId);
        routers.get(routerId).removeReachablePrefix(prefixStr);
        prefixTrie.delete(prefixStr);
    }
}
