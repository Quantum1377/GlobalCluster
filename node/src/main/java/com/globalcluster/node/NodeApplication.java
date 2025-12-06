package com.globalcluster.node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class NodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class, args);

        // Registrar Node no Master assim que iniciar
        registerNode();
    }

    private static void registerNode() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String masterUrl = "http://localhost:8080/register";

            // Cria informações do Node
            NodeInfo node = new NodeInfo("Node-1", "US-East", 4, 8192);

            // Faz POST para o Master
            String response = restTemplate.postForObject(masterUrl, node, String.class);
            System.out.println("Response from Master: " + response);

        } catch (Exception e) {
            System.err.println("Failed to register Node: " + e.getMessage());
        }
    }
}
