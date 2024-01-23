package com.example.backend.controllers;

import com.example.backend.api.VideoApi;
import com.example.backend.model.EmailRequest;
import com.example.backend.model.Movements;
import com.example.backend.services.BackendService;
import com.example.backend.services.EmailService;
import com.example.backend.services.StreamingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.mail.MessagingException;
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
import java.util.Iterator;
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

  private Mat imag;

  @Override
  public ResponseEntity<InputStreamResource> streamVideo(String title) throws IOException {
    ClassPathResource classPathResource = new ClassPathResource("videos/" + title + ".mp4");

    InputStream videoStream = classPathResource.getInputStream();

    CompletableFuture.runAsync(() -> {
      try {
        detectMotion(title);
      } catch (IOException | MessagingException e) {
        throw new RuntimeException(e);
      }
    });


    System.out.println("Sending video.");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    InputStreamResource inputStreamResource = new InputStreamResource(videoStream);
    return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
  }


  public void detectMotion(String title) throws IOException, MessagingException {
    System.out.println("Detection started");

    ClassPathResource classPathResource = new ClassPathResource("videos/" + title + ".mp4");
    InputStream videoStream = classPathResource.getInputStream();
    File tempVideoFile = File.createTempFile("tempVideo", ".mp4");
    try (OutputStream out = new FileOutputStream(tempVideoFile)) {
      IOUtils.copy(videoStream, out);
    }
    System.out.println("Created temporary file.");

    VideoCapture videoCapture = new VideoCapture(tempVideoFile.getAbsolutePath());

    Mat frame = new Mat();
    Mat outerBox = new Mat();
    Mat diff_frame = null;
    Mat tempon_frame = null;
    ArrayList<Rect> array = new ArrayList<Rect>();
    Size sz = new Size(640, 480);
    int i = 0;

    boolean movementDetected = false;
    System.out.println("Opening video capture.");
    while (videoCapture.isOpened() && !movementDetected) {
      if (videoCapture.read(frame) ) {
        Imgproc.resize(frame, frame, sz);
        imag = frame.clone();
        outerBox = new Mat(frame.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(frame, outerBox, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(outerBox, outerBox, new Size(3, 3), 0);

        if (i == 0) {
          diff_frame = new Mat(outerBox.size(), CvType.CV_8UC1);
          tempon_frame = new Mat(outerBox.size(), CvType.CV_8UC1);
          diff_frame = outerBox.clone();
        }

        if (i == 1) {
          Core.subtract(outerBox, tempon_frame, diff_frame);
          Imgproc.adaptiveThreshold(diff_frame, diff_frame, 255,
              Imgproc.ADAPTIVE_THRESH_MEAN_C,
              Imgproc.THRESH_BINARY_INV, 5, 2);
          array = detection_contours(diff_frame);
          if (array.size() > 0) {
            instertMovementSendEmail(imag.clone(), title);
            System.out.println("Motion detected.");
            movementDetected = true;
            Iterator<Rect> it2 = array.iterator();
            while (it2.hasNext()) {
              Rect obj = it2.next();
              Imgproc.rectangle(imag, obj.br(), obj.tl(),
                  new Scalar(0, 255, 0), 1);
            }
            break;
          }
        }

        i = 1;
        tempon_frame = outerBox.clone();
      } else {
        break;
      }
    }

    videoCapture.release();
    tempVideoFile.delete();
    imag = null;
    System.out.println("Detection ended.");
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