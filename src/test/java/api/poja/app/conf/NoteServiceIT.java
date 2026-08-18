package api.poja.app.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import api.poja.app.endpoint.rest.model.NoteDto;
import api.poja.app.model.Affectation;
import api.poja.app.model.Cours;
import api.poja.app.model.Examen;
import api.poja.app.model.Groupe;
import api.poja.app.model.Inscription;
import api.poja.app.model.ParcoursType;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.AffectationRepository;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.ExamenRepository;
import api.poja.app.repository.GroupeRepository;
import api.poja.app.repository.InscriptionRepository;
import api.poja.app.repository.NoteHistoryRepository;
import api.poja.app.repository.NoteRepository;
import api.poja.app.repository.UserRepository;
import api.poja.app.service.NoteService;
import api.poja.app.service.exception.ConflictException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class NoteServiceIT extends BaseIT {

  @Autowired NoteService noteService;

  @Autowired NoteRepository noteRepository;

  @Autowired NoteHistoryRepository noteHistoryRepository;

  @Autowired CoursRepository coursRepository;

  @Autowired ExamenRepository examenRepository;

  @Autowired GroupeRepository groupeRepository;

  @Autowired InscriptionRepository inscriptionRepository;

  @Autowired AffectationRepository affectationRepository;

  @Autowired UserRepository userRepository;

  private Cours cours;
  private Examen examen;
  private Inscription inscription;
  private User teacher;
  private User student;

  @BeforeEach
  void setup() {
    noteHistoryRepository.deleteAll();
    noteRepository.deleteAll();
    affectationRepository.deleteAll();
    inscriptionRepository.deleteAll();
    examenRepository.deleteAll();
    coursRepository.deleteAll();
    groupeRepository.deleteAll();
    userRepository.deleteAll();

    cours =
        coursRepository.save(
            Cours.builder()
                .ref("PROG9")
                .intitule("Programmation 9")
                .credits(6)
                .semestre(1)
                .build());
    examen =
        examenRepository.save(
            Examen.builder()
                .cours(cours)
                .date(Instant.parse("2024-01-15T09:00:00Z"))
                .coefficient(new BigDecimal("0.5"))
                .build());
    var groupe = groupeRepository.save(Groupe.builder().ref("K9").build());
    teacher =
        userRepository.save(
            User.builder()
                .nom("Doe")
                .prenom("John")
                .email("t@hei.school")
                .password("x")
                .role(Role.TEACHER)
                .build());
    student =
        userRepository.save(
            User.builder()
                .std("STD-0001")
                .nom("Rakoto")
                .prenom("Bob")
                .email("s@hei.school")
                .password("x")
                .role(Role.STUDENT)
                .parcours(ParcoursType.EL)
                .build());
    inscription =
        inscriptionRepository.save(
            Inscription.builder().student(student).groupe(groupe).semestre(1).annee(2024).build());
    affectationRepository.save(
        Affectation.builder().cours(cours).groupe(groupe).teacher(teacher).annee(2024).build());
  }

  @Test
  void grade_creates_note_with_version_1() {
    var note =
        noteService.grade(
            new NoteDto(
                null,
                student.getId(),
                examen.getId(),
                inscription.getId(),
                new BigDecimal("15.5"),
                "Saisie initiale"),
            teacher);

    assertEquals(new BigDecimal("15.5"), note.getValeur());
    assertEquals(1, note.getVersion());
    assertEquals(student.getId(), note.getStudent().getId());
  }

  @Test
  void grade_creates_history_on_update() {
    var first =
        noteService.grade(
            new NoteDto(
                null,
                student.getId(),
                examen.getId(),
                inscription.getId(),
                new BigDecimal("10.00"),
                "Saisie initiale"),
            teacher);

    var second =
        noteService.grade(
            new NoteDto(
                first.getId(),
                student.getId(),
                examen.getId(),
                inscription.getId(),
                new BigDecimal("17.00"),
                "Réclamation de l'étudiant"),
            teacher);

    assertEquals(new BigDecimal("17.00"), second.getValeur());
    assertEquals(2, second.getVersion());
    var histories = noteHistoryRepository.findByNoteId(second.getId());
    assertEquals(1, histories.size());
    assertEquals(0, new BigDecimal("10.00").compareTo(histories.get(0).getAncienneValeur()));
    assertEquals(0, new BigDecimal("17.00").compareTo(histories.get(0).getNouvelleValeur()));
    assertEquals(teacher.getId(), histories.get(0).getModifiePar().getId());
    assertEquals("Réclamation de l'étudiant", histories.get(0).getRaison());
  }

  @Test
  void grade_throws_when_raison_missing_on_update() {
    var first =
        noteService.grade(
            new NoteDto(
                null,
                student.getId(),
                examen.getId(),
                inscription.getId(),
                new BigDecimal("10.00"),
                "Saisie initiale"),
            teacher);

    assertThrows(
        ConflictException.class,
        () ->
            noteService.grade(
                new NoteDto(
                    first.getId(),
                    student.getId(),
                    examen.getId(),
                    inscription.getId(),
                    new BigDecimal("17.00"),
                    null),
                teacher));
  }

  @Test
  void grade_same_value_does_not_create_history() {
    var first =
        noteService.grade(
            new NoteDto(
                null,
                student.getId(),
                examen.getId(),
                inscription.getId(),
                new BigDecimal("12.00"),
                "Saisie initiale"),
            teacher);

    var second =
        noteService.grade(
            new NoteDto(
                first.getId(),
                student.getId(),
                examen.getId(),
                inscription.getId(),
                new BigDecimal("12.00"),
                "Correction sans changement"),
            teacher);

    assertEquals(0, noteHistoryRepository.findByNoteId(second.getId()).size());
    assertEquals(1, second.getVersion());
  }

  @Test
  void grade_throws_when_teacher_not_assigned() {
    var otherTeacher =
        userRepository.save(
            User.builder()
                .nom("Doe")
                .prenom("Jane")
                .email("t2@hei.school")
                .password("x")
                .role(Role.TEACHER)
                .build());

    assertThrows(
        ConflictException.class,
        () ->
            noteService.grade(
                new NoteDto(
                    null,
                    student.getId(),
                    examen.getId(),
                    inscription.getId(),
                    new BigDecimal("14.00"),
                    "Saisie initiale"),
                otherTeacher));
  }

  @Test
  void viewByStudentId_returns_sorted_notes() {
    var cours2 =
        coursRepository.save(
            Cours.builder().ref("WEB9").intitule("Web 9").credits(6).semestre(2).build());
    var examen2 =
        examenRepository.save(
            Examen.builder()
                .cours(cours2)
                .date(Instant.parse("2024-06-15T09:00:00Z"))
                .coefficient(new BigDecimal("1.0"))
                .build());
    var groupe2 = groupeRepository.save(Groupe.builder().ref("K9b").build());
    var inscription2 =
        inscriptionRepository.save(
            Inscription.builder().student(student).groupe(groupe2).semestre(2).annee(2024).build());
    affectationRepository.save(
        Affectation.builder().cours(cours2).groupe(groupe2).teacher(teacher).annee(2024).build());

    noteService.grade(
        new NoteDto(
            null,
            student.getId(),
            examen.getId(),
            inscription.getId(),
            new BigDecimal("10.00"),
            "Saisie initiale"),
        teacher);
    noteService.grade(
        new NoteDto(
            null,
            student.getId(),
            examen2.getId(),
            inscription2.getId(),
            new BigDecimal("11.00"),
            "Saisie initiale"),
        teacher);

    var views = noteService.viewByStudentId(student.getId());
    assertEquals(2, views.size());
    assertEquals("PROG9", views.get(0).coursRef());
    assertEquals("WEB9", views.get(1).coursRef());
  }
}
