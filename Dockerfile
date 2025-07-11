# Stage 1: Builder
FROM maven:3.8-openjdk-8 AS builder

WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime
FROM openjdk:8-jre-slim

# Create appuser and install curl
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    useradd -ms /bin/bash appuser && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership and switch to appuser
RUN chown -R appuser:appuser /app
USER appuser

# Expose the application port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD curl -f http://localhost:8080/ || exit 1

# Set the application version and label
ARG APP_VERSION
LABEL org.opencontainers.image.version=$APP_VERSION

# Command to run the application
CMD ["java", "-jar", "app.jar"]