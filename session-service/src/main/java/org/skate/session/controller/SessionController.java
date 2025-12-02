package org.skate.session.controller;

import org.skate.session.model.*;
import org.skate.session.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/session")
public class SessionController {
  @Autowired
  private SessionService sessionService;

  /**
   * Start a new session
   * POST /api/sessions
   */
  @PostMapping
  public ResponseEntity<Session> startSession(
      @RequestBody Map<String, String> request,
      @RequestHeader("X-Skater-Id") String skaterId) {

    String spotId = request.get("spotId");
    String weather = request.get("weather");
    String notes = request.get("notes");

    Session session = sessionService.startSession(skaterId, spotId, weather, notes);
    return ResponseEntity.status(HttpStatus.CREATED).body(session);
  }

  /**
   * Stop an active session
   * PUT /api/sessions/{sessionId}/end
   */
  @PutMapping("/{sessionId}/end")
  public ResponseEntity<Session> endSession(
      @PathVariable String sessionId,
      @RequestBody(required = false) Map<String, Integer> request,
      @RequestHeader("X-Skater-Id") String skaterId) {

    Integer rating = (request != null) ? request.get("rating") : null;
    Session session = sessionService.endSession(sessionId, skaterId, rating);
    return ResponseEntity.ok(session);
  }

  /**
   * Get a session by ID
   * GET /api/sessions/{sessionId}
   */
  @GetMapping("/{sessionId}")
  public ResponseEntity<Session> getSession(
      @PathVariable String sessionId) {

    Session session = sessionService.getSession(sessionId);
    return ResponseEntity.ok(session);
  }

  /**
   * Get all session from a skater
   * GET /api/sessions/skater/{skaterId}
   */
  @GetMapping("/skater/{skaterId}")
  public ResponseEntity<List<Session>> getSessionsBySkater(
      @PathVariable String skaterId) {

    List<Session> sessions = sessionService.getSessionsBySkater(skaterId);
    return ResponseEntity.ok(sessions);
  }

  /**
   * Get active session from curent skater
   * Current skater defined as Skater ID in RequestHeader (X-Skater-Id)
   * GET /api/sessions/active
   */
  @GetMapping("/active")
  public ResponseEntity<Session> getActiveSession(
      @RequestHeader("X-Skater-Id") String skaterId) {

    Session session = sessionService.getActiveSession(skaterId);
    return ResponseEntity.ok(session);
  }
}
