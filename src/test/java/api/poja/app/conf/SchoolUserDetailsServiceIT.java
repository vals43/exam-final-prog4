package api.poja.app.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.UserRepository;
import api.poja.app.service.SchoolUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class SchoolUserDetailsServiceIT extends FacadeIT {

  @Autowired UserRepository userRepository;

  @Autowired PasswordEncoder passwordEncoder;

  @Autowired SchoolUserDetailsService schoolUserDetailsService;

  @BeforeEach
  void insertUser() {
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
  void loadUserByUsername_returns_encoded_password_and_role() {
    var details = schoolUserDetailsService.loadUserByUsername("student@hei.school");

    assertEquals("student@hei.school", details.getUsername());
    assertEquals(true, passwordEncoder.matches("password123", details.getPassword()));
    assertEquals(
        "ROLE_STUDENT", details.getAuthorities().stream().findFirst().orElseThrow().getAuthority());
  }

  @Test
  void loadUserByUsername_throws_when_not_found() {
    assertThrows(
        UsernameNotFoundException.class,
        () -> schoolUserDetailsService.loadUserByUsername("missing@hei.school"));
  }
}
