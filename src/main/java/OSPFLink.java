import java.net.Inet4Address;

public class OSPFLink {
    int srcRouterID;
    int destRouterID;

    Inet4Address srcInterface;
    Inet4Address destInterface;
    Attribute attributes;
}
