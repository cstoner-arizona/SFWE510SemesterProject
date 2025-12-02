package org.skate.session.model;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "sessions")
public class Session extends RepresentationModel<Session> {
  @Id
  @Column(name = "session_id", nullable = false)
  @GeneratedValue(strategy = GenerationType.UUID)
  private String sessionId;

  @Column(name = "skater_id", nullable = false)
  private String skaterId;

  @Column(name = "spot_id", nullable = false)
  private String spotId;

  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  @Column(name = "end_time", nullable = true)
  private LocalDateTime endTime;

  @Column(name = "notes")
  private String notes;

  @Column(name = "weather")
  private String weather;

  @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<SessionTrick> sessionTricks = new ArrayList<>();

  @Column(name = "rating")
  private Integer rating;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public void addSessionTrick(SessionTrick trick) {
    sessionTricks.add(trick);
    trick.setSession(this);
  }

  public void removeSessionTrick(SessionTrick trick) {
    sessionTricks.remove(trick);
    trick.setSession(null);
  }
}
