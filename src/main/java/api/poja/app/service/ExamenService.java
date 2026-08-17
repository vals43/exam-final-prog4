package api.poja.app.service;

import api.poja.app.endpoint.rest.model.ExamenDto;
import api.poja.app.model.Cours;
import api.poja.app.model.Examen;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.ExamenRepository;
import api.poja.app.service.exception.ConflictException;
import api.poja.app.service.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ExamenService {

  private final ExamenRepository examenRepository;
  private final CoursRepository coursRepository;

  public List<Examen> listByCoursId(String coursId) {
    return examenRepository.findByCoursId(coursId);
  }

  public Examen getById(String id) {
    return examenRepository.findById(id).orElseThrow(() -> new NotFoundException("Examen: " + id));
  }

  @Transactional
  public Examen create(ExamenDto dto) {
    Cours cours =
        coursRepository
            .findById(dto.coursId())
            .orElseThrow(() -> new NotFoundException("Cours: " + dto.coursId()));
    BigDecimal existing =
        examenRepository.findByCoursId(cours.getId()).stream()
            .map(Examen::getCoefficient)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal somme = existing.add(dto.coefficient());
    if (somme.compareTo(BigDecimal.ONE) > 0) {
      throw new ConflictException(
          "Somme des coefficients du cours "
              + cours.getRef()
              + " = "
              + somme
              + " (doit être inférieure ou égale à 1)");
    }
    return examenRepository.save(
        Examen.builder().cours(cours).date(dto.date()).coefficient(dto.coefficient()).build());
  }

  @Transactional
  public void delete(String id) {
    getById(id);
    examenRepository.deleteById(id);
  }
}
