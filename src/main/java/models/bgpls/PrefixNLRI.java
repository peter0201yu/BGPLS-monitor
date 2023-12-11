package models.bgpls;

import models.bgpls.descriptors.NodeDescriptor;
import models.bgpls.descriptors.PrefixDescriptor;

public class PrefixNLRI extends NLRI {
    public NodeDescriptor local;
    public PrefixDescriptor descriptor;

    @Override
    public String getBgplsId() {
        return local.bgplsId;
    }

    @Override
    public Integer getAs() {
        return local.as;
    }
}
