package api.poja.app.repository;

import api.poja.app.model.Inscription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscriptionRepository extends JpaRepository<Inscription, String> {
  List<Inscription> findByStudentId(String studentId);

  List<Inscription> findByGroupeId(String groupeId);

  List<Inscription> findByStudentIdAndAnnee(String studentId, Integer annee);
}
