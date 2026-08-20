package api.poja.app.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import api.poja.app.endpoint.rest.model.CoursDto;
import api.poja.app.model.Parcours;
import api.poja.app.model.ParcoursType;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.ParcoursRepository;
import api.poja.app.service.CoursService;
import api.poja.app.service.exception.ConflictException;
import api.poja.app.service.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
public class CoursServiceIT extends BaseIT {

  @Autowired CoursService coursService;

  @Autowired CoursRepository coursRepository;

  @Autowired ParcoursRepository parcoursRepository;

  @BeforeEach
  void clean() {
    coursRepository.deleteAll();
    parcoursRepository.deleteAll();
  }

  @Test
  void create_and_update_cours() {
    var created = coursService.create(new CoursDto(null, "PROG9", "Programmation 9", 6, 1));

    assertEquals("PROG9", created.getRef());
    assertEquals(6, created.getCredits());

    var updated =
        coursService.update(created.getId(), new CoursDto(null, "PROG9", "Prog avancée", 8, 2));
    assertEquals("Prog avancée", updated.getIntitule());
    assertEquals(8, updated.getCredits());
  }

  @Test
  void create_throws_when_ref_already_used() {
    coursService.create(new CoursDto(null, "PROG9", "Programmation 9", 6, 1));

    assertThrows(
        ConflictException.class,
        () -> coursService.create(new CoursDto(null, "PROG9", "Autre", 4, 2)));
  }

  @Test
  void update_throws_when_ref_taken_by_another() {
    var first = coursService.create(new CoursDto(null, "PROG9", "Programmation 9", 6, 1));
    coursService.create(new CoursDto(null, "WEB9", "Web 9", 6, 1));

    assertThrows(
        ConflictException.class,
        () -> coursService.update(first.getId(), new CoursDto(null, "WEB9", "Renommé", 6, 1)));
  }

  @Test
  void delete_removes_cours() {
    var created = coursService.create(new CoursDto(null, "PROG9", "Programmation 9", 6, 1));

    coursService.delete(created.getId());

    assertThrows(NotFoundException.class, () -> coursService.getById(created.getId()));
  }

  @Test
  @Transactional
  void addParcours_links_cours_to_parcours() {
    var cours = coursService.create(new CoursDto(null, "PROG9", "Programmation 9", 6, 1));
    var parcours =
        parcoursRepository.save(
            Parcours.builder().code(ParcoursType.EL).nom("Ecosysteme Logiciel").build());

    coursService.addParcours(cours.getId(), parcours.getId());

    var withParcours = coursRepository.findById(cours.getId()).orElseThrow();
    assertEquals(1, withParcours.getParcours().size());
    assertEquals(ParcoursType.EL, withParcours.getParcours().get(0).getCode());
  }
}
