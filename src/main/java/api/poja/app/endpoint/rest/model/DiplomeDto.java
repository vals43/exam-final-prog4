package api.poja.app.endpoint.rest.model;

import java.math.BigDecimal;

public record DiplomeDto(
    int rang, String std, String nom, String prenom, BigDecimal moyenneGenerale) {}
