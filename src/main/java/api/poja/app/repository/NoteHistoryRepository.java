package api.poja.app.repository;

import api.poja.app.model.NoteHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteHistoryRepository extends JpaRepository<NoteHistory, String> {
  @Query("select h from NoteHistory h join fetch h.modifiePar where h.note.id = :noteId")
  List<NoteHistory> findByNoteId(@Param("noteId") String noteId);
}
