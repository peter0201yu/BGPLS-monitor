import java.util.*;


public class OSPFShortestPathTree {
    // typically root router is ABR
    public String rootRouterId;
    // spanning tree: <child, parent>
    public HashMap<String, String> parents;
    // spanning tree costs: <child, cost>
    public HashMap<String, Float> costs;

    public OSPFShortestPathTree(String _rootRouterId) {
        rootRouterId = _rootRouterId;
        parents = new HashMap<>();
        costs = new HashMap<>();
        costs.put(rootRouterId, 0f);
    }

    // Add router to the shortest path tree
    public void addToTree(String childId, String parentId, Float cost) {
        parents.put(childId, parentId);
        costs.put(childId, cost);
    }

    // get path based on spanning tree
    public OSPFPath getPath(String dstRouterId){
        String currRouterId = dstRouterId;
        ArrayList<String> path = new ArrayList<>();

        while (!currRouterId.equals(rootRouterId)) {
            String parentId = parents.get(currRouterId);
            path.add(currRouterId); // Add the router to the path
            currRouterId = parentId;
        }
        path.add(rootRouterId);
        Collections.reverse(path);
        return new OSPFPath(rootRouterId, dstRouterId, path, costs.get(dstRouterId));
    }
}
