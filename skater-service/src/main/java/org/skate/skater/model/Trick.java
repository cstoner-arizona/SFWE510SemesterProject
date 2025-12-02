package org.skate.skater.model;

import org.skate.skater.enums.*;

import java.util.Set;
import java.time.LocalDate;

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
@Table(name = "tricks")
public class Trick extends RepresentationModel<Trick> {
  @Id
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @ElementCollection(targetClass = TrickCategory.class)
  @CollectionTable(name = "trick_categories", joinColumns = @JoinColumn(name = "id"))
  @Column(name = "category")
  @Enumerated(EnumType.STRING)
  private Set<TrickCategory> categories;

  @Column(name = "difficulty")
  private Integer difficulty;

  @Column(name = "learned_at")
  private LocalDate learnedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "skater_id", nullable = false)
  @JsonIgnore
  private Skater skater;
}
