package com.example.backend.controllers;

import com.example.backend.model.Chunk;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
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

  public WebSocketController(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @MessageMapping("/requestVideo/{roomId}")
  public void handleVideoRequest(@DestinationVariable String roomId) throws Exception {
    System.out.println("Requested room: " + roomId);
    extractFrames(roomId);
  }

  public void extractFrames(String title) throws IOException, InterruptedException {

    ClassPathResource classPathResource = new ClassPathResource("videos/" + title + ".mp4");

    VideoCapture videoCapture = new VideoCapture();
    videoCapture.open(classPathResource.getFile().getAbsolutePath());

    if (!videoCapture.isOpened()) {
      System.err.println("Error: Couldn't open video file.");
      return;
    }

    byte[] image = null;
    Mat frame = new Mat();
    while (videoCapture.read(frame)) {
      MatOfByte matOfByte = new MatOfByte();
      Imgcodecs.imencode(".jpg", frame, matOfByte);
      image = matOfByte.toArray();
      sendFrame(image, false);
    }

    sendFrame(image, true);

    videoCapture.release();
  }

  private void sendFrame(byte[] frame, boolean lastFrame) throws InterruptedException {
    Chunk chunk = new Chunk(lastFrame, frame);
    Thread.sleep(1/30);
    messagingTemplate.convertAndSend("/topic/getVideo", chunk); // Send the Chunk object
  }

}
