# ==== BUILD ====
FROM gradle:8-jdk17 AS build
WORKDIR /src

# Gradle wrapper와 설정 파일들 먼저 복사 (캐싱 최적화)
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle ./

# 의존성 다운로드 (캐싱 레이어)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ==== RUN ====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 필요한 패키지 설치 (curl for healthcheck)
RUN apk add --no-cache curl

# 비루트 사용자 생성 (Alpine 기반)
RUN addgroup -S app && adduser -S app -G app
USER app

# 빌드 산출물 복사 (Gradle의 경우 build/libs/에 생성됨)
COPY --from=build --chown=app:app /src/build/libs/*.jar app.jar

# 헬스체크 (기본 루트 경로 또는 특정 API 사용)
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/ || curl -f http://localhost:8080/api/health || exit 1

# 환경 변수
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE="prod"

EXPOSE 8080

# JVM 최적화 옵션 추가
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
