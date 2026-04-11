FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# Resolve wrapper/dependencies early to improve cache hit rate.
RUN ./gradlew --no-daemon -q help

COPY src ./src

RUN ./gradlew --no-daemon clean bootJar -x test \
    && JAR_PATH="$(ls build/libs/*.jar | grep -v -- '-plain\\.jar' | head -n 1)" \
    && test -n "$JAR_PATH" \
    && cp "$JAR_PATH" /workspace/app.jar

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

RUN useradd --create-home --uid 10001 appuser

COPY --from=build /workspace/app.jar /app/app.jar

USER appuser
EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
