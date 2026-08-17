package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.LigneReleve;
import api.poja.app.endpoint.rest.model.ReleveDto;
import api.poja.app.endpoint.rest.model.ReleveMode;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ReleveMapper {

  public ReleveDto toDto(
      String studentId,
      String std,
      String nom,
      String prenom,
      Integer annee,
      ReleveMode mode,
      BigDecimal moyenneGenerale,
      Integer creditsValides,
      List<LigneReleve> lignes) {
    return new ReleveDto(
        studentId, std, nom, prenom, annee, mode, moyenneGenerale, creditsValides, lignes);
  }
}
