# Event Tracker Service

A robust Spring Boot microservice for tracking and managing events with asynchronous processing using RabbitMQ, distributed scheduling with ShedLock, and comprehensive security features. Built with enterprise-grade patterns for scalability and reliability.

## 🎯 Purpose

Event Tracker provides:
- **Event Management** - Create, track, and manage events
- **Asynchronous Processing** - RabbitMQ-based event handling
- **Distributed Locking** - ShedLock for coordinated task execution
- **Security** - Spring Security integration
- **API Documentation** - OpenAPI/Swagger documentation
- **Code Quality** - Spotless code formatting

## 🛠️ Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.5.0
- **Build Tool:** Maven with Spotless plugin
- **Database:** PostgreSQL with Flyway migrations
- **Message Queue:** RabbitMQ (AMQP)
- **UI:** Thymeleaf + Bootstrap 5.3.5
- **Distributed Locking:** ShedLock 6.3.0
- **DTO Mapping:** MapStruct 1.6.3
- **API Documentation:** SpringDoc OpenAPI 2.8.6

## ✨ Key Features

- **Event Streaming** - Asynchronous event processing via RabbitMQ
- **Distributed Tasks** - Coordinated scheduling with ShedLock
- **REST API** - Comprehensive RESTful endpoints
- **Security** - Authentication and authorization
- **Type-Safe Mapping** - MapStruct for DTO conversions
- **Database Migrations** - Flyway for version control
- **API Documentation** - Interactive Swagger UI
- **Code Formatting** - Automated with Spotless

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.8+
- PostgreSQL database
- RabbitMQ server
- Docker & Docker Compose (optional)

## 🚀 Getting Started

### 1. Start Infrastructure Services

```bash
docker compose up -d
```

This starts:
- PostgreSQL database
- RabbitMQ message broker

### 2. Configure Application

Create `application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eventtracker
    username: postgres
    password: postgres
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### 3. Run the Application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Access the application at `http://localhost:9081`

## 📡 RabbitMQ Configuration

### Exchange and Queue Setup

The application auto-configures:
- **Exchange:** `events-exchange`
- **Queue:** `events-queue`
- **Routing Key:** `event.created`

### Message Publishing

Events are published to RabbitMQ when created:

```java
rabbitTemplate.convertAndSend("events-exchange", "event.created", eventDto);
```

## 🔒 Distributed Locking with ShedLock

ShedLock prevents duplicate execution in distributed environments:

```java
@Scheduled(cron = "0 0 * * * *")
@SchedulerLock(name = "processEvents", lockAtMostFor = "10m")
public void processEvents() {
    // Task logic
}
```

## 🌐 API Endpoints

### Event Management

- `POST /api/events` - Create new event
- `GET /api/events` - List all events
- `GET /api/events/{id}` - Get event by ID
- `PUT /api/events/{id}` - Update event
- `DELETE /api/events/{id}` - Delete event

### Documentation

- `GET /swagger-ui.html` - Interactive API documentation
- `GET /v3/api-docs` - OpenAPI specification JSON

### Health & Monitoring

- `GET /actuator/health` - Application health
- `GET /actuator/info` - Application info

## 🐛 Useful Commands

**Find process on port 9081:**
```bash
lsof -ti:9081
```

**Kill process on port 9081:**
```bash
kill -9 $(lsof -ti:9081)
```

**View RabbitMQ Management UI:**
```
http://localhost:15672
Default credentials: guest/guest
```


## Development

When starting the application `docker compose up` is called and the app will connect to the contained services.
[Docker](https://www.docker.com/get-started/) must be available on the current system.

During development it is recommended to use the profile `local`. In IntelliJ `-Dspring.profiles.active=local` can be
added in the VM options of the Run Configuration after enabling this property in "Modify options". Create your own
`application-local.yml` file to override settings for development.

Lombok must be supported by your IDE. For IntelliJ install the Lombok plugin and enable annotation processing -
[learn more](https://bootify.io/next-steps/spring-boot-with-lombok.html).

After starting the application it is accessible under `localhost:8080`.

## Testing requirements

Testcontainers is used for running the integration tests. Due
to the reuse flag, the container will not shut down after the tests. It can be stopped manually if needed.

## Build

The application can be tested and built using the following command:

```
mvnw clean package
```

Start your application with the following command - here with the profile `production`:

```
java -Dspring.profiles.active=production -jar ./target/event-service-0.0.1-SNAPSHOT.jar
```

If required, a Docker image can be created with the Spring Boot plugin. Add `SPRING_PROFILES_ACTIVE=production` as
environment variable when running the container.

```
mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=me.sathish/event-service
```