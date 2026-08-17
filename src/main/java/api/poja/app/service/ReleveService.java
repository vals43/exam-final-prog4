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
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
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
    bucketComponent.upload(genererPdf(releve), key);
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

  private File genererPdf(ReleveDto releve) {
    try {
      var file = File.createTempFile("releve-" + releve.studentId() + "-" + releve.annee(), ".pdf");
      var document = new Document();
      PdfWriter.getInstance(document, Files.newOutputStream(file.toPath()));
      document.open();
      document.add(
          new Paragraph(
              "Relevé de notes — "
                  + releve.prenom()
                  + " "
                  + releve.nom()
                  + " (STD "
                  + releve.std()
                  + ")",
              FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD)));
      document.add(new Paragraph("Année : " + releve.annee() + " — Mode : " + releve.mode()));
      document.add(new Paragraph(" "));
      var table = new PdfPTable(new float[] {3f, 6f, 2f, 2f, 2f});
      table.setWidthPercentage(100);
      table.addCell(cellBold("Réf"));
      table.addCell(cellBold("Intitulé"));
      table.addCell(cellBold("Sem."));
      table.addCell(cellBold("Crédits"));
      table.addCell(cellBold("Note"));
      for (LigneReleve ligne : releve.lignes()) {
        table.addCell(new PdfPCell(new Phrase(ligne.coursRef())));
        table.addCell(new PdfPCell(new Phrase(ligne.coursIntitule())));
        table.addCell(new PdfPCell(new Phrase(String.valueOf(ligne.semestre()))));
        table.addCell(new PdfPCell(new Phrase(String.valueOf(ligne.credits()))));
        table.addCell(
            new PdfPCell(
                new Phrase(ligne.noteFinale() == null ? "—" : ligne.noteFinale().toString())));
      }
      document.add(table);
      document.add(new Paragraph(" "));
      document.add(
          new Paragraph(
              "Moyenne générale : "
                  + (releve.moyenneGenerale() == null ? "—" : releve.moyenneGenerale())
                  + " — Crédits validés : "
                  + releve.creditsValides()));
      document.close();
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static PdfPCell cellBold(String text) {
    return new PdfPCell(
        new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD)));
  }
}