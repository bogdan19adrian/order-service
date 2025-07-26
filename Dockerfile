
# --- Stage 1: Build with Maven ---
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /order-service

# Copy the whole Spring Boot project including pom.xml
COPY . .
# Clean and package the application (no tests)
RUN  mvn clean package -DskipTests

# --- Stage 2: Run with JDK ---
FROM eclipse-temurin:21-jdk
WORKDIR /order-service
COPY --from=builder /order-service/target/*.jar app.jar

# Optional: expose port 8080
EXPOSE 8080

# Runtime entry

ENTRYPOINT ["java", "-jar", "app.jar"]
