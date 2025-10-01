package org.skate.spot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.skate.spot.model.Spot;

@Getter
@Setter
@ToString
@Entity
@Table(name = "trick_attempts")
public class TrickAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="skater_id", nullable = false)
    private String skaterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    @JsonIgnore
    private Spot spot;

    private String trickName;
}
