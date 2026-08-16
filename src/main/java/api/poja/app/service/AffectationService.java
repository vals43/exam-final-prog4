package api.poja.app.service;

import api.poja.app.endpoint.rest.model.AffectationDto;
import api.poja.app.model.Affectation;
import api.poja.app.model.Cours;
import api.poja.app.model.Groupe;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.AffectationRepository;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.GroupeRepository;
import api.poja.app.repository.UserRepository;
import api.poja.app.service.exception.ConflictException;
import api.poja.app.service.exception.NotFoundException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AffectationService {

  private final AffectationRepository affectationRepository;
  private final CoursRepository coursRepository;
  private final GroupeRepository groupeRepository;
  private final UserRepository userRepository;

  public List<Affectation> list() {
    return affectationRepository.findAll();
  }

  public List<Affectation> listByAnnee(Integer annee) {
    return affectationRepository.findByAnnee(annee);
  }

  public Affectation getById(String id) {
    return affectationRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Affectation: " + id));
  }

  public List<Affectation> listByTeacherId(String teacherId) {
    return affectationRepository.findByTeacherId(teacherId);
  }

  public List<Affectation> listByGroupeId(String groupeId) {
    return affectationRepository.findByGroupeId(groupeId);
  }

  @Transactional
  public Affectation create(AffectationDto dto) {
    Cours cours =
        coursRepository
            .findById(dto.coursId())
            .orElseThrow(() -> new NotFoundException("Cours: " + dto.coursId()));
    Groupe groupe =
        groupeRepository
            .findById(dto.groupeId())
            .orElseThrow(() -> new NotFoundException("Groupe: " + dto.groupeId()));
    User teacher =
        userRepository
            .findById(dto.teacherId())
            .orElseThrow(() -> new NotFoundException("Teacher: " + dto.teacherId()));
    if (teacher.getRole() != Role.TEACHER) {
      throw new ConflictException("L'utilisateur " + dto.teacherId() + " n'est pas un enseignant");
    }
    return affectationRepository.save(
        Affectation.builder()
            .cours(cours)
            .groupe(groupe)
            .teacher(teacher)
            .annee(dto.annee())
            .build());
  }

  @Transactional
  public void delete(String id) {
    getById(id);
    affectationRepository.deleteById(id);
  }
}
