package org.skate.spot.model;

import org.hibernate.annotations.CreationTimestamp;
import org.skate.spot.enums.SpotType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@Table(name = "spots")
public class Spot extends RepresentationModel<Spot> {
  @Id
  @Column(name = "spot_id", nullable = false)
  @GeneratedValue(strategy = GenerationType.UUID)
  private String spotId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "founder_skater_id", nullable = false)
  private String founderSkaterId;

  @Column(name = "address")
  private String address;

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  @ElementCollection(targetClass = SpotType.class)
  @CollectionTable(name = "spot_types", joinColumns = @JoinColumn(name = "spot_id"))
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private Set<SpotType> types;

  @Column(name = "difficulty_rating")
  private Integer difficultyRating; // Scale from 1 to 10

  @Column(name = "surface_quality_rating")
  private Integer surfaceQualityRating; // Scale from 1 to 10

  @Column(name = "description", length = 2000)
  private String description;

  @Column(name = "ideal_skate_time")
  private String idealSkateTime; // e.g., "Afternoon", "Morning", "Evening"

  @ElementCollection
  @CollectionTable(name = "spot_photos", joinColumns = @JoinColumn(name = "spot_id"))
  @Column(name = "photo_url")
  private List<String> photoUrls; // URLs to photos of the spot

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "spot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<TrickAttempt> trickAttempts = new ArrayList<>();

  public void addTrickAttempt(TrickAttempt trickAttempt) {
    trickAttempts.add(trickAttempt);
    trickAttempt.setSpot(this);
  }

  public void removeTrickAttempt(TrickAttempt trickAttempt) {
    trickAttempts.remove(trickAttempt);
    trickAttempt.setSpot(null);
  }
}
