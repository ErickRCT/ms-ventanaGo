# Imagen para Render (u otro servicio con Docker). Compila con Maven y ejecuta el jar con Java 17.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Plan gratuito de Render: 512 MB de RAM. La JVM usa como máximo el 75 % para el heap.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k"
EXPOSE 7099
ENTRYPOINT ["java", "-jar", "app.jar"]
