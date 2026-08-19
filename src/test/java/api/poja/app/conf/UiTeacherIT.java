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

public class UiTeacherIT extends UiBaseIT {

  @Test
  void teacher_views_its_affectations() throws Exception {
    Cookie session = login("teacher@hei.school");

    mockMvc
        .perform(get("/teacher/ui/affectations").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("PROG1")));
  }

  @Test
  void teacher_views_grade_page_for_group() throws Exception {
    Cookie session = login("teacher@hei.school");

    mockMvc
        .perform(
            get("/teacher/ui/affectations/" + groupe.getId() + "/1/" + cours.getId())
                .cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Groupe G1")))
        .andExpect(content().string(containsString("Prenom")));
  }

  @Test
  void teacher_grades_student_and_creates_history() throws Exception {
    Cookie session = login("teacher@hei.school");

    mockMvc
        .perform(
            post("/teacher/ui/notes")
                .param("studentId", student.getId())
                .param("inscriptionId", inscription.getId())
                .param("coursId", cours.getId())
                .param("groupeId", groupe.getId())
                .param("annee", "1")
                .param("valeur_" + examen.getId(), "14")
                .param("raison", "Saisie initiale")
                .cookie(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            redirectedUrl(
                "/teacher/ui/affectations/" + groupe.getId() + "/1/" + cours.getId() + "?saved=1"));

    assertThat(noteRepository.count()).isEqualTo(1);
    assertThat(noteHistoryRepository.count()).isZero();

    mockMvc
        .perform(
            post("/teacher/ui/notes")
                .param("studentId", student.getId())
                .param("inscriptionId", inscription.getId())
                .param("coursId", cours.getId())
                .param("groupeId", groupe.getId())
                .param("annee", "1")
                .param("valeur_" + examen.getId(), "16")
                .param("raison", "Correction après recours")
                .cookie(session))
        .andExpect(status().is3xxRedirection());

    assertThat(noteRepository.count()).isEqualTo(1);
    assertThat(noteHistoryRepository.count()).isEqualTo(1);
    assertThat(noteRepository.findAll().get(0).getVersion()).isEqualTo(2);

    mockMvc
        .perform(get("/teacher/ui/notes").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("PROG1")));
  }

  @Test
  void teacher_must_provide_raison() throws Exception {
    Cookie session = login("teacher@hei.school");

    mockMvc
        .perform(
            post("/teacher/ui/notes")
                .param("studentId", student.getId())
                .param("inscriptionId", inscription.getId())
                .param("coursId", cours.getId())
                .param("groupeId", groupe.getId())
                .param("annee", "1")
                .param("valeur_" + examen.getId(), "14")
                .cookie(session))
        .andExpect(status().isOk())
        .andExpect(
            content().string(containsString("La raison de la modification est obligatoire.")));

    assertThat(noteRepository.count()).isZero();
  }

  @Test
  void teacher_cannot_grade_outside_0_20() throws Exception {
    Cookie session = login("teacher@hei.school");

    mockMvc
        .perform(
            post("/teacher/ui/notes")
                .param("studentId", student.getId())
                .param("inscriptionId", inscription.getId())
                .param("coursId", cours.getId())
                .param("groupeId", groupe.getId())
                .param("annee", "1")
                .param("valeur_" + examen.getId(), "25")
                .param("raison", "Raison")
                .cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("La note doit être comprise entre 0 et 20.")));

    assertThat(noteRepository.count()).isZero();
  }

  @Test
  void teacher_cannot_access_admin_page() throws Exception {
    Cookie session = login("teacher@hei.school");

    mockMvc
        .perform(get("/admin/ui").cookie(session))
        .andExpect(status().isForbidden())
        .andExpect(content().string(containsString("Accès refusé")));
  }
}
