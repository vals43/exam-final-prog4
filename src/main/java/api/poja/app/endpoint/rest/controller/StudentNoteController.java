package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.NoteViewDto;
import api.poja.app.endpoint.rest.model.ReleveDto;
import api.poja.app.endpoint.rest.model.ReleveMode;
import api.poja.app.model.User;
import api.poja.app.service.NoteService;
import api.poja.app.service.ReleveService;
import api.poja.app.service.UserService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
@AllArgsConstructor
public class StudentNoteController {

  private final UserService userService;
  private final NoteService noteService;
  private final ReleveService releveService;

  @GetMapping("/notes")
  @PreAuthorize("hasRole('STUDENT')")
  public List<NoteViewDto> myNotes(Authentication authentication) {
    User student = userService.getByEmail(authentication.getName());
    return noteService.viewByStudentId(student.getId());
  }

  @GetMapping("/releves/{annee}")
  @PreAuthorize("hasRole('STUDENT')")
  public ReleveDto myReleve(
      Authentication authentication,
      @PathVariable Integer annee,
      @RequestParam(defaultValue = "PROVISOIRE") ReleveMode mode) {
    User student = userService.getByEmail(authentication.getName());
    return releveService.genererReleve(student.getId(), annee, mode);
  }
}
