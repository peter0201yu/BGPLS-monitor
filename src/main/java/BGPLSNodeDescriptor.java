import java.net.Inet4Address;

public class BGPLSNodeDescriptor {
    int autonomousSystemID;
    int BGPLSIdentifier;
    Inet4Address OSPFAreaID;
    Inet4Address routerID;

    public BGPLSNodeDescriptor(int autonomousSystemID, int BGPLSIdentifier, Inet4Address OSPFAreaID, Inet4Address RouterID) {
        this.autonomousSystemID = autonomousSystemID;
        this.BGPLSIdentifier = BGPLSIdentifier;
        this.OSPFAreaID = OSPFAreaID;
        this.routerID = RouterID;
    }
}
