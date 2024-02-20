package dir.controllers;

import dir.model.Chunk;
import dir.model.EmailRequest;
import dir.model.Movements;
import dir.services.BackendService;
import dir.services.EmailService;
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
import java.util.concurrent.CompletableFuture;

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

  //Mediante este servicio se recibe la solicitud de video del cliente
  @MessageMapping("/requestVideo/{roomId}")
  public void handleVideoRequest(@DestinationVariable String roomId) throws Exception {
    System.out.println("Requested room: " + roomId);
    extractFrames(roomId);
  }

  /*
  Este metodo itera a traves de todos los fotogramas del video solicitado para realizar una deteccion de movimiento
  y para aenviar los fotogramas uno a uno.
   */
  public void extractFrames(String title) throws IOException, InterruptedException {

    ClassPathResource classPathResource = new ClassPathResource("videos/" + title + ".mp4");

    VideoCapture videoCapture = new VideoCapture();
    videoCapture.open(classPathResource.getFile().getAbsolutePath()); //Abrir video

    if (!videoCapture.isOpened()) {
      System.err.println("Error: Couldn't open video file.");
      return;
    }

    byte[] imageByteArray = null;
    Mat frame = new Mat();
    Mat currentFrame = null;
    Mat previousFrame = null;
    Mat diffFrames = null;
    ArrayList<Rect> array = new ArrayList<Rect>();
    Size sz = new Size(640, 480);
    boolean firstFrame = true;
    boolean movementDetected = false;

    while (videoCapture.read(frame)) { //Iterar fotogramas

      MatOfByte matOfByte = new MatOfByte();

      Imgproc.resize(frame, frame, sz);
      imag = frame.clone();
      currentFrame = new Mat(frame.size(), CvType.CV_8UC1);
      Imgproc.cvtColor(frame, currentFrame, Imgproc.COLOR_BGR2GRAY); //Se convierte a una escala de grises
      Imgproc.GaussianBlur(currentFrame, currentFrame, new Size(3, 3), 0); //Se le aplica un desenfoque gaussiano

      if (!firstFrame) {
        Core.subtract(currentFrame, previousFrame, diffFrames); //Se resten el fotograma actual y anterior
        Imgproc.adaptiveThreshold(diffFrames, diffFrames, 255,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY_INV, 5, 2); //Se le aplica umbral adaptativo
        array = detection_contours(diffFrames);
        if (array.size() > 0 && !movementDetected) {        //Se utliza la flag movementDetected para reducir la cantidad de
          Imgcodecs.imencode(".jpg", imag, matOfByte); //movimientos insertados y correos enviados en el entorno de pruebas
          byte[] finalImageByteArray = matOfByte.toArray();
          CompletableFuture.runAsync(() -> {
            instertMovementSendEmail(finalImageByteArray, title);
          });
          System.out.println("Motion detected.");
          movementDetected = true;
        }
      }
      else {
        previousFrame = new Mat(currentFrame.size(), CvType.CV_8UC1);
        diffFrames = currentFrame.clone();
        firstFrame = false;
      }

      previousFrame = currentFrame.clone();

      Imgcodecs.imencode(".jpg", imag, matOfByte);
      imageByteArray = matOfByte.toArray();
      sendFrame(imageByteArray, false);
    }

    sendFrame(imageByteArray, true);
    videoCapture.release();
  }

  private void sendFrame(byte[] frame, boolean lastFrame) throws InterruptedException {
    Chunk chunk = new Chunk(lastFrame, frame);
    Thread.sleep(10);
    messagingTemplate.convertAndSend("/topic/getVideo", chunk); // Send the Chunk object
  }

  /*
  Esta funcion devuelve un listado de contornos de los movimientos detectados, tambien dibuja una
  silueta roja al rededor de los movimientos para poder observarlo mejor.
   */
  private ArrayList<Rect> detection_contours(Mat diffFrames) {
    Mat mat1 = new Mat();
    Mat mat2 = diffFrames.clone();
    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    Imgproc.findContours(mat2, contours, mat1, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

    double maxArea = 100;
    int maxAreaIdx = -1;
    Rect rect = null;
    ArrayList<Rect> rect_array = new ArrayList<Rect>();

    for (int idx = 0; idx < contours.size(); idx++) {
      Mat contour = contours.get(idx);
      double contourarea = Imgproc.contourArea(contour);
      if (contourarea > maxArea) {
        maxAreaIdx = idx;
        rect = Imgproc.boundingRect(contours.get(maxAreaIdx));
        rect_array.add(rect);
        Imgproc.drawContours(imag, contours, maxAreaIdx, new Scalar(0, 0, 255));
      }
    }

    mat1.release();

    return rect_array;
  }

  /*
  Este metodo inserta un movimiento en la tabla y envia un correo electronico para alertar al usuario
   */
  private void instertMovementSendEmail(byte[] image, String title) {
    LocalDateTime currentDateTime = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedDateTime = currentDateTime.format(formatter);

    try {
      Movements movements = new Movements();
      movements.setRoom(title);
      movements.setUserId("1");
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
