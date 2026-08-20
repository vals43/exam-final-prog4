package api.poja.app.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.poja.app.endpoint.rest.model.UserDto;
import api.poja.app.model.Groupe;
import api.poja.app.model.Inscription;
import api.poja.app.model.ParcoursType;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.GroupeRepository;
import api.poja.app.repository.InscriptionRepository;
import api.poja.app.repository.UserRepository;
import api.poja.app.service.UserService;
import api.poja.app.service.exception.ConflictException;
import api.poja.app.service.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class UserServiceIT extends BaseIT {

  @Autowired UserService userService;

  @Autowired UserRepository userRepository;

  @Autowired GroupeRepository groupeRepository;

  @Autowired InscriptionRepository inscriptionRepository;

  @Autowired PasswordEncoder passwordEncoder;

  @BeforeEach
  void clean() {
    userRepository.deleteAll();
  }

  @Test
  void create_encodes_default_password_and_keeps_std() {
    var dto =
        new UserDto(
            null,
            "STD-0001",
            "Doe",
            "John",
            "john@hei.school",
            Role.STUDENT,
            ParcoursType.EL,
            null);

    var created = userService.create(dto);

    assertEquals("STD-0001", created.getStd());
    assertEquals("john@hei.school", created.getEmail());
    assertEquals(Role.STUDENT, created.getRole());
    assertTrue(passwordEncoder.matches("password123", created.getPassword()));
  }

  @Test
  void create_throws_when_email_already_used() {
    var dto =
        new UserDto(null, "STD-0001", "Doe", "John", "dup@hei.school", Role.TEACHER, null, null);
    userService.create(dto);

    assertThrows(
        ConflictException.class,
        () ->
            userService.create(
                new UserDto(
                    null, "STD-0002", "Doe", "Jane", "dup@hei.school", Role.TEACHER, null, null)));
  }

  @Test
  void create_throws_when_student_without_parcours() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            userService.create(
                new UserDto(
                    null, "STD-0001", "Doe", "John", "p@hei.school", Role.STUDENT, null, null)));
  }

  @Test
  void update_keeps_std_and_password() {
    var original =
        userRepository.save(
            User.builder()
                .std("STD-0001")
                .nom("Doe")
                .prenom("John")
                .email("old@hei.school")
                .password(passwordEncoder.encode("secret"))
                .role(Role.STUDENT)
                .parcours(ParcoursType.TN)
                .build());

    var updated =
        userService.update(
            original.getId(),
            new UserDto(
                null, null, "Doe", "Jane", "new@hei.school", Role.STUDENT, ParcoursType.TN, null));

    assertEquals(original.getId(), updated.getId());
    assertEquals("STD-0001", updated.getStd());
    assertEquals("new@hei.school", updated.getEmail());
    assertEquals("Jane", updated.getPrenom());
    assertTrue(passwordEncoder.matches("secret", updated.getPassword()));
  }

  @Test
  void delete_removes_user() {
    var user =
        userRepository.save(
            User.builder()
                .nom("Doe")
                .prenom("John")
                .email("del@hei.school")
                .password("x")
                .role(Role.TEACHER)
                .build());

    userService.delete(user.getId());

    assertThrows(NotFoundException.class, () -> userService.getById(user.getId()));
  }

  @Test
  void listByRole_returns_only_matching_users() {
    userRepository.save(
        User.builder()
            .nom("Doe")
            .prenom("A")
            .email("a@hei.school")
            .password("x")
            .role(Role.TEACHER)
            .build());
    userRepository.save(
        User.builder()
            .nom("Doe")
            .prenom("B")
            .email("b@hei.school")
            .password("x")
            .role(Role.STUDENT)
            .parcours(ParcoursType.EL)
            .build());

    var teachers = userService.listByRole(Role.TEACHER);
    var students = userService.listByRole(Role.STUDENT);

    assertEquals(1, teachers.size());
    assertEquals(1, students.size());
    assertEquals("a@hei.school", teachers.get(0).getEmail());
  }

  @Test
  void changeGroupe_updates_all_student_inscriptions() {
    Groupe j1 = groupeRepository.save(Groupe.builder().ref("J1").build());
    Groupe j2 = groupeRepository.save(Groupe.builder().ref("J2").build());
    User student =
        userRepository.save(
            User.builder()
                .std("STD-0001")
                .nom("Doe")
                .prenom("John")
                .email("g@hei.school")
                .password("x")
                .role(Role.STUDENT)
                .parcours(ParcoursType.EL)
                .promotion(2023)
                .build());
    inscriptionRepository.save(
        Inscription.builder().student(student).groupe(j1).semestre(1).annee(1).build());
    inscriptionRepository.save(
        Inscription.builder().student(student).groupe(j1).semestre(2).annee(1).build());

    userService.changeGroupe(student.getId(), j2.getId());

    var inscriptions = inscriptionRepository.findByStudentId(student.getId());
    assertEquals(2, inscriptions.size());
    inscriptions.forEach(
        i -> assertEquals("J2", i.getGroupe().getRef(), "toutes les inscriptions changées"));
  }

  @Test
  void changeGroupe_rejects_group_from_another_promotion() {
    Groupe g1 = groupeRepository.save(Groupe.builder().ref("G1").build());
    User student =
        userRepository.save(
            User.builder()
                .std("STD-0001")
                .nom("Doe")
                .prenom("John")
                .email("g2@hei.school")
                .password("x")
                .role(Role.STUDENT)
                .parcours(ParcoursType.EL)
                .promotion(2023)
                .build());

    assertThrows(
        IllegalArgumentException.class,
        () -> userService.changeGroupe(student.getId(), g1.getId()));
  }

  @Test
  void groupesForPromo_returns_only_letter_matching_groups() {
    groupeRepository.save(Groupe.builder().ref("G1").build());
    groupeRepository.save(Groupe.builder().ref("H1").build());
    groupeRepository.save(Groupe.builder().ref("J1").build());
    groupeRepository.save(Groupe.builder().ref("J2").build());

    var groupes = userService.groupesForPromo(2023);
    assertEquals(2, groupes.size());
    assertEquals("J1", groupes.get(0).getRef());
    assertEquals("J2", groupes.get(1).getRef());
    assertTrue(userService.groupesForPromo(2099).isEmpty());
  }
}
