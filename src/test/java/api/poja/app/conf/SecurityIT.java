package api.poja.app.conf;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIT extends FacadeIT {

  @Autowired MockMvc mockMvc;

  @Autowired UserRepository userRepository;

  @Autowired PasswordEncoder passwordEncoder;

  @BeforeEach
  void insertStudent() {
    userRepository.deleteAll();
    userRepository.save(
        User.builder()
            .std("STD-0001")
            .nom("Doe")
            .prenom("John")
            .email("student@hei.school")
            .password(passwordEncoder.encode("password123"))
            .role(Role.STUDENT)
            .build());
  }

  @Test
  void unauthenticated_redirects_to_login() throws Exception {
    mockMvc
        .perform(get("/student/whoami"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("http://localhost/login"));
  }

  @Test
  void login_with_valid_credentials_is_authenticated() throws Exception {
    mockMvc
        .perform(formLogin().user("student@hei.school").password("password123"))
        .andExpect(authenticated());
  }

  @Test
  void login_with_invalid_credentials_is_rejected() throws Exception {
    mockMvc
        .perform(formLogin().user("student@hei.school").password("wrong-password"))
        .andExpect(unauthenticated());
  }

  @Test
  @WithMockUser(username = "admin@hei.school", roles = "ADMIN")
  void admin_can_access_admin_endpoint() throws Exception {
    mockMvc.perform(get("/admin/whoami")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "teacher@hei.school", roles = "TEACHER")
  void teacher_cannot_access_admin_endpoint() throws Exception {
    mockMvc.perform(get("/admin/whoami")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "student@hei.school", roles = "STUDENT")
  void student_can_access_student_endpoint() throws Exception {
    mockMvc.perform(get("/student/whoami")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "student@hei.school", roles = "STUDENT")
  void student_cannot_access_teacher_endpoint() throws Exception {
    mockMvc.perform(get("/teacher/whoami")).andExpect(status().isForbidden());
  }
}
