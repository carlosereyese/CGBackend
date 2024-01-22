package com.example.backend.services;

import com.example.backend.model.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class EmailService {

  @Value("${spring.mail.username}")
  private String fromEmail;

  private final JavaMailSender javaMailSender;

  public EmailService(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  public void sendEmail(EmailRequest emailRequest) throws MessagingException, IOException {
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

    helper.setFrom(fromEmail);
    helper.setTo(emailRequest.getToEmail());
    helper.setSubject("Movimiento detectado");

    byte[] imageBytes = Base64.getDecoder().decode(emailRequest.getBase64());

    helper.addAttachment("img.jpg", new ByteArrayDataSource(imageBytes, "image/jpg"));

    helper.setText("<html><body>" +
        "<p>Movimiento Detectado en: " + emailRequest.getMovimiento() + "</p>" +
        "<img src='cid:img.jpg'>" +
        "</body></html>", true);

    javaMailSender.send(mimeMessage);
  }

}
