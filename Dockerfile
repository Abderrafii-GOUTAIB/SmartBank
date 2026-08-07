# ═══════════════════════════════════════════════════════════
# BUILD STAGE
# ═══════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -B

# ═══════════════════════════════════════════════════════════
# RUNTIME STAGE (optimized)
# ═══════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# non-root user + data dir + ownership — ONE layer
RUN addgroup -S bank && \
    adduser -S bank -G bank && \
    mkdir -p /app/smartbank-data

# copy the JAR from the build stage
COPY --from=builder /build/smart-bank-app/target/smart-bank-app-1.0-SNAPSHOT.jar app.jar

# give the non-root user ownership
RUN chown -R bank:bank /app

# run as non-root
USER bank

# container-aware JVM
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]