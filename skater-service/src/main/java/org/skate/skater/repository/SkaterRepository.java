package org.skate.skater.repository;

import org.skate.skater.model.Skater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkaterRepository extends JpaRepository<Skater, String> {
  List<Skater> findByUsernameIgnoreCase(String name);
}
