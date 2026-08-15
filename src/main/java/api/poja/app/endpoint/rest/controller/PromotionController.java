package api.poja.app.endpoint.rest.controller;

import api.poja.app.repository.InscriptionRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@AllArgsConstructor
public class PromotionController {

  private final InscriptionRepository inscriptionRepository;

  @GetMapping("/promotions")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public String promotions(Model model) {
    List<Integer> annees = inscriptionRepository.findDistinctAnnees();
    model.addAttribute("promotions", annees);
    return "promotions";
  }

  @GetMapping("/promotions/{annee}/diplomes")
  @PreAuthorize("hasRole('ADMIN')")
  public String diplomes(@PathVariable Integer annee) {
    throw new UnsupportedOperationException(
        "La liste des diplômés (XLSX) est fournie par le co-équipier (livrable B).");
  }
}
