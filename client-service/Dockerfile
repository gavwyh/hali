FROM eclipse-temurin:17-jre-jammy

# Add a non-root user and install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/* \
    && groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

COPY build/libs/client-service-0.0.1-SNAPSHOT.jar client-service.jar

# Set ownership to the non-root user
RUN chown -R spring:spring /app

USER spring

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "client-service.jar"]