# ========================================
# Multi-stage build for Blog API
# ========================================

# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Install Node.js and npm for React build
RUN apt-get update && apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy gradle files for dependency caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Copy React frontend
COPY login-app ./login-app

# Build the application including React frontend
RUN gradle clean bootJar -x test --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Note: Firebase service account file is mounted via docker-compose.yml volume
# from secrets/firebase-service-account.json to /app/firebase-service-account.json

# Change ownership to non-root user
RUN chown -R spring:spring /app

USER spring

# Expose application port
# Note: Actual port is determined by APP_PORT environment variable (default: 8080)
EXPOSE 8080 80

# Health check
# Note: docker-compose.yml overrides this with dynamic port from APP_PORT
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD sh -c 'wget --no-verbose --tries=1 --spider http://localhost:${APP_PORT:-8080}/health || exit 1'

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
