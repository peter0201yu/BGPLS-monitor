import models.Topologies;
import models.bgpls.NLRI;
import models.bgpls.UpdateMessage;
import models.igp.IGPInstance;
import models.igp.ospf.OSPFInstance;
import parser.exabgp.ExabgpParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String resourceName = "dummydata/bgpls-examples.json";
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(resourceName);

        ExabgpParser parser = new ExabgpParser();
        Topologies topologies = new Topologies();
        try {
            List<UpdateMessage> messages = parser.readMessage(inputStream);
            for (UpdateMessage message : messages) {
                for (NLRI nlri : message.nlris) {
                    // TODO: refactor this code into separate function, also used in OSPFInstanceTest
                    IGPInstance topology = topologies.get(
                            nlri.getBgplsId(),
                            nlri.getAs(),
                            nlri.protocolId,
                            nlri.instanceId
                    );

                    if (topology == null) {
                        // TODO: better way of creating topologies when its not just ospf
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
        } catch (IOException e) {
            System.out.println("boo hoo");
        }

        // Test end to end same domain inter-area route retrieval and cost calculation
        String ingressNetwork = "192.168.0.10/32";
        String egressNetwork = "10.0.0.40/30";
        // TODO: need to map ip to topology.
        IGPInstance topology = topologies.get("0", 65530, 3, 0);
        topology.getShortestPath(ingressNetwork, egressNetwork);
    }
}
