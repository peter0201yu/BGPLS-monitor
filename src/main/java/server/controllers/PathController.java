package server.controllers;

import models.Topologies;
import models.igp.IGPInstance;
import models.igp.IGPPath;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


@RestController
public class PathController {

    private final Topologies topologies;

    @Autowired
    public PathController(Topologies topologies) {
        this.topologies = topologies;
    }

    @PostMapping("/calculatePath")
    public String calculatePath(@RequestBody Map<String, Object> request) {


        IGPInstance topology = topologies.get(
                (String) request.get("bgplsId"),
                (int) request.get("as"),
                (int) request.get("protocolId"),
                (int) request.get("instanceId")
        );

        IGPPath path = topology.getShortestPath(
                (String) request.get("ingressNetwork"),
                (String) request.get("egressNetwork")
        );

        return path.toString();
    }
}

