# How to Run EventTracker from IntelliJ

## The Problem You're Hitting

```
Consider the following:
If you want an embedded database (H2, HSQL or Derby), please put it on the classpath.
```

This means: **PostgreSQL is not running or not connected**

---

## Solution: 3 Simple Steps

### STEP 1: Start Infrastructure (Terminal)

Open a terminal and run:

```bash
cd /path/to/eventstracker
./eventtracker.sh deps
```

Wait for output like:
```
✓ Config Server ready
✓ Infrastructure started
ℹ Config Server: http://localhost:8888
ℹ PostgreSQL: localhost:6433
ℹ RabbitMQ: localhost:5672
```

**Leave this terminal running.** Do NOT close it.

---

### STEP 2: Open EventTracker in IntelliJ

1. File → Open
2. Select: `/path/to/eventstracker`
3. Wait for IntelliJ to index and download dependencies

---

### STEP 3: Run from IntelliJ

**Option A: Using Run Configuration**

1. Click the green ▶ button (top right)
2. Select "EventTrackerApplication" (or similar)
3. Wait for startup

**Option B: Using Terminal in IntelliJ**

1. Open: View → Tool Windows → Terminal
2. Run:
   ```bash
   mvn spring-boot:run
   ```

**Wait for message:**
```
Started EventTrackerApplication in X seconds
```

**Access application:**
```
http://localhost:9081
```

---

## If It Still Fails

### Check 1: Database Connection
```bash
# In a new terminal, test PostgreSQL
docker ps

# Should show:
# event-service-db
# sathish-config-server
# sathishproject-rabbitmq
```

### Check 2: Environment File
In IntelliJ:
1. Run → Edit Configurations
2. Select "EventTrackerApplication"
3. Look for "Environment variables"
4. Add or check these exist:
   ```
   SPRING_PROFILES_ACTIVE=local
   CONFIG_SERVER_URL=http://localhost:8888
   ```

### Check 3: View Application Logs
In IntelliJ bottom panel:
1. Look for "Run" tab
2. Scroll to find error messages
3. Search for: "PostgreSQL", "connection", "database"

### Check 4: Verify Ports
```bash
# Make sure infrastructure started correctly
./eventtracker.sh status

# Should show:
# ✓ Port 6433 is in use (PostgreSQL)
# ✓ Port 5672 is in use (RabbitMQ)
# ✓ Port 8888 is in use (Config Server)
```

---

## Common Errors & Fixes

### Error: "Connection refused"
```
Cause: PostgreSQL not running
Fix: Run: ./eventtracker.sh deps (keep it running)
```

### Error: "No bean named 'userDetailsService'"
```
Cause: Config server not running
Fix: Check ./eventtracker.sh deps output - wait for "Config Server ready"
```

### Error: "Failed to configure a DataSource"
```
Cause: Missing environment variables
Fix: In IntelliJ Run Configuration, add:
     SPRING_PROFILES_ACTIVE=local
```

### Error: "Address already in use"
```
Cause: Port 9081 already in use
Fix: Kill process: ./eventtracker.sh stop
     Or: Use different port in application.yml
```

---

## Still Stuck?

Run this diagnostic:
```bash
cd eventstracker
./eventtracker.sh check
```

Share the output - it will show exactly what's missing.
