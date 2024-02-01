package com.example.backend.controllers;

import com.example.backend.model.Chunk;
import com.example.backend.model.EmailRequest;
import com.example.backend.model.Movements;
import com.example.backend.services.BackendService;
import com.example.backend.services.EmailService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;

@Controller
@SecurityRequirement(name = "authenticate")
public class WebSocketController {

  private final SimpMessagingTemplate messagingTemplate;
  private Mat imag;
  @Autowired
  BackendService backendService;

  @Autowired
  EmailService emailService;

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

  private ArrayList<Rect> detection_contours(Mat outmat) {
    Mat v = new Mat();
    Mat vv = outmat.clone();
    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    Imgproc.findContours(vv, contours, v, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

    double maxArea = 100;
    int maxAreaIdx = -1;
    Rect r = null;
    ArrayList<Rect> rect_array = new ArrayList<Rect>();

    for (int idx = 0; idx < contours.size(); idx++) {
      Mat contour = contours.get(idx);
      double contourarea = Imgproc.contourArea(contour);
      if (contourarea > maxArea) {
        maxAreaIdx = idx;
        r = Imgproc.boundingRect(contours.get(maxAreaIdx));
        rect_array.add(r);
        Imgproc.drawContours(imag, contours, maxAreaIdx, new Scalar(0, 0, 255));
      }
    }

    v.release();

    return rect_array;
  }

  private void instertMovementSendEmail(Mat mat, String title) {
    MatOfByte matOfByte = new MatOfByte();
    Imgcodecs.imencode(".jpg", mat, matOfByte);
    byte[] image = matOfByte.toArray();
    LocalDateTime currentDateTime = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedDateTime = currentDateTime.format(formatter);

    try {
      Movements movements = new Movements();
      movements.setRoom(title);
      movements.setId_user("1");
      movements.setImage(image);
      movements.setDateTime(formattedDateTime);
      backendService.insert(movements);
      System.out.println("Movement inserted in bd.");
    }
    catch (Exception e) {
      System.out.println("Failed to insert movement in bd.");
    }

    try {
      EmailRequest email = new EmailRequest();
      email.setToEmail("carlosereyese@gmail.com");
      email.setMovimiento(title + " a las: " + formattedDateTime);
      email.setBase64(Base64.getEncoder().encodeToString(image));
      emailService.sendEmail(email);
      System.out.println("Email sent.");
    } catch (Exception e) {
      System.out.println("Failed to send email.");
    }
  }

}
