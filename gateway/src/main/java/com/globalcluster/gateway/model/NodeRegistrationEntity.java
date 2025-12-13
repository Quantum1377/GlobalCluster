package com.globalcluster.gateway.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "node_registrations")
public class NodeRegistrationEntity {

    @Id
    private String ipAddress; // IP como chave primária, assume IP único por nó

    private String continent;
    private int assignedPort;
    private LocalDateTime registrationTime;
    private LocalDateTime lastHeartbeat; // Adicionado para futura complexidade

    public NodeRegistrationEntity() {
    }

    public NodeRegistrationEntity(String ipAddress, String continent, int assignedPort, LocalDateTime registrationTime) {
        this.ipAddress = ipAddress;
        this.continent = continent;
        this.assignedPort = assignedPort;
        this.registrationTime = registrationTime;
        this.lastHeartbeat = registrationTime; // Inicializa com o tempo de registro
    }

    // Getters e Setters
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public int getAssignedPort() {
        return assignedPort;
    }

    public void setAssignedPort(int assignedPort) {
        this.assignedPort = assignedPort;
    }

    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(LocalDateTime registrationTime) {
        this.registrationTime = registrationTime;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
}
