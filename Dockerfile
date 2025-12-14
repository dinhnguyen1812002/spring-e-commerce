# Stage 1: Build the application
FROM maven:3.9.11-eclipse-temurin-25 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean install -DskipTests

# Stage 2: Create the final image
FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]



# Stage 2: Create the final image
#FROM eclipse-temurin:25-jdk-alpine
#WORKDIR /app
#COPY --from=build /app/target/*.jar app.jar