package org.skate.gateway.filters;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Order
@Component
public class TrackingFilter implements GlobalFilter {
  private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);

  @Autowired
  FilterUtils filterUtils;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
    if (isCorrelationIdPresent(requestHeaders)) {
      logger.debug("tmx-correlation-id found in tracking filter: {}",
          filterUtils.getCorrelationId(requestHeaders));
    } else {
      String correlationId = UUID.randomUUID().toString();
      exchange = filterUtils.setCorrelationId(exchange, correlationId);
      logger.debug("tmx-correlation-id generated in tracking filter: {}", correlationId);
    }
    return chain.filter(exchange);
  }

  private boolean isCorrelationIdPresent(HttpHeaders requestHeaders) {
    return filterUtils.getCorrelationId(requestHeaders) != null ? true : false;
  }
}
