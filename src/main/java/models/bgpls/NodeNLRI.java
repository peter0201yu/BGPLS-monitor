package models.bgpls;

import models.bgpls.descriptors.NodeDescriptor;

public class NodeNLRI extends NLRI {
    public NodeDescriptor descriptor;

    @Override
    public String getBgplsId() {
        return descriptor.bgplsId;
    }

    @Override
    public Integer getAs() {
        return descriptor.as;
    }
}
