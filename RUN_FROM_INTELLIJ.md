# How to Run EventTracker from IntelliJ

## The Problem You're Hitting

```
Consider the following:
If you want an embedded database (H2, HSQL or Derby), please put it on the classpath.
```

This means: **PostgreSQL is not running or app config values were not loaded**

---

## Solution: 3 Simple Steps

### STEP 1: Start Infrastructure (Terminal)

Open a terminal and run:

```bash
cd /Users/skminfotech/IdeaProjects/eventstracker
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

You can close this terminal after it finishes. Services run detached in Docker.

---

### STEP 2: Open EventTracker in IntelliJ

1. File → Open
2. Select: `/Users/skminfotech/IdeaProjects/eventstracker`
3. Wait for IntelliJ to index and download dependencies

---

### STEP 3: Run from IntelliJ

1. Run → Edit Configurations → `EventServiceApplication`
2. Set **Active profiles** to `local`
3. Set **Working directory** to `$MODULE_WORKING_DIR$` (or the `eventstracker` folder)
4. Leave environment variables empty unless you want to override `.env`
5. Start Debug/Run

Why this works: local profile now auto-loads `eventstracker/.env`, which is generated/synced from consolidated-postgres bootstrap.

**Wait for message:**
```
Started EventServiceApplication in X seconds
```

**Access application:**
```
http://localhost:9081
```

---

## If It Still Fails

### Check 1: Containers are Running
```bash
docker ps
```

Should include:
- `event-service-db`
- `sathish-config-server`
- `sathishproject-rabbitmq`

### Check 2: `.env` Exists and Has Credentials
```bash
cd /Users/skminfotech/IdeaProjects/eventstracker
ls -la .env
grep -E '^(EVENTS_TRACKER_DB_URL|EVENTS_TRACKER_DB_USER|SPRING_CLOUD_CONFIG_USERNAME)=' .env
```

If `.env` is missing, run:
```bash
./eventtracker.sh deps
```

### Check 3: Verify Ports
```bash
./eventtracker.sh status
```

Expected in-use ports:
- `6433` (PostgreSQL)
- `5672` (RabbitMQ)
- `8888` (Config Server)

### Check 4: Database Login from Container
```bash
docker exec event-service-db psql -U "$(grep '^EVENTS_TRACKER_DB_USER=' .env | cut -d= -f2-)" -d event-service -c 'select 1;'
```

---

## Common Errors & Fixes

### Error: `password authentication failed for user "postgres"`
Cause: app or IDE still using stale `postgres` user while local stack uses generated `EVENTS_TRACKER_DB_USER`.

Fix:
1. Re-run `./eventtracker.sh deps`
2. Ensure IntelliJ run config working directory is `eventstracker`
3. Remove hardcoded DB env vars from IntelliJ run config

### Error: `Could not locate PropertySource` from Config Server
Cause: Config server not running, or config credentials not loaded.

Fix:
1. Confirm `sathish-config-server` is running
2. Confirm `.env` has `SPRING_CLOUD_CONFIG_USERNAME` and `SPRING_CLOUD_CONFIG_PASSWORD`
3. Re-run `./eventtracker.sh deps`

### Error: `Failed to configure a DataSource`
Cause: local profile or `.env` was not loaded.

Fix:
1. Set run profile to `local`
2. Ensure working directory points to `eventstracker` root

---

## Still Stuck?

Run this diagnostic:

```bash
cd /Users/skminfotech/IdeaProjects/eventstracker
./eventtracker.sh check
./eventtracker.sh status
```

Then share the exact first failure stack trace block from IntelliJ Run/Debug output.
