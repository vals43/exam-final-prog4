package api.poja.app.service;

import api.poja.app.endpoint.rest.model.CoursDto;
import api.poja.app.model.Cours;
import api.poja.app.model.Parcours;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.ParcoursRepository;
import api.poja.app.service.exception.ConflictException;
import api.poja.app.service.exception.NotFoundException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CoursService {

  private final CoursRepository coursRepository;
  private final ParcoursRepository parcoursRepository;

  public List<Cours> list() {
    return coursRepository.findAll();
  }

  public Cours getById(String id) {
    return coursRepository.findById(id).orElseThrow(() -> new NotFoundException("Cours: " + id));
  }

  @Transactional
  public Cours create(CoursDto dto) {
    if (coursRepository.findByRef(dto.ref()).isPresent()) {
      throw new ConflictException("Référence déjà utilisée: " + dto.ref());
    }
    return coursRepository.save(
        Cours.builder()
            .ref(dto.ref())
            .intitule(dto.intitule())
            .credits(dto.credits())
            .semestre(dto.semestre())
            .build());
  }

  @Transactional
  public Cours update(String id, CoursDto dto) {
    Cours existing = getById(id);
    coursRepository
        .findByRef(dto.ref())
        .filter(other -> !other.getId().equals(id))
        .ifPresent(
            other -> {
              throw new ConflictException("Référence déjà utilisée: " + dto.ref());
            });
    existing.setRef(dto.ref());
    existing.setIntitule(dto.intitule());
    existing.setCredits(dto.credits());
    existing.setSemestre(dto.semestre());
    return coursRepository.save(existing);
  }

  @Transactional
  public void delete(String id) {
    getById(id);
    coursRepository.deleteById(id);
  }

  @Transactional
  public Cours addParcours(String coursId, String parcoursId) {
    Cours cours = getById(coursId);
    Parcours parcours =
        parcoursRepository
            .findById(parcoursId)
            .orElseThrow(() -> new NotFoundException("Parcours: " + parcoursId));
    if (cours.getParcours() == null) {
      cours.setParcours(new java.util.ArrayList<>());
    }
    if (cours.getParcours().stream().noneMatch(p -> p.getId().equals(parcoursId))) {
      cours.getParcours().add(parcours);
      return coursRepository.save(cours);
    }
    return cours;
  }
}
