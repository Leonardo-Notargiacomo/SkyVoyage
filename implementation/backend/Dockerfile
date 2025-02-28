# backend/Dockerfile
FROM eclipse-temurin:21-jdk-jammy AS deps

WORKDIR /build

# Install Maven (required to generate mvnw)
RUN apt-get update && apt-get install -y maven

# Generate the mvnw wrapper and .mvn directory
RUN mvn -N io.takari:maven:wrapper

# Copy the parent pom.xml and all module directories
COPY pom.xml .
COPY datarecords/ ./datarecords
COPY persistence/ ./persistence
COPY persistence-api/ ./persistence-api
COPY businesslogic/ ./businesslogic
COPY businesslogic-api/ ./businesslogic-api
COPY restapi/ ./restapi
COPY assembler/ ./assembler

# Install all modules and dependencies
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean install -DskipTests

# Stage for building the application
FROM deps AS package

WORKDIR /build

# Build the project using the generated mvnw
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests

# Final stage for running the application
FROM eclipse-temurin:21-jre-jammy AS final

# Create a non-privileged user
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

# Copy the JAR file with dependencies from the build stage
COPY --from=package /build/assembler/target/assembler-1.0-SNAPSHOT-jar-with-dependencies.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]