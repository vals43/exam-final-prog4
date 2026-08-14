package api.poja.app.repository;

import api.poja.app.model.Groupe;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupeRepository extends JpaRepository<Groupe, String> {
  Optional<Groupe> findByRef(String ref);

  List<Groupe> findByAnnee(Integer annee);
}
