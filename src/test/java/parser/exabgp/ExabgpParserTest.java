package parser.exabgp;

import models.bgpls.LinkNLRI;
import models.bgpls.NodeNLRI;
import models.bgpls.PrefixNLRI;
import models.bgpls.UpdateMessage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExabgpParserTest {

    @Test
    public void testParseNodeUpdate(){
        String resourceName = "dummydata/node-1.json";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        ExabgpParser parser = new ExabgpParser();
        try {
            List<UpdateMessage> messages = parser.readMessage(inputStream);
            // 6 node nlris
            assertEquals(6, messages.get(0).nlris.size());
            // downcast
            NodeNLRI nlri = (NodeNLRI) messages.get(0).nlris.get(0);
            assertEquals("192.168.0.2", nlri.descriptor.routerId);
            // 5 bgp-ls attributes
            assertEquals(5, messages.get(0).attributes.size());
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testParseLinkUpdate(){
        String resourceName = "dummydata/link-1.json";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        ExabgpParser parser = new ExabgpParser();
        try {
            List<UpdateMessage> messages = parser.readMessage(inputStream);
            // downcast
            LinkNLRI nlri = (LinkNLRI) messages.get(0).nlris.get(0);
            assertEquals("10.0.0.50", nlri.descriptor.ipv4Interface);
            assertEquals(1, messages.get(0).attributes.get("igp-metric"));
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testParsePrefixUpdate(){
        String resourceName = "dummydata/prefix-1.json";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        ExabgpParser parser = new ExabgpParser();
        try {
            List<UpdateMessage> messages = parser.readMessage(inputStream);
            // downcast
            PrefixNLRI nlri = (PrefixNLRI) messages.get(0).nlris.get(0);
            assertEquals("192.168.0.2", nlri.descriptor.ipPrefix);
            assertEquals(32, nlri.descriptor.prefixLength);
            assertEquals(1, messages.get(0).attributes.get("prefix-metric"));
        } catch (IOException e) {
            assertTrue(false);
        }
    }
}
