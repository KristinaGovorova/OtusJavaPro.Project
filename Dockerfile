# Фаза 1: Сборка приложения
FROM gradle:8.13-jdk17-alpine AS build
LABEL authors="KristinaGovorova"
WORKDIR /app
COPY . .
RUN gradle clean build

# Фаза 2: Создание образа
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]