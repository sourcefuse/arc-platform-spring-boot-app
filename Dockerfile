FROM alpine:latest
# Stage 1: Build the application
FROM maven:3.8.7-eclipse-temurin-21 AS build

WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre

# Add a non-root user
RUN useradd -m springuser

WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/sample-spring-boot-app-backstage-1.0-SNAPSHOT.jar app.jar

# Change ownership of the application files
RUN chown -R springuser:springuser /app

# Switch to the non-root user
USER springuser

# Expose the application port
EXPOSE 8080

# Add metadata
LABEL maintainer="Your Name <your.email@example.com>"
LABEL org.opencontainers.image.source="https://github.com/your-repo/sample-spring-boot-app-backstage"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]