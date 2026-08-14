package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.CoursDto;
import api.poja.app.model.Cours;
import org.springframework.stereotype.Component;

@Component
public class CoursMapper {
  public CoursDto toDto(Cours cours) {
    return new CoursDto(
        cours.getId(),
        cours.getRef(),
        cours.getIntitule(),
        cours.getCredits(),
        cours.getSemestre());
  }
}
