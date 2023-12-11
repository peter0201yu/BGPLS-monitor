package models.igp;

import util.Attribute;

public class IGPNode {
    public String id;
    public Attribute attributes;
    public IGPNode(String id) {
        this.id = id;
    }

    public void setAttributes(Attribute attributes) {
        this.attributes = attributes;
    }
}
