package models.igp.ospf;

import util.Attribute;

import java.net.Inet4Address;

public class OSPFLink {
    int srcRouterID;
    int destRouterID;

    Inet4Address srcInterface;
    Inet4Address destInterface;
    Attribute attributes = new Attribute();

    public OSPFLink(int srcID, int destID, Inet4Address srcInterface, Inet4Address destInterface) {
        this.srcRouterID = srcID;
        this.destRouterID = destID;
        this.srcInterface = srcInterface;
        this.destInterface = destInterface;
    }
}
