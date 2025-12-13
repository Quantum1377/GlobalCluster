package com.globalcluster.dashboard.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthSessionService {

    // Armazena o código de verificação pendente para cada email
    private final Map<String, Integer> pendingSessions = new ConcurrentHashMap<>();
    
    // Armazena o token da sessão ativa para cada email
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();

    /**
     * Cria uma sessão pendente com um código de verificação.
     * @param email O email do usuário.
     * @param code O código de 6 dígitos enviado.
     * @return Uma mensagem indicando que o código foi enviado.
     */
    public String createPendingSession(String email, int code) {
        pendingSessions.put(email, code);
        // Em um cenário real, você definiria um tempo de expiração para o código.
        return "Código de verificação enviado para " + email;
    }

    /**
     * Verifica se o código fornecido corresponde ao código pendente.
     * @param email O email do usuário.
     * @param code O código fornecido.
     * @return true se o código for válido, false caso contrário.
     */
    public boolean verifyCode(String email, int code) {
        Integer expectedCode = pendingSessions.get(email);
        return expectedCode != null && expectedCode.equals(code);
    }

    /**
     * Inicia uma sessão ativa, gerando um token seguro.
     * @param email O email do usuário verificado.
     * @return O token da sessão.
     */
    public String startSession(String email) {
        pendingSessions.remove(email); // Remove o código de verificação usado
        String token = UUID.randomUUID().toString();
        activeSessions.put(email, token);
        return token;
    }

    /**
     * Verifica se o token fornecido é válido e corresponde a uma sessão ativa.
     * O token deve ser passado no formato "Bearer <token>".
     * @param bearerToken O cabeçalho de autorização.
     * @return true se o token for válido, false caso contrário.
     */
    public boolean isLoggedIn(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return false;
        }
        String token = bearerToken.substring(7);
        return activeSessions.containsValue(token);
    }

    /**
     * Encerra a sessão associada a um token.
     * @param bearerToken O token da sessão a ser encerrada.
     */
    public void logout(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return;
        }
        String token = bearerToken.substring(7);
        activeSessions.values().remove(token);
    }
}
