package dir.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class EmailRequest {

  private String toEmail;
  private String movimiento;
  private String base64;


}
