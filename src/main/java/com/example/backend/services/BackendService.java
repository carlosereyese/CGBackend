package com.example.backend.services;

import com.example.backend.model.Movements;
import com.example.backend.repository.MovementsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BackendService {

  @Autowired
  MovementsRepository movementsRepository;

  public List<Movements> getAllMovements() {
    return movementsRepository.findAll();
  }

  public Movements insert(Movements movement) { return movementsRepository.save(movement); }

}
