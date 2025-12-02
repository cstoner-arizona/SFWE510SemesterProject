package org.skate.session.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SkaterServiceClient {
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

  public SkaterResponse getSkater(String skaterId) {
    return restClient.get()
        .uri("/api/skater/{id}", skaterId)
        .retrieve()
        .body(SkaterResponse.class);
  }
}
