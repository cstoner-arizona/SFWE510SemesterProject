package org.skate.skater.controller;

import java.util.List;

import org.skate.skater.model.Skater;
import org.skate.skater.service.SkaterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skater")
public class SkaterController {
  @Autowired
  private SkaterService skaterService;

  @PostMapping
  public ResponseEntity<Skater> createSkater(
      @RequestBody Skater skater) {
    Skater createdSkater = skaterService.createSkater(skater);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdSkater);
  }

  @GetMapping("/{skaterId}")
  public ResponseEntity<Skater> getSkater(@PathVariable String skaterId) {
    Skater skater = skaterService.getSkater(skaterId);
    return ResponseEntity.ok(skater);
  }

  @GetMapping("/all")
  public ResponseEntity<List<Skater>> getAllSkaters() {
    List<Skater> skaters = skaterService.getAllSkaters();
    return ResponseEntity.ok(skaters);
  }

  @PutMapping("/{skaterId}")
  public ResponseEntity<Skater> updateSkater(
      @PathVariable String skaterId,
      @RequestBody Skater skater) {
    Skater updatedSkater = skaterService.updateSkater(skater, skaterId);
    return ResponseEntity.ok(updatedSkater);
  }

  @DeleteMapping("/{skaterId}")
  public ResponseEntity<Void> deleteSkater(
      @PathVariable String skaterId) {
    skaterService.deleteSkater(skaterId);
    return ResponseEntity.noContent().build();
  }
}
