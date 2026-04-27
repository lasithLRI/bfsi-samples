# Developer Guide

A comprehensive guide for developers working on the WSO2 Open Banking Sample Demo Application. Covers frontend and backend development workflows, configuration reference, branding customization, and local development setup.

---

## Table of Contents

1. [Development Environment Setup](#1-development-environment-setup)
2. [Frontend Development](#2-frontend-development)
3. [Backend Development](#3-backend-development)
4. [Configuration Reference](#4-configuration-reference)
5. [Branding & Theming](#5-branding--theming)
6. [Build Workflows](#6-build-workflows)
7. [Adding New Features](#7-adding-new-features)
8. [Code Architecture Conventions](#8-code-architecture-conventions)
9. [Deployment Configuration](#9-deployment-configuration)
10. [Accelerator WAR Replacement](#10-accelerator-war-replacement)

---

## 1. Development Environment Setup

### Required Tools

| Tool | Version | Purpose |
|------|---------|---------|
| JDK | 11+ | Backend compilation |
| Maven | 3.6+ | Build automation |
| Node.js | 22.12.0 | Frontend development (auto-installed by Maven, manual install for standalone dev) |
| pnpm | 9.15.0 | Package manager (auto-installed by Maven, manual install for standalone dev) |
| Docker Desktop | 20.10+ | Container runtime |
| IDE | IntelliJ IDEA / VS Code | Recommended editors |

### IDE Setup

**IntelliJ IDEA** (recommended for Java backend):
- Open `demo-application/` as a Maven project.
- Set Project SDK to JDK 11.
- Enable auto-import for Maven dependencies.

**VS Code** (recommended for React frontend):
- Open `demo-application/components/ob_demo_application/frontend/` as workspace.
- Install extensions: ESLint, Prettier, TypeScript.
- The workspace uses TypeScript strict mode.

### Standalone Frontend Development

If you want to run the React frontend independently (without Maven):

```bash
cd demo-application/components/ob_demo_application/frontend

# Install pnpm if not already installed
npm install -g pnpm@9.15.0

# Install dependencies
pnpm install

# Start Vite dev server
pnpm dev
```

The dev server starts at `http://localhost:5173`. Note that API calls will fail unless the backend services (OBIAM/OBAM) are running.

---

## 2. Frontend Development

### Source Code Location

```
demo-application/components/ob_demo_application/frontend/
├── public/
│   ├── configurations/config.json    # Bank data & UI configuration
│   └── resources/assets/images/      # Logos, icons, backgrounds
├── src/
│   ├── main.tsx                      # Entry point
│   ├── app.tsx                       # Router configuration
│   ├── root.tsx                      # Root component
│   ├── authConfig.ts                 # OAuth/Asgardeo configuration
│   ├── index.scss                    # Global styles
│   ├── components/                   # Reusable UI components
│   ├── hooks/                        # Custom React hooks
│   ├── layouts/                      # Page layouts
│   ├── pages/                        # Route-level page components
│   ├── providers/                    # Context providers
│   └── utility/                      # API client, formatters, utils
├── package.json
├── vite.config.ts
├── tsconfig.json
└── eslint.config.js
```

### Key Frontend Files

| File | Purpose |
|------|---------|
| `app.tsx` | React Router route definitions |
| `authConfig.ts` | Asgardeo OAuth2 configuration (client ID, redirect URIs, scopes) |
| `hooks/use-config-context.ts` | Global configuration context (banks, users, colors) |
| `hooks/use-config.ts` | Loads `config.json` at runtime |
| `utility/api.ts` | Axios HTTP client with base URL configuration |
| `providers/app-theme-provider.tsx` | MUI theme provider using config colors |

### Adding a New Page

1. Create a new page component under `src/pages/`:

```tsx
// src/pages/my-page/my-page.tsx
export default function MyPage() {
  return <div>My New Page</div>;
}
```

2. Add a route in `src/app.tsx`:

```tsx
<Route path={`/${appInfo.route}/my-page`} element={<MyPage />} />
```

3. If the page needs the standard layout, wrap it in the appropriate layout component.

### Adding a New Component

Place reusable components under `src/components/`. Follow the existing pattern:

```
src/components/
├── my-component/
│   ├── my-component.tsx
│   └── my-component.scss    # Optional component-specific styles
```

### API Client Usage

The API client is defined in `src/utility/api.ts`. Use it with React Query:

```tsx
import { useQuery } from "@tanstack/react-query";
import api from "../utility/api";

function useAccounts() {
  return useQuery({
    queryKey: ["accounts"],
    queryFn: () => api.get("/accounts").then(res => res.data),
  });
}
```

### Styling Approach

- **Global styles**: `src/index.scss`
- **Component styles**: Co-located `.scss` files
- **Theming**: MUI theme via `AppThemeProvider` -- colors come from `config.json`
- **CSS-in-JS**: Emotion (used by MUI internally)

### Frontend Build Commands

```bash
cd demo-application/components/ob_demo_application/frontend

pnpm dev          # Start dev server (hot reload)
pnpm build        # Production build (outputs to dist/)
pnpm lint         # Run ESLint
pnpm preview      # Preview production build locally
```

---

## 3. Backend Development

### Source Code Location

```
demo-application/components/ob_demo_application/src/main/
├── java/com/wso2/openbanking/demo/
│   ├── controller/
│   │   └── ApiController.java        # REST endpoint definitions
│   ├── service/
│   │   ├── AccountService.java       # AISP account operations
│   │   ├── PaymentService.java       # PISP payment operations
│   │   ├── AuthService.java          # OAuth flow orchestration
│   │   ├── OAuthTokenService.java    # Token endpoint communication
│   │   ├── JwtTokenService.java      # JWT creation & signing (PS256)
│   │   ├── HttpTlsClient.java        # mTLS HTTP client
│   │   ├── RequestObjectPayload.java # JWT request object builder
│   │   ├── ClientAssertionPayload.java # Client assertion builder
│   │   ├── JwtHeader.java            # JWT header builder
│   │   └── KeyReader.java            # Certificate file reader
│   ├── models/
│   │   ├── Account.java
│   │   ├── Payment.java
│   │   ├── Transaction.java
│   │   ├── StandingOrder.java
│   │   └── Payee.java
│   ├── constants/
│   │   ├── ApiConstants.java         # API response field names
│   │   └── OpenBankingConstants.java # OB specification constants
│   ├── security/
│   │   └── CorsFilter.java          # CORS configuration
│   ├── http/
│   │   ├── AuthUrlBuilder.java       # OAuth URL construction
│   │   ├── HttpConnection.java       # Base HTTP client
│   │   └── SSLContextFactory.java    # TLS context builder
│   ├── exceptions/                   # Custom exception types
│   └── utils/
│       ├── ConfigLoader.java         # Properties file loader
│       └── JwtUtils.java            # JWT utility methods
├── resources/
│   ├── application.properties        # Backend configuration
│   ├── obsigning.key                 # JWT signing key
│   ├── obtransport.key               # mTLS client key
│   └── obtransport.pem               # mTLS client certificate
└── webapp/
    └── WEB-INF/web.xml               # Servlet deployment descriptor
```

### REST API Endpoints

All endpoints are defined in `ApiController.java`:

```java
@Path("/")
public class ApiController {

    @POST @Path("/add-accounts")
    public Response addAccounts(Map<String, String> bankInfo);

    @POST @Path("/payment")
    public Response payment(Payment payment);

    @GET @Path("/processAuth")
    public Response processAuth(@QueryParam("code") String code);

    @DELETE @Path("/revoke-consent")
    public Response revokeConsent(
        @QueryParam("accountId") String accountId,
        @QueryParam("bankName") String bankName,
        @QueryParam("consentId") String consentId);
}
```

### Adding a New API Endpoint

1. Add the method to `ApiController.java`:

```java
@GET
@Path("/my-endpoint")
@Produces(MediaType.APPLICATION_JSON)
public Response myEndpoint() {
    // Implementation
    return Response.ok(result).build();
}
```

2. If business logic is complex, create a new service class under `service/`.

3. Rebuild:
```bash
cd demo-application
mvn clean package -DskipTests
```

### Backend Configuration

All configuration is in `src/main/resources/application.properties`:

```properties
# Identity Server
is.base.url=https://obiam:9446

# OAuth2
oauth.client.id=6LU91CbY4QsoPhpnW1hySYOfipQa
oauth.client.kid=sCekNgSWIauQ34klRhDGqfwpjc4
oauth.algorithm=PS256
oauth.token.type=JWT
oauth.token.url=https://obiam:9446/oauth2/token
oauth.authorize.url=https://obiam:9446/oauth2/authorize
oauth.redirect.uri=https://obiam:9446/api-ob-demo-1.0.0/callback
oauth.response.type=code id_token

# Open Banking APIs
openbanking.account.base.url=https://obam:8243/open-banking/v3.1/aisp
openbanking.payment.base.url=https://obam:8243/open-banking/v3.1/pisp
openbanking.fapi.financial.id=open-bank

# SSL/TLS
ssl.certificate.path=/obtransport.pem
ssl.key.path=/obtransport.key
ssl.truststore.path=/client-truststore.jks
ssl.truststore.password=123456

# URLs
backend.base.url=https://obiam:9446/api-ob-demo-1.0.0/init
frontend.home.url=https://obiam:9446/api-ob-demo-1.0.0
```

### Backend Build Commands

```bash
cd demo-application

# Full build (frontend + backend)
mvn clean package -DskipTests

# Build with debug output
mvn clean package -DskipTests -X

# Skip frontend build (backend-only changes)
# Not supported -- frontend build is integrated into the Maven POM.
# The Maven build always triggers the frontend build.
```

The generated WAR is at: `demo-application/target/api-ob-demo-1.0.0.war`

---

## 4. Configuration Reference

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `is.base.url` | `https://obiam:9446` | WSO2 IS base URL |
| `oauth.client.id` | `6LU91CbY...` | OAuth2 client ID registered in IS |
| `oauth.client.kid` | `sCekNgSW...` | Key ID for JWT signing |
| `oauth.algorithm` | `PS256` | JWT signing algorithm |
| `oauth.token.type` | `JWT` | Token format |
| `oauth.token.url` | `https://obiam:9446/oauth2/token` | Token endpoint |
| `oauth.authorize.url` | `https://obiam:9446/oauth2/authorize` | Authorization endpoint |
| `oauth.redirect.uri` | `https://obiam:9446/api-ob-demo-1.0.0/callback` | OAuth redirect URI |
| `oauth.response.type` | `code id_token` | OAuth response type (hybrid flow) |
| `openbanking.account.base.url` | `https://obam:8243/open-banking/v3.1/aisp` | AISP API |
| `openbanking.payment.base.url` | `https://obam:8243/open-banking/v3.1/pisp` | PISP API |
| `openbanking.fapi.financial.id` | `open-bank` | FAPI financial ID header |
| `ssl.certificate.path` | `/obtransport.pem` | mTLS client certificate |
| `ssl.key.path` | `/obtransport.key` | mTLS private key |
| `ssl.truststore.path` | `/client-truststore.jks` | Java truststore |
| `ssl.truststore.password` | `123456` | Truststore password |
| `backend.base.url` | `https://obiam:9446/api-ob-demo-1.0.0/init` | Backend init URL |
| `frontend.home.url` | `https://obiam:9446/api-ob-demo-1.0.0` | Frontend home URL |

### Frontend Config (config.json)

| Section | Purpose |
|---------|---------|
| `user` | Demo user profile (name, avatar, background) |
| `name` | Application route and display name |
| `banks[]` | Bank definitions with accounts, transactions, standing orders |
| `payees[]` | Payment recipient list |
| `types[]` | Use case flow definitions (account, payment) |
| `transactionTableHeaderData` | Column definitions for transaction tables |
| `standingOrdersTableHeaderData` | Column definitions for standing order tables |
| `colors[]` | Theme color palette |
| `accountNumbersToAdd` | Account number suffixes for new accounts |

### WSO2 Deployment TOMLs

#### OBIAM Configuration (`configuration-files/obiam-deployment.toml`)

| Section | Key Settings |
|---------|-------------|
| `[server]` | Hostname: `obiam`, Port offset: 1 (9446) |
| `[super_admin]` | Username: `is_admin@wso2.com`, Password: `wso2123` |
| `[database.*]` | MySQL connections for identity, config, user, consent DBs |
| `[oauth]` | PS256 signing, self-contained JWT tokens, hybrid flow |
| `[open_banking.consent]` | Consent scopes: accounts, payments, fundsconfirmations |
| `[authentication]` | FAPI compliance enforcement |

#### OBAM Configuration (`configuration-files/obam-deployment.toml`)

| Section | Key Settings |
|---------|-------------|
| `[server]` | Hostname: `obam`, Ports: 9443, 8243, 8280 |
| `[super_admin]` | Username: `am_admin@wso2.com`, Password: `wso2123` |
| `[database.*]` | MySQL connections for API management, config, user DBs |
| `[apim.cors]` | CORS configuration |
| `[service_provider]` | IS service URL: `https://obiam:9446/services/` |

---

## 5. Branding & Theming

### Logo Replacement

Logos are stored at:
```
frontend/public/resources/assets/images/logos/
├── assend_global_bank_logo.png    # Bank 1
├── global_asset_trust_logo.png    # Bank 2
└── trust_union_logo.png           # Bank 3
```

To replace a bank's logo:
1. Create your logo image (PNG recommended, transparent background).
2. Name it to match the existing filename you're replacing.
3. Drop it into the logos directory.
4. Rebuild with `./build.sh` or `mvn clean package -DskipTests`.

### Bank Names & Colors

Edit `frontend/public/configurations/config.json`. Each bank entry:

```json
{
  "name": "Your Bank Name",
  "image": "./resources/assets/images/logos/your_logo.png",
  "currency": "USD",
  "color": "#003366",
  "border": "#003366",
  "route": "your-bank-slug",
  "bankThemeId": 1,
  "startingAccountNumbers": "0066"
}
```

### Application Name

```json
{
  "name": {
    "route": "accounts-central",
    "applicationName": "Your App Name"
  }
}
```

### Color Theme

The `colors` array in `config.json` controls the entire UI palette:

| Key | Purpose | Default |
|-----|---------|---------|
| `primary` | Primary brand color | `#FF5100` |
| `secondaryColor` | Secondary accent | `#EAA340` |
| `backgroundColor` | Page background | `#FFF5EE` |
| `button` | Button text color | `#FFFFFF` |
| `tableBackground` | Table cell background | `#FFFFFF` |
| `tableHeaderBackground` | Table header background | `#F6F6F7` |
| `tableHeaderFontColor` | Table header text | `#6B7280` |
| `greenArrowColor` | Credit indicator | `#2ecc71` |
| `redArrowColor` | Debit indicator | `#c0392b` |
| `formValidationError` | Form error color | `#c0392b` |

### Background & Profile Images

| Asset | Path | Format |
|-------|------|--------|
| Dashboard background | `frontend/public/resources/assets/images/background/bg-image.webp` | WebP |
| User profile picture | `frontend/public/resources/assets/images/profile/dp_image.webp` | WebP |

Replace with same filename and format, then rebuild.

### Sample Account Data

To customize demo accounts, transactions, and standing orders, edit the `banks[].accounts[]`, `banks[].accounts[].transactions[]`, and `banks[].standingOrders[]` arrays in `config.json`.

---

## 6. Build Workflows

### Full Build (Recommended)

```bash
./build.sh
```

This builds everything from scratch: frontend, backend, Docker images, and starts the stack.

### Rebuild After Code Changes

After modifying frontend or backend code:

```bash
# Rebuild the WAR
cd demo-application
mvn clean package -DskipTests

# Copy WAR to deployment location
cp target/api-ob-demo-1.0.0.war ../configuration-files/api-ob-demo.war

# Rebuild and restart only the IS container (which hosts the demo app)
cd ../docker-files/docker-compose
docker compose down
cd ../..
# Rebuild the IS image with the new WAR
docker build \
    --build-arg BASE_PRODUCT_VERSION=7.1.0 \
    --build-arg RESOURCE_URL=<http-server-url> \
    -f docker-files/wso2is_with_obiam/Dockerfile \
    -t wso2is-ob:4.0.0 .
cd docker-files/docker-compose
docker compose up -d
```

Or simply re-run `./build.sh` for a clean rebuild.

### Frontend-Only Development

For rapid frontend iteration without full rebuilds:

```bash
cd demo-application/components/ob_demo_application/frontend
pnpm dev
```

This starts Vite's dev server with hot module replacement at `http://localhost:5173`. Note: OAuth flows and API calls require the full Docker stack running.

---

## 7. Adding New Features

### Adding a New Open Banking API

1. **Define constants** in `OpenBankingConstants.java`:
```java
public static final String NEW_API_PATH = "/new-api/";
```

2. **Create a service** under `service/`:
```java
public class NewApiService {
    // API call logic using HttpTlsClient
}
```

3. **Add an endpoint** in `ApiController.java`.

4. **Add frontend page** under `src/pages/` and register the route in `app.tsx`.

5. **Rebuild**: `mvn clean package -DskipTests` then `./build.sh`.

### Adding a New Bank

1. Add a bank entry to `config.json` under `banks[]`.
2. Place the bank's logo in `frontend/public/resources/assets/images/logos/`.
3. Rebuild.

### Modifying OAuth Flows

OAuth configuration is split across:
- `application.properties` (backend OAuth settings)
- `authConfig.ts` (frontend Asgardeo settings)
- `obiam-deployment.toml` (IS-side OAuth configuration)

Changes to OAuth flows may require updating all three files and rebuilding both the WAR and the IS Docker image.

---

## 8. Code Architecture Conventions

### Frontend Conventions

- **File naming**: `kebab-case` for files and directories (`my-component.tsx`).
- **Component naming**: PascalCase for React components (`MyComponent`).
- **Hooks**: Prefix with `use` (`useConfig`, `useConfigContext`).
- **Styles**: Co-located SCSS files with the same name as the component.
- **State**: Prefer React Query for server state, React Context for app config.

### Backend Conventions

- **Package structure**: Domain-driven (`controller`, `service`, `models`, `http`, `security`).
- **REST endpoints**: Defined in `ApiController.java` with JAX-RS annotations.
- **Service layer**: One service per domain concern (`AccountService`, `PaymentService`).
- **Configuration**: Loaded from `application.properties` via `ConfigLoader`.
- **Error handling**: Custom exceptions (`AuthorizationException`, `PaymentException`).

---

## 9. Deployment Configuration

### Overriding WSO2 Configurations

All runtime deployment overrides are centrally managed in `configuration-files/`:

| File | Purpose |
|------|---------|
| `obiam-deployment.toml` | WSO2 IS configuration (OAuth, databases, consent) |
| `obam-deployment.toml` | WSO2 AM configuration (gateway, APIs, databases) |
| `customErrorFormatter.xml` | API error response formatting |
| `mysql-connector-java-5.1.44.jar` | MySQL JDBC driver |
| `trust_certs.zip` | CA certificates |
| `keystores/private-keys.jks` | Server private keys |
| `keystores/public-certs.jks` | Server public certificates |

To change a deployment parameter:
1. Edit the relevant TOML file.
2. Rebuild the Docker image (`./build.sh` or manual Docker build).
3. Restart the stack.

### Changing Admin Passwords

Edit the `[super_admin]` section in the deployment TOML files:

**OBIAM** (`obiam-deployment.toml`):
```toml
[super_admin]
username = "is_admin@wso2.com"
password = "your-new-password"
```

**OBAM** (`obam-deployment.toml`):
```toml
[super_admin]
username = "am_admin@wso2.com"
password = "your-new-password"
```

After changing, rebuild Docker images and restart.

---

## 10. Accelerator WAR Replacement

### Replacing the Demo App WAR

The demo app WAR is deployed to the WSO2 IS container. To replace it:

1. Build or obtain the new WAR file.
2. Copy it to the expected location:
```bash
cp your-new-app.war configuration-files/api-ob-demo.war
```
3. Rebuild the IS Docker image and restart.

### Replacing the Accelerator Backend WAR

The `api#fs#backend.war` is the Open Banking accelerator's backend. To replace it:

1. Place the new WAR at `configuration-files/api#fs#backend.war`.
2. Rebuild the Docker images.

> **Warning**: Replacing accelerator WARs may break compatibility. Only do this if you are updating to a compatible accelerator version.
