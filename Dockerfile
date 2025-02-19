# Dockerfile
FROM openjdk:11-jdk-slim as build

WORKDIR /app

# Instalar certificados y curl
RUN apt-get update && \
    apt-get install -y ca-certificates curl && \
    update-ca-certificates

# Copiar archivos del proyecto
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Asegurarse de que el directorio resources existe
RUN mkdir -p src/main/resources

# Dar permisos de ejecución al gradlew
RUN chmod +x ./gradlew

# Construir el proyecto
RUN ./gradlew clean build -x test

# Imagen final
FROM openjdk:11-jre-slim

WORKDIR /app

# Instalar curl para el health check
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Copiar el jar construido
COPY --from=build /app/build/libs/*.jar app.jar

# Puerto expuesto
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]