package com.globalcluster.dashboard.controller;

import com.globalcluster.dashboard.service.AuthSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private AuthSessionService authSessionService;

    @GetMapping
    public String dashboard(@RequestHeader("Authorization") String token) {

        if (!authSessionService.isLoggedIn(token)) {
            return "ACESSO NEGADO: faça login no /auth/send-code e /auth/verify";
        }

        return "Bem-vindo ao painel GlobalCluster! (Acesso AUTORIZADO)";
    }
}
