package api.poja.app.endpoint.rest.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TeacherWhoController {
  @GetMapping("/teacher/whoami")
  @PreAuthorize("hasRole('TEACHER')")
  public String whoami(Authentication authentication) {
    return "TEACHER: " + authentication.getName();
  }
}
