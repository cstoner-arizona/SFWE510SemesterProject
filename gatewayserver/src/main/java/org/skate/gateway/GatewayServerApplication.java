package org.skate.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(GatewayServerApplication.class, args);
  }

  @Bean
  public RouteLocator skateRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
        // Spot Service Routes
        .route("spot-service", r -> r
            .path("/api/spots/**")
            .uri("lb://spot-service"))

        // Skater Service Routes
        .route("skater-service", r -> r
            .path("/api/skater/**")
            .uri("lb://skater-service"))

        // Session Service Routes
        .route("session-service", r -> r
            .path("/api/session/**")
            .uri("lb://session-service"))

        .build();
  }
}
