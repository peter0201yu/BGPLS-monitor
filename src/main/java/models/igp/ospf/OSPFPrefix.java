package models.igp.ospf;

import models.igp.IGPPrefix;
import util.Attribute;

import java.util.HashMap;
import java.util.Map;

public class OSPFPrefix extends IGPPrefix {
    public Map<String, Attribute> routerIDToAttributes;
    public Attribute attributes;
    public OSPFPrefix() {
        this.routerIDToAttributes = new HashMap<>();
    }
    public void setAttributes(Attribute attributes) {
        this.attributes = attributes;
    }
}
