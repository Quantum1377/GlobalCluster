package com.globalcluster.gateway;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@RestController
public class GatewayController {

    // Armazena os Nodes conectados: NodeID -> porta do Master regional
    private final Map<String, Integer> connectedNodes = new ConcurrentHashMap<>();

    // Simula a atribuição de porta do Master regional baseado na região
    private int assignMasterPort(String region) {
        return switch(region.toLowerCase()) {
            case "america" -> 8081;
            case "europe" -> 8082;
            case "asia" -> 8083;
            case "africa" -> 8084;
            case "oceania" -> 8085;
            case "antarctica" -> 8086;
            case "southamerica" -> 8087;
            default -> 8081; // fallback
        };
    }

    // Registrar um Node
    @PostMapping("/registerNode")
    public String registerNode(@RequestParam String nodeId, @RequestParam String nodeIp) {
        String region = RegionResolver.getRegionByIp(nodeIp); // descobrir região
        int masterPort = assignMasterPort(region);

        connectedNodes.put(nodeId, masterPort);

        System.out.printf("Node %s from %s -> Master port %d%n", nodeId, region, masterPort);

        return "Connect to Master on port: " + masterPort;
    }

    // Dashboard dos Nodes conectados
    @GetMapping("/dashboard")
    public Map<String, Integer> dashboard() {
        return connectedNodes;
    }
}
