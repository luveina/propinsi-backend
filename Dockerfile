# =============================================================
# Stage 1: BUILD
# Uses the full JDK image to compile the application with Maven.
# The Maven wrapper (mvnw) is used so no Maven installation is
# needed on the build machine / CI runner.
# =============================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy dependency descriptors first to leverage Docker layer caching.
# The Maven dependencies are only re-downloaded when pom.xml changes.
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy the rest of the source code and build the fat JAR.
# -DskipTests: tests are run in a dedicated CI stage, not during image build.
COPY src ./src

RUN ./mvnw package -DskipTests -B

# =============================================================
# Stage 2: RUNTIME
# Uses the slim JRE image (no compiler tooling) to keep the
# final image as small as possible.
# =============================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user for security best practices.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the fat JAR produced in the build stage.
COPY --from=builder /app/target/*.jar app.jar

# Transfer ownership so the non-root user can read the file.
RUN chown appuser:appgroup app.jar

USER appuser

# Expose the Spring Boot default port.
EXPOSE 8080

# Use exec form to ensure the JVM receives OS signals (SIGTERM) correctly.
# -XX:+UseContainerSupport lets the JVM honour cgroup memory/CPU limits.
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
