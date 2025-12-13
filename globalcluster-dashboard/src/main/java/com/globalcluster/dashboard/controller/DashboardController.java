package com.globalcluster.dashboard.controller;

import com.globalcluster.dashboard.model.NodeMetric;
import com.globalcluster.dashboard.service.AuthSessionService;
import com.globalcluster.shared.NodeRegistrationInfo; // Importar a classe compartilhada
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

// Alterado para @Controller para servir o HTML e @ResponseBody para endpoints API
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private AuthSessionService authSessionService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${globalcluster.gateway.url}")
    private String gatewayUrl;

    @GetMapping
    public String dashboard(@RequestHeader(value = "Authorization", required = false) String token, Model model) {
        // A autenticação agora será verificada para acesso ao HTML
        if (!authSessionService.isLoggedIn(token)) {
            // Se não autenticado, redireciona para a página de login
            return "redirect:/login.html"; // Assume que login.html está em static
        }
        model.addAttribute("message", "Bem-vindo ao painel GlobalCluster! (Acesso AUTORIZADO)");
        return "dashboard.html"; // Serve o arquivo estático dashboard.html
    }

    @GetMapping("/api/nodes/metrics")
    @ResponseBody // Indica que este método retorna diretamente o corpo da resposta (JSON/XML)
    public List<NodeMetric> getNodeMetrics() {
        List<NodeMetric> metrics = new ArrayList<>();
        try {
            // Chamar o endpoint /dashboard do Gateway
            ResponseEntity<Map<String, NodeRegistrationInfo>> response = restTemplate.exchange(
                    gatewayUrl + "/dashboard",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, NodeRegistrationInfo>>() {}
            );

            Map<String, NodeRegistrationInfo> gatewayNodes = response.getBody();

            if (gatewayNodes != null) {
                for (NodeRegistrationInfo info : gatewayNodes.values()) {
                    // Simular métricas adicionais
                    long ping = ThreadLocalRandom.current().nextLong(20, 200); // 20-200 ms
                    long latency = ping + ThreadLocalRandom.current().nextLong(5, 50); // ping + 5-50 ms
                    String status = ThreadLocalRandom.current().nextBoolean() ? "UP" : "DOWN"; // 50% chance for UP/DOWN

                    metrics.add(new NodeMetric(
                            info.getIpAddress(),
                            info.getContinent(),
                            info.getAssignedPort(),
                            info.getRegistrationTime(),
                            info.getLastHeartbeat(), // lastHeartbeat agora vem do NodeRegistrationInfo
                            ping,
                            latency,
                            status
                    ));
                }
            }
        } catch (Exception e) {
            // Logar o erro e retornar lista vazia ou lançar exceção
            System.err.println("Erro ao buscar métricas do Gateway: " + e.getMessage());
        }
        return metrics;
    }
}
