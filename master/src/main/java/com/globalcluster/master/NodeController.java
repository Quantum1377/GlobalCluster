package com.globalcluster.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
public class NodeController {

    private static final Logger logger = LoggerFactory.getLogger(NodeController.class);

    // Armazena nodes ativos (Region -> List<NodeInfo>)
    private final Map<String, List<NodeInfo>> nodesByRegion = new ConcurrentHashMap<>();

    // Map to quickly find node by ID for heartbeat updates and load updates
    private final Map<String, NodeInfo> allNodes = new ConcurrentHashMap<>();

    // Configurable heartbeat timeout (e.g., 30 seconds)
    private static final long HEARTBEAT_TIMEOUT_SECONDS = 30;

    @PostMapping("/register")
    public String registerNode(@RequestBody NodeInfo node) {
        // Update heartbeat on registration
        node.setLastHeartbeat(LocalDateTime.now());
        
        // Remove existing node with same ID if any, to handle re-registrations
        allNodes.computeIfPresent(node.getId(), (id, existingNode) -> {
            nodesByRegion.computeIfPresent(existingNode.getRegion(), (region, nodes) -> {
                nodes.removeIf(n -> n.getId().equals(id));
                return nodes;
            });
            return null; // Remove from allNodes map
        });

        nodesByRegion.computeIfAbsent(node.getRegion(), k -> new CopyOnWriteArrayList<>()).add(node);
        allNodes.put(node.getId(), node);
        logger.info("Node registered: {}", node);
        return "Node registered successfully!";
    }

    @PostMapping("/heartbeat/{nodeId}")
    public String receiveHeartbeat(@PathVariable String nodeId, @RequestBody NodeInfo updatedNodeInfo) {
        NodeInfo node = allNodes.get(nodeId);
        if (node != null) {
            node.setLastHeartbeat(LocalDateTime.now());
            node.setCurrentLoad(updatedNodeInfo.getCurrentLoad()); // Update load
            logger.debug("Received heartbeat from node: {} with load: {}", nodeId, updatedNodeInfo.getCurrentLoad());
            return "Heartbeat received!";
        }
        logger.warn("Received heartbeat from unknown node: {}", nodeId);
        return "Node not found.";
    }

    @DeleteMapping("/deregister/{nodeId}")
    public String deregisterNode(@PathVariable String nodeId) {
        NodeInfo removedNode = allNodes.remove(nodeId);
        if (removedNode != null) {
            nodesByRegion.computeIfPresent(removedNode.getRegion(), (region, nodes) -> {
                nodes.removeIf(n -> n.getId().equals(nodeId));
                if (nodes.isEmpty()) {
                    return null; // Remove region entry if no nodes left
                }
                return nodes;
            });
            logger.info("Node {} deregistered gracefully.", nodeId);
            return "Node deregistered successfully!";
        }
        logger.warn("Attempted to deregister unknown node: {}", nodeId);
        return "Node not found.";
    }

    @GetMapping("/nodes")
    public Map<String, List<NodeInfo>> listNodes() {
        return nodesByRegion;
    }

    @GetMapping("/nodes/{region}")
    public List<NodeInfo> listNodesByRegion(@PathVariable String region) {
        return nodesByRegion.getOrDefault(region, List.of());
    }

    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    public void cleanUpInactiveNodes() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);
        logger.info("Running inactive node cleanup. Cutoff time: {}", cutoff);

        for (Map.Entry<String, List<NodeInfo>> entry : nodesByRegion.entrySet()) {
            String region = entry.getKey();
            List<NodeInfo> nodesInRegion = entry.getValue();

            // Filter out inactive nodes
            List<NodeInfo> activeNodes = nodesInRegion.stream()
                    .filter(node -> node.getLastHeartbeat().isAfter(cutoff))
                    .collect(Collectors.toList());

            // Identify removed nodes
            nodesInRegion.stream()
                    .filter(node -> !activeNodes.contains(node))
                    .forEach(removedNode -> {
                        logger.warn("Node {} in region {} deemed inactive and removed.", removedNode.getId(), removedNode.getRegion());
                        allNodes.remove(removedNode.getId());
                    });
            
            // Update the list for the region
            if (activeNodes.isEmpty()) {
                nodesByRegion.remove(region);
            } else {
                nodesByRegion.put(region, new CopyOnWriteArrayList<>(activeNodes)); // Replace with filtered list
            }
        }
    }
}
