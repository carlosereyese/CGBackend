package com.example.backend.api;

import com.example.backend.model.Movements;
import com.example.backend.model.Spaces;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

public interface BackendRestApi {

  @GetMapping(path="/movements/getAllMovements")
  List<Movements> getAllMovements();

  @PostMapping(path="/movements/insertMovement")
  Movements insertMovement(Movements movement);

  @GetMapping(path="/spaces/getAllSpaces")
  List<Spaces> getAllSpaces();

  @PostMapping(path="/spaces/insertSpaces")
  Spaces insertSpaces(Spaces space);
}
