package com.example.backend.api;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

public interface VideoApi {

  @GetMapping(value = "/video/getVideo")
  ResponseEntity<InputStreamResource> getVideo(@RequestParam String title) throws IOException;

  @GetMapping(value = "/video/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  ResponseEntity<InputStreamResource> streamVideo(@RequestParam String title) throws IOException;
}
