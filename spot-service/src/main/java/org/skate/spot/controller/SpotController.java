package org.skate.spot.controller;

import org.skate.spot.model.Spot;
import org.skate.spot.model.TrickAttempt;
import org.skate.spot.service.SpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/spots")
public class SpotController {

  @Autowired
  private SpotService spotService;

  // Create a new spot
  @PostMapping
  public ResponseEntity<Spot> createSpot(
      @RequestBody Spot spot,
      @RequestHeader("X-Skater-Id") String skaterId) { // TODO: Replace with actual auth

    Spot createdSpot = spotService.createSpot(spot, skaterId);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdSpot);
  }

  // Get spot by ID
  @GetMapping("/{spotId}")
  public ResponseEntity<Spot> getSpot(@PathVariable String spotId) {
    Spot spot = spotService.getSpot(spotId);
    return ResponseEntity.ok(spot);
  }

  // Get all spots
  @GetMapping
  public ResponseEntity<List<Spot>> getAllSpots() {
    List<Spot> spots = spotService.getAllSpots();
    return ResponseEntity.ok(spots);
  }

  // Update spot
  @PutMapping("/{spotId}")
  public ResponseEntity<Spot> updateSpot(
      @PathVariable String spotId,
      @RequestBody Spot spot,
      @RequestHeader("X-Skater-Id") String skaterId) { // TODO: Replace with actual auth

    Spot updatedSpot = spotService.updateSpot(spotId, spot, skaterId);
    return ResponseEntity.ok(updatedSpot);
  }

  // Delete spot
  @DeleteMapping("/{spotId}")
  public ResponseEntity<Void> deleteSpot(
      @PathVariable String spotId,
      @RequestHeader("X-Skater-Id") String skaterId) { // TODO: Replace with actual auth

    spotService.deleteSpot(spotId, skaterId);
    return ResponseEntity.noContent().build();
  }

  // Add trick attempt to spot
  @PostMapping("/{spotId}/trick-attempts")
  public ResponseEntity<List<TrickAttempt>> addTrickAttempt(
      @PathVariable String spotId,
      @RequestBody Map<String, String> request, // Just a simple map for trickName
      @RequestHeader("X-Skater-Id") String skaterId) { // TODO: Replace with actual auth

    String trickName = request.get("trickName");
    List<TrickAttempt> attempt = spotService.addTrickAttempt(spotId, skaterId, trickName);
    return ResponseEntity.status(HttpStatus.CREATED).body(attempt);
  }

  // Get trick attempts for a spot
  @GetMapping("/{spotId}/trick-attempts")
  public ResponseEntity<List<TrickAttempt>> getTrickAttemptsForSpot(@PathVariable String spotId) {
    List<TrickAttempt> attempts = spotService.getTrickAttemptsForSpot(spotId);
    return ResponseEntity.ok(attempts);
  }

  // Get spots by founder
  @GetMapping("/founder/{founderSkaterId}")
  public ResponseEntity<List<Spot>> getSpotsByFounder(@PathVariable String founderSkaterId) {
    List<Spot> spots = spotService.getSpotsByFounder(founderSkaterId);
    return ResponseEntity.ok(spots);
  }

  // Search spots by name
  @GetMapping("/search")
  public ResponseEntity<List<Spot>> searchSpots(@RequestParam String name) {
    List<Spot> spots = spotService.searchSpotsByName(name);
    return ResponseEntity.ok(spots);
  }

  // Get spots in geographic area
  @GetMapping("/area")
  public ResponseEntity<List<Spot>> getSpotsInArea(
      @RequestParam Double minLat,
      @RequestParam Double maxLat,
      @RequestParam Double minLon,
      @RequestParam Double maxLon) {

    List<Spot> spots = spotService.getSpotsInArea(minLat, maxLat, minLon, maxLon);
    return ResponseEntity.ok(spots);
  }
}
