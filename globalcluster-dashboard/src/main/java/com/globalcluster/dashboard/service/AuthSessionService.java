package com.globalcluster.dashboard.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthSessionService {

    private Set<String> authenticatedIps = new HashSet<>();

    public void authenticate(String ip) {
        authenticatedIps.add(ip);
    }

    public boolean isAuthenticated(String ip) {
        return authenticatedIps.contains(ip);
    }

    public void logout(String ip) {
        authenticatedIps.remove(ip);
    }
}
