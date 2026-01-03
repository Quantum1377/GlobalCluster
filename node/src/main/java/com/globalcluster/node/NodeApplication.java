package com.globalcluster.node;

import com.globalcluster.node.NodeInfo;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.core.functions.CheckedSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
@EnableScheduling
public class NodeApplication {

    private static final Logger logger = LoggerFactory.getLogger(NodeApplication.class);

    @Value("${globalcluster.master.url}")
    private String masterUrl;

    @Autowired
    private GeoIpService geoIpService;

    private String nodeId;
    private RestTemplate restTemplate;

    // NodeInfo to be sent during registration and used for heartbeats
    private NodeInfo currentNodeInfo;

    private final Random random = new Random();

    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ApplicationRunner init(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.nodeId = UUID.randomUUID().toString(); // Generate a unique ID for this node

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .build();
        Retry retry = Retry.of("registerNode", retryConfig);

        return args -> {
            try {
                String publicIp = getSimulatedPublicIpForTesting();
                String continent = geoIpService.getContinent(publicIp);

                this.currentNodeInfo = new NodeInfo(
                        nodeId,
                        continent,
                        Runtime.getRuntime().availableProcessors(),
                        (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024)),
                        generateSimulatedLoad() // Initial load
                );

                String registerUrl = masterUrl + "/register";
                logger.info("Registering with Master at: {}", registerUrl);

                CheckedSupplier<String> registerCall = () -> restTemplate.postForObject(registerUrl, currentNodeInfo, String.class);
                String registrationResponse = Retry.decorateCheckedSupplier(retry, registerCall).get();
                logger.info("Registration response from Master: {}", registrationResponse);

            } catch (Throwable e) {
                logger.error("Failed to initialize and register Node: {}", e.getMessage(), e);
            }
        };
    }

    @Scheduled(fixedRate = 15000) // Send heartbeat every 15 seconds
    public void sendHeartbeat() {
        if (currentNodeInfo != null) {
            // Update simulated load before sending heartbeat
            currentNodeInfo.setCurrentLoad(generateSimulatedLoad());

            String heartbeatUrl = masterUrl + "/heartbeat/" + currentNodeInfo.getId();
            try {
                restTemplate.postForObject(heartbeatUrl, currentNodeInfo, String.class); // Send updated NodeInfo
                logger.debug("Heartbeat sent for node: {} with load {}", currentNodeInfo.getId(), currentNodeInfo.getCurrentLoad());
            } catch (Exception e) {
                logger.error("Failed to send heartbeat for node {}: {}", currentNodeInfo.getId(), e.getMessage());
            }
        }
    }

    @PreDestroy
    public void deregisterNode() {
        if (currentNodeInfo != null && restTemplate != null) {
            try {
                String deregisterUrl = masterUrl + "/deregister/" + currentNodeInfo.getId();
                logger.info("Deregistering node {} from Master at: {}", currentNodeInfo.getId(), deregisterUrl);
                restTemplate.delete(deregisterUrl);
                logger.info("Node {} successfully deregistered.", currentNodeInfo.getId());
            } catch (Exception e) {
                logger.error("Failed to deregister node {}: {}", currentNodeInfo.getId(), e.getMessage());
            }
        }
    }

    private int generateSimulatedLoad() {
        // Simulate a load between 0 and 100
        return random.nextInt(101);
    }

    private String getSimulatedPublicIpForTesting() {
        String[] ips = {"8.8.8.8", "203.0.113.45", "198.51.100.10"}; // IPs de teste: EUA, Oceania, Europa
        int randomIndex = (int) (Math.random() * ips.length);
        return ips[randomIndex];
    }
}


