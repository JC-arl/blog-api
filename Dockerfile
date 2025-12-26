# ========================================
# Multi-stage build for Blog API
# ========================================

# Stage 1: Frontend build
FROM node:20-alpine AS frontend

WORKDIR /login-app

# Build arguments for React environment variables
# NOTE: These are NOT secrets - they are client-side config values that will be
# embedded in the built JavaScript bundle and sent to browsers. Firebase API keys
# are protected by domain restrictions in Firebase Console, not by keeping them secret.
# hadolint ignore=DL3007,DL3009
ARG REACT_APP_FIREBASE_API_KEY
ARG REACT_APP_FIREBASE_AUTH_DOMAIN
ARG REACT_APP_FIREBASE_PROJECT_ID
ARG REACT_APP_FIREBASE_STORAGE_BUCKET
ARG REACT_APP_FIREBASE_MESSAGING_SENDER_ID
ARG REACT_APP_FIREBASE_APP_ID
ARG REACT_APP_KAKAO_REST_API_KEY
ARG REACT_APP_BACKEND_URL

# Set environment variables for React build
# These values will be embedded in the JavaScript bundle during build
ENV REACT_APP_FIREBASE_API_KEY=$REACT_APP_FIREBASE_API_KEY \
    REACT_APP_FIREBASE_AUTH_DOMAIN=$REACT_APP_FIREBASE_AUTH_DOMAIN \
    REACT_APP_FIREBASE_PROJECT_ID=$REACT_APP_FIREBASE_PROJECT_ID \
    REACT_APP_FIREBASE_STORAGE_BUCKET=$REACT_APP_FIREBASE_STORAGE_BUCKET \
    REACT_APP_FIREBASE_MESSAGING_SENDER_ID=$REACT_APP_FIREBASE_MESSAGING_SENDER_ID \
    REACT_APP_FIREBASE_APP_ID=$REACT_APP_FIREBASE_APP_ID \
    REACT_APP_KAKAO_REST_API_KEY=$REACT_APP_KAKAO_REST_API_KEY \
    REACT_APP_BACKEND_URL=$REACT_APP_BACKEND_URL

# Copy package files
COPY login-app/package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY login-app/ ./

# Build React app
RUN npm run build

# Stage 2: Backend build
FROM gradle:8.5-jdk21 AS backend

WORKDIR /app

# Copy gradle files for dependency caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Copy React build from frontend stage directly to static resources
COPY --from=frontend /login-app/build ./src/main/resources/static

# Build Spring Boot application (skip npm install and React build tasks)
RUN gradle clean bootJar -x test -x npmInstall -x buildReact -x copyReactBuild --no-daemon

# Stage 3: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy built jar from backend stage
COPY --from=backend /app/build/libs/*.jar app.jar

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
