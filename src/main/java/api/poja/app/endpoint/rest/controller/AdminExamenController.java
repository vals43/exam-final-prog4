package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.ExamenDto;
import api.poja.app.service.ExamenService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/examens")
@AllArgsConstructor
public class AdminExamenController {

  private final ExamenService examenService;
  private final ExamenMapper examenMapper;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<ExamenDto> listByCours(@RequestParam String coursId) {
    return examenService.listByCoursId(coursId).stream().map(examenMapper::toDto).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ExamenDto getById(@PathVariable String id) {
    return examenMapper.toDto(examenService.getById(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ExamenDto create(@Valid @RequestBody ExamenDto dto) {
    return examenMapper.toDto(examenService.create(dto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable String id) {
    examenService.delete(id);
  }
}
