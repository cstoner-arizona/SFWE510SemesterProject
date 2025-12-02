package org.skate.session.repository;

import org.skate.session.model.SessionTrick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionTrickRepository extends JpaRepository<SessionTrick, Long> {
  SessionTrick findBySessionTrickId(Long sessionId);
}
