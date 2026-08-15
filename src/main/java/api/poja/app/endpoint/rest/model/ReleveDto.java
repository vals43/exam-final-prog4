package api.poja.app.endpoint.rest.model;

import java.math.BigDecimal;
import java.util.List;

public record ReleveDto(
    String studentId,
    String std,
    String nom,
    String prenom,
    Integer annee,
    ReleveMode mode,
    BigDecimal moyenneGenerale,
    Integer creditsValides,
    List<LigneReleve> lignes) {}
