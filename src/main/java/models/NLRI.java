package models;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class NLRI {
    public String type;
    public int protocolId;
    public int instanceId;
}
