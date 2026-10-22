# Stage 1: Build application with Maven and Java 11
FROM maven:3.8.8-eclipse-temurin-11 AS builder

WORKDIR /build

# Copy dependency definitions for build caching
COPY pom.xml ./
COPY package.json package-lock.json ./
RUN mvn dependency:go-offline -B || true

# Copy source files and configuration
COPY src ./src
COPY webpack.config.js webpack.generated.js tsconfig.json types.d.ts ./

# Build application package with Vaadin frontend pre-compiled (production mode)
ARG BUILD_PROFILE=prod
RUN mvn clean package -DskipTests -P ${BUILD_PROFILE}

# Stage 2: Runtime environment with Java 11
FROM eclipse-temurin:11-jre

WORKDIR /app

# Install runtime utilities for conversion scripts
RUN apt-get update && \
    apt-get install -y --no-install-recommends poppler-utils libvips-tools && \
    rm -rf /var/lib/apt/lists/*

# Create working directory for temporary conversion files
RUN mkdir -p /app/dei/temp

# Copy built artifact from builder stage
COPY --from=builder /build/target/dei-*.jar /app/app.jar

# Configuration environment variables
ENV SPRING_PROFILES_ACTIVE=dev \
    JAVA_OPTS="-Dfile.encoding=UTF-8 -Dhttps.protocols=TLSv1.3,TLSv1.2"

EXPOSE 8443 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
