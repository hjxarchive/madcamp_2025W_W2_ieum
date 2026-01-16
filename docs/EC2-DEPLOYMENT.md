# AWS EC2 배포 가이드

## 1️⃣ EC2 인스턴스 생성

### AWS Console에서 EC2 인스턴스 시작

1. **AWS Console** → **EC2** → **인스턴스 시작**

2. **이름 및 태그**
   - 이름: `ieum-server`

3. **애플리케이션 및 OS 이미지 (AMI)**
   - Ubuntu Server 22.04 LTS (HVM), SSD Volume Type
   - 64비트 (x86)

4. **인스턴스 유형**
   - 개발/테스트: `t2.micro` (프리티어)
   - 프로덕션: `t3.small` 이상 권장

5. **키 페어(로그인)**
   - 새 키 페어 생성
   - 키 페어 이름: `ieum-key`
   - 키 페어 유형: RSA
   - 프라이빗 키 파일 형식: `.pem`
   - **다운로드한 키 저장**: `~/Downloads/ieum-key.pem`

6. **네트워크 설정**
   - VPC: 기본값
   - 서브넷: 기본값
   - 퍼블릭 IP 자동 할당: **활성화**
   - 보안 그룹 생성:
     ```
     SSH (22)    - 내 IP (또는 특정 IP)
     HTTP (80)   - 0.0.0.0/0
     HTTPS (443) - 0.0.0.0/0
     ```

7. **스토리지 구성**
   - 최소 20 GB (권장: 30 GB)
   - gp3 (범용 SSD)

8. **인스턴스 시작**

---

## 2️⃣ 로컬에서 EC2 연결 설정

### 키 파일 권한 설정
```bash
# 키 파일을 안전한 위치로 이동
mkdir -p ~/.ssh
mv ~/Downloads/ieum-key.pem ~/.ssh/

# 권한 설정 (필수!)
chmod 400 ~/.ssh/ieum-key.pem
```

### SSH 접속
```bash
# EC2 퍼블릭 IP 또는 DNS로 접속
ssh -i ~/.ssh/ieum-key.pem ubuntu@<EC2-PUBLIC-IP>

# 예시:
# ssh -i ~/.ssh/ieum-key.pem ubuntu@52.79.123.45
```

### SSH Config 설정 (선택사항 - 편리함)
```bash
# ~/.ssh/config 파일 생성/편집
nano ~/.ssh/config
```

다음 내용 추가:
```
Host ieum-server
    HostName <EC2-PUBLIC-IP>
    User ubuntu
    IdentityFile ~/.ssh/ieum-key.pem
    ServerAliveInterval 60
```

이제 간단하게 접속:
```bash
ssh ieum-server
```

---

## 3️⃣ EC2 초기 설정

### EC2에 접속 후 실행
```bash
# 시스템 업데이트
sudo apt-get update && sudo apt-get upgrade -y

# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# Git 설치
sudo apt-get install -y git

# 재접속 (docker 그룹 적용을 위해)
exit
ssh ieum-server
```

또는 준비된 스크립트 사용:
```bash
# 로컬에서 스크립트 전송
scp -i ~/.ssh/ieum-key.pem scripts/setup-ec2.sh ubuntu@<EC2-PUBLIC-IP>:~/

# EC2에서 실행
ssh ieum-server
chmod +x setup-ec2.sh
./setup-ec2.sh
```

---

## 4️⃣ 프로젝트 배포

### 방법 1: GitHub를 통한 배포 (권장)

#### 로컬에서 코드 푸시
```bash
# 로컬 프로젝트에서
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/yourusername/ieum_private.git
git push -u origin main
```

#### EC2에서 클론 및 배포
```bash
# EC2에 접속
ssh ieum-server

# 프로젝트 클론
git clone https://github.com/yourusername/ieum_private.git
cd ieum_private

# 환경 변수 설정
cp .env.example .env
nano .env  # 프로덕션 값으로 수정
```

`.env` 파일 예시:
```env
DB_NAME=ieum_db
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password_123!@#
PROFILE=prod
DDL_AUTO=validate
```

#### 배포 실행
```bash
# 배포 스크립트 실행
chmod +x deploy.sh
./deploy.sh
```

### 방법 2: rsync로 직접 전송

```bash
# 로컬에서 실행
rsync -avz --exclude 'build' --exclude '.gradle' --exclude 'node_modules' \
  -e "ssh -i ~/.ssh/ieum-key.pem" \
  /Users/hjxarchive/ieum_private/ \
  ubuntu@<EC2-PUBLIC-IP>:~/ieum_private/

# EC2에 접속하여 배포
ssh ieum-server
cd ieum_private
cp .env.example .env
nano .env  # 설정 수정
./deploy.sh
```

---

## 5️⃣ 서비스 확인

### 접속 테스트
```bash
# 로컬에서 테스트
curl http://<EC2-PUBLIC-IP>/api/health

# 브라우저에서 접속
# http://<EC2-PUBLIC-IP>
```

### 로그 확인
```bash
# EC2에서
cd ~/ieum_private

# 전체 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### 컨테이너 상태 확인
```bash
docker-compose ps
docker ps -a
```

---

## 6️⃣ 도메인 연결 (선택사항)

### Route 53에서 도메인 설정
1. AWS Route 53 → 호스팅 영역 → 도메인 선택
2. 레코드 생성:
   - 레코드 이름: `@` (또는 `www`)
   - 레코드 유형: A
   - 값: EC2 퍼블릭 IP
   - TTL: 300

### Elastic IP 할당 (권장)
```bash
# AWS Console → EC2 → Elastic IP → Elastic IP 주소 할당
# EC2 인스턴스에 연결
```

이유: EC2를 재시작하면 퍼블릭 IP가 변경되는데, Elastic IP는 고정 IP를 제공합니다.

---

## 7️⃣ SSL/HTTPS 설정

### Let's Encrypt로 무료 SSL 인증서 발급

#### EC2에서 실행
```bash
# Certbot 설치
sudo apt-get install -y certbot python3-certbot-nginx

# Nginx 직접 설치 (Docker 외부)
sudo apt-get install -y nginx

# Nginx 설정
sudo nano /etc/nginx/sites-available/ieum
```

Nginx 설정 파일:
```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
# 심볼릭 링크 생성
sudo ln -s /etc/nginx/sites-available/ieum /etc/nginx/sites-enabled/

# 기본 사이트 비활성화
sudo rm /etc/nginx/sites-enabled/default

# Nginx 테스트 및 재시작
sudo nginx -t
sudo systemctl restart nginx

# SSL 인증서 발급
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# 자동 갱신 설정 확인
sudo certbot renew --dry-run
```

---

## 8️⃣ 자동 배포 설정 (선택사항)

### GitHub Actions CI/CD

프로젝트에 `.github/workflows/deploy.yml` 생성:

```yaml
name: Deploy to EC2

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Deploy to EC2
      uses: appleboy/ssh-action@master
      with:
        host: ${{ secrets.EC2_HOST }}
        username: ubuntu
        key: ${{ secrets.EC2_SSH_KEY }}
        script: |
          cd ~/ieum_private
          git pull origin main
          docker-compose down
          docker-compose up -d --build
```

GitHub Secrets 설정:
- `EC2_HOST`: EC2 퍼블릭 IP
- `EC2_SSH_KEY`: `ieum-key.pem` 내용

---

## 9️⃣ 모니터링 및 유지보수

### 로그 로테이션
```bash
# EC2에서
sudo nano /etc/logrotate.d/docker-compose
```

```
/home/ubuntu/ieum_private/logs/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
}
```

### 자동 재시작 설정
```bash
# Docker 컨테이너 자동 재시작은 docker-compose.yml에 이미 설정됨
# restart: unless-stopped
```

### 백업 스크립트
```bash
# ~/backup.sh
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker exec ieum-postgres pg_dump -U postgres ieum_db > ~/backups/db_$DATE.sql
find ~/backups -type f -mtime +7 -delete  # 7일 이상 된 백업 삭제
```

---

## 🔧 트러블슈팅

### Docker 빌드 실패
```bash
# 로그 확인
docker-compose logs backend

# 컨테이너 재빌드
docker-compose build --no-cache backend
docker-compose up -d
```

### 메모리 부족
```bash
# 스왑 메모리 생성 (setup-ec2.sh에 포함됨)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

### 포트가 이미 사용 중
```bash
# 포트 사용 확인
sudo lsof -i :80
sudo lsof -i :8080

# 프로세스 종료
sudo kill -9 <PID>
```

### Docker 디스크 공간 정리
```bash
# 사용하지 않는 이미지/컨테이너 정리
docker system prune -a
docker volume prune
```

---

## 📊 비용 예상 (AWS)

### 프리티어 (1년간 무료)
- EC2 t2.micro: 750시간/월
- EBS 30GB
- 데이터 전송 15GB/월

### 프리티어 이후 (서울 리전)
- t3.small: 약 $15/월
- EBS 30GB: 약 $3/월
- Elastic IP: 무료 (사용 중일 때)
- 데이터 전송: 약 $0.126/GB

**예상 비용**: 월 $20-30

---

## ✅ 체크리스트

- [ ] EC2 인스턴스 생성
- [ ] 보안 그룹 설정 (SSH, HTTP, HTTPS)
- [ ] SSH 키 권한 설정 (`chmod 400`)
- [ ] EC2 접속 확인
- [ ] Docker & Docker Compose 설치
- [ ] 프로젝트 배포 (Git 또는 rsync)
- [ ] `.env` 파일 설정
- [ ] `./deploy.sh` 실행
- [ ] 서비스 접속 확인
- [ ] (선택) 도메인 연결
- [ ] (선택) SSL 인증서 설정
- [ ] (선택) 백업 설정

---

## 📞 도움이 필요한 경우

AWS 공식 문서:
- [EC2 시작하기](https://docs.aws.amazon.com/ko_kr/AWSEC2/latest/UserGuide/EC2_GetStarted.html)
- [Ubuntu에 Docker 설치](https://docs.docker.com/engine/install/ubuntu/)
