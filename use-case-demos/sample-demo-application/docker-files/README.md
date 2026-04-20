# Docker Artifacts for WSO2 Open Banking Sandbox

This directory contains the Docker-related artifacts used by the sample demo application.

## Purpose

The `docker-files/` directory is the Docker build and orchestration root for the WSO2 Open Banking Sandbox deployment. It includes:

- `docker-compose/` for orchestration and service startup
- `my_sql/` for the MySQL persistence image
- `wso2am_with_obam/` for the WSO2 API Manager accelerator Dockerfile
- `wso2is_with_obiam/` for the WSO2 Identity Server accelerator Dockerfile

## Deployment Workflow

The recommended deployment path is to run the root `build.sh` script from the repository root. This script performs the following actions:

1. Builds the MySQL image from `docker-files/my_sql`
2. Builds the demo backend WAR from `demo-application`
3. Copies the WAR into `configuration-files/api-ob-demo.war`
4. Builds the WSO2 OBIAM and OBAM Docker images
5. Starts the Docker Compose deployment from `docker-files/docker-compose`

### Manual Docker Build Steps

If you need to build images manually instead of using `build.sh`:

1. Build the database image:
   ```bash
   cd docker-files/my_sql
   docker build -t ob_database .
   ```
2. Build the WSO2 IS image:
   ```bash
   cd docker-files/wso2is_with_obiam
   docker build --build-arg BASE_PRODUCT_VERSION=7.1.0 \
     --build-arg OB_TRUSTED_CERTS_URL=<URL> \
     --build-arg WSO2_OB_KEYSTORES_URL=<URL> \
     --build-arg RESOURCE_URL=<URL> \
     -t wso2is-ob:4.0.0 .
   ```
3. Build the WSO2 AM image:
   ```bash
   cd docker-files/wso2am_with_obam
   docker build --build-arg BASE_PRODUCT_VERSION=4.5.0 \
     --build-arg OB_TRUSTED_CERTS_URL=<URL> \
     --build-arg WSO2_OB_KEYSTORES_URL=<URL> \
     --build-arg RESOURCE_URL=<URL> \
     -t wso2am-ob:4.0.0 .
   ```

## Configuration Overrides

All deployment configuration overrides are stored in `configuration-files/`:

- `obam-deployment.toml`
- `obiam-deployment.toml`
- `api-ob-demo.war`

Do not perform manual runtime overrides outside this folder unless you are intentionally changing the sandbox deployment.

## Accelerator WAR Deployment

To deploy a new accelerator WAR:

1. Place the updated WAR in `configuration-files/api-ob-demo.war`.
2. Restart the Compose deployment.

The root `build.sh` workflow already copies the generated WAR into that location.

## Persistence Reminder

This deployment is backed by MySQL. If you make permanent changes to the database schema or runtime state, export a new database dump before destroying volumes.

## Compose Orchestration

The actual Compose orchestration lives in `docker-files/docker-compose`.

Use `docker compose up -d` from that directory after building the required images.

If the external network `ob-network` does not exist, create it first:

```bash
docker network create ob-network
```
