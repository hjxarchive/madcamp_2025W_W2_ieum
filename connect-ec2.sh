#!/bin/bash

# EC2 SSH 접속 헬퍼 스크립트

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 키 파일 확인
KEY_FILE="./ieum_key.pem"
if [ ! -f "$KEY_FILE" ]; then
    echo -e "${RED}❌ 키 파일을 찾을 수 없습니다: $KEY_FILE${NC}"
    exit 1
fi

# 키 파일 권한 확인
KEY_PERMS=$(stat -f "%A" "$KEY_FILE" 2>/dev/null || stat -c "%a" "$KEY_FILE" 2>/dev/null)
if [ "$KEY_PERMS" != "400" ]; then
    echo -e "${YELLOW}⚠️  키 파일 권한 설정 중...${NC}"
    chmod 400 "$KEY_FILE"
fi

echo -e "${GREEN}✅ 키 파일 준비 완료${NC}"
echo ""

# EC2 IP 입력 받기
if [ -z "$1" ]; then
    echo -e "${YELLOW}EC2 퍼블릭 IP 주소를 입력하세요:${NC}"
    read EC2_IP
else
    EC2_IP=$1
fi

if [ -z "$EC2_IP" ]; then
    echo -e "${RED}❌ IP 주소가 입력되지 않았습니다.${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}🚀 EC2에 접속합니다...${NC}"
echo -e "   IP: ${YELLOW}$EC2_IP${NC}"
echo ""

# SSH 접속
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@"$EC2_IP"
