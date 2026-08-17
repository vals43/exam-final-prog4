# Plan d'implémentation — Équipe de 2 personnes

## 0. Démarrage commun (en pair)

- Cloner le repo sur la branche `preprod`, créer la branche de travail.
- Ajouter les dépendances : JPA + PostgreSQL, Spring Security, Thymeleaf, OpenPDF (PDF), Apache POI (XLSX).
- Définir le schéma de la base + seed réaliste (cours, groupes K1–K4, examens avec coefficients, étudiants EL/TN, changements de groupe).
- **Livrable** : app qui démarre, seed chargé, 60 crédits/an validés.

## 1. Répartition des rôles (2 personnes)

| Tâche | Responsable | Dépend de |
|-------|-------------|-----------|
| Modèle JPA + repositories + seed | **A & B** (pair) | Phase 0 |
| Spring Security (3 rôles, JWT) | **A** | Modèle |
| CRUD admin (créer/modifier étudiants, profs, cours, affectations) | **A** | Security |
| Student : voir ses notes / relevé | **B** | Security |
| Teacher : noter sa matière + historique des notes | **B** | Security |
| Calcul moyennes (année / 3 ans), crédits, rangs | **B** | Modèle |
| Relevé de notes PDF → S3 → email async (SQS) | **A** | Security, moyennes |
| Liste des diplômés XLSX → S3 → téléchargement | **B** | moyennes |
| Interface Thymeleaf (promotions + bouton diplômés) | **A** | endpoints REST |
| Intégration end-to-end + tests | **A & B** | toutes |
| Vidéo 3 min + formulaire/PR | **A & B** (pair) | fin |

## 2. Itérations

### Itération 1 — Fondations (A & B)
- [ ] Entités JPA (`User`, `Cours`, `Groupe`, `Parcours`, `Examen`, `Affectation`, `Inscription`, `Note`, `NoteHistory`)
- [ ] Repositories + migration/schema
- [ ] Seed : 2 parcours, cours avec crédits (30/sem, 60/an), 2 groupes, examens (somme coeff = 1)
- [ ] Spring Security : `STUDENT`/`TEACHER`/`ADMIN`, endpoints protégés

### Itération 2 — Domaine (A & B)
- **A** : CRUD admin (étudiants, profs, cours, affectation profs→cours/groupe/année)
- **B** : Teacher note ses matières uniquement ; création `NoteHistory` à chaque changement ; Student consulte ses notes

### Itération 3 — Calculs (B)
- [ ] Note finale par cours (pondération coefficients)
- [ ] Moyenne générale annuelle + crédits (relevé provisoire/complet)
- [ ] Détection diplômés (10/20 partout sur les 3 ans) + rang

### Itération 4 — Livrables (A & B)
- **A** : génération PDF relevé → upload S3 → publish `SendEmailRequested` (email async, smart retry)
- **B** : génération XLSX liste diplômés (rang, STD, nom, prénom, moyenne) → S3 → endpoint de téléchargement

### Itération 5 — UI & finition (A & B)
- **A** : pages Thymeleaf — liste des promotions + bouton « Télécharger la liste des diplômés »
- **B** : validations (parcours EL/TN filtrés sur les bulletins, notes jamais perdues)
- **A & B** : tests (repositories, services, security), README, deploy preprod, enquête Moustache server (ASB), note de test distincte, démo film.

## 3. Critères de validation métier

1. Un étudiant TN ne voit jamais de notes EL sur son bulletin.
2. Un étudiant ayant changé de groupe garde toutes ses notes (aucun trou).
3. Toute modification de note est tracée (`NoteHistory`).
4. Somme des coefficients par cours = 1.
5. 30 crédits/semestre, 60/an sur le seed.
6. Le relevé annuel affiche moyenne générale + crédits et son statut (provisoire/complet).
7. La liste des diplômés est triée par rang, unique par promotion.
8. Seuls les 10/20 partout figurent dans la liste.

## 4. Estimation & ordre de priorité

1. Modèle + seed + security — **bloquant** (tout dépend)
2. Règles métier (notes, moyennes, historisation) — **bloquant**
3. Relevé PDF + email async — livrable exigé
4. XLSX diplômés + téléchargement — livrable exigé
5. UI Thymeleaf — livrable exigé
6. Vidéo + formulaire — rendu final (deadline : 20 avant minuit)