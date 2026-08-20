package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.UserDto;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.AffectationRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  private final AffectationRepository affectationRepository;

  public UserMapper(AffectationRepository affectationRepository) {
    this.affectationRepository = affectationRepository;
  }

  public UserDto toDto(User user) {
    List<String> coursIds = List.of();
    if (user.getRole() == Role.TEACHER) {
      coursIds =
          affectationRepository.findByTeacherId(user.getId()).stream()
              .map(a -> a.getCours().getId())
              .distinct()
              .sorted()
              .toList();
    }
    return new UserDto(
        user.getId(),
        user.getStd(),
        user.getNom(),
        user.getPrenom(),
        user.getEmail(),
        user.getRole(),
        user.getParcours(),
        user.getPromotion(),
        coursIds);
  }
}
