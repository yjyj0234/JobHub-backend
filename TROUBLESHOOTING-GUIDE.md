# 🆘 EC2 배포 문제 해결 가이드

## ❌ 자주 발생하는 문제들

### 1. SSH 접속 문제

#### 문제: "Permission denied (publickey)"
```bash
# 해결책 1: 키 파일 권한 설정
chmod 400 jobhub-key.pem

# 해결책 2: 올바른 사용자명 사용
ssh -i jobhub-key.pem ec2-user@[IP주소]  # Amazon Linux
ssh -i jobhub-key.pem ubuntu@[IP주소]    # Ubuntu
```

#### 문제: "Connection timed out"
- **원인**: 보안 그룹에서 SSH(포트 22) 허용 안됨
- **해결**: AWS 콘솔 → EC2 → 보안 그룹 → SSH 규칙 추가

### 2. 웹 접속 문제

#### 문제: "http://[IP]:8080 접속 안됨"
```bash
# 해결책 1: 보안 그룹에 포트 8080 추가 확인
# AWS 콘솔 → EC2 → 보안 그룹 → 인바운드 규칙 편집

# 해결책 2: 애플리케이션 상태 확인
docker ps                    # 컨테이너 실행 상태
docker logs jobhub-app      # 애플리케이션 로그
```

#### 문제: "502 Bad Gateway"
```bash
# 해결책: 애플리케이션 재시작
docker restart jobhub-app

# 또는 완전 재배포
./quick-deploy.sh
```

#### 문제: "API 404 Not Found - 프론트엔드 연동"
```javascript
// ❌ 잘못된 프론트엔드 API 설정
const API_BASE_URL = "http://3.35.136.37";        // 포트 누락
const loginUrl = "/auth/login";                    // api 접두사 누락

// ✅ 올바른 프론트엔드 API 설정
const API_BASE_URL = "http://3.35.136.37:8080";   // 포트 8080 추가
const loginUrl = "/api/auth/login";                // api 접두사 추가

// 최종 API 엔드포인트들:
// - 로그인: http://3.35.136.37:8080/api/auth/login
// - 회원가입: http://3.35.136.37:8080/api/auth/register  
// - 이력서: http://3.35.136.37:8080/api/resumes
// - 모든 API: http://3.35.136.37:8080/api/*
```

### 3. 메모리 부족 문제

#### 문제: "OutOfMemoryError"
```bash
# 해결책 1: JVM 메모리 설정 조정
docker run -e JAVA_OPTS="-Xmx1g -Xms512m" ...

# 해결책 2: 더 큰 인스턴스 타입 사용
# t3.micro → t3.small → t3.medium
```

### 4. Docker 권한 문제

#### 문제: "permission denied while trying to connect to Docker"
```bash
# 해결책: Docker 그룹 추가 후 재로그인
sudo usermod -a -G docker $USER
newgrp docker

# 또는 터미널 재시작
exit
ssh -i jobhub-key.pem ec2-user@[IP]
```

## 🔧 유용한 디버깅 명령어

### 시스템 상태 확인
```bash
# 시스템 리소스 확인
free -h          # 메모리 사용량
df -h            # 디스크 사용량
top              # CPU 사용량

# Docker 상태 확인
docker ps -a     # 모든 컨테이너
docker images    # 이미지 목록
docker stats     # 리소스 사용량 실시간
```

### 로그 확인
```bash
# 애플리케이션 로그
docker logs jobhub-app
docker logs -f jobhub-app    # 실시간 로그

# 시스템 로그
sudo journalctl -u docker    # Docker 서비스 로그
dmesg | tail                 # 시스템 메시지
```

### 네트워크 확인
```bash
# 포트 확인
sudo netstat -tlnp | grep 8080
curl localhost:8080/actuator/health

# 방화벽 확인
sudo firewall-cmd --list-all     # CentOS/RHEL
sudo ufw status                  # Ubuntu
```

## 🔄 완전 초기화 및 재배포

### 문제가 계속 발생할 때
```bash
# 1. 모든 Docker 컨테이너/이미지 삭제
docker stop $(docker ps -aq)
docker rm $(docker ps -aq)
docker rmi $(docker images -q)

# 2. 작업 디렉토리 삭제
rm -rf ~/jobhub

# 3. 원클릭 배포 재실행
curl -s https://raw.githubusercontent.com/yjyj0234/JobHub-backend/son/quick-deploy.sh | bash
```

## 📞 긴급 복구 명령어

### 애플리케이션이 완전히 멈췄을 때
```bash
# 빠른 복구 (1분 내 복구)
docker restart jobhub-app

# 중간 복구 (5분 내 복구)
cd ~/jobhub/jobhub-backend
git pull origin son
docker build -t jobhub-backend .
docker stop jobhub-app && docker rm jobhub-app
docker run -d --name jobhub-app --restart unless-stopped -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod -e JAVA_OPTS="-Xmx2g -Xms1g" jobhub-backend

# 완전 복구 (10분 내 복구)
curl -s https://raw.githubusercontent.com/yjyj0234/JobHub-backend/son/quick-deploy.sh | bash
```

## 📋 체크리스트

### ✅ 배포 전 확인사항
- [ ] AWS 보안 그룹에 포트 22, 8080 열림
- [ ] EC2 인스턴스 t3.small 이상 (2GB RAM)
- [ ] 키 페어 파일(.pem) 다운로드 완료
- [ ] SSH 접속 테스트 완료

### ✅ 배포 후 확인사항
- [ ] `docker ps` 명령어로 컨테이너 실행 중 확인
- [ ] `curl localhost:8080/actuator/health` 응답 확인
- [ ] 브라우저에서 `http://[IP]:8080/actuator/health` 접속 확인
- [ ] 애플리케이션 로그 정상 확인

## 🆘 마지막 수단

### 모든 것이 실패했을 때
1. **EC2 인스턴스 재시작**: AWS 콘솔에서 인스턴스 재부팅
2. **새 인스턴스 생성**: 처음부터 다시 생성
3. **지원 요청**: AWS Support 또는 개발팀 문의
