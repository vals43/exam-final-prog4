package api.poja.app.service.export;

import api.poja.app.endpoint.rest.model.LigneReleve;
import api.poja.app.endpoint.rest.model.ReleveDto;
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
import java.nio.file.Files;
import org.springframework.stereotype.Component;

@Component
public class PdfReleveExporter {

  public File generer(ReleveDto releve) {
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
                  + " ("
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
