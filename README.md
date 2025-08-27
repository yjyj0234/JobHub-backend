# JobHub Backend

구직자의 **이력서 작성 · 관리 · 공개/공유**를 지원하는 Spring Boot 백엔드입니다.  
학력/경력/프로젝트/스킬/자격증/외국어/포트폴리오/대외활동을 **섹션 단위**로 구조화하고, 사용자 **프로필** · **대표 이력서** · **공개 여부** · **완성도(%)** 관리 등 핵심 기능을 제공합니다.

---

## ✨ Features

- **인증/인가**: 이메일 로그인, JWT(Access/Refresh), CORS/쿠키
- **프로필**: 이름·연락처·한 줄 소개·지역·생년/생일·요약 CRUD
- **이력서**: 생성/수정/삭제, 대표 설정, 공개 여부, 완성도(%) 관리
- **섹션 CRUD**
  - 학력, 경력, 프로젝트, 기술(스킬 링크/신규 생성), 자격증, 외국어, 포트폴리오, 대외활동
- **검색/참조**: 지역(시/도→시군구) 트리 및 빠른 검색 API
- **파일 업로드**: 로컬 또는 S3(선택)

---

## 🧱 Tech Stack

| Area       | Stack                                                                 |
|-----------|------------------------------------------------------------------------|
| Language  | **Java 17**                                                            |
| Framework | **Spring Boot 3**, Spring MVC, Spring Security, Spring Data JPA        |
| Auth      | **JWT** (Access/Refresh), Cookie, CORS                                 |
| Database  | **MySQL 8** (InnoDB, UTF8MB4)                                          |
| Build     | **Gradle**                                                             |
| DevOps    | (옵션) **Docker**                                                      |
| Others    | Bean Validation, Pageable, (옵션) Swagger/OpenAPI, MapStruct/Redis      |

---

## 🧭 Architecture

```mermaid
flowchart TD
    client["Web Client (React)"]
    api["JobHub API (Spring Boot)"]
    sec["Spring Security / JWT"]
    svc["Domain Services"]
    db[("MySQL 8")]
    s3["(Optional) Object Storage (S3)"]

    client -->|JWT| api
    api --> sec
    api --> svc
    svc --> db
    api --> s3
```

### 📂 Project Structure

```
jobhub-backend/
 ├─ src/main/java/com/example/jobhub
 │   ├─ auth/             # 로그인/토큰/필터
 │   ├─ config/           # Security/JWT/CORS/Swagger 등
 │   ├─ user/             # User, UserProfile
 │   ├─ resume/           # Resume + 섹션(education/experience/project/…)
 │   ├─ skill/            # Skill, ResumeSkill
 │   ├─ region/           # Region(시/도/시군구) API
 │   ├─ file/             # 파일 업로드
 │   ├─ common/           # 공통 DTO/예외/유틸
 │   └─ JobHubApplication.java
 ├─ src/main/resources/
 │   ├─ application.yml
 │   └─ schema.sql / data.sql (옵션)
 └─ build.gradle
```

### 🔌 API Overview (요약)

**Auth**
- `POST   /api/auth/login`                       # Access/Refresh 발급

**Profile**
- `GET    /api/profile/{userId}`
- `PUT    /api/profile/{userId}`

**Resume**
- `GET    /api/resumes`                          # 내 이력서 목록
- `POST   /api/resumes`                          # 생성
- `GET    /api/resumes/{id}`                     # 상세
- `PUT    /api/resumes/{id}`                     # 수정
- `DELETE /api/resumes/{id}`                     # 삭제
- `POST   /api/resumes/{id}/copy`                # 복사

**Sections (예: 경력)**
- `GET    /api/resumes/{resumeId}/experiences`
- `POST   /api/resumes/{resumeId}/experiences`
- `PUT    /api/resumes/experiences/{experienceId}`
- `DELETE /api/resumes/experiences/{experienceId}`

**Skills**
- `GET    /api/resumes/{resumeId}/skills`
- `POST   /api/resumes/{resumeId}/skills?skillId={id}`  # 기존 스킬 연결
- `POST   /api/skills`                                   # 신규 스킬 생성
- `DELETE /api/resumes/{resumeId}/skills/{resumeSkillId}`

**Regions**
- `GET    /api/search/regions`            # parentId로 하위 조회
- `GET    /api/search/regions/tree`       # 전체 트리

---

## ⚙️ Environment (.env 예시)

```
# Server
SERVER_PORT=8080

# DB
DB_HOST=localhost
DB_PORT=3306
DB_NAME=jobhub
DB_USERNAME=jobhub
DB_PASSWORD=jobhub

# JWT
JWT_SECRET=change-me-very-secret
JWT_ACCESS_TOKEN_TTL=3600
JWT_REFRESH_TOKEN_TTL=1209600

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000

# S3 (optional)
S3_BUCKET=
S3_ACCESS_KEY=
S3_SECRET_KEY=
S3_REGION=
```

---

## 🚀 Getting Started

1) MySQL 준비

```sql
CREATE DATABASE jobhub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'jobhub'@'%' IDENTIFIED BY 'jobhub';
GRANT ALL PRIVILEGES ON jobhub.* TO 'jobhub'@'%';
FLUSH PRIVILEGES;
```

2) 애플리케이션 실행

```bash
# 개발 모드
./gradlew clean bootRun

# 또는 빌드 후 실행
./gradlew clean build
java -jar build/libs/jobhub-*.jar
```

3) (선택) 더미 계정

- t1@test.com ~ t20@test.com, user_type=USER, is_active=1  
필요 시 user_profiles 도 일괄 INSERT로 채울 수 있습니다.

---

## ✅ Quality (권장)

- 테스트: JUnit5 + Spring Test
- 정적 분석: Spotless/Checkstyle (옵션)
- 문서화: Swagger/OpenAPI (옵션)

---

## 📄 License

이 저장소의 라이선스 정책이 정해지지 않았다면, 사용 전 팀/소유자와 협의하세요.

---

## 🙌 Contributing

이슈/PR 환영합니다.  
코딩 컨벤션과 커밋 메시지 규칙을 지켜 주세요.
