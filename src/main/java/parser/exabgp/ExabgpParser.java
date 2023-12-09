package parser.exabgp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import models.descriptors.NodeDescriptor;
import models.descriptors.LinkDescriptor;
import models.descriptors.PrefixDescriptor;
import parser.Parser;

public class ExabgpParser extends Parser {
    @Override
    public List<UpdateMessage> readMessage(InputStream input) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(input);

        List<UpdateMessage> messages = new ArrayList();
        if (rootNode.isArray()) {
            for (JsonNode message: rootNode) {
                UpdateMessage updateMessage = handleMessage(message);
                if (updateMessage != null) {
                    messages.add(updateMessage);
                }
            }
        } else {
            UpdateMessage updateMessage = handleMessage(rootNode);
            if (updateMessage != null) {
                messages.add(updateMessage);
            }
        }

        return messages;
    }

    private UpdateMessage handleMessage(JsonNode message) {
        String messageType = message.get("type").asText();
        if (messageType.equals("update")) {
            return readUpdate(message);
        } else {
            System.out.println("ERROR: Non-update BGP-LS messages aren't supported");
            return null;
        }
    }

    private UpdateMessage readUpdate(JsonNode message) {
        JsonNode neighbor = message.get("neighbor");
        JsonNode update = neighbor.get("message").get("update");
        JsonNode bgplsAnnouncement = update.get("announce").get("bgp-ls bgp-ls");

        // Assuming NLRIs only come from one peer, which is specified by peerAddress
        assert bgplsAnnouncement.size() == 1;

        String peerAddress = neighbor.get("address").get("peer").asText();
        JsonNode attributesJSON = update.get("attribute").get("bgp-ls");
        JsonNode nlriJSONs = bgplsAnnouncement.get(peerAddress);
        assert nlriJSONs.isArray();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> attributes = mapper.convertValue(attributesJSON, new TypeReference<Map<String, Object>>(){});
        List<NLRI> nlris = new ArrayList();
        for (JsonNode nlriJSON : nlriJSONs) {
            NLRI nlri = getNLRI(nlriJSON);
            nlris.add(nlri);
        }

        UpdateMessage updateMessage = new UpdateMessage();
        updateMessage.attributes = attributes;
        updateMessage.nlris = nlris;
        return updateMessage;
    }

    private NLRI getNLRI(JsonNode nlriJSON) {

        String type = nlriJSON.get("ls-nlri-type").asText();
        NLRI nlri;
        switch (type) {
            case "bgpls-node":
                nlri = getNodeNLRI(nlriJSON);
                break;
            case "bgpls-link":
                nlri = getLinkNLRI(nlriJSON);
                break;
            case "bgpls-prefix-v4":
                nlri = getPrefixNLRI(nlriJSON);
                break;
            default:
                System.out.println("ERROR: Unsupported BGP-LS NLRI type");
                return null;
        }
        nlri.type = type;
        nlri.protocolId = nlriJSON.get("protocol-id").asInt();
        nlri.instanceId = nlriJSON.get("l3-routing-topology").asInt();

        return nlri;
    }

    private NodeNLRI getNodeNLRI(JsonNode nlriJSON) {
        NodeNLRI node = new NodeNLRI();
        node.descriptor = getNodeDescriptor(nlriJSON, "node-descriptors");

        return node;
    }

    private LinkNLRI getLinkNLRI(JsonNode nlriJSON) {
        LinkNLRI link = new LinkNLRI();
        link.descriptor = getLinkDescriptor(nlriJSON);
        link.local = getNodeDescriptor(nlriJSON, "local-node-descriptors");
        link.remote = getNodeDescriptor(nlriJSON, "remote-node-descriptors");

        return link;
    }

    private PrefixNLRI getPrefixNLRI(JsonNode nlriJSON) {
        PrefixNLRI prefix = new PrefixNLRI();
        prefix.descriptor = getPrefixDescriptor(nlriJSON);
        prefix.local = getNodeDescriptor(nlriJSON, "node-descriptors");

        return prefix;
    }

    private NodeDescriptor getNodeDescriptor(JsonNode nlriJSON, String descriptorKey) {
        JsonNode nodeDescriptor = nlriJSON.get(descriptorKey);

        NodeDescriptor descriptor = new NodeDescriptor();
        descriptor.as = nodeDescriptor.get("autonomous-system").asInt();
        descriptor.bgplsId = nodeDescriptor.get("bgp-ls-identifier").asText();
        JsonNode ospfAreaIdNode = nodeDescriptor.get("ospf-area-id");
        if (ospfAreaIdNode != null) {
            descriptor.ospfAreaId = ospfAreaIdNode.asText();
        }
        descriptor.routerId = nodeDescriptor.get("router-id").asText();

        return descriptor;
    }

    private LinkDescriptor getLinkDescriptor(JsonNode nlriJSON) {
        LinkDescriptor descriptor = new LinkDescriptor();
        descriptor.ipv4Interface = nlriJSON.get("interface-address").get("interface-address").asText();
        descriptor.ipv4Neighbor = nlriJSON.get("neighbor-address").get("neighbor-address").asText();

        return descriptor;
    }

    private PrefixDescriptor getPrefixDescriptor(JsonNode nlriJSON) {
        String prefixString = nlriJSON.get("ip-reach-prefix").asText();
        String[] prefixParts = prefixString.split("/");

        PrefixDescriptor descriptor = new PrefixDescriptor();
        descriptor.ipPrefix = prefixParts[0];
        try {
            descriptor.prefixLength = Integer.parseInt(prefixParts[1]);
        } catch (NumberFormatException e) {
            System.out.println("ERROR: prefix length is not an integer");
        }
        descriptor.ospfRouteType = nlriJSON.get("ospf-route-type").asText();

        return descriptor;
    }
}
