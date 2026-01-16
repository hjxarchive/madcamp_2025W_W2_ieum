# 이음 프로젝트 (IEUM)

Kotlin/JS + Spring Boot + PostgreSQL을 사용하는 풀스택 웹 애플리케이션

## 🏗️ 프로젝트 구조

```
ieum_private/
├── backend/                 # Spring Boot 백엔드 (Kotlin)
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── com/ieum/
│   │   │   │       ├── IeumApplication.kt
│   │   │   │       ├── config/
│   │   │   │       │   └── SecurityConfig.kt
│   │   │   │       └── controller/
│   │   │   │           └── HealthController.kt
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-local.yml
│   │   │       └── application-prod.yml
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── Dockerfile
│
├── frontend/                # Kotlin/JS 프론트엔드
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   ├── Main.kt
│   │   │   │   ├── App.kt
│   │   │   │   └── api/
│   │   │   │       └── ApiClient.kt
│   │   │   └── resources/
│   │   │       └── index.html
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── Dockerfile
│
├── database/                # PostgreSQL 설정
│   ├── docker-compose.yml
│   └── init.sql
│
├── nginx/                   # Nginx 설정
│   ├── nginx.conf
│   └── nginx-ssl.conf
│
├── scripts/                 # 배포 스크립트
│   └── setup-ec2.sh
│
├── docker-compose.yml       # 전체 애플리케이션 Docker Compose
├── deploy.sh               # 배포 스크립트
├── .env.example            # 환경 변수 예시
└── README.md
```

## 🚀 시작하기

### 필수 요구사항

- JDK 17 이상
- Gradle 8.5 이상
- Docker & Docker Compose
- PostgreSQL 16 (Docker 사용 시 불필요)

### 로컬 개발 환경 설정

#### 1. 환경 변수 설정
```bash
cp .env.example .env
# .env 파일을 열어 필요한 값들을 수정하세요
```

#### 2. PostgreSQL 실행 (Docker 사용)
```bash
cd database
docker-compose up -d
```

#### 3. 백엔드 실행
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### 4. 프론트엔드 실행
```bash
cd frontend
./gradlew browserDevelopmentRun --continuous
```

### Docker Compose로 전체 스택 실행

```bash
# 환경 변수 설정
cp .env.example .env

# 전체 스택 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 중지
docker-compose down
```

## 🌐 서비스 접속

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/api/health
- **Nginx**: http://localhost (Docker Compose 사용 시)

## 📦 빌드

### 백엔드 빌드
```bash
cd backend
./gradlew build
# JAR 파일: build/libs/ieum-backend-0.0.1-SNAPSHOT.jar
```

### 프론트엔드 빌드
```bash
cd frontend
./gradlew browserProductionWebpack
# 빌드 결과: build/distributions/
```

## 🚢 AWS EC2 배포

### 1. EC2 초기 설정
```bash
# EC2 인스턴스에 SSH 접속 후
chmod +x scripts/setup-ec2.sh
./scripts/setup-ec2.sh

# 로그아웃 후 재접속
```

### 2. 프로젝트 배포
```bash
# 프로젝트 클론
git clone <repository-url>
cd ieum_private

# 환경 변수 설정
cp .env.example .env
nano .env  # 프로덕션 환경에 맞게 수정

# 배포 실행
chmod +x deploy.sh
./deploy.sh
```

### 3. Nginx 설정 (선택사항)

SSL 인증서가 있는 경우:
```bash
# Let's Encrypt 설치
sudo apt-get install certbot python3-certbot-nginx

# SSL 인증서 발급
sudo certbot --nginx -d yourdomain.com

# nginx-ssl.conf 사용
sudo cp nginx/nginx-ssl.conf /etc/nginx/nginx.conf
sudo nginx -s reload
```

## 🔧 주요 기술 스택

### Backend
- **Kotlin** 1.9.21
- **Spring Boot** 3.2.1
- **Spring Data JPA**
- **Spring Security**
- **PostgreSQL** 16

### Frontend
- **Kotlin/JS** 1.9.21
- **React** 18.2.0 (Kotlin Wrappers)
- **Kotlin Emotion** (CSS-in-JS)

### Infrastructure
- **Docker** & **Docker Compose**
- **Nginx** (Reverse Proxy)
- **AWS EC2**
- **PostgreSQL** 16

## 📝 API 문서

### Health Check
```
GET /api/health
```

응답:
```json
{
  "status": "UP",
  "service": "ieum-backend"
}
```

## 🔐 보안 설정

- Spring Security 기본 설정 (CSRF 비활성화, CORS 활성화)
- PostgreSQL 비밀번호는 `.env` 파일로 관리
- 프로덕션 환경에서는 HTTPS 사용 권장
- Nginx rate limiting 적용

## 🐛 트러블슈팅

### 포트가 이미 사용 중인 경우
```bash
# 포트 사용 확인
lsof -i :8080
lsof -i :3000
lsof -i :5432

# 프로세스 종료
kill -9 <PID>
```

### Docker 컨테이너 로그 확인
```bash
docker-compose logs backend
docker-compose logs frontend
docker-compose logs postgres
```

### PostgreSQL 연결 오류
```bash
# 컨테이너 상태 확인
docker-compose ps

# PostgreSQL 재시작
docker-compose restart postgres
```

## 📚 추가 개발 가이드

### 새 API 엔드포인트 추가
1. `backend/src/main/kotlin/com/ieum/controller/`에 컨트롤러 추가
2. `backend/src/main/kotlin/com/ieum/service/`에 서비스 추가
3. `backend/src/main/kotlin/com/ieum/repository/`에 리포지토리 추가
4. `frontend/src/main/kotlin/api/ApiClient.kt`에 API 호출 함수 추가

### 데이터베이스 마이그레이션
- 개발: `application-local.yml`에서 `ddl-auto: update` 사용
- 프로덕션: Flyway 또는 Liquibase 사용 권장

## 📄 라이선스

이 프로젝트는 [LICENSE](LICENSE) 파일을 참조하세요.

## 👥 기여

이슈와 풀 리퀘스트를 환영합니다!

---

Made with ❤️ using Kotlin
