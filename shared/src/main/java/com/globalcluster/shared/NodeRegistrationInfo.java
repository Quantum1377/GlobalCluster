package com.globalcluster.shared;

import java.time.LocalDateTime;

public class NodeRegistrationInfo {
    private String ipAddress;
    private String continent;
    private int assignedPort;
    private LocalDateTime registrationTime;
    // Add more fields for complexity later, e.g., last heartbeat, status

    public NodeRegistrationInfo(String ipAddress, String continent, int assignedPort) {
        this.ipAddress = ipAddress;
        this.continent = continent;
        this.assignedPort = assignedPort;
        this.registrationTime = LocalDateTime.now();
    }

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
}
