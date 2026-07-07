FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home --home-dir /app socialanalytics
COPY --from=build /workspace/target/socialanalytics-0.1.0.jar app.jar
USER socialanalytics
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
