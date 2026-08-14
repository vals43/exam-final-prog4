# Sujet — Examen Final PROG4/SYS3

Gestion des notes sur le parcours de trois ans à HEI.

## 1. Contexte

Résoudre un problème de gestion scolaire pour HEI :

- assigner des professeurs aux cours, chaque année, pour des groupes (EL, TN) ;
- publier les notes de chaque année ainsi que les notes finales sur les trois ans ;
- calculer la moyenne générale sur les trois ans ;
- lister les diplômés de l'année.

**Définition d'un diplôme** : obtenir au moins 10/20 à tous les cours de son parcours.

## 2. Entités principales

### Cours

- Référence HEI (ex. : PROK1, WEB1, WEB2, WEB3, PROK4, CQ1). Les intitulés exacts ne sont pas obligatoires, ils doivent rester réalistes.
- Intitulé.
- Nombre de crédits.
- Attributs additionnels possibles.

**Règles de gestion** :
- 30 crédits par semestre ;
- 60 crédits par année (2 semestres).

### Groupes

- ID.
- Référence (ex. : K1, K2, K3, K4).

### Examens

- Date et heure.
- Coefficient.

**Règle de gestion** : pour une matière donnée, la somme des coefficients de tous ses examens doit être égale à 1 (ex. : 0,5 + 0,2 + 0,3). Les coefficients peuvent être exprimés en fractions (un quart, un tiers, trois huitièmes…).

## 3. Utilisateurs et sécurité (Spring Security)

| Rôle | Droits |
|------|--------|
| Student | Consulter ses propres notes. |
| Teacher | Noter, uniquement sa propre matière (un cours peut être enseigné par plusieurs enseignants). |
| Admin | Tout faire, y compris modifier ou créer des étudiants. |

## 4. Règles métier (réalités complexes de HEI)

1. **Deux parcours : EL et TN.**
   Certains cours sont propres à un parcours (ex. : les EL ne font pas TN1/TN2, mais font PROK4). Les notes d'un parcours ne doivent jamais apparaître sur le bulletin de l'autre.

2. **Les groupes ne sont pas fixes.**
   Un étudiant peut changer de groupe en cours de parcours. Les notes obtenues dans chaque groupe doivent être conservées (aucune note perdue lors d'un changement de groupe).

3. **Les notes peuvent changer.**
   Les réclamations sont possibles (erreur de transcription…). Les notes doivent donc être **historisées**.

## 5. Livrables attendus

### Relevés de notes

- Générés en **PDF**.
- Envoyés par **email** (S3 + mailer POJA), idéalement en asynchrone.
- Doivent être marqués comme **provisoires** ou **complets** :
  - provisoire : relevé partiel ;
  - complet : relevé annuel avec moyenne générale de l'année et nombre de crédits.

### Liste des diplômés

- Format **XLSX** (fichier Excel) stocké sur **S3**, téléchargeable directement depuis l'application (sans email).
- Contenu, trié par rang :
  - rang (devant) ;
  - STD ;
  - nom ;
  - prénom ;
  - moyenne générale.
- Détermination : pour chaque promotion, parmi tous les cours suivis (même après changements de groupe), l'étudiant a obtenu 10 ou plus partout.

### Interface web

- Front avec **Thymeleaf**.
- Affichage de la liste des promotions.
- Bouton **« Télécharger la liste des diplômés »** sur chaque promotion.

## 6. Modalités de rendu

- Deadline : **au plus tard le 20, avant minuit**.
- Envoyer :
  - un formulaire rempli (lien préproduction + repo GitHub) ;
  - une vidéo de **3 minutes maximum** exposant les réalisations et leur adéquation aux attentes.
