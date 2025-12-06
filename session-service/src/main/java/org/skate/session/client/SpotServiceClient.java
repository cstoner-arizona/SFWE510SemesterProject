package org.skate.session.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SpotServiceClient {
  private static final Logger logger = LoggerFactory.getLogger(SpotServiceClient.class);

  private final RestClient restClient;

  public SpotServiceClient(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder
        .baseUrl("http://spot-service")
        .build();
  }

  // Checks if a given spotId exists in spot service
  public boolean exists(String spotId) {
    try {
      restClient.get()
          .uri("/api/spots/{id}", spotId)
          .retrieve()
          .toBodilessEntity();
      return true;
    } catch (RestClientException e) {
      return false;
    }
  }

  @Cacheable(value = "spots", key = "#spotId")
  public SpotResponse getSpot(String spotId) {
    logger.debug("Fetching spot {} from spot-service (cache miss)", spotId);
    return restClient.get()
        .uri("/api/spots/{id}", spotId)
        .retrieve()
        .body(SpotResponse.class);
  }

  @CacheEvict(value = "spots", key = "#spotId")
  public void evictSpotCache(String spotId) {
    logger.debug("Evicting spot {} from cache", spotId);
  }
}
