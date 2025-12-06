package com.globalcluster.gateway;

public class RegionResolver {

    public static String getRegionByIp(String ip) {
        if(ip.startsWith("192.168.1")) return "America";
        if(ip.startsWith("192.168.2")) return "Europe";
        if(ip.startsWith("192.168.3")) return "Asia";
        if(ip.startsWith("192.168.4")) return "Africa";
        if(ip.startsWith("192.168.5")) return "Oceania";
        if(ip.startsWith("192.168.6")) return "Antarctica";
        if(ip.startsWith("192.168.7")) return "SouthAmerica";
        return "Unknown";
    }

    public static int getMasterPort(String region) {
        return switch(region) {
            case "America" -> 8081;
            case "Europe" -> 8082;
            case "Asia" -> 8083;
            case "Africa" -> 8084;
            case "Oceania" -> 8085;
            case "Antarctica" -> 8086;
            case "SouthAmerica" -> 8087;
            default -> 0;
        };
    }
}
