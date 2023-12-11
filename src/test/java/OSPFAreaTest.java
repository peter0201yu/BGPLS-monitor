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
        OSPFArea area = new OSPFArea("0.0.0.0", true);

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
            OSPFPath ospfPath = area.findShortestPathBetweenRouters("192.168.0.1", "192.168.0.2");
            assertEquals(2, ospfPath.cost);
            assertEquals("192.168.0.1", ospfPath.path.get(0));
            assertEquals("192.168.0.2", ospfPath.path.get(1));

            // two hops
            ospfPath = area.findShortestPathBetweenRouters("192.168.0.2", "192.168.0.3");
            assertEquals(7, ospfPath.cost);
            assertEquals("192.168.0.1", ospfPath.path.get(1));

            // three hops
            ospfPath = area.findShortestPathBetweenRouters("192.168.0.2", "192.168.0.101");
            assertEquals(8, ospfPath.cost);
            assertEquals("192.168.0.1", ospfPath.path.get(1));
            assertEquals("192.168.0.3", ospfPath.path.get(2));

            // three hops
            ospfPath = area.findShortestPathBetweenRouters("192.168.0.1", "192.168.0.101");
            assertEquals(6, ospfPath.cost);
            assertEquals("192.168.0.3", ospfPath.path.get(1));

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception as needed
        }
    }

    @Test
    public void testBuildShortestPathTree(){
        OSPFArea area = new OSPFArea("0.0.0.0", true);

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

            // build shortest path tree from 192.168.0.1
            area.buildSpanningTree("192.168.0.1");
            OSPFShortestPathTree tree = area.areaBorderRouters.get("192.168.0.1");
            assertEquals("192.168.0.1", tree.parents.get("192.168.0.2"));
            assertEquals(2, tree.costs.get("192.168.0.2"));
            assertEquals("192.168.0.1", tree.parents.get("192.168.0.3"));
            assertEquals(5, tree.costs.get("192.168.0.3"));
            assertEquals("192.168.0.1", tree.parents.get("192.168.0.4"));
            assertEquals(4, tree.costs.get("192.168.0.4"));
            assertEquals("192.168.0.3", tree.parents.get("192.168.0.101"));
            assertEquals(6, tree.costs.get("192.168.0.101"));

            // build shortest path tree from 192.168.0.2
            area.buildSpanningTree("192.168.0.2");
            tree = area.areaBorderRouters.get("192.168.0.2");
            assertEquals("192.168.0.2", tree.parents.get("192.168.0.1"));
            assertEquals(2, tree.costs.get("192.168.0.1"));
            assertEquals("192.168.0.2", tree.parents.get("192.168.0.4"));
            assertEquals(5.5f, tree.costs.get("192.168.0.4"));
            assertEquals("192.168.0.1", tree.parents.get("192.168.0.3"));
            assertEquals(7, tree.costs.get("192.168.0.3"));
            assertEquals("192.168.0.3", tree.parents.get("192.168.0.101"));
            assertEquals(8, tree.costs.get("192.168.0.101"));
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception as needed
        }
    }
}
