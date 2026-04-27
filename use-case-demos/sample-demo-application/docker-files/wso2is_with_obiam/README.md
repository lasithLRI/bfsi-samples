# WSO2 Identity Server with OBIAM Accelerator - Dockerfile

Builds an Alpine Linux Docker image that deploys the WSO2 Financial Services Identity Access Management (OBIAM) Accelerator 4.0.0 on top of WSO2 Identity Server 7.1.0.

## What This Image Contains

- WSO2 Identity Server 7.1.0 (base product from `registry.wso2.com`)
- OBIAM Accelerator 4.0.0 (merged from `registry.wso2.com/wso2-ob/obiam-accelerator`)
- Custom keystores and certificates for sandbox hostnames (`obiam`, `obam`)
- MySQL JDBC connector
- `obiam-deployment.toml` configuration override
- Root and issuer CA certificates
- Demo application WAR (`api-ob-demo-1.0.0.war`)
- Consent Manager portal (hostname patched to `obiam`)

## Build Arguments

| Argument | Description | Example |
|----------|-------------|---------|
| `BASE_PRODUCT_VERSION` | WSO2 IS version | `7.1.0` |
| `OB_TRUSTED_CERTS_URL` | URL to `trust_certs.zip` | `http://host.docker.internal:8000/configuration-files/trust_certs.zip` |
| `WSO2_OB_KEYSTORES_URL` | URL to keystores directory | `http://host.docker.internal:8000/configuration-files/keystores` |
| `RESOURCE_URL` | Base URL for configuration files | `http://host.docker.internal:8000` |

## How to Build

### Recommended: Use `build.sh`

The root `build.sh` script handles everything automatically, including starting the HTTP server, building the WAR, and passing correct build arguments.

```bash
cd <repository-root>
./build.sh
```

### Manual Build

If you need to build this image manually:

1. Start an HTTP server from the repository root to serve configuration files:
   ```bash
   cd <repository-root>
   python -m http.server 8000 &
   ```

2. Build the image (from the repository root, so the demo WAR is in context):
   ```bash
   docker build \
       --build-arg BASE_PRODUCT_VERSION=7.1.0 \
       --build-arg OB_TRUSTED_CERTS_URL=http://host.docker.internal:8000/configuration-files/trust_certs.zip \
       --build-arg WSO2_OB_KEYSTORES_URL=http://host.docker.internal:8000/configuration-files/keystores \
       --build-arg RESOURCE_URL=http://host.docker.internal:8000 \
       -f docker-files/wso2is_with_obiam/Dockerfile \
       -t wso2is-ob:4.0.0 .
   ```

> **Important**: The build context must be the repository root so that the `demo-application/target/api-ob-demo-1.0.0.war` is accessible via the `COPY` instruction.

## Exposed Ports

| Port | Protocol | Purpose |
|------|----------|---------|
| 9446 | HTTPS | IS Console, Demo App, Consent Manager, OAuth endpoints |

## Multi-Stage Build

- **Stage 1** (`obiam`): Pulls the pre-built OBIAM accelerator image and verifies the accelerator directory.
- **Stage 2** (final): Merges the accelerator into the base IS image, imports certificates, applies configuration, and deploys the demo WAR.

## Prerequisites

- Docker 20.10+
- Authentication to `registry.wso2.com` (`docker login registry.wso2.com`)
- The demo WAR must be pre-built: `cd demo-application && mvn clean package -DskipTests`

## Further Reading

- [Main README](../../README.md)
- [Architecture Overview](../../docs/ARCHITECTURE.md)
- [Operations Guide](../../docs/OPERATIONS_GUIDE.md)
