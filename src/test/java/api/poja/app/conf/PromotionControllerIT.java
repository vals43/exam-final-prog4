package api.poja.app.conf;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import api.poja.app.model.Groupe;
import api.poja.app.model.Inscription;
import api.poja.app.model.ParcoursType;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.GroupeRepository;
import api.poja.app.repository.InscriptionRepository;
import api.poja.app.repository.UserRepository;
import api.poja.app.service.DiplomeService;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PromotionControllerIT extends BaseIT {

  @Autowired MockMvc mockMvc;

  @Autowired UserRepository userRepository;

  @Autowired GroupeRepository groupeRepository;

  @Autowired InscriptionRepository inscriptionRepository;

  @MockBean DiplomeService diplomeService;

  @BeforeEach
  void setup() {
    inscriptionRepository.deleteAll();
    groupeRepository.deleteAll();
    userRepository.deleteAll();

    var admin =
        userRepository.save(
            User.builder()
                .nom("Admin")
                .prenom("System")
                .email("admin@hei.school")
                .password("x")
                .role(Role.ADMIN)
                .build());
    var groupe = groupeRepository.save(Groupe.builder().ref("K9").annee(1).build());
    var student =
        userRepository.save(
            User.builder()
                .std("STD-0001")
                .nom("Rakoto")
                .prenom("Bob")
                .email("s@hei.school")
                .password("x")
                .role(Role.STUDENT)
                .parcours(ParcoursType.EL)
                .build());
    inscriptionRepository.save(
        Inscription.builder().student(student).groupe(groupe).semestre(1).annee(1).build());
    inscriptionRepository.save(
        Inscription.builder().student(student).groupe(groupe).semestre(3).annee(2).build());
  }

  @Test
  void admin_views_promotions_page() throws Exception {
    mockMvc
        .perform(get("/promotions").with(user("admin@hei.school").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(view().name("promotions"));
  }

  @Test
  void admin_downloads_diplomes_via_redirect() throws Exception {
    var url = new URL("https://bucket.example/diplomes/promo-1.xlsx");
    when(diplomeService.genererListeDiplomes(1)).thenReturn(url);

    mockMvc
        .perform(get("/promotions/1/diplomes").with(user("admin@hei.school").roles("ADMIN")))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(url.toExternalForm()));
  }

  @Test
  void teacher_cannot_download_diplomes() throws Exception {
    mockMvc
        .perform(get("/promotions/1/diplomes").with(user("t@hei.school").roles("TEACHER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void unauthenticated_promotions_page_is_unauthorized() throws Exception {
    mockMvc.perform(get("/promotions")).andExpect(status().isUnauthorized());
  }
}
