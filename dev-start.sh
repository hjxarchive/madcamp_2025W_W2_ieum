#!/bin/bash

# 개발 환경 실행 스크립트

echo "🔧 개발 환경 시작..."

# PostgreSQL 실행
echo "📦 PostgreSQL 시작 중..."
cd database
docker-compose up -d
cd ..

# 백엔드 실행 준비 대기
sleep 5

# 백엔드 실행 (백그라운드)
echo "🚀 백엔드 시작 중..."
cd backend
./gradlew bootRun --args='--spring.profiles.active=local' &
BACKEND_PID=$!
cd ..

# 프론트엔드 실행 준비 대기
sleep 10

# 프론트엔드 실행 (백그라운드)
echo "🎨 프론트엔드 시작 중..."
cd frontend
./gradlew browserDevelopmentRun --continuous &
FRONTEND_PID=$!
cd ..

echo ""
echo "✅ 개발 환경이 시작되었습니다!"
echo ""
echo "📍 접속 정보:"
echo "   Frontend: http://localhost:8081"
echo "   Backend: http://localhost:8080/api"
echo "   Health: http://localhost:8080/api/health"
echo ""
echo "🛑 중지하려면 Ctrl+C를 누르세요"
echo ""

# 종료 시그널 처리
trap "echo ''; echo '🛑 개발 환경을 종료합니다...'; kill $BACKEND_PID $FRONTEND_PID; cd database; docker-compose down; exit" INT TERM

# 프로세스 대기
wait
