package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.UserDto;
import api.poja.app.model.Role;
import api.poja.app.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@AllArgsConstructor
public class AdminUserController {

  private final UserService userService;
  private final UserMapper userMapper;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserDto> listByRole(@RequestParam Role role) {
    return userService.listByRole(role).stream().map(userMapper::toDto).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto getById(@PathVariable String id) {
    return userMapper.toDto(userService.getById(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto create(@Valid @RequestBody UserDto dto) {
    return userMapper.toDto(userService.create(dto));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto update(@PathVariable String id, @Valid @RequestBody UserDto dto) {
    return userMapper.toDto(userService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable String id) {
    userService.delete(id);
  }
}
