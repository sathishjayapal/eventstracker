# RabbitMQ Connection Fix

## Problem

The eventstracker application was attempting to connect to `localhost:5672` instead of the configured RabbitMQ host
`192.168.4.39:5672`, resulting in connection refused errors.

## Root Cause

Spring Boot AMQP was using default localhost configuration instead of reading from the `.env` environment variables.

## Solution Implemented

### 1. Updated Bootstrap Configuration Files

Added explicit RabbitMQ configuration to all bootstrap files to read from environment variables:

#### `src/main/resources/bootstrap.yml`

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

#### `src/main/resources/bootstrap-local.yml`

- Same RabbitMQ configuration added for local development profile

#### `src/main/resources/bootstrap-prod.yml`

- Same RabbitMQ configuration added for production profile

### 2. How It Works

- Spring Boot loads the `.env` file via: `spring.config.import: optional:file:.env[.properties]`
- Environment variables from `.env` are resolved in the YAML files using `${VARIABLE_NAME:default}`
- The `dev-up.sh` script automatically generates the `.env` file with correct values from `jubilant-memory/config/.env`

## How to Use

### Prerequisites

1. Ensure `jubilant-memory/config/.env` exists with:
   ```
   RABBITMQ_HOST=192.168.4.39  (or your actual RabbitMQ host)
   RABBITMQ_PORT=5672
   RABBITMQ_USERNAME=guest
   RABBITMQ_PASSWORD=guest
   ```

2. Run the setup script from eventstracker directory:
   ```bash
   ./dev-up.sh
   ```
   This generates the local `.env` file with all required values.

### Running the Application

**Local Development:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Production:**

```bash
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

**Verify Configuration:**
Check the Spring Boot startup logs for:

```
Attempting to connect to: [192.168.4.39:5672]
```

## Connection Recovery

Spring Boot AMQP automatically retries connections. The error messages show:

```
Recovering consumer in 5000 ms.
```

This is normal retry behavior and will continue until RabbitMQ is available.

## Troubleshooting

### Still connecting to localhost?

1. Verify `.env` file exists: `cat .env`
2. Verify environment variable format: `RABBITMQ_HOST=192.168.4.39` (no extra spaces)
3. Rebuild the project: `mvn clean compile`
4. Restart the application

### Connection refused despite correct host?

1. Verify RabbitMQ is running on `192.168.4.39:5672`:
   ```bash
   nc -zv 192.168.4.39 5672
   ```
2. Check RabbitMQ credentials: `guest/guest`
3. Verify network connectivity to the host

### Verify credentials with RabbitMQ

```bash
# If RabbitMQ is running locally on 192.168.4.39
docker exec <rabbitmq-container> rabbitmqctl list_users
```

## Files Modified

- `src/main/resources/bootstrap.yml` - Added RabbitMQ configuration
- `src/main/resources/bootstrap-local.yml` - Added RabbitMQ configuration
- `src/main/resources/bootstrap-prod.yml` - Added RabbitMQ configuration

