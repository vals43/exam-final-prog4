package api.poja.app.endpoint.rest.model;

import java.math.BigDecimal;

public record LigneReleve(
    String coursRef,
    String coursIntitule,
    Integer semestre,
    Integer credits,
    BigDecimal noteFinale,
    boolean valide) {}
