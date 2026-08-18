package api.poja.app.endpoint.web.controller;

import api.poja.app.endpoint.web.WebUsers;
import api.poja.app.model.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class UiHomeController {

  private final WebUsers webUsers;

  @GetMapping("/home")
  public String home(Authentication authentication) {
    User user = webUsers.current(authentication);
    if (user == null) {
      return "redirect:/login";
    }
    return switch (user.getRole()) {
      case ADMIN -> "redirect:/admin/ui";
      case TEACHER -> "redirect:/teacher/ui/affectations";
      case STUDENT -> "redirect:/student/ui/releves";
    };
  }
}
