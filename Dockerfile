FROM eclipse-temurin:11-jre

WORKDIR /app

# Install runtime utilities for conversion scripts
RUN apt-get update && \
    apt-get install -y --no-install-recommends poppler-utils libvips-tools && \
    rm -rf /var/lib/apt/lists/*

# Create working directory for temporary conversion files and set OpenShift-compatible permissions
RUN mkdir -p /app/dei/temp /data/conversion && \
    chgrp -R 0 /app /data && \
    chmod -R g=u /app /data

# Copy built artifact from workspace
COPY target/dei-*.jar /app/app.jar

# Configuration environment variables
ENV SPRING_PROFILES_ACTIVE=dev \
    JAVA_OPTS="-Dfile.encoding=UTF-8 -Dhttps.protocols=TLSv1.3,TLSv1.2"

EXPOSE 8443 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
