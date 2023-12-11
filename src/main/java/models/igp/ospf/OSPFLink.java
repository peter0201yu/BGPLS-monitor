package models.igp.ospf;

import models.igp.IGPLink;
import util.Attribute;

import java.net.InetAddress;

public class OSPFLink extends IGPLink {
    public InetAddress srcInterface;
    public InetAddress destInterface;

    public OSPFLink(String srcId, String destId, InetAddress interfaceAddress, InetAddress neighborAddress) {
        super(srcId, destId);
        this.srcInterface = interfaceAddress;
        this.destInterface = neighborAddress;
    }
}
