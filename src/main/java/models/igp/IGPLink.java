package models.igp;

public class IGPLink {
    public String srcNodeId;
    public String destNodeId;
    public IGPLink(String srcNodeId, String destNodeId) {
        this.srcNodeId = srcNodeId;
        this.destNodeId = destNodeId;
    }
}
