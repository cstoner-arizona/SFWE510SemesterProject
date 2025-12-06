package org.skate.session.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SkaterServiceClient {
  private static final Logger logger = LoggerFactory.getLogger(SkaterServiceClient.class);

  private final RestClient restClient;

  public SkaterServiceClient(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder
        .baseUrl("http://skater-service")
        .build();
  }

  public boolean exists(String skaterId) {
    try {
      restClient.get()
          .uri("api/skater/{id}", skaterId)
          .retrieve()
          .toBodilessEntity();
      return true;
    } catch (RestClientException e) {
      return false;
    }
  }

  @Cacheable(value = "skaters", key = "#skaterId")
  public SkaterResponse getSkater(String skaterId) {
    logger.debug("Fetching skater {} from skater-service (cache miss)", skaterId);
    return restClient.get()
        .uri("/api/skater/{id}", skaterId)
        .retrieve()
        .body(SkaterResponse.class);
  }

  @CacheEvict(value = "skaters", key = "#skaterId")
  public void evictSkaterCache(String skaterId) {
    logger.debug("Evicting skater {} from cache", skaterId);
  }
}
