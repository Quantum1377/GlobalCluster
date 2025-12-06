package com.globalcluster.node;

public class NodeInfo {
    private String id;
    private String region;
    private int cpuCores;
    private int ramMB;

    public NodeInfo() {}
    public NodeInfo(String id, String region, int cpuCores, int ramMB) {
        this.id = id;
        this.region = region;
        this.cpuCores = cpuCores;
        this.ramMB = ramMB;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public int getCpuCores() { return cpuCores; }
    public void setCpuCores(int cpuCores) { this.cpuCores = cpuCores; }
    public int getRamMB() { return ramMB; }
    public void setRamMB(int ramMB) { this.ramMB = ramMB; }
}
