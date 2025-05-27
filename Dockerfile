# Dockerfile
# Stage 1: Build jar
FROM eclipse-temurin:21-jdk-alpine as builder

WORKDIR /app

# Copy toàn bộ source code vào container
COPY . .

# Build jar (cần có Maven Wrapper trong dự án)
RUN ./mvnw clean package -DskipTests

# Stage 2: Chạy ứng dụng
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy file jar từ stage build sang
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
