# WSO2 API Manager with OBAM Accelerator - Dockerfile

Builds an Alpine Linux Docker image that deploys the WSO2 Financial Services API Management (OBAM) Accelerator 4.0.0 on top of WSO2 API Manager 4.5.0.

## What This Image Contains

- WSO2 API Manager 4.5.0 (base product from `registry.wso2.com`)
- OBAM Accelerator 4.0.0 (merged from `registry.wso2.com/wso2-ob/obam-accelerator`)
- Custom keystores and certificates for sandbox hostnames (`obiam`, `obam`)
- MySQL JDBC connector
- `obam-deployment.toml` configuration override
- Custom error formatter XML for API error responses
- CORS sequence configuration
- Root and issuer CA certificates
- IS service URL patched to point to `obiam`

## Build Arguments

| Argument | Description | Example |
|----------|-------------|---------|
| `BASE_PRODUCT_VERSION` | WSO2 AM version | `4.5.0` |
| `OB_TRUSTED_CERTS_URL` | URL to `trust_certs.zip` | `http://host.docker.internal:8000/configuration-files/trust_certs.zip` |
| `WSO2_OB_KEYSTORES_URL` | URL to keystores directory | `http://host.docker.internal:8000/configuration-files/keystores` |
| `RESOURCE_URL` | Base URL for configuration files | `http://host.docker.internal:8000` |

## How to Build

### Recommended: Use `build.sh`

The root `build.sh` script handles everything automatically, including starting the HTTP server and passing correct build arguments.

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

2. Build the image (from the repository root):
   ```bash
   docker build \
       --build-arg BASE_PRODUCT_VERSION=4.5.0 \
       --build-arg OB_TRUSTED_CERTS_URL=http://host.docker.internal:8000/configuration-files/trust_certs.zip \
       --build-arg WSO2_OB_KEYSTORES_URL=http://host.docker.internal:8000/configuration-files/keystores \
       --build-arg RESOURCE_URL=http://host.docker.internal:8000 \
       -f docker-files/wso2am_with_obam/Dockerfile \
       -t wso2am-ob:4.0.0 .
   ```

> **Important**: The build context must be the repository root so that configuration files are accessible.

## Exposed Ports

| Port | Protocol | Purpose |
|------|----------|---------|
| 9443 | HTTPS | AM Admin Console, Publisher Portal |
| 8243 | HTTPS | API Gateway (data plane, mTLS) |
| 8280 | HTTP | API Gateway (HTTP pass-through) |

## Multi-Stage Build

- **Stage 1** (`obam`): Pulls the pre-built OBAM accelerator image and verifies the accelerator directory.
- **Stage 2** (final): Merges the accelerator into the base AM image, imports certificates, applies configuration, and patches the IS service URL.

## Prerequisites

- Docker 20.10+
- Authentication to `registry.wso2.com` (`docker login registry.wso2.com`)

## Further Reading

- [Main README](../../README.md)
- [Architecture Overview](../../docs/ARCHITECTURE.md)
- [Operations Guide](../../docs/OPERATIONS_GUIDE.md)
