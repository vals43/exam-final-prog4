package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.LoginRequest;
import api.poja.app.endpoint.rest.model.LoginResponse;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.UserRepository;
import api.poja.app.security.JwtService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserRepository userRepository;

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    User user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    String token =
        jwtService.generateToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build());
    return new LoginResponse(token, user.getEmail(), Role.valueOf(user.getRole().name()));
  }
}
