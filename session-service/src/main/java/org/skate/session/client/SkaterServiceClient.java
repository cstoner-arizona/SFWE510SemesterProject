package org.skate.session.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SkaterServiceClient {
  private final RestClient restClient;

  public SkaterServiceClient(@Value("${services.skater.url}") String baseUrl) {
    this.restClient = RestClient.builder()
        .baseUrl(baseUrl)
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
