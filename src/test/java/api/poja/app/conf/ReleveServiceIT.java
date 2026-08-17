package api.poja.app.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import api.poja.app.endpoint.event.EventProducer;
import api.poja.app.endpoint.event.model.SendEmailRequested;
import api.poja.app.endpoint.rest.model.ReleveMode;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.model.Cours;
import api.poja.app.model.Examen;
import api.poja.app.model.Groupe;
import api.poja.app.model.Inscription;
import api.poja.app.model.Note;
import api.poja.app.model.Parcours;
import api.poja.app.model.ParcoursType;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.ExamenRepository;
import api.poja.app.repository.GroupeRepository;
import api.poja.app.repository.InscriptionRepository;
import api.poja.app.repository.NoteHistoryRepository;
import api.poja.app.repository.NoteRepository;
import api.poja.app.repository.ParcoursRepository;
import api.poja.app.repository.UserRepository;
import api.poja.app.service.ReleveService;
import api.poja.app.service.exception.ConflictException;
import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class ReleveServiceIT extends BaseIT {

  @Autowired ReleveService releveService;

  @Autowired NoteRepository noteRepository;

  @Autowired NoteHistoryRepository noteHistoryRepository;

  @Autowired CoursRepository coursRepository;

  @Autowired ExamenRepository examenRepository;

  @Autowired GroupeRepository groupeRepository;

  @Autowired InscriptionRepository inscriptionRepository;

  @Autowired UserRepository userRepository;

  @Autowired ParcoursRepository parcoursRepository;

  @MockBean BucketComponent bucketComponent;

  @MockBean EventProducer<SendEmailRequested> eventProducer;

  private User student;
  private Cours cours1;
  private Cours cours2;

  @BeforeEach
  void setup() {
    noteHistoryRepository.deleteAll();
    noteRepository.deleteAll();
    inscriptionRepository.deleteAll();
    examenRepository.deleteAll();
    coursRepository.deleteAll();
    groupeRepository.deleteAll();
    userRepository.deleteAll();
    parcoursRepository.deleteAll();

    var el = parcoursRepository.save(Parcours.builder().code(ParcoursType.EL).nom("EL").build());
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
    cours1 =
        coursRepository.save(
            Cours.builder()
                .ref("PROG9")
                .intitule("Programmation 9")
                .credits(6)
                .semestre(1)
                .parcours(List.of(el))
                .build());
    cours2 =
        coursRepository.save(
            Cours.builder()
                .ref("WEB9")
                .intitule("Web 9")
                .credits(6)
                .semestre(2)
                .parcours(List.of(el))
                .build());
    var groupe = groupeRepository.save(Groupe.builder().ref("K9").annee(1).build());
    var inscription1 =
        inscriptionRepository.save(
            Inscription.builder().student(student).groupe(groupe).semestre(1).annee(1).build());
    var inscription2 =
        inscriptionRepository.save(
            Inscription.builder().student(student).groupe(groupe).semestre(2).annee(1).build());
    var examen1 =
        examenRepository.save(
            Examen.builder()
                .cours(cours1)
                .date(Instant.parse("2024-01-15T09:00:00Z"))
                .coefficient(new BigDecimal("1.0"))
                .build());
    var examen2 =
        examenRepository.save(
            Examen.builder()
                .cours(cours2)
                .date(Instant.parse("2024-06-15T09:00:00Z"))
                .coefficient(new BigDecimal("1.0"))
                .build());
    noteRepository.save(
        Note.builder()
            .student(student)
            .examen(examen1)
            .inscription(inscription1)
            .valeur(new BigDecimal("12.00"))
            .version(1)
            .dateCreation(Instant.now())
            .build());
    noteRepository.save(
        Note.builder()
            .student(student)
            .examen(examen2)
            .inscription(inscription2)
            .valeur(new BigDecimal("9.00"))
            .version(1)
            .dateCreation(Instant.now())
            .build());
  }

  @Test
  void genererReleve_compute_lines_average_credits_and_publish_event() {
    var releve = releveService.genererReleve(student.getId(), 1, ReleveMode.COMPLET);

    assertEquals(2, releve.lignes().size());
    assertEquals("PROG9", releve.lignes().get(0).coursRef());
    assertEquals("WEB9", releve.lignes().get(1).coursRef());
    assertEquals(0, new BigDecimal("12.00").compareTo(releve.lignes().get(0).noteFinale()));
    assertTrue(releve.lignes().get(0).valide());
    assertTrue(!releve.lignes().get(1).valide());
    assertEquals(6, releve.creditsValides());
    assertEquals(0, new BigDecimal("12.00").compareTo(releve.moyenneGenerale()));
    assertEquals(ReleveMode.COMPLET, releve.mode());

    verify(bucketComponent).upload(any(File.class), eq("releves/STD-0001/1.pdf"));
    verify(eventProducer).accept(any());
  }

  @Test
  void genererReleve_with_teacher_role_throws_conflict() {
    var teacher =
        userRepository.save(
            User.builder()
                .nom("Doe")
                .prenom("John")
                .email("t@hei.school")
                .password("x")
                .role(Role.TEACHER)
                .build());

    assertThrows(
        ConflictException.class,
        () -> releveService.genererReleve(teacher.getId(), 1, ReleveMode.PROVISOIRE));
  }

  @Test
  void genererReleve_complet_throws_when_not_all_graded() {
    noteRepository.deleteAll();

    assertThrows(
        ConflictException.class,
        () -> releveService.genererReleve(student.getId(), 1, ReleveMode.COMPLET));
  }

  @Test
  void genererReleve_provisoire_allows_unfinished_notes() {
    noteRepository.deleteAll();

    var releve = releveService.genererReleve(student.getId(), 1, ReleveMode.PROVISOIRE);

    assertEquals(2, releve.lignes().size());
    assertNull(releve.lignes().get(0).noteFinale());
    assertNull(releve.moyenneGenerale());
    assertEquals(0, releve.creditsValides());
  }

  @Test
  void genererReleve_ignores_course_whose_exam_coefficients_do_not_sum_to_one() {
    noteRepository.deleteAll();
    examenRepository.deleteAll();
    var examenPartiel =
        examenRepository.save(
            Examen.builder()
                .cours(cours1)
                .date(Instant.parse("2024-01-15T09:00:00Z"))
                .coefficient(new BigDecimal("0.5"))
                .build());
    var inscription =
        inscriptionRepository.findAll().stream()
            .filter(i -> i.getSemestre() == 1)
            .findFirst()
            .orElseThrow();
    noteRepository.save(
        Note.builder()
            .student(student)
            .examen(examenPartiel)
            .inscription(inscription)
            .valeur(new BigDecimal("14.00"))
            .version(1)
            .dateCreation(Instant.now())
            .build());

    var releve = releveService.genererReleve(student.getId(), 1, ReleveMode.PROVISOIRE);

    assertNull(releve.lignes().get(0).noteFinale());
    assertEquals(0, releve.creditsValides());
  }
}
