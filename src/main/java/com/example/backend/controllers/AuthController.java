package com.example.backend.controllers;

import com.example.backend.api.AuthApi;
import com.example.backend.model.AuthenticationRequest;
import com.example.backend.model.AuthenticationResponse;
import com.example.backend.model.RegisterRequest;
import com.example.backend.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

  @Autowired
  AuthService authService;

  @Override
  public ResponseEntity<AuthenticationResponse> register(RegisterRequest registerRequest) {
    return ResponseEntity.ok(authService.register(registerRequest));
  }

  @Override
  public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
    return ResponseEntity.ok(authService.authenticate(authenticationRequest));
  }
}
