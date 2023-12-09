import com.fasterxml.jackson.databind.ObjectMapper;
import models.UpdateMessage;
import parser.exabgp.ExabgpParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String resourceName = "dummydata/bgpls-examples.json";
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(resourceName);

        ExabgpParser parser = new ExabgpParser();
        try {
            List<UpdateMessage> messages = parser.readMessage(inputStream);
        } catch (IOException e) {
            System.out.println("boo hoo");
        }
    }
}
