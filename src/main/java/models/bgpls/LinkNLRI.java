package models.bgpls;

import models.bgpls.descriptors.NodeDescriptor;
import models.bgpls.descriptors.LinkDescriptor;

public class LinkNLRI extends NLRI {
    public NodeDescriptor local;
    public NodeDescriptor remote;
    public LinkDescriptor descriptor;

    @Override
    public String getBgplsId() {
        assert(local.bgplsId.equals(remote.bgplsId));
        return local.bgplsId;
    }

    @Override
    public Integer getAs() {
        assert(local.as == remote.as);
        return local.as;
    }
}
