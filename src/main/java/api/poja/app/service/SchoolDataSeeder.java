package api.poja.app.service;

import api.poja.app.model.Affectation;
import api.poja.app.model.Cours;
import api.poja.app.model.Examen;
import api.poja.app.model.Groupe;
import api.poja.app.model.Inscription;
import api.poja.app.model.Note;
import api.poja.app.model.Parcours;
import api.poja.app.model.ParcoursType;
import api.poja.app.model.Role;
import api.poja.app.model.User;
import api.poja.app.repository.AffectationRepository;
import api.poja.app.repository.CoursRepository;
import api.poja.app.repository.ExamenRepository;
import api.poja.app.repository.GroupeRepository;
import api.poja.app.repository.InscriptionRepository;
import api.poja.app.repository.NoteRepository;
import api.poja.app.repository.ParcoursRepository;
import api.poja.app.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private static final Map<String, List<BigDecimal>> REAL_NOTES_STD23107 =
      Map.of(
          "SYS1", List.of(new BigDecimal("12.50"), new BigDecimal("12.50")),
          "PROG1", List.of(new BigDecimal("10.17"), new BigDecimal("10.17")),
          "MGT1", List.of(new BigDecimal("17.50"), new BigDecimal("17.50")),
          "DONNEES1", List.of(new BigDecimal("11.63"), new BigDecimal("11.63")),
          "WEB1", List.of(new BigDecimal("14.33"), new BigDecimal("14.33")),
          "SYS2",
              List.of(new BigDecimal("3.33"), new BigDecimal("11.67"), new BigDecimal("15.00")));

  private static final Map<String, List<BigDecimal>> REAL_NOTES_STD23108 =
      Map.of(
          "PROG1", List.of(new BigDecimal("11.83"), new BigDecimal("11.83")),
          "DONNEES1", List.of(new BigDecimal("13.63"), new BigDecimal("13.63")),
          "THEORIE1", List.of(new BigDecimal("18.50"), new BigDecimal("18.50")),
          "WEB1", List.of(new BigDecimal("15.50"), new BigDecimal("15.50")));

  private static final Map<String, List<BigDecimal>> REAL_NOTES_STD23111 =
      Map.of(
          "PROG1", List.of(new BigDecimal("15.33"), new BigDecimal("15.33")),
          "DONNEES1", List.of(new BigDecimal("17.75"), new BigDecimal("17.75")),
          "WEB1", List.of(new BigDecimal("17.83"), new BigDecimal("17.83")));

  private static final Map<String, List<BigDecimal>> REAL_NOTES_STD23112 =
      Map.of(
          "PROG1", List.of(new BigDecimal("7.33"), new BigDecimal("7.33")),
          "DONNEES1", List.of(new BigDecimal("9.25"), new BigDecimal("9.25")),
          "WEB1", List.of(new BigDecimal("9.33"), new BigDecimal("9.33")));

  private static final Map<String, List<BigDecimal>> REAL_NOTES_STD23113 =
      Map.of("PROG1", List.of(new BigDecimal("6.33"), new BigDecimal("6.33")));

  private static final Map<String, Map<String, List<BigDecimal>>> REAL_NOTES =
      Map.of(
          "STD23107", REAL_NOTES_STD23107,
          "STD23108", REAL_NOTES_STD23108,
          "STD23111", REAL_NOTES_STD23111,
          "STD23112", REAL_NOTES_STD23112,
          "STD23113", REAL_NOTES_STD23113);

  private static final List<String> REUSSITE_STDS =
      List.of("STD21001", "STD22001", "STD23107", "STD23108", "STD23111");
  private static final List<String> ECHEC_STDS =
      List.of("STD21004", "STD22004", "STD23112", "STD23113");

  private final ParcoursRepository parcoursRepository;
  private final CoursRepository coursRepository;
  private final GroupeRepository groupeRepository;
  private final UserRepository userRepository;
  private final ExamenRepository examenRepository;
  private final AffectationRepository affectationRepository;
  private final InscriptionRepository inscriptionRepository;
  private final NoteRepository noteRepository;
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

    var g1 = newGroupe("G1");
    var g2 = newGroupe("G2");
    var g3 = newGroupe("G3");
    var h1 = newGroupe("H1");
    var h2 = newGroupe("H2");
    var h3 = newGroupe("H3");
    var j1 = newGroupe("J1");
    var j2 = newGroupe("J2");
    var j3 = newGroupe("J3");
    groupeRepository.saveAll(List.of(g1, g2, g3, h1, h2, h3, j1, j2, j3));

    var admin = newUser("ADM00", "Admin", "System", "admin@hei.edu", Role.ADMIN, null, null);
    var manitra =
        newUser(null, "Ramaniraka", "Manitra", "manitra@hei.edu", Role.TEACHER, null, null);
    var lova = newUser(null, "Andrianina", "Lova", "lova@hei.edu", Role.TEACHER, null, null);

    var fanjasoa =
        newUser(
            "STD21001",
            "ANDRIAMANJAKA",
            "Fanjasoa",
            "fanjasoa@hei.edu",
            Role.STUDENT,
            ParcoursType.EL,
            2021);
    var mihary =
        newUser(
            "STD21004",
            "ANDRIAMILANTO",
            "Mihary Joël",
            "mihary@hei.edu",
            Role.STUDENT,
            ParcoursType.TN,
            2021);
    var loiqua =
        newUser(
            "STD22001",
            "ANDRIAMAHALY ARINIAINA",
            "Barthélemy Loiqua",
            "loiqua@hei.edu",
            Role.STUDENT,
            ParcoursType.EL,
            2022);
    var joachim =
        newUser(
            "STD22004",
            "ANDRIAMASINORO",
            "Jean Joachim",
            "joachim@hei.edu",
            Role.STUDENT,
            ParcoursType.TN,
            2022);
    var ninah =
        newUser(
            "STD23107",
            "HANTANIRINA",
            "Ninah",
            "ninah@hei.edu",
            Role.STUDENT,
            ParcoursType.EL,
            2023);
    var fanhasina =
        newUser(
            "STD23108",
            "RAKOTOARISOA",
            "Ny Herimanankasina Fanhasina",
            "fanhasina@hei.edu",
            Role.STUDENT,
            ParcoursType.TN,
            2023);
    var nicolas =
        newUser(
            "STD23111",
            "RANDRIANARIVONY",
            "Aro Nicolas",
            "nicolas@hei.edu",
            Role.STUDENT,
            ParcoursType.EL,
            2023);
    var jessica =
        newUser(
            "STD23112",
            "RANDRIAMANDIMBY",
            "Hasiniaina Jessica",
            "jessica@hei.edu",
            Role.STUDENT,
            ParcoursType.TN,
            2023);
    var christophe =
        newUser(
            "STD23113",
            "MERCI LEONARDO",
            "Christophe Muriel's",
            "christophe@hei.edu",
            Role.STUDENT,
            ParcoursType.EL,
            2023);
    var students =
        List.of(fanjasoa, mihary, loiqua, joachim, ninah, fanhasina, nicolas, jessica, christophe);
    userRepository.saveAll(
        List.of(
            admin,
            manitra,
            lova,
            fanjasoa,
            mihary,
            loiqua,
            joachim,
            ninah,
            fanhasina,
            nicolas,
            jessica,
            christophe));

    var coursByRef = createCourses(el, tn);

    var inscriptions = new ArrayList<Inscription>();
    for (User student : students) {
      var groups = groupsFor(student, g1, g2, g3, h1, h2, h3, j1, j2, j3);
      inscriptions.addAll(
          List.of(
              newInscription(student, groups.get(0), 1, 1),
              newInscription(student, groups.get(1), 2, 1),
              newInscription(student, groups.get(2), 3, 2),
              newInscription(student, groups.get(3), 4, 2),
              newInscription(student, groups.get(4), 5, 3),
              newInscription(student, groups.get(5), 6, 3)));
    }
    inscriptionRepository.saveAll(inscriptions);

    createExams(coursByRef, 2024, 1);
    createExams(coursByRef, 2025, 3);
    createExams(coursByRef, 2026, 5);

    var groupsByPromo = List.of(List.of(g1, g2, g3), List.of(h1, h2, h3), List.of(j1, j2, j3));
    var affectations = new ArrayList<Affectation>();
    int index = 0;
    for (Cours cours : coursByRef.values()) {
      int annee = (cours.getSemestre() + 1) / 2;
      boolean elOnly = isOnly(cours, ParcoursType.EL);
      boolean tnOnly = isOnly(cours, ParcoursType.TN);
      for (List<Groupe> promoGroups : groupsByPromo) {
        List<Groupe> groupes = promoGroups;
        if (elOnly) {
          groupes = List.of(promoGroups.get(0), promoGroups.get(1));
        } else if (tnOnly) {
          groupes = List.of(promoGroups.get(2));
        }
        for (Groupe groupe : groupes) {
          User teacher = index % 2 == 0 ? manitra : lova;
          affectations.add(newAffectation(cours, groupe, teacher, annee));
          index++;
        }
      }
    }
    affectationRepository.saveAll(affectations);

    seedNotes(students, inscriptions, coursByRef);

    log.info(
        "Seed terminé : {} cours, {} examens, {} groupes, {} affectations, {} inscriptions, {}"
            + " notes.",
        coursByRef.size(),
        examenRepository.count(),
        groupeRepository.count(),
        affectationRepository.count(),
        inscriptionRepository.count(),
        noteRepository.count());
  }

  private void seedNotes(
      List<User> students, List<Inscription> inscriptions, Map<String, Cours> coursByRef) {
    var notes = new ArrayList<Note>();
    for (User student : students) {
      var studentInscriptions =
          inscriptions.stream()
              .filter(i -> i.getStudent().getId().equals(student.getId()))
              .toList();
      for (Cours cours : coursByRef.values()) {
        var examens = examensOrdonnes(cours);
        for (int i = 0; i < examens.size(); i++) {
          var examen = examens.get(i);
          var semestre = cours.getSemestre();
          if (studentInscriptions.stream().noneMatch(ins -> ins.getSemestre().equals(semestre))) {
            continue;
          }
          boolean appartientAuParcours =
              cours.getParcours() != null
                  && cours.getParcours().stream()
                      .anyMatch(p -> p.getCode() == student.getParcours());
          if (!appartientAuParcours) {
            continue;
          }
          Inscription inscription =
              studentInscriptions.stream()
                  .filter(ins -> ins.getSemestre().equals(semestre))
                  .findFirst()
                  .orElseThrow();
          var valeur = noteValue(student, cours, i);
          notes.add(
              Note.builder()
                  .student(student)
                  .examen(examen)
                  .inscription(inscription)
                  .valeur(valeur)
                  .version(1)
                  .dateCreation(Instant.now())
                  .build());
        }
      }
    }
    noteRepository.saveAll(notes);
  }

  private List<Examen> examensOrdonnes(Cours cours) {
    return examenRepository.findByCoursId(cours.getId()).stream()
        .sorted(Comparator.comparing(Examen::getDate))
        .toList();
  }

  private BigDecimal noteValue(User student, Cours cours, int examenIndex) {
    var realNotes = REAL_NOTES.get(student.getStd());
    if (realNotes != null && realNotes.containsKey(cours.getRef())) {
      var valeurs = realNotes.get(cours.getRef());
      if (examenIndex >= 0 && examenIndex < valeurs.size()) {
        return valeurs.get(examenIndex);
      }
    }
    if (REUSSITE_STDS.contains(student.getStd())) {
      var seed = student.getStd().hashCode() * 31 + cours.getRef().hashCode() * 7;
      var hash = Math.floorMod(seed, 100);
      // réussite garantie : note entre 11 et 19
      return BigDecimal.valueOf(11 + hash % 9).setScale(2);
    }
    if (ECHEC_STDS.contains(student.getStd()) && cours.getRef().equals("PROG1")) {
      // échec garanti sur au moins un cours pour rendre la liste des diplômés significative
      return new BigDecimal("5.00");
    }
    var seed = student.getId().hashCode() * 31 + cours.getRef().hashCode() * 7;
    var hash = Math.floorMod(seed, 100);
    // valeurs réalistes entre 5 et 19, avec une petite probabilité d'échec (< 10)
    var base = 5 + hash % 15;
    if (hash % 7 == 0) {
      base = 4 + hash % 5; // quelques échecs pour rendre les diplômés significatifs
    }
    return BigDecimal.valueOf(Math.min(19, base)).setScale(2);
  }

  private Parcours newParcours(ParcoursType code, String nom) {
    return Parcours.builder().code(code).nom(nom).build();
  }

  private Groupe newGroupe(String ref) {
    return Groupe.builder().ref(ref).build();
  }

  private List<Groupe> groupsFor(
      User student,
      Groupe g1,
      Groupe g2,
      Groupe g3,
      Groupe h1,
      Groupe h2,
      Groupe h3,
      Groupe j1,
      Groupe j2,
      Groupe j3) {
    return switch (student.getStd()) {
      case "STD21001" -> List.of(g1, g1, g1, g1, g2, g2);
      case "STD21004" -> List.of(g2, g2, g2, g3, g3, g3);
      case "STD22001" -> List.of(h2, h2, h2, h1, h1, h1);
      case "STD22004" -> List.of(h1, h1, h1, h3, h3, h3);
      case "STD23107" -> List.of(j1, j1, j1, j1, j2, j2);
      case "STD23108" -> List.of(j1, j1, j1, j3, j3, j3);
      case "STD23111" -> List.of(j2, j2, j2, j2, j1, j1);
      case "STD23112" -> List.of(j2, j2, j2, j3, j3, j3);
      case "STD23113" -> List.of(j3, j3, j3, j1, j1, j1);
      default -> throw new IllegalStateException("Étudiant inconnu : " + student.getStd());
    };
  }

  private boolean isOnly(Cours cours, ParcoursType parcours) {
    return cours.getParcours() != null
        && cours.getParcours().size() == 1
        && cours.getParcours().get(0).getCode() == parcours;
  }

  private User newUser(
      String std,
      String nom,
      String prenom,
      String email,
      Role role,
      ParcoursType parcours,
      Integer promotion) {
    return User.builder()
        .std(std)
        .nom(nom)
        .prenom(prenom)
        .email(email)
        .password(passwordEncoder.encode("password123"))
        .role(role)
        .parcours(parcours)
        .promotion(promotion)
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

  private Map<String, Cours> createCourses(Parcours el, Parcours tn) {
    var cours = coursRepository.saveAll(definitions(el, tn));
    var byRef = new HashMap<String, Cours>();
    cours.forEach(c -> byRef.put(c.getRef(), c));
    return byRef;
  }

  public static List<Cours> definitions(Parcours el, Parcours tn) {
    var common = List.of(el, tn);
    return List.of(
        cours("PROG1", "Programmation 1", 6, 1, common),
        cours("WEB1", "Web 1", 6, 1, common),
        cours("THEORIE1", "Théorie 1", 6, 1, common),
        cours("SYS1", "Systèmes et Réseaux 1", 6, 1, common),
        cours("MGT1", "Management 1", 4, 1, common),
        cours("LV1", "Langues Vivantes 1", 2, 1, common),
        cours("PROG2", "Programmation 2", 6, 2, common),
        cours("DONNEES1", "Données 1", 4, 2, common),
        cours("SYS2", "Systèmes et Réseaux 2", 8, 2, common),
        cours("WEB2", "Web 2", 12, 2, common),
        cours("WEB3", "Web 3", 9, 3, common),
        cours("PROG3", "Programmation 3", 6, 3, common),
        cours("MGT2", "Management 2", 5, 3, common),
        cours("DONNEES2", "Données 2", 5, 3, common),
        cours("LV2", "Langues Vivantes 2", 5, 3, common),
        cours("PROJET1", "Projet 1", 18, 4, common),
        cours("IA1", "Intelligence Artificielle 1", 3, 4, common),
        cours("PROG4", "Programmation 4", 6, 4, List.of(el)),
        cours("SYS3", "Systèmes et Réseaux 3", 3, 4, List.of(el)),
        cours("TN1", "Télécommunications 1", 6, 4, List.of(tn)),
        cours("METIER1", "Métier 1", 3, 4, List.of(tn)),
        cours("SECU1", "Sécurité 1", 6, 5, common),
        cours("SECU2", "Sécurité 2", 4, 5, common),
        cours("SECU3", "Sécurité 3", 6, 5, common),
        cours("PRO3", "Projet 3", 3, 5, common),
        cours("PROG5", "Programmation 5", 7, 5, List.of(el)),
        cours("SYS4", "Systèmes et Réseaux 4", 4, 5, List.of(el)),
        cours("TN2", "Télécommunications 2", 7, 5, List.of(tn)),
        cours("METIER2", "Métier 2", 4, 5, List.of(tn)),
        cours("PROJET4", "Projet 4 (Stage)", 30, 6, common));
  }

  private static Cours cours(
      String ref, String intitule, int credits, int semestre, List<Parcours> parcours) {
    return Cours.builder()
        .ref(ref)
        .intitule(intitule)
        .credits(credits)
        .semestre(semestre)
        .parcours(parcours)
        .build();
  }

  private void createExams(Map<String, Cours> coursByRef, int annee, int semestreDebut) {
    var examens = new ArrayList<Examen>();
    coursByRef
        .values()
        .forEach(
            c -> {
              if (c.getSemestre() >= semestreDebut && c.getSemestre() < semestreDebut + 2) {
                if (c.getRef().equals("SYS2")) {
                  examens.add(examen(c, annee, 1, new BigDecimal("0.3")));
                  examens.add(examen(c, annee, 2, new BigDecimal("0.3")));
                  examens.add(examen(c, annee, 3, new BigDecimal("0.4")));
                } else {
                  examens.add(examen(c, annee, 1, new BigDecimal("0.4")));
                  examens.add(examen(c, annee, 2, new BigDecimal("0.6")));
                }
              }
            });
    examenRepository.saveAll(examens);
  }

  private Examen examen(Cours cours, int annee, int numero, BigDecimal coefficient) {
    var start =
        LocalDateTime.of(
            annee,
            cours.getSemestre() <= 2 ? 1 : 9,
            numero == 1 ? 15 : numero == 2 ? 16 : 17,
            9,
            0);
    return Examen.builder()
        .cours(cours)
        .date(start.toInstant(ZoneOffset.UTC))
        .coefficient(coefficient)
        .build();
  }
}
