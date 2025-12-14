package com.globalcluster.node;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.core.functions.CheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class NodeApplication {

    private static final Logger logger = LoggerFactory.getLogger(NodeApplication.class);

    @Value("${globalcluster.gateway.url}")
    private String gatewayUrl;

    private String nodeExternalIp; // Para armazenar o IP do nó
    private RestTemplate restTemplate; // Para usar no PreDestroy
    private Retry registerRetry;
    private Retry deregisterRetry;
    private CircuitBreaker continentServerCircuitBreaker;

    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ApplicationRunner init(RestTemplate restTemplate) {
        this.restTemplate = restTemplate; // Armazenar o RestTemplate injetado

        // Configurar Retry para registro e desregistro (tentar 3 vezes com 2s de delay)
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .retryExceptions(ResourceAccessException.class, ConnectException.class)
                .build();
        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
        registerRetry = retryRegistry.retry("registerNode");
        deregisterRetry = retryRegistry.retry("deregisterNode");

        // Configurar CircuitBreaker para a conexão com o servidor do continente
        // Se 50% das últimas 10 requisições falharem, abre o circuito por 5s
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% de falha
                .slidingWindowSize(10)    // em uma janela de 10 requisições
                .minimumNumberOfCalls(5)  // mínimo de 5 chamadas para abrir
                .waitDurationInOpenState(Duration.ofSeconds(5)) // permanece aberto por 5s
                .build();
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        continentServerCircuitBreaker = circuitBreakerRegistry.circuitBreaker("continentServer");


        return args -> {
            try {
                // 1. Get own external IP (or simulated for testing)
                // Usar o IP simulado para GeoIP Testing
                String simulatedPublicIp = getSimulatedPublicIpForTesting(); 
                logger.info("Node's public IP (simulated for GeoIP testing): {}", simulatedPublicIp);
                nodeExternalIp = simulatedPublicIp; // Armazenar para desregistro

                // 2. Register with Gateway (com Retry)
                String registerUrl = gatewayUrl + "/registerNode?testIp=" + simulatedPublicIp; // Enviar como query param
                logger.info("Registering with Gateway at: {}", registerUrl);
                
                CheckedSupplier<String> registerCall = () -> restTemplate.postForObject(registerUrl, null, String.class);
                String registrationResponse;
                try {
                    registrationResponse = Retry.decorateCheckedSupplier(registerRetry, registerCall).get();
                } catch (Throwable t) {
                    logger.error("Failed to register with Gateway after retries: {}", t.getMessage());
                    // Decide what to do if registration fails after all retries (e.g., exit, keep retrying indefinitely)
                    return; // Exit ApplicationRunner if registration fails
                }
                logger.info("Registration response from Gateway: {}", registrationResponse);

                // 3. Parse Gateway Response for assigned port
                Pattern pattern = Pattern.compile("Connect to Master on port: (\\d+)");
                Matcher matcher = pattern.matcher(registrationResponse);
                if (matcher.find()) {
                    int assignedPort = Integer.parseInt(matcher.group(1));
                    logger.info("Assigned continent port: {}", assignedPort);

                    // 4. Connect to assigned continent port and get welcome message (com Circuit Breaker e Retry)
                    String continentServerUrl = gatewayUrl.substring(0, gatewayUrl.lastIndexOf(":")) + ":" + assignedPort + "/";
                    logger.info("Connecting to continent server at: {}", continentServerUrl);
                    
                    CheckedSupplier<String> continentCall = () -> restTemplate.getForObject(continentServerUrl, String.class);
                    String welcomeMessage;
                    try {
                        welcomeMessage = CircuitBreaker.decorateCheckedSupplier(continentServerCircuitBreaker, 
                                                Retry.decorateCheckedSupplier(registerRetry, continentCall))
                                                .get();
                    } catch (Throwable t) {
                        logger.error("Failed to connect to continent server after retries and circuit breaker: {}", t.getMessage());
                        return; // Exit ApplicationRunner if connection fails
                    }
                    logger.info("Welcome message from continent server: {}", welcomeMessage);

                } else {
                    logger.error("Could not parse assigned port from Gateway response: {}", registrationResponse);
                }

            } catch (Exception e) {
                logger.error("Failed to initialize Node: {}", e.getMessage(), e);
            }
        };
    }

    @PreDestroy
    public void deregisterNode() {
        if (nodeExternalIp != null && restTemplate != null) {
            try {
                String deregisterUrl = gatewayUrl + "/deregisterNode/" + nodeExternalIp;
                logger.info("Deregistering node {} from Gateway at: {}", nodeExternalIp, deregisterUrl);
                
                CheckedRunnable deregisterCall = () -> restTemplate.delete(deregisterUrl);
                
                try {
                    Retry.decorateCheckedRunnable(deregisterRetry, deregisterCall)
                            .run();
                    logger.info("Node {} successfully deregistered.", nodeExternalIp);
                } catch (Throwable t) {
                    logger.error("Failed to deregister node {} after retries: {}", nodeExternalIp, t.getMessage());
                }
            } catch (Exception e) {
                logger.error("Failed to deregister node {}: {}", nodeExternalIp, e.getMessage());
            }
        }
    }

    private String getSimulatedPublicIpForTesting() {
        // Em um cenário real, um nó buscaria seu IP público real (via checkip.amazonaws.com ou outro serviço)
        // Aqui, retornamos um IP público fixo para que o GeoIP no Gateway possa resolver um continente.
        String[] ips = {"8.8.8.8", "203.0.113.45", "198.51.100.10"}; // IPs de teste: EUA, Oceania, Europa
        int randomIndex = (int) (Math.random() * ips.length);
        return ips[randomIndex];
    }
}

