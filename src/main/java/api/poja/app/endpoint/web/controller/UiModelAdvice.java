package api.poja.app.endpoint.web.controller;

import api.poja.app.endpoint.web.WebUsers;
import api.poja.app.model.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "api.poja.app.endpoint.web")
@AllArgsConstructor
public class UiModelAdvice {

  private final WebUsers webUsers;

  @ModelAttribute
  public void addCurrentUser(Model model, Authentication authentication) {
    User user = webUsers.current(authentication);
    model.addAttribute("currentUser", user);
    model.addAttribute("isAdmin", user != null && user.getRole().name().equals("ADMIN"));
    model.addAttribute("isTeacher", user != null && user.getRole().name().equals("TEACHER"));
    model.addAttribute("isStudent", user != null && user.getRole().name().equals("STUDENT"));
  }
}
