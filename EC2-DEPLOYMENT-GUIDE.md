# JobHub Backend EC2 배포 가이드

## 🔐 AWS 보안 그룹 설정

EC2 인스턴스에 접속하기 위해 다음 포트들을 열어야 합니다:

### 인바운드 규칙

| 타입 | 프로토콜 | 포트 범위 | 소스 | 설명 |
|------|----------|-----------|------|------|
| SSH | TCP | 22 | 0.0.0.0/0 또는 내 IP | EC2 SSH 접속 |
| 사용자 지정 TCP | TCP | 8080 | 0.0.0.0/0 | JobHub Backend API |
| HTTP | TCP | 80 | 0.0.0.0/0 | HTTP 접속 (선택사항) |
| HTTPS | TCP | 443 | 0.0.0.0/0 | HTTPS 접속 (선택사항) |

### AWS 콘솔에서 설정하는 방법

1. **EC2 콘솔** → **인스턴스** → 해당 인스턴스 선택
2. **보안** 탭 → **보안 그룹** 클릭
3. **인바운드 규칙** → **인바운드 규칙 편집**
4. **규칙 추가**:
   - 유형: 사용자 지정 TCP
   - 포트: 8080
   - 소스: 0.0.0.0/0 (어디서나) 또는 특정 IP 대역
5. **규칙 저장**

## 🚀 배포 실행 단계

### 1단계: EC2 인스턴스 준비

```bash
# EC2 인스턴스에 SSH 접속
ssh -i your-key.pem ec2-user@your-ec2-public-ip

# 사전 준비 스크립트 실행
curl -O https://raw.githubusercontent.com/yjyj0234/JobHub-backend/son/setup-ec2.sh
chmod +x setup-ec2.sh
./setup-ec2.sh
```

### 2단계: 원클릭 배포

```bash
# 방법 1: 원클릭 배포 스크립트 (권장)
curl -O https://raw.githubusercontent.com/yjyj0234/JobHub-backend/son/quick-deploy.sh
chmod +x quick-deploy.sh
./quick-deploy.sh

# 방법 2: 수동 배포
curl -O https://raw.githubusercontent.com/yjyj0234/JobHub-backend/son/deploy.sh
chmod +x deploy.sh
./deploy.sh

# 방법 3: Docker Compose 사용
git clone -b son https://github.com/yjyj0234/JobHub-backend.git
cd JobHub-backend
docker-compose up -d
```

## 💻 권장 EC2 인스턴스 스펙

| 구분 | 최소 | 권장 |
|------|------|------|
| 인스턴스 타입 | t3.micro | t3.small 이상 |
| vCPU | 1 | 2 |
| 메모리 | 1GB | 2GB 이상 |
| 스토리지 | 8GB | 20GB 이상 |

## 🔍 배포 후 확인 사항

### 1. 애플리케이션 상태 확인
```bash
# 컨테이너 상태
docker ps

# 애플리케이션 로그
docker logs -f jobhub-app

# 헬스체크
curl http://localhost:8080/actuator/health
```

### 2. 외부 접속 확인
```bash
# Public IP 확인
curl http://169.254.169.254/latest/meta-data/public-ipv4

# 브라우저에서 접속
# http://[EC2-PUBLIC-IP]:8080/actuator/health
```

## 🛠️ 문제 해결

### Docker 권한 오류
```bash
# Docker 그룹에 사용자 추가 후 재로그인
sudo usermod -a -G docker $USER
newgrp docker
```

### 포트 8080 접속 불가
1. AWS 보안 그룹에서 8080 포트 확인
2. EC2 인스턴스 방화벽 확인
3. 애플리케이션 로그 확인

### 메모리 부족 오류
```bash
# 현재 메모리 사용량 확인
free -h

# JVM 메모리 설정 조정
docker run ... -e JAVA_OPTS="-Xmx1g -Xms512m" ...
```

## 📱 모니터링 및 관리

### 로그 관리
```bash
# 실시간 로그 확인
docker logs -f jobhub-app

# 최근 100줄 로그
docker logs --tail 100 jobhub-app

# 로그 파일 크기 제한 (docker-compose.yml에서 설정)
logging:
  driver: json-file
  options:
    max-size: "10m"
    max-file: "3"
```

### 백업 및 업데이트
```bash
# 이미지 백업
docker tag jobhub-backend:latest jobhub-backend:backup-$(date +%Y%m%d)

# 애플리케이션 업데이트
git pull origin son
docker build -t jobhub-backend:latest .
docker restart jobhub-app
```

## 🌐 도메인 연결 (선택사항)

### Nginx 리버스 프록시 설정
```bash
# docker-compose.yml의 nginx 섹션 활성화
# 또는 별도 Nginx 설치하여 설정
```

### SSL 인증서 설정
```bash
# Let's Encrypt 무료 SSL 인증서
sudo yum install -y certbot
sudo certbot certonly --standalone -d your-domain.com
```
