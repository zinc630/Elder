FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 先列出当前目录的所有文件，看构建上下文里到底有什么
RUN echo "--- Listing build context files ---" && ls -la

# 复制 JAR 包
COPY elder-backend-0.1.0-SNAPSHOT.jar app.jar

# 复制后再列出一次，确认文件是否复制成功
RUN echo "--- After COPY, listing files in /app ---" && ls -la

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]