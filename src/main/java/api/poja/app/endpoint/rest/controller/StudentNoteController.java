package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.NoteViewDto;
import api.poja.app.model.User;
import api.poja.app.service.NoteService;
import api.poja.app.service.UserService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
@AllArgsConstructor
public class StudentNoteController {

  private final UserService userService;
  private final NoteService noteService;

  @GetMapping("/notes")
  @PreAuthorize("hasRole('STUDENT')")
  public List<NoteViewDto> myNotes(Authentication authentication) {
    User student = userService.getByEmail(authentication.getName());
    return noteService.viewByStudentId(student.getId());
  }
}
