package com.globalcluster.dashboard.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfig {

    private static final String ALLOWED_IP = "192.168.1.7";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/login", "/verify").permitAll()
                        .requestMatchers("/dashboard/**").access((authentication, context) -> {
                            HttpServletRequest req = context.getRequest();
                            String ip = req.getRemoteAddr();

                            if (ALLOWED_IP.equals(ip)) {
                                return Customizer.withDefaults().authorize(authentication).isAllowed();
                            }
                            return Customizer.withDefaults().authorize(authentication).isDenied();
                        })
                        .anyRequest().denyAll()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }
}
