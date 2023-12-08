import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        OSPFRoutingUniverse universe = new OSPFRoutingUniverse(0);

        // Load the resource as a stream
        ObjectMapper objectMapper = new ObjectMapper();
        String resourceName = "dummydata/bgpls-examples.json";
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(resourceName);
        if (inputStream != null) {
            try {
                // Parse the JSON content
                JsonNode rootNode = objectMapper.readTree(inputStream);
                if (rootNode.isArray()) {
                    for (JsonNode message : rootNode) {
                        universe.readUpdateMessage(message);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close(); // Close the input stream
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Resource not found: " + resourceName);
        }
    }
}
