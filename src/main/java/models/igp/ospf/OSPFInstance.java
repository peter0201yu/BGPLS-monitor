package models.igp.ospf;

import models.bgpls.LinkNLRI;
import models.bgpls.NLRI;
import models.bgpls.NodeNLRI;
import models.bgpls.PrefixNLRI;
import models.bgpls.descriptors.NodeDescriptor;
import models.igp.IGPInstance;
import models.igp.IGPPath;
import util.Attribute;
import util.Pair;
import util.Triple;
import java.util.*;

public class OSPFInstance extends IGPInstance {
    public Map<String, OSPFRouter> routers;
    public Map<Pair<String, String>, OSPFLink> links;
    public Map<String, OSPFPrefix> prefixes;
    public Map<String, OSPFArea> subgraphs;
    public PrefixTrie prefixTrie;
    private Set<String> backboneAbrs;
    private OSPFSummaryGraph summaryGraph;

    public OSPFInstance() {
        routers = new HashMap<>();
        links = new HashMap<>();
        subgraphs = new HashMap<>();
        prefixes = new HashMap<>();
        prefixTrie = new PrefixTrie();
        backboneAbrs = new HashSet<>();
        summaryGraph = new OSPFSummaryGraph();
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

            if (areaId.equals("0.0.0.0")) {
                // TODO: is backbone 0.0.0.0 for non-exabgp messages?
                backboneAbrs.add(routerId);
            }

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
            area.addEdge(nlri.local.routerId, nlri.descriptor.interfaceAddress, nlri.remote.routerId, nlri.descriptor.neighborAddress, (float) ((Integer) attributes.get("igp-metric")));

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
        prefix.addRouter(routerId, attributes);

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
        prefix.removeRouter(routerId);
        routers.get(routerId).removeReachablePrefix(prefixStr);
        prefixTrie.delete(prefixStr);
    }

    public void getShortestPath(String ingressNetwork, String egressNetwork) {
        String ingressPrefixStr = prefixTrie.longestMatch(ingressNetwork);
        String egressPrefixStr = prefixTrie.longestMatch(egressNetwork);
        OSPFPrefix ingressPrefix = prefixes.get(ingressPrefixStr);
        OSPFPrefix egressPrefix = prefixes.get(egressPrefixStr);
        Set<String> ingressRouterIds = ingressPrefix.attributesForRouter.keySet();
        Set<String> egressRouterIds = egressPrefix.attributesForRouter.keySet();

        System.out.println("ingress prefix: " + ingressNetwork + ", " + ingressPrefixStr);
        System.out.println("egress prefix: " + egressNetwork + ", " + egressPrefixStr);
        for (String ingressRouterId : ingressRouterIds) {
            System.out.println("ingress: " + ingressRouterId);
        }
        for (String egressRouterId : egressRouterIds) {
            System.out.println("egress: " + egressRouterId);
        }

        // 1. Add a dummy node for the ingress and egress prefix
        summaryGraph.addNode(ingressNetwork);
        summaryGraph.addNode(egressNetwork);

        // 2. add edges from ingress prefix to ingress routers. Also keep track of mapping from areaId to reachableIngressRouters
        Map<String, Set<String>> areaToIngressRouter = new HashMap<>(); // ingressRouterId, areaId
        for (String ingressRouterId : ingressRouterIds) {
            float cost = (float) ((Integer) ingressPrefix.attributesForRouter.get(ingressRouterId).get("prefix-metric"));
            summaryGraph.addNode(ingressRouterId);
            summaryGraph.addEdge(ingressNetwork, ingressRouterId, cost);
            summaryGraph.addEdge(ingressRouterId, ingressNetwork, cost);

            OSPFRouter ingressRouter = routers.get(ingressRouterId);
            for (String areaId : ingressRouter.areaIds) {
                areaToIngressRouter.putIfAbsent(areaId, new HashSet<>());
                Set<String> routersInArea = areaToIngressRouter.get(areaId);
                routersInArea.add(ingressRouterId);
            }
        }
        // print areaToIngressRouter
        for (String areaId : areaToIngressRouter.keySet()) {
            System.out.println("bbbbb: " + areaId);
            for (String ingressRouterId : areaToIngressRouter.get(areaId)) {
                System.out.println("aaaaa: " + ingressRouterId);
            }
        }

        // 3. Find ingress ABRs that connect to backbone and a ingress router area. Map from ingressABRId to ingressRouterId to areaId
        Set<Triple<String, String, String>> ingressTriples = new HashSet<>(); // ingressRouterId, ingressAbrId, areaId
        for (String ingressAbrId : backboneAbrs) {
            OSPFRouter ingressAbr = routers.get(ingressAbrId);
            for (String areaId : ingressAbr.areaIds) {
                if (areaToIngressRouter.containsKey(areaId)) {
                    Set<String> areaRouterIds = areaToIngressRouter.get(areaId);
                    for (String ingressRouterId : areaRouterIds) {
                        ingressTriples.add(new Triple<>(ingressRouterId, ingressAbrId, areaId));
                    }
                }
            }
        }

        // 4. Add ingress ABR node and edges to summary graph
        for (Triple<String, String, String> ingressTriple : ingressTriples) {
            String ingressRouterId = ingressTriple.first;
            String ingressAbrId = ingressTriple.second;
            String areaId = ingressTriple.third;

            OSPFArea area = subgraphs.get(areaId);
            IGPPath path = area.findShortestPathBetweenNodes(ingressRouterId, ingressAbrId);

            summaryGraph.addNode(ingressAbrId);
            System.out.println("ingress edge: " + ingressRouterId + " -> " + ingressAbrId + ", cost: " + path.cost);
            summaryGraph.updateEdgeIfSmaller(ingressRouterId, ingressAbrId, path.cost);
        }
        
        // 5. add edges from egress prefix to egress routers. Also keep track of mapping from areaId to reachableEgressRouters
        Map<String, Set<String>> areaToEgressRouter = new HashMap<>(); // egressRouterId, areaId
        for (String egressRouterId : egressRouterIds) {
            float cost = (float) ((Integer) egressPrefix.attributesForRouter.get(egressRouterId).get("prefix-metric"));
            summaryGraph.addNode(egressRouterId);
            summaryGraph.addEdge(egressNetwork, egressRouterId, cost);
            summaryGraph.addEdge(egressRouterId, egressNetwork, cost);

            OSPFRouter egressRouter = routers.get(egressRouterId);
            for (String areaId : egressRouter.areaIds) {
                areaToEgressRouter.putIfAbsent(areaId, new HashSet<>());
                Set<String> routersInArea = areaToEgressRouter.get(areaId);
                routersInArea.add(egressRouterId);
            }
        }

        // 6. Find egress ABRs that connect to backbone and a egress router area. Map from egressABRId to egressRouterId to areaId
        Set<Triple<String, String, String>> egressTriples = new HashSet<>(); // egressRouterId, egressAbrId, areaId
        for (String egressAbrId : backboneAbrs) {
            OSPFRouter egressAbr = routers.get(egressAbrId);
            for (String areaId : egressAbr.areaIds) {
                if (areaToEgressRouter.containsKey(areaId)) {
                    Set<String> areaRouterIds = areaToEgressRouter.get(areaId);
                    for (String egressRouterId : areaRouterIds) {
                        egressTriples.add(new Triple<>(egressRouterId, egressAbrId, areaId));
                    }
                }
            }
        }

        // 7. Add egress ABR node and edges to summary graph
        for (Triple<String, String, String> egressTriple : egressTriples) {
            String egressRouterId = egressTriple.first;
            String egressAbrId = egressTriple.second;
            String areaId = egressTriple.third;

            OSPFArea area = subgraphs.get(areaId);
            IGPPath path = area.findShortestPathBetweenNodes(egressRouterId, egressAbrId);

            summaryGraph.addNode(egressAbrId);
            System.out.println("egress edge: " + egressRouterId + " -> " + egressAbrId + ", cost: " + path.cost);
            summaryGraph.updateEdgeIfSmaller(egressRouterId, egressAbrId, path.cost);
        }

        // // Find shortest path from ingressRouter --> egressRouter
        // Pair<String, IGPPath> minPath = null;
        // for (String possibleIngressAbr : ingressTriples.keySet()) {
        //     for (String possibleEgressAbr : egressAbrs.keySet()) {
        //         IGPPath path = IGPPath.join(
        //                 ingressToIngressAbr.get(possibleIngressAbr),
        //                 ingressAbrToEgressAbr.get(possibleIngressAbr).get(possibleEgressAbr),
        //                 egressAbrToEgress.get(possibleEgressAbr)
        //         );
        //         if (minPath == null || path.cost < minPath.value().cost) {
        //             minPath = new Pair<>(possibleIngressAbr, path);
        //         }
        //     }
        // }

        // // Print path
        // if (minPath != null) {
        //     System.out.println("Shortest path from " + ingressNetwork + " to " + egressNetwork + " is:");
        //     for (String routerId : minPath.value().path) {
        //         System.out.println(routerId);
        //     }
        //     System.out.println("Cost: " + minPath.value().cost);
        // }
    }
}
