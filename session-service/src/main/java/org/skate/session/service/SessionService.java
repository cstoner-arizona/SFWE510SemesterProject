package org.skate.session.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.skate.session.client.SkaterServiceClient;
import org.skate.session.client.SpotServiceClient;
import org.skate.session.model.*;
import org.skate.session.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SessionService {
  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private SessionTrickRepository sessionTrickRepository;

  @Autowired
  private SpotServiceClient spotClient;

  @Autowired
  private SkaterServiceClient skaterClient;

  public Session startSession(String skaterId, String spotId, String weather, String notes) {
    // Check if any running active session has already started
    if (sessionRepository.existsBySkaterIdAndEndTimeIsNull(skaterId)) {
      throw new RuntimeException("You already have an active session. End it before starting a new one.");
    }

    // Check if skater for session even exists
    if (!skaterClient.exists(skaterId)) {
      throw new RuntimeException("Skater not found: " + skaterId);
    }

    // Check if spot exists for session
    if (!spotClient.exists(spotId)) {
      throw new RuntimeException("Spot not found: " + spotId);
    }

    // Create new session
    Session session = new Session();
    session.setSkaterId(skaterId);
    session.setSpotId(spotId);
    session.setStartTime(LocalDateTime.now());
    session.setWeather(weather);
    session.setNotes(notes);

    return sessionRepository.save(session);
  }

  public Session endSession(String sessionId, String skaterId, Integer rating) {
    Session session = getSession(sessionId);

    // Verify the skaterId
    if (!session.getSkaterId().equals(skaterId)) {
      throw new RuntimeException("Provided Skater ID does not match session's skater ID.");
    }

    // Check that session isn't already ended
    if (session.getEndTime() != null) {
      throw new RuntimeException("Session already ended");
    }

    session.setEndTime(LocalDateTime.now());
    if (rating != null) {
      session.setRating(rating);
    }

    return sessionRepository.save(session);
  }

  @Transactional(readOnly = true)
  public Session getSession(String sessionId) {
    return sessionRepository.findById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
  }

  @Transactional(readOnly = true)
  public List<Session> getSessionsBySkater(String skaterId) {
    return sessionRepository.findBySkaterId(skaterId);
  }

  @Transactional(readOnly = true)
  public List<Session> getSessionBySpot(String spotId) {
    return sessionRepository.findBySpotId(spotId);
  }

  @Transactional(readOnly = true)
  public Session getActiveSession(String skaterId) {
    Optional<Session> active_session = sessionRepository.findFirstBySkaterIdAndEndTimeIsNull(skaterId);
    return active_session.orElseThrow(() -> new RuntimeException("No Active Session Found: " + skaterId));
  }

  public SessionTrick addTrickToSession(String sessionId, String skaterId, SessionTrick trick) {
    Session session = getSession(sessionId);

    if (!session.getSkaterId().equals(skaterId)) {
      throw new RuntimeException("Provided skater ID does not match session skater ID");
    }

    if (session.getEndTime() != null) {
      throw new RuntimeException("Cannot add tricks to an ended session");
    }

    trick.setTimestamp(LocalDateTime.now());
    session.addSessionTrick(trick);
    sessionRepository.save(session);

    return trick;
  }

  @Transactional(readOnly = true)
  public List<SessionTrick> getTricksFromSession(String sessionId) {
    return sessionTrickRepository.findBySessionSessionId(sessionId);
  }

  public void deleteSession(String sessionId, String skaterId) {
    Session session = getSession(sessionId);

    if (!session.getSkaterId().equals(skaterId)) {
      throw new RuntimeException("Recieved skaterId does not match skaterId of session.");
    }

    sessionRepository.delete(session);
  }
}
