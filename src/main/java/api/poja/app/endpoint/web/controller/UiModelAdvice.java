package api.poja.app.endpoint.web.controller;

import api.poja.app.endpoint.web.WebUsers;
import api.poja.app.model.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ControllerAdvice(basePackages = "api.poja.app.endpoint.web")
@AllArgsConstructor
public class UiModelAdvice {

  private final WebUsers webUsers;

  @ModelAttribute
  public void addRequestUri(Model model) {
    var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      model.addAttribute("uri", attributes.getRequest().getRequestURI());
    }
  }

  @ModelAttribute
  public void addCurrentUser(Model model, Authentication authentication) {
    User user = webUsers.current(authentication);
    model.addAttribute("currentUser", user);
    model.addAttribute("isAdmin", user != null && user.getRole().name().equals("ADMIN"));
    model.addAttribute("isTeacher", user != null && user.getRole().name().equals("TEACHER"));
    model.addAttribute("isStudent", user != null && user.getRole().name().equals("STUDENT"));
  }
}
