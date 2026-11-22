#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage :"
  echo "  sudo bash deploy.sh --check                 Inspecte le serveur sans rien modifier"
  echo "  sudo bash deploy.sh <ip-ou-domaine> [port]  Déploie (port HTTP libre, 8088 par défaut)"
}

port_in_use() {
  ss -ltnH "( sport = :$1 )" 2>/dev/null | grep -q . && return 0
  return 1
}

if [ "${1:-}" = "--check" ]; then
  echo "== Système"; uname -a; grep PRETTY_NAME /etc/os-release || true
  echo; echo "== Mémoire"; free -m
  echo; echo "== Disque"; df -h /
  echo; echo "== Ports en écoute"; ss -ltnp 2>/dev/null | awk 'NR==1 || /LISTEN/'
  echo; echo "== Docker"
  if command -v docker >/dev/null 2>&1; then
    docker --version
    docker compose version 2>/dev/null || docker-compose --version 2>/dev/null || echo "pas de docker compose"
    docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Ports}}'
  else
    echo "Docker non installé"
  fi
  echo; echo "== Serveurs web hôte"
  for s in nginx apache2 httpd caddy traefik; do systemctl is-active --quiet "$s" 2>/dev/null && echo "$s : actif"; done
  ls /etc/nginx/sites-enabled 2>/dev/null || true
  echo; echo "Aucune modification n'a été faite."
  exit 0
fi

HOST="${1:-}"
HTTP_PORT="${2:-8088}"
BASE_DIR="${BASE_DIR:-/opt/tunisie-holidays}"
API_REPO="https://github.com/Hichem-Nasri/tunisie-holiday-api.git"
FRONT_REPO="https://github.com/Hichem-Nasri/tunisie-holiday-front.git"

if [ -z "$HOST" ]; then usage; exit 1; fi
if [ "$(id -u)" -ne 0 ]; then echo "Lancez ce script avec sudo ou en root."; exit 1; fi

if [ -d "$BASE_DIR/tunisie-holiday-api/.git" ] && docker ps --format '{{.Names}}' 2>/dev/null | grep -q '^tunisie-holidays-frontend'; then
  FIRST_RUN=0
else
  FIRST_RUN=1
fi

if [ "$FIRST_RUN" = "1" ] && port_in_use "$HTTP_PORT"; then
  echo "Le port $HTTP_PORT est déjà utilisé sur ce serveur :"
  ss -ltnp "( sport = :$HTTP_PORT )" || true
  echo "Choisissez un autre port : sudo bash deploy.sh $HOST <port-libre>"
  exit 1
fi

if ! command -v git >/dev/null 2>&1 || ! command -v curl >/dev/null 2>&1 || ! command -v openssl >/dev/null 2>&1; then
  echo "==> Installation de git, curl, openssl"
  apt-get update -qq && apt-get install -y -qq git curl openssl ca-certificates >/dev/null
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "==> Installation de Docker"
  curl -fsSL https://get.docker.com | sh
fi

if docker compose version >/dev/null 2>&1; then
  COMPOSE="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE="docker-compose"
else
  echo "==> Installation du plugin docker compose"
  apt-get install -y -qq docker-compose-plugin >/dev/null 2>&1 || apt-get install -y -qq docker-compose-v2 >/dev/null
  COMPOSE="docker compose"
fi

MEM_MB=$(awk '/MemTotal/ {print int($2/1024)}' /proc/meminfo)
if [ "$MEM_MB" -lt 3000 ] && ! swapon --show | grep -q . && [ ! -e /swapfile ]; then
  echo "==> Création d'un swap de 2 Go (RAM : ${MEM_MB} Mo)"
  fallocate -l 2G /swapfile || dd if=/dev/zero of=/swapfile bs=1M count=2048
  chmod 600 /swapfile
  mkswap /swapfile >/dev/null
  swapon /swapfile
  grep -q '^/swapfile ' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

echo "==> Code dans $BASE_DIR (rien d'autre n'est modifié sur le serveur)"
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
HTTP_PORT=${HTTP_PORT}
DB_USER=tunisie
DB_PASSWORD=$(openssl rand -hex 16)
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
ADMIN_EMAIL=admin@tunisie-holidays.com
ADMIN_PASSWORD=${ADMIN_PASSWORD}
DEMO_PARTNER_PASSWORD=Partner@2026!
FLYWAY_LOCATIONS=classpath:db/migration,classpath:db/demo
CORS_ALLOWED_ORIGINS=http://${HOST}:${HTTP_PORT}
AMADEUS_ENABLED=false
AMADEUS_BASE_URL=https://test.api.amadeus.com
AMADEUS_CLIENT_ID=
AMADEUS_CLIENT_SECRET=
ENV
  chmod 600 .env
  NEW_ENV=1
fi
HTTP_PORT="$(grep '^HTTP_PORT=' .env | cut -d= -f2)"

if command -v ufw >/dev/null 2>&1 && ufw status | grep -q "Status: active"; then
  echo "==> Pare-feu : ouverture du port $HTTP_PORT"
  ufw allow "${HTTP_PORT}/tcp" >/dev/null
fi

echo "==> Construction et démarrage (premier lancement : 5 à 10 minutes)"
$COMPOSE -f docker-compose.prod.yml up -d --build

echo "==> Attente du démarrage de l'API"
READY=0
for i in $(seq 1 60); do
  if curl -fs "http://localhost:${HTTP_PORT}/api/destinations" >/dev/null 2>&1; then READY=1; break; fi
  sleep 5
done

echo
if [ "$READY" = "1" ]; then
  echo "Site en ligne : http://${HOST}:${HTTP_PORT}"
else
  echo "Les conteneurs tournent mais l'API ne répond pas encore."
  echo "Logs : cd $BASE_DIR/tunisie-holiday-api && $COMPOSE -f docker-compose.prod.yml logs -f backend"
fi
if [ "$NEW_ENV" = "1" ]; then
  echo
  echo "Compte administrateur : admin@tunisie-holidays.com"
  echo "Mot de passe          : $(grep '^ADMIN_PASSWORD=' .env | cut -d= -f2)"
  echo "(conservé dans $BASE_DIR/tunisie-holiday-api/.env)"
fi
echo "Comptes partenaires de démo : agence@demo.tn, loueur@demo.tn, hote@demo.tn, loisirs@demo.tn / Partner@2026!"
