package com.globalcluster.master;

import org.springframework.web.bind.annotation.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@RestController
public class NodeController {

    // Armazena nodes ativos (NodeID -> Info)
    private final Map<String, NodeInfo> nodes = new ConcurrentHashMap<>();

    @PostMapping("/register")
    public String registerNode(@RequestBody NodeInfo node) {
        nodes.put(node.getId(), node);
        System.out.println("Node registered: " + node);
        return "Node registered successfully!";
    }

    @GetMapping("/nodes")
    public Map<String, NodeInfo> listNodes() {
        return nodes;
    }
}
