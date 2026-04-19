# EventTracker - Spring Cloud Config Server Integration

## Overview

EventTracker now integrates with the **sathishconfigserver** (jubilant-memory) for centralized configuration management. This allows you to manage configurations for local and production environments from a single Git repository.

---

## Architecture

```
┌─────────────────┐         ┌──────────────────────┐         ┌─────────────────────┐
│  EventTracker   │────────▶│  Config Server       │────────▶│  jubilant-memory    │
│  Application    │         │  (Port 8888)         │         │  Git Repository     │
└─────────────────┘         └──────────────────────┘         └─────────────────────┘
```

---

## Configuration Files

### In EventTracker (`/src/main/resources/`)

1. **`bootstrap.yml`** - Main bootstrap config
   - Sets application name: `eventstracker`
   - Configures config server URL
   - Default profile: `local`

2. **`bootstrap-local.yml`** - Local development
   - Config server: `http://localhost:8888`
   - Debug logging enabled

3. **`bootstrap-prod.yml`** - Production
   - Config server: `http://sathish-config-server:8888`
   - Fail-fast enabled
   - Enhanced retry logic

### In jubilant-memory Config Repository

1. **`eventstracker-local.yml`** - Local environment config
   - Database: `localhost:6433/event-service`
   - RabbitMQ: `localhost:5672`
   - Debug logging

2. **`eventstracker-prod.yml`** - Production environment config
   - Database: Cloud/Docker database
   - RabbitMQ: Docker/Cloud RabbitMQ
   - Production logging

---

## Running Locally

### 1. Start Config Server

```bash
cd /Users/sathishjayapal/IdeaProjects/jubilant-memory/config
docker-compose up config-server
```

**Verify config server is running:**
```bash
curl http://localhost:8888/actuator/health
```

### 2. Start EventTracker

**Option A: Using Maven**
```bash
cd /Users/sathishjayapal/IdeaProjects/eventstracker
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Option B: Using IDE**
- Set VM Options: `-Dspring.profiles.active=local`
- Or set Environment Variable: `SPRING_PROFILES_ACTIVE=local`

### 3. Verify Configuration

Check that EventTracker loaded config from server:
```bash
curl http://localhost:9081/actuator/env | jq '.propertySources[] | select(.name | contains("config"))'
```

---

## Running in Production

### 1. Set Environment Variables

```bash
export SPRING_PROFILES_ACTIVE=prod
export CONFIG_SERVER_URL=http://sathish-config-server:8888
export EVENTSTRACKER_DB_PASSWORD=<your-db-password>
export EVENT_DOMAIN_USER_PASSWORD=<your-password>
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest
```

### 2. Start with Docker Compose

Add to `docker-compose.yml`:
```yaml
eventstracker:
  container_name: eventstracker
  image: your-registry/eventstracker:latest
  ports:
    - "9081:9081"
  environment:
    - SPRING_PROFILES_ACTIVE=prod
    - CONFIG_SERVER_URL=http://sathish-config-server:8888
    - EVENTSTRACKER_DB_PASSWORD=${EVENTSTRACKER_DB_PASSWORD}
    - EVENT_DOMAIN_USER_PASSWORD=${EVENT_DOMAIN_USER_PASSWORD}
  depends_on:
    - config-server
    - eventstracker-db
    - sathishproject-rabbitmq
  networks:
    - gotoaws_net
```

### 3. Start Services

```bash
docker-compose up -d config-server
# Wait for config server to be healthy
docker-compose up -d eventstracker
```

---

## Configuration Hierarchy

EventTracker loads configuration in this order (later overrides earlier):

1. **`application.yml`** (in eventstracker project) - Base defaults
2. **`eventstracker-local.yml`** or **`eventstracker-prod.yml`** (from config server) - Environment-specific
3. **Environment variables** - Runtime overrides

---

## Key Configuration Properties

### Database
```yaml
# Local
spring.datasource.url: jdbc:postgresql://localhost:6433/event-service

# Production
spring.datasource.url: ${EVENTSTRACKER_DB_URL}
```

### RabbitMQ
```yaml
# Local
spring.rabbitmq.host: localhost
spring.rabbitmq.port: 5672

# Production
spring.rabbitmq.host: ${RABBITMQ_HOST:sathishproject-rabbitmq}
```

### Config Server
```yaml
# Local
spring.cloud.config.uri: http://localhost:8888

# Production
spring.cloud.config.uri: ${CONFIG_SERVER_URL:http://sathish-config-server:8888}
```

---

## Troubleshooting

### Config Server Not Reachable

**Symptom:** EventTracker fails to start with connection errors

**Solution:**
1. Verify config server is running:
   ```bash
   curl http://localhost:8888/actuator/health
   ```

2. Check config server logs:
   ```bash
   docker logs sathish-config-server
   ```

3. Test config endpoint:
   ```bash
   curl http://localhost:8888/eventstracker/local
   ```

### Configuration Not Loading

**Symptom:** EventTracker uses default values instead of config server values

**Solution:**
1. Check bootstrap is enabled:
   - Verify `spring-cloud-starter-bootstrap` dependency exists
   - Check `bootstrap.yml` is in `src/main/resources/`

2. Verify profile is set:
   ```bash
   echo $SPRING_PROFILES_ACTIVE
   ```

3. Check EventTracker logs for config loading:
   ```
   Located property source: [BootstrapPropertySource {name='bootstrap'}]
   ```

### Database Connection Issues

**Symptom:** EventTracker can't connect to database

**Solution:**
1. Verify database is running:
   ```bash
   docker ps | grep event-service-db
   ```

2. Test database connection:
   ```bash
   psql -h localhost -p 6433 -U eventsvc_local -d event-service
   ```

3. Check config server returned correct DB URL:
   ```bash
   curl http://localhost:8888/eventstracker/local | jq '.propertySources[].source | select(.["spring.datasource.url"])'
   ```

---

## Updating Configuration

### 1. Update Config Files

Edit files in jubilant-memory:
```bash
cd /Users/sathishjayapal/IdeaProjects/jubilant-memory
vi eventstracker-local.yml
# or
vi eventstracker-prod.yml
```

### 2. Commit Changes

```bash
git add eventstracker-*.yml
git commit -m "Update eventstracker configuration"
git push
```

### 3. Refresh Application

**Option A: Restart EventTracker**
```bash
# Local
mvn spring-boot:stop
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Docker
docker-compose restart eventstracker
```

**Option B: Use Refresh Endpoint** (if enabled)
```bash
curl -X POST http://localhost:9081/actuator/refresh
```

---

## Benefits

✅ **Centralized Configuration** - All configs in one Git repository  
✅ **Environment-Specific** - Separate configs for local/prod  
✅ **Version Controlled** - Track configuration changes  
✅ **Dynamic Updates** - Update configs without rebuilding  
✅ **Secure** - Sensitive data via environment variables  
✅ **Consistent** - Same config server for all microservices  

---

## Next Steps

1. ✅ Add eventstracker to jubilant-memory config repository
2. ✅ Configure Spring Cloud Config Client
3. ✅ Create bootstrap configurations
4. ⏭️ Test local environment
5. ⏭️ Deploy to production
6. ⏭️ Set up config encryption for sensitive data

---

**Last Updated:** 2026-04-12  
**Config Server:** sathish-config-server (jubilant-memory)  
**Application:** eventstracker  
**Profiles:** local, prod
