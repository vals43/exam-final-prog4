package api.poja.app.repository;

import api.poja.app.model.Affectation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AffectationRepository extends JpaRepository<Affectation, String> {
  List<Affectation> findByAnnee(Integer annee);

  List<Affectation> findByCoursId(String coursId);

  List<Affectation> findByGroupeId(String groupeId);

  @Query(
      "select a from Affectation a join fetch a.cours join fetch a.groupe join fetch a.teacher"
          + " where a.teacher.id = :teacherId")
  List<Affectation> findByTeacherId(@Param("teacherId") String teacherId);

  Optional<Affectation> findByCoursIdAndGroupeIdAndAnnee(
      String coursId, String groupeId, Integer annee);

  @Query("select a from Affectation a join fetch a.cours join fetch a.groupe join fetch a.teacher")
  List<Affectation> findAll();
}
