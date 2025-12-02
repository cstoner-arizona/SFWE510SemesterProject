package org.skate.spot.service;

import org.skate.spot.model.Spot;
import org.skate.spot.model.TrickAttempt;
import org.skate.spot.repository.SpotRepository;
import org.skate.spot.repository.TrickAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SpotService {

  @Autowired
  private SpotRepository spotRepository;

  @Autowired
  private TrickAttemptRepository trickAttemptRepository;

  public Spot createSpot(Spot spot, String founderSkaterId) {
    spot.setFounderSkaterId(founderSkaterId);
    return spotRepository.save(spot);
  }

  @Transactional(readOnly = true)
  public Spot getSpot(String spotId) {
    return spotRepository.findById(spotId)
        .orElseThrow(() -> new RuntimeException("Spot not found: " + spotId));
  }

  @Transactional(readOnly = true)
  public List<Spot> getAllSpots() {
    return spotRepository.findAll();
  }

  public Spot updateSpot(String spotId, Spot updatedSpot, String requestingSkaterId) {
    Spot spot = getSpot(spotId);

    // Optional: Check if requester is the founder
    if (!spot.getFounderSkaterId().equals(requestingSkaterId)) {
      throw new RuntimeException("Only the spot founder can update this spot");
    }

    if (updatedSpot.getName() != null)
      spot.setName(updatedSpot.getName());
    if (updatedSpot.getAddress() != null)
      spot.setAddress(updatedSpot.getAddress());
    if (updatedSpot.getLatitude() != null)
      spot.setLatitude(updatedSpot.getLatitude());
    if (updatedSpot.getLongitude() != null)
      spot.setLongitude(updatedSpot.getLongitude());
    if (updatedSpot.getTypes() != null)
      spot.setTypes(updatedSpot.getTypes());
    if (updatedSpot.getDifficultyRating() != null)
      spot.setDifficultyRating(updatedSpot.getDifficultyRating());
    if (updatedSpot.getSurfaceQualityRating() != null)
      spot.setSurfaceQualityRating(updatedSpot.getSurfaceQualityRating());
    if (updatedSpot.getDescription() != null)
      spot.setDescription(updatedSpot.getDescription());
    if (updatedSpot.getIdealSkateTime() != null)
      spot.setIdealSkateTime(updatedSpot.getIdealSkateTime());
    if (updatedSpot.getPhotoUrls() != null)
      spot.setPhotoUrls(updatedSpot.getPhotoUrls());

    spot.setUpdatedAt(LocalDateTime.now());

    return spotRepository.save(spot);
  }

  public void deleteSpot(String spotId, String requestingSkaterId) {
    Spot spot = getSpot(spotId);

    // Optional: Check if requester is the founder
    if (!spot.getFounderSkaterId().equals(requestingSkaterId)) {
      throw new RuntimeException("Only the spot founder can delete this spot");
    }

    spotRepository.delete(spot);
  }

  public List<TrickAttempt> addTrickAttempt(String spotId, String skaterId, String trickName) {
    Spot spot = getSpot(spotId);

    TrickAttempt attempt = new TrickAttempt();
    attempt.setSkaterId(skaterId);
    attempt.setTrickName(trickName);

    spot.addTrickAttempt(attempt);
    spotRepository.save(spot);

    return getTrickAttemptsForSpot(spot.getSpotId());
  }

  @Transactional(readOnly = true)
  public List<TrickAttempt> getTrickAttemptsForSpot(String spotId) {
    return trickAttemptRepository.findBySpot_SpotId(spotId);
  }

  @Transactional(readOnly = true)
  public List<Spot> getSpotsByFounder(String founderSkaterId) {
    return spotRepository.findByFounderSkaterId(founderSkaterId);
  }

  @Transactional(readOnly = true)
  public List<Spot> searchSpotsByName(String name) {
    return spotRepository.findByNameContainingIgnoreCase(name);
  }

  @Transactional(readOnly = true)
  public List<Spot> getSpotsInArea(Double minLat, Double maxLat, Double minLon, Double maxLon) {
    return spotRepository.findSpotsInArea(minLat, maxLat, minLon, maxLon);
  }
}
