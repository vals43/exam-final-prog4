package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.ExamenDto;
import api.poja.app.model.Examen;
import org.springframework.stereotype.Component;

@Component
public class ExamenMapper {
  public ExamenDto toDto(Examen examen) {
    return new ExamenDto(
        examen.getId(), examen.getCours().getId(), examen.getDate(), examen.getCoefficient());
  }
}
