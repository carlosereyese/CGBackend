package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
public class Movements {

  @Id
  @JsonIgnore
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String dateTime;
  private String room;

}
