import java.net.Inet4Address;
public class BGPLSPrefix {
    private Inet4Address ipReachability;
    private int numBitsPrefix; // Typically 24
    private BGPLSProtocolID protocol;

    public BGPLSPrefix(Inet4Address ipReachability, int numBitsPrefix, int protocolID) {
        this.ipReachability = ipReachability;
        this.numBitsPrefix = numBitsPrefix;
        this.protocol = BGPLSProtocolID.valueOf(protocolID);

    }
}
