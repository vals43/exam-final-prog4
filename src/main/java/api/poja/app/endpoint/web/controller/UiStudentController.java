package api.poja.app.endpoint.web.controller;

import api.poja.app.endpoint.rest.model.NoteViewDto;
import api.poja.app.endpoint.rest.model.ReleveDto;
import api.poja.app.endpoint.rest.model.ReleveMode;
import api.poja.app.endpoint.web.WebUsers;
import api.poja.app.model.User;
import api.poja.app.service.NoteService;
import api.poja.app.service.ReleveService;
import api.poja.app.service.exception.ConflictException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/student/ui")
@AllArgsConstructor
public class UiStudentController {

  private final WebUsers webUsers;
  private final NoteService noteService;
  private final ReleveService releveService;

  @GetMapping("/notes")
  public String notes(Authentication authentication, Model model) {
    User student = webUsers.current(authentication);
    List<NoteViewDto> notes = noteService.viewByStudentId(student.getId());
    model.addAttribute("notes", notes);
    model.addAttribute("student", student);
    return "student/notes";
  }

  @GetMapping("/releves")
  public String releves(Authentication authentication, Model model) {
    User student = webUsers.current(authentication);
    model.addAttribute("student", student);
    model.addAttribute("annees", List.of(1, 2, 3));
    return "student/releves";
  }

  @GetMapping("/releves/{annee}")
  public String releve(
      Authentication authentication,
      @PathVariable Integer annee,
      @RequestParam(defaultValue = "PROVISOIRE") ReleveMode mode,
      Model model) {
    User student = webUsers.current(authentication);
    try {
      ReleveDto releve = releveService.genererReleve(student.getId(), annee, mode);
      model.addAttribute("releve", releve);
    } catch (ConflictException e) {
      model.addAttribute("error", e.getMessage());
      model.addAttribute("releve", null);
    }
    model.addAttribute("annee", annee);
    model.addAttribute("mode", mode);
    model.addAttribute("student", student);
    return "student/releve";
  }
}
