# Runtime only — JAR is built by Maven on the host (or in CI) before docker build.
# This avoids the sathish-projects-logger SNAPSHOT resolution issue inside Docker.
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
WORKDIR /app
COPY target/app.jar app.jar
USER appuser
EXPOSE 9081
ENTRYPOINT ["java", "-jar", "app.jar"]
