#!/usr/bin/env bash
set -euo pipefail

HOST="${1:-}"
BASE_DIR="${BASE_DIR:-/opt/tunisie-holidays}"
API_REPO="https://github.com/Hichem-Nasri/tunisie-holiday-api.git"
FRONT_REPO="https://github.com/Hichem-Nasri/tunisie-holiday-front.git"

if [ -z "$HOST" ]; then
  echo "Usage : sudo ./deploy.sh <ip-ou-domaine>   (ex. sudo ./deploy.sh 2.25.11.27)"
  exit 1
fi
if [ "$(id -u)" -ne 0 ]; then
  echo "Lancez ce script avec sudo ou en root."
  exit 1
fi

echo "==> Paquets de base"
if command -v apt-get >/dev/null 2>&1; then
  apt-get update -qq
  apt-get install -y -qq git curl openssl ca-certificates >/dev/null
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "==> Installation de Docker"
  curl -fsSL https://get.docker.com | sh
fi
systemctl enable --now docker >/dev/null 2>&1 || true

MEM_MB=$(awk '/MemTotal/ {print int($2/1024)}' /proc/meminfo)
if [ "$MEM_MB" -lt 3000 ] && ! swapon --show | grep -q .; then
  echo "==> Création d'un swap de 2 Go (RAM : ${MEM_MB} Mo)"
  fallocate -l 2G /swapfile || dd if=/dev/zero of=/swapfile bs=1M count=2048
  chmod 600 /swapfile
  mkswap /swapfile >/dev/null
  swapon /swapfile
  grep -q '/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

echo "==> Récupération du code dans $BASE_DIR"
mkdir -p "$BASE_DIR"
cd "$BASE_DIR"
for pair in "tunisie-holiday-api|$API_REPO" "tunisie-holiday-front|$FRONT_REPO"; do
  dir="${pair%%|*}"
  url="${pair#*|}"
  if [ -d "$dir/.git" ]; then
    git -C "$dir" fetch -q origin master
    git -C "$dir" reset -q --hard origin/master
  else
    git clone -q "$url" "$dir"
  fi
done

cd "$BASE_DIR/tunisie-holiday-api"
NEW_ENV=0
if [ ! -f .env ]; then
  echo "==> Génération du fichier .env"
  ADMIN_PASSWORD="$(openssl rand -base64 18 | tr -dc 'A-Za-z0-9' | head -c 16)"
  cat > .env <<ENV
DB_USER=tunisie
DB_PASSWORD=$(openssl rand -hex 16)
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
ADMIN_EMAIL=admin@tunisie-holidays.com
ADMIN_PASSWORD=${ADMIN_PASSWORD}
DEMO_PARTNER_PASSWORD=Partner@2026!
FLYWAY_LOCATIONS=classpath:db/migration,classpath:db/demo
CORS_ALLOWED_ORIGINS=http://${HOST}
AMADEUS_ENABLED=false
AMADEUS_BASE_URL=https://test.api.amadeus.com
AMADEUS_CLIENT_ID=
AMADEUS_CLIENT_SECRET=
ENV
  chmod 600 .env
  NEW_ENV=1
fi

if command -v ufw >/dev/null 2>&1 && ufw status | grep -q "Status: active"; then
  echo "==> Ouverture du port 80 dans le pare-feu"
  ufw allow 80/tcp >/dev/null
fi

echo "==> Construction et démarrage (premier lancement : 5 à 10 minutes)"
docker compose -f docker-compose.prod.yml up -d --build

echo "==> Attente du démarrage de l'API"
for i in $(seq 1 60); do
  if curl -fs "http://localhost/api/destinations" >/dev/null 2>&1; then
    READY=1
    break
  fi
  sleep 5
done

echo
if [ "${READY:-0}" = "1" ]; then
  echo "Site en ligne : http://${HOST}"
else
  echo "Les conteneurs tournent mais l'API ne répond pas encore. Logs : docker compose -f docker-compose.prod.yml logs -f backend"
fi
echo "Swagger : désactivé publiquement (port 8080 non exposé)."
if [ "$NEW_ENV" = "1" ]; then
  echo
  echo "Compte administrateur : admin@tunisie-holidays.com"
  echo "Mot de passe          : $(grep '^ADMIN_PASSWORD=' .env | cut -d= -f2)"
  echo "(conservé dans $BASE_DIR/tunisie-holiday-api/.env)"
fi
echo "Comptes partenaires de démo : agence@demo.tn, loueur@demo.tn, hote@demo.tn, loisirs@demo.tn / Partner@2026!"
