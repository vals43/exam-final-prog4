package api.poja.app.endpoint.web.controller;

import api.poja.app.endpoint.rest.model.AffectationDto;
import api.poja.app.endpoint.rest.model.CoursDto;
import api.poja.app.endpoint.rest.model.ReleveDto;
import api.poja.app.endpoint.rest.model.ReleveMode;
import api.poja.app.endpoint.rest.model.UserDto;
import api.poja.app.endpoint.web.model.ExamenForm;
import api.poja.app.model.Role;
import api.poja.app.repository.AffectationRepository;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.ExamenRepository;
import api.poja.app.repository.GroupeRepository;
import api.poja.app.repository.NoteRepository;
import api.poja.app.repository.UserRepository;
import api.poja.app.service.AffectationService;
import api.poja.app.service.CoursService;
import api.poja.app.service.ExamenService;
import api.poja.app.service.ReleveService;
import api.poja.app.service.UserService;
import api.poja.app.service.exception.ConflictException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/ui")
@AllArgsConstructor
public class UiAdminController {

  private final UserService userService;
  private final CoursService coursService;
  private final ExamenService examenService;
  private final AffectationService affectationService;
  private final ReleveService releveService;
  private final UserRepository userRepository;
  private final CoursRepository coursRepository;
  private final ExamenRepository examenRepository;
  private final AffectationRepository affectationRepository;
  private final GroupeRepository groupeRepository;
  private final NoteRepository noteRepository;

  @GetMapping
  public String dashboard(Model model) {
    model.addAttribute("nbCours", coursRepository.count());
    model.addAttribute("nbExamens", examenRepository.count());
    model.addAttribute("nbAffectations", affectationRepository.count());
    model.addAttribute("nbNotes", noteRepository.count());
    model.addAttribute("nbGroupes", groupeRepository.count());
    model.addAttribute("nbEtudiants", userRepository.findByRole(Role.STUDENT).size());
    model.addAttribute("nbEnseignants", userRepository.findByRole(Role.TEACHER).size());
    model.addAttribute("promotions", userRepository.findDistinctPromotions());
    return "admin/dashboard";
  }

  // ---------------- COURS ----------------

  @GetMapping("/cours")
  public String cours(Model model) {
    model.addAttribute("cours", coursService.list());
    model.addAttribute("form", new CoursDto(null, "", "", null, null));
    return "admin/cours";
  }

  @PostMapping("/cours")
  public String createCours(@ModelAttribute CoursDto form, Model model) {
    String error = validateCours(form);
    if (error != null) {
      return renderCoursError(model, form, error);
    }
    try {
      coursService.create(form);
      return "redirect:/admin/ui/cours?created=1";
    } catch (ConflictException e) {
      return renderCoursError(model, form, e.getMessage());
    }
  }

  @GetMapping("/cours/{id}/edit")
  public String editCours(@PathVariable String id, Model model) {
    var c = coursService.getById(id);
    model.addAttribute("cours", coursService.list());
    model.addAttribute(
        "form",
        new CoursDto(c.getId(), c.getRef(), c.getIntitule(), c.getCredits(), c.getSemestre()));
    model.addAttribute("editId", id);
    return "admin/cours";
  }

  @PostMapping("/cours/{id}")
  public String updateCours(@PathVariable String id, @ModelAttribute CoursDto form, Model model) {
    String error = validateCours(form);
    if (error != null) {
      return renderCoursError(model, form, error);
    }
    try {
      coursService.update(id, form);
      return "redirect:/admin/ui/cours?created=1";
    } catch (RuntimeException e) {
      return renderCoursError(model, form, friendly(e));
    }
  }

  @PostMapping("/cours/{id}/delete")
  public String deleteCours(@PathVariable String id, Model model) {
    try {
      coursService.delete(id);
      return "redirect:/admin/ui/cours";
    } catch (DataIntegrityViolationException e) {
      return renderCoursError(
          model,
          new CoursDto(null, "", "", null, null),
          "Impossible de supprimer ce cours : il est référencé par des examens ou des"
              + " affectations.");
    }
  }

  private static String validateCours(CoursDto form) {
    if (form.ref() == null || form.ref().isBlank()) {
      return "La référence du cours est obligatoire.";
    }
    if (form.intitule() == null || form.intitule().isBlank()) {
      return "L'intitulé du cours est obligatoire.";
    }
    if (form.credits() == null) {
      return "Le nombre de crédits est obligatoire.";
    }
    if (form.semestre() == null) {
      return "Le semestre est obligatoire.";
    }
    return null;
  }

  private String renderCoursError(Model model, CoursDto form, String error) {
    model.addAttribute("cours", coursService.list());
    model.addAttribute("form", form);
    model.addAttribute("error", error);
    return "admin/cours";
  }

  // ---------------- EXAMENS ----------------

  @GetMapping("/examens")
  public String examens(@RequestParam(required = false) String coursId, Model model) {
    model.addAttribute("coursList", coursService.list());
    if (coursId != null) {
      model.addAttribute("examens", examenService.listByCoursId(coursId));
      model.addAttribute("coursSelectionne", coursService.getById(coursId));
    }
    model.addAttribute("form", new ExamenForm("", "", null));
    return "admin/examens";
  }

  @PostMapping("/examens")
  public String createExamen(@ModelAttribute ExamenForm form, Model model) {
    if (form.coursId() == null || form.coursId().isBlank()) {
      return renderExamenError(model, form, "Choisissez un cours.");
    }
    Instant date;
    try {
      date = parseDate(form.date());
    } catch (IllegalArgumentException e) {
      return renderExamenError(model, form, e.getMessage());
    }
    if (form.coefficient() == null) {
      return renderExamenError(model, form, "Le coefficient est obligatoire.");
    }
    try {
      examenService.create(
          new api.poja.app.endpoint.rest.model.ExamenDto(
              null, form.coursId(), date, form.coefficient()));
      return "redirect:/admin/ui/examens?coursId=" + form.coursId();
    } catch (ConflictException e) {
      return renderExamenError(model, form, e.getMessage());
    }
  }

  @PostMapping("/examens/{id}/delete")
  public String deleteExamen(
      @PathVariable String id, @RequestParam(required = false) String coursId) {
    try {
      examenService.delete(id);
    } catch (DataIntegrityViolationException ignored) {
      return "redirect:/admin/ui/examens?coursId=" + (coursId == null ? "" : coursId);
    }
    return "redirect:/admin/ui/examens?coursId=" + (coursId == null ? "" : coursId);
  }

  private String renderExamenError(Model model, ExamenForm form, String error) {
    model.addAttribute("coursList", coursService.list());
    model.addAttribute("form", form);
    model.addAttribute("error", error);
    return "admin/examens";
  }

  // ---------------- AFFECTATIONS ----------------

  @GetMapping("/affectations")
  public String affectations(Model model) {
    model.addAttribute("affectations", affectationService.list());
    model.addAttribute("coursList", coursService.list());
    model.addAttribute("groupes", groupeRepository.findAll());
    model.addAttribute("enseignants", userService.listByRole(Role.TEACHER));
    model.addAttribute("form", new AffectationDto(null, "", "", "", null));
    return "admin/affectations";
  }

  @PostMapping("/affectations")
  public String createAffectation(@ModelAttribute AffectationDto form, Model model) {
    if (form.coursId() == null
        || form.coursId().isBlank()
        || form.groupeId() == null
        || form.groupeId().isBlank()
        || form.teacherId() == null
        || form.teacherId().isBlank()
        || form.annee() == null) {
      return renderAffectationError(
          model, form, "Veuillez compléter tous les champs de l'affectation.");
    }
    try {
      affectationService.create(form);
      return "redirect:/admin/ui/affectations";
    } catch (ConflictException e) {
      return renderAffectationError(model, form, e.getMessage());
    }
  }

  @PostMapping("/affectations/{id}/delete")
  public String deleteAffectation(@PathVariable String id) {
    try {
      affectationService.delete(id);
    } catch (DataIntegrityViolationException ignored) {
      // referenced by notes
    }
    return "redirect:/admin/ui/affectations";
  }

  private String renderAffectationError(Model model, AffectationDto form, String error) {
    model.addAttribute("affectations", affectationService.list());
    model.addAttribute("coursList", coursService.list());
    model.addAttribute("groupes", groupeRepository.findAll());
    model.addAttribute("enseignants", userService.listByRole(Role.TEACHER));
    model.addAttribute("form", form);
    model.addAttribute("error", error);
    return "admin/affectations";
  }

  // ---------------- UTILISATEURS ----------------

  @GetMapping("/users")
  public String users(
      @RequestParam(defaultValue = "STUDENT") Role role,
      @RequestParam(required = false) String edit,
      Model model) {
    model.addAttribute("role", role);
    model.addAttribute("users", userService.listByRole(role));
    UserDto form = new UserDto(null, "", "", "", "", role, null, null);
    if (edit != null) {
      var u = userService.getById(edit);
      form =
          new UserDto(
              u.getId(),
              u.getStd(),
              u.getNom(),
              u.getPrenom(),
              u.getEmail(),
              u.getRole(),
              u.getParcours(),
              u.getPromotion());
    }
    model.addAttribute("form", form);
    return "admin/users";
  }

  @PostMapping("/users")
  public String createUser(@ModelAttribute UserDto form, Model model) {
    UserDto cleaned = sanitize(form);
    try {
      userService.create(cleaned);
      return "redirect:/admin/ui/users?role=" + cleaned.role();
    } catch (RuntimeException e) {
      return renderUserError(model, cleaned, friendly(e));
    }
  }

  @PostMapping("/users/{id}")
  public String updateUser(@PathVariable String id, @ModelAttribute UserDto form, Model model) {
    UserDto cleaned = sanitize(form);
    try {
      userService.update(id, cleaned);
      return "redirect:/admin/ui/users?role=" + cleaned.role();
    } catch (RuntimeException e) {
      return renderUserError(model, cleaned, friendly(e));
    }
  }

  @PostMapping("/users/{id}/delete")
  public String deleteUser(@PathVariable String id, @RequestParam Role role) {
    try {
      userService.delete(id);
    } catch (DataIntegrityViolationException e) {
      return "redirect:/admin/ui/users?role=" + role;
    }
    return "redirect:/admin/ui/users?role=" + role;
  }

  private String renderUserError(Model model, UserDto form, String error) {
    model.addAttribute("role", form.role());
    model.addAttribute("users", userService.listByRole(form.role()));
    model.addAttribute("form", form);
    model.addAttribute("error", error);
    return "admin/users";
  }

  private static UserDto sanitize(UserDto dto) {
    String std = dto.std() == null || dto.std().isBlank() ? null : dto.std();
    return new UserDto(
        dto.id(),
        std,
        dto.nom(),
        dto.prenom(),
        dto.email(),
        dto.role(),
        dto.parcours(),
        dto.promotion());
  }

  // ---------------- RELEVÉS ----------------

  @GetMapping("/releves")
  public String releves(Model model) {
    model.addAttribute("etudiants", userService.listByRole(Role.STUDENT));
    model.addAttribute("annees", List.of(1, 2, 3));
    return "admin/releves";
  }

  @PostMapping("/releves")
  public String genererReleve(
      @RequestParam String studentId,
      @RequestParam Integer annee,
      @RequestParam(defaultValue = "PROVISOIRE") ReleveMode mode,
      Model model) {
    model.addAttribute("etudiants", userService.listByRole(Role.STUDENT));
    model.addAttribute("annees", List.of(1, 2, 3));
    try {
      ReleveDto releve = releveService.genererReleve(studentId, annee, mode);
      model.addAttribute("releve", releve);
      model.addAttribute(
          "success",
          "Relevé de " + releve.prenom() + " " + releve.nom() + " généré et envoyé par email.");
    } catch (ConflictException e) {
      model.addAttribute("error", e.getMessage());
    }
    return "admin/releves";
  }

  private static Instant parseDate(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("La date de l'examen est obligatoire.");
    }
    return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
  }

  private static String friendly(RuntimeException e) {
    if (e instanceof IllegalArgumentException ia) {
      return ia.getMessage() == null ? "Données invalides." : ia.getMessage();
    }
    return e.getMessage() == null ? "Opération impossible." : e.getMessage();
  }
}
