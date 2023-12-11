import models.Topologies;
import models.bgpls.NLRI;
import models.bgpls.UpdateMessage;
import models.igp.IGPInstance;
import models.igp.ospf.*;
import parser.exabgp.ExabgpParser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class EndToEndTest {

    @Test
    public void BuildOSPFTopologyTest(){
        String resourceName = "dummydata/bgpls-examples.json";
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(resourceName);

        ExabgpParser parser = new ExabgpParser();
        Topologies topologies = new Topologies();
        try {
            List<UpdateMessage> messages = parser.readMessage(inputStream);
            for (UpdateMessage message : messages) {
                for (NLRI nlri : message.nlris) {
                    IGPInstance topology = topologies.get(
                            nlri.getBgplsId(),
                            nlri.getAs(),
                            nlri.protocolId,
                            nlri.instanceId
                    );

                    if (topology == null) {
                        topology = new OSPFInstance();
                        topologies.add(
                                nlri.getBgplsId(),
                                nlri.getAs(),
                                nlri.protocolId,
                                nlri.instanceId,
                                topology
                        );
                    }

                    topology.handleNLRI(message.attributes, nlri);
                }
            }
            assertEquals(1, topologies.size());
            OSPFInstance ospfInstance = (OSPFInstance) topologies.get("0", 65530, 3, 0);

            // print areas, nodes, links detail
            int linkCount = 0;
            for (String areaId : ospfInstance.subgraphs.keySet()){
                OSPFArea area = ospfInstance.subgraphs.get(areaId);
                assertEquals(areaId, area.areaId);
                System.out.println("Area: " + areaId + ": " + area.nodesToInterfaces.keySet().size() + " routers ");
                for (String routerId : area.nodesToInterfaces.keySet()){
                    Set<String> interfaces = area.nodesToInterfaces.get(routerId);
                    System.out.println("    Router " + routerId +
                            ": interfaces " + interfaces);
                    for (String srcInterface : interfaces){
                        for (String dstInterface : area.edgeCosts.get(srcInterface).keySet()){
                            System.out.println("        Edge: " + srcInterface + " -> " +
                                    dstInterface + ": cost " + area.edgeCosts.get(srcInterface).get(dstInterface));
                            linkCount++;
                        }
                    }
                    OSPFRouter router = ospfInstance.routers.get(routerId);
                    System.out.println("        Reachable prefix: " + router.reachablePrefixes);
                }
            }

            // link information
            System.out.println("Number of links: " + ospfInstance.links.keySet().size());
            assertEquals(linkCount, ospfInstance.links.keySet().size());

            System.out.println("======================================");

            // prefix reachability
            for (String prefixStr : ospfInstance.prefixes.keySet()){
                System.out.println("Prefix: " + prefixStr);
                OSPFPrefix prefix = ospfInstance.prefixes.get(prefixStr);
                for (String routerId : prefix.attributesForRouter.keySet()){
                    System.out.println("    Router " + routerId + ": " +
                            prefix.attributesForRouter.get(routerId).get("prefix-metric"));
                }
            }

            // test prefix trie
            // make sure all prefixes are inside the trie
            for (String prefixStr : ospfInstance.prefixes.keySet()){
                assertEquals(prefixStr, ospfInstance.prefixTrie.longestMatch(prefixStr));
            }
            assertEquals("10.0.0.48/30", ospfInstance.prefixTrie.longestMatch("10.0.0.48/32"));
            assertEquals("192.168.0.3/32", ospfInstance.prefixTrie.longestMatch("192.168.0.3/32"));
            assertEquals("192.168.1.0/24", ospfInstance.prefixTrie.longestMatch("192.168.1.0/30"));
            assertEquals("", ospfInstance.prefixTrie.longestMatch("10.0.0.0/32"));

        } catch (IOException e) {
            System.out.println("RIP");
        }
    }
}
