package api.poja.app.repository;

import api.poja.app.model.Inscription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InscriptionRepository extends JpaRepository<Inscription, String> {
  List<Inscription> findByStudentId(String studentId);

  List<Inscription> findByGroupeId(String groupeId);

  @Query(
      "select i from Inscription i join fetch i.student where i.groupe.id = :groupeId"
          + " and i.annee = :annee")
  List<Inscription> findByGroupeIdAndAnnee(
      @Param("groupeId") String groupeId, @Param("annee") Integer annee);

  List<Inscription> findByAnnee(Integer annee);

  List<Inscription> findByStudentIdAndAnnee(String studentId, Integer annee);
}
