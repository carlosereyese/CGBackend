package dir.repository;

import dir.model.Spaces;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpacesRepository extends JpaRepository<Spaces, Integer> {

  List<Spaces> findAllByUserId(String userId);

}
