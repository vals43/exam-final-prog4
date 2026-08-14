package api.poja.app.repository;

import api.poja.app.model.Affectation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffectationRepository extends JpaRepository<Affectation, String> {
  List<Affectation> findByAnnee(Integer annee);

  List<Affectation> findByCoursId(String coursId);

  List<Affectation> findByGroupeId(String groupeId);

  List<Affectation> findByTeacherId(String teacherId);

  Optional<Affectation> findByCoursIdAndGroupeIdAndAnnee(
      String coursId, String groupeId, Integer annee);
}
