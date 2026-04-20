# Docker Compose for WSO2 Open Banking Sandbox

This directory holds the Compose orchestration for the sample demo application that connects to the WSO2 Open Banking Sandbox Bank.

## Purpose

The Compose stack brings up the following services:

- `mysql` – local MySQL persistence for WSO2 and demo runtime data
- `obiam` – WSO2 Identity Server with the Open Banking IAM accelerator
- `obam` – WSO2 API Manager with the Open Banking API Management accelerator

## Assumptions

- `configuration-files/` contains all deployment overrides.
- `configuration-files/api-ob-demo.war` contains the demo accelerator WAR.
- The external network `ob-network` exists.
- No special manual runtime configuration is required beyond the override files in `configuration-files/`.

## Start the Stack

From the repository root or from `docker-files/docker-compose`:

```bash
cd docker-files/docker-compose
docker compose up -d
```

If `ob-network` does not exist:

```bash
docker network create ob-network
```

## Recommended Build Flow

The recommended workflow is to use the root `build.sh` script. It ensures the following:

- MySQL image is built from `docker-files/my_sql`
- The demo backend WAR is built and copied to `configuration-files/api-ob-demo.war`
- WSO2 IS and AM Docker images are built correctly
- The Compose deployment starts with the expected artifacts

## Runtime Access

Once the stack is running, access the services using these URLs:

- WSO2 API Manager: `https://obam:9443`
- Open Banking gateway: `https://obam:8243`
- WSO2 Identity Server / OBIAM Console: `https://obiam:9446/console`

## Using a New Accelerator WAR

If you build or obtain a new accelerator WAR, place it at:

- `configuration-files/api-ob-demo.war`

Then restart the Compose deployment to apply the new WAR.

## Persistence Note

The MySQL service stores state in a Docker volume named `mysql_data`.

> Important: if you apply permanent changes to the database or demo state, export a database dump before destroying the containers or volume. Otherwise, your state may be lost.

## Troubleshooting

If Compose fails to start because the network is missing, create `ob-network` manually. If the WAR is not deployed, verify that `configuration-files/api-ob-demo.war` exists and is up to date.
