package api.poja.app.endpoint.rest.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record ExamenDto(
    String id, @NotBlank String coursId, @NotNull Instant date, @NotNull BigDecimal coefficient) {}
