package com.globalcluster.gateway;

import com.globalcluster.gateway.model.NodeRegistrationEntity;
import com.globalcluster.gateway.repository.NodeRegistrationRepository;
import com.globalcluster.shared.NodeRegistrationInfo; // Importar a classe compartilhada
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class GatewayController {

    private static final Logger logger = LoggerFactory.getLogger(GatewayController.class);

    @Autowired
    private GeoIpService geoIpService;

    @Autowired
    private NodeRegistrationRepository nodeRegistrationRepository; // Injetar o repositório

    // Mapeia continentes para portas específicas
    private int assignContinentPort(String continentName) {
        if (continentName == null) {
            return 8080; // Default to dashboard port if continent is unknown
        }
        return switch (continentName.toUpperCase()) {
            case "NORTH AMERICA", "SOUTH AMERICA" -> 8081; // Americas
            case "EUROPE" -> 8082;
            case "AFRICA" -> 8083;
            case "ASIA" -> 8084;
            case "OCEANIA" -> 8085;
            case "ANTARCTICA" -> 8086;
            default -> 8080; // Fallback to dashboard port
        };
    }

    /**
     * Endpoint para o registro automático de nós.
     * O IP do nó é detectado automaticamente da requisição.
     * Redireciona para a porta do continente correspondente.
     */
    @PostMapping("/registerNode")
    public String registerNode(HttpServletRequest request) {
        String nodeIp = request.getRemoteAddr(); // Captura o IP do cliente
        if (nodeIp == null || nodeIp.equals("0:0:0:0:0:0:0:1") || nodeIp.equals("127.0.0.1")) {
            // For local testing, use a dummy public IP or allow explicit override
            logger.warn("Received registration from local IP: {}. Using a dummy IP for GeoIP lookup for testing purposes.", nodeIp);
            nodeIp = "8.8.8.8"; // Example public IP for testing
        }

        String continent = geoIpService.getContinent(nodeIp);
        int assignedPort = assignContinentPort(continent);

        // Criar ou atualizar a entidade no banco de dados
        NodeRegistrationEntity entity = nodeRegistrationRepository.findById(nodeIp).orElse(new NodeRegistrationEntity());
        entity.setIpAddress(nodeIp);
        entity.setContinent(continent);
        entity.setAssignedPort(assignedPort);
        entity.setRegistrationTime(LocalDateTime.now());
        entity.setLastHeartbeat(LocalDateTime.now()); // Atualiza o heartbeat no registro

        nodeRegistrationRepository.save(entity); // Salvar ou atualizar no banco

        logger.info("Node {} from {} registered and assigned to port {}.", nodeIp, continent, assignedPort);

        return "Connect to Master on port: " + assignedPort;
    }

    /**
     * Endpoint do Dashboard para visualizar os nós conectados.
     * Restrito a IPs específicos na porta 8080.
     */
    @GetMapping("/dashboard")
    public Map<String, NodeRegistrationInfo> dashboard() {
        List<NodeRegistrationEntity> entities = nodeRegistrationRepository.findAll();
        return entities.stream()
                .map(this::toNodeRegistrationInfo)
                .collect(Collectors.toConcurrentMap(NodeRegistrationInfo::getIpAddress, info -> info));
    }

    // Método auxiliar para converter Entity para DTO
    private NodeRegistrationInfo toNodeRegistrationInfo(NodeRegistrationEntity entity) {
        // Assume que NodeRegistrationInfo tem um construtor ou setters apropriados
        NodeRegistrationInfo dto = new NodeRegistrationInfo(entity.getIpAddress(), entity.getContinent(), entity.getAssignedPort());
        dto.setRegistrationTime(entity.getRegistrationTime());
        // Se NodeRegistrationInfo pudesse ter lastHeartbeat, também seria setado aqui
        return dto;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Gateway!";
    }

    /**
     * Endpoint para desregistrar um nó.
     * Remove o registro do nó do banco de dados.
     */
    @DeleteMapping("/deregisterNode/{nodeIp}")
    public void deregisterNode(@PathVariable String nodeIp) {
        nodeRegistrationRepository.deleteById(nodeIp);
        logger.info("Node {} deregistered.", nodeIp);
    }
}
