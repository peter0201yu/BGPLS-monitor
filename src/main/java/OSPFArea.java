import java.util.*;

public class OSPFArea {
    public String areaId;
    public boolean isBackbone;
    public ArrayList<OSPFRouter> areaBorderRouters;

    // This stores the cost of each link:
    // srcInterfaceId => (dstInterfaceId => IGPMetric)
    public HashMap<String, HashMap<String, Float>> IGPMetricCosts;

    // Create these two maps for quick lookup
    public HashMap<String, ArrayList<String>> routerToInterfaces;
    public HashMap<String, String> interfaceToRouter;

    public void populateIGPMetricCosts(){
        // TODO: build from routing graph
    }

    public void populateRouterToInterfaces(){
        // TODO: build using OSPF instance info
    }

    public void populateInterfaceToRouter(){
        // TODO: build using OSPF instance info
    }

    // Dijkstra's algorithm, with many routerId <=> interfaceId lookups
    public Float findShortestPathBetweenRouters(String srcRouterId, String dstRouterId, ArrayList<String> path) {

        // Router => distance from srcRouter
        HashMap<String, Float> distances = new HashMap<>();
        // Router => previous router on the way from srcRouter
        HashMap<String, String> previous = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>((router1, router2) -> {
            float distance1 = distances.getOrDefault(router1, Float.MAX_VALUE);
            float distance2 = distances.getOrDefault(router2, Float.MAX_VALUE);
            return Float.compare(distance1, distance2);
        });

        // Initialize distances
        for (String routerId : routerToInterfaces.keySet()) {
            distances.put(routerId, Float.MAX_VALUE);
            previous.put(routerId, null);
        }
        distances.put(srcRouterId, 0f);

        queue.add(srcRouterId);

        while (!queue.isEmpty()) {
            String currentRouterId = queue.poll();

            // If the destination router is reached, reconstruct the path and return the total cost
            if (currentRouterId.equals(dstRouterId)) {
                reconstructPath(previous, srcRouterId, dstRouterId, path);
                return distances.get(dstRouterId);
            }

            // Iterate through adjacent routers & interfaces, update distances if a shorter path is found
            for (String currentInterfaceId : routerToInterfaces.get(currentRouterId)) {
                for (String nextInterfaceId : IGPMetricCosts.get(currentInterfaceId).keySet()){
                    String nextRouterId = interfaceToRouter.get(nextInterfaceId);
                    Float edgeCost = IGPMetricCosts.get(currentInterfaceId).getOrDefault(nextInterfaceId, Float.MAX_VALUE);
                    Float newDistance = distances.get(currentRouterId) + edgeCost;
                    if (newDistance < distances.get(nextRouterId)) {
                        distances.put(nextRouterId, newDistance);
                        previous.put(nextRouterId, currentRouterId);
                        if (!queue.contains(nextRouterId)) {
                            queue.add(nextRouterId);
                        }
                    }
                }
            }
        }
        return -1f; // Destination router is unreachable
    }

    private void reconstructPath(HashMap<String, String> previous, String srcRouterId, String dstRouterId, ArrayList<String> path) {
        String current = dstRouterId;
        while (!current.equals(srcRouterId)) {
            path.add(current);
            current = previous.get(current);
        }
        path.add(srcRouterId);
        Collections.reverse(path);
    }
}
