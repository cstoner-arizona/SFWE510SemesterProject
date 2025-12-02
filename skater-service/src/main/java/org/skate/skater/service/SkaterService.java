package org.skate.skater.service;

import org.skate.skater.model.*;
import org.skate.skater.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SkaterService {
  @Autowired
  private SkaterRepository skaterRepository;

  public Skater createSkater(Skater skater) {
    skater.setCreatedAt(LocalDateTime.now());
    return skaterRepository.save(skater);
  }

  @Transactional(readOnly = true)
  public Skater getSkater(String skaterId) {
    return skaterRepository.findById(skaterId)
        .orElseThrow(() -> new RuntimeException("Skater not found: " + skaterId));
  }

  @Transactional(readOnly = true)
  public List<Skater> getAllSkaters() {
    return skaterRepository.findAll();
  }

  public Skater updateSkater(Skater updateSkater, String skaterId) throws RuntimeException {
    Skater foundSkater = getSkater(skaterId);
    foundSkater.updateFrom(updateSkater);
    skaterRepository.save(foundSkater);
    return foundSkater;
  }

  public void deleteSkater(String skaterId) {
    skaterRepository.deleteById(skaterId);
  }
}
