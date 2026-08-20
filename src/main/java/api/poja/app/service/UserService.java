package api.poja.app.service;

import api.poja.app.endpoint.rest.model.UserDto;
import api.poja.app.model.Affectation;
import api.poja.app.model.Cours;
import api.poja.app.model.Groupe;
import api.poja.app.model.ParcoursType;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.AffectationRepository;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.GroupeRepository;
import api.poja.app.repository.UserRepository;
import api.poja.app.service.exception.ConflictException;
import api.poja.app.service.exception.NotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

  private static final String DEFAULT_PASSWORD = "password123";

  private static final Map<Character, Integer> PROMO_BY_LETTER =
      Map.of('G', 2021, 'H', 2022, 'J', 2023);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AffectationRepository affectationRepository;
  private final CoursRepository coursRepository;
  private final GroupeRepository groupeRepository;

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
        userRepository.save(
            User.builder()
                .std(dto.std())
                .nom(dto.nom())
                .prenom(dto.prenom())
                .email(dto.email())
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(dto.role())
                .parcours(dto.parcours())
                .promotion(dto.promotion())
                .build());
    syncAffectations(user, dto.coursIds());
    return user;
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
    User saved = userRepository.save(existing);
    if (saved.getRole() != Role.TEACHER) {
      affectationRepository.findByTeacherId(saved.getId()).forEach(affectationRepository::delete);
    } else {
      syncAffectations(saved, dto.coursIds());
    }
    return saved;
  }

  @Transactional
  public void delete(String id) {
    getById(id);
    userRepository.deleteById(id);
  }

  public List<String> coursIdsOf(User teacher) {
    if (teacher.getRole() != Role.TEACHER) {
      return List.of();
    }
    return affectationRepository.findByTeacherId(teacher.getId()).stream()
        .map(a -> a.getCours().getId())
        .distinct()
        .sorted()
        .toList();
  }

  private void syncAffectations(User teacher, List<String> coursIds) {
    if (teacher.getRole() != Role.TEACHER || coursIds == null) {
      return;
    }
    var ids = coursIds.stream().filter(id -> id != null && !id.isBlank()).distinct().toList();
    var selected =
        ids.isEmpty()
            ? Map.<String, Cours>of()
            : coursRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Cours::getId, cours -> cours));
    affectationRepository.findByTeacherId(teacher.getId()).stream()
        .filter(a -> !selected.containsKey(a.getCours().getId()))
        .forEach(affectationRepository::delete);
    for (Cours cours : selected.values()) {
      for (Affectation candidate : expectedAffectations(teacher, cours)) {
        affectationRepository
            .findByCoursIdAndGroupeIdAndAnnee(
                candidate.getCours().getId(), candidate.getGroupe().getId(), candidate.getAnnee())
            .orElseGet(() -> affectationRepository.save(candidate));
      }
    }
  }

  private List<Affectation> expectedAffectations(User teacher, Cours cours) {
    var result = new ArrayList<Affectation>();
    var groupesByPromo = new HashMap<Integer, List<Groupe>>();
    for (Groupe groupe : groupeRepository.findAll()) {
      Integer promo = PROMO_BY_LETTER.get(groupe.getRef().charAt(0));
      if (promo != null) {
        groupesByPromo.computeIfAbsent(promo, k -> new ArrayList<>()).add(groupe);
      }
    }
    boolean onlyEL = isOnly(cours, ParcoursType.EL);
    boolean onlyTN = isOnly(cours, ParcoursType.TN);
    int annee = (cours.getSemestre() + 1) / 2;
    for (List<Groupe> groups : groupesByPromo.values()) {
      if (groups.isEmpty()) {
        continue;
      }
      groups.sort(Comparator.comparing(Groupe::getRef));
      List<Groupe> targets;
      if (onlyEL) {
        targets = groups.subList(0, Math.min(2, groups.size()));
      } else if (onlyTN) {
        targets = groups.subList(groups.size() - 1, groups.size());
      } else {
        targets = groups;
      }
      for (Groupe groupe : targets) {
        result.add(
            Affectation.builder()
                .cours(cours)
                .groupe(groupe)
                .teacher(teacher)
                .annee(annee)
                .build());
      }
    }
    return result;
  }

  private static boolean isOnly(Cours cours, ParcoursType parcours) {
    return cours.getParcours() != null
        && cours.getParcours().size() == 1
        && cours.getParcours().get(0).getCode() == parcours;
  }

  private static void validateParcoursForRole(Role role, ParcoursType parcours) {
    if (role == Role.STUDENT && parcours == null) {
      throw new IllegalArgumentException("Le parcours est obligatoire pour un étudiant");
    }
  }
}
