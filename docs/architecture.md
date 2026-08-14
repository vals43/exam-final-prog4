# Architecture technique

## 1. Stack

| Composant | Technologie |
|-----------|-------------|
| Backend | Spring Boot 3.2.2 (Java 21) |
| Base de données | PostgreSQL (à ajouter — JPA/Hibernate) |
| Front | Thymeleaf + Spring MVC (à ajouter) |
| Sécurité | Spring Security + JWT (à ajouter) |
| Stockage fichiers | AWS S3 (`BucketComponent`, déjà présent) |
| Emails asynchrones | AWS SQS + SES (`SendEmailRequested`/`Mailer`, déja présents) |
| PDF relevés | OpenPDF / pdfbox (à ajouter) |
| XLSX diplômés | Apache POI (à ajouter) |

Le squelette est un template **Poja** : endpoints REST dans `endpoint/rest`, événements asynchrones dans `endpoint/event`, services dans `service`, stockage S3 dans `file/bucket`, email dans `mail`.

## 2. Organisation des couches

```
src/main/java/api/poja/app/
├── endpoint/rest        → Controllers Spring (Spring Security + Thymeleaf views)
├── endpoint/event       → Événements async SQS (SendEmailRequested) + consumers
├── service              → Logique métier (notes, moyennes, diplômés)
├── service.event        → Consumers SQS (ex. SendEmailRequestedService)
├── repository           → JPA repositories (à créer)
├── model                → Entités JPA (à créer)
├── file/bucket          → BucketComponent (upload S3, déjà présent)
├── mail                 → Mailer (envoi email via SES, déjà présent)
└── concurrency          → Workers (traitement en parallèle)
```

La génération de code se fait aussi via **OpenAPI** (`api.yml`), le template générant les endpoints dans `endpoint/rest`.

## 3. Flux — Relevé de notes (PDF par email, asynchrone)

```mermaid
sequenceDiagram
    participant Admin/Teacher
    participant Controller
    participant Service
    participant S3
    participant SQS
    participant Worker as SendEmailRequestedService
    participant SES

    Admin/Teacher->>Controller: Demande génération relevé (année, étudiant)
    Controller->>Service: Request créer relevé PDF
    Service->>Service: Construit PDF (notes, moyenne, crédits)
    Service->>S3: Upload PDF (bucket relevés, clef par étudiant/année)
    Service->>SQS: Publish SendEmailRequested(to=student)
    SQS->>Worker: Consume event (asynchrone, retries)
    Worker->>SES: Envoie email avec lien/pièce jointe
    SES-->>Student: Email reçu
```

## 4. Flux — Liste des diplômés (XLSX, téléchargement direct)

```mermaid
sequenceDiagram
    participant User
    participant UI as Thymeleaf
    participant Controller
    participant Service
    participant S3

    User->>UI: Liste des promotions + bouton "Télécharger les diplômés"
    User->>Controller: GET /promotions/{promo}/diplomes
    Controller->>Service: Calcul diplômés (10/20 partout, rang)
    Service->>Service: Génère XLSX (rang, STD, nom, prénom, moyenne)
    Service->>S3: Upload XLSX (fichier temporaire)
    Controller-->>User: Redirect vers URL S3 (téléchargement direct)
```

## 5. Points d'attention

- **Notes historisées** : toute modification crée une entrée d'historique (pas de mise à jour destructive).
- **Parcours EL/TN** : les requêtes de bulletin filtrent strictement les cours du parcours de l'étudiant.
- **Groupes non fixes** : les notes sont rattachées à l'étudiant via inscription groupe/semestre, jamais perdues.
- **Coefficients** : somme des coefficients des examens d'une matière = 1 (représentation décimale ou fractionnaire).
- **Async** : les envois d'emails passent par SQS (pas de blocage du thread HTTP).
- **Sécurité** : chaque endpoint vérifie le rôle (Student/Teacher/Admin).