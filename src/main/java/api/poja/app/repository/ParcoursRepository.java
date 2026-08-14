package api.poja.app.repository;

import api.poja.app.model.Parcours;
import api.poja.app.model.ParcoursType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcoursRepository extends JpaRepository<Parcours, String> {
  Optional<Parcours> findByCode(ParcoursType code);
}
