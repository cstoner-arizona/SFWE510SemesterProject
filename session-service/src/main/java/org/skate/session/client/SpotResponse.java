package org.skate.session.client;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpotResponse implements Serializable {
  private static final long serialVersionUID = 1L;

  private String spotId;
  private String name;
  private String address;
  private Double latitude;
  private Double longitude;
}
