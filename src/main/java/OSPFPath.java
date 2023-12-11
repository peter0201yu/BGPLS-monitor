import java.util.ArrayList;

public class OSPFPath {
    public String srcRouterId;
    public String dstRouterId;
    public ArrayList<String> path;
    public Float cost;
    public OSPFPath(String srcRouterId, String dstRouterId, ArrayList<String> path, Float cost) {
        this.srcRouterId = srcRouterId;
        this.dstRouterId = dstRouterId;
        this.path = path;
        this.cost = cost;
    }
}
