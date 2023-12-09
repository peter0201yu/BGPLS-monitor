package models;

import models.descriptors.NodeDescriptor;
import models.descriptors.LinkDescriptor;

public class LinkNLRI extends NLRI {
    public NodeDescriptor local;
    public NodeDescriptor remote;
    public LinkDescriptor descriptor;
}
