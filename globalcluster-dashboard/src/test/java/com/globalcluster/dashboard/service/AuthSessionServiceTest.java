package com.globalcluster.dashboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AuthSessionServiceTest {

    private AuthSessionService authSessionService;

    @BeforeEach
    void setUp() {
        authSessionService = new AuthSessionService();
    }

    @Test
    void testAuthenticationLifecycle() {
        String email = "test@example.com";
        int correctCode = 123456;
        int wrongCode = 654321;

        // 1. Criação da sessão pendente
        authSessionService.createPendingSession(email, correctCode);
        
        // 2. Verificação do código
        assertFalse(authSessionService.verifyCode(email, wrongCode), "Deveria falhar com o código errado");
        assertTrue(authSessionService.verifyCode(email, correctCode), "Deveria ter sucesso com o código correto");

        // 3. Início da sessão
        String token = authSessionService.startSession(email);
        assertNotNull(token, "O token não deveria ser nulo");
        assertFalse(token.isEmpty(), "O token não deveria estar vazio");
        String bearerToken = "Bearer " + token;

        // O código pendente deve ser removido após o uso
        assertFalse(authSessionService.verifyCode(email, correctCode), "O código pendente deveria ter sido removido");

        // 4. Validação do token
        assertTrue(authSessionService.isLoggedIn(bearerToken), "Deveria estar logado com um token válido");
        assertFalse(authSessionService.isLoggedIn("Bearer " + UUID.randomUUID()), "Não deveria estar logado com um token aleatório");
        assertFalse(authSessionService.isLoggedIn(null), "Não deveria estar logado com token nulo");
        assertFalse(authSessionService.isLoggedIn("InvalidFormat"), "Não deveria estar logado com um token mal formatado");
        
        // 5. Logout
        authSessionService.logout(bearerToken);
        assertFalse(authSessionService.isLoggedIn(bearerToken), "Deveria ter deslogado após o logout");
    }
}
