package api.poja.app.conf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import api.poja.app.endpoint.event.EventProducer;
import api.poja.app.endpoint.event.model.SendEmailRequested;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.model.Affectation;
import api.poja.app.model.Cours;
import api.poja.app.model.Examen;
import api.poja.app.model.Groupe;
import api.poja.app.model.Inscription;
import api.poja.app.model.Parcours;
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
import api.poja.app.repository.ParcoursRepository;
import api.poja.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@ActiveProfiles("test")
public abstract class UiBaseIT extends BaseIT {

  @Autowired protected MockMvc mockMvc;

  @Autowired protected ObjectMapper objectMapper;

  @Autowired protected PasswordEncoder passwordEncoder;

  @Autowired protected UserRepository userRepository;

  @Autowired protected CoursRepository coursRepository;

  @Autowired protected ExamenRepository examenRepository;

  @Autowired protected GroupeRepository groupeRepository;

  @Autowired protected InscriptionRepository inscriptionRepository;

  @Autowired protected AffectationRepository affectationRepository;

  @Autowired protected NoteRepository noteRepository;

  @Autowired protected NoteHistoryRepository noteHistoryRepository;

  @Autowired protected ParcoursRepository parcoursRepository;

  @MockBean protected BucketComponent bucketComponent;

  @MockBean protected EventProducer<SendEmailRequested> eventProducer;

  protected User admin;
  protected User teacher;
  protected User student;
  protected Groupe groupe;
  protected Cours cours;
  protected Examen examen;
  protected Inscription inscription;
  protected Parcours el;

  @BeforeEach
  void seedUi() {
    admin = saveUser("admin@hei.school", Role.ADMIN, null, null);
    teacher = saveUser("teacher@hei.school", Role.TEACHER, null, null);
    student = saveUser("student@hei.school", Role.STUDENT, ParcoursType.EL, 2023);
    el =
        parcoursRepository.save(
            Parcours.builder().code(ParcoursType.EL).nom("Ecosysteme Logiciel").build());
    groupe = groupeRepository.save(Groupe.builder().ref("G1").build());
    cours =
        coursRepository.save(
            Cours.builder()
                .ref("PROG1")
                .intitule("Programmation")
                .credits(6)
                .semestre(1)
                .parcours(List.of(el))
                .build());
    examen =
        examenRepository.save(
            Examen.builder()
                .cours(cours)
                .date(Instant.parse("2024-01-15T09:00:00Z"))
                .coefficient(new BigDecimal("0.5"))
                .build());
    inscription =
        inscriptionRepository.save(
            Inscription.builder().student(student).groupe(groupe).semestre(1).annee(1).build());
    affectationRepository.save(
        Affectation.builder().cours(cours).groupe(groupe).teacher(teacher).annee(1).build());
  }

  protected User saveUser(String email, Role role, ParcoursType parcours, Integer promotion) {
    return userRepository.save(
        User.builder()
            .nom("Nom")
            .prenom("Prenom")
            .email(email)
            .password(passwordEncoder.encode("password123"))
            .role(role)
            .parcours(parcours)
            .promotion(promotion)
            .build());
  }

  protected Cookie login(String email) throws Exception {
    MvcResult result =
        mockMvc
            .perform(post("/ui/login").param("email", email).param("password", "password123"))
            .andExpect(redirectedUrl("/home"))
            .andReturn();
    return result.getResponse().getCookie("AUTH_TOKEN");
  }
}
