# syntax=docker/dockerfile:1

# Stage for resolving and downloading dependencies
FROM eclipse-temurin:21-jdk-jammy AS deps

WORKDIR /build

# Install Maven (required to generate mvnw)
RUN apt-get update && apt-get install -y maven

# Generate the mvnw wrapper and .mvn directory
RUN mvn -N io.takari:maven:wrapper

# Copy the parent pom.xml and all module directories
COPY backend/pom.xml pom.xml
COPY backend/datarecords ./datarecords
COPY backend/persistence ./persistence
COPY backend/persistence-api ./persistence-api
COPY backend/businesslogic ./businesslogic
COPY backend/businesslogic-api ./businesslogic-api
COPY backend/restapi ./restapi
COPY backend/assembler ./assembler

# Install all modules and dependencies
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean install -DskipTests

# Stage for building the application
FROM deps AS package

WORKDIR /build

# Copy the rest of the backend source code (if any additional files are needed)
COPY backend/ .

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