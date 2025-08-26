#!/bin/bash

# JobHub Backend EC2 배포 스크립트
set -e

echo "🚀 JobHub Backend 배포 시작..."

# 환경 변수 설정
PROJECT_NAME="jobhub-backend"
CONTAINER_NAME="jobhub-app"
IMAGE_NAME="jobhub-backend"
PORT="8080"
GITHUB_REPO="https://github.com/yjyj0234/JobHub-backend.git"
BRANCH="son"

# 1. 기존 컨테이너 정리
echo "📦 기존 컨테이너 정리..."
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    docker stop $CONTAINER_NAME || true
    docker rm $CONTAINER_NAME || true
fi

# 2. 기존 이미지 삭제 (옵션)
echo "🗑️ 기존 이미지 정리..."
docker rmi $IMAGE_NAME:latest || true

# 3. 최신 소스 코드 가져오기
echo "📥 소스 코드 업데이트..."
if [ -d "$PROJECT_NAME" ]; then
    cd $PROJECT_NAME
    git fetch origin
    git checkout $BRANCH
    git pull origin $BRANCH
else
    git clone -b $BRANCH $GITHUB_REPO $PROJECT_NAME
    cd $PROJECT_NAME
fi

# 4. Docker 이미지 빌드
echo "🔨 Docker 이미지 빌드..."
docker build -t $IMAGE_NAME:latest .

# 5. 컨테이너 실행
echo "🏃 컨테이너 실행..."
docker run -d \
    --name $CONTAINER_NAME \
    --restart unless-stopped \
    -p $PORT:$PORT \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e JAVA_OPTS="-Xmx2g -Xms1g" \
    -e TZ=Asia/Seoul \
    $IMAGE_NAME:latest

# 6. 헬스체크 대기
echo "⏰ 애플리케이션 시작 대기..."
sleep 30

# 7. 헬스체크
echo "🔍 헬스체크 수행..."
for i in {1..10}; do
    if curl -f http://localhost:$PORT/actuator/health > /dev/null 2>&1; then
        echo "✅ 애플리케이션이 성공적으로 시작되었습니다!"
        echo "🌐 접속 URL: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):$PORT"
        docker logs --tail 20 $CONTAINER_NAME
        exit 0
    fi
    echo "대기 중... ($i/10)"
    sleep 10
done

echo "❌ 헬스체크 실패. 로그를 확인하세요:"
docker logs --tail 50 $CONTAINER_NAME
exit 1
