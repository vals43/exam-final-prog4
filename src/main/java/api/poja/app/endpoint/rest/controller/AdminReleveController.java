package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.ReleveDto;
import api.poja.app.endpoint.rest.model.ReleveMode;
import api.poja.app.service.ReleveService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/releves")
@AllArgsConstructor
public class AdminReleveController {

  private final ReleveService releveService;

  @PostMapping("/{studentId}/{annee}")
  @PreAuthorize("hasRole('ADMIN')")
  public ReleveDto generer(
      @PathVariable String studentId,
      @PathVariable Integer annee,
      @RequestParam(defaultValue = "PROVISOIRE") ReleveMode mode) {
    return releveService.genererReleve(studentId, annee, mode);
  }
}
