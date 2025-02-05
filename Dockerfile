# Use a multi-stage build to reduce the final image size
FROM gradle:8.3-jdk17 AS build

WORKDIR /app

# Copy build files
COPY gradle ./gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .

# Resolve dependencies and build the application
RUN ./gradlew build -x test

# Create a smaller image for the application
FROM openjdk:17-alpine

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Expose the port the application runs on (adjust if needed)
EXPOSE 8080

# Set the startup command
CMD ["java", "-jar", "app.jar"]