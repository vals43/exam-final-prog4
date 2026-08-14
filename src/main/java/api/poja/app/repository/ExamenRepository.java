package api.poja.app.repository;

import api.poja.app.model.Examen;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamenRepository extends JpaRepository<Examen, String> {
  List<Examen> findByCoursId(String coursId);
}
