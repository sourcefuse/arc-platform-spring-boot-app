# Multi-stage Dockerfile for Spring Boot Application

# Builder Stage
FROM maven:3.8-openjdk-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application
COPY src ./src
RUN mvn package -DskipTests -B

# Runtime Stage
FROM openjdk:17-jre-slim

# Create a user and group for running the application
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && groupadd -r appuser && useradd -r -g appuser appuser \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/sample-spring-boot-app-backstage-1.0-SNAPSHOT.jar /app/app.jar

# Change ownership of the application files
RUN chown -R appuser:appuser /app

# Switch to the non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Define the health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD curl -f http://localhost:8080/ || exit 1

# Set the entry point
CMD ["java", "-jar", "/app/app.jar"]

# Metadata
ARG APP_VERSION="1.0-SNAPSHOT"
LABEL org.opencontainers.image.version=$APP_VERSION
