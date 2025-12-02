package org.skate.session.repository;

import org.skate.session.model.SessionTrick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionTrickRepository extends JpaRepository<SessionTrick, Long> {
  SessionTrick findBySessionTrickId(Long sessionId);

  List<SessionTrick> findBySessionSessionId(Long sessionId);

  List<SessionTrick> findByTrickName(String trickName);

  // Find all tricks landed across all sessions
  List<SessionTrick> findByLandsGreaterThan(Integer minLands);

  // Count how many times a trick was attempted across ALL sessions
  @Query("SELECT SUM(st.attempts) FROM SessionTrick st WHERE st.trickName = :trickName")
  Integer getTotalAttemptsForTrick(@Param("trickName") String trickName);
}
