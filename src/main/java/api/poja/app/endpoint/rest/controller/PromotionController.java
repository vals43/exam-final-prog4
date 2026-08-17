package api.poja.app.endpoint.rest.controller;

import api.poja.app.repository.InscriptionRepository;
import api.poja.app.service.DiplomeService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@AllArgsConstructor
public class PromotionController {

  private final InscriptionRepository inscriptionRepository;
  private final DiplomeService diplomeService;

  @GetMapping("/promotions")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public String promotions(Authentication authentication, Model model) {
    boolean isAdmin =
        authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    List<Integer> annees = inscriptionRepository.findDistinctAnnees();
    model.addAttribute("promotions", annees);
    model.addAttribute("isAdmin", isAdmin);
    return "promotions";
  }

  @GetMapping("/promotions/{annee}/diplomes")
  @PreAuthorize("hasRole('ADMIN')")
  public String diplomes(@PathVariable Integer annee) {
    return "redirect:" + diplomeService.genererListeDiplomes(annee);
  }
}
