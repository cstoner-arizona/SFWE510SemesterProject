package org.skate.session.repository;

import org.skate.session.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
  Session findBySessionId(String sessionId);

  List<Session> findBySkaterId(String skaterId);

  List<Session> findBySpotId(String spotId);

  boolean existsBySkaterIdAndEndTimeIsNull(String skaterId);

  Optional<Session> findFirstBySkaterIdAndEndTimeIsNull(String skaterId);
}
