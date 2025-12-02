package org.skate.session.client;

import lombok.Data;

@Data
public class SpotResponse {
  private String spotId;
  private String name;
  private String address;
  private Double latitude;
  private Double longitude;
}
