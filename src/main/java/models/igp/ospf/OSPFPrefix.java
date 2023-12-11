package models.igp.ospf;

import models.igp.IGPPrefix;

import java.util.*;
import util.Attribute;

public class OSPFPrefix extends IGPPrefix {
    // routerId => attribute
    public HashMap<String, Attribute> attributesForRouter;

    public OSPFPrefix(){
        attributesForRouter = new HashMap<>();
    }
}
