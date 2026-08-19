package api.poja.app.conf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

public class UiAdminIT extends UiBaseIT {

  @Test
  void admin_views_dashboard() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(get("/admin/ui").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Tableau de bord")));
  }

  // ---------------- COURS ----------------

  @Test
  void admin_views_cours_page() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(get("/admin/ui/cours").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("PROG1")));
  }

  @Test
  void admin_creates_cours() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/cours")
                .param("ref", "WEB1")
                .param("intitule", "Web")
                .param("credits", "6")
                .param("semestre", "2")
                .cookie(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/ui/cours?created=1"));

    assertThat(coursRepository.findByRef("WEB1")).isPresent();
  }

  @Test
  void admin_create_cours_without_ref_shows_error() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/cours")
                .param("intitule", "Web")
                .param("credits", "6")
                .param("semestre", "2")
                .cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("La référence du cours est obligatoire.")));
  }

  @Test
  void admin_edits_cours() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(get("/admin/ui/cours/" + cours.getId() + "/edit").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Modifier")));

    mockMvc
        .perform(
            post("/admin/ui/cours/" + cours.getId())
                .param("ref", "PROG2")
                .param("intitule", "Programmation 2")
                .param("credits", "4")
                .param("semestre", "1")
                .cookie(session))
        .andExpect(status().is3xxRedirection());

    assertThat(coursRepository.findByRef("PROG2")).isPresent();
  }

  @Test
  void admin_delete_referenced_cours_shows_error() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(post("/admin/ui/cours/" + cours.getId() + "/delete").cookie(session))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    containsString(
                        "Impossible de supprimer ce cours : il est référencé par des examens ou des"
                            + " affectations.")));

    assertThat(coursRepository.findById(cours.getId())).isPresent();
  }

  @Test
  void admin_deletes_unreferenced_cours() throws Exception {
    Cookie session = login("admin@hei.school");
    var newCours =
        coursRepository.save(
            api.poja.app.model.Cours.builder()
                .ref("FREE1")
                .intitule("Libre")
                .credits(2)
                .semestre(1)
                .build());

    mockMvc
        .perform(post("/admin/ui/cours/" + newCours.getId() + "/delete").cookie(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/ui/cours"));

    assertThat(coursRepository.findById(newCours.getId())).isEmpty();
  }

  // ---------------- EXAMENS ----------------

  @Test
  void admin_views_examens_page() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(get("/admin/ui/examens").param("coursId", cours.getId()).cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("PROG1")));
  }

  @Test
  void admin_creates_examen() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/examens")
                .param("coursId", cours.getId())
                .param("date", "2024-06-15T09:00")
                .param("coefficient", "0.5")
                .cookie(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/ui/examens?coursId=" + cours.getId()));

    assertThat(examenRepository.findAll()).hasSize(2);
  }

  @Test
  void admin_create_examen_without_date_shows_error() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/examens")
                .param("coursId", cours.getId())
                .param("coefficient", "0.5")
                .cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("examen est obligatoire")));
  }

  // ---------------- AFFECTATIONS ----------------

  @Test
  void admin_views_affectations_page() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(get("/admin/ui/affectations").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("PROG1")));
  }

  @Test
  void admin_creates_affectation() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/affectations")
                .param("coursId", cours.getId())
                .param("groupeId", groupe.getId())
                .param("teacherId", teacher.getId())
                .param("annee", "2")
                .cookie(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/ui/affectations"));

    assertThat(affectationRepository.findAll()).hasSize(2);
  }

  @Test
  void admin_create_affectation_with_missing_fields_shows_error() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/affectations")
                .param("groupeId", groupe.getId())
                .param("annee", "2")
                .cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("tous les champs")));
  }

  // ---------------- UTILISATEURS ----------------

  @Test
  void admin_views_users_page() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(get("/admin/ui/users").param("role", "STUDENT").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("student@hei.school")));
  }

  @Test
  void admin_creates_teacher() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/users")
                .param("nom", "Dupont")
                .param("prenom", "Marie")
                .param("email", "marie@hei.school")
                .param("role", "TEACHER")
                .cookie(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/ui/users?role=TEACHER"));

    assertThat(userRepository.findByEmail("marie@hei.school")).isPresent();
  }

  @Test
  void admin_create_student_without_parcours_shows_error() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/users")
                .param("nom", "Dupont")
                .param("prenom", "Marie")
                .param("email", "marie@hei.school")
                .param("role", "STUDENT")
                .cookie(session))
        .andExpect(status().isOk())
        .andExpect(
            content().string(containsString("Le parcours est obligatoire pour un étudiant")));
  }

  @Test
  void admin_updates_user() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/users/" + student.getId())
                .param("std", student.getStd() == null ? "" : student.getStd())
                .param("nom", "Rakoto")
                .param("prenom", "Bob")
                .param("email", "student@hei.school")
                .param("role", "STUDENT")
                .param("parcours", "EL")
                .param("promotion", "2023")
                .cookie(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/ui/users?role=STUDENT"));

    assertThat(userRepository.findByEmail("student@hei.school").orElseThrow().getNom())
        .isEqualTo("Rakoto");
  }

  @Test
  void admin_deletes_unreferenced_user() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/users/" + admin.getId() + "/delete")
                .param("role", "ADMIN")
                .cookie(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/ui/users?role=ADMIN"));

    assertThat(userRepository.findById(admin.getId())).isEmpty();
  }

  @Test
  void admin_delete_referenced_student_keeps_user() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/users/" + student.getId() + "/delete")
                .param("role", "STUDENT")
                .cookie(session))
        .andExpect(status().is3xxRedirection());

    assertThat(userRepository.findById(student.getId())).isPresent();
  }

  // ---------------- RELEVÉS ----------------

  @Test
  void admin_views_releves_page() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(get("/admin/ui/releves").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Prenom Nom")));
  }

  @Test
  void admin_generates_student_releve() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(
            post("/admin/ui/releves")
                .param("studentId", student.getId())
                .param("annee", "1")
                .param("mode", "PROVISOIRE")
                .cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("généré et envoyé par email")))
        .andExpect(content().string(containsString("PROG1")));
  }

  @Test
  void admin_cannot_access_student_page() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc
        .perform(get("/student/ui/notes").cookie(session))
        .andExpect(status().isForbidden())
        .andExpect(content().string(containsString("Accès refusé")));
  }
}
