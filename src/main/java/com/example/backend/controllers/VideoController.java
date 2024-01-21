package com.example.backend.controllers;

import com.example.backend.api.VideoApi;
import com.example.backend.model.EmailRequest;
import com.example.backend.model.Movements;
import com.example.backend.services.BackendService;
import com.example.backend.services.EmailService;
import com.example.backend.services.StreamingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.opencv.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

@RestController
@SecurityRequirement(name = "authenticate")
public class VideoController implements VideoApi {

  @Autowired
  private StreamingService service;
  @Autowired
  BackendService backendService;

  @Autowired
  EmailService emailService;

  @Override
  public ResponseEntity<InputStreamResource> streamVideo(String title) throws IOException {
    ClassPathResource classPathResource = new ClassPathResource("videos/" + title + ".mp4");

    InputStream videoStream = classPathResource.getInputStream();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);


    /*
    CompletableFuture.runAsync(() -> {
      try {
        detectMotion(videoStream, title);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

     */

    return new ResponseEntity<>(new InputStreamResource(videoStream), headers, HttpStatus.OK);
  }

  private static final double MOTION_THRESHOLD = 1000.0;
  // Motion detection method
  public boolean detectMotion(InputStream videoStream, String title) throws IOException {
    File tempVideoFile = File.createTempFile("tempVideo", ".mp4");
    try (OutputStream out = new FileOutputStream(tempVideoFile)) {
      IOUtils.copy(videoStream, out);
    }

    Boolean motionResult = false;
    VideoCapture videoCapture = new VideoCapture(tempVideoFile.getAbsolutePath());
    Mat currentFrame = new Mat();
    Mat previousFrame = new Mat();
    Mat difference = new Mat();
    Mat hierarchy = new Mat();

    if (videoCapture.isOpened()) {
      videoCapture.read(previousFrame);
      System.out.println("Detection starts.");

      while (videoCapture.read(currentFrame)) {
        // Calculate absolute difference between frames
        Core.absdiff(previousFrame, currentFrame, difference);

        // Convert difference to grayscale
        Imgproc.cvtColor(difference, difference, Imgproc.COLOR_BGR2GRAY);

        // Apply threshold to identify significant differences
        Imgproc.threshold(difference, difference, 30, 255, Imgproc.THRESH_BINARY);

        // Find contours in the binary image
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(difference, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Check for motion based on contour area
        for (MatOfPoint contour : contours) {
          double contourArea = Imgproc.contourArea(contour);
          if (contourArea > 0.0) { System.out.println("Contour = " + contourArea); }
          if (contourArea > MOTION_THRESHOLD) {
            // Motion detected, set the flag and capture the frame
            motionResult = true;
            byte[] image = convertMatToByteArray(currentFrame);

            instertMovementSendEmail(image, title);

            break;
          }
        }

        // Update previous frame for the next iteration
        currentFrame.copyTo(previousFrame);

        if (motionResult) {
          // Motion detected, no need to continue processing subsequent frames
          break;
        }
      }

      videoCapture.release();
    }

    // Delete the temporary video file
    tempVideoFile.delete();

    System.out.println("Detection finished.");
    return motionResult;
  }

  private byte[] convertMatToByteArray(Mat mat) {
    MatOfByte matOfByte = new MatOfByte();
    Imgcodecs.imencode(".jpg", mat, matOfByte);
    return matOfByte.toArray();
  }

  private void instertMovementSendEmail(byte[] image, String title) {
    Movements movements = new Movements();
    movements.setRoom(title);
    movements.setId_user("1");
    movements.setImage(image);
    LocalDateTime currentDateTime = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedDateTime = currentDateTime.format(formatter);
    movements.setDateTime(formattedDateTime);
    backendService.insert(movements);

    EmailRequest email = new EmailRequest();
    email.setToEmail("carlosereyese@gmail.com");
    email.setMovimiento("movimiento");
    email.setBase64(Base64.getEncoder().encodeToString(image));
  }
}