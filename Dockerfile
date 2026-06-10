# 使用带 Java 的基础镜像（这里用 Java 17，和你项目保持一致）
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制你项目里的 JAR 包到容器里.
# 注意：你的 JAR 包文件名是 elder-backend-0.1.0-SNAPSHOT.jar，我已经帮你写好了
COPY elder-backend-0.1.0-SNAPSHOT.jar app.jar

# 暴露端口（和你的 Spring Boot 应用端口保持一致）
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]