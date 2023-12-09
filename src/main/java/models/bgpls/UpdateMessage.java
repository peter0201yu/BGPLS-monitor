package models.bgpls;

import util.Attribute;

import java.util.List;
import java.util.Map;

public class UpdateMessage {
    public Attribute attributes;
    public List<NLRI> nlris;
}
