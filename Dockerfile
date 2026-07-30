# Multi-stage Dockerfile for building and running the Spring Boot backend

# Builder stage: compile with Maven
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace
# Copy sources
COPY . /workspace
# Use Maven wrapper if present; otherwise use container's mvn
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

# Runtime stage: run the fat jar
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=builder /workspace/${JAR_FILE} app.jar
RUN mkdir -p /app/images
EXPOSE 8015
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV IMAGE_FOLDER=/app/images
VOLUME ["/app/images"]
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
