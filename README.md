# 이음 프로젝트 (IEUM)

Kotlin/JS Frontend + Spring Boot Backend + PostgreSQL + AWS EC2 + Nginx

## 🚀 빠른 시작

### 로컬 개발
```bash
# PostgreSQL 실행
cd database && docker-compose up -d

# 백엔드 실행
cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'

# 프론트엔드 실행 (새 터미널)
cd frontend && ./gradlew browserDevelopmentRun --continuous
```

### Docker로 전체 실행
```bash
cp .env.example .env
docker-compose up -d
```

### 접속
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- Health Check: http://localhost:8080/api/health

## 📁 프로젝트 구조
- `backend/` - Spring Boot (Kotlin)
- `frontend/` - Kotlin/JS + React
- `database/` - PostgreSQL 설정
- `nginx/` - Nginx 설정
- `scripts/` - 배포 스크립트

자세한 내용은 [README_DETAILED.md](README_DETAILED.md)를 참조하세요.
Madcamp ieum backend
