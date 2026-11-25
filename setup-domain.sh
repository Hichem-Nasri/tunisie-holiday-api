#!/usr/bin/env bash
set -euo pipefail
trap 'echo "ERREUR ligne $LINENO : $BASH_COMMAND" >&2' ERR

DOMAIN="${1:-tunisie-holidays.com}"
APP_PORT="${2:-8088}"
SITE_NAME="tunisie-holidays"

if [ "$(id -u)" -ne 0 ]; then echo "Lancez ce script avec sudo ou en root."; exit 1; fi

SERVER_IP="$(curl -fs4 https://api.ipify.org || hostname -I | awk '{print $1}')"
echo "==> Domaine : $DOMAIN (+ www)  |  IP du serveur : $SERVER_IP  |  application : 127.0.0.1:$APP_PORT"

if ! curl -fs "http://127.0.0.1:${APP_PORT}/api/destinations" >/dev/null; then
  echo "L'application ne répond pas sur le port $APP_PORT. Lancez d'abord deploy.sh."
  exit 1
fi

for name in "$DOMAIN" "www.$DOMAIN"; do
  resolved="$(getent ahostsv4 "$name" | awk 'NR==1{print $1}')"
  if [ "$resolved" != "$SERVER_IP" ]; then
    echo "Le DNS de $name pointe vers '${resolved:-rien}' et pas encore vers $SERVER_IP."
    echo "La propagation peut prendre jusqu'à 1 heure. Relancez ce script plus tard."
    exit 1
  fi
done
echo "==> DNS OK"

OWNER80="$(ss -ltnpH '( sport = :80 )' 2>/dev/null | head -1 || true)"
echo "==> Port 80 : ${OWNER80:-libre}"

if echo "$OWNER80" | grep -q 'docker-proxy'; then
  echo
  echo "Le port 80 est tenu par un conteneur Docker (reverse proxy de l'autre application)."
  echo "Pour ne rien casser, ce script s'arrête ici. Envoyez cette sortie :"
  docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Ports}}'
  exit 1
fi

if echo "$OWNER80" | grep -Eq 'apache2|httpd'; then
  echo "==> Apache détecté : ajout d'un VirtualHost dédié"
  a2enmod proxy proxy_http headers >/dev/null
  cat > "/etc/apache2/sites-available/${SITE_NAME}.conf" <<CONF
<VirtualHost *:80>
    ServerName ${DOMAIN}
    ServerAlias www.${DOMAIN}
    ProxyPreserveHost On
    ProxyPass / http://127.0.0.1:${APP_PORT}/
    ProxyPassReverse / http://127.0.0.1:${APP_PORT}/
    RequestHeader set X-Forwarded-Proto "http"
</VirtualHost>
CONF
  a2ensite "${SITE_NAME}" >/dev/null
  apache2ctl configtest
  systemctl reload apache2
  apt-get install -y -qq certbot python3-certbot-apache >/dev/null
  certbot --apache -d "$DOMAIN" -d "www.$DOMAIN" --non-interactive --agree-tos --register-unsafely-without-email --redirect
else
  if [ -z "$OWNER80" ] && ! command -v nginx >/dev/null 2>&1; then
    echo "==> Installation de nginx"
    apt-get update -qq && apt-get install -y -qq nginx >/dev/null
  fi
  if ! command -v nginx >/dev/null 2>&1; then
    echo "Serveur web non reconnu sur le port 80 : $OWNER80"
    echo "Ce script s'arrête sans rien modifier. Envoyez cette sortie."
    exit 1
  fi
  echo "==> nginx : ajout d'un site dédié (les autres sites ne sont pas modifiés)"
  mkdir -p /etc/nginx/sites-available /etc/nginx/sites-enabled
  cat > "/etc/nginx/sites-available/${SITE_NAME}" <<CONF
server {
    listen 80;
    listen [::]:80;
    server_name ${DOMAIN} www.${DOMAIN};

    client_max_body_size 10m;

    location / {
        proxy_pass http://127.0.0.1:${APP_PORT};
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
CONF
  ln -sf "/etc/nginx/sites-available/${SITE_NAME}" "/etc/nginx/sites-enabled/${SITE_NAME}"
  nginx -t
  systemctl enable --now nginx >/dev/null 2>&1 || true
  systemctl reload nginx
  apt-get install -y -qq certbot python3-certbot-nginx >/dev/null
  certbot --nginx -d "$DOMAIN" -d "www.$DOMAIN" --non-interactive --agree-tos --register-unsafely-without-email --redirect
fi

if command -v ufw >/dev/null 2>&1 && ufw status | grep -q "Status: active"; then
  ufw allow 80/tcp >/dev/null
  ufw allow 443/tcp >/dev/null
fi

ENV_FILE="$(dirname "$(readlink -f "$0")")/.env"
if [ -f "$ENV_FILE" ]; then
  sed -i "s#^CORS_ALLOWED_ORIGINS=.*#CORS_ALLOWED_ORIGINS=https://${DOMAIN},https://www.${DOMAIN}#" "$ENV_FILE"
fi

echo
if curl -fs "https://${DOMAIN}/api/destinations" >/dev/null; then
  echo "Site en ligne : https://${DOMAIN}"
else
  echo "Configuration terminée. Vérifiez https://${DOMAIN} dans votre navigateur."
fi
echo "Le certificat HTTPS se renouvelle automatiquement (certbot)."
