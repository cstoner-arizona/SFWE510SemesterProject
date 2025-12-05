package org.skate.spot.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class UserContext {
  public static final String CORRELATION_ID = "tmx-correlation-id";
  public static final String AUTH_TOKEN = "Authorization";
  public static final String SKATER_ID = "X-Skater-Id";

  private static final ThreadLocal<String> correlationId = new ThreadLocal<String>();
  private static final ThreadLocal<String> authToken = new ThreadLocal<String>();
  private static final ThreadLocal<String> skaterId = new ThreadLocal<String>();

  public static String getCorrelationId() {
    return correlationId.get();
  }

  public static void setCorrelationId(String cid) {
    correlationId.set(cid);
  }

  public static String getAuthToken() {
    return authToken.get();
  }

  public static void setAuthToken(String aToken) {
    authToken.set(aToken);
  }

  public static String getSkaterId() {
    return skaterId.get();
  }

  public static void setSkaterId(String sid) {
    skaterId.set(sid);
  }

  public static HttpHeaders getHttpHeaders() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(CORRELATION_ID, getCorrelationId());
    return httpHeaders;
  }
}
