package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.AffectationDto;
import api.poja.app.model.Affectation;
import org.springframework.stereotype.Component;

@Component
public class AffectationMapper {
  public AffectationDto toDto(Affectation affectation) {
    return new AffectationDto(
        affectation.getId(),
        affectation.getCours().getId(),
        affectation.getGroupe().getId(),
        affectation.getTeacher().getId(),
        affectation.getAnnee());
  }
}
