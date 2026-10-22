FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --uid 10001 appuser

COPY --from=build /app/target/*.jar /app/app.jar

USER 10001

EXPOSE 9999

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=65.0", "-jar", "/app/app.jar"]
