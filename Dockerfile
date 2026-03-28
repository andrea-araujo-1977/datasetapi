# Multi-stage build for Spring Boot 4 / Java 25
FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

# Required runtime variables (set as requested)
ENV DB_USERNAME=app-user
ENV DB_PASSWORD=chapeu@2025
ENV SPOTIFY_CLIENT_ID=6bbe0697c1c64144826ecbc3bc2e86e2
ENV SPOTIFY_CLIENT_SECRET=cf36bc49d65e486eb4c8f4f4137cb4bc

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

