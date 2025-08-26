#!/bin/bash

# JobHub Backend EC2 환경 사전 준비 스크립트
set -e

echo "🚀 JobHub Backend EC2 환경 준비를 시작합니다..."

# 운영체제 확인
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$NAME
    echo "운영체제: $OS"
else
    echo "❌ 운영체제를 확인할 수 없습니다."
    exit 1
fi

# 1. 시스템 업데이트
echo "📦 시스템 패키지 업데이트..."
if [[ "$OS" == *"Amazon Linux"* ]]; then
    sudo yum update -y
    PACKAGE_MANAGER="yum"
elif [[ "$OS" == *"Ubuntu"* ]]; then
    sudo apt-get update -y
    sudo apt-get upgrade -y
    PACKAGE_MANAGER="apt"
else
    echo "❌ 지원하지 않는 운영체제입니다: $OS"
    exit 1
fi

# 2. 필수 패키지 설치
echo "🔧 필수 패키지 설치..."
if [ "$PACKAGE_MANAGER" = "yum" ]; then
    sudo yum install -y curl wget git htop
elif [ "$PACKAGE_MANAGER" = "apt" ]; then
    sudo apt-get install -y curl wget git htop
fi

# 3. Docker 설치
echo "🐳 Docker 설치..."
if ! command -v docker &> /dev/null; then
    if [ "$PACKAGE_MANAGER" = "yum" ]; then
        # Amazon Linux 2
        sudo yum install -y docker
        sudo systemctl start docker
        sudo systemctl enable docker
    elif [ "$PACKAGE_MANAGER" = "apt" ]; then
        # Ubuntu
        curl -fsSL https://get.docker.com -o get-docker.sh
        sudo sh get-docker.sh
        sudo systemctl start docker
        sudo systemctl enable docker
        rm get-docker.sh
    fi
    
    # 현재 사용자를 docker 그룹에 추가
    sudo usermod -a -G docker $USER
    echo "✅ Docker 설치 완료"
else
    echo "✅ Docker가 이미 설치되어 있습니다."
fi

# 4. Docker Compose 설치
echo "🔧 Docker Compose 설치..."
if ! command -v docker-compose &> /dev/null; then
    # 최신 버전의 Docker Compose 설치
    DOCKER_COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep 'tag_name' | cut -d\" -f4)
    sudo curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo "✅ Docker Compose $DOCKER_COMPOSE_VERSION 설치 완료"
else
    echo "✅ Docker Compose가 이미 설치되어 있습니다."
fi

# 5. 방화벽 설정 (포트 8080 열기)
echo "🔥 방화벽 설정..."
if command -v firewall-cmd &> /dev/null; then
    # CentOS/RHEL/Amazon Linux (firewalld)
    sudo firewall-cmd --permanent --add-port=8080/tcp
    sudo firewall-cmd --reload
    echo "✅ 방화벽에서 포트 8080 열기 완료"
elif command -v ufw &> /dev/null; then
    # Ubuntu (ufw)
    sudo ufw allow 8080/tcp
    echo "✅ 방화벽에서 포트 8080 열기 완료"
else
    echo "⚠️ 방화벽 설정을 수동으로 확인하세요. 포트 8080이 열려있는지 확인하세요."
fi

# 6. 작업 디렉토리 생성
echo "📁 작업 디렉토리 생성..."
WORK_DIR="/home/$USER/jobhub"
mkdir -p $WORK_DIR
cd $WORK_DIR
echo "작업 디렉토리: $WORK_DIR"

# 7. 시스템 리소스 확인
echo "💻 시스템 리소스 확인..."
echo "CPU: $(nproc) 코어"
echo "메모리: $(free -h | awk '/^Mem:/ { print $2 }')"
echo "디스크: $(df -h / | awk '/\// { print $4 " 사용 가능" }')"

# 8. Docker 서비스 상태 확인
echo "🔍 Docker 서비스 상태 확인..."
sudo systemctl status docker --no-pager

# 9. 환경 설정 완료 메시지
echo ""
echo "🎉 EC2 환경 준비가 완료되었습니다!"
echo ""
echo "📋 다음 단계:"
echo "1. 터미널을 재시작하거나 다음 명령어 실행: newgrp docker"
echo "2. Docker 권한 확인: docker run hello-world"
echo "3. 배포 스크립트 실행:"
echo "   curl -O https://raw.githubusercontent.com/yjyj0234/JobHub-backend/son/deploy.sh"
echo "   chmod +x deploy.sh"
echo "   ./deploy.sh"
echo ""
echo "🌐 접속 예정 URL: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo 'EC2-PUBLIC-IP'):8080"
echo ""
echo "⚠️ 주의사항:"
echo "- AWS 보안 그룹에서 포트 8080이 열려있는지 확인하세요"
echo "- EC2 인스턴스에 최소 2GB RAM을 권장합니다"
echo ""
