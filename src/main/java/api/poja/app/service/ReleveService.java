package api.poja.app.service;

import api.poja.app.endpoint.event.EventProducer;
import api.poja.app.endpoint.event.model.SendEmailRequested;
import api.poja.app.endpoint.rest.model.LigneReleve;
import api.poja.app.endpoint.rest.model.ReleveDto;
import api.poja.app.endpoint.rest.model.ReleveMode;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.model.Cours;
import api.poja.app.model.Note;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.NoteRepository;
import api.poja.app.service.exception.ConflictException;
import api.poja.app.service.export.PdfReleveExporter;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ReleveService {

  private final UserService userService;
  private final CoursRepository coursRepository;
  private final NoteRepository noteRepository;
  private final BucketComponent bucketComponent;
  private final EventProducer<SendEmailRequested> eventProducer;
  private final NoteCalculator noteCalculator;
  private final PdfReleveExporter pdfReleveExporter;

  @Transactional
  public ReleveDto genererReleve(String studentId, Integer annee, ReleveMode mode) {
    User student = userService.getById(studentId);
    if (student.getRole() != Role.STUDENT) {
      throw new ConflictException("L'utilisateur " + studentId + " n'est pas un étudiant");
    }
    int semestreDebut = annee * 2 - 1;
    int semestreFin = annee * 2;
    List<Cours> coursAnnee =
        coursRepository.findBySemestreBetween(semestreDebut, semestreFin).stream()
            .filter(c -> noteCalculator.appartientAuParcours(c, student.getParcours()))
            .toList();
    var notesByCours =
        noteRepository.findByStudentId(studentId).stream()
            .collect(Collectors.groupingBy(n -> n.getExamen().getCours().getId()));
    List<LigneReleve> lignes =
        coursAnnee.stream()
            .map(c -> toLigne(c, notesByCours.getOrDefault(c.getId(), List.of())))
            .sorted(
                Comparator.comparing(LigneReleve::semestre).thenComparing(LigneReleve::coursRef))
            .toList();
    boolean toutesNotees = lignes.stream().allMatch(l -> l.noteFinale() != null);
    if (mode == ReleveMode.COMPLET && !toutesNotees) {
      throw new ConflictException(
          "Relevé complet impossible : toutes les notes de l'année "
              + annee
              + " ne sont pas saisies");
    }
    int creditsValides =
        lignes.stream().filter(LigneReleve::valide).mapToInt(LigneReleve::credits).sum();
    BigDecimal moyenne = moyennePonderee(lignes);
    ReleveDto releve =
        new ReleveDto(
            studentId,
            student.getStd(),
            student.getNom(),
            student.getPrenom(),
            annee,
            mode,
            moyenne,
            creditsValides,
            lignes);

    var key = "releves/" + student.getStd() + "/" + annee + ".pdf";
    bucketComponent.upload(pdfReleveExporter.generer(releve), key);
    eventProducer.accept(
        List.of(
            SendEmailRequested.builder()
                .to(student.getEmail())
                .subject("Relevé de notes " + student.getPrenom() + " " + student.getNom())
                .htmlBody("<p>Votre relevé de notes de l'année " + annee + " est disponible.</p>")
                .bucketKey(key)
                .build()));
    return releve;
  }

  private LigneReleve toLigne(Cours cours, List<Note> notes) {
    BigDecimal finale = noteCalculator.noteFinale(notes);
    return new LigneReleve(
        cours.getRef(),
        cours.getIntitule(),
        cours.getSemestre(),
        cours.getCredits(),
        finale,
        NoteCalculator.estValide(finale));
  }

  private BigDecimal moyennePonderee(List<LigneReleve> lignes) {
    var notesEtCredits =
        lignes.stream()
            .map(l -> new NoteCalculator.NoteEtCredits(l.noteFinale(), l.credits()))
            .toList();
    return NoteCalculator.moyennePondereeValidee(notesEtCredits);
  }
}
