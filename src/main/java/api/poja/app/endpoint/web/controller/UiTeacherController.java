package api.poja.app.endpoint.web.controller;

import api.poja.app.endpoint.rest.model.NoteDto;
import api.poja.app.endpoint.web.WebUsers;
import api.poja.app.endpoint.web.model.GradeCell;
import api.poja.app.endpoint.web.model.GradeRow;
import api.poja.app.endpoint.web.model.TeacherAffectationView;
import api.poja.app.endpoint.web.model.TeacherNoteView;
import api.poja.app.model.Affectation;
import api.poja.app.model.Cours;
import api.poja.app.model.Examen;
import api.poja.app.model.Inscription;
import api.poja.app.model.Note;
import api.poja.app.model.NoteHistory;
import api.poja.app.model.User;
import api.poja.app.repository.InscriptionRepository;
import api.poja.app.repository.NoteHistoryRepository;
import api.poja.app.repository.NoteRepository;
import api.poja.app.service.AffectationService;
import api.poja.app.service.ExamenService;
import api.poja.app.service.NoteService;
import api.poja.app.service.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/teacher/ui")
@AllArgsConstructor
public class UiTeacherController {

  private final WebUsers webUsers;
  private final AffectationService affectationService;
  private final ExamenService examenService;
  private final NoteService noteService;
  private final NoteRepository noteRepository;
  private final NoteHistoryRepository noteHistoryRepository;
  private final InscriptionRepository inscriptionRepository;

  @GetMapping("/affectations")
  public String affectations(Authentication authentication, Model model) {
    User teacher = webUsers.current(authentication);
    List<TeacherAffectationView> views =
        affectationService.listByTeacherId(teacher.getId()).stream()
            .map(
                a ->
                    new TeacherAffectationView(
                        a.getCours().getId(),
                        a.getCours().getRef(),
                        a.getCours().getIntitule(),
                        a.getGroupe().getId(),
                        a.getGroupe().getRef(),
                        a.getAnnee()))
            .sorted(
                Comparator.comparing(TeacherAffectationView::getAnnee)
                    .thenComparing(TeacherAffectationView::getGroupeRef)
                    .thenComparing(TeacherAffectationView::getCoursRef))
            .toList();
    model.addAttribute("affectations", views);
    model.addAttribute("teacher", teacher);
    return "teacher/affectations";
  }

  @GetMapping("/affectations/{groupeId}/{annee}/{coursId}")
  public String groupeNotes(
      Authentication authentication,
      @PathVariable String groupeId,
      @PathVariable Integer annee,
      @PathVariable String coursId,
      @RequestParam(required = false) Integer saved,
      Model model) {
    if (saved != null && saved > 0) {
      model.addAttribute("success", saved + " note(s) enregistrée(s).");
    }
    return buildGradePage(authentication, groupeId, annee, coursId, model, null);
  }

  @PostMapping("/notes")
  public String grade(
      Authentication authentication,
      @RequestParam String studentId,
      @RequestParam String inscriptionId,
      @RequestParam String coursId,
      @RequestParam String groupeId,
      @RequestParam Integer annee,
      @RequestParam(required = false) String raison,
      @RequestParam Map<String, String> params,
      Model model) {
    User teacher = webUsers.current(authentication);
    Map<String, BigDecimal> grades = new HashMap<>();
    String error = null;
    for (Map.Entry<String, String> e : params.entrySet()) {
      if (e.getKey().startsWith("valeur_") && e.getValue() != null && !e.getValue().isBlank()) {
        String examenId = e.getKey().substring("valeur_".length());
        try {
          BigDecimal value = new BigDecimal(e.getValue());
          if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("20")) > 0) {
            error = "La note doit être comprise entre 0 et 20.";
            break;
          }
          grades.put(examenId, value);
        } catch (NumberFormatException nfe) {
          error = "Note invalide pour l'examen " + examenId;
          break;
        }
      }
    }
    if (error == null && !grades.isEmpty() && (raison == null || raison.isBlank())) {
      error = "La raison de la modification est obligatoire.";
    }
    if (error != null) {
      model.addAttribute("error", error);
      return buildGradePage(authentication, groupeId, annee, coursId, model, error);
    }
    int count = 0;
    for (Map.Entry<String, BigDecimal> g : grades.entrySet()) {
      noteService.grade(
          new NoteDto(null, studentId, g.getKey(), inscriptionId, g.getValue(), raison), teacher);
      count++;
    }
    return "redirect:/teacher/ui/affectations/"
        + groupeId
        + "/"
        + annee
        + "/"
        + coursId
        + "?saved="
        + count;
  }

  @GetMapping("/notes")
  public String notes(Authentication authentication, Model model) {
    User teacher = webUsers.current(authentication);
    List<Affectation> assignments = affectationService.listByTeacherId(teacher.getId());
    List<TeacherNoteView> views =
        noteRepository.findAll().stream()
            .filter(
                n ->
                    assignments.stream()
                        .anyMatch(
                            a ->
                                a.getCours().getId().equals(n.getExamen().getCours().getId())
                                    && a.getGroupe()
                                        .getId()
                                        .equals(n.getInscription().getGroupe().getId())
                                    && a.getAnnee().equals(n.getInscription().getAnnee())))
            .map(
                n ->
                    new TeacherNoteView(
                        n.getExamen().getCours().getRef(),
                        n.getExamen().getCours().getIntitule(),
                        n.getExamen().getCours().getSemestre(),
                        n.getStudent().getStd(),
                        n.getStudent().getNom(),
                        n.getStudent().getPrenom(),
                        n.getExamen().getDate(),
                        n.getExamen().getCoefficient(),
                        n.getValeur(),
                        n.getVersion()))
            .sorted(
                Comparator.comparing(TeacherNoteView::getCoursRef)
                    .thenComparing(TeacherNoteView::getStudentStd)
                    .thenComparing(TeacherNoteView::getExamenDate))
            .toList();
    model.addAttribute("notes", views);
    model.addAttribute("teacher", teacher);
    return "teacher/notes";
  }

  private String buildGradePage(
      Authentication authentication,
      String groupeId,
      Integer annee,
      String coursId,
      Model model,
      String error) {
    User teacher = webUsers.current(authentication);
    Affectation affectation =
        affectationService.listByTeacherId(teacher.getId()).stream()
            .filter(
                a ->
                    a.getCours().getId().equals(coursId)
                        && a.getGroupe().getId().equals(groupeId)
                        && a.getAnnee().equals(annee))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Affectation " + coursId + "/" + groupeId));
    Cours cours = affectation.getCours();
    List<Examen> examens = examenService.listByCoursId(coursId);
    List<Inscription> inscriptions = inscriptionRepository.findByGroupeIdAndAnnee(groupeId, annee);

    Map<String, Note> notesByKey = new HashMap<>();
    for (Note n : noteRepository.findAll()) {
      notesByKey.put(n.getStudent().getId() + "|" + n.getExamen().getId(), n);
    }

    List<GradeRow> rows =
        inscriptions.stream()
            .map(
                ins ->
                    new GradeRow(
                        ins.getStudent(),
                        ins.getId(),
                        examens.stream()
                            .map(
                                ex -> {
                                  Note note =
                                      notesByKey.get(ins.getStudent().getId() + "|" + ex.getId());
                                  List<NoteHistory> history =
                                      note == null
                                          ? List.of()
                                          : noteHistoryRepository.findByNoteId(note.getId());
                                  return new GradeCell(
                                      ex.getId(),
                                      ex.getDate(),
                                      ex.getCoefficient(),
                                      note == null ? null : note.getId(),
                                      note == null ? null : note.getValeur(),
                                      note == null ? null : note.getVersion(),
                                      history);
                                })
                            .toList()))
            .sorted(Comparator.comparing(r -> r.getStudent().getNom()))
            .toList();

    model.addAttribute("affectation", affectation);
    model.addAttribute("cours", cours);
    model.addAttribute("examens", examens);
    model.addAttribute("rows", rows);
    model.addAttribute("annee", annee);
    return "teacher/groupe-notes";
  }
}
