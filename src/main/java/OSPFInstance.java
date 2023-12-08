import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;

public class OSPFInstance extends IGPInstance {

    HashMap<Integer, OSPFRouter> routers;
    HashMap<Integer, HashMap<Integer, OSPFRouter>> links;
    HashMap<String, OSPFPrefix> prefixes;
    @Override
    public void readUpdateMessage(JsonNode message) {

    }


}
