FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the JAR file
COPY target/account_online.jar app.jar

# Create directory for customer images
RUN mkdir -p /app/customer-image/nid /app/customer-image/selfie

# Expose port
EXPOSE 9393

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --quiet --tries=1 --spider http://localhost:9393/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]