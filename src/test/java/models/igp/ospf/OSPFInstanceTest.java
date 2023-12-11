package models.igp.ospf;

import models.Topologies;
import models.bgpls.NLRI;
import models.bgpls.UpdateMessage;
import models.igp.IGPInstance;
import org.junit.jupiter.api.Test;
import parser.exabgp.ExabgpParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class OSPFInstanceTest {
    Topologies topologies = new Topologies();
    ExabgpParser parser = new ExabgpParser();

    private UpdateMessage loadResource(String resourceName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        List<UpdateMessage> messages = parser.readMessage(inputStream);
        assert(messages.size() == 1);
        UpdateMessage message = messages.get(0);

        for (NLRI nlri : message.nlris) {
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

        return message;
    }

    private IGPInstance getSingleTopology(UpdateMessage message) {
        assert(topologies.size() == 1);
        String bgplsId = message.nlris.get(0).getBgplsId();
        Integer as = message.nlris.get(0).getAs();
        Integer protocolId = message.nlris.get(0).protocolId;
        Integer instanceId = message.nlris.get(0).instanceId;

        return topologies.get(bgplsId, as, protocolId, instanceId);
    }

    @Test
    public void testHandleNLRIs() throws IOException {
        loadResource("dummydata/node-1.json");
        loadResource("dummydata/link-1.json");
        UpdateMessage message = loadResource("dummydata/prefix-1.json");

        IGPInstance topology = getSingleTopology(message);
        assert(topology instanceof OSPFInstance);

        OSPFInstance ospfTopology = (OSPFInstance) topology;
        assert(ospfTopology.routers.size() == 4);
        assert(ospfTopology.links.size() == 2);
        assert(ospfTopology.prefixes.size() == 1);

    }
}
