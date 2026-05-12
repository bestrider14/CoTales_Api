FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY scripts ./scripts
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mkdir -p .git/hooks && mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# dotenv library expects .env to exist; real values come from container env vars
RUN touch .env
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
