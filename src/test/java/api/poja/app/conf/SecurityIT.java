package api.poja.app.conf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIT extends BaseIT {

  @Autowired MockMvc mockMvc;

  @Autowired UserRepository userRepository;

  @Autowired PasswordEncoder passwordEncoder;

  @Autowired ObjectMapper objectMapper;

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
  void unauthenticated_access_is_unauthorized() throws Exception {
    mockMvc.perform(get("/student/whoami")).andExpect(status().isUnauthorized());
  }

  @Test
  void login_with_valid_credentials_returns_token() throws Exception {
    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("email", "student@hei.school", "password", "password123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.email").value("student@hei.school"))
        .andExpect(jsonPath("$.role").value("STUDENT"));
  }

  @Test
  void login_with_invalid_credentials_is_rejected() throws Exception {
    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("email", "student@hei.school", "password", "wrong-password"))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void authenticated_with_token_can_access_protected_endpoint() throws Exception {
    MvcResult login =
        mockMvc
            .perform(
                post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of("email", "student@hei.school", "password", "password123"))))
            .andExpect(status().isOk())
            .andReturn();
    String token =
        objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();

    mockMvc
        .perform(get("/student/whoami").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value("STUDENT: student@hei.school"));
  }

  @Test
  void invalid_token_is_rejected() throws Exception {
    mockMvc
        .perform(get("/student/whoami").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());
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
