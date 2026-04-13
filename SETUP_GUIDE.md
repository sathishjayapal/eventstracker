# EventTracker - Setup & Troubleshooting Guide

## ❌ Why EventTracker Won't Start

### Current Issues on Your Machine:

1. **❌ Maven Not Installed**
   - Required for building the project
   - Required for running `mvn spring-boot:run`

2. **❌ Docker Not Available**
   - Required for PostgreSQL database
   - Required for RabbitMQ message broker
   - Required for Config Server

3. **❌ Java Version Mismatch**
   - Running: Java 11
   - Required: Java 21 (pom.xml specifies `<java.version>21</java.version>`)

4. **❌ Config Server Not Running**
   - Config server needs to be running on port 8888
   - Provides centralized configuration management

---

## ✅ Solution: Setup Your Machine

### Option 1: Use IntelliJ IDE (Recommended)

1. **Install Prerequisites:**
   - Install Java 21 JDK (from adoptopenjdk.net or oracle.com)
   - Install Maven (brew install maven on Mac, apt install maven on Linux)
   - Install Docker Desktop

2. **Open in IntelliJ:**
   - File → Open → Select eventstracker folder
   - IntelliJ will auto-detect pom.xml

3. **Configure IntelliJ:**
   - Set Project SDK to Java 21
   - Preferences → Java → Maven
   - Let IntelliJ download dependencies

4. **Run from IntelliJ:**
   - Start infrastructure: `./eventtracker.sh deps` (in terminal)
   - Then: Run → Run 'EventTrackerApplication' (green play button)

### Option 2: Command Line (Shell Script)

1. **Install Prerequisites:**
   ```bash
   # macOS
   brew install java21 maven docker-desktop
   
   # Ubuntu/Linux
   sudo apt install openjdk-21-jdk maven docker.io
   sudo usermod -aG docker $USER
   
   # Windows
   # Download from: adoptopenjdk.net and maven.apache.org
   ```

2. **Verify Installation:**
   ```bash
   java -version        # Should show Java 21
   mvn --version        # Should show Maven 3.x+
   docker ps            # Should work without errors
   ```

3. **Start EventTracker:**
   ```bash
   cd eventstracker
   
   # Full stack (everything)
   ./eventtracker.sh start
   
   # OR: Just infrastructure
   ./eventtracker.sh deps
   
   # Then in another terminal:
   mvn spring-boot:run
   ```

---

## 🔍 Step-by-Step Debugging

### Step 1: Check Java Version
```bash
java -version
# Should show: Java 21.x.x or later
```

### Step 2: Install/Check Maven
```bash
mvn --version
# Should show: Apache Maven 3.x.x
```

### Step 3: Check Docker
```bash
docker ps
# Should list running containers (even if empty)
```

### Step 4: Validate EventTracker Project
```bash
cd eventstracker
./eventtracker.sh check
```

### Step 5: Download Dependencies
```bash
mvn clean install -DskipTests
# This downloads all required libraries (takes 5-10 minutes first time)
```

### Step 6: Start Infrastructure
```bash
./eventtracker.sh deps
# Waits 30 seconds, then starts Config Server, PostgreSQL, RabbitMQ
```

### Step 7: Start Application
```bash
mvn spring-boot:run
# Application starts on http://localhost:9081
```

---

## 📋 Checklist Before Running

- [ ] Java 21 installed? (`java -version`)
- [ ] Maven installed? (`mvn --version`)
- [ ] Docker installed and running? (`docker ps`)
- [ ] Config server directory exists? (`ls ../sathishproject-config-server/.env`)
- [ ] Infrastructure directory exists? (`ls ../jubilant-memory/config/docker-compose.yml`)
- [ ] Dependencies downloaded? (`mvn clean install -DskipTests` completed)

---

## 🚀 Quick Start (After Prerequisites)

```bash
# Terminal 1: Start infrastructure
cd eventstracker
./eventtracker.sh deps
# Wait for message: "Infrastructure started"

# Terminal 2: Start application
cd eventstracker
mvn clean install -DskipTests    # First time only
mvn spring-boot:run

# Terminal 3: Access application
open http://localhost:9081
```

---

## 🔐 Credentials Needed

### Config Server (port 8888)
- Username: `sathish`
- Password: `pass`

### Database (PostgreSQL)
- Host: `localhost:6433`
- Username: `psqladmin`
- Password: `psqladminpas$`
- Database: `event-service`

### RabbitMQ (port 5672)
- Username: `guest`
- Password: `guest`
- Management UI: `http://localhost:15672`

---

## 🆘 If Nothing Works

1. **Uninstall conflicting software:**
   ```bash
   # Remove old Java versions
   # Remove old Maven
   # Stop Docker if conflicts
   ```

2. **Fresh install:**
   ```bash
   brew reinstall java21 maven
   # OR
   sudo apt reinstall openjdk-21-jdk maven
   ```

3. **Clear cache:**
   ```bash
   rm -rf ~/.m2/repository
   mvn clean install -DskipTests
   ```

4. **Check logs:**
   ```bash
   # EventTracker logs
   tail -f target/spring.log
   
   # Docker logs
   docker logs sathish-config-server
   docker logs event-service-db
   docker logs sathishproject-rabbitmq
   ```

---

## 📞 Support

If still stuck:
1. Run: `./eventtracker.sh check` - Shows what's missing
2. Show: `java -version`, `mvn --version`, `docker ps` output
3. Check: IntelliJ configuration (Project SDK must be Java 21)

---

## Architecture Overview

```
eventstracker/
├── eventtracker.sh          ← Main control script
├── pom.xml                  ← Maven configuration (Java 21)
├── docker-compose.yml       ← Local PostgreSQL
└── src/main/java/...        ← Application code

Port 9081 (EventTracker Application)
    ↓ calls config from
Port 8888 (Config Server - sathishproject-config-server)
    ↓ depends on
Port 6433 (PostgreSQL - event-service-db)
Port 5672 (RabbitMQ - sathishproject-rabbitmq)
```
