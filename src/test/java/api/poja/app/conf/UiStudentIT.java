package api.poja.app.conf;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import api.poja.app.model.Note;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class UiStudentIT extends UiBaseIT {

  @Test
  void student_views_its_notes() throws Exception {
    Cookie session = login("student@hei.school");
    noteRepository.save(
        Note.builder()
            .student(student)
            .examen(examen)
            .inscription(inscription)
            .valeur(new BigDecimal("14.00"))
            .version(1)
            .dateCreation(Instant.now())
            .build());

    mockMvc
        .perform(get("/student/ui/notes").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Mes notes")))
        .andExpect(content().string(containsString("PROG1")));
  }

  @Test
  void student_views_releves_list() throws Exception {
    Cookie session = login("student@hei.school");

    mockMvc
        .perform(get("/student/ui/releves").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Mes relevés de notes")));
  }

  @Test
  void student_generates_provisoire_releve() throws Exception {
    Cookie session = login("student@hei.school");

    mockMvc
        .perform(get("/student/ui/releves/1").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Relevé — Année")))
        .andExpect(content().string(containsString("PROG1")));
  }

  @Test
  void student_complet_releve_without_all_notes_shows_error() throws Exception {
    Cookie session = login("student@hei.school");

    mockMvc
        .perform(get("/student/ui/releves/1").param("mode", "COMPLET").cookie(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Relevé complet impossible")));
  }

  @Test
  void student_cannot_access_teacher_page() throws Exception {
    Cookie session = login("student@hei.school");

    mockMvc
        .perform(get("/teacher/ui/affectations").cookie(session))
        .andExpect(status().isForbidden())
        .andExpect(content().string(not(containsString("<!DOCTYPE html>"))));
  }
}
