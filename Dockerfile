# Dockerfile
FROM openjdk:11-jdk-slim as build

WORKDIR /app

# Copiar archivos del proyecto
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Dar permisos de ejecución al gradlew
RUN chmod +x ./gradlew

# Construir el proyecto
RUN ./gradlew clean build -x test

# Imagen final
FROM openjdk:11-jre-slim

WORKDIR /app

# Copiar el jar construido
COPY --from=build /app/build/libs/*.jar app.jar

# Puerto expuesto
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]