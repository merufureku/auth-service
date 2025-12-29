# Builder stage: use an Eclipse Temurin JDK 25 image so the Gradle wrapper can run inside the container
FROM eclipse-temurin:25-jdk-jammy AS builder

# Set the working directory inside the builder container
WORKDIR /home/gradle/project

# Copy Gradle wrapper and wrapper jar to leverage layer caching (this avoids re-downloading wrapper each build)
COPY gradlew gradlew.bat gradle/ ./

# Copy project build files first to leverage Docker cache for dependency resolution
COPY build.gradle settings.gradle ./

# Copy the Gradle wrapper directory contents (gradle/wrapper) so the wrapper jar and properties are present
COPY gradle/wrapper/ gradle/wrapper/

# Copy the src directory to include the application sources
COPY src ./src

# Make the Gradle wrapper executable (required when using the wrapper inside the container)
RUN chmod +x ./gradlew

# Build the application JAR using the Gradle wrapper (produces a bootJar in build/libs)
# --no-daemon keeps it simple and stable inside containers
RUN ./gradlew clean bootJar --no-daemon


# Runtime stage: use a small, production-ready JRE image
FROM eclipse-temurin:25-jre-jammy

# Set a non-root working directory for running the app
WORKDIR /app

# Create non-root user and group
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the built JAR from the builder stage into the runtime image
# Use a glob because Gradle will produce build/libs/<name>-<version>.jar
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# Give ownership to the spring user
RUN chown spring:spring app.jar

# Switch to non-root user (from this point forward, everything runs as 'spring')
USER spring

# Expose the default Spring Boot port (optional metadata for users)
EXPOSE 8081

# Use a minimal, safe command to run the Spring Boot fat JAR
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app/app.jar"]
