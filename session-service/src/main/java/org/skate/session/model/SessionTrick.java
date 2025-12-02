package org.skate.session.model;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "session_tricks")
public class SessionTrick extends RepresentationModel<SessionTrick> {
  @Id
  @Column(name = "id", nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "trick_name", nullable = false)
  private String trickName;

  @Column(name = "attempts", nullable = false)
  private Integer attempts;

  @Column(name = "lands")
  private Integer lands;

  @Column(name = "is_new_trick")
  private Boolean isNewTrick;

  @Column(name = "notes")
  private String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  @JsonIgnore
  private Session session;
}
