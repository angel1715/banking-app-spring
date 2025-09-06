# Etapa 1: build con Maven + JDK 17
FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar archivos de Maven
COPY pom.xml .
COPY src ./src

# Construir la app sin tests
RUN mvn clean package -DskipTests

# Etapa 2: runtime con JDK 17 liviano
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copiar el .jar generado en la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto (Render asigna dinámicamente $PORT)
EXPOSE 8080

# Comando para ejecutar la app
CMD ["java", "-jar", "app.jar"]