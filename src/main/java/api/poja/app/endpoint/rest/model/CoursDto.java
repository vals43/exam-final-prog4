package api.poja.app.endpoint.rest.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CoursDto(
    String id,
    @NotBlank String ref,
    @NotBlank String intitule,
    @NotNull Integer credits,
    @NotNull Integer semestre) {}
