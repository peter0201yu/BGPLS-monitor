package models.igp;

import util.Attribute;

public class IGPLink {
    public String srcNodeId;
    public String destNodeId;
    public Attribute attributes;

    public IGPLink(String srcNodeId, String destNodeId) {
        this.srcNodeId = srcNodeId;
        this.destNodeId = destNodeId;
    }

    public void setAttributes(Attribute attributes) {
        this.attributes = attributes;
    }
}
