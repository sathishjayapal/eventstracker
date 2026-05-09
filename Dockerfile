# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src/ src/
RUN ./mvnw package -DskipTests -B && \
    mv target/*.jar target/app.jar

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
USER appuser
EXPOSE 9081
ENTRYPOINT ["java", "-jar", "app.jar"]
