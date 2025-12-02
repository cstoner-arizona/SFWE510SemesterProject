package org.skate.skater.model;

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
public class TrickId implements Serializable {
  @Column(name = "skater_id")
  private String skaterId;

  @Column(name = "trick_number")
  private Integer trickNumber;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TrickId trickId = (TrickId) o;
    return Objects.equals(skaterId, trickId.skaterId) &&
           Objects.equals(trickNumber, trickId.trickNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(skaterId, trickNumber);
  }
}
