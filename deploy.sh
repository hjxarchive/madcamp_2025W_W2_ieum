#!/bin/bash

# EC2 배포 스크립트

set -e

echo "🚀 이음 프로젝트 배포 시작..."

# 환경 변수 체크
if [ ! -f .env ]; then
    echo "⚠️  .env 파일이 없습니다. .env.example을 복사하여 .env를 생성하세요."
    exit 1
fi

# Docker 및 Docker Compose 설치 확인
if ! command -v docker &> /dev/null; then
    echo "❌ Docker가 설치되어 있지 않습니다."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose가 설치되어 있지 않습니다."
    exit 1
fi

# 이전 컨테이너 정리
echo "🧹 이전 컨테이너 정리 중..."
docker-compose down -v

# 이미지 빌드
echo "🔨 Docker 이미지 빌드 중..."
docker-compose build --no-cache

# 컨테이너 실행
echo "🚢 컨테이너 실행 중..."
docker-compose up -d

# 헬스 체크
echo "🏥 서비스 헬스 체크 중..."
sleep 10

# PostgreSQL 헬스 체크
if docker exec ieum-postgres pg_isready -U postgres > /dev/null 2>&1; then
    echo "✅ PostgreSQL 정상 작동"
else
    echo "❌ PostgreSQL 오류"
    exit 1
fi

# Backend 헬스 체크
for i in {1..30}; do
    if curl -s http://localhost:8080/api/health > /dev/null; then
        echo "✅ Backend 정상 작동"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Backend 시작 실패"
        docker-compose logs backend
        exit 1
    fi
    echo "⏳ Backend 시작 대기 중... ($i/30)"
    sleep 2
done

# Frontend 헬스 체크
if curl -s http://localhost > /dev/null; then
    echo "✅ Frontend(Nginx) 정상 작동"
else
    echo "❌ Frontend 오류"
    exit 1
fi

echo ""
echo "🎉 배포 완료!"
echo ""
echo "📍 서비스 접속 정보:"
echo "   Frontend: http://localhost:3000"
echo "   Backend API: http://localhost:8080/api"
echo "   Nginx: http://localhost"
echo ""
echo "📊 로그 확인: docker-compose logs -f"
echo "🛑 중지: docker-compose down"
