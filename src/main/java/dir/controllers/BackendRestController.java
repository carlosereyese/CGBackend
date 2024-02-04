package dir.controllers;

import dir.api.BackendRestApi;
import dir.model.Movements;
import dir.model.Spaces;
import dir.services.BackendService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SecurityRequirement(name = "authenticate")
public class BackendRestController implements BackendRestApi {

  @Autowired
  BackendService backendService;

  @Override
  public List<Movements> getAllMovements() {
    return backendService.getAllMovements();
  }

  @Override
  public Movements insertMovement(Movements movement) { return backendService.insert(movement); }

  @Override
  public List<Spaces> getAllSpaces() {
    return backendService.getAllSpaces();
  }

  @Override
  public Spaces insertSpaces(Spaces space) { return backendService.insert(space); }

}
