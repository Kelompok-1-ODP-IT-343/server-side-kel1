# syntax=docker/dockerfile:1

# ---- Build stage: uses Oracle JDK 21 and Maven Wrapper ----
FROM container-registry.oracle.com/java/jdk:21 AS build

WORKDIR /app

# Install curl for Maven Wrapper to download Maven if needed
RUN microdnf -y update && microdnf -y install curl && microdnf clean all

# Copy Maven wrapper and configuration first (better layer caching)
COPY .mvn/wrapper/maven-wrapper.properties .mvn/wrapper/maven-wrapper.properties
COPY mvnw mvnw
RUN chmod +x mvnw

# Copy pom for dependency resolution
COPY pom.xml pom.xml

# Pre-fetch dependencies to optimize build cache
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copy source
COPY src src

# Build the application (skip tests for faster image build)
RUN ./mvnw -q -DskipTests package


# ---- Runtime stage: Oracle JDK 21 ----
FROM container-registry.oracle.com/java/jdk:21

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Expose Spring Boot port
EXPOSE 8080

# Optional JVM and Spring profile configuration
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    SPRING_PROFILES_ACTIVE=default

# Run the application
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
