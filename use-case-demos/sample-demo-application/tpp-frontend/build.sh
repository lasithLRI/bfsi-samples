#!/bin/bash
set -e

BASE_URL="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DOCKER_COMPOSE_DIRECTORY=$BASE_URL/docker-files/docker-compose
WSO2_IS_SERVER=$BASE_URL/docker-files/wso2is_with_obiam
WSO2_AM_SERVER=$BASE_URL/docker-files/wso2am_with_obam
MY_SQL=$BASE_URL/docker-files/my_sql
DEMO_BACKEND=$BASE_URL/demo-application

IS_CONTAINER_NAME="obiam"
IS_WEBAPPS_PATH="/home/wso2carbon/wso2is-7.1.0/repository/deployment/server/webapps"

netstat -ano | grep :8000 | awk '{print $5}' | xargs -I {} taskkill //PID {} //F 2>/dev/null || true

python -m http.server 8000 &
SERVER_PID=$!

cd "$MY_SQL"
docker build -t ob_database .
echo "MySQL build complete"

cd "$WSO2_IS_SERVER"
docker build \
    --build-arg BASE_PRODUCT_VERSION=7.1.0 \
    --build-arg OB_TRUSTED_CERTS_URL=http://host.docker.internal:8000/configuration-files/trust_certs.zip \
    --build-arg WSO2_OB_KEYSTORES_URL=http://host.docker.internal:8000/configuration-files/keystores \
    --build-arg RESOURCE_URL=http://host.docker.internal:8000/ \
    --no-cache -t wso2is-ob:4.0.0 .
echo "IS server build complete"

cd "$WSO2_AM_SERVER"
docker build \
    --build-arg BASE_PRODUCT_VERSION=4.5.0 \
    --build-arg OB_TRUSTED_CERTS_URL=http://host.docker.internal:8000/configuration-files/trust_certs.zip \
    --build-arg WSO2_OB_KEYSTORES_URL=http://host.docker.internal:8000/configuration-files/keystores \
    --build-arg RESOURCE_URL=http://host.docker.internal:8000 \
    --no-cache -t wso2am-ob:4.0.0 .
echo "AM server build complete"

cd "$DEMO_BACKEND"
mvn clean package -DskipTests
echo "Demo backend WAR build complete"

kill $SERVER_PID 2>/dev/null || true

cd "$DOCKER_COMPOSE_DIRECTORY"          # ← fixed
docker compose up -d
echo "Docker compose started"

echo "Waiting for obiam to be healthy..."
until [ "$(docker inspect -f '{{.State.Health.Status}}' obiam)" = "healthy" ]; do
    printf "."
    sleep 10
done
echo ""
echo "obiam is healthy — deploying WAR"

WAR_FILE=$(find "$DEMO_BACKEND/target" -name "*.war" | head -1)
docker cp "$WAR_FILE" "$IS_CONTAINER_NAME:$IS_WEBAPPS_PATH/"
echo "WAR deployed: $(basename $WAR_FILE)"

echo "──────────────────────────────────────────"
echo "All done!"
echo "IS Console : https://obiam:9446/console"
echo "App URL    : https://obiam:9446/ob-demo-backend-1.0.0"
echo "──────────────────────────────────────────"

docker compose logs -f
