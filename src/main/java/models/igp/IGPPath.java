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
}
