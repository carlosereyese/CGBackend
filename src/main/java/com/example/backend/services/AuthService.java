package com.example.backend.services;

import com.example.backend.model.AuthenticationRequest;
import com.example.backend.model.AuthenticationResponse;
import com.example.backend.model.RegisterRequest;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  @Autowired
  UserRepository userRepository;
  @Autowired
  PasswordEncoder passwordEncoder;
  @Autowired JwtService jwtService;
  public final AuthenticationManager authenticationManager;

  public AuthenticationResponse register(RegisterRequest registerRequest) {
    var user = User.builder()
        .username(registerRequest.getUsername())
        .password(passwordEncoder.encode(registerRequest.getPassword()))
        .email(registerRequest.getEmail())
        .build();
    userRepository.save(user);
    var jwtToken = jwtService.generateToken(user);
    return AuthenticationResponse.builder()
        .token(jwtToken)
        .build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
        authenticationRequest.getUsername(),
        authenticationRequest.getPassword()
    ));
    var user = userRepository.findByUsername(authenticationRequest.getUsername())
        .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    return AuthenticationResponse.builder()
        .token(jwtToken)
        .build();
  }

}
