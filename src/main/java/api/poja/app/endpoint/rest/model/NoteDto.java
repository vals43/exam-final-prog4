package api.poja.app.endpoint.rest.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record NoteDto(
    String id,
    @NotBlank String studentId,
    @NotBlank String examenId,
    @NotBlank String inscriptionId,
    @NotNull @DecimalMin("0.00") @DecimalMax("20.00") BigDecimal valeur) {}
