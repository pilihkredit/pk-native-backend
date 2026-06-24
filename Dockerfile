# For docker build only. Do not execute as a shell script.
# ==========================================
# Stage 1: Build pk-app and dependencies
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Limit Maven JVM memory to reduce OOM risk on low-memory hosts (exit 137).
ENV MAVEN_OPTS="-Xmx768m -Xms256m -XX:+UseG1GC -XX:MaxMetaspaceSize=256m"

COPY . .

# -T 1: single-threaded build to lower peak memory usage.
# repackage must run (see pk-app/pom.xml); fail the image build if the jar is not executable.
RUN mvn clean package -T 1 -DskipTests -Dmaven.test.skip=true -pl pk-app -am -B && \
    BOOT_JAR="pk-app/target/pk-app-0.1.0-SNAPSHOT.jar" && \
    test "$(stat -c%s "${BOOT_JAR}")" -gt 1000000 && \
    jar xf "${BOOT_JAR}" META-INF/MANIFEST.MF && \
    grep -q 'Start-Class:' META-INF/MANIFEST.MF && \
    cp "${BOOT_JAR}" /tmp/app.jar && \
    echo "pk-app build finished"

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /tmp/app.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Asia/Jakarta
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Jakarta"

EXPOSE 8831

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
