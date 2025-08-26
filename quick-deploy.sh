#!/bin/bash

# JobHub Backend 원클릭 배포 스크립트 (EC2용)
set -e

echo "🚀 JobHub Backend 원클릭 배포를 시작합니다..."

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 함수 정의
log_info() {
    echo -e "${BLUE}ℹ️ $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 환경 변수 설정
PROJECT_NAME="jobhub-backend"
CONTAINER_NAME="jobhub-app"
IMAGE_NAME="jobhub-backend"
PORT="8080"
GITHUB_REPO="https://github.com/yjyj0234/JobHub-backend.git"
BRANCH="son"
WORK_DIR="/home/$USER/jobhub"

# 1. 사전 준비 확인
log_info "시스템 환경 확인 중..."

# Docker 설치 확인
if ! command -v docker &> /dev/null; then
    log_warning "Docker가 설치되지 않았습니다. 설치를 진행합니다..."
    
    # OS 확인
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$NAME
    else
        log_error "운영체제를 확인할 수 없습니다."
        exit 1
    fi
    
    # 시스템 업데이트
    log_info "시스템 업데이트 중..."
    if [[ "$OS" == *"Amazon Linux"* ]]; then
        sudo yum update -y
        sudo yum install -y docker curl git
    elif [[ "$OS" == *"Ubuntu"* ]]; then
        sudo apt-get update -y
        curl -fsSL https://get.docker.com -o get-docker.sh
        sudo sh get-docker.sh
        rm get-docker.sh
    fi
    
    # Docker 서비스 시작
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -a -G docker $USER
    
    log_success "Docker 설치 완료"
    log_warning "Docker 그룹 적용을 위해 터미널을 재시작하거나 'newgrp docker' 명령어를 실행하세요."
    
    # 그룹 변경 시도
    exec sg docker "$0 $*"
fi

log_success "Docker가 준비되었습니다."

# 2. 작업 디렉토리 준비
log_info "작업 디렉토리 준비 중..."
mkdir -p $WORK_DIR
cd $WORK_DIR

# 3. 기존 배포 정리
log_info "기존 배포 정리 중..."
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    docker stop $CONTAINER_NAME || true
    docker rm $CONTAINER_NAME || true
    log_success "기존 컨테이너 정리 완료"
fi

# 4. 소스 코드 가져오기
log_info "최신 소스 코드 가져오기..."
if [ -d "$PROJECT_NAME" ]; then
    cd $PROJECT_NAME
    git fetch origin
    git checkout $BRANCH
    git pull origin $BRANCH
    log_success "소스 코드 업데이트 완료"
else
    git clone -b $BRANCH $GITHUB_REPO $PROJECT_NAME
    cd $PROJECT_NAME
    log_success "소스 코드 다운로드 완료"
fi

# 5. Docker 이미지 빌드
log_info "Docker 이미지 빌드 중... (시간이 소요될 수 있습니다)"
docker build -t $IMAGE_NAME:latest .
log_success "Docker 이미지 빌드 완료"

# 6. 컨테이너 실행
log_info "애플리케이션 컨테이너 실행 중..."
docker run -d \
    --name $CONTAINER_NAME \
    --restart unless-stopped \
    -p $PORT:$PORT \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e JAVA_OPTS="-Xmx2g -Xms1g" \
    -e TZ=Asia/Seoul \
    $IMAGE_NAME:latest

log_success "컨테이너 실행 완료"

# 7. 애플리케이션 시작 대기
log_info "애플리케이션 시작 대기 중..."
sleep 45

# 8. 헬스체크
log_info "애플리케이션 상태 확인 중..."
MAX_RETRIES=12
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -f http://localhost:$PORT/actuator/health > /dev/null 2>&1; then
        log_success "애플리케이션이 성공적으로 시작되었습니다!"
        break
    fi
    
    RETRY_COUNT=$((RETRY_COUNT + 1))
    log_info "헬스체크 대기 중... ($RETRY_COUNT/$MAX_RETRIES)"
    sleep 10
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    log_error "헬스체크에 실패했습니다. 로그를 확인하세요:"
    docker logs --tail 50 $CONTAINER_NAME
    exit 1
fi

# 9. 배포 완료 정보 출력
echo ""
echo "🎉 JobHub Backend 배포가 완료되었습니다!"
echo ""
echo "📊 배포 정보:"
echo "  ├─ 컨테이너: $CONTAINER_NAME"
echo "  ├─ 이미지: $IMAGE_NAME:latest"
echo "  ├─ 포트: $PORT"
echo "  └─ 프로필: production"
echo ""

# Public IP 가져오기
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null)
if [ $? -eq 0 ] && [ -n "$PUBLIC_IP" ]; then
    echo "🌐 접속 URL:"
    echo "  ├─ 애플리케이션: http://$PUBLIC_IP:$PORT"
    echo "  └─ 헬스체크: http://$PUBLIC_IP:$PORT/actuator/health"
else
    echo "🌐 접속 URL: http://[EC2-PUBLIC-IP]:$PORT"
fi

echo ""
echo "🔧 관리 명령어:"
echo "  ├─ 로그 확인: docker logs -f $CONTAINER_NAME"
echo "  ├─ 컨테이너 중지: docker stop $CONTAINER_NAME"
echo "  ├─ 컨테이너 시작: docker start $CONTAINER_NAME"
echo "  ├─ 컨테이너 재시작: docker restart $CONTAINER_NAME"
echo "  └─ 상태 확인: docker ps"
echo ""

# 최근 로그 출력
log_info "최근 애플리케이션 로그:"
docker logs --tail 10 $CONTAINER_NAME

echo ""
log_success "배포 완료! 🚀"
