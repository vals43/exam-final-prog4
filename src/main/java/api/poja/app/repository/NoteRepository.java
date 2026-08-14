package api.poja.app.repository;

import api.poja.app.model.Note;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, String> {
  List<Note> findByStudentId(String studentId);

  List<Note> findByExamenId(String examenId);

  List<Note> findByInscriptionId(String inscriptionId);

  Optional<Note> findByStudentIdAndExamenId(String studentId, String examenId);
}
