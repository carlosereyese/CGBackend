package com.example.backend.api;

import com.example.backend.model.EmailRequest;
import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

public interface EmailApi {
  @PostMapping(value = "/email/send")
  void sendEmail(@RequestBody EmailRequest emailRequest) throws MessagingException, IOException;
}
