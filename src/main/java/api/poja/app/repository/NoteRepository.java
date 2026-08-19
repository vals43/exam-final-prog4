package api.poja.app.repository;

import api.poja.app.model.Note;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoteRepository extends JpaRepository<Note, String> {
  List<Note> findByStudentId(String studentId);

  List<Note> findByExamenId(String examenId);

  List<Note> findByInscriptionId(String inscriptionId);

  Optional<Note> findByStudentIdAndExamenId(String studentId, String examenId);

  @Query(
      "select n from Note n join fetch n.student join fetch n.examen e join fetch e.cours"
          + " join fetch n.inscription i join fetch i.groupe")
  List<Note> findAll();
}
