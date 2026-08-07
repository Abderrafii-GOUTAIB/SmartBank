# FILE: Dockerfile   [NEW — at project root]

# ── BASE IMAGE ──────────────────────────────────────────────
# start from a minimal image with Java 21 runtime only (JRE, not JDK)
# → we only need to RUN the JAR, not compile it
FROM eclipse-temurin:21-jre-alpine

# ── WORKDIR ─────────────────────────────────────────────────
# everything happens in /app inside the container
WORKDIR /app

# ── COPY THE JAR ────────────────────────────────────────────
# copy the pre-built fat JAR into the image
# (this changes every build → near the bottom)
COPY smart-bank-app/target/smart-bank-app-1.0-SNAPSHOT.jar app.jar

# ── DATA DIRECTORY ──────────────────────────────────────────
# SmartBank writes to smartbank-data/ — create it
RUN mkdir -p /app/smartbank-data

# ── ENTRYPOINT ──────────────────────────────────────────────
# when the container STARTS, run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]






# FILE: Dockerfile   [multi-stage version]

# ═══════════════════════════════════════════════════════════
# STAGE 1 — BUILD (heavy: JDK + Maven, compiles everything)
# ═══════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jdk-alpine AS builder
#                                  └── name this stage "builder"

WORKDIR /build

# copy the Maven wrapper + pom files FIRST (for layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY smart-bank-domain/pom.xml smart-bank-domain/
COPY smart-bank-service/pom.xml smart-bank-service/
COPY smart-bank-app/pom.xml smart-bank-app/

# download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# NOW copy the source code (changes often → after deps)
COPY smart-bank-domain/src smart-bank-domain/src
COPY smart-bank-service/src smart-bank-service/src
COPY smart-bank-app/src smart-bank-app/src

# build the fat JAR
RUN ./mvnw clean package -DskipTests -B

# ═══════════════════════════════════════════════════════════
# STAGE 2 — RUNTIME (light: JRE only, just runs the JAR)
# ═══════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# copy ONLY the built JAR from the builder stage
# everything else (JDK, Maven, source) is LEFT BEHIND
COPY --from=builder /build/smart-bank-app/target/smart-bank-app-1.0-SNAPSHOT.jar app.jar
#         └── pull from stage 1

RUN mkdir -p /app/smartbank-data

ENTRYPOINT ["java", "-jar", "app.jar"]