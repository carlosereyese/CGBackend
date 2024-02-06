package dir.controllers;

import dir.api.EmailApi;
import dir.model.EmailRequest;
import dir.services.AuthService;
import dir.services.EmailService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@SecurityRequirement(name = "authenticate")
public class EmailController implements EmailApi {

  @Autowired
  private EmailService emailService;

  @Autowired
  AuthService authService;

  @Override
  public void sendEmail(EmailRequest emailRequest) throws MessagingException, IOException {
    emailRequest.setToEmail(authService.getEmail());
    emailService.sendEmail(emailRequest);
  }

}
