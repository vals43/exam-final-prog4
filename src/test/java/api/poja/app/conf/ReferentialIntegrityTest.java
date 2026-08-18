package api.poja.app.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import api.poja.app.model.Cours;
import api.poja.app.model.Parcours;
import api.poja.app.model.ParcoursType;
import api.poja.app.service.SchoolDataSeeder;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReferentialIntegrityTest {

  @Test
  void chaqueSemestreVautExactement30CreditsPourELEtTN() {
    var el = Parcours.builder().code(ParcoursType.EL).nom("Électronicien").build();
    var tn = Parcours.builder().code(ParcoursType.TN).nom("Télécommunicant").build();
    var cours = SchoolDataSeeder.definitions(el, tn);

    for (int semestre = 1; semestre <= 6; semestre++) {
      int elCredits = creditsFor(cours, semestre, ParcoursType.EL);
      int tnCredits = creditsFor(cours, semestre, ParcoursType.TN);
      assertEquals(30, elCredits, "Semestre " + semestre + " (EL)");
      assertEquals(30, tnCredits, "Semestre " + semestre + " (TN)");
    }
  }

  private int creditsFor(List<Cours> cours, int semestre, ParcoursType parcours) {
    return cours.stream()
        .filter(c -> c.getSemestre() == semestre)
        .filter(c -> c.getParcours().stream().anyMatch(p -> p.getCode() == parcours))
        .mapToInt(Cours::getCredits)
        .sum();
  }
}
