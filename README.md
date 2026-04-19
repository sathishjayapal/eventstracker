# EventsTracker

EventsTracker is a Spring Boot service for event ingestion and tracking with PostgreSQL, RabbitMQ, and externalized configuration via Spring Cloud Config.

## Current State

- Java 21 + Maven wrapper (`./mvnw`)
- Spring Boot `3.5.7`
- Primary code under `src/`
- Operational setup scripts and guides in the repository root

## Quick Start

1. Install Java 21, Docker, and Maven-compatible tooling.
2. Start dependencies:
   ```bash
   ./eventtracker.sh deps
   ```
   This now uses the consolidated local-db flow (`dev-up.sh` + `jubilant-memory/config/.env`) so DB credentials stay consistent.
3. Start the app:
   ```bash
   ./mvnw spring-boot:run
   ```

Detailed setup and troubleshooting:
- `SETUP_GUIDE.md`
- `RUN_FROM_INTELLIJ.md`
- `CONFIG_SERVER_SETUP.md`

## README Update Policy

This repository enforces README freshness in CI:

- If important project files change (for example `src/**`, `pom.xml`, `docker-compose.yml`, `init.sql`, or operational scripts/guides), at least one README must also be updated.
- Accepted README targets:
  - `README.md`
  - `docs/README.md`

If your change affects behavior, architecture, configuration, setup, or operations, update the README in the same check-in so documentation always reflects the true state of the project.
