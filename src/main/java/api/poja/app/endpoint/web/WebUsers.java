package api.poja.app.endpoint.web;

import api.poja.app.model.User;
import api.poja.app.service.UserService;
import api.poja.app.service.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class WebUsers {

  private final UserService userService;

  public User current(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }
    try {
      return userService.getByEmail(authentication.getName());
    } catch (NotFoundException e) {
      return null;
    }
  }
}
