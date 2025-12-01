package org.skate.spot.repository;

import org.skate.spot.model.TrickAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrickAttemptRepository extends JpaRepository<TrickAttempt, Long> {
    List<TrickAttempt> findBySpot_SpotId(String spotId);
    List<TrickAttempt> findBySkaterId(String skaterId);

    @Query("SELECT ta FROM TrickAttempt ta WHERE ta.spot.spotId = :spotId AND ta.skaterId = :skaterId")
    List<TrickAttempt> findBySpotAndSkater(@Param("spotId") String spotId, @Param("skaterId") String skaterId);
}
