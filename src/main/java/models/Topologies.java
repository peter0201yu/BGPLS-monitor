package models;

import models.igp.IGPInstance;

import java.util.HashMap;
import java.util.Map;

public class Topologies {
    private Map<String, Map<Integer, Map<Integer, Map<Integer, IGPInstance>>>> topologies;
    // Using    bgplsId,    as#,         protocolId,  instanceId
    public Topologies() {
        this.topologies = new HashMap<>();
    }

    public IGPInstance get(String bgplsId, Integer as, Integer protocolId, Integer instanceId) {
        if (!topologies.containsKey(bgplsId)) {
            return null;
        }
        Map<Integer, Map<Integer, Map<Integer, IGPInstance>>> byAS = topologies.get(bgplsId);

        if (!byAS.containsKey(as)) {
            return null;
        }
        Map<Integer, Map<Integer, IGPInstance>> byProtocolId = byAS.get(as);

        if (!byProtocolId.containsKey(protocolId)) {
            return null;
        }
        Map<Integer, IGPInstance> byInstanceId = byProtocolId.get(protocolId);

        if (!byInstanceId.containsKey(instanceId)) {
            return null;
        }
        return byInstanceId.get(instanceId);
    }

    public Integer size() {
        Integer totalSize = 0;
        for (Map.Entry<String, Map<Integer, Map<Integer, Map<Integer, IGPInstance>>>> bgplsEntry : topologies.entrySet()) {
            for (Map.Entry<Integer, Map<Integer, Map<Integer, IGPInstance>>> asEntry : bgplsEntry.getValue().entrySet()) {
                for (Map.Entry<Integer, Map<Integer, IGPInstance>> protocolEntry : asEntry.getValue().entrySet()) {
                    totalSize += protocolEntry.getValue().size();
                }
            }
        }
        return totalSize;
    }

    public void add(String bgplsId, Integer as, Integer protocolId, Integer instanceId, IGPInstance topology) {
        if (!topologies.containsKey(bgplsId)) {
            topologies.put(bgplsId, new HashMap<>());
        }
        Map<Integer, Map<Integer, Map<Integer, IGPInstance>>> byAS = topologies.get(bgplsId);

        if (!byAS.containsKey(as)) {
            byAS.put(as, new HashMap<>());
        }
        Map<Integer, Map<Integer, IGPInstance>> byProtocolId = byAS.get(as);

        if (!byProtocolId.containsKey(protocolId)) {
            byProtocolId.put(protocolId, new HashMap<>());
        }
        Map<Integer, IGPInstance> byInstanceId = byProtocolId.get(protocolId);

        byInstanceId.put(instanceId, topology);
    }
}
