import java.lang.reflect.Array;
import java.util.ArrayList;

public class BGPLSNode {
    ArrayList<BGPLSPrefix> reachablePrefixes;
    ArrayList<BGPLSLink> outboundLinks;
    BGPLSNodeDescriptor nodeDescriptor;
    int l3RoutingTopology;
    BGPLSProtocolID protocol;

    BGPLSNode(int protocolID, int l3RoutingTopology, BGPLSNodeDescriptor nodeDescriptor) {
        this.protocol = BGPLSProtocolID.valueOf(protocolID);
        this.nodeDescriptor = nodeDescriptor;
        this.l3RoutingTopology = l3RoutingTopology;
    }

    void addPrefix() {

    }
}
