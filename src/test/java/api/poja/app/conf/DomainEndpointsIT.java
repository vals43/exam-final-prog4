package api.poja.app.conf;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DomainEndpointsIT extends BaseIT {

  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  @Autowired UserRepository userRepository;

  @Autowired PasswordEncoder passwordEncoder;

  @Autowired CoursRepository coursRepository;

  @Autowired GroupeRepository groupeRepository;

  @Autowired ExamenRepository examenRepository;

  @Autowired InscriptionRepository inscriptionRepository;

  @Autowired AffectationRepository affectationRepository;

  @Autowired NoteRepository noteRepository;

  @Autowired NoteHistoryRepository noteHistoryRepository;

  private User admin;
  private User teacher;
  private User student;
  private Cours cours;
  private Cours cours2;
  private Examen examen;
  private Inscription inscription;

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

    admin =
        userRepository.save(
            User.builder()
                .nom("Admin")
                .prenom("System")
                .email("admin@hei.school")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());
    teacher =
        userRepository.save(
            User.builder()
                .nom("Doe")
                .prenom("John")
                .email("teacher@hei.school")
                .password(passwordEncoder.encode("password123"))
                .role(Role.TEACHER)
                .build());
    student =
        userRepository.save(
            User.builder()
                .std("STD-0001")
                .nom("Rakoto")
                .prenom("Bob")
                .email("student@hei.school")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STUDENT)
                .parcours(ParcoursType.EL)
                .build());
    cours =
        coursRepository.save(
            Cours.builder()
                .ref("PROG9")
                .intitule("Programmation 9")
                .credits(6)
                .semestre(1)
                .build());
    var groupe = groupeRepository.save(Groupe.builder().ref("K9").annee(1).build());
    cours2 =
        coursRepository.save(
            Cours.builder().ref("WEB9").intitule("Web 9").credits(6).semestre(1).build());
    examen =
        examenRepository.save(
            Examen.builder()
                .cours(cours)
                .date(Instant.parse("2024-01-15T09:00:00Z"))
                .coefficient(new BigDecimal("0.5"))
                .build());
    inscription =
        inscriptionRepository.save(
            Inscription.builder().student(student).groupe(groupe).semestre(1).annee(2024).build());
    affectationRepository.save(
        Affectation.builder().cours(cours).groupe(groupe).teacher(teacher).annee(2024).build());
  }

  @Test
  void admin_creates_student_teacher_assigns_then_teacher_grades_and_student_views()
      throws Exception {
    var body =
        objectMapper.writeValueAsString(
            Map.of(
                "std", "STD-0101",
                "nom", "Randria",
                "prenom", "Alice",
                "email", "alice@hei.school",
                "role", "STUDENT",
                "parcours", "EL"));
    mockMvc
        .perform(
            post("/admin/users")
                .with(user(admin.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("alice@hei.school"));

    mockMvc
        .perform(
            post("/admin/affectations")
                .with(user(admin.getEmail()).roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "coursId", cours2.getId(),
                            "groupeId", inscription.getGroupe().getId(),
                            "teacherId", teacher.getId(),
                            "annee", 2024))))
        .andExpect(status().isOk());

    var gradeBody =
        objectMapper.writeValueAsString(
            Map.of(
                "studentId", student.getId(),
                "examenId", examen.getId(),
                "inscriptionId", inscription.getId(),
                "valeur", 14.5));
    mockMvc
        .perform(
            post("/teacher/notes")
                .with(user(teacher.getEmail()).roles("TEACHER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(gradeBody))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/student/notes").with(user(student.getEmail()).roles("STUDENT")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].coursRef").value("PROG9"))
        .andExpect(jsonPath("$[0].valeur").value(14.5))
        .andExpect(jsonPath("$[0].version").value(1));
  }

  @Test
  void teacher_cannot_grade_without_assignment() throws Exception {
    var otherTeacher =
        userRepository.save(
            User.builder()
                .nom("Doe")
                .prenom("Jane")
                .email("other@hei.school")
                .password(passwordEncoder.encode("password123"))
                .role(Role.TEACHER)
                .build());

    var gradeBody =
        objectMapper.writeValueAsString(
            Map.of(
                "studentId", student.getId(),
                "examenId", examen.getId(),
                "inscriptionId", inscription.getId(),
                "valeur", 14.5));
    mockMvc
        .perform(
            post("/teacher/notes")
                .with(user(otherTeacher.getEmail()).roles("TEACHER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(gradeBody))
        .andExpect(status().isConflict());
  }

  @Test
  void student_cannot_access_admin_endpoint() throws Exception {
    mockMvc
        .perform(
            get("/admin/users")
                .param("role", "STUDENT")
                .with(user(student.getEmail()).roles("STUDENT")))
        .andExpect(status().isForbidden());
  }

  @Test
  void admin_lists_students() throws Exception {
    mockMvc
        .perform(
            get("/admin/users")
                .param("role", "STUDENT")
                .with(user(admin.getEmail()).roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value("student@hei.school"));
  }
}
