package api.poja.app.repository;

import api.poja.app.model.NoteHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteHistoryRepository extends JpaRepository<NoteHistory, String> {
  List<NoteHistory> findByNoteId(String noteId);
}
