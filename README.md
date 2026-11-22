# Tunisie-Holidays.com

Moteur de recherche tourisme pour la Tunisie : vols, hôtels, location de voitures, locations de vacances et loisirs.
Le site compare les offres et **redirige le visiteur vers le site du partenaire** pour réserver ; chaque clic est enregistré pour les statistiques.

- **Backend** : Spring Boot 3.3 (Java 21), Spring Security + JWT, JPA/Hibernate, Flyway, PostgreSQL 16
- **Frontend** : Angular 18 (composants standalone, signals), SCSS
- **Sources des offres** : partenaires locaux (espace partenaire) + API Amadeus pour les vols (optionnelle)

## Démarrage rapide (Docker)

Clonez les deux dépôts côte à côte (`tunisie-holiday-api` et `tunisie-holiday-front`), puis depuis `tunisie-holiday-api` :

```bash
cp .env.example .env
# Éditez .env : JWT_SECRET, ADMIN_PASSWORD, DB_PASSWORD
openssl rand -base64 48   # pour générer JWT_SECRET
docker compose up --build
```

- Site : http://localhost:8081
- API + Swagger : http://localhost:8080/swagger-ui.html

## Déploiement sur un serveur (VPS Ubuntu/Debian)

Connectez-vous en SSH puis :

```bash
curl -fsSL https://raw.githubusercontent.com/Hichem-Nasri/tunisie-holiday-api/master/deploy.sh -o deploy.sh
sudo bash deploy.sh 2.25.11.27
```

Le script installe Docker si besoin, clone les deux dépôts dans `/opt/tunisie-holidays`, génère un `.env` avec des secrets aléatoires,
puis lance PostgreSQL, l'API et le site (port 80). Seul le port 80 est exposé. Pour mettre à jour, relancez la même commande.

## Démarrage en développement

```bash
# 1. PostgreSQL
docker compose up -d postgres

# 2. Backend (port 8080) — ce dépôt
mvn spring-boot:run

# 3. Frontend (port 4200, proxy /api -> 8080) — dépôt tunisie-holiday-front
cd ../tunisie-holiday-front
npm install
npm start
```

## Comptes de démonstration

Créés au démarrage quand `DEMO_PARTNER_PASSWORD` est renseigné (par défaut `Partner@2026!` en dev) :

| Rôle | Email | Mot de passe |
|---|---|---|
| Administrateur | `admin@tunisie-holidays.com` | valeur de `ADMIN_PASSWORD` (dev : `Admin@2026!`) |
| Agence (vols, hôtels…) | `agence@demo.tn` | `Partner@2026!` |
| Loueur de voitures | `loueur@demo.tn` | `Partner@2026!` |
| Locations de vacances | `hote@demo.tn` | `Partner@2026!` |
| Loisirs | `loisirs@demo.tn` | `Partner@2026!` |

Les données de démo (partenaires, hôtels, vols quotidiens sur 240 jours, voitures, locations, activités) sont chargées par `db/demo/V100__demo_data.sql`.
**En production**, désactivez-les : `FLYWAY_LOCATIONS=classpath:db/migration` et laissez `DEMO_PARTNER_PASSWORD` vide.

Exemple de recherche avec les données de démo : vol **Paris (PAR) → Djerba (DJE)**, hôtels à **Hammamet**, voitures à **TUN**.

## Fonctionnalités

**Visiteurs**
- Recherche vols (aller simple / aller-retour, villes multi-aéroports comme PAR), hôtels, voitures, locations, loisirs
- Comparaison des prix d'un même vol / hôtel entre plusieurs agences
- Filtres et tris (prix, durée, meilleur choix, étoiles, formule, équipements, catégorie…)
- Affichage multi-devises (TND, EUR, USD, GBP)
- Alerte prix sur un trajet (enregistrée en base)
- Redirection vers le partenaire via `/api/go/{TYPE}/{id}` avec suivi du clic

**Partenaires** (`/partenaire`)
- Inscription (compte en attente jusqu'à validation par l'admin)
- Gestion des offres selon le type de compte : agence → tout ; hôtel → hôtels ; loueur → voitures ; hôte → locations ; prestataire → loisirs
- Ajout d'hôtels au catalogue, activation / masquage / suppression des offres
- Tableau de bord : clics par jour, par type, par offre (30 jours)

**Administration** (`/admin`)
- Validation / suspension des partenaires
- Destinations (création, modification, mise en avant)
- Taux de change
- Statistiques globales et top partenaires

## API Amadeus (vols)

1. Créez une application sur https://developers.amadeus.com (environnement Self-Service).
2. Renseignez `AMADEUS_ENABLED=true`, `AMADEUS_CLIENT_ID`, `AMADEUS_CLIENT_SECRET`.
3. En production : `AMADEUS_BASE_URL=https://api.amadeus.com`.

Les résultats Amadeus sont fusionnés avec ceux des agences (même vol = même carte, offres comparées).
L'API Self-Service ne fournit pas de lien de réservation : le clic est redirigé vers l'URL définie par
`AMADEUS_BOOKING_URL_TEMPLATE` (variables `{origin}`, `{destination}`, `{departureDate}`, `{returnDate}`, `{adults}`).
Remplacez-la par votre lien d'affiliation.

## Points d'API principaux

| Méthode | Chemin | Accès |
|---|---|---|
| GET | `/api/search/flights?origin=PAR&destination=DJE&departureDate=2026-06-12&returnDate=2026-06-21&adults=2&currency=EUR` | public |
| GET | `/api/search/hotels?q=hammamet&checkIn=…&checkOut=…&adults=2` | public |
| GET | `/api/search/cars?location=TUN&pickupAt=2026-06-12T10:00&dropoffAt=2026-06-19T10:00` | public |
| GET | `/api/search/rentals`, `/api/search/activities` | public |
| GET | `/api/go/{FLIGHT/HOTEL/CAR/RENTAL/ACTIVITY}/{id}` | public (302 + clic) |
| POST | `/api/auth/login`, `/api/auth/register-partner` | public |
| GET/POST/PUT/PATCH/DELETE | `/api/partner/offers/{TYPE}` | partenaire |
| GET/PATCH/POST/PUT | `/api/admin/**` | admin |

Documentation complète : Swagger UI.

## Structure

Backend : ce dépôt. Frontend : [tunisie-holiday-front](https://github.com/Hichem-Nasri/tunisie-holiday-front).

```
tunisie-holiday-api/
  src/main/java/tn/tunisieholidays/
    domain/        entités JPA
    repository/    Spring Data
    service/       métier (search/, partner/)
    web/           contrôleurs REST
    security/      JWT
  src/main/resources/db/migration   schéma + données de référence
  src/main/resources/db/demo        données de démonstration
tunisie-holiday-front/
  src/app/core       modèles, API, auth, devises
  src/app/features   pages (home, vols, hotels, voitures, locations, loisirs, partner, admin)
  src/app/shared     pipes et composants communs
```

## À prévoir avant la mise en production

- Envoi des emails (alertes prix, validation partenaire) : brancher un service SMTP / transactionnel
- Remplacer les illustrations par de vraies photos et les liens `example.com` des données de démo
- Mettre à jour les taux de change (manuellement dans l'admin ou via une API de taux)
- HTTPS (reverse proxy) et sauvegardes PostgreSQL
