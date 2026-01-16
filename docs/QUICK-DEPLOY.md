# 🚀 빠른 배포 가이드 (Quick Deploy)

EC2에 프로젝트를 배포하는 가장 빠른 방법입니다.

## 전제 조건
- AWS 계정
- EC2 인스턴스 (Ubuntu 22.04)
- SSH 키 파일 (`.pem`)

---

## 1단계: SSH 키 설정 (로컬)

```bash
# 키 파일 권한 설정
chmod 400 ~/Downloads/ieum-key.pem
mv ~/Downloads/ieum-key.pem ~/.ssh/
```

---

## 2단계: EC2 접속

```bash
# EC2_IP를 실제 IP로 변경
ssh -i ~/.ssh/ieum-key.pem ubuntu@<EC2_IP>
```

---

## 3단계: 초기 설정 (EC2에서 실행)

```bash
# 원스텝 설치
curl -fsSL https://get.docker.com -o get-docker.sh && \
sudo sh get-docker.sh && \
sudo usermod -aG docker $USER && \
sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose && \
sudo chmod +x /usr/local/bin/docker-compose && \
sudo apt-get install -y git

# 재접속 (docker 그룹 적용)
exit
ssh -i ~/.ssh/ieum-key.pem ubuntu@<EC2_IP>
```

---

## 4단계: 프로젝트 배포

### GitHub 사용

```bash
# 1. 프로젝트 클론
git clone https://github.com/yourusername/ieum_private.git
cd ieum_private

# 2. 환경 변수 설정
cp .env.example .env
nano .env  # DB 비밀번호 등 수정

# 3. 배포
chmod +x deploy.sh
./deploy.sh
```

### rsync 사용 (로컬에서 실행)

```bash
# 프로젝트 전송
rsync -avz --exclude 'build' --exclude '.gradle' \
  -e "ssh -i ~/.ssh/ieum-key.pem" \
  /Users/hjxarchive/ieum_private/ \
  ubuntu@<EC2_IP>:~/ieum_private/

# EC2 접속하여 배포
ssh -i ~/.ssh/ieum-key.pem ubuntu@<EC2_IP>
cd ieum_private
cp .env.example .env
nano .env
./deploy.sh
```

---

## 5단계: 접속 확인

브라우저에서 `http://<EC2_IP>` 접속

또는:
```bash
curl http://<EC2_IP>/api/health
```

---

## 완료! 🎉

서비스가 다음 주소에서 실행됩니다:
- Frontend: `http://<EC2_IP>`
- Backend: `http://<EC2_IP>/api`

---

## 명령어 모음

```bash
# 로그 확인
docker-compose logs -f

# 재시작
docker-compose restart

# 중지
docker-compose down

# 업데이트 및 재배포
git pull
docker-compose down
docker-compose up -d --build
```
