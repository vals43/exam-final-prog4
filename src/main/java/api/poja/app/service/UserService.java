package api.poja.app.service;

import api.poja.app.endpoint.rest.model.UserDto;
import api.poja.app.model.Groupe;
import api.poja.app.model.Inscription;
import api.poja.app.model.ParcoursType;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.GroupeRepository;
import api.poja.app.repository.InscriptionRepository;
import api.poja.app.repository.UserRepository;
import api.poja.app.service.exception.ConflictException;
import api.poja.app.service.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

  private static final String DEFAULT_PASSWORD = "password123";

  private static final Map<Integer, Character> LETTER_BY_YEAR =
      Map.of(2021, 'G', 2022, 'H', 2023, 'J');

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final GroupeRepository groupeRepository;
  private final InscriptionRepository inscriptionRepository;

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

  public List<Groupe> groupesForPromo(Integer promotion) {
    Character letter = LETTER_BY_YEAR.get(promotion);
    if (letter == null) {
      return List.of();
    }
    return groupeRepository.findAll().stream()
        .filter(g -> g.getRef().charAt(0) == letter)
        .sorted((a, b) -> a.getRef().compareTo(b.getRef()))
        .toList();
  }

  public List<String> groupeRefsOf(String studentId) {
    return inscriptionRepository.findByStudentId(studentId).stream()
        .map(i -> i.getGroupe().getRef())
        .distinct()
        .sorted()
        .toList();
  }

  @Transactional
  public void changeGroupe(String studentId, String groupeId) {
    User student = getById(studentId);
    if (student.getRole() != Role.STUDENT) {
      throw new IllegalArgumentException("Seuls les étudiants ont un groupe");
    }
    if (student.getPromotion() == null) {
      throw new IllegalArgumentException("Promotion manquante pour cet étudiant");
    }
    Character letter = LETTER_BY_YEAR.get(student.getPromotion());
    if (letter == null) {
      throw new IllegalArgumentException(
          "Aucun groupe connu pour la promotion " + student.getPromotion());
    }
    Groupe groupe =
        groupeRepository
            .findById(groupeId)
            .orElseThrow(() -> new NotFoundException("Groupe: " + groupeId));
    if (groupe.getRef().charAt(0) != letter) {
      throw new IllegalArgumentException(
          "Le groupe "
              + groupe.getRef()
              + " n'appartient pas à la promotion "
              + student.getPromotion()
              + " (groupes attendus : "
              + letter
              + "1, "
              + letter
              + "2, "
              + letter
              + "3)");
    }
    List<Inscription> inscriptions = inscriptionRepository.findByStudentId(studentId);
    inscriptions.forEach(i -> i.setGroupe(groupe));
    inscriptionRepository.saveAll(inscriptions);
  }

  private static void validateParcoursForRole(Role role, ParcoursType parcours) {
    if (role == Role.STUDENT && parcours == null) {
      throw new IllegalArgumentException("Le parcours est obligatoire pour un étudiant");
    }
  }
}
