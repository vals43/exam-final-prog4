package api.poja.app.conf;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class UiAuthIT extends UiBaseIT {

  @Test
  void login_page_renders() throws Exception {
    mockMvc
        .perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Connexion")));
  }

  @Test
  void root_redirects_to_home() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/home"));
  }

  @Test
  void form_login_sets_cookie_and_redirects_to_home() throws Exception {
    mockMvc
        .perform(
            post("/ui/login").param("email", "student@hei.school").param("password", "password123"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/home"))
        .andExpect(cookie().exists("AUTH_TOKEN"))
        .andExpect(cookie().httpOnly("AUTH_TOKEN", true));
  }

  @Test
  void login_with_bad_credentials_rerenders_login_page_with_error() throws Exception {
    mockMvc
        .perform(post("/ui/login").param("email", "student@hei.school").param("password", "wrong"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Email ou mot de passe incorrect")));
  }

  @Test
  void logout_clears_cookie_and_redirects_to_login() throws Exception {
    Cookie session = login("student@hei.school");

    mockMvc
        .perform(get("/logout").cookie(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"))
        .andExpect(cookie().maxAge("AUTH_TOKEN", 0));
  }

  @Test
  void home_redirects_student_to_its_releves() throws Exception {
    Cookie session = login("student@hei.school");

    mockMvc.perform(get("/home").cookie(session)).andExpect(redirectedUrl("/student/ui/releves"));
  }

  @Test
  void home_redirects_teacher_to_its_affectations() throws Exception {
    Cookie session = login("teacher@hei.school");

    mockMvc
        .perform(get("/home").cookie(session))
        .andExpect(redirectedUrl("/teacher/ui/affectations"));
  }

  @Test
  void home_redirects_admin_to_its_dashboard() throws Exception {
    Cookie session = login("admin@hei.school");

    mockMvc.perform(get("/home").cookie(session)).andExpect(redirectedUrl("/admin/ui"));
  }

  @Test
  void home_with_unknown_user_redirects_to_login() throws Exception {
    mockMvc
        .perform(get("/home").with(user("ghost@hei.school").roles("STUDENT")))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  void unauthenticated_html_page_redirects_to_login() throws Exception {
    mockMvc
        .perform(get("/admin/ui").accept(MediaType.TEXT_HTML))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  void unauthenticated_api_still_returns_401_json() throws Exception {
    mockMvc
        .perform(get("/student/whoami"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(not(containsString("<!DOCTYPE html>"))));
  }
}
