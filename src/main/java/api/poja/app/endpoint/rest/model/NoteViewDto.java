package api.poja.app.endpoint.rest.model;

import java.math.BigDecimal;
import java.time.Instant;

public record NoteViewDto(
    String id,
    String coursRef,
    String coursIntitule,
    Integer semestre,
    Instant examenDate,
    BigDecimal coefficient,
    BigDecimal valeur,
    Integer version) {}
