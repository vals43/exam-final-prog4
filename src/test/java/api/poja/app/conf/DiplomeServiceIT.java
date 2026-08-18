package api.poja.app.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.poja.app.endpoint.rest.model.DiplomeDto;
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
import api.poja.app.service.DiplomeService;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class DiplomeServiceIT extends BaseIT {

  @Autowired DiplomeService diplomeService;

  @Autowired NoteRepository noteRepository;

  @Autowired NoteHistoryRepository noteHistoryRepository;

  @Autowired CoursRepository coursRepository;

  @Autowired ExamenRepository examenRepository;

  @Autowired GroupeRepository groupeRepository;

  @Autowired InscriptionRepository inscriptionRepository;

  @Autowired UserRepository userRepository;

  @Autowired ParcoursRepository parcoursRepository;

  @MockBean BucketComponent bucketComponent;

  private User alice;
  private User bob;
  private User charly;

  @BeforeEach
  void setup() {
    noteHistoryRepository.deleteAll();
    noteRepository.deleteAll();
    inscriptionRepository.deleteAll();
    examenRepository.deleteAll();
    coursRepository.deleteAll();
    groupeRepository.deleteAll();
    parcoursRepository.deleteAll();
    userRepository.deleteAll();

    var el = parcoursRepository.save(Parcours.builder().code(ParcoursType.EL).nom("EL").build());
    var tn = parcoursRepository.save(Parcours.builder().code(ParcoursType.TN).nom("TN").build());

    alice = newStudent("STD-EL1", "Alice", "alice@hei.school", ParcoursType.EL);
    bob = newStudent("STD-EL2", "Bob", "bob@hei.school", ParcoursType.EL);
    charly = newStudent("STD-TN1", "Charly", "charly@hei.school", ParcoursType.TN);

    var coursEl = newCours("PROG9", 6, 1, List.of(el));
    var coursWeb = newCours("WEB9", 4, 2, List.of(el));
    var coursTn = newCours("TN9", 6, 1, List.of(tn));

    var k1 = newGroupe("K1");
    var k2 = newGroupe("K2");

    var insAlice1 = newInscription(alice, k1, 1, 1);
    var insAlice2 = newInscription(alice, k2, 2, 1);
    var insBob1 = newInscription(bob, k1, 1, 1);
    var insBob2 = newInscription(bob, k2, 2, 1);
    var insCharly1 = newInscription(charly, k1, 1, 1);

    var exProg = newExamen(coursEl);
    var exWeb = newExamen(coursWeb);
    var exTn = newExamen(coursTn);

    newNote(alice, exProg, insAlice1, "12.00");
    newNote(alice, exWeb, insAlice2, "10.00");
    newNote(bob, exProg, insBob1, "14.00");
    newNote(bob, exWeb, insBob2, "6.00");
    newNote(charly, exTn, insCharly1, "11.00");
  }

  @Test
  void diplomes_lists_only_full_parcours_passed_students_ranked() {
    var diplomes = diplomeService.diplomes(2023);

    assertEquals(2, diplomes.size());
    assertEquals("STD-EL1", diplomes.get(0).std());
    assertEquals(1, diplomes.get(0).rang());
    assertEquals(0, diplomes.get(0).moyenneGenerale().compareTo(new BigDecimal("11.20")));
    assertEquals("STD-TN1", diplomes.get(1).std());
    assertEquals(2, diplomes.get(1).rang());
    assertEquals(0, diplomes.get(1).moyenneGenerale().compareTo(new BigDecimal("11.00")));
  }

  @Test
  void diplomes_filters_by_promotion() {
    var diplomes = diplomeService.diplomes(2022);

    assertEquals(0, diplomes.size());
  }

  @Test
  void diplomes_filters_parcours_strictly() {
    // Alice (EL) has no note on the TN-only course yet graduates:
    // the TN course must never appear in an EL parcours.
    // Charly (TN) has no note on EL courses yet graduates too.
    var diplomes = diplomeService.diplomes(2023);

    assertEquals(2, diplomes.size());
    assertEquals(List.of("STD-EL1", "STD-TN1"), diplomes.stream().map(DiplomeDto::std).toList());
  }

  @Test
  void genererListeDiplomes_uploads_xlsx_and_returns_presigned_url() throws Exception {
    var presigned = new URL("https://bucket.example/diplomes/promo-2023.xlsx");
    when(bucketComponent.presign(eq("diplomes/promo-2023.xlsx"), any())).thenReturn(presigned);

    var url = diplomeService.genererListeDiplomes(2023);

    assertEquals(presigned, url);
    verify(bucketComponent).upload(any(File.class), eq("diplomes/promo-2023.xlsx"));
  }

  private User newStudent(String std, String prenom, String email, ParcoursType parcours) {
    return userRepository.save(
        User.builder()
            .std(std)
            .nom("Doe")
            .prenom(prenom)
            .email(email)
            .password("x")
            .role(Role.STUDENT)
            .parcours(parcours)
            .promotion(2023)
            .build());
  }

  private Cours newCours(String ref, int credits, int semestre, List<Parcours> parcours) {
    return coursRepository.save(
        Cours.builder()
            .ref(ref)
            .intitule(ref)
            .credits(credits)
            .semestre(semestre)
            .parcours(parcours)
            .build());
  }

  private Groupe newGroupe(String ref) {
    return groupeRepository.save(Groupe.builder().ref(ref).build());
  }

  private Inscription newInscription(User student, Groupe groupe, int semestre, int annee) {
    return inscriptionRepository.save(
        Inscription.builder()
            .student(student)
            .groupe(groupe)
            .semestre(semestre)
            .annee(annee)
            .build());
  }

  private Examen newExamen(Cours cours) {
    return examenRepository.save(
        Examen.builder()
            .cours(cours)
            .date(Instant.parse("2024-01-15T09:00:00Z"))
            .coefficient(new BigDecimal("1.0"))
            .build());
  }

  private void newNote(User student, Examen examen, Inscription inscription, String valeur) {
    noteRepository.save(
        Note.builder()
            .student(student)
            .examen(examen)
            .inscription(inscription)
            .valeur(new BigDecimal(valeur))
            .version(1)
            .dateCreation(Instant.now())
            .build());
  }
}
