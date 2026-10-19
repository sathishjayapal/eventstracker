# syntax=docker/dockerfile:1

# Self-contained multi-stage build (same shape as runs-app). The old runtime-only Dockerfile
# existed because sathish-projects-logger was a local SNAPSHOT that could not be resolved
# inside Docker; that dependency is gone, so the jar is now built here.

# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# pom first so the dependency layer is cached until the pom changes
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

COPY src src

# Tests and the Spotless check run in CI before this image is built; do not let a formatting
# nit fail the image build.
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -Dspotless.check.skip=true -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Patch OS packages in the stage that actually ships (this is what clears Trivy's OS-level CVEs)
RUN apk update && apk upgrade --no-cache

RUN addgroup -S appgrp && adduser -S appuser -G appgrp

COPY --from=build --chown=appuser:appgrp /app/target/*.jar app.jar

USER appuser
EXPOSE 9081

HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
  CMD wget -qO- http://localhost:9081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
