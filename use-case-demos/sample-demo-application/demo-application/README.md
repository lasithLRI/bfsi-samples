# Demo Application Backend Setup

This project is the backend WAR and packaging module for the sample Open Banking demonstrator.

## Purpose

- Builds the backend WAR for the demo application
- Packages the React frontend into the WAR
- Provides the runtime web application deployed by WSO2

## Repository Layout

- `pom.xml` — backend Maven project configuration
- `components/ob_demo_application/src/main/java` — Java source files
- `components/ob_demo_application/src/main/resources` — backend resources
- `components/ob_demo_application/src/main/webapp` — final webapp layout
- `components/ob_demo_application/frontend` — TPP frontend source and Vite build

## Prerequisites

- JDK 11 or newer
- Maven 3.6+ (or higher)
- Git Bash / WSL on Windows for shell compatibility if using `build.sh`

## Complete Build Steps

From the backend project root:

```bash
cd demo-application
mvn clean package -DskipTests
```

This command performs the following actions:

1. Installs frontend dependencies inside `components/ob_demo_application/frontend`.
2. Builds the React frontend with `pnpm run build`.
3. Copies the generated frontend assets into `components/ob_demo_application/src/main/webapp`.
4. Packages the entire backend and frontend into `target/api-ob-demo-1.0.0.war`.

## WAR Artifact

The generated deployment artifact is:

- `demo-application/target/api-ob-demo-1.0.0.war`

The root deployment workflow expects this WAR to be copied to:

- `configuration-files/api-ob-demo.war`

When you use `build.sh`, the script automatically performs this copy step.

## Modifying Backend Features

If you change backend Java code or resources:

1. Edit files under `components/ob_demo_application/src/main/java`
2. Update resource files under `components/ob_demo_application/src/main/resources`
3. Optionally edit web UI files in `components/ob_demo_application/src/main/webapp`
4. Run `mvn clean package -DskipTests`

## Modifying Frontend Features

The frontend is a part of the backend build. To change UI behavior:

1. Edit `demo-application/components/ob_demo_application/frontend/src`
2. Optionally update sample data in `demo-application/components/ob_demo_application/frontend/public/configurations/config.json`
3. Run `mvn clean package -DskipTests`

## Configuration Overrides

All runtime deployment overrides are managed at the repository root in `configuration-files/`.

Key override files:

- `configuration-files/obam-deployment.toml`
- `configuration-files/obiam-deployment.toml`
- `configuration-files/api-ob-demo.war`

> Important: no special manual runtime environment configuration is required outside `configuration-files/`.

## New Accelerator WAR Deployment

To deploy a new accelerator WAR file:

1. Build or obtain the replacement WAR.
2. Copy it into `configuration-files/api-ob-demo.war`.
3. Restart the Docker Compose stack.

## Persistence Warning

This deployment uses MySQL state in Docker volumes.

> If the demo state must be preserved permanently after changes, create a new database dump before destroying containers or volumes.

## Notes

- Use the root `build.sh` script for a complete end-to-end setup.
- If you want to validate only the backend build, `mvn clean package -DskipTests` is sufficient.
- The frontend build is automatically triggered by Maven during the backend packaging process.
