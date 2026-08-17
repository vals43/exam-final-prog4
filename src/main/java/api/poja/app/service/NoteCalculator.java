package api.poja.app.service;

import api.poja.app.model.Cours;
import api.poja.app.model.Note;
import api.poja.app.model.ParcoursType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NoteCalculator {

  public static final BigDecimal SEUIL_VALIDATION = new BigDecimal("10.00");

  public boolean appartientAuParcours(Cours cours, ParcoursType parcours) {
    return cours.getParcours() != null
        && cours.getParcours().stream().anyMatch(p -> p.getCode() == parcours);
  }

  public BigDecimal noteFinale(List<Note> notes) {
    if (notes.isEmpty()) {
      return null;
    }
    BigDecimal totalCoefficients =
        notes.stream()
            .map(n -> n.getExamen().getCoefficient())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalCoefficients.compareTo(BigDecimal.ONE) != 0) {
      return null;
    }
    var somme =
        notes.stream()
            .map(n -> n.getExamen().getCoefficient().multiply(n.getValeur()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return somme.setScale(2, RoundingMode.HALF_UP);
  }

  public static boolean estValide(BigDecimal noteFinale) {
    return noteFinale != null && noteFinale.compareTo(SEUIL_VALIDATION) >= 0;
  }

  public static BigDecimal moyennePondereeValidee(List<NoteEtCredits> notes) {
    var valides = notes.stream().filter(n -> estValide(n.noteFinale())).toList();
    if (valides.isEmpty()) {
      return null;
    }
    int totalCredits = valides.stream().mapToInt(NoteEtCredits::credits).sum();
    BigDecimal somme =
        valides.stream()
            .map(n -> n.noteFinale().multiply(BigDecimal.valueOf(n.credits())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return somme.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);
  }

  public record NoteEtCredits(BigDecimal noteFinale, int credits) {}
}
