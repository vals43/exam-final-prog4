# Guide de test — Connexion des 3 rôles (Postman)

**Base URL** : `https://ioc5c5l5twlvmdapgdfduhzrvq0ddbau.lambda-url.eu-west-3.on.aws`

## Setup
1. Crée un environnement Postman `HEI` avec la variable `base_url` = Base URL ci-dessus.
2. Crée une requête **Login** avec :
   - **POST** `{{base_url}}/login`
   - **Headers** : `Content-Type: application/json`
   - **Body** (raw, JSON) :
     ```json
     { "email": "...", "password": "password123" }
     ```

## Les 3 logins
| Nom | Body (email) | Test à coller |
|-----|--------------|---------------|
| Login ADMIN | `admin@hei.edu` | `pm.environment.set("admin_token", pm.response.json().token);` |
| Login TEACHER | `manitra@hei.edu` | `pm.environment.set("teacher_token", pm.response.json().token);` |
| Login STUDENT | `fanjasoa@hei.edu` | `pm.environment.set("student_token", pm.response.json().token);` |

Réponse attendue (200) : `{ "token": "...", "email": "...", "role": "..." }`

## Vérification
Vite fait 3 requêtes :
- **GET** `{{base_url}}/admin/whoami` → Bearer `{{admin_token}}` → 200 `admin@hei.edu`
- **GET** `{{base_url}}/teacher/whoami` → Bearer `{{teacher_token}}` → 200 `manitra@hei.edu`
- **GET** `{{base_url}}/student/whoami` → Bearer `{{student_token}}` → 200 `fanjasoa@hei.edu`

## Autres comptes seed
Tous avec mot de passe `password123` : `lova@hei.edu` (TEACHER), `mihary@hei.edu`, `loiqua@hei.edu`, `joachim@hei.edu`, `ninah@hei.edu`, `fanhasina@hei.edu` (STUDENT).

## Erreurs
`401` identifiants invalides · `403` rôle insuffisant · `404` introuvable · réponse toujours `{ "message": "..." }`