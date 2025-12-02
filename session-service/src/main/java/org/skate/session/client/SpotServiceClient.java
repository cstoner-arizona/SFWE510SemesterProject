package org.skate.session.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SpotServiceClient {
  private final RestClient restClient;

  public SpotServiceClient(@Value("${services.spot.url}") String baseUrl) {
    this.restClient = RestClient.builder()
        .baseUrl(baseUrl)
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

  public SpotResponse getSpot(String spotId) {
    return restClient.get()
        .uri("/api/spots/{id}", spotId)
        .retrieve()
        .body(SpotResponse.class);
  }
}
