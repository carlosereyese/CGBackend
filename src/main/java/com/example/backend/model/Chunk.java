package com.example.backend.model;

import lombok.*;

import java.nio.ByteBuffer;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Chunk {

  private boolean lastChunk;
  private byte[] data;

}
