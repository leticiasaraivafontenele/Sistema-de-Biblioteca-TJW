# ---- Estagio de build: compila o jar usando Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copia o pom primeiro para aproveitar o cache de dependencias do Docker
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Estagio de runtime: imagem enxuta apenas com a JRE ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
