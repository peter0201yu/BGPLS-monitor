import json

def extract_bgpls_updates():
    json_values = []
    with open('dummydata/exabgp_output_1.log', 'r') as file:
        data = file.read()
        for line in data.splitlines():
            # Parse the line
            if 'decoded UPDATE (   0)' in line and '\"bgp-ls bgp-ls\"' in line:
                starting_brace = line.index('{')
                ending_brace = line.rindex('}')
                json_string = line[starting_brace:ending_brace+1]
                json_value = json.loads(json_string)
                json_values.append(json_value)

    # with open('dummydata/bgpls-examples.json', 'w') as file:
    #     json.dump(json_values, file, indent=4)

def parse_bgpls_examples():
    nlri_type_count = {}
    local_ips = []
    peer_ips = []
    key_is_peer_ip = True
    ip_set = set()
    interface_neighbor_address_set = set()
    router_interface_address_set = set()
    router_neighbor_address_set = set()
    nlri_stats = {
        'bgpls-link': {
            'local-autonomous-system': [],
            'local-bgp-ls-identifier': [],
            'local-ospf-area-id': [],
            'local-router-id': [],
            'remote-autonomous-system': [],
            'remote-bgp-ls-identifier': [],
            'remote-ospf-area-id': [],
            'remote-router-id': [],
            'interface-address': [],
            'neighbor-address': [],
        },
        'bgpls-node': {
            'autonomous-system': [],
            'bgp-ls-identifier': [],
            'ospf-area-id': [],
            'router-id': [],
            'nexthop': [],
        },
        'bgpls-prefix-v4': {
            'autonomous-system': [],
            'bgp-ls-identifier': [],
            'ospf-area-id': [],
            'router-id': [],
            'ip-reachability-tlv': [],
            'ip-reach-prefix': [],
            'nexthop': [],
            'ospf-route-type': [],
        },
    }
    nlris_by_type = {
        'bgpls-link': [],
        'bgpls-node': [],
        'bgpls-prefix-v4': [],
    }
    with open('dummydata/bgpls-examples.json', 'r') as file:
        bgpls_updates = json.load(file)
        for bgpls_update in bgpls_updates:
            address = bgpls_update['neighbor']['address']
            asn = bgpls_update['neighbor']['asn']
            message = bgpls_update['neighbor']['message']

            attribute = message['update']['attribute']
            announce = message['update']['announce']

            local_ip = address['local']
            peer_ip = address['peer']
            local_asn = asn['local']
            peer_asn = asn['peer']

            origin = attribute['origin']
            local_preference = attribute['local-preference']
            bgp_ls_attribute = attribute['bgp-ls']

            peers = announce['bgp-ls bgp-ls'].keys()
            peer_updates = announce['bgp-ls bgp-ls']

            ip_set.add((local_ip, peer_ip))
            local_ips.append(local_ip)
            peer_ips.append(peer_ip)

            for peer in peers:
                if peer != peer_ip:
                    key_is_peer_ip = False

                nlris = peer_updates[peer]
                for nlri in nlris:
                    nlri_type = nlri['ls-nlri-type']
                    routing_topology = nlri['l3-routing-topology']
                    protocol_id = nlri['protocol-id']

                    nlri_type_count[nlri_type] = nlri_type_count.get(nlri_type, 0) + 1
                    nlris_by_type[nlri_type].append(nlri)

                    if nlri_type == "bgpls-link":
                        local_node_descriptors = nlri['local-node-descriptors']
                        for key in local_node_descriptors.keys():
                            nlri_stats['bgpls-link'][f'local-{key}'].append(local_node_descriptors[key])

                        remote_node_descriptors = nlri['remote-node-descriptors']
                        for key in remote_node_descriptors.keys():
                            nlri_stats['bgpls-link'][f'remote-{key}'].append(remote_node_descriptors[key])

                        nlri_stats['bgpls-link']['interface-address'].append(nlri['interface-address']['interface-address'])
                        nlri_stats['bgpls-link']['neighbor-address'].append(nlri['neighbor-address']['neighbor-address'])
                        interface_neighbor_address_set.add((nlri['interface-address']['interface-address'], nlri['neighbor-address']['neighbor-address']))
                        router_interface_address_set.add((nlri['local-node-descriptors']['router-id'], nlri['interface-address']['interface-address']))
                        router_neighbor_address_set.add((nlri['local-node-descriptors']['router-id'], nlri['neighbor-address']['neighbor-address']))

                        if len(nlri.keys()) != 4 + 3:
                            print('uhoh link')
                    elif nlri_type == "bgpls-node":
                        node_descriptors = nlri['node-descriptors']
                        for key in node_descriptors.keys():
                            nlri_stats['bgpls-node'][key].append(node_descriptors[key])

                        nlri_stats['bgpls-node']['nexthop'].append(nlri['nexthop'])

                        if len(nlri.keys()) != 2 + 3:
                            print('uhoh node')
                    elif nlri_type == "bgpls-prefix-v4":
                        node_descriptors = nlri['node-descriptors']
                        for key in node_descriptors.keys():
                            nlri_stats['bgpls-prefix-v4'][key].append(node_descriptors[key])

                        nlri_stats['bgpls-prefix-v4']['ip-reachability-tlv'].append(nlri['ip-reachability-tlv'])
                        nlri_stats['bgpls-prefix-v4']['ip-reach-prefix'].append(nlri['ip-reach-prefix'])
                        nlri_stats['bgpls-prefix-v4']['nexthop'].append(nlri['nexthop'])
                        nlri_stats['bgpls-prefix-v4']['ospf-route-type'].append(nlri['ospf-route-type'])

                        if len(nlri.keys()) != 5 + 3:
                            print('uhoh prefix')

                    
    print('nlri_type_count: ', nlri_type_count)
    print('local_ips: ', set(local_ips))
    print('peer_ips: ', set(peer_ips))
    print('ip_set: ', ip_set)
    print('key_is_peer_ip: ', key_is_peer_ip)
    print()
    for key in nlri_stats:
        print(key)
        for key2 in nlri_stats[key]:
            print('\t', key2, set(nlri_stats[key][key2]))
        if key == 'bgpls-link':
            print('\t', 'interface_neighbor_address_set', interface_neighbor_address_set)
            print('\t', 'router_interface_address_set', router_interface_address_set)
            print('\t', 'router_neighbor_address_set', router_neighbor_address_set)
        print()

    # for nlri in nlris_by_type:
    #     with open(f'dummydata/{nlri}.json', 'w') as file:
    #         json.dump(nlris_by_type[nlri], file, indent=4)

def parse_prefix_nlris():
    with open('dummydata/bgpls-prefix-v4.json', 'r') as file:
        nlris = json.load(file)

        ospf_route_id_to_reachability = {}
        ospf_route_id_to_router = {}
        for nlri in nlris:
            ospf_route_type = nlri['ospf-route-type']
            ip_reachability = nlri['ip-reachability-tlv']
            ip_reach_prefix = nlri['ip-reach-prefix']
            if ospf_route_type not in ospf_route_id_to_reachability:
                ospf_route_id_to_reachability[ospf_route_type] = set()
            ospf_route_id_to_reachability[ospf_route_type].add(ip_reachability)
            if ospf_route_type not in ospf_route_id_to_router:
                ospf_route_id_to_router[ospf_route_type] = set()
            ospf_route_id_to_router[ospf_route_type].add(ip_reach_prefix)

    #turn value which are sets into lists
    ospf_route_id_to_reachability = {key: list(value) for key, value in ospf_route_id_to_reachability.items()}
    ospf_route_id_to_router = {key: list(value) for key, value in ospf_route_id_to_router.items()}
    print('ospf_route_id_to_reachability', json.dumps(ospf_route_id_to_reachability, indent=4))
    print('ospf_route_id_to_router', json.dumps(ospf_route_id_to_router, indent=4))
