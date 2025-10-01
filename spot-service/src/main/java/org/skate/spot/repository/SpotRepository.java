package org.skate.spot.repository;

import org.skate.spot.model.Spot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpotRepository extends JpaRepository<Spot, String> {
    List<Spot> findByFounderSkaterId(String founderSkaterId);
    List<Spot> findByNameContainingIgnoreCase(String name);

    @Query("""
        SELECT s FROM Spot s 
        WHERE s.latitude BETWEEN :minLat AND :maxLat 
        AND s.longitude BETWEEN :minLon AND :maxLon
        """)
    List<Spot> findSpotsInArea(
        @Param("minLat") double minLat,
        @Param("maxLat") double maxLat,
        @Param("minLon") double minLon,
        @Param("maxLon") double maxLon
    );

    @Query("SELECT s FROM Spot s LEFT JOIN FETCH s.trickAttempts WHERE s.spotId = :spotId")
    Optional<Spot> findByIdWithTrickAttempts(@Param("spotId") String spotId);

    List<Spot> findByDifficultyRating(Integer difficultyRating);

    List<Spot> findBySurfaceQualityRatingGreaterThanEqual(Integer surfaceQualityRating);
}
