# 使用官方 OpenJDK 17 轻量镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制你的 JAR 包到容器里
COPY elder-backend-0.1.0-SNAPSHOT.jar app.jar

# 容器启动时运行的命令
CMD ["java", "-jar", "app.jar"]