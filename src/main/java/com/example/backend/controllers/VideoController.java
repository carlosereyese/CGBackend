package com.example.backend.controllers;

import com.example.backend.api.VideoApi;
import com.example.backend.services.StreamingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
@SecurityRequirement(name = "authenticate")
public class VideoController implements VideoApi {
  @Autowired
  private StreamingService service;

  @Override
  public ResponseEntity<InputStreamResource> getVideo(String title) throws IOException {
    ClassPathResource classPathResource = new ClassPathResource("videos/" + title + ".mp4");

    InputStream videoStream = classPathResource.getInputStream();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("video/mp4"));
    return new ResponseEntity<>(new InputStreamResource(videoStream), headers, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<InputStreamResource> streamVideo(String title) throws IOException {
    ClassPathResource classPathResource = new ClassPathResource("videos/" + title + ".mp4");

    InputStream videoStream = classPathResource.getInputStream();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

    return new ResponseEntity<>(new InputStreamResource(videoStream), headers, HttpStatus.OK);
  }

}
