package com.example.backend.controllers;

import com.example.backend.api.EmailApi;
import com.example.backend.model.EmailRequest;
import com.example.backend.services.EmailService;
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

  @Override
  public void sendEmail(EmailRequest emailRequest) throws MessagingException, IOException {
    emailService.sendEmail(emailRequest.getToEmail(), emailRequest.getMovimiento(), emailRequest.getBase64());
  }

}
