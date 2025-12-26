# ========================================
# Multi-stage build for Blog API
# ========================================

# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy gradle files for dependency caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Copy React frontend if exists
COPY login-app ./login-app

# Build the application (skip React build for now, will be handled separately if needed)
# If you want to include React build, uncomment the following:
# RUN gradle clean build -x test --no-daemon

# For now, build without React to avoid npm dependencies in Docker
RUN gradle clean bootJar -x test --no-daemon -x buildReact -x copyReactBuild

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy Firebase service account file (if exists)
# Note: In production, use secrets management instead
COPY --chown=spring:spring src/main/resources/firebase-service-account.json /app/firebase-service-account.json

# Change ownership to non-root user
RUN chown -R spring:spring /app

USER spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
