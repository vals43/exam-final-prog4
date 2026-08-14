package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.UserDto;
import api.poja.app.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public UserDto toDto(User user) {
    return new UserDto(
        user.getId(),
        user.getStd(),
        user.getNom(),
        user.getPrenom(),
        user.getEmail(),
        user.getRole(),
        user.getParcours());
  }
}
