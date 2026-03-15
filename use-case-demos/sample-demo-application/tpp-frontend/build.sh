#!/bin/bash
set -e

BASE_URL="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DOCKER_COMPOSE_DIRECTORY=$BASE_URL/docker-files/docker-compose
WSO2_IS_SERVER=$BASE_URL/docker-files/wso2is_with_obiam
WSO2_AM_SERVER=$BASE_URL/docker-files/wso2am_with_obam
MY_SQL=$BASE_URL/docker-files/my_sql
DEMO_BACKEND=$BASE_URL/demo-application

# Fix CRLF line endings on all shell scripts (Windows Git Bash compatibility)
find "$BASE_URL" -name "*.sh" | while read f; do
    tr -d '\r' < "$f" > "$f.tmp" && mv "$f.tmp" "$f"
done
echo "CRLF fix applied to all shell scripts"

lsof -ti:8000 | xargs kill -9 2>/dev/null || true

python3 -m http.server 8000 &
SERVER_PID=$!

cd "$MY_SQL"
docker build -t ob_database .
echo "MySQL build complete"

# Build the demo backend WAR FIRST (before IS image build so it can be COPYed in)
cd "$DEMO_BACKEND"
mvn clean package -DskipTests
echo "Demo backend WAR build complete"

# Build IS image with root as build context so Dockerfile can COPY the WAR
cd "$BASE_URL"
docker build \
    --build-arg BASE_PRODUCT_VERSION=7.1.0 \
    --build-arg OB_TRUSTED_CERTS_URL=http://host.docker.internal:8000/configuration-files/trust_certs.zip \
    --build-arg WSO2_OB_KEYSTORES_URL=http://host.docker.internal:8000/configuration-files/keystores \
    --build-arg RESOURCE_URL=http://host.docker.internal:8000 \
    --no-cache \
    -f "$WSO2_IS_SERVER/Dockerfile" \
    -t wso2is-ob:4.0.0 .
echo "IS server build complete"

# cd "$WSO2_AM_SERVER"
# docker build \
#     --build-arg BASE_PRODUCT_VERSION=4.5.0 \
#     --build-arg OB_TRUSTED_CERTS_URL=http://host.docker.internal:8000/configuration-files/trust_certs.zip \
#     --build-arg WSO2_OB_KEYSTORES_URL=http://host.docker.internal:8000/configuration-files/keystores \
#     --build-arg RESOURCE_URL=http://host.docker.internal:8000 \
#     --no-cache -t wso2am-ob:4.0.0 .
# echo "AM server build complete"

kill $SERVER_PID 2>/dev/null || true

cd "$DOCKER_COMPOSE_DIRECTORY"
# Explicitly fix CRLF on wait-for-it.sh (mounted into containers via volume)
# tr -d '\r' < "$DOCKER_COMPOSE_DIRECTORY/wait-for-it.sh" > "$DOCKER_COMPOSE_DIRECTORY/wait-for-it.sh.tmp" && mv "$DOCKER_COMPOSE_DIRECTORY/wait-for-it.sh.tmp" "$DOCKER_COMPOSE_DIRECTORY/wait-for-it.sh"
docker compose up -d
echo "Docker compose started"

echo "──────────────────────────────────────────"
echo "All done!"
echo "IS Console : https://obiam:9446/console"
echo "App URL    : https://obiam:9446/ob-demo-backend-1.0.0"
echo "──────────────────────────────────────────"

docker compose logs -f
