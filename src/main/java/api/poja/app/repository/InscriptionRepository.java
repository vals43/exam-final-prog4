package api.poja.app.repository;

import api.poja.app.model.Inscription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InscriptionRepository extends JpaRepository<Inscription, String> {
  List<Inscription> findByStudentId(String studentId);

  List<Inscription> findByGroupeId(String groupeId);

  List<Inscription> findByAnnee(Integer annee);

  List<Inscription> findByStudentIdAndAnnee(String studentId, Integer annee);

  @Query("select distinct i.annee from Inscription i order by i.annee")
  List<Integer> findDistinctAnnees();
}
