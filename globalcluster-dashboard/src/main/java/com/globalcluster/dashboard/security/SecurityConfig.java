package com.globalcluster.dashboard.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.security.core.Authentication;


@Configuration
public class SecurityConfig {

    @Value("${dashboard.allowed-ips}")
    private String allowedIpsString;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/login", "/verify").permitAll()
                        .requestMatchers("/dashboard/**").access(this::isAllowedIp)
                        .anyRequest().denyAll()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    private AuthorizationDecision isAllowedIp(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        String remoteIp = context.getRequest().getRemoteAddr();
        List<String> allowedIps = Arrays.asList(allowedIpsString.split(","));
        boolean isAllowed = allowedIps.contains(remoteIp);
        return new AuthorizationDecision(isAllowed);
    }
}
