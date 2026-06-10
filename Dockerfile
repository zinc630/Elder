# 改用 Eclipse Temurin 官方镜像，稳定且不会失效
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制你的 JAR 包，注意文件名要和你项目里的一致
COPY elder-backend-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]