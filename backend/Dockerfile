# 使用 Java 17 作为基础镜像（和你的 Spring Boot 版本对应）
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 和源码
COPY pom.xml .
COPY src ./src

# 安装 Maven 并打包项目
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

# 复制打包好的 jar 包并重命名
COPY target/*.jar app.jar

# 启动命令
CMD ["java", "-jar", "app.jar"]