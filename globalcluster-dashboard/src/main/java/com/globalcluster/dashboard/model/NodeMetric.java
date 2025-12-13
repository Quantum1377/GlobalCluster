package com.globalcluster.dashboard.model;

import java.time.LocalDateTime;

public class NodeMetric {
    private String ipAddress;
    private String continent;
    private int assignedPort;
    private LocalDateTime registrationTime;
    private LocalDateTime lastHeartbeat;
    private long ping; // Simulated ping in milliseconds
    private long latency; // Simulated latency in milliseconds
    private String status; // e.g., "UP", "DOWN", "DEGRADED"

    public NodeMetric() {
    }

    public NodeMetric(String ipAddress, String continent, int assignedPort, LocalDateTime registrationTime, LocalDateTime lastHeartbeat, long ping, long latency, String status) {
        this.ipAddress = ipAddress;
        this.continent = continent;
        this.assignedPort = assignedPort;
        this.registrationTime = registrationTime;
        this.lastHeartbeat = lastHeartbeat;
        this.ping = ping;
        this.latency = latency;
        this.status = status;
    }

    // Getters and Setters
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

    public long getPing() {
        return ping;
    }

    public void setPing(long ping) {
        this.ping = ping;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
