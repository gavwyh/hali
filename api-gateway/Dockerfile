FROM eclipse-temurin:17-jre-jammy

# Add a non-root user
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

COPY build/libs/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar

# Set ownership to the non-root user
RUN chown -R spring:spring /app

USER spring

EXPOSE 9000

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:9000/actuator/health || exit 1


ENTRYPOINT ["java", "-jar", "api-gateway.jar"]