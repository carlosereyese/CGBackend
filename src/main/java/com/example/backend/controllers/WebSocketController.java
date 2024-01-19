package com.example.backend.controllers;

import com.example.backend.model.Greeting;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
@SecurityRequirement(name = "authenticate")
public class WebSocketController {

  @MessageMapping("/requestVideo/{roomId}")
  @SendTo("/topic/getVideo")
  public Greeting clientVideoRequest(@DestinationVariable String roomId) throws Exception {
    Thread.sleep(1000); // simulated delay
    System.out.println("Requested room: " + roomId);
    return new Greeting("Enviando " + roomId);
  }
}
