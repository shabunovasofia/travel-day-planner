# --- Stage 1: сборка jar'а внутри docker, чтобы не зависеть от локального ./gradlew bootJar.
# Используем JDK 21 (toolchain в build.gradle).
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Сначала копируем только то, что нужно для разрешения зависимостей —
# это позволяет docker'у кэшировать слой с .gradle, если зависимости не менялись.
COPY gradlew settings.gradle build.gradle checkstyle.xml ./
COPY gradle ./gradle
RUN chmod +x ./gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Теперь исходники и сборка jar'а.
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# --- Stage 2: финальный slim-образ только с JRE и собранным jar.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
