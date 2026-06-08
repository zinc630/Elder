FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests
COPY backend/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]