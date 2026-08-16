package api.poja.app.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import api.poja.app.endpoint.rest.model.ExamenDto;
import api.poja.app.model.Cours;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.ExamenRepository;
import api.poja.app.service.ExamenService;
import api.poja.app.service.exception.ConflictException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class ExamenServiceIT extends BaseIT {

  @Autowired ExamenService examenService;

  @Autowired ExamenRepository examenRepository;

  @Autowired CoursRepository coursRepository;

  private Cours cours;

  @BeforeEach
  void setup() {
    examenRepository.deleteAll();
    coursRepository.deleteAll();
    cours =
        coursRepository.save(
            Cours.builder()
                .ref("PROG9")
                .intitule("Programmation 9")
                .credits(6)
                .semestre(1)
                .build());
  }

  @Test
  void create_accepts_when_coefficients_sum_to_one() {
    examenService.create(
        new ExamenDto(
            null, cours.getId(), Instant.parse("2024-01-15T09:00:00Z"), new BigDecimal("0.4")));
    examenService.create(
        new ExamenDto(
            null, cours.getId(), Instant.parse("2024-01-16T09:00:00Z"), new BigDecimal("0.6")));

    assertEquals(2, examenRepository.findByCoursId(cours.getId()).size());
  }

  @Test
  void create_throws_when_coefficients_sum_is_not_one() {
    examenService.create(
        new ExamenDto(
            null, cours.getId(), Instant.parse("2024-01-15T09:00:00Z"), new BigDecimal("0.4")));

    assertThrows(
        ConflictException.class,
        () ->
            examenService.create(
                new ExamenDto(
                    null,
                    cours.getId(),
                    Instant.parse("2024-01-16T09:00:00Z"),
                    new BigDecimal("0.5"))));
  }

  @Test
  void delete_removes_examen() {
    var created =
        examenService.create(
            new ExamenDto(
                null, cours.getId(), Instant.parse("2024-01-15T09:00:00Z"), new BigDecimal("1.0")));

    examenService.delete(created.getId());

    assertEquals(0, examenRepository.findByCoursId(cours.getId()).size());
  }
}
