# Sample Demo Application for WSO2 Open Banking Sandbox

This repository contains a sample demonstrator application that connects to the WSO2 Open Banking Sandbox Bank. It combines a Java backend, a React frontend, and the required WSO2 Open Banking accelerator deployment artifacts.

## Core Purpose

The application demonstrates how a Third-Party Provider (TPP) front-end interacts with WSO2 Open Banking components. The deployment uses:

- WSO2 Identity Server with the Open Banking Identity & Access Management accelerator (OBIAM)
- WSO2 API Manager with the Open Banking API Management accelerator (OBAM)
- MySQL persistence for runtime data
- A packaged WAR backend for the demo application

## Key Configuration Principles

- No special manual deployment configuration is required.
- All runtime overrides must be managed through the `configuration-files/` folder.
- The most important override files are:
  - `configuration-files/obam-deployment.toml`
  - `configuration-files/obiam-deployment.toml`
- Use these files to tune environment values, endpoints, keystore settings, and database connections.

## Repository Structure

- `configuration-files/`
  - Contains deployment overrides, database drivers, certificate bundles, and demo WAR artifacts.
- `demo-application/`
  - Contains the Java Maven project and the React frontend source.
- `docker-files/`
  - Contains Docker build and Compose orchestration artifacts.
- `build.sh`
  - Root build script that builds the database image, backend WAR, WSO2 images, and starts the Docker Compose deployment.

## Getting Started

### Prerequisites

- Docker
- Docker Compose
- Git Bash or WSL on Windows for `./build.sh`
- JDK 11 or higher and Maven if you want to build the backend locally outside the root script

### Recommended Start

From the repository root, run:

```bash
./build.sh
```

This script:

1. Builds the `ob_database` image from `docker-files/my_sql`
2. Runs `mvn clean package -DskipTests` in `demo-application`
3. Copies the generated WAR to `configuration-files/api-ob-demo.war`
4. Builds the WSO2 IS and AM Docker images
5. Starts the Compose deployment in `docker-files/docker-compose`

### Alternative Compose Start

If the images are already built, you may also start the deployment manually:

```bash
cd docker-files/docker-compose
docker compose up -d
```

Ensure the external network `ob-network` exists before running Compose.

## Development Guide

### Modify Frontend Features

1. Edit the frontend sources in:
   - `demo-application/components/ob_demo_application/frontend/src`
2. Install dependencies and test locally if needed with `pnpm`.
3. Rebuild the frontend assets by running the Maven build in `demo-application`.

To package the full application, run:

```bash
cd demo-application
mvn clean package -DskipTests
```

The build generates the WAR and copies the frontend `dist` output into the final web application layout.

### Modify Backend Features

1. Edit Java sources in:
   - `demo-application/components/ob_demo_application/src/main/java`
2. Edit backend resources in:
   - `demo-application/components/ob_demo_application/src/main/resources`
   - `demo-application/components/ob_demo_application/src/main/webapp`
3. Rebuild the WAR:

```bash
cd demo-application
mvn clean package -DskipTests
```

### Incorporate a New Accelerator WAR

The deployment uses the WAR file located at:

- `configuration-files/api-ob-demo.war`

To replace it with a new accelerator WAR:

1. Build or obtain the new WAR file.
2. Copy it to `configuration-files/api-ob-demo.war`.
3. Restart the Compose deployment so the updated WAR is deployed.

When using `build.sh`, the script already copies the generated WAR to the correct location.

## Persistence and Data State

This project stores runtime data in a MySQL volume called `mysql_data`.

> Important: if you make permanent changes to the demo data or deployment state, you must create a new database dump. Otherwise, state changes may be lost when containers or volumes are rebuilt.

To preserve state, export the database from the MySQL container and store the dump alongside your deployment artifacts.

## Important Notes

- This repository is designed as a demonstrator for other implementers.
- All sandbox-specific overrides are centrally managed through `configuration-files/`.
- Do not edit the Docker Compose internals unless you are intentionally changing container orchestration or external network setup.

## Useful URLs

- WSO2 API Manager: `https://obam:9443`
- WSO2 Identity Server / OBIAM Console: `https://obiam:9446/console`
- Demo app deployment path: see `build.sh` output, typically available through the deployed WSO2 server
