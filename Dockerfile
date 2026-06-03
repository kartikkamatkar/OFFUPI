# ============================================================================
# DOCKERFILE - Builds the Spring Boot application as a Docker container
# ============================================================================

# Base image: Eclipse Temurin (OpenJDK) Java 21 JDK
# This image contains Java development tools needed to compile and run
FROM eclipse-temurin:21-jdk

# Set working directory inside the container
# All subsequent commands run from this directory
WORKDIR /app

# Copy ALL files from current directory (host) to /app (container)
# The dot (.) means "everything" - source code, pom.xml, etc.
COPY . .

# Make the Maven wrapper script executable
# mvnw is the Maven wrapper - downloads Maven if not installed
RUN chmod +x mvnw

# Compile and package the application
# clean: delete old compiled files
# package: compile and create JAR file
# -DskipTests: skip running tests (faster build)
RUN ./mvnw clean package -DskipTests

# Declare that this container listens on port 8080
# This is documentation - doesn't actually open the port
EXPOSE 8080

# Command to run when container starts
# Exec form (JSON array) for proper signal handling
# Runs the compiled JAR file
CMD ["java", "-jar", "target/OFFUPI-0.0.1-SNAPSHOT.jar"]