package org.skate.skater.repository;

import org.skate.skater.model.Skater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkaterRepository extends JpaRepository<Skater, String> {
  Skater findBySkaterId(String skaterId);

  List<Skater> findByNameIgnoreCase(String name);

}
