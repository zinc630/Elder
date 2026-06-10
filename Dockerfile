# 阶段1：使用 Maven 镜像编译打包项目
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# 先复制 pom.xml 等依赖配置文件
COPY pom.xml .
COPY backend/pom.xml backend/

# 下载依赖（缓存层）
RUN mvn dependency:go-offline -pl backend

# 复制项目源码
COPY backend/src backend/src

# 执行打包命令，生成 JAR 包
RUN mvn clean package -DskipTests -pl backend

# 阶段2：使用轻量级 JRE 镜像运行
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 从构建阶段复制生成的 JAR 包
COPY --from=builder /app/backend/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]