package api.poja.app.endpoint.rest.model;

import api.poja.app.model.ParcoursType;
import api.poja.app.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserDto(
    String id,
    String std,
    @NotBlank String nom,
    @NotBlank String prenom,
    @NotBlank @Email String email,
    @NotNull Role role,
    ParcoursType parcours,
    Integer promotion) {}
