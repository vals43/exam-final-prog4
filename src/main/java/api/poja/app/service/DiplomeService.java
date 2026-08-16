package api.poja.app.service;

import api.poja.app.endpoint.rest.model.DiplomeDto;
import api.poja.app.file.bucket.BucketComponent;
import api.poja.app.model.Cours;
import api.poja.app.model.Inscription;
import api.poja.app.model.Note;
import api.poja.app.model.ParcoursType;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.InscriptionRepository;
import api.poja.app.repository.NoteRepository;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DiplomeService {

  private static final BigDecimal SEUIL_DIPLOME = new BigDecimal("10.00");
  private static final String BUCKET_PREFIX = "diplomes/";

  private final InscriptionRepository inscriptionRepository;
  private final NoteRepository noteRepository;
  private final CoursRepository coursRepository;
  private final BucketComponent bucketComponent;

  @Transactional(readOnly = true)
  public URL genererListeDiplomes(Integer annee) {
    var diplomes = diplomes(annee);
    File xlsx = genererXlsx(diplomes, annee);
    var key = BUCKET_PREFIX + "promo-" + annee + ".xlsx";
    bucketComponent.upload(xlsx, key);
    return bucketComponent.presign(key, Duration.ofMinutes(15));
  }

  @Transactional(readOnly = true)
  public List<DiplomeDto> diplomes(Integer annee) {
    var students =
        inscriptionRepository.findByAnnee(annee).stream()
            .map(Inscription::getStudent)
            .filter(s -> s.getRole() == Role.STUDENT)
            .distinct()
            .toList();
    var ranked = new ArrayList<DiplomeDto>();
    students.stream()
        .map(s -> toDiplome(s, annee))
        .filter(d -> d != null)
        .sorted(Comparator.comparing(DiplomeDto::moyenneGenerale).reversed())
        .forEach(
            d ->
                ranked.add(
                    new DiplomeDto(
                        ranked.size() + 1, d.std(), d.nom(), d.prenom(), d.moyenneGenerale())));
    return ranked;
  }

  private DiplomeDto toDiplome(User student, Integer annee) {
    var inscrits =
        inscriptionRepository.findByStudentId(student.getId()).stream()
            .filter(i -> i.getAnnee() <= annee)
            .toList();
    if (inscrits.isEmpty() || student.getParcours() == null) {
      return null;
    }
    var notesByCours =
        noteRepository.findByStudentId(student.getId()).stream()
            .collect(Collectors.groupingBy(n -> n.getExamen().getCours().getId()));
    var coursSuivis =
        coursRepository.findBySemestreBetween(1, annee * 2).stream()
            .filter(c -> appartientAuParcours(c, student.getParcours()))
            .toList();
    BigDecimal sommePonderee = BigDecimal.ZERO;
    int totalCredits = 0;
    for (Cours cours : coursSuivis) {
      BigDecimal finale = noteFinale(notesByCours.getOrDefault(cours.getId(), List.of()));
      if (finale == null || finale.compareTo(SEUIL_DIPLOME) < 0) {
        return null;
      }
      sommePonderee = sommePonderee.add(finale.multiply(BigDecimal.valueOf(cours.getCredits())));
      totalCredits += cours.getCredits();
    }
    if (totalCredits == 0) {
      return null;
    }
    var moyenne = sommePonderee.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);
    return new DiplomeDto(0, student.getStd(), student.getNom(), student.getPrenom(), moyenne);
  }

  private boolean appartientAuParcours(Cours cours, ParcoursType parcours) {
    return cours.getParcours() != null
        && cours.getParcours().stream().anyMatch(p -> p.getCode() == parcours);
  }

  private BigDecimal noteFinale(List<Note> notes) {
    if (notes.isEmpty()) {
      return null;
    }
    var somme =
        notes.stream()
            .map(n -> n.getExamen().getCoefficient().multiply(n.getValeur()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return somme.setScale(2, RoundingMode.HALF_UP);
  }

  private File genererXlsx(List<DiplomeDto> diplomes, Integer annee) {
    try {
      var file = File.createTempFile("diplomes-promo-" + annee + "-", ".xlsx");
      try (var workbook = new XSSFWorkbook();
          var out = new FileOutputStream(file)) {
        Sheet sheet = workbook.createSheet("Diplomes " + annee);
        writeRow(sheet, 0, "Rang", "STD", "Nom", "Prenom", "Moyenne");
        int i = 1;
        for (DiplomeDto d : diplomes) {
          writeRow(
              sheet,
              i++,
              String.valueOf(d.rang()),
              d.std(),
              d.nom(),
              d.prenom(),
              d.moyenneGenerale().toString());
        }
        workbook.write(out);
        return file;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeRow(Sheet sheet, int rowIndex, String... values) {
    Row row = sheet.createRow(rowIndex);
    for (int i = 0; i < values.length; i++) {
      Cell cell = row.createCell(i);
      cell.setCellValue(values[i]);
    }
  }
}
