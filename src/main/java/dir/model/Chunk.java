package dir.model;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Chunk {

  private boolean lastChunk;
  private byte[] data;

}
