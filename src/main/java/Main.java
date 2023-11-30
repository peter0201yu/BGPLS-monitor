import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Load the resource as a stream
        String resourceName = "dummydata/bgpls-examples.json";
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(resourceName);

        if (inputStream != null) {
            try {
                // Parse the JSON content
                JsonNode jsonNode = objectMapper.readTree(inputStream);

                // Use the parsed JSON content
                // Example: Print the content of the JSON
                System.out.println("JSON content:");
                System.out.println(jsonNode.toPrettyString()); // Print JSON content

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
