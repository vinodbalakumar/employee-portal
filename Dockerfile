# Build stage
FROM maven:3.8-openjdk-11 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
ENV DB_HOST=host.docker.internal
ENV DB_PORT=3306
ENV DB_NAME=test
ENV DB_USERNAME=root
ENV DB_PASSWORD=root
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
