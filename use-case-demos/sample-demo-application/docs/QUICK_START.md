# Quick Start Guide

Get the WSO2 Open Banking Sample Demo Application running in under 10 minutes (excluding Docker image build time).

> For detailed instructions, see the [main README](../README.md). For architecture details, see [ARCHITECTURE.md](ARCHITECTURE.md).

---

## Prerequisites Checklist

Before you begin, ensure you have:

- [ ] **Docker Desktop** running (20.10+) with 8 GB+ RAM allocated
- [ ] **JDK 11+** installed (`java -version`)
- [ ] **Maven 3.6+** installed (`mvn -version`)
- [ ] **Python 3** installed (`python --version` or `python3 --version`)
- [ ] **Git** installed
- [ ] **WSO2 registry credentials** (for `registry.wso2.com`)

---

## Step 1: Clone & Navigate

```bash
git clone <repository-url>
cd bfsi-samples/use-case-demos/sample-demo-application
```

---

## Step 2: WSO2 Registry Login

```bash
docker login registry.wso2.com
# Enter your WSO2 credentials when prompted
```

---

## Step 3: Configure Host File

Add these entries to your hosts file:

**macOS / Linux** (`/etc/hosts`):
```bash
sudo sh -c 'echo "127.0.0.1   obiam" >> /etc/hosts'
sudo sh -c 'echo "127.0.0.1   obam" >> /etc/hosts'
```

**Windows** (run Notepad as Administrator, edit `C:\Windows\System32\drivers\etc\hosts`):
```
127.0.0.1   obiam
127.0.0.1   obam
```

Verify: `ping obiam` should resolve to `127.0.0.1`.

---

## Step 4: Build & Deploy

```bash
chmod +x build.sh    # macOS/Linux only
./build.sh
```

> **Python note**: If you get `python: command not found`, either run `alias python=python3` first, or edit line 28 of `build.sh` to use `python3`.

> **Windows**: Run this from **Git Bash** (not CMD or PowerShell).

This will take 15-30 minutes on first run. The script builds Docker images, compiles the application, and starts all services.

Wait for:
```
──────────────────────────────────────────
All done!
IS Console : https://obiam:9446/console
App URL    : https://obiam:9446/api-ob-demo-1.0.0
──────────────────────────────────────────
```

---

## Step 5: Access the Application

Open these URLs in your browser (accept certificate warnings):

| Application | URL | Login |
|------------|-----|-------|
| **Demo App** | https://obiam:9446/api-ob-demo-1.0.0 | `psu@gold.com` / `Wso21234` |
| **IS Console** | https://obiam:9446/console | `is_admin@wso2.com` / `wso2123` |
| **APIM Publisher** | https://obam:9443/publisher | `am_admin@wso2.com` / `wso2123` |
| **Consent Manager** | https://obiam:9446/consentmgr | `ann@gold.com` / `Ann@1234` |

---

## Step 6: Verify Deployment

```bash
# Quick health check
curl -k -s -o /dev/null -w "IS: %{http_code}\n" https://obiam:9446/carbon/admin/login.jsp
curl -k -s -o /dev/null -w "AM: %{http_code}\n" https://obam:9443/carbon/admin/login.jsp
curl -k -s -o /dev/null -w "App: %{http_code}\n" https://obiam:9446/api-ob-demo-1.0.0
```

All should return `200`.

---

## Shutdown

```bash
# Stop (preserve data)
cd docker-files/docker-compose && docker compose down

# Stop and delete all data
cd docker-files/docker-compose && docker compose down -v
```

---

## Common Issues

| Problem | Fix |
|---------|-----|
| `python: command not found` | `alias python=python3` or edit `build.sh` line 28 |
| `unauthorized` during Docker build | `docker login registry.wso2.com` |
| Browser can't reach `obiam` | Add host file entries (Step 3) |
| Certificate warnings | Click "Advanced" > "Proceed" -- self-signed certs are expected |
| Port already in use | `lsof -ti:9446 \| xargs kill -9` (replace port as needed) |
| Containers not starting | Check Docker has 8 GB+ RAM; run `docker logs obiam` for errors |

---

## Next Steps

- [Architecture Overview](ARCHITECTURE.md) -- Understand the system design
- [Developer Guide](DEVELOPER_GUIDE.md) -- Modify frontend/backend code, customize branding
- [Operations Guide](OPERATIONS_GUIDE.md) -- Docker commands, troubleshooting, monitoring
