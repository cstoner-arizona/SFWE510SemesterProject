package org.skate.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        .authorizeExchange(exchanges -> exchanges
            // Allow actuator endpoints without authentication
            .pathMatchers("/actuator/**").permitAll()
            // Allow Keycloak endpoints (for token retrieval during development)
            .pathMatchers("/oauth2/**", "/login/**").permitAll()
            // All other requests require authentication
            .anyExchange().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> {})
        )
        .csrf(csrf -> csrf.disable());

    return http.build();
  }
}
