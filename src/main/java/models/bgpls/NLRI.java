package models.bgpls;

public abstract class NLRI {
    public String type;
    public int protocolId;
    public int instanceId;
    public abstract String getBgplsId();
    public abstract Integer getAs();
}
