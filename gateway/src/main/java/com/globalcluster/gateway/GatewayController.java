package com.globalcluster.gateway;

import com.globalcluster.shared.NodeInfo;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.core.functions.CheckedFunction;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class GatewayController {

    private static final Logger logger = LoggerFactory.getLogger(GatewayController.class);

    @Autowired
    private GeoIpService geoIpService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Retry masterApiRetry;

    @Autowired
    private CircuitBreaker masterApiCircuitBreaker;

    @Value("${globalcluster.master.url}")
    private String masterUrl;

    @GetMapping("/**")
    public ResponseEntity<String> proxyRequest(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String continent = geoIpService.getContinent(clientIp);

        if (continent == null) {
            logger.warn("Could not determine continent for IP: {}. Using default region.", clientIp);
            continent = "default";
        }

        List<NodeInfo> nodes = Collections.emptyList();
        try {
            CheckedFunction<String, List<NodeInfo>> masterCall = (region) -> {
                String url = masterUrl + "/nodes/" + region;
                ResponseEntity<List<NodeInfo>> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<NodeInfo>>() {}
                );
                return response.getBody();
            };

            nodes = CircuitBreaker.decorateCheckedFunction(masterApiCircuitBreaker, masterApiRetry.decorateCheckedFunction(masterCall)).apply(continent);
        } catch (Throwable t) {
            logger.error("Failed to retrieve node list from Master after retries and circuit breaker: {}", t.getMessage());
            return ResponseEntity.status(503).body("Service temporarily unavailable. Please try again later.");
        }


        if (nodes == null || nodes.isEmpty()) {
            logger.error("No nodes available for region: {}", continent);
            return ResponseEntity.status(503).body("No nodes available for your region.");
        }

        // Least Connections Load Balancing
        Optional<NodeInfo> leastLoadedNode = nodes.stream()
                .min(Comparator.comparingInt(NodeInfo::getCurrentLoad));

        if (leastLoadedNode.isEmpty()) {
            logger.error("Could not find a least loaded node for region: {}", continent);
            return ResponseEntity.status(503).body("No available nodes to handle your request.");
        }

        NodeInfo selectedNode = leastLoadedNode.get();

        String targetUrl = "http://" + selectedNode.getId() + request.getRequestURI();
        logger.info("Proxying request for {} to node {} at {}", clientIp, selectedNode.getId(), targetUrl);

        // In a real scenario, you would proxy the request body and headers as well.
        // For this example, we'll just redirect.
        return ResponseEntity.status(302).header("Location", targetUrl).build();
    }
}
