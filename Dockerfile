# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
ENV DB_HOST=host.docker.internal
ENV DB_PORT=3306
ENV DB_NAME=tesla
ENV DB_USERNAME=root
ENV DB_PASSWORD=root
ENV TESLA_PROXY_BASE=https://host.docker.internal:4443
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
