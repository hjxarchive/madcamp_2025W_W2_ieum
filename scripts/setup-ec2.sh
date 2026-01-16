#!/bin/bash

# EC2 초기 설정 스크립트

set -e

echo "🔧 EC2 서버 초기 설정 시작..."

# 패키지 업데이트
echo "📦 시스템 패키지 업데이트 중..."
sudo apt-get update -y
sudo apt-get upgrade -y

# Docker 설치
echo "🐳 Docker 설치 중..."
sudo apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io

# Docker 그룹에 현재 사용자 추가
sudo usermod -aG docker $USER

# Docker Compose 설치
echo "🔨 Docker Compose 설치 중..."
sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Git 설치
echo "📚 Git 설치 중..."
sudo apt-get install -y git

# 방화벽 설정
echo "🔥 방화벽 설정 중..."
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable

# 스왑 메모리 설정 (메모리가 부족한 경우)
if [ $(free -m | grep Swap | awk '{print $2}') -eq 0 ]; then
    echo "💾 스왑 메모리 설정 중..."
    sudo fallocate -l 2G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
fi

# 시스템 리소스 제한 설정
echo "⚙️  시스템 설정 최적화 중..."
sudo sysctl -w vm.max_map_count=262144
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf

echo ""
echo "✅ EC2 초기 설정 완료!"
echo ""
echo "⚠️  변경사항을 적용하려면 로그아웃 후 다시 로그인하세요."
echo ""
echo "다음 단계:"
echo "1. 프로젝트 클론: git clone <repository-url>"
echo "2. .env 파일 생성: cp .env.example .env"
echo "3. .env 파일 수정 (데이터베이스 비밀번호 등)"
echo "4. 배포 실행: ./deploy.sh"
