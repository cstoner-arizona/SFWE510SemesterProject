package org.skate.session.client;

import lombok.Data;

@Data
public class SkaterResponse {
  private String skaterId;
  private String username;
  private String displayName;
}
