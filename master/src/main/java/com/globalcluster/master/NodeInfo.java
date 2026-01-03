package com.globalcluster.master;

import java.time.LocalDateTime;

public class NodeInfo {
    private String id;
    private String region;
    private int cpuCores;
    private int ramMB;
    private int currentLoad; // New field for load metric
    private LocalDateTime lastHeartbeat; // Adicionado para health check

    public NodeInfo() {} // Necessário para Jackson

    public NodeInfo(String id, String region, int cpuCores, int ramMB) {
        this.id = id;
        this.region = region;
        this.cpuCores = cpuCores;
        this.ramMB = ramMB;
        this.currentLoad = 0; // Initialize load to 0
        this.lastHeartbeat = LocalDateTime.now(); // Inicializa com o tempo de criação
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public int getCpuCores() { return cpuCores; }
    public void setCpuCores(int cpuCores) { this.cpuCores = cpuCores; }

    public int getRamMB() { return ramMB; }
    public void setRamMB(int ramMB) { this.ramMB = ramMB; }

    public int getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(int currentLoad) { this.currentLoad = currentLoad; }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "id='" + id + '\'' +
                ", region='" + region + '\'' +
                ", cpuCores=" + cpuCores +
                ", ramMB=" + ramMB +
                ", currentLoad=" + currentLoad +
                ", lastHeartbeat=" + lastHeartbeat +
                '}';
    }
}
