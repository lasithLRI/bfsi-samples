# TPP Frontend Demo Application

This frontend is the TPP demo client for the WSO2 Open Banking Sandbox. It renders the user-facing banking experience and is packaged into the backend WAR during the Maven build.

## Purpose

- Demonstrates the TPP workflow for Open Banking.
- Provides a React/Vite demo application that can be updated independently.
- Serves as the frontend layer for the backend WAR deployed through WSO2.

## Key Files

- `src/` — React application source code
- `public/` — static assets and the demo configuration file
- `public/configurations/config.json` — sample application configuration data
- `vite.config.ts` — Vite development server configuration
- `package.json` / `pnpm-lock.yaml` — frontend dependency manifests

## Local Setup

### Prerequisites

- Node.js 22.12.0 (or compatible; auto-installed by Maven during WAR build)
- pnpm 9.15.0 (auto-installed by Maven during WAR build)
- A compatible shell environment on Windows (Git Bash or WSL is recommended)

### Install Dependencies

From the frontend directory:

```bash
cd demo-application/components/ob_demo_application/frontend
pnpm install
```

### Run Locally

Start the local development server:

```bash
pnpm run dev
```

Open the URL shown in the terminal, typically `http://localhost:5173`.

### Build for Production

Generate production assets:

```bash
pnpm run build
```

The compiled output is written to `dist/`.

## Deployment Notes

- The frontend is packaged automatically when the backend Maven build runs.
- The root build script `build.sh` handles the complete integration path and copies the generated WAR to `configuration-files/api-ob-demo.war`.
- Do not rely on local development settings for sandbox deployment. The sandbox runtime configuration is managed in `configuration-files/` at the repository root.

## Configuration

Sample UI and demo data values are stored in `public/configurations/config.json`.

- Change UI behavior and sample content here for local demo validation.
- Keep sandbox endpoint overrides in the root `configuration-files/` folder.

## When to Rebuild

Rebuild the frontend if you change:

- React source files under `src/`
- UI layout or static assets
- `public/configurations/config.json`

Then either run `pnpm run build` locally or rebuild the backend WAR with Maven, which also builds the frontend automatically.
