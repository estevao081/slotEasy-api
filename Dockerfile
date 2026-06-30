FROM maven:3.9.9-eclipse-temurin-21 AS builder

RUN apt-get update && \
    apt-get install -y && \
    apt-get clean

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

EXPOSE 8080

COPY --from=builder /target/slotes-api-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]