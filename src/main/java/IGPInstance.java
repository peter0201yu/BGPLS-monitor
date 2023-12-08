import com.fasterxml.jackson.databind.JsonNode;

abstract class IGPInstance {
    // read message and build topology
    public abstract void readUpdateMessage(JsonNode message);
}
