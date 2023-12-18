# BGPLS Monitor

## Usage:

Currently, the server takes in a resource file that contains all the BGPLS messages. Launch the server with an unnamed argument `resourceName`, which should be the name of a JSON file, e.g. `dummydata/bgpls-example.json`. By default, the server runs on localhost 8080.

To find the shortest path between two network prefixes, use the POST API that takes in JSON data. Inside the JSON data,
the user needs to specify `bgplsId, as, protocolId, instanceId` to indicate which topology instance we are looking at, and pass in `ingressNetwork, egressNetwork` as source and destination network prefixes. For example,
```
curl -X POST -H "Content-Type: application/json" -d '{"bgplsId":"0","as":65530,"protocolId":3,"instanceId":0,"ingressNetwork":"10.0.0.12/30","egressNetwork":"10.0.1.4/30"}' http://localhost:8080/calculatePath
```
The server will return a result like this:
```
Path: src 10.0.0.12/30 -> 10.0.1.4/30
  hops: [10.0.0.12/30, 192.168.0.4, 192.168.0.101, 10.0.1.4/30]
  cost: 3.0
```
