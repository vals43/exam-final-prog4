# HEI — Gestion des notes (exam-final-prog4)

Application de gestion des notes sur le parcours de trois ans à HEI (PROG4/SYS3).
Spring Boot 3.2.2 + PostgreSQL + JWT, déployée sur AWS via le template [Poja](https://poja.io).

## Fonctionnalités

- **3 rôles (Spring Security + JWT stateless)** :
  - `STUDENT` : consulte ses propres notes (`/student/notes`).
  - `TEACHER` : note uniquement ses matières affectées (`/teacher/notes`), avec historisation (`NoteHistory`).
  - `ADMIN` : CRUD utilisateurs, cours, affectations, examens, relevés et diplômés.
- **Deux parcours** : EL et TN — les notes d'un parcours n'apparaissent jamais sur le bulletin de l'autre.
- **Groupes non fixes** : un étudiant peut changer de groupe, ses notes sont conservées via les inscriptions.
- **Relevés de notes PDF** : provisoire ou complet (moyenne générale + crédits), upload S3 puis envoi par **email asynchrone** (SQS + SES).
- **Liste des diplômés XLSX** : triée par rang, téléchargeable directement (S3 pre-signed), pour chaque promotion.
- **Interface Thymeleaf** : liste des promotions + bouton « Télécharger la liste des diplômés ».

## Déploiement (preprod)

```
https://ioc5c5l5twlvmdapgdfduhzrvq0ddbau.lambda-url.eu-west-3.on.aws
```

## Démarrage local

```bash
export JAVA_HOME=$HOME/.jdks/ms-21.0.11   # JDK 21
export PATH=$JAVA_HOME/bin:$PATH
export JWT_SECRET=<secret>
export SPRING_DATASOURCE_URL=<postgres-url>
./gradlew test
./gradlew bootRun
```

## Comptes de démonstration (seed)

| Email | Rôle | Mot de passe |
|-------|------|--------------|
| admin@hei.edu | ADMIN | password123 |
| manitra@hei.edu | TEACHER | password123 |
| lova@hei.edu | TEACHER | password123 |
| fanjasoa@hei.edu | STUDENT (EL) — promo 2021 (G) | password123 |
| mihary@hei.edu | STUDENT (TN) — promo 2021 (G) | password123 |
| loiqua@hei.edu | STUDENT (EL) — promo 2022 (H) | password123 |
| joachim@hei.edu | STUDENT (TN) — promo 2022 (H) | password123 |
| ninah@hei.edu | STUDENT (EL) — promo 2023 (J) | password123 |
| fanhasina@hei.edu | STUDENT (TN) — promo 2023 (J) | password123 |
| nicolas@hei.edu | STUDENT (EL) — promo 2023 (J) | password123 |
| jessica@hei.edu | STUDENT (TN) — promo 2023 (J) | password123 |
| christophe@hei.edu | STUDENT (EL) — promo 2023 (J) | password123 |

Le seed charge 3 cohortes réelles HEI (promotions G/H/J = 2021/2022/2023) avec 3 ans de cours/examens (référentiels réels L1-L3, 60 crédits/an), des affectations par cours × groupe × année, des inscriptions avec changement de groupe (K1/K2 → K3 → K4) et des notes réalistes. Parmi les étudiants, 5 sont diplômés (Fanjasoa, Loiqua, Ninah, Fanhasina, Nicolas) et 4 échouent (Mihary, Joachim, Jessica, Christophe).

## API principales

| Méthode | Endpoint | Rôle |
|---------|----------|------|
| POST | `/login` | public |
| GET | `/student/notes` | STUDENT |
| GET | `/student/releves/{annee}?mode=PROVISOIRE\|COMPLET` | STUDENT |
| GET | `/teacher/notes` | TEACHER |
| POST | `/teacher/notes` | TEACHER |
| GET | `/teacher/affectations` | TEACHER |
| GET | `/admin/users?role=` | ADMIN |
| POST | `/admin/users` | ADMIN |
| GET | `/admin/cours` | ADMIN |
| POST | `/admin/affectations` | ADMIN |
| GET | `/admin/examens?coursId=` | ADMIN |
| POST | `/admin/examens` (somme des coefficients = 1) | ADMIN |
| POST | `/admin/releves/{studentId}/{annee}?mode=PROVISOIRE\|COMPLET` | ADMIN |
| GET | `/promotions` | ADMIN, TEACHER |
| GET | `/promotions/{annee}/diplomes` | ADMIN |

## Règles métier

- Diplôme = au moins 10/20 à tous les cours du parcours (sur les 3 ans).
- Somme des coefficients des examens d'un cours = 1.
- Toute modification de note crée une entrée `NoteHistory` (réclamations tracées).
- Note finale d'un cours = `Σ (coefficient × valeur)` des examens.

## Structure

```
src/main/java/api/poja/app/
├── config          → SecurityConfig (JWT stateless)
├── security        → JwtService, JwtAuthenticationFilter
├── endpoint/rest   → Controllers + DTOs
├── service         → Logique métier (notes, relevés, diplômés, calculs)
├── service/export  → Génération PDF (OpenPDF) et XLSX (Apache POI)
├── repository      → JPA repositories
├── model           → Entités JPA
├── mail            → Mailer (SES)
├── file/bucket     → BucketComponent (S3)
└── endpoint/event  → Événements async (SendEmailRequested)
```

## Tests

```bash
./gradlew test
```

Intégration avec Testcontainers (PostgreSQL) + couverture JaCoCo (≥ 80 %).

## Documentation

Spécification OpenAPI : `doc/api.yml`.
