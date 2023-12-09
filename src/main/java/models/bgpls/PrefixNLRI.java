package models.bgpls;

import models.bgpls.descriptors.NodeDescriptor;
import models.bgpls.descriptors.PrefixDescriptor;

public class PrefixNLRI extends NLRI {
    public NodeDescriptor local;
    public PrefixDescriptor descriptor;
}
