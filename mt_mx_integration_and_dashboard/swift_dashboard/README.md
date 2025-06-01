# OpenSearch & OpenSearch Dashboards Setup Guide

This README provides instructions to download, install, and configure OpenSearch and OpenSearch Dashboards using the official sources.

---

## Prerequisites

* Java 17 or later
* Window 10 or 11
* Node.js (for OpenSearch Dashboards : version 20)
* Minimum 4GB RAM for stable operation

---

## Developement Setup

## 1. Download OpenSearch (Version 2.19.0)

Visit the official OpenSearch download page: [https://opensearch.org/downloads/](https://opensearch.org/downloads/)

---

## 2. Download OpenSearch Dashboards (Version 2.19.0)

Visit the official OpenSearch Dashboards download page: [https://opensearch.org/downloads/#opensearch-dashboards](https://opensearch.org/downloads/#opensearch-dashboards)

---

## 3. Configure OpenSearch

Edit `config/opensearch.yml`:

```yaml
cluster.name: opensearch-cluster
node.name: node-1
network.host: 0.0.0.0
http.port: 9200
discovery.seed_hosts: ["127.0.0.1"]
cluster.initial_master_nodes: ["node-1"]
plugins.security.disabled: true
```

> Note: Only disable security for local development.

---

## 4. Configure OpenSearch Dashboards

Edit `config/opensearch_dashboards.yml`:

```yaml
opensearch.hosts: [https://localhost:9200]
opensearch.ssl.verificationMode: none
opensearch.username: kibanaserver
opensearch.password: kibanaserver
opensearch.requestHeadersWhitelist: [authorization, securitytenant]

opensearch_security.multitenancy.enabled: true
opensearch_security.multitenancy.tenants.preferred: [Private, Global]
opensearch_security.readonly_mode.roles: [kibana_read_only]
# Use this setting if you are running opensearch-dashboards without https
opensearch_security.cookie.secure: false
```

---

## 5. Start Services

### Start OpenSearch in Windows

```bash
./bin/opensearch.bat
```

### Start OpenSearch Dashboards in Windows

```bash
./bin/opensearch-dashboards.bat
```

Access Dashboards at [http://localhost:5601](http://localhost:5601)

---

## 6. Verify Setup

1. Open your browser and go to `http://localhost:5601`
2. You should see the OpenSearch Dashboards UI
3. Create indices, build visualizations, or explore logs

---

## 7. Add a Custom Plugin to OpenSearch Dashboards

### Step 1: Clone the OpenSearch Dashboards Repository

```bash
git clone https://github.com/opensearch-project/OpenSearch-Dashboards.git
cd OpenSearch-Dashboards
```

### Step 2: Bootstrap the OpenSearch Dashboards Development Environment

```bash
yarn osd bootstrap
```

> Use **Node.js v20** for optimal compatibility.

### Step 3: Add Your React Project

Place your plugin (React project) inside the `plugins` directory of OpenSearch Dashboards.

* Frontend UI components should go inside the `public` folder.
* Backend/server logic should go inside the `server` folder.

Ensure you use `App.tsx`, `index.ts`, and `plugin.ts` that follow the plugin structure and component usage patterns defined in the OpenSearch Dashboards project.

Your `package.json` should contain a build script like:

```json
"build": "yarn node ../../scripts/plugin_helpers build"
```

You should have a file named `opensearch_dashboards.json` which will contain information regarding the version of the OpenSearch Dashboards and the plugin.

```json
{
  "id": "swift-dashboard",
  "version": "1.0.0",
  "opensearchDashboardsVersion": "2.19.0",
  "server": true,
  "ui": true,
  "requiredPlugins": ["navigation"],
  "optionalPlugins": ["data", "savedObjects"]
}
```

> Follow the existing plugin structure carefully for consistency and component usage patterns.

### Step 4: Build the Plugin

Before building, install dependencies:

```bash
cd plugins/your-plugin-name
yarn install
```

Then build your plugin:

```bash
yarn build
```

### Step 5: Install the Built Plugin into a Packaged Dashboard

Use the `opensearch-dashboards-plugin` utility inside the downloaded OpenSearch Dashboards package to install the plugin zip:

```bash
bin\opensearch-dashboards-plugin.bat install file://./plugins/swift-dashboard-backend/build/swiftDashboard-2.19.0.zip
```

> Replace the path with the actual location of your built plugin zip.

Once installed, launch OpenSearch Dashboards. Your plugin should appear on the left-hand panel.

![Dashboard Screenshot](../images/opensearch-dashboards-plugin.png)


**Note:** Each time you make changes to your plugin:

* Rebuild the plugin (`yarn build`)
* Remove the previous version from the plugin directory before reinstalling

---


## Deployement Setup

## 1. Download OpenSearch (Version 2.19.0)

Visit the official OpenSearch download page: [https://opensearch.org/downloads/](https://opensearch.org/downloads/)

---

## 2. Download OpenSearch Dashboards (Version 2.19.0)

Visit the official OpenSearch Dashboards download page: [https://opensearch.org/downloads/#opensearch-dashboards](https://opensearch.org/downloads/#opensearch-dashboards)

---

## 3. Configure OpenSearch

Edit `config/opensearch.yml`:

```yaml
########################### Cluster ###########################
cluster.name: opensearch-prod-cluster

############################ Node #############################
node.name: node-1
node.roles: [data, master, ingest]

########################## Network ############################
network.host: 0.0.0.0  # Or better: ["_eth0_", "_local_"]
http.port: 9200

########################### Discovery #########################
discovery.seed_hosts: ["node-1.internal", "node-2.internal", "node-3.internal"]
cluster.initial_master_nodes: ["node-1", "node-2", "node-3"]

######################### Path Settings #######################
path.data: /var/lib/opensearch
path.logs: /var/log/opensearch

######################### Security ############################
plugins.security.disabled: false
plugins.security.ssl.transport.enabled: true
plugins.security.ssl.transport.pemcert_filepath: node-1.pem
plugins.security.ssl.transport.pemkey_filepath: node-1-key.pem
plugins.security.ssl.transport.pemtrustedcas_filepath: root-ca.pem
plugins.security.ssl.transport.enforce_hostname_verification: false

plugins.security.ssl.http.enabled: true
plugins.security.ssl.http.pemcert_filepath: node-1.pem
plugins.security.ssl.http.pemkey_filepath: node-1-key.pem
plugins.security.ssl.http.pemtrustedcas_filepath: root-ca.pem

plugins.security.allow_default_init_securityindex: true

########################### Authentication ####################
plugins.security.authcz.admin_dn:
  - "CN=admin,OU=SSL,O=Test,L=Test,C=DE"

plugins.security.nodes_dn:
  - "CN=node-1,OU=SSL,O=Test,L=Test,C=DE"
  - "CN=node-2,OU=SSL,O=Test,L=Test,C=DE"
  - "CN=node-3,OU=SSL,O=Test,L=Test,C=DE"

########################### Performance #######################
bootstrap.memory_lock: true
indices.query.bool.max_clause_count: 10240
action.destructive_requires_name: true
```

> Note: Only disable security for local development.

---

## 4. Configure OpenSearch Dashboards

Edit `config/opensearch_dashboards.yml`:

```yaml
########################## OpenSearch Connection ##########################
opensearch.hosts: ["https://opensearch-node1.example.com:9200"]
opensearch.ssl.verificationMode: full  # full = verify cert and hostname
opensearch.username: "kibanaserver"
opensearch.password: "your-secure-password"
opensearch.requestHeadersWhitelist: [authorization, securitytenant]

########################## Server Settings ###############################
server.host: "0.0.0.0"  # Or set to internal IP/domain
server.port: 5601
server.ssl.enabled: true
server.ssl.certificate: /etc/opensearch-dashboards/certs/dashboards.pem
server.ssl.key: /etc/opensearch-dashboards/certs/dashboards-key.pem

########################## Security Plugin ###############################
opensearch_security.multitenancy.enabled: true
opensearch_security.multitenancy.tenants.preferred: [Private, Global]
opensearch_security.readonly_mode.roles: ["kibana_read_only"]

# Make sure cookies are secure in production
opensearch_security.cookie.secure: true

########################## Logging (Optional) #############################
logging.dest: /var/log/opensearch-dashboards/opensearch-dashboards.log
logging.verbose: false
```

---

## 5. Start Services

### Start OpenSearch in Windows

```bash
./bin/opensearch.bat
```

### Start OpenSearch Dashboards in Windows

```bash
./bin/opensearch-dashboards.bat
```

Access Dashboards at [http://localhost:5601](http://localhost:5601)

---

## 6. Verify Setup

1. Open your browser and go to `http://localhost:5601`
2. You should see the OpenSearch Dashboards UI
3. Create indices, build visualizations, or explore logs

---

## 7. Add a Custom Plugin to OpenSearch Dashboards

### Step 1: Clone the OpenSearch Dashboards Repository

```bash
git clone https://github.com/opensearch-project/OpenSearch-Dashboards.git
cd OpenSearch-Dashboards
```

### Step 2: Bootstrap the OpenSearch Dashboards Development Environment

```bash
yarn osd bootstrap
```

> Use **Node.js v20** for optimal compatibility.

### Step 3: Place the Built Plugin in the Plugin Folder

### Step 4: Install the Built Plugin into a Packaged Dashboard

Use the `opensearch-dashboards-plugin` utility inside the downloaded OpenSearch Dashboards package to install the plugin zip:

```bash
bin\opensearch-dashboards-plugin.bat install file://./plugins/swift-dashboard-backend/build/swiftDashboard-2.19.0.zip
```

> Replace the path with the actual location of your built plugin zip.

Once installed, launch OpenSearch Dashboards. Your plugin should appear on the left-hand panel.

![Dashboard Screenshot](../images/opensearch-dashboards-plugin.png)

---

## Resources

* [OpenSearch Documentation](https://opensearch.org/docs/latest/)
* [OpenSearch Dashboards Docs](https://opensearch.org/docs/latest/dashboards/)
* [OpenSearch GitHub](https://github.com/opensearch-project/OpenSearch)
* [Dashboards GitHub](https://github.com/opensearch-project/OpenSearch-Dashboards)

---

> For production setup, refer to OpenSearch's security and performance tuning guides. [https://docs.opensearch.org/docs/latest/security/] (https://docs.opensearch.org/docs/latest/security/)


# Federated Login Setup for OpenSearch Plugin using Asgardeo (OIDC)

This guide walks you through configuring federated login for OpenSearch using OpenID Connect (OIDC) with [Asgardeo](https://wso2.com/asgardeo/). You'll learn how to:

* Register an application in Asgardeo
* Enable required protocol settings (including hybrid flow)
* Configure scopes and user attributes
* Create users and roles
* Assign roles to users

---

## Prerequisites

* A working OpenSearch instance.
* An Asgardeo account ([https://console.asgardeo.io/](https://console.asgardeo.io/)).
* Administrator access to both Asgardeo and OpenSearch.

---

## 1. Create an Application in Asgardeo

1. Log in to the Asgardeo Console.
2. Navigate to **"Applications"** in the left menu.
3. Click **"New Application"**.
4. Choose **"Traditional Web Application"**.
5. Enter a name for your application (e.g., `OpenSearch Federation`).
6. Choose OpenId Connect
7. Enter the Redirect URL (http://localhost:5601/auth/openid/login) 
7. Click **"Create"**.

![App Creation Screenshot](../images/app_creation.png)

---

## 2. Configure Protocol Settings

Once the app is created, configure OIDC protocol settings.

### Under **Protocol** tab:

1. In the allowed origins section add the HTTP origins that host your OpenSearch application. (http://localhost:5601)
2. Enable the Hybrid Flow and select `code id_token` option under it.
3. Choose JWT as the token type under Access Token section
4. Add the Back channel logout URL in the Logout URLs section

![Allow Cors Screenshot](../images/allow_cors.png)

![Hybrid_Flow Screenshot](../images/hybrid_flow.png)

---

## 3. Configure User Attributes (Scopes)

Navigate to the **"User Attributes"** tab:

1. Under **Scopes**:

   * Enable the **"role"** scope – this will expose the roles assigned to the user, allowing OpenSearch to map them.

![Enable Scopee Screenshot](../images/user_attribute.png)

2. Under **Subject Attribute**:

   * Set it to **"username"** – this will be used as the authenticated user's identity.

![Subject Screenshot](../images/subject.png)

---

## 4. Create Roles in Asgardeo

1. Go to the **"Roles"** in the User Management section in the left panel.
2. Click **"New Role"**.
3. Enter a role name (e.g., `os-admin`, `os-analyst`, etc.).
4. Choose role audience.
5. Choose the application to which the role should be assigned.
6. Select API Resource as Application Management API
7. Click **"Finish"**.

---

## 5. Create Users in Asgardeo

1. Go to **"Users"** in the User Management section in the left panel.
2. Click **"Add User"**.
3. Fill in user details.
4. Click **"Finish"**.

---

## 6. Assign Roles to Users in Asgardeo

1. Go to **"Roles"** in the User Management section in the left panel.
2. Select the role to be assigned.
3. Navigate to the **"Users"** section.
4. Click **"Assign User"**
5. Select the user to be assigned.
4. Click **"Save"**.

---

## 7. Update OpenSearch Configuration

In OpenSearch navigate to the opensearch-security folder inside the config folder:

* Navigate to config.yml and set the following OIDC parameters:

```yaml
_meta:
  type: "config"
  config_version: 2

config:
  dynamic:
    http:
      anonymous_auth_enabled: false
    authc:
      basic_internal_auth_domain:
        http_enabled: true
        transport_enabled: true
        order: 0
        http_authenticator:
          type: basic
          challenge: false
        authentication_backend:
          type: internal
      openid_auth_domain:
        http_enabled: true
        transport_enabled: false
        order: 1
        http_authenticator:
          type: openid
          challenge: false
          config:
            subject_key: sub
            roles_key: roles
            openid_connect_url: https://api.asgardeo.io/t/<org-name>/oauth2/token/.well-known/openid-configuration
            jwt_header: Authorization
        authentication_backend:
          type: noop
```

> Note: It is required to have `basic_internal_auth_domain` to connect OpenSearch Dashboards to OpenSearch.

* Navigate to the roles_mapping.yml and define roles based on use case:

```yaml
Admin:
  reserved: false
  hidden: false
  backend_roles:
  - "Admin"
  users: []
  hosts: []
  and_backend_roles: []
  description: "Maps admin to all_access"
```

> Note: The name **"Admin"** at the top is the role that will be assigned in OpenSearch and the name **"Admin"** under backend_roles is the role from the openid(Asgardeo App) 

* Navigate to the roles.yml and define permissions given to each roles based on use case:

```yaml
Admin:
  reserved: false
  hidden: false
  cluster_permissions:
  - "cluster_monitor"
  - "read"
  - "indices:monitor/settings/get"
  index_permissions:
  - index_patterns: [
     "*"]
    allowed_actions: [
      "read",
      "indices_monitor",
      "indices:monitor/*",
      "index",
      "search"]
  tenant_permissions:
  - tenant_patterns: [
     "global tenant" ]
    allowed_actions: [
      "kibana_all_read"]
  static: false
```
> Note: In order for OpenSearch to pick the changes made in these files the below command has to run while the OpenSearch instance is running.

```bash
cd plugins\opensearch-security\tools

securityadmin.bat -cacert ../../../config/root-ca.pem -cert ../../../config/kirk.pem -key ../../../config/kirk-key.pem -cd ../../../config/opensearch-security
```
---

## 8. Update OpenSearch Dashboards Configuration

* Navigate to opensearch_dashboards.yml and set the following configurations:

```yaml
opensearch_security.auth.type: "openid"
opensearch_security.openid.header: "Authorization"
opensearch_security.openid.connect_url: "https://api.asgardeo.io/t/<org-name>/oauth2/token/.well-known/openid-configuration"
opensearch_security.openid.client_id: "client_id"
opensearch_security.openid.client_secret: "client_secret"
opensearch_security.openid.scope: "openid profile roles"
opensearch_security.openid.base_redirect_url: "http://localhost:5601"
opensearch_security.openid.logout_url: "https://api.asgardeo.io/t/<org-name>/oidc/logout"
```

Make sure to replace placeholders with actual values from your Asgardeo app.

## 9. Test the Integration

1. Restart OpenSearch Dashboards if needed.
2. Open OpenSearch Dashboards in a browser.
3. You should be redirected to Asgardeo for login.
4. Log in with a test user.
5. Verify that roles are passed and that access is granted accordingly. This can be done by clicking the profile and then clicking the **"View roles and identities"** where you will be able to see the role assigned for the logged in user.

![Role Check Screenshot](../images/role_check.png)

---

## Resources

* [Asgardeo Documentation](https://wso2.com/asgardeo/docs/)
* [OpenSearch Security Plugin](https://opensearch.org/docs/latest/security-plugin/)
