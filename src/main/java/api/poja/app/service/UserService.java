package api.poja.app.service;

import api.poja.app.endpoint.rest.model.UserDto;
import api.poja.app.model.ParcoursType;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.UserRepository;
import api.poja.app.service.exception.ConflictException;
import api.poja.app.service.exception.NotFoundException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

  private static final String DEFAULT_PASSWORD = "password123";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public List<User> listByRole(Role role) {
    return userRepository.findByRole(role);
  }

  public User getById(String id) {
    return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User: " + id));
  }

  public User getByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new NotFoundException("User: " + email));
  }

  @Transactional
  public User create(UserDto dto) {
    validateParcoursForRole(dto.role(), dto.parcours());
    if (userRepository.findByEmail(dto.email()).isPresent()) {
      throw new ConflictException("Email déjà utilisé: " + dto.email());
    }
    User user =
        User.builder()
            .std(dto.std())
            .nom(dto.nom())
            .prenom(dto.prenom())
            .email(dto.email())
            .password(passwordEncoder.encode(DEFAULT_PASSWORD))
            .role(dto.role())
            .parcours(dto.parcours())
            .promotion(dto.promotion())
            .build();
    return userRepository.save(user);
  }

  @Transactional
  public User update(String id, UserDto dto) {
    validateParcoursForRole(dto.role(), dto.parcours());
    User existing = getById(id);
    userRepository
        .findByEmail(dto.email())
        .filter(other -> !other.getId().equals(id))
        .ifPresent(
            other -> {
              throw new ConflictException("Email déjà utilisé: " + dto.email());
            });
    existing.setNom(dto.nom());
    existing.setPrenom(dto.prenom());
    existing.setEmail(dto.email());
    existing.setRole(dto.role());
    existing.setParcours(dto.parcours());
    existing.setPromotion(dto.promotion());
    if (dto.std() != null) {
      existing.setStd(dto.std());
    }
    return userRepository.save(existing);
  }

  @Transactional
  public void delete(String id) {
    getById(id);
    userRepository.deleteById(id);
  }

  private static void validateParcoursForRole(Role role, ParcoursType parcours) {
    if (role == Role.STUDENT && parcours == null) {
      throw new IllegalArgumentException("Le parcours est obligatoire pour un étudiant");
    }
  }
}
