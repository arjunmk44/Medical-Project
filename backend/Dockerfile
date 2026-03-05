FROM eclipse-temurin:17-jdk as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven && \
    mvn package -DskipTests -B

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
