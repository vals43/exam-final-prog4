package api.poja.app.endpoint.rest.controller;

import api.poja.app.repository.UserRepository;
import api.poja.app.service.DiplomeService;
import java.util.LinkedHashMap;
import java.util.Map;
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

  private final UserRepository userRepository;
  private final DiplomeService diplomeService;

  @GetMapping("/promotions")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public String promotions(Authentication authentication, Model model) {
    boolean isAdmin =
        authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    Map<Integer, String> promotions = new LinkedHashMap<>();
    for (Integer promotion : userRepository.findDistinctPromotions()) {
      promotions.put(promotion, promotion + " (" + lettrePromotion(promotion) + ")");
    }
    model.addAttribute("promotions", promotions);
    model.addAttribute("isAdmin", isAdmin);
    return "promotions";
  }

  @GetMapping("/promotions/{promotion}/diplomes")
  @PreAuthorize("hasRole('ADMIN')")
  public String diplomes(@PathVariable Integer promotion) {
    return "redirect:" + diplomeService.genererListeDiplomes(promotion);
  }

  private static String lettrePromotion(int anneeEntree) {
    return anneeEntree == 2021 ? "G" : anneeEntree == 2022 ? "H" : "J";
  }
}
