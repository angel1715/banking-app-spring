# Usa una imagen oficial de Java 21 (compatible con tu proyecto)
FROM eclipse-temurin:21-jdk

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el JAR compilado al contenedor
COPY target/Banking-App-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto 8080 (o el que uses en tu app)
EXPOSE 8080

# Comando para ejecutar tu aplicación Spring Boot
CMD ["java", "-jar", "app.jar"]