package com.example.backend.controllers;

import com.example.backend.model.Chunk;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
@SecurityRequirement(name = "authenticate")
public class WebSocketController {

  private final SimpMessagingTemplate messagingTemplate;
  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private static final int CHUNK_SIZE = 1024;

  public WebSocketController(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @MessageMapping("/requestVideo/{roomId}")
  public void handleVideoRequest(@DestinationVariable String roomId) throws Exception {
    System.out.println("Requested room: " + roomId);

    ClassPathResource classPathResource = new ClassPathResource("videos/" + roomId + ".mp4");

    InputStream videoStream = classPathResource.getInputStream();

    executorService.scheduleAtFixedRate(() -> {
      try {
        byte[] buffer = new byte[CHUNK_SIZE];
        int bytesRead = videoStream.read(buffer);

        if (bytesRead != -1) {
          ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
          boolean isLastChunk = bytesRead < CHUNK_SIZE; // Check if it's the last chunk
          sendChunk(byteBuffer, isLastChunk);
        } else {
          // End of video, close the connection or handle accordingly
          System.out.println("Video sent.");
          executorService.shutdown();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }, 0, 1000, TimeUnit.MILLISECONDS);
  }

  private void sendChunk(ByteBuffer data, boolean lastChunk) {
    Chunk chunk = new Chunk(true, data);
    messagingTemplate.convertAndSend("/topic/getVideo", chunk); // Send the Chunk object
  }

}
