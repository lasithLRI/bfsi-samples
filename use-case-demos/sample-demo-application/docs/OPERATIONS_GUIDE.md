# Operations & Troubleshooting Guide

A complete reference for operating, monitoring, debugging, and recovering the WSO2 Open Banking Sample Demo Application deployment.

---

## Table of Contents

1. [Docker Operations Reference](#1-docker-operations-reference)
2. [Container Management](#2-container-management)
3. [Log Analysis](#3-log-analysis)
4. [Health Monitoring](#4-health-monitoring)
5. [Database Operations](#5-database-operations)
6. [Troubleshooting Guide](#6-troubleshooting-guide)
7. [Verification & Smoke Tests](#7-verification--smoke-tests)
8. [Shutdown & Cleanup Procedures](#8-shutdown--cleanup-procedures)
9. [Backup & Recovery](#9-backup--recovery)
10. [Performance Tuning](#10-performance-tuning)

---

## 1. Docker Operations Reference

### Essential Docker Commands

#### Container Status

```bash
# List running containers with status
docker ps

# List all containers (including stopped/exited)
docker ps -a

# Formatted table output
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}\t{{.Image}}"

# Show only container names and health status
docker ps --format "{{.Names}}: {{.Status}}"
```

#### Container Logs

```bash
# View full logs for a container
docker logs obiam
docker logs obam

# Follow logs in real time (Ctrl+C to stop)
docker logs -f obiam

# Show last N lines
docker logs --tail 100 obam

# Show logs with timestamps
docker logs -t obiam

# Show logs since a time
docker logs --since "2024-01-01T00:00:00" obiam
docker logs --since "30m" obam     # Last 30 minutes
```

#### Container Management

```bash
# Restart a container
docker restart obiam
docker restart obam

# Stop a container gracefully
docker stop obam

# Start a stopped container
docker start obiam

# Force stop (immediate kill)
docker kill obam

# Execute a command inside a running container
docker exec -it obiam bash
docker exec -it obam bash

# Run a one-off command (non-interactive)
docker exec obiam cat /home/wso2carbon/wso2is-7.1.0/repository/conf/deployment.toml
```

#### Docker Compose Operations

```bash
cd docker-files/docker-compose

# Start all services (detached)
docker compose up -d

# Stop all services (preserve volumes)
docker compose down

# Stop and remove volumes (destroys data)
docker compose down -v

# View compose service status
docker compose ps

# Follow all service logs
docker compose logs -f

# Follow logs for a specific service
docker compose logs -f obiam

# Restart a single service
docker compose restart obiam

# Rebuild and restart
docker compose up -d --build

# Scale (not typically needed for this project)
docker compose up -d --scale obiam=1
```

#### Image Management

```bash
# List all images
docker images

# List images related to this project
docker images | grep -E "ob_database|wso2is-ob|wso2am-ob"

# Remove a specific image
docker rmi wso2is-ob:4.0.0

# Remove all project images
docker rmi ob_database wso2is-ob:4.0.0 wso2am-ob:4.0.0

# Remove dangling (untagged) images
docker image prune

# Remove all unused images
docker image prune -a
```

#### Network Operations

```bash
# List networks
docker network ls

# Inspect the project network
docker network inspect ob-network

# Create the project network
docker network create ob-network

# Remove the network (after stopping containers)
docker network rm ob-network
```

#### Volume Operations

```bash
# List volumes
docker volume ls

# Inspect the MySQL volume
docker volume inspect docker-compose_mysql_data

# Remove a specific volume (DESTROYS DATA)
docker volume rm docker-compose_mysql_data
```

#### System Cleanup

```bash
# Remove stopped containers, unused networks, dangling images
docker system prune

# Remove everything including unused images (DESTRUCTIVE)
docker system prune -a

# Remove everything including volumes (VERY DESTRUCTIVE)
docker system prune -a --volumes

# Show disk usage
docker system df
```

---

## 2. Container Management

### Container Dependency Order

```
mysql (must be healthy first)
  └── obiam (depends on mysql)
       └── obam (depends on mysql + obiam)
```

Always start MySQL first. If you need to restart individual containers:

```bash
# Restart only MySQL (will cause dependent services to lose DB connection)
docker restart mysql

# Restart IS (API Manager may need restart too)
docker restart obiam

# Restart AM only
docker restart obam
```

### Accessing Container Shells

```bash
# WSO2 IS container
docker exec -it obiam bash
# Key paths inside:
#   /home/wso2carbon/wso2is-7.1.0/                    # Product home
#   /home/wso2carbon/wso2is-7.1.0/repository/conf/     # Configuration
#   /home/wso2carbon/wso2is-7.1.0/repository/logs/     # Log files
#   /home/wso2carbon/wso2is-7.1.0/repository/deployment/server/webapps/  # Deployed apps

# WSO2 AM container
docker exec -it obam bash
# Key paths inside:
#   /home/wso2carbon/wso2am-4.5.0/                     # Product home
#   /home/wso2carbon/wso2am-4.5.0/repository/conf/      # Configuration
#   /home/wso2carbon/wso2am-4.5.0/repository/logs/      # Log files

# MySQL container
docker exec -it $(docker ps -q --filter ancestor=ob_database) bash
# Or connect directly to MySQL:
docker exec -it $(docker ps -q --filter ancestor=ob_database) mysql -u wso2 -pwso2
```

### Inspecting Container Health

```bash
# Check health status
docker inspect --format='{{.State.Health.Status}}' obiam
docker inspect --format='{{.State.Health.Status}}' obam

# Check health check details (last 5 results)
docker inspect --format='{{json .State.Health}}' obiam | python -m json.tool

# Check resource usage
docker stats obiam obam --no-stream
```

---

## 3. Log Analysis

### WSO2 Server Logs

Inside the containers, WSO2 logs are at:
- IS: `/home/wso2carbon/wso2is-7.1.0/repository/logs/wso2carbon.log`
- AM: `/home/wso2carbon/wso2am-4.5.0/repository/logs/wso2carbon.log`

```bash
# Read IS carbon log
docker exec obiam cat /home/wso2carbon/wso2is-7.1.0/repository/logs/wso2carbon.log

# Tail AM carbon log
docker exec obam tail -100 /home/wso2carbon/wso2am-4.5.0/repository/logs/wso2carbon.log
```

### Common Log Patterns to Watch For

| Pattern | Meaning |
|---------|---------|
| `Pass-through HTTPS Listener started` | Service is ready to accept HTTPS requests |
| `Carbon started in X sec` | WSO2 Carbon server fully initialized |
| `ERROR {org.wso2...}` | Application-level error |
| `java.net.ConnectException` | Cannot reach a dependent service (DB or other WSO2 server) |
| `java.sql.SQLException` | Database connection or query failure |
| `javax.net.ssl.SSLHandshakeException` | TLS/certificate issue between services |
| `401 Unauthorized` | Token validation failure or missing credentials |
| `WARN {org.wso2.carbon.user.core}` | User/authentication warnings |

### Filtering Logs

```bash
# Find errors in IS logs
docker logs obiam 2>&1 | grep -i "error"

# Find SSL issues
docker logs obam 2>&1 | grep -i "ssl\|certificate\|handshake"

# Find database issues
docker logs obiam 2>&1 | grep -i "sql\|database\|connection refused"

# Check if service is ready
docker logs obam 2>&1 | grep "Pass-through HTTPS Listener started"
```

---

## 4. Health Monitoring

### Quick Health Check Script

```bash
#!/bin/bash
echo "=== Container Status ==="
docker ps --format "table {{.Names}}\t{{.Status}}"

echo ""
echo "=== Health Checks ==="
for container in obiam obam; do
    status=$(docker inspect --format='{{.State.Health.Status}}' $container 2>/dev/null)
    echo "$container: $status"
done

echo ""
echo "=== Endpoint Checks ==="
for url in "https://obiam:9446/carbon/admin/login.jsp" "https://obam:9443/carbon/admin/login.jsp"; do
    code=$(curl -k -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
    echo "$url -> HTTP $code"
done

echo ""
echo "=== MySQL ==="
docker exec $(docker ps -q --filter ancestor=ob_database) mysqladmin ping -u wso2 -pwso2 2>/dev/null
```

### Endpoint Health Checks

```bash
# IS login page (should return 200)
curl -k -s -o /dev/null -w "%{http_code}\n" https://obiam:9446/carbon/admin/login.jsp

# AM login page (should return 200)
curl -k -s -o /dev/null -w "%{http_code}\n" https://obam:9443/carbon/admin/login.jsp

# Demo app (should return 200)
curl -k -s -o /dev/null -w "%{http_code}\n" https://obiam:9446/api-ob-demo-1.0.0

# API Gateway (should return 200 or 401)
curl -k -s -o /dev/null -w "%{http_code}\n" https://obam:8243/open-banking/v3.1/aisp/accounts

# MySQL ping
docker exec $(docker ps -q --filter ancestor=ob_database) mysqladmin ping -u wso2 -pwso2
```

---

## 5. Database Operations

### Connect to MySQL

```bash
# Interactive MySQL shell
docker exec -it $(docker ps -q --filter ancestor=ob_database) mysql -u wso2 -pwso2

# As root
docker exec -it $(docker ps -q --filter ancestor=ob_database) mysql -u root -proot
```

### Common Database Queries

```sql
-- List all databases
SHOW DATABASES;

-- Check consent records
USE fs_consentdb;
SHOW TABLES;

-- Check identity data
USE fs_identitydb;
SHOW TABLES;

-- Check API management data
USE fs_apimgtdb;
SHOW TABLES;
```

### Database Backup

```bash
# Export all databases
docker exec $(docker ps -q --filter ancestor=ob_database) \
    mysqldump -u root -proot --all-databases > backup_$(date +%Y%m%d).sql

# Export a specific database
docker exec $(docker ps -q --filter ancestor=ob_database) \
    mysqldump -u root -proot fs_consentdb > consent_backup.sql
```

### Database Restore

```bash
# Restore from backup
cat backup.sql | docker exec -i $(docker ps -q --filter ancestor=ob_database) mysql -u root -proot
```

---

## 6. Troubleshooting Guide

### Problem: Registry Login Failures

**Symptoms**:
- `unauthorized: authentication required` during Docker build
- `Error response from daemon: Head ... unauthorized`

**Resolution**:
```bash
# Clear existing credentials and re-login
docker logout registry.wso2.com
docker login registry.wso2.com

# Verify credentials work
docker pull registry.wso2.com/wso2-is/is:7.1.0.0-alpine
```

**If the image doesn't exist**: Verify the exact image tag. The Dockerfiles expect:
- `registry.wso2.com/wso2-ob/obiam-accelerator:4.0.0.0-alpine`
- `registry.wso2.com/wso2-is/is:7.1.0.0-alpine`
- `registry.wso2.com/wso2-ob/obam-accelerator:4.0.0.0-alpine`
- `registry.wso2.com/wso2-apim/am:4.5.0.0-alpine`

---

### Problem: Python Version Issues in build.sh

**Symptoms**:
- `python: command not found`
- `No module named http.server`

**Resolution**:

The script uses `python -m http.server 8000`. If your system uses `python3`:

```bash
# Quick fix: create alias
alias python=python3
./build.sh

# Permanent fix: edit build.sh line 28
# Change: python -m http.server 8000 &
# To:     python3 -m http.server 8000 &

# System fix (Ubuntu/Debian)
sudo apt install python-is-python3
```

---

### Problem: Host Mapping Issues

**Symptoms**:
- `ERR_NAME_NOT_RESOLVED` in browser
- `Could not resolve host: obiam` from curl
- Services can't reach each other

**Resolution**:

Verify host entries:
```bash
# Check entries exist
cat /etc/hosts | grep -E "obiam|obam"
# Expected:
# 127.0.0.1   obiam
# 127.0.0.1   obam

# Test resolution
ping -c 1 obiam
ping -c 1 obam
```

If entries exist but still fail, flush DNS:
```bash
# macOS
sudo dscacheutil -flushcache && sudo killall -HUP mDNSResponder

# Linux (systemd)
sudo systemctl restart systemd-resolved

# Windows (admin cmd)
ipconfig /flushdns
```

> **Note**: Container-to-container communication uses Docker DNS, not `/etc/hosts`. The host file is only needed for browser access from the host machine.

---

### Problem: Certificate/Browser Trust Issues

**Symptoms**:
- `NET::ERR_CERT_AUTHORITY_INVALID`
- `SSL_ERROR_RX_RECORD_TOO_LONG`
- Browser refuses to load pages

**Resolution**:

All services use self-signed certificates. This is expected in a sandbox environment.

**Browser bypass**:
- Chrome: Click "Advanced" > "Proceed to site (unsafe)"
- Firefox: Click "Advanced" > "Accept the Risk and Continue"
- Safari: Click "Show Details" > "visit this website"
- Edge: Click "Advanced" > "Continue to site (unsafe)"

**For API testing**:
```bash
# Use -k flag with curl
curl -k https://obiam:9446/carbon/admin/login.jsp
```

**Chrome tip**: If Chrome blocks the page entirely (HSTS), type `thisisunsafe` while the error page is focused (no input field needed).

---

### Problem: Container Startup Failures

**Symptoms**:
- Containers exit immediately after starting
- Health checks never pass
- `docker ps` shows containers as `Restarting` or `Exited`

**Diagnosis**:
```bash
# Check what's happening
docker ps -a
docker logs obiam 2>&1 | tail -100
docker logs obam 2>&1 | tail -100

# Check if MySQL is healthy (must be healthy for others to start)
docker inspect --format='{{.State.Health.Status}}' $(docker ps -q --filter ancestor=ob_database)
```

**Common causes & fixes**:

| Cause | Fix |
|-------|-----|
| MySQL not ready | Wait longer; check MySQL logs: `docker logs $(docker ps -q --filter ancestor=ob_database)` |
| Insufficient memory | Increase Docker Desktop memory to 8 GB+ |
| Port conflict | See [Port Conflicts](#problem-port-conflicts) below |
| Corrupted volume | `docker compose down -v` then restart |
| Missing Docker network | `docker network create ob-network` |

---

### Problem: Port Conflicts

**Symptoms**:
- `Bind for 0.0.0.0:9446 failed: port is already allocated`
- `address already in use`

**Resolution**:

Find and stop the conflicting process:

```bash
# macOS / Linux
lsof -i :9446
lsof -i :9443
lsof -i :8243
lsof -i :3306

# Windows
netstat -ano | findstr :9446
```

Ports used by this project:

| Port | Service | Notes |
|------|---------|-------|
| 3306 | MySQL | Check for local MySQL installations |
| 8000 | HTTP server | Only during `build.sh` execution |
| 8243 | OBAM Gateway | Check for other WSO2 instances |
| 8280 | OBAM HTTP | Check for other WSO2 instances |
| 9443 | OBAM Admin | Check for other WSO2 instances |
| 9446 | OBIAM | Check for other WSO2 instances |

```bash
# Kill process on a specific port (macOS/Linux)
lsof -ti:9446 | xargs kill -9

# Stop conflicting Docker containers
docker stop <container-name>
```

---

### Problem: Permission Issues

**Symptoms**:
- `Permission denied` running `build.sh`
- `Permission denied` writing to Docker socket

**Resolution**:

```bash
# Make build script executable
chmod +x build.sh
chmod +x docker-files/docker-compose/wait-for-it.sh

# Docker socket permission (Linux)
sudo usermod -aG docker $USER
# Then log out and back in

# Verify Docker access
docker ps
```

---

### Problem: Build Failures

**Maven build fails**:
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Ensure JDK 11
java -version

# Run with debug
cd demo-application
mvn clean package -DskipTests -X
```

**Frontend build fails during Maven**:
```bash
# Clear frontend cache
rm -rf demo-application/components/ob_demo_application/frontend/node_modules

# Re-run Maven
cd demo-application
mvn clean package -DskipTests
```

**Docker build fails -- HTTP server unreachable**:
```bash
# Verify HTTP server is running
curl http://localhost:8000/configuration-files/obiam-deployment.toml

# If not running, start manually:
cd <repo-root>
python -m http.server 8000 &
```

---

### Problem: HTTP Server Port 8000 In Use

**Symptoms**:
- `build.sh` fails because port 8000 is already occupied
- `Address already in use` error

**Resolution**:
```bash
# Kill existing process on port 8000
lsof -ti:8000 | xargs kill -9

# Verify port is free
lsof -i :8000

# Re-run build
./build.sh
```

---

### Problem: Docker Network Missing

**Symptoms**:
- `network ob-network declared as external, but could not be found`

**Resolution**:
```bash
docker network create ob-network
```

The `build.sh` script creates this automatically, but manual `docker compose up` requires it to exist.

---

### Problem: Containers Can't Communicate

**Symptoms**:
- `java.net.ConnectException: Connection refused` in logs
- OBAM can't reach OBIAM or MySQL

**Diagnosis**:
```bash
# Verify all containers are on the same network
docker network inspect ob-network

# Test connectivity from inside a container
docker exec obam ping mysql
docker exec obam curl -k https://obiam:9446
```

---

## 7. Verification & Smoke Tests

### Post-Deployment Checklist

Run through these checks after every deployment:

#### Infrastructure Checks

```bash
# 1. All containers running
docker ps --format "table {{.Names}}\t{{.Status}}"
# Expected: mysql (Up, healthy), obiam (Up, healthy), obam (Up, healthy)

# 2. MySQL accepting connections
docker exec $(docker ps -q --filter ancestor=ob_database) mysqladmin ping -u wso2 -pwso2
# Expected: mysqld is alive

# 3. OBIAM responding
curl -k -s -o /dev/null -w "OBIAM: HTTP %{http_code}\n" https://obiam:9446/carbon/admin/login.jsp
# Expected: OBIAM: HTTP 200

# 4. OBAM responding
curl -k -s -o /dev/null -w "OBAM: HTTP %{http_code}\n" https://obam:9443/carbon/admin/login.jsp
# Expected: OBAM: HTTP 200

# 5. Demo app responding
curl -k -s -o /dev/null -w "Demo App: HTTP %{http_code}\n" https://obiam:9446/api-ob-demo-1.0.0
# Expected: Demo App: HTTP 200

# 6. Consent Manager responding
curl -k -s -o /dev/null -w "Consent Mgr: HTTP %{http_code}\n" https://obiam:9446/consentmgr
# Expected: Consent Mgr: HTTP 200
```

#### Application Checks (Manual)

| # | Check | Expected Result |
|---|-------|----------------|
| 1 | Open [https://obiam:9446/api-ob-demo-1.0.0](https://obiam:9446/api-ob-demo-1.0.0) | Demo app login/home page loads |
| 2 | Open [https://obiam:9446/console](https://obiam:9446/console) | IS admin console login page |
| 3 | Log in to IS console: `is_admin@wso2.com` / `wso2123` | Dashboard loads |
| 4 | Open [https://obam:9443/publisher](https://obam:9443/publisher) | APIM publisher login page |
| 5 | Log in to APIM: `am_admin@wso2.com` / `wso2123` | Publisher dashboard loads |
| 6 | Open [https://obiam:9446/consentmgr](https://obiam:9446/consentmgr) | Consent Manager login page |
| 7 | Log in to Consent Manager: `ann@gold.com` / `Ann@1234` | Consent list page loads |

#### Functional Checks (Manual)

| # | Check | Expected Result |
|---|-------|----------------|
| 1 | Demo App: Log in as `psu@gold.com` / `Wso21234` | Home dashboard with bank accounts |
| 2 | Demo App: Click "Add Account" | OAuth consent flow initiates |
| 3 | Demo App: Initiate a payment | Payment consent flow initiates |
| 4 | Consent Manager: View consents | List of active consents shown |

---

## 8. Shutdown & Cleanup Procedures

### Graceful Shutdown (Preserve Data)

```bash
cd docker-files/docker-compose
docker compose down
```

Containers are stopped and removed. The MySQL volume (`mysql_data`) is preserved.

### Full Shutdown (Remove Data)

```bash
cd docker-files/docker-compose
docker compose down -v
```

> **Warning**: This destroys all database data. Run `build.sh` to reinitialize.

### Complete Cleanup

```bash
# Stop and remove everything
cd docker-files/docker-compose
docker compose down -v

# Remove Docker images
docker rmi ob_database wso2is-ob:4.0.0 wso2am-ob:4.0.0

# Remove the network
docker network rm ob-network

# Remove dangling resources
docker system prune
```

---

## 9. Backup & Recovery

### Database Backup

```bash
# Full backup
docker exec $(docker ps -q --filter ancestor=ob_database) \
    mysqldump -u root -proot --all-databases > full_backup_$(date +%Y%m%d_%H%M%S).sql

# Consent database only
docker exec $(docker ps -q --filter ancestor=ob_database) \
    mysqldump -u root -proot fs_consentdb > consent_backup.sql
```

### Database Restore

```bash
# Restore from backup
cat full_backup.sql | docker exec -i $(docker ps -q --filter ancestor=ob_database) mysql -u root -proot
```

### Recovery from Corrupted State

If containers are in an unrecoverable state:

```bash
# Nuclear reset
cd docker-files/docker-compose
docker compose down -v
docker rmi ob_database wso2is-ob:4.0.0 wso2am-ob:4.0.0
docker network rm ob-network 2>/dev/null

# Fresh build
cd ../..
./build.sh
```

---

## 10. Performance Tuning

### Docker Resource Allocation

For optimal performance, configure Docker Desktop with:

| Resource | Minimum | Recommended |
|----------|---------|-------------|
| CPUs | 2 | 4+ |
| Memory | 4 GB | 8 GB+ |
| Disk | 20 GB | 50 GB+ |
| Swap | 1 GB | 2 GB |

### Container Resource Monitoring

```bash
# Real-time resource usage
docker stats

# One-shot stats
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}"
```

### Slow Startup Diagnosis

If containers take too long to start:

1. Check Docker resource allocation (memory is the most common bottleneck).
2. Check MySQL initialization (first run imports SQL dumps).
3. Check network connectivity (WSO2 servers need to reach MySQL).
4. Check disk I/O (SSDs recommended).

```bash
# Monitor startup progress
docker logs -f obiam 2>&1 | grep -E "started|ready|error|exception"
```
