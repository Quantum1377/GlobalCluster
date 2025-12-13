package com.globalcluster.gateway;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebServerConfig {

    // Main application port (default 8080 for dashboard)
    @Value("${server.port:8080}")
    private int serverPort;

    // Additional ports for continent-specific services
    private static final List<Integer> CONTINENT_PORTS = List.of(8081, 8082, 8083, 8084, 8085, 8086);

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            List<Connector> additionalConnectors = new ArrayList<>();
            for (Integer port : CONTINENT_PORTS) {
                if (port != serverPort) { // Avoid duplicating the main port
                    Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
                    connector.setScheme("http");
                    connector.setPort(port);
                    additionalConnectors.add(connector);
                    System.out.println("Adicionando porta de conector: " + port); // Debugging
                }
            }
            factory.addAdditionalTomcatConnectors(additionalConnectors.toArray(new Connector[0]));
        };
    }
}
