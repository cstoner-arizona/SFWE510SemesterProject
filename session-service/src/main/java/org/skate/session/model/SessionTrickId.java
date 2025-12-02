package org.skate.session.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionTrickId implements Serializable {
  @Column(name = "session_id")
  private String sessionId;

  @Column(name = "trick_number")
  private Integer trickNumber;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SessionTrickId that = (SessionTrickId) o;
    return Objects.equals(sessionId, that.sessionId) &&
           Objects.equals(trickNumber, that.trickNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sessionId, trickNumber);
  }
}
