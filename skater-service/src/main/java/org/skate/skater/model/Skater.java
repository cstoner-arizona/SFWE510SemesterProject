package org.skate.skater.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.skate.skater.enums.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "skaters")
public class Skater extends RepresentationModel<Skater> {
  @Id
  @Column(name = "skater_id", nullable = false)
  @GeneratedValue(strategy = GenerationType.UUID)
  private String skaterId;

  @Column(name = "username", nullable = false)
  private String username;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Column(name = "bio", length = 160)
  private String bio;

  @Column(name = "skill_level")
  @Enumerated(EnumType.STRING)
  private SkillLevel skillLevel;

  @Column(name = "stance")
  @Enumerated(EnumType.STRING)
  private Stance stance;

  @Column(name = "hometown")
  private String hometown;

  @Column(name = "profile_photo_url")
  private String profilePhotoUrl;

  @Column(name = "tricks")
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "trick", joinColumns = @JoinColumn(name = "skater_id"))
  @ElementCollection(targetClass = Trick.class)
  private List<Trick> tricks;

  @Column(name = "created_at")
  private LocalDateTime created_at;

  @Column(name = "updated_at")
  private LocalDateTime updated_at;

  public void updateFrom(Skater other) {
    if (other.getUsername() != null)
      this.username = other.getUsername();
    if (other.getEmail() != null)
      this.email = other.getEmail();
    if (other.getDisplayName() != null)
      this.displayName = other.getDisplayName();
    if (other.getBio() != null)
      this.bio = other.getBio();
    if (other.getSkillLevel() != null && other.getSkillLevel() instanceof SkillLevel)
      this.skillLevel = other.getSkillLevel();
    if (other.getStance() != null && other.getStance() instanceof Stance)
      this.stance = other.getStance();
    if (other.getHometown() != null)
      this.hometown = other.getHometown();
    if (other.getProfilePhotoUrl() != null)
      this.profilePhotoUrl = other.getProfilePhotoUrl();
    if (other.getTricks() != null && other.getTricks() instanceof List<Trick>)
      this.tricks = other.getTricks();

    this.updated_at = LocalDateTime.now();
  }
}
