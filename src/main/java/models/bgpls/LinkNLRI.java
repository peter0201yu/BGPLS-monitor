package models.bgpls;

import models.bgpls.descriptors.NodeDescriptor;
import models.bgpls.descriptors.LinkDescriptor;

public class LinkNLRI extends NLRI {
    public NodeDescriptor local;
    public NodeDescriptor remote;
    public LinkDescriptor descriptor;
}
