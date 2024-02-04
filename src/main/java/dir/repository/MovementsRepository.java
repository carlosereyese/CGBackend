package dir.repository;

import dir.model.Movements;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovementsRepository extends JpaRepository<Movements, Integer> {

  List<Movements> findAll(Sort sort);

}
