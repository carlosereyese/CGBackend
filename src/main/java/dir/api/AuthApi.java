package dir.api;

import dir.model.AuthenticationRequest;
import dir.model.AuthenticationResponse;
import dir.model.RegisterRequest;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

public interface AuthApi {

  @PostMapping(value = "/auth/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest registerRequest);

  @PostMapping(value = "/auth/authenticate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest);


}
