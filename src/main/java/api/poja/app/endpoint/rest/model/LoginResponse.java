package api.poja.app.endpoint.rest.model;

import api.poja.app.model.Role;

public record LoginResponse(String token, String email, Role role) {}
