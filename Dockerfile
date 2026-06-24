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
RUN mvn clean package -T 1 -DskipTests -Dmaven.test.skip=true -pl pk-app -am -B && \
    echo "pk-app build finished"

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/pk-app/target/pk-app-0.1.0-SNAPSHOT.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Asia/Jakarta
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Jakarta"

EXPOSE 8831

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
