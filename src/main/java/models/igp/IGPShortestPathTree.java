package models.igp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class IGPShortestPathTree {
    public String rootNodeId;
    // spanning tree: <child, parent>
    public HashMap<String, String> parents;
    // spanning tree costs: <child, cost>
    public HashMap<String, Float> costs;

    public IGPShortestPathTree(String _rootNodeId) {
        rootNodeId = _rootNodeId;
        parents = new HashMap<>();
        costs = new HashMap<>();
        costs.put(rootNodeId, 0f);
    }

    // Add router to the shortest path tree
    public void addToTree(String childId, String parentId, Float cost) {
        parents.put(childId, parentId);
        costs.put(childId, cost);
    }

    public Float getCost(String dstNodeId){
        return costs.get(dstNodeId);
    }

    // get path based on spanning tree
    public IGPPath getPath(String dstNodeId){
        String currRouterId = dstNodeId;
        ArrayList<String> path = new ArrayList<>();

        while (!currRouterId.equals(rootNodeId)) {
            String parentId = parents.get(currRouterId);
            path.add(currRouterId); // Add the router to the path
            currRouterId = parentId;
        }
        path.add(rootNodeId);
        Collections.reverse(path);
        return new IGPPath(rootNodeId, dstNodeId, path, costs.get(dstNodeId));
    }
}
