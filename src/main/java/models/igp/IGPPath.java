package models.igp;

import java.util.ArrayList;

public class IGPPath {
    public String srcNodeId;
    public String dstNodeId;
    public ArrayList<String> path;
    public Float cost;
    public IGPPath(String srcNodeId, String dstNodeId, ArrayList<String> path, Float cost) {
        this.srcNodeId = srcNodeId;
        this.dstNodeId = dstNodeId;
        this.path = path;
        this.cost = cost;
    }

    public static IGPPath join(IGPPath ...parts) {
        IGPPath joined = new IGPPath(parts[0].srcNodeId, parts[parts.length - 1].dstNodeId, new ArrayList<>(), 0f);
        for (int i = 0; i < parts.length; i++) {
            assert(parts[i].dstNodeId == parts[i + 1].srcNodeId);
            joined.path.addAll(parts[i].path);
            if (i != parts.length - 1) {
                joined.path.removeLast();
            }
            joined.cost += parts[i].cost;
        }

        return joined;
    }

    @Override
    public String toString(){
        String s = "";
        s += "Path: src " + srcNodeId + " -> " + dstNodeId + "\n";
        s += "  hops: " + path + "\n";
        s += "  cost: " + cost + "\n";
        return s;
    }
}
