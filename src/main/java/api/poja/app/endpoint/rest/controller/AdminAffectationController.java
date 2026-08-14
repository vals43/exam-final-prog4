package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.AffectationDto;
import api.poja.app.service.AffectationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/affectations")
@AllArgsConstructor
public class AdminAffectationController {

  private final AffectationService affectationService;
  private final AffectationMapper affectationMapper;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<AffectationDto> list() {
    return affectationService.list().stream().map(affectationMapper::toDto).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public AffectationDto getById(@PathVariable String id) {
    return affectationMapper.toDto(affectationService.getById(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public AffectationDto create(@Valid @RequestBody AffectationDto dto) {
    return affectationMapper.toDto(affectationService.create(dto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable String id) {
    affectationService.delete(id);
  }
}
