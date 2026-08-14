package api.poja.app.service;

import api.poja.app.model.Affectation;
import api.poja.app.model.Cours;
import api.poja.app.model.Examen;
import api.poja.app.model.Groupe;
import api.poja.app.model.Inscription;
import api.poja.app.model.Parcours;
import api.poja.app.model.ParcoursType;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.AffectationRepository;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.ExamenRepository;
import api.poja.app.repository.GroupeRepository;
import api.poja.app.repository.InscriptionRepository;
import api.poja.app.repository.ParcoursRepository;
import api.poja.app.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Profile("!test")
@AllArgsConstructor
public class SchoolDataSeeder implements CommandLineRunner {
  private static final List<String> COURSE_REFS =
      List.of(
          "PROK1", "WEB1", "CQ1", "MATH1", "ANG1", "SYS1", "PROK2", "WEB2", "CQ2", "MATH2", "ANG2",
          "SYS2", "PROK3", "WEB3", "MATH3", "CQ3", "PROBL3", "TN1", "ELEC1", "PROK4", "ELEC2",
          "TN2", "TN3", "WEB4", "MATH4", "CQ4", "ANG4", "MOB4", "INC5", "DIST5", "ROUT5", "ADM5",
          "CQ5", "WEB5", "ANG5", "MAG5", "PFE6");

  private final ParcoursRepository parcoursRepository;
  private final CoursRepository coursRepository;
  private final GroupeRepository groupeRepository;
  private final UserRepository userRepository;
  private final ExamenRepository examenRepository;
  private final AffectationRepository affectationRepository;
  private final InscriptionRepository inscriptionRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) {
    if (parcoursRepository.count() > 0) {
      log.info("Seed déjà exécuté, aucune insertion.");
      return;
    }

    var el = newParcours(ParcoursType.EL, "Électronicien");
    var tn = newParcours(ParcoursType.TN, "Télécommunicant");
    parcoursRepository.saveAll(List.of(el, tn));

    var k1 = newGroupe("K1", 1);
    var k2 = newGroupe("K2", 1);
    var k3 = newGroupe("K3", 2);
    var k4 = newGroupe("K4", 3);
    groupeRepository.saveAll(List.of(k1, k2, k3, k4));

    var admin = newUser("ADM00", "Admin", "System", "admin@hei.edu", Role.ADMIN, null);
    var manitra = newUser(null, "Ramaniraka", "Manitra", "manitra@hei.edu", Role.TEACHER, null);
    var lova = newUser(null, "Andrianina", "Lova", "lova@hei.edu", Role.TEACHER, null);
    var alice =
        newUser("STD101", "Randria", "Alice", "alice@hei.edu", Role.STUDENT, ParcoursType.EL);
    var bob = newUser("STD102", "Rakoto", "Bob", "bob@hei.edu", Role.STUDENT, ParcoursType.EL);
    var charly =
        newUser("STD103", "Andria", "Charly", "charly@hei.edu", Role.STUDENT, ParcoursType.TN);
    userRepository.saveAll(List.of(admin, manitra, lova, alice, bob, charly));

    var coursByRef = createCourses(el, tn);

    inscriptionRepository.saveAll(
        List.of(
            newInscription(alice, k1, 1, 1),
            newInscription(alice, k1, 2, 1),
            newInscription(alice, k3, 3, 2),
            newInscription(alice, k1, 4, 2),
            newInscription(bob, k2, 1, 1),
            newInscription(bob, k2, 2, 1),
            newInscription(bob, k1, 3, 2),
            newInscription(charly, k2, 1, 1),
            newInscription(charly, k2, 2, 1),
            newInscription(charly, k2, 3, 2),
            newInscription(charly, k2, 4, 2)));

    createExams(coursByRef, 2024, 1);

    var year = 2024;
    affectationRepository.saveAll(
        List.of(
            newAffectation(coursByRef.get("PROK1"), k1, manitra, year),
            newAffectation(coursByRef.get("PROK1"), k2, manitra, year),
            newAffectation(coursByRef.get("WEB1"), k1, lova, year),
            newAffectation(coursByRef.get("WEB1"), k2, lova, year),
            newAffectation(coursByRef.get("MATH1"), k1, manitra, year),
            newAffectation(coursByRef.get("MATH1"), k2, manitra, year),
            newAffectation(coursByRef.get("ELEC1"), k1, lova, year)));

    log.info(
        "Seed terminé : {} cours, {} examens, {} groupes, {} affectations, {} inscriptions.",
        coursByRef.size(),
        examenRepository.count(),
        groupeRepository.count(),
        affectationRepository.count(),
        inscriptionRepository.count());
  }

  private Parcours newParcours(ParcoursType code, String nom) {
    return Parcours.builder().code(code).nom(nom).build();
  }

  private Groupe newGroupe(String ref, int annee) {
    return Groupe.builder().ref(ref).annee(annee).build();
  }

  private User newUser(
      String std, String nom, String prenom, String email, Role role, ParcoursType parcours) {
    return User.builder()
        .std(std)
        .nom(nom)
        .prenom(prenom)
        .email(email)
        .password(passwordEncoder.encode("password123"))
        .role(role)
        .parcours(parcours)
        .build();
  }

  private Inscription newInscription(User student, Groupe groupe, int semestre, int annee) {
    return Inscription.builder()
        .student(student)
        .groupe(groupe)
        .semestre(semestre)
        .annee(annee)
        .build();
  }

  private Affectation newAffectation(Cours cours, Groupe groupe, User teacher, int annee) {
    return Affectation.builder().cours(cours).groupe(groupe).teacher(teacher).annee(annee).build();
  }

  private java.util.Map<String, Cours> createCourses(Parcours el, Parcours tn) {
    var common = List.of(el, tn);
    var cours =
        coursRepository.saveAll(
            List.of(
                cours("PROK1", "Programmation 1", 6, 1, common),
                cours("WEB1", "Web 1", 6, 1, common),
                cours("CQ1", "Conduite de Projet 1", 4, 1, common),
                cours("MATH1", "Mathématiques 1", 7, 1, common),
                cours("ANG1", "Anglais 1", 3, 1, common),
                cours("SYS1", "Systèmes et Réseaux 1", 4, 1, common),
                cours("PROK2", "Programmation 2", 6, 2, common),
                cours("WEB2", "Web 2", 6, 2, common),
                cours("CQ2", "Conduite de Projet 2", 4, 2, common),
                cours("MATH2", "Mathématiques 2", 7, 2, common),
                cours("ANG2", "Anglais 2", 3, 2, common),
                cours("SYS2", "Systèmes et Réseaux 2", 4, 2, common),
                cours("PROK3", "Programmation 3", 6, 3, common),
                cours("WEB3", "Web 3", 6, 3, common),
                cours("MATH3", "Mathématiques 3", 6, 3, common),
                cours("CQ3", "Conduite de Projet 3", 4, 3, common),
                cours("PROBL3", "Problématique 3", 3, 3, common),
                cours("TN1", "Réseaux TN 1", 5, 3, List.of(tn)),
                cours("ELEC1", "Électronique EL 1", 5, 3, List.of(el)),
                cours("PROK4", "Programmation 4", 6, 4, List.of(el)),
                cours("ELEC2", "Électronique EL 2", 6, 4, List.of(el)),
                cours("TN2", "Réseaux TN 2", 6, 4, List.of(tn)),
                cours("TN3", "Télécommunications TN", 6, 4, List.of(tn)),
                cours("WEB4", "Web 4", 6, 4, common),
                cours("MATH4", "Mathématiques 4", 4, 4, common),
                cours("CQ4", "Conduite de Projet 4", 4, 4, common),
                cours("ANG4", "Anglais 4", 2, 4, common),
                cours("MOB4", "Mobile 4", 2, 4, common),
                cours("INC5", "Intégration EL", 8, 5, List.of(el)),
                cours("DIST5", "Systèmes distribués", 7, 5, List.of(el)),
                cours("ROUT5", "Routage TN", 8, 5, List.of(tn)),
                cours("ADM5", "Administration TN", 7, 5, List.of(tn)),
                cours("CQ5", "Conduite de Projet 5", 4, 5, common),
                cours("WEB5", "Web 5", 5, 5, common),
                cours("ANG5", "Anglais 5", 3, 5, common),
                cours("MAG5", "Management", 3, 5, common),
                cours("PFE6", "Projet de fin d'études", 30, 6, common)));
    var byRef = new java.util.HashMap<String, Cours>();
    cours.forEach(c -> byRef.put(c.getRef(), c));
    return byRef;
  }

  private Cours cours(
      String ref, String intitule, int credits, int semestre, List<Parcours> parcours) {
    return Cours.builder()
        .ref(ref)
        .intitule(intitule)
        .credits(credits)
        .semestre(semestre)
        .parcours(parcours)
        .build();
  }

  private void createExams(java.util.Map<String, Cours> coursByRef, int annee, int semestreDebut) {
    var examens = new java.util.ArrayList<Examen>();
    coursByRef
        .values()
        .forEach(
            c -> {
              if (c.getSemestre() >= semestreDebut && c.getSemestre() < semestreDebut + 2) {
                examens.add(examen(c, annee, 1, new BigDecimal("0.4")));
                examens.add(examen(c, annee, 2, new BigDecimal("0.6")));
              }
            });
    examenRepository.saveAll(examens);
  }

  private Examen examen(Cours cours, int annee, int numero, BigDecimal coefficient) {
    var start =
        LocalDateTime.of(annee, cours.getSemestre() <= 2 ? 1 : 9, numero == 1 ? 15 : 16, 9, 0);
    return Examen.builder()
        .cours(cours)
        .date(start.toInstant(ZoneOffset.UTC))
        .coefficient(coefficient)
        .build();
  }
}
