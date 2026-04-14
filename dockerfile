# Билд стадия
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Копируем только pom.xml для кэширования зависимостей
COPY pom.xml .

# Загружаем зависимости (кэшируется, если pom.xml не менялся)
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src src

# Собираем приложение
# RUN mvn clean package -DskipTests
RUN mvn clean package -Dmaven.test.skip=true

# Финальный образ
FROM eclipse-temurin:21-jre-alpine

# Создаем непривилегированного пользователя
RUN addgroup -S subtitle && adduser -S subtitle -G subtitle

WORKDIR /app

# Копируем собранный JAR из билд стадии
COPY --from=builder /app/target/*.jar app.jar

# Создаем необходимые директории
RUN mkdir -p /app/uploads /app/converted /app/logs && \
    chown -R subtitle:subtitle /app

# Переключаемся на непривилегированного пользователя
USER subtitle

# Открываем порт
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/subtitle/health || exit 1

# Запускаем приложение
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-Dfile.encoding=UTF-8", \
     "-jar", "app.jar"]