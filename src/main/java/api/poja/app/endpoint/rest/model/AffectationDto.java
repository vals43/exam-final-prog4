package api.poja.app.endpoint.rest.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AffectationDto(
    String id,
    @NotBlank String coursId,
    @NotBlank String groupeId,
    @NotBlank String teacherId,
    @NotNull Integer annee) {}
