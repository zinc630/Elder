FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]