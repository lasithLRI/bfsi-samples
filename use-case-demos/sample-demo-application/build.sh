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

# Kill any existing server on port 8000
lsof -ti:8000 | xargs kill -9 2>/dev/null || true

# Start HTTP server from BASE_URL so Docker can fetch all configuration files
cd "$BASE_URL"
python -m http.server 8000 &
SERVER_PID=$!
sleep 2
echo "HTTP server started (PID: $SERVER_PID) serving: $BASE_URL"

# Verify key files are reachable before starting builds
echo "Verifying HTTP server can serve required files..."
curl -sf "http://localhost:8000/configuration-files/keystores/private-keys.jks" -o /dev/null \
    && echo "  private-keys.jks reachable" \
    || echo "  WARNING: private-keys.jks not found - check configuration-files/keystores/"
curl -sf "http://localhost:8000/configuration-files/keystores/public-certs.jks" -o /dev/null \
    && echo "  public-certs.jks reachable" \
    || echo "  WARNING: public-certs.jks not found - check configuration-files/keystores/"
curl -sf "http://localhost:8000/configuration-files/trust_certs.zip" -o /dev/null \
    && echo "  trust_certs.zip reachable" \
    || echo "  WARNING: trust_certs.zip not found - check configuration-files/"
curl -sf "http://localhost:8000/configuration-files/customErrorFormatter.xml" -o /dev/null \
    && echo "  customErrorFormatter.xml reachable" \
    || echo "  WARNING: customErrorFormatter.xml not found - check configuration-files/"

# Build MySQL image
cd "$MY_SQL"
docker build -t ob_database .
echo "MySQL build complete"

# Build demo backend WAR
cd "$DEMO_BACKEND"
mvn clean package -DskipTests
cp "target/ob-demo-backend-1.0.0.war" "$BASE_URL/configuration-files/api-fs-backend.war"
echo "Demo backend WAR build complete"

# Verify WAR is reachable
curl -sf "http://localhost:8000/configuration-files/api-fs-backend.war" -o /dev/null \
    && echo "  api-fs-backend.war reachable" \
    || echo "  WARNING: api-fs-backend.war not found - check configuration-files/"

# Build IS server image (context must be BASE_URL so all files are accessible)
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

# Build AM server image (context must be BASE_URL so all files are accessible)
cd "$BASE_URL"
docker build \
    --build-arg BASE_PRODUCT_VERSION=4.5.0 \
    --build-arg OB_TRUSTED_CERTS_URL=http://host.docker.internal:8000/configuration-files/trust_certs.zip \
    --build-arg WSO2_OB_KEYSTORES_URL=http://host.docker.internal:8000/configuration-files/keystores \
    --build-arg RESOURCE_URL=http://host.docker.internal:8000 \
    --no-cache \
    -f "$WSO2_AM_SERVER/Dockerfile" \
    -t wso2am-ob:4.0.0 .
echo "AM server build complete"

# Stop the HTTP server now that all builds are done
kill $SERVER_PID 2>/dev/null || true
echo "HTTP server stopped"

# Pre-create the network so compose never errors on a missing network
docker network create ob-network 2>/dev/null || true
echo "ob-network ensured"

cd "$DOCKER_COMPOSE_DIRECTORY"

# Verify the compose file is present before attempting up
if [ ! -f "docker-compose.yml" ] && [ ! -f "compose.yml" ]; then
    echo "ERROR: No docker-compose.yml found in $DOCKER_COMPOSE_DIRECTORY"
    exit 1
fi

docker compose up -d
echo "Docker compose started"

# Wait for obam container to be ready before deploying WAR
echo "Waiting for obam to be ready..."
until docker logs obam 2>&1 | grep -q "Pass-through HTTPS Listener started on 0.0.0.0:8243"; do
    echo "  still waiting..."
    sleep 5
done
echo "obam is ready!"

# Replace api#fs#backend WAR in running container
docker exec obam rm -f '/home/wso2carbon/wso2am-4.5.0/repository/deployment/server/webapps/api#fs#backend.war'
docker exec obam rm -rf '/home/wso2carbon/wso2am-4.5.0/repository/deployment/server/webapps/api#fs#backend'
docker cp "$BASE_URL/configuration-files/api#fs#backend.war" obam:'/home/wso2carbon/wso2am-4.5.0/repository/deployment/server/webapps/api#fs#backend.war'
echo "api#fs#backend.war deployed to obam"

echo "──────────────────────────────────────────"
echo "All done!"
echo "IS Console : https://obiam:9446/console"
echo "App URL    : https://obiam:9446/ob-demo-backend-1.0.0"
echo "──────────────────────────────────────────"

docker compose logs -f
