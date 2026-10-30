# --- build: compila o JAR com Maven ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# baixa dependencias primeiro (camada reaproveitada enquanto o pom nao muda)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# --- runtime: apenas a JRE + o JAR ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/semeando-digital-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
