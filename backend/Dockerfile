# Backend Dockerfile
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Gradle 캐시를 활용하기 위해 의존성 먼저 복사
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

# 애플리케이션 빌드
RUN gradle build --no-daemon -x test

# 런타임 이미지
FROM eclipse-temurin:17-jre
WORKDIR /app

# 보안을 위해 non-root 사용자 생성
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# 빌드된 jar 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 환경 변수 설정
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV PROFILE="prod"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$PROFILE -jar app.jar"]
