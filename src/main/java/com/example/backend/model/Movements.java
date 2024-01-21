package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
  private String id_user;
  @Lob
  @Column(columnDefinition = "MEDIUMBLOB")
  private byte[] image;

}
