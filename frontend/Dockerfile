# Frontend Dockerfile
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Gradle 캐시를 활용하기 위해 의존성 먼저 복사
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

# 애플리케이션 빌드
RUN gradle browserProductionWebpack --no-daemon

# Nginx로 정적 파일 서빙
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# 빌드된 정적 파일 복사
COPY --from=build /app/build/distributions/* ./

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
