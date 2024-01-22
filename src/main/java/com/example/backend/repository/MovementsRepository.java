package com.example.backend.repository;

import com.example.backend.model.Movements;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovementsRepository extends JpaRepository<Movements, Integer> {

  List<Movements> findAll(Sort sort);

}
