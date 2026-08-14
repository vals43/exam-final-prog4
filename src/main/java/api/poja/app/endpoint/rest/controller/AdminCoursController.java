package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.CoursDto;
import api.poja.app.service.CoursService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/cours")
@AllArgsConstructor
public class AdminCoursController {

  private final CoursService coursService;
  private final CoursMapper coursMapper;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<CoursDto> list() {
    return coursService.list().stream().map(coursMapper::toDto).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public CoursDto getById(@PathVariable String id) {
    return coursMapper.toDto(coursService.getById(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public CoursDto create(@Valid @RequestBody CoursDto dto) {
    return coursMapper.toDto(coursService.create(dto));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public CoursDto update(@PathVariable String id, @Valid @RequestBody CoursDto dto) {
    return coursMapper.toDto(coursService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable String id) {
    coursService.delete(id);
  }
}
