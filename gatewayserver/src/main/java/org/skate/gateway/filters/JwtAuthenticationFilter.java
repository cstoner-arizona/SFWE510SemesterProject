package org.skate.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filter that extracts the user ID (sub claim) from the JWT token
 * and sets it as the X-Skater-Id header for downstream services.
 */
@Order(2)  // Run after TrackingFilter (Order 1)
@Component
public class JwtAuthenticationFilter implements GlobalFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  @Autowired
  private FilterUtils filterUtils;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .map(securityContext -> securityContext.getAuthentication())
        .filter(authentication -> authentication instanceof JwtAuthenticationToken)
        .cast(JwtAuthenticationToken.class)
        .map(JwtAuthenticationToken::getToken)
        .map(jwt -> {
          // Extract the 'sub' claim from the JWT - this is the skater ID
          String skaterId = jwt.getSubject();
          logger.debug("Extracted skaterId from JWT sub claim: {}", skaterId);

          // Set the X-Skater-Id header for downstream services
          return filterUtils.setSkaterId(exchange, skaterId);
        })
        .defaultIfEmpty(exchange)  // If no JWT (e.g., permitAll endpoints), continue without setting header
        .flatMap(chain::filter);
  }
}
