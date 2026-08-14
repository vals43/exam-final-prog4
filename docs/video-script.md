# Script vidéo — 3 minutes maximum

## Objectif

Montrer, en 3 min, que l'application répond aux attentes du sujet : gestion des notes sur 3 ans, relevés PDF par email, liste des diplômés, interface Thymeleaf.

## Contenu

### 0:00 — Intro (15 s)
> « Bonjour, je m'appelle [prénom], avec [coéquipier]. Voici notre application de gestion des notes pour HEI sur le parcours de trois ans. »

### 0:15 — Modèle & règles métier (30 s)
> « Nous avons modélisé les cours avec crédits (30 par semestre, 60 par an), les examens dont la somme des coefficients vaut 1, et deux parcours EL et TN. Les groupes ne sont pas fixes : en changeant de groupe, l'étudiant garde toutes ses notes. »

- **Écran** : schéma BDD ou seed des cours/examens.

### 0:45 — Rôles & sécurité (30 s)
> « Spring Security gère trois rôles : l'étudiant voit ses notes, le professeur ne note que sa matière, et l'admin peut tout créer ou modifier. Chaque modification d'une note est historisée. »

- **Écran** : démo login teacher → saisie d'une note puis relecture de l'historique.

### 1:15 — Relevé de notes PDF (40 s)
> « Pour chaque étudiant, nous générons un relevé de notes en PDF — provisoire ou complet avec la moyenne générale et les crédits — envoyé par email de façon asynchrone via S3 et le mailer Poja. »

- **Écran** : clic sur "Envoyer le relevé" → file d'attente SQS → email reçu (aperçu PDF à l'écran).

### 1:55 — Liste des diplômés (40 s)
> « Enfin, la liste des diplômés : pour chaque promotion, un fichier Excel téléchargeable contenant le rang, le STD, le nom, le prénom et la moyenne générale, trié par rang. Seuls les étudiants ayant 10/20 partout y figurent. »

- **Écran** : interface Thymeleaf (liste des promotions) → bouton « Télécharger la liste des diplômés » → fichier XLSX ouvert.

### 2:35 — Conclusion (25 s)
> « Tout est déployé : le lien de préproduction et le code sont dans la description. Merci de votre attention ! »

## Consignes de tournage

- Durée totale : **≤ 3:00** — chronomètrez le script à voix haute.
- Démo avec données réalistes (étudiants EL et TN, changements de groupe visibles).
- Privilégier des plans courts avec du texte à l'écran pour la lisibilité.
- Terminer par l'affichage de l'URL de préproduction et du repo GitHub.