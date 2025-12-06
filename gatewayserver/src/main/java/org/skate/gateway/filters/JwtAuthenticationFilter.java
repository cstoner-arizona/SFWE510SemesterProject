package org.skate.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
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

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .map(securityContext -> securityContext.getAuthentication())
        .filter(authentication -> authentication instanceof JwtAuthenticationToken)
        .cast(JwtAuthenticationToken.class)
        .map(JwtAuthenticationToken::getToken)
        .flatMap(jwt -> {
          // Extract the 'sub' claim from the JWT - this is the skater ID
          String skaterId = jwt.getSubject();
          logger.debug("Extracted skaterId from JWT sub claim: {}", skaterId);

          // Create a new request with mutable headers
          ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
              HttpHeaders headers = new HttpHeaders();
              headers.putAll(super.getHeaders());
              headers.add(FilterUtils.SKATER_ID, skaterId);
              return headers;
            }
          };

          return chain.filter(exchange.mutate().request(mutatedRequest).build());
        })
        .switchIfEmpty(chain.filter(exchange));  // If no JWT (e.g., permitAll endpoints), continue without setting header
  }
}
