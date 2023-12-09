import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OSPFAreaTest {
    @Test
    public void testFindShortestPathBetweenRouters(){
        OSPFArea area = new OSPFArea();

        String IGPMetricCostsFile = "dummydata/area-1-IGPMetricsCosts.json";
        String interfaceToRouterFile = "dummydata/area-1-interfaceToRouter.json";
        String routerToInterfacesFile = "dummydata/area-1-routerToInterfaces.json";

        URL IGPMetricCostsUrl = getClass().getClassLoader().getResource(IGPMetricCostsFile);
        URL interfaceToRouterUrl = getClass().getClassLoader().getResource(interfaceToRouterFile);
        URL routerToInterfacesUrl = getClass().getClassLoader().getResource(routerToInterfacesFile);

        ObjectMapper mapper = new ObjectMapper();
        try {
            area.IGPMetricCosts = mapper.readValue(
                    new File(IGPMetricCostsUrl.getFile()),
                    new TypeReference<HashMap<String, HashMap<String, Float>>>() {}
            );
            area.routerToInterfaces = mapper.readValue(
                    new File(routerToInterfacesUrl.getFile()),
                    new TypeReference<HashMap<String, ArrayList<String>>>() {}
            );
            area.interfaceToRouter = mapper.readValue(
                    new File(interfaceToRouterUrl.getFile()),
                    new TypeReference<HashMap<String, String>>() {}
            );

            // one hop
            ArrayList<String> path1 = new ArrayList<>();
            Float cost = area.findShortestPathBetweenRouters("192.168.0.1", "192.168.0.2", path1);
            assertEquals(2, cost);
            assertEquals("192.168.0.1", path1.get(0));
            assertEquals("192.168.0.2", path1.get(1));

            // two hops
            ArrayList<String> path2 = new ArrayList<>();
            cost = area.findShortestPathBetweenRouters("192.168.0.2", "192.168.0.3", path2);
            assertEquals(7, cost);
            assertEquals("192.168.0.1", path2.get(1));

            // three hops
            ArrayList<String> path3 = new ArrayList<>();
            cost = area.findShortestPathBetweenRouters("192.168.0.2", "192.168.0.101", path3);
            assertEquals(8, cost);
            assertEquals("192.168.0.1", path3.get(1));
            assertEquals("192.168.0.3", path3.get(2));

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception as needed
        }
    }
}
