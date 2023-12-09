package models;

import models.NLRI;

import java.util.List;
import java.util.Map;

public class UpdateMessage {
    public Map<String, Object> attributes;
    public List<NLRI> nlris;
}
