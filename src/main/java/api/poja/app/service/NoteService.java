package api.poja.app.service;

import api.poja.app.endpoint.rest.model.NoteDto;
import api.poja.app.endpoint.rest.model.NoteViewDto;
import api.poja.app.model.Affectation;
import api.poja.app.model.Examen;
import api.poja.app.model.Inscription;
import api.poja.app.model.Note;
import api.poja.app.model.NoteHistory;
import api.poja.app.model.User;
import api.poja.app.repository.AffectationRepository;
import api.poja.app.repository.ExamenRepository;
import api.poja.app.repository.InscriptionRepository;
import api.poja.app.repository.NoteHistoryRepository;
import api.poja.app.repository.NoteRepository;
import api.poja.app.service.exception.ConflictException;
import api.poja.app.service.exception.NotFoundException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class NoteService {

  private final NoteRepository noteRepository;
  private final NoteHistoryRepository noteHistoryRepository;
  private final ExamenRepository examenRepository;
  private final InscriptionRepository inscriptionRepository;
  private final AffectationRepository affectationRepository;

  public List<Note> listByStudentId(String studentId) {
    return noteRepository.findByStudentId(studentId);
  }

  @Transactional(readOnly = true)
  public List<NoteViewDto> viewByStudentId(String studentId) {
    return listByStudentId(studentId).stream().map(this::toView).sorted(comparator()).toList();
  }

  public List<Note> listByExamenId(String examenId) {
    return noteRepository.findByExamenId(examenId);
  }

  @Transactional
  public Note grade(NoteDto dto, User teacher) {
    Examen examen =
        examenRepository
            .findById(dto.examenId())
            .orElseThrow(() -> new NotFoundException("Examen: " + dto.examenId()));
    Inscription inscription =
        inscriptionRepository
            .findById(dto.inscriptionId())
            .orElseThrow(() -> new NotFoundException("Inscription: " + dto.inscriptionId()));
    if (!inscription.getStudent().getId().equals(dto.studentId())) {
      throw new ConflictException("L'inscription ne correspond pas à l'étudiant");
    }
    assertTeacherAssignments(teacher, examen, inscription);

    return noteRepository
        .findByStudentIdAndExamenId(dto.studentId(), dto.examenId())
        .map(note -> updateNote(note, dto.valeur(), teacher))
        .orElseGet(() -> createNote(dto, examen, inscription));
  }

  @Transactional
  public Note createNote(NoteDto dto, Examen examen, Inscription inscription) {
    return noteRepository.save(
        Note.builder()
            .student(inscription.getStudent())
            .examen(examen)
            .inscription(inscription)
            .valeur(dto.valeur())
            .version(1)
            .dateCreation(Instant.now())
            .build());
  }

  @Transactional
  public Note updateNote(Note note, java.math.BigDecimal nouvelleValeur, User teacher) {
    if (note.getValeur().compareTo(nouvelleValeur) != 0) {
      noteHistoryRepository.save(
          NoteHistory.builder()
              .note(note)
              .ancienneValeur(note.getValeur())
              .nouvelleValeur(nouvelleValeur)
              .dateModification(Instant.now())
              .modifiePar(teacher)
              .build());
      note.setValeur(nouvelleValeur);
      note.setVersion(note.getVersion() + 1);
    }
    return noteRepository.save(note);
  }

  private void assertTeacherAssignments(User teacher, Examen examen, Inscription inscription) {
    List<Affectation> assignments = affectationRepository.findByTeacherId(teacher.getId());
    boolean assigned =
        assignments.stream()
            .anyMatch(
                a ->
                    a.getCours().getId().equals(examen.getCours().getId())
                        && a.getGroupe().getId().equals(inscription.getGroupe().getId())
                        && a.getAnnee().equals(inscription.getAnnee()));
    if (!assigned) {
      throw new ConflictException(
          "Le teacher " + teacher.getId() + " n'est pas affecté à ce cours/groupe/année");
    }
  }

  private Comparator<NoteViewDto> comparator() {
    return Comparator.comparing(NoteViewDto::semestre).thenComparing(NoteViewDto::coursRef);
  }

  private NoteViewDto toView(Note note) {
    return new NoteViewDto(
        note.getId(),
        note.getExamen().getCours().getRef(),
        note.getExamen().getCours().getIntitule(),
        note.getExamen().getCours().getSemestre(),
        note.getExamen().getDate(),
        note.getExamen().getCoefficient(),
        note.getValeur(),
        note.getVersion());
  }
}
