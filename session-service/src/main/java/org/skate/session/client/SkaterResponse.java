package org.skate.session.client;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkaterResponse implements Serializable {
  private static final long serialVersionUID = 1L;

  private String skaterId;
  private String username;
  private String displayName;
}
