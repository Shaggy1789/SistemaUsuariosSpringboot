# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copiar archivos de Gradle
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Dar permisos y descargar dependencias
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Copiar código fuente
COPY src src

# Construir la aplicación - USAR bootJar explícitamente
RUN ./gradlew clean bootJar

# Verificar que el JAR se creó (útil para debug)
RUN ls -la /app/build/libs/

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el JAR (con nombre específico)
COPY --from=builder /app/build/libs/app.jar app.jar

# Verificar que el JAR se copió (útil para debug)
RUN ls -la /app/

# Crear directorios necesarios (opcional)
RUN mkdir -p /app/templates /app/static

# Exponer puerto
EXPOSE 8080

# Ejecutar (usando variable de entorno PORT)
ENTRYPOINT ["java", "-jar", "app.jar"]