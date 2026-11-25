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
  PROXY="$(docker ps --filter publish=80 --format '{{.Names}}' | head -1)"
  PROXY_IMAGE="$(docker inspect -f '{{.Config.Image}}' "$PROXY")"
  if ! echo "$PROXY_IMAGE" | grep -qi caddy; then
    echo "Le port 80 est tenu par le conteneur $PROXY ($PROXY_IMAGE), qui n'est pas Caddy."
    echo "Ce script s'arrête sans rien modifier. Envoyez cette sortie."
    exit 1
  fi
  echo "==> Caddy détecté dans le conteneur $PROXY"

  CADDYFILE="$(docker inspect -f '{{range .Mounts}}{{if eq .Destination "/etc/caddy/Caddyfile"}}{{.Source}}{{end}}{{end}}' "$PROXY")"
  if [ -z "$CADDYFILE" ]; then
    CADDYDIR="$(docker inspect -f '{{range .Mounts}}{{if eq .Destination "/etc/caddy"}}{{.Source}}{{end}}{{end}}' "$PROXY")"
    [ -n "$CADDYDIR" ] && CADDYFILE="$CADDYDIR/Caddyfile"
  fi
  if [ -z "$CADDYFILE" ] || [ ! -f "$CADDYFILE" ]; then
    echo "Le Caddyfile de $PROXY n'est pas monté depuis le serveur : impossible de l'étendre sans reconstruire l'autre application."
    docker inspect -f '{{range .Mounts}}{{.Source}} -> {{.Destination}}{{"\n"}}{{end}}' "$PROXY"
    exit 1
  fi
  echo "==> Caddyfile : $CADDYFILE"

  GATEWAY="$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.Gateway}} {{end}}' "$PROXY" | awk '{print $1}')"
  SUBNET="$(docker network inspect -f '{{range .IPAM.Config}}{{.Subnet}} {{end}}' "$(docker inspect -f '{{range $k, $v := .NetworkSettings.Networks}}{{$k}} {{end}}' "$PROXY" | awk '{print $1}')" | awk '{print $1}')"
  echo "==> Caddy joindra l'application via ${GATEWAY}:${APP_PORT}"
  if ! docker exec "$PROXY" wget -q -T 5 -O /dev/null "http://${GATEWAY}:${APP_PORT}/api/destinations" 2>/dev/null; then
    if command -v ufw >/dev/null 2>&1 && ufw status | grep -q "Status: active" && [ -n "$SUBNET" ]; then
      ufw allow from "$SUBNET" to any port "$APP_PORT" proto tcp >/dev/null
    fi
    if ! docker exec "$PROXY" wget -q -T 5 -O /dev/null "http://${GATEWAY}:${APP_PORT}/api/destinations" 2>/dev/null; then
      echo "Caddy n'arrive pas à joindre ${GATEWAY}:${APP_PORT}. Rien n'a été modifié."
      exit 1
    fi
  fi

  if grep -q "${DOMAIN}" "$CADDYFILE"; then
    echo "==> Le domaine est déjà présent dans le Caddyfile, aucune modification."
  else
    BACKUP="${CADDYFILE}.bak-$(date +%Y%m%d-%H%M%S)"
    cp -p "$CADDYFILE" "$BACKUP"
    echo "==> Sauvegarde : $BACKUP"
    cat >> "$CADDYFILE" <<CONF

${DOMAIN}, www.${DOMAIN} {
	encode gzip
	reverse_proxy ${GATEWAY}:${APP_PORT}
}
CONF
    if ! docker exec "$PROXY" caddy validate --config /etc/caddy/Caddyfile --adapter caddyfile >/tmp/caddy-validate.log 2>&1; then
      cat "$BACKUP" > "$CADDYFILE"
      echo "Configuration Caddy invalide : le Caddyfile a été restauré tel quel."
      tail -5 /tmp/caddy-validate.log
      exit 1
    fi
  fi
  docker exec -w /etc/caddy "$PROXY" caddy reload --config /etc/caddy/Caddyfile --adapter caddyfile
  echo "==> Caddy rechargé (les autres sites ne sont pas interrompus). Obtention du certificat HTTPS…"

  ENV_FILE="$(dirname "$(readlink -f "$0")")/.env"
  [ -f "$ENV_FILE" ] && sed -i "s#^CORS_ALLOWED_ORIGINS=.*#CORS_ALLOWED_ORIGINS=https://${DOMAIN},https://www.${DOMAIN}#" "$ENV_FILE"

  for i in $(seq 1 24); do
    if curl -fs "https://${DOMAIN}/api/destinations" >/dev/null 2>&1; then
      echo
      echo "Site en ligne : https://${DOMAIN}"
      exit 0
    fi
    sleep 5
  done
  echo "Caddy est configuré ; le certificat peut prendre encore une minute. Suivi : docker logs --tail 30 $PROXY"
  exit 0
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
