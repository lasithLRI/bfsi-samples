# OpenSearch & OpenSearch Dashboards Setup Guide

This README provides instructions to download, install, and configure OpenSearch and OpenSearch Dashboards using the official sources.

---

## Prerequisites

* Java 17 or later
* Node.js (for OpenSearch Dashboards : version 20)
* Minimum 4GB RAM for stable operation

---

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

### Start OpenSearch

```bash
./bin/opensearch.bat
```

### Start OpenSearch Dashboards

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

> Use **Node.js v18** for optimal compatibility.

### Step 3: Add Your React Project

Place your plugin (React project) inside the `plugins` directory of OpenSearch Dashboards.

* Frontend UI components should go inside the `public` folder.
* Backend/server logic should go inside the `server` folder.

Ensure you use `App.tsx`, `index.ts`, and `plugin.ts` that follow the plugin structure and component usage patterns defined in the OpenSearch Dashboards project.

Your `package.json` should contain a build script like:

```json
"build": "yarn node ../../scripts/plugin_helpers build"
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
opensearch-dashboards-plugin.bat install file:///C:/Users/Ajai/Desktop/OpenSearch_Dashboards/OpenSearch-Dashboards/plugins/swift-dashboard-backend/build/swiftDashboard-2.19.0.zip
```

> Replace the path with the actual location of your built plugin zip.

Once installed, launch OpenSearch Dashboards. Your plugin should appear on the left-hand panel.

**Note:** Each time you make changes to your plugin:

* Rebuild the plugin (`yarn build`)
* Remove the previous version from the plugin directory before reinstalling

---

## Resources

* [OpenSearch Documentation](https://opensearch.org/docs/latest/)
* [OpenSearch Dashboards Docs](https://opensearch.org/docs/latest/dashboards/)
* [OpenSearch GitHub](https://github.com/opensearch-project/OpenSearch)
* [Dashboards GitHub](https://github.com/opensearch-project/OpenSearch-Dashboards)

---

> For production setup, refer to OpenSearch's security and performance tuning guides.
