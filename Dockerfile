# syntax=docker/dockerfile:1

# ---- Build stage: use Temurin JDK 21 and Maven for build ----
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom for dependency resolution
COPY pom.xml .

# Pre-fetch dependencies to optimize build cache
RUN mvn -q -DskipTests dependency:go-offline

# Copy source
COPY src ./src

# Build the application (skip tests for faster image build)
RUN mvn -q -DskipTests package


# ---- Runtime stage: Temurin JRE 21 ----
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Expose Spring Boot port
EXPOSE 18080

# Optional JVM and Spring profile configuration
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    SPRING_PROFILES_ACTIVE=default

# Run the application
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
