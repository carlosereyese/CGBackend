package dir.services;

import dir.model.Movements;
import dir.model.Spaces;
import dir.repository.MovementsRepository;
import dir.repository.SpacesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BackendService {

  @Autowired
  MovementsRepository movementsRepository;

  @Autowired
  SpacesRepository spacesRepository;

  @Autowired
  AuthService authService;

  public List<Movements> getAllMovements() {
    Sort sort = Sort.by(Sort.Direction.DESC, "id");
    return movementsRepository.findAllByUserId(authService.getUserId(), sort);
  }

  public Movements insert(Movements movement) {
    movement.setUserId(authService.getUserId());
    return movementsRepository.save(movement);
  }

  public List<Spaces> getAllSpaces() {
    return spacesRepository.findAllByUserId(authService.getUserId());
  }

  public Spaces insert(Spaces space) {
    space.setUserId(authService.getUserId());
    return spacesRepository.save(space);
  }

  public boolean toggleAlarm() { return true; }

}
