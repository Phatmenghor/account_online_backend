# Build stage - Maven builds the JAR inside Docker
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests

# List the target directory to verify JAR was created
RUN ls -lh /app/target/

# Runtime stage - Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from build stage (using the finalName from pom.xml)
COPY --from=build /app/target/account_online.jar app.jar

# Verify JAR was copied
RUN ls -lh /app/

# Set ownership
RUN chown -R spring:spring /app
USER spring:spring

# Expose port
EXPOSE 9000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9000/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]