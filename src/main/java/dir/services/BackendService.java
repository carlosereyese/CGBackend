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

  public List<Movements> getAllMovements() {
    Sort sort = Sort.by(Sort.Direction.DESC, "id");
    return movementsRepository.findAll(sort);
  }

  public Movements insert(Movements movement) { return movementsRepository.save(movement); }

  public List<Spaces> getAllSpaces() { return spacesRepository.findAll(); }

  public Spaces insert(Spaces space) { return spacesRepository.save(space); }

}
