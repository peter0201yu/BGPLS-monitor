import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;

public class OSPFRoutingUniverse extends IGPInstance{
    int id;
    ArrayList<OSPFArea> areas;
    public OSPFRoutingUniverse(int id) {
        this.id = id;
        // Initialize other fields if needed
        this.areas = new ArrayList<>();
    }
    @Override
    public void readUpdateMessage(JsonNode message) {
        // build topology
    }
}
