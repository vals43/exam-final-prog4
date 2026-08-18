package api.poja.app.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import api.poja.app.endpoint.rest.model.AffectationDto;
import api.poja.app.model.Cours;
import api.poja.app.model.Groupe;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.AffectationRepository;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.GroupeRepository;
import api.poja.app.repository.UserRepository;
import api.poja.app.service.AffectationService;
import api.poja.app.service.exception.ConflictException;
import api.poja.app.service.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class AffectationServiceIT extends BaseIT {

  @Autowired AffectationService affectationService;

  @Autowired AffectationRepository affectationRepository;

  @Autowired CoursRepository coursRepository;

  @Autowired GroupeRepository groupeRepository;

  @Autowired UserRepository userRepository;

  private Cours cours;
  private Groupe groupe;
  private User teacher;

  @BeforeEach
  void setup() {
    affectationRepository.deleteAll();
    coursRepository.deleteAll();
    groupeRepository.deleteAll();
    userRepository.deleteAll();
    cours =
        coursRepository.save(
            Cours.builder()
                .ref("PROG9")
                .intitule("Programmation 9")
                .credits(6)
                .semestre(1)
                .build());
    groupe = groupeRepository.save(Groupe.builder().ref("K9").build());
    teacher =
        userRepository.save(
            User.builder()
                .nom("Doe")
                .prenom("John")
                .email("t@hei.school")
                .password("x")
                .role(Role.TEACHER)
                .build());
  }

  @Test
  void create_affectation() {
    var created =
        affectationService.create(
            new AffectationDto(null, cours.getId(), groupe.getId(), teacher.getId(), 2024));

    assertEquals(cours.getId(), created.getCours().getId());
    assertEquals(groupe.getId(), created.getGroupe().getId());
    assertEquals(teacher.getId(), created.getTeacher().getId());
    assertEquals(2024, created.getAnnee());
  }

  @Test
  void create_allows_multiple_teachers_on_same_cours_groupe_annee() {
    var secondTeacher =
        userRepository.save(
            User.builder()
                .nom("Doe")
                .prenom("Jane")
                .email("t2@hei.school")
                .password("x")
                .role(Role.TEACHER)
                .build());
    affectationService.create(
        new AffectationDto(null, cours.getId(), groupe.getId(), teacher.getId(), 2024));

    var second =
        affectationService.create(
            new AffectationDto(null, cours.getId(), groupe.getId(), secondTeacher.getId(), 2024));

    assertEquals(2, affectationRepository.findByCoursId(cours.getId()).size());
    assertEquals(secondTeacher.getId(), second.getTeacher().getId());
  }

  @Test
  void create_throws_when_user_is_not_teacher() {
    var student =
        userRepository.save(
            User.builder()
                .nom("Rakoto")
                .prenom("Bob")
                .email("s@hei.school")
                .password("x")
                .role(Role.STUDENT)
                .build());

    assertThrows(
        ConflictException.class,
        () ->
            affectationService.create(
                new AffectationDto(null, cours.getId(), groupe.getId(), student.getId(), 2024)));
  }

  @Test
  void delete_removes_affectation() {
    var created =
        affectationService.create(
            new AffectationDto(null, cours.getId(), groupe.getId(), teacher.getId(), 2024));

    affectationService.delete(created.getId());

    assertThrows(NotFoundException.class, () -> affectationService.getById(created.getId()));
  }

  @Test
  void listByTeacherId_returns_only_teacher_assignments() {
    var secondTeacher =
        userRepository.save(
            User.builder()
                .nom("Doe")
                .prenom("Jane")
                .email("t2@hei.school")
                .password("x")
                .role(Role.TEACHER)
                .build());
    var otherGroupe = groupeRepository.save(Groupe.builder().ref("K8").build());
    affectationService.create(
        new AffectationDto(null, cours.getId(), groupe.getId(), teacher.getId(), 2024));
    affectationService.create(
        new AffectationDto(null, cours.getId(), otherGroupe.getId(), secondTeacher.getId(), 2024));

    assertEquals(1, affectationService.listByTeacherId(teacher.getId()).size());
  }
}
