package com.example.backend.controllers;

import com.example.backend.api.BackendRestApi;
import com.example.backend.model.Movements;
import com.example.backend.services.BackendService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SecurityRequirement(name = "authenticate")
public class BackendRestController implements BackendRestApi {

  @Autowired
  BackendService backendService;

  @Override
  public List<Movements> getAllMovements() {
    return backendService.getAllMovements();
  }

  @Override
  public Movements insertMovement(Movements movement) { return backendService.insert(movement); }

}
