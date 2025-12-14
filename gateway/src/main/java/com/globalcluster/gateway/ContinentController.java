package com.globalcluster.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ContinentController {

    private static final Map<Integer, String> PORT_TO_CONTINENT = new HashMap<>();

    static {
        PORT_TO_CONTINENT.put(8081, "Americas");
        PORT_TO_CONTINENT.put(8082, "Europe");
        PORT_TO_CONTINENT.put(8083, "Africa");
        PORT_TO_CONTINENT.put(8084, "Asia");
        PORT_TO_CONTINENT.put(8085, "Oceania");
        PORT_TO_CONTINENT.put(8086, "Antarctica");
    }

    @GetMapping("/")
    public String welcomeMessage(HttpServletRequest request) {
        int localPort = request.getLocalPort(); // Get the port the request came in on
        String continent = PORT_TO_CONTINENT.getOrDefault(localPort, "Unknown Continent");
        return String.format("Bem vindo ao servidor da/do %s", continent);
    }

    // A simple health check for all continent ports
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}
