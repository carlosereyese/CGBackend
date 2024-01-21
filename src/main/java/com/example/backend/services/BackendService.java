package com.example.backend.services;

import com.example.backend.model.Movements;
import com.example.backend.model.Spaces;
import com.example.backend.repository.MovementsRepository;
import com.example.backend.repository.SpacesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BackendService {

  @Autowired
  MovementsRepository movementsRepository;

  @Autowired
  SpacesRepository spacesRepository;

  public List<Movements> getAllMovements() {
    return movementsRepository.findAll();
  }

  public Movements insert(Movements movement) { return movementsRepository.save(movement); }

  public List<Spaces> getAllSpaces() { return spacesRepository.findAll(); }

  public Spaces insert(Spaces space) { return spacesRepository.save(space); }

}
