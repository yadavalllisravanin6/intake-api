# --- Build stage: compiles the app with Maven ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# --- Run stage: smaller image, just the JRE + the built jar ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render sets $PORT at runtime - our application.properties already reads
# server.port=${PORT:8080}, so no extra config needed here.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]