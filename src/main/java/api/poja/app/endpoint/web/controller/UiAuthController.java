package api.poja.app.endpoint.web.controller;

import api.poja.app.endpoint.rest.model.LoginRequest;
import api.poja.app.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class UiAuthController {

  public static final String AUTH_COOKIE = "AUTH_TOKEN";
  private static final int COOKIE_MAX_AGE_SECONDS = 86400;

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @GetMapping("/")
  public String root() {
    return "redirect:/home";
  }

  @GetMapping("/login")
  public String loginPage() {
    return "login";
  }

  @PostMapping("/ui/login")
  public String login(@Valid LoginRequest request, Model model, HttpServletResponse response) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    } catch (RuntimeException e) {
      model.addAttribute("error", "Email ou mot de passe incorrect");
      model.addAttribute("email", request.email());
      return "login";
    }
    UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
    response.addCookie(authCookie(jwtService.generateToken(userDetails)));
    return "redirect:/home";
  }

  @GetMapping("/logout")
  public String logout(HttpServletResponse response) {
    Cookie cookie = new Cookie(AUTH_COOKIE, "");
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
    return "redirect:/login";
  }

  private static Cookie authCookie(String token) {
    Cookie cookie = new Cookie(AUTH_COOKIE, token);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
    cookie.setAttribute("SameSite", "Lax");
    return cookie;
  }
}
