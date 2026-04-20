# Demo Application Module

This module contains the backend packaging and the bundled frontend for the WSO2 Open Banking sample demonstrator.

## Purpose

- Package the backend Java application into a WAR file.
- Integrate the React frontend build into the backend webapp.
- Provide the deployment artifact consumed by the sandbox runtime.

## Directory Layout

- `frontend/` — frontend source and local build instructions
- `src/` — backend Java sources and resources
- `pom.xml` (in `demo-application/`) — build configuration for the module

## Backend Setup

This module is built by the parent Maven project in `demo-application/`.

### Recommended Build Command

From the repository root:

```bash
cd demo-application
mvn clean package -DskipTests
```

This performs the following:

- Installs frontend dependencies in `frontend/`
- Builds the React frontend assets
- Copies `frontend/dist/` into `src/main/webapp`
- Packages the backend and frontend into `target/api-ob-demo-1.0.0.war`

### Backend Code Changes

Modify backend behavior using the following paths:

- `src/main/java` — Java implementation code
- `src/main/resources` — backend configuration and resources
- `src/main/webapp` — deployed web application content

After changes, rebuild with Maven.

## Frontend Setup

The frontend has its own README in `frontend/`:

- `demo-application/components/ob_demo_application/frontend/README.md`

Use that file for frontend development, dependency management, and local Vite runtime instructions.

## Runtime Deployment

Once the WAR is built, the deployment workflow expects the artifact at:

- `configuration-files/api-ob-demo.war`

The root build script `build.sh` copies the generated WAR into that location automatically.

## Notes

- Use `configuration-files/` for runtime deployment overrides.
- Permanent database or runtime state changes should be backed up with a database dump before destroying containers or volumes.
