package dir.controllers;

import dir.api.AuthApi;
import dir.model.AuthenticationRequest;
import dir.model.AuthenticationResponse;
import dir.model.RegisterRequest;
import dir.services.AuthService;
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
