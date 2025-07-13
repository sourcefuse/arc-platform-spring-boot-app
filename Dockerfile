# Builder stage
FROM maven:3.8-openjdk-21 AS builder

WORKDIR /build

# Copy dependency definitions first for layer caching
COPY pom.xml .

# Download dependencies and cache them
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ ./src/

# Build the application (skip tests for build efficiency)
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

# Set working directory
WORKDIR /app

# Install curl for health checks and create non-root user in one layer
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -r appuser && useradd -r -g appuser appuser

# Set application version as build argument
ARG APP_VERSION=1.0-SNAPSHOT

# Add metadata labels
LABEL org.opencontainers.image.version=$APP_VERSION \
      org.opencontainers.image.title="Spring Boot Application" \
      org.opencontainers.image.description="Spring Boot Application for Backstage Demo" \
      org.opencontainers.image.vendor="sourcefuse" \
      org.opencontainers.image.authors="devops@sourcefuse.com"

# Copy the JAR file from the builder stage
COPY --from=builder /build/target/*.jar /app/app.jar

# Set ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with JVM tuning
CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
