package com.globalcluster.dashboard.controller;

import com.globalcluster.dashboard.service.EmailService;
import com.globalcluster.dashboard.service.AuthSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthSessionService authSessionService;

    // ================
    // 1) Enviar código
    // ================
    @PostMapping("/send-code")
    public String sendCode(@RequestParam String email) {

        if (!email.equals("abneransilva@gmail.com")) {
            return "Apenas o dono do painel pode fazer login.";
        }

        int code = (int) (Math.random() * 900000 + 100000);  // 6 dígitos

        String subject = "Seu código de verificação";
        String body = "O código para fazer login no GlobalCluster é: " + code;
        emailService.sendEmail(email, subject, body);

        return authSessionService.createPendingSession(email, code);
    }

    // ======================
    // 2) Verificar o código
    // ======================
    @PostMapping("/verify")
    public String verify(@RequestParam String email,
                         @RequestParam int code) {

        if (authSessionService.verifyCode(email, code)) {
            String token = authSessionService.startSession(email);
            return token;
        }

        return "Código inválido.";
    }

    // ======================
    // 3) Checar login
    // ======================
    @GetMapping("/check")
    public boolean check(@RequestHeader("Authorization") String token) {
        return authSessionService.isLoggedIn(token);
    }
}
