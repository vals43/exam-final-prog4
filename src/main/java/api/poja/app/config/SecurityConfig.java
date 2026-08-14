package api.poja.app.config;

import api.poja.app.model.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/login", "/error", "/ping", "/health/**")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole(Role.ADMIN.name())
                    .requestMatchers("/teacher/**")
                    .hasRole(Role.TEACHER.name())
                    .requestMatchers("/student/**")
                    .hasRole(Role.STUDENT.name())
                    .anyRequest()
                    .authenticated())
        .formLogin(form -> form.defaultSuccessUrl("/ping", true))
        .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login"));
    return http.build();
  }
}
