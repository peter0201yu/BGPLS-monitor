import java.net.Inet4Address;

public class BGPLSLink {
    int l3RoutingTopology;
    int linkCostMetric;
    BGPLSProtocolID protocol;

    BGPLSNodeDescriptor localNodeDescriptor;
    BGPLSNodeDescriptor remoteNoteDescriptor;

    Inet4Address interfaceAddress;
    Inet4Address neighborAddress;

    BGPLSLink(int protocolID, int l3RoutingTopology, int linkMetric,
              BGPLSNodeDescriptor localNodeDescriptor, BGPLSNodeDescriptor remoteNodeDescriptor,
              Inet4Address interfaceAddress, Inet4Address neighborAddress) {
        this.protocol = BGPLSProtocolID.valueOf(protocolID);
        this.l3RoutingTopology = l3RoutingTopology;
        this.localNodeDescriptor = localNodeDescriptor;
        this.remoteNoteDescriptor = remoteNodeDescriptor;
        this.interfaceAddress = interfaceAddress;
        this.neighborAddress = neighborAddress;
        this.linkCostMetric = linkMetric;
    }
}
