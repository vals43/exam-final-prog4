package api.poja.app.config;

import api.poja.app.model.Role;
import api.poja.app.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .requestCache(cache -> cache.requestCache(new NullRequestCache()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/",
                        "/login",
                        "/ui/login",
                        "/logout",
                        "/error",
                        "/ping",
                        "/health/**",
                        "/css/**",
                        "/js/**",
                        "/images/**")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole(Role.ADMIN.name())
                    .requestMatchers("/teacher/**")
                    .hasRole(Role.TEACHER.name())
                    .requestMatchers("/student/**")
                    .hasRole(Role.STUDENT.name())
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            handling ->
                handling
                    .authenticationEntryPoint(
                        (request, response, authException) -> {
                          if (acceptsHtml(request)) {
                            response.sendRedirect("/login");
                          } else {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Non authentifié\"}");
                          }
                        })
                    .accessDeniedHandler(
                        (request, response, accessDeniedException) -> {
                          if (acceptsHtml(request)) {
                            response.setStatus(403);
                            response.setContentType("text/html;charset=UTF-8");
                            response
                                .getWriter()
                                .write(
                                    "<!DOCTYPE html><html lang=\"fr\"><head><meta"
                                        + " charset=\"UTF-8\"><title>Accès"
                                        + " refusé</title></head><body"
                                        + " style=\"font-family:sans-serif;text-align:center;padding:4rem;color:#7f1d1d;\"><h1>403"
                                        + " — Accès refusé</h1><p>Vous n'avez pas les droits pour"
                                        + " accéder à cette page.</p><a href=\"/home\""
                                        + " style=\"color:#1f4e79;\">Retour à"
                                        + " l'accueil</a></body></html>");
                          } else {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Accès refusé\"}");
                          }
                        }));
    return http.build();
  }

  private static boolean acceptsHtml(HttpServletRequest request) {
    String accept = request.getHeader("Accept");
    return accept != null && accept.contains("text/html");
  }
}
