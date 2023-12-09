package models.igp.ospf;

import util.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class OSPFRouter {
    String id;
    HashSet<Integer> areaIDs = new HashSet<>();
    Attribute attributes = new Attribute();
    ArrayList<String> reachablePrefixes = new ArrayList<>();

    public OSPFRouter(int routerID, int areaID) {
        this.routerID = routerID;
        this.areaIDs.add(areaID);
    }
}
