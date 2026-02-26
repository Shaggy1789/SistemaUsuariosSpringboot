# Build stage with cache optimization
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy build files FIRST for better caching
COPY gradlew .
COPY gradle/wrapper gradle/wrapper
COPY build.gradle .
COPY settings.gradle .

#Damos permisos
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code AFTER
COPY src src

# Build application (uses cached dependencies)
RUN ./gradlew clean build -x test

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy JAR
COPY --from=builder /app/build/libs/*.jar app.jar

COPY --from=builder /app/src/main/resources/templates /app/templates
COPY --from=builder /app/src/main/resources/static /app/static

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]