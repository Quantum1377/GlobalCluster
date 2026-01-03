package com.globalcluster.gateway;

import org.springframework.stereotype.Service;

@Service
public class RegionResolver {

    public int getMasterPort(String continentName) {
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
}
