package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.DiplomeDto;
import org.springframework.stereotype.Component;

@Component
public class DiplomeMapper {

  public DiplomeDto withRang(int rang, DiplomeDto sansRang) {
    return new DiplomeDto(
        rang, sansRang.std(), sansRang.nom(), sansRang.prenom(), sansRang.moyenneGenerale());
  }
}
