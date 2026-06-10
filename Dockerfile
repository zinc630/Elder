# 使用 Eclipse Temurin 官方维护的 OpenJDK 17 镜像
FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 复制你的 JAR 包到容器里
COPY elder-backend-0.1.0-SNAPSHOT.jar app.jar

# 容器启动命令
CMD ["java", "-jar", "app.jar"]