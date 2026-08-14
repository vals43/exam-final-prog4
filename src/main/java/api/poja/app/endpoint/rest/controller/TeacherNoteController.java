package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.AffectationDto;
import api.poja.app.endpoint.rest.model.NoteDto;
import api.poja.app.model.User;
import api.poja.app.service.AffectationService;
import api.poja.app.service.NoteService;
import api.poja.app.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teacher")
@AllArgsConstructor
public class TeacherNoteController {

  private final UserService userService;
  private final AffectationService affectationService;
  private final AffectationMapper affectationMapper;
  private final NoteService noteService;

  @GetMapping("/affectations")
  @PreAuthorize("hasRole('TEACHER')")
  public List<AffectationDto> myAssignments(Authentication authentication) {
    User teacher = userService.getByEmail(authentication.getName());
    return affectationService.listByTeacherId(teacher.getId()).stream()
        .map(affectationMapper::toDto)
        .toList();
  }

  @GetMapping("/affectations/{groupeId}/{annee}")
  @PreAuthorize("hasRole('TEACHER')")
  public List<AffectationDto> myAssignmentsForGroup(
      Authentication authentication, @PathVariable String groupeId, @PathVariable Integer annee) {
    User teacher = userService.getByEmail(authentication.getName());
    return affectationService.listByTeacherId(teacher.getId()).stream()
        .filter(a -> a.getGroupe().getId().equals(groupeId) && a.getAnnee().equals(annee))
        .map(affectationMapper::toDto)
        .toList();
  }

  @PostMapping("/notes")
  @PreAuthorize("hasRole('TEACHER')")
  public void grade(Authentication authentication, @Valid @RequestBody NoteDto dto) {
    User teacher = userService.getByEmail(authentication.getName());
    noteService.grade(dto, teacher);
  }
}
