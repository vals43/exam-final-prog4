package api.poja.app.repository;

import api.poja.app.model.Cours;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursRepository extends JpaRepository<Cours, String> {
  Optional<Cours> findByRef(String ref);

  List<Cours> findBySemestre(Integer semestre);
}
