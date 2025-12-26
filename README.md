# Blog API

Spring Boot 기반 블로그 백엔드 REST API 서버입니다.
JWT + 소셜 로그인(Firebase, Kakao) 인증, Redis 캐싱, Docker 배포, Swagger 문서화를 지원합니다.

---

## 목차

- [기술 스택](#기술-스택)
- [배포 주소](#배포-주소)
- [시작하기](#시작하기)
- [환경변수 설명](#환경변수-설명)
- [인증 플로우](#인증-플로우)
- [역할/권한 체계](#역할권한-체계)
- [API 엔드포인트](#api-엔드포인트)
- [테스트 계정](#테스트-계정)
- [데이터베이스](#데이터베이스)
- [성능 및 보안](#성능-및-보안)
- [한계와 개선 계획](#한계와-개선-계획)
- [프로젝트 구조](#프로젝트-구조)

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.4.1 |
| **ORM** | Spring Data JPA (Hibernate) |
| **Database** | MySQL 8.0 |
| **Cache** | Redis 7 |
| **Authentication** | JWT (HS256), Firebase Auth, Kakao OAuth2 |
| **Security** | Spring Security 6, BCrypt (strength 10) |
| **API Docs** | Swagger/OpenAPI 3.0 |
| **Migration** | Flyway 10.20.1 |
| **Container** | Docker, Docker Compose |
| **Build** | Gradle 8.5 |

---

## 배포 주소

### 프로덕션 환경

| 항목 | URL | 설명 |
|------|-----|------|
| **Base URL** | `http://113.198.66.68` | API 기본 주소 |
| **Swagger UI** | `http://113.198.66.68/swagger-ui.html` | API 문서 및 테스트 |
| **Health Check** | `http://113.198.66.68/health` | 서버 상태 확인 |

### 로컬 개발 환경

| 항목 | URL |
|------|-----|
| **Base URL** | `http://localhost:8080` |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` |
| **Health Check** | `http://localhost:8080/health` |

---

## 시작하기

### 1. 필수 요구사항

- Java 21+
- Docker & Docker Compose
- Gradle 8.5+

### 2. 환경 변수 설정

`.env.example` 파일을 참고하여 `.env` 파일을 생성합니다:

```bash
cp .env.example .env
```

주요 환경변수를 설정합니다 (자세한 설명은 [환경변수 설명](#환경변수-설명) 참조):

```properties
# 서버 배포 주소
PUBLISHED_URL=113.198.66.68
APP_PORT=80

# 데이터베이스
MYSQL_ROOT_PASSWORD=your-secure-password
MYSQL_DATABASE=blog
MYSQL_USER=app
MYSQL_PASSWORD=your-secure-password

# JWT 설정
JWT_SECRET=your-super-secret-jwt-key-at-least-32-bytes-long

# Firebase
FIREBASE_PROJECT_ID=your-firebase-project-id

# Kakao
KAKAO_REST_API_KEY=your-kakao-rest-api-key
```

### 3. Docker로 실행 (권장)

#### 로컬 개발 (이미지 빌드)

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
```

#### 프로덕션 배포 (GHCR 이미지 사용)

**서버 배포 방법은 [DEPLOYMENT.md](./DEPLOYMENT.md) 참조**

간단한 배포 명령어:

```bash
# 서버에서 실행 (Docker 및 .env 설정 완료 후)
docker compose pull
docker compose up -d

# Health 확인
curl http://113.198.66.68/health
```

**배포 전 필수 작업:**
- ✅ `.env` 파일 작성 (환경변수 설정)
- ✅ `secrets/firebase-service-account.json` 파일 업로드
- ✅ Firebase Authorized domains에 서버 IP 추가
- ✅ GitHub Actions 빌드 완료 확인

자세한 내용은 [배포 가이드](./DEPLOYMENT.md)를 참조하세요.

### 4. Gradle로 실행

```bash
# 빌드
./gradlew clean build -x test

# 실행
./gradlew bootRun

# 또는 JAR 실행
java -jar build/libs/blog-api-0.0.1-SNAPSHOT.jar
```

---

## 환경변수 설명

`.env.example` 파일의 모든 환경변수 설명입니다.

### Docker 이미지 설정

| 변수명 | 설명 | 기본값 | 필수 |
|--------|------|--------|------|
| `DOCKER_IMAGE` | GHCR 이미지 경로 | `ghcr.io/YOUR_USERNAME/blog-api:latest` | 프로덕션 시 |

### 애플리케이션 설정

| 변수명 | 설명 | 기본값 | 필수 |
|--------|------|--------|------|
| `APP_PORT` | 서버 포트 | `80` | O |
| `PUBLISHED_URL` | 배포 서버 주소 (IP 또는 도메인) | `113.198.66.68` | O |
| `SPRING_PROFILES_ACTIVE` | Spring 프로파일 (`local`, `prod`) | `prod` | O |
| `FRONTEND_URL` | 프론트엔드 URL (OAuth 리다이렉트) | `http://113.198.66.68` | O |
| `BACKEND_URL` | 백엔드 API URL | `http://113.198.66.68` | O |
| `CORS_ALLOWED_ORIGINS` | CORS 허용 도메인 (쉼표 구분) | `http://113.198.66.68,...` | O |

### 데이터베이스 설정 (MySQL)

| 변수명 | 설명 | 기본값 | 필수 |
|--------|------|--------|------|
| `MYSQL_ROOT_PASSWORD` | MySQL root 비밀번호 | - | O |
| `MYSQL_DATABASE` | 데이터베이스 이름 | `blog` | O |
| `MYSQL_USER` | 애플리케이션 DB 사용자 | `app` | O |
| `MYSQL_PASSWORD` | 애플리케이션 DB 비밀번호 | - | O |
| `MYSQL_PORT` | MySQL 포트 | `3306` | X |

**참고:** Docker 환경에서 `MYSQL_HOST`는 자동으로 `mysql` (서비스 이름)로 설정됩니다.

### JWT 설정

| 변수명 | 설명 | 기본값 | 필수 |
|--------|------|--------|------|
| `JWT_SECRET` | JWT 서명 키 (최소 32바이트) | - | O |
| `JWT_ACCESS_EXP` | Access Token 만료 시간 (초) | `900` (15분) | X |
| `JWT_REFRESH_EXP` | Refresh Token 만료 시간 (초) | `1209600` (14일) | X |

### Redis 설정

| 변수명 | 설명 | 기본값 | 필수 |
|--------|------|--------|------|
| `REDIS_HOST` | Redis 호스트 | `localhost` | X |
| `REDIS_PORT` | Redis 포트 | `6379` | X |

**참고:** Docker 환경에서 `REDIS_HOST`는 자동으로 `redis` (서비스 이름)로 설정됩니다.

### Firebase 설정

| 변수명 | 설명 | 기본값 | 필수 |
|--------|------|--------|------|
| `FIREBASE_PROJECT_ID` | Firebase 프로젝트 ID | - | O |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | 서비스 계정 키 파일 경로 | `/app/firebase-service-account.json` (Docker) | X |

**서비스 계정 키 파일 배치:**
- **Docker 프로덕션**: `~/blog-api/secrets/firebase-service-account.json`에 파일 배치 (docker-compose.yml이 자동 마운트)
- **로컬 개발**: `src/main/resources/firebase-service-account.json` 경로에 파일 배치
- 환경변수 `FIREBASE_SERVICE_ACCOUNT_PATH`는 주석 처리 유지 (기본값 사용)

**승인된 도메인 설정 (중요!):**

Firebase Auth (Google 로그인 등)를 사용하려면 Firebase Console에서 도메인을 승인해야 합니다:

1. [Firebase Console](https://console.firebase.google.com/) → 프로젝트 선택
2. **Authentication** → **Settings** → **Authorized domains**
3. **Add domain** 클릭
4. 다음 도메인 추가:
   - `localhost` (기본 포함)
   - `113.198.66.68` (프로덕션 서버)
   - `yourdomain.com` (커스텀 도메인 사용 시)

**미설정 시 오류:**
```
Firebase: Error (auth/unauthorized-domain)
```

### Kakao OAuth 설정

| 변수명 | 설명 | 기본값 | 필수 |
|--------|------|--------|------|
| `KAKAO_REST_API_KEY` | Kakao REST API 키 | - | O |

---

## 인증 플로우

본 API는 **3가지 인증 방식**을 지원합니다.

### 1. JWT 인증 (로컬 회원가입/로그인)

```
[클라이언트]                          [서버]
     |                                  |
     |  POST /auth/signup               |
     |  (email, password, nickname)     |
     |--------------------------------->|
     |                                  | - BCrypt로 비밀번호 해싱
     |                                  | - User 생성 (ROLE_USER)
     |  Access Token + Refresh Token    |
     |<---------------------------------|
     |                                  |
     |  POST /auth/login                |
     |  (email, password)               |
     |--------------------------------->|
     |                                  | - 비밀번호 검증
     |                                  | - JWT 생성 (HS256)
     |  Access Token + Refresh Token    |
     |<---------------------------------|
     |                                  |
     |  API 요청                        |
     |  Header: Authorization: Bearer {token}
     |--------------------------------->|
     |                                  | - JwtAuthFilter 동작
     |                                  | - JWT 파싱 및 검증
     |                                  | - SecurityContext 설정
     |  응답 데이터                      |
     |<---------------------------------|
     |                                  |
     |  POST /auth/refresh              |
     |  (refreshToken)                  |
     |--------------------------------->|
     |                                  | - Redis에서 토큰 검증
     |  새 Access Token                 |
     |<---------------------------------|
```

**토큰 저장소:**
- Access Token: 클라이언트 메모리/LocalStorage
- Refresh Token: Redis (서버) + 클라이언트

**만료 시간:**
- Access Token: 15분 (기본값)
- Refresh Token: 14일 (기본값)

### 2. Firebase 인증

```
[클라이언트]                          [서버]
     |                                  |
     |  Firebase SDK로 로그인           |
     |  (Google, Email 등)              |
     |--------------------------------->|
     |  Firebase ID Token               |
     |<---------------------------------|
     |                                  |
     |  API 요청                        |
     |  Header: Authorization: Bearer {firebaseIdToken}
     |--------------------------------->|
     |                                  | - FirebaseAuthFilter 동작
     |                                  | - Firebase Admin SDK로 토큰 검증
     |                                  | - User 조회 또는 자동 생성
     |                                  | - SecurityContext 설정
     |  응답 데이터                      |
     |<---------------------------------|
```

**특징:**
- Firebase ID Token을 직접 검증
- 첫 로그인 시 User 자동 생성 (provider: FIREBASE)
- 역할: ROLE_USER (기본값)

### 3. Kakao OAuth 인증

```
[클라이언트]                          [서버]
     |                                  |
     |  Kakao SDK로 로그인              |
     |--------------------------------->|
     |  Kakao Access Token              |
     |<---------------------------------|
     |                                  |
     |  POST /auth/kakao-login          |
     |  { kakaoAccessToken }            |
     |--------------------------------->|
     |                                  | - Kakao API로 사용자 정보 조회
     |                                  | - User 조회 또는 자동 생성
     |                                  | - Firebase Custom Token 생성
     |  Firebase Custom Token           |
     |<---------------------------------|
     |                                  |
     |  Firebase SDK로 Custom Token 교환|
     |--------------------------------->|
     |  Firebase ID Token               |
     |<---------------------------------|
     |                                  |
     |  이후 Firebase 인증 플로우 사용    |
```

**특징:**
- Kakao Access Token → Firebase Custom Token 변환
- 첫 로그인 시 User 자동 생성 (provider: KAKAO)
- 이후 Firebase 인증 플로우 사용

### 인증 필터 체인 순서

```
RequestLoggingFilter
    ↓
FirebaseAuthFilter (Firebase ID Token 검증)
    ↓
JwtAuthFilter (JWT Access Token 검증)
    ↓
UsernamePasswordAuthenticationFilter
    ↓
Authorization (@PreAuthorize 검증)
```

---

## 역할/권한 체계

### 역할 종류

| 역할 | 설명 | 부여 방법 |
|------|------|-----------|
| `ROLE_USER` | 일반 사용자 | 회원가입 시 자동 부여 |
| `ROLE_ADMIN` | 관리자 | DB 직접 수정 또는 관리자가 승격 |

### 엔드포인트별 권한 매트릭스

| 엔드포인트 | Public | ROLE_USER | ROLE_ADMIN |
|-----------|--------|-----------|------------|
| **인증** |
| `POST /auth/signup` | ✅ | - | - |
| `POST /auth/login` | ✅ | - | - |
| `POST /auth/refresh` | ✅ | - | - |
| `POST /auth/logout` | - | ✅ | ✅ |
| `POST /auth/kakao-login` | ✅ | - | - |
| **게시글** |
| `GET /posts` | ✅ | ✅ | ✅ |
| `GET /posts/{id}` | ✅ | ✅ | ✅ |
| `GET /posts/search` | ✅ | ✅ | ✅ |
| `POST /posts` | - | ✅ | ✅ |
| `PUT /posts/{id}` | - | ✅ (본인) | ✅ |
| `DELETE /posts/{id}` | - | ✅ (본인) | ✅ |
| `DELETE /posts/{id}/force` | - | - | ✅ |
| `GET /posts/my` | - | ✅ | ✅ |
| **댓글** |
| `GET /posts/{postId}/comments` | ✅ | ✅ | ✅ |
| `POST /posts/{postId}/comments` | - | ✅ | ✅ |
| `PUT /comments/{id}` | - | ✅ (본인) | ✅ |
| `DELETE /comments/{id}` | - | ✅ (본인) | ✅ |
| `DELETE /comments/{id}/force` | - | - | ✅ |
| `GET /comments/my` | - | ✅ | ✅ |
| **좋아요** |
| `POST /posts/{postId}/like` | - | ✅ | ✅ |
| `GET /posts/{postId}/like/status` | - | ✅ | ✅ |
| `GET /posts/{postId}/like/count` | ✅ | ✅ | ✅ |
| `GET /posts/{postId}/likes` | ✅ | ✅ | ✅ |
| `GET /likes/my` | - | ✅ | ✅ |
| **카테고리** |
| `GET /categories` | ✅ | ✅ | ✅ |
| `GET /categories/{id}` | ✅ | ✅ | ✅ |
| `POST /categories` | - | - | ✅ |
| `PUT /categories/{id}` | - | - | ✅ |
| `DELETE /categories/{id}` | - | - | ✅ |
| **관리자** |
| `GET /admin/statistics` | - | - | ✅ |
| `GET /admin/users` | - | - | ✅ |
| `PUT /admin/users/{id}` | - | - | ✅ |
| `POST /admin/users/{id}/suspend` | - | - | ✅ |
| `POST /admin/users/{id}/activate` | - | - | ✅ |
| `DELETE /admin/users/{id}` | - | - | ✅ |
| **헬스체크** |
| `GET /health` | ✅ | ✅ | ✅ |

**범례:**
- ✅: 접근 가능
- (본인): 본인이 작성한 콘텐츠만 수정/삭제 가능
- `-`: 접근 불가

---

## API 엔드포인트

### 인증 (Auth)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/auth/signup` | 회원가입 (이메일/비밀번호) | Public |
| POST | `/auth/login` | 로그인 (JWT 발급) | Public |
| POST | `/auth/refresh` | Access Token 갱신 | Public |
| POST | `/auth/logout` | 로그아웃 (Refresh Token 무효화) | User |
| POST | `/auth/kakao-login` | Kakao OAuth 로그인 | Public |

### 게시글 (Posts)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/posts` | 게시글 목록 조회 (페이징) | Public |
| GET | `/posts/{id}` | 게시글 상세 조회 (조회수 증가) | Public |
| GET | `/posts/search` | 게시글 검색 (키워드) | Public |
| GET | `/posts/category/{categoryId}` | 카테고리별 게시글 조회 | Public |
| GET | `/posts/my` | 내가 작성한 게시글 목록 | User |
| POST | `/posts` | 게시글 작성 | User |
| PUT | `/posts/{id}` | 게시글 수정 | User (본인) |
| DELETE | `/posts/{id}` | 게시글 삭제 (소프트 삭제) | User (본인) |
| DELETE | `/posts/{id}/force` | 게시글 강제 삭제 | Admin |

### 댓글 (Comments)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/posts/{postId}/comments` | 댓글 목록 조회 (페이징) | Public |
| GET | `/posts/{postId}/comments/count` | 댓글 수 조회 | Public |
| GET | `/comments/{id}` | 댓글 상세 조회 | Public |
| GET | `/comments/my` | 내가 작성한 댓글 목록 | User |
| POST | `/posts/{postId}/comments` | 댓글 작성 | User |
| PUT | `/comments/{id}` | 댓글 수정 | User (본인) |
| DELETE | `/comments/{id}` | 댓글 삭제 (소프트 삭제) | User (본인) |
| DELETE | `/comments/{id}/force` | 댓글 강제 삭제 | Admin |

### 좋아요 (Likes)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/posts/{postId}/like` | 좋아요 토글 (추가/취소) | User |
| GET | `/posts/{postId}/like/status` | 좋아요 여부 확인 | User |
| GET | `/posts/{postId}/like/count` | 좋아요 수 조회 | Public |
| GET | `/posts/{postId}/likes` | 좋아요한 사용자 목록 | Public |
| GET | `/likes/my` | 내가 좋아요한 게시글 목록 | User |

### 카테고리 (Categories)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/categories` | 카테고리 목록 조회 | Public |
| GET | `/categories/{id}` | 카테고리 상세 조회 | Public |
| GET | `/categories/slug/{slug}` | Slug로 카테고리 조회 | Public |
| POST | `/categories` | 카테고리 생성 | Admin |
| PUT | `/categories/{id}` | 카테고리 수정 | Admin |
| DELETE | `/categories/{id}` | 카테고리 삭제 | Admin |

### 관리자 (Admin)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/admin/statistics` | 전체 통계 조회 | Admin |
| GET | `/admin/users` | 사용자 목록 조회 (페이징) | Admin |
| GET | `/admin/users/status/{status}` | 상태별 사용자 조회 | Admin |
| GET | `/admin/users/{userId}` | 사용자 상세 조회 | Admin |
| PUT | `/admin/users/{userId}` | 사용자 정보 수정 | Admin |
| POST | `/admin/users/{userId}/suspend` | 사용자 정지 | Admin |
| POST | `/admin/users/{userId}/activate` | 사용자 활성화 | Admin |
| DELETE | `/admin/users/{userId}` | 사용자 영구 삭제 | Admin |

### 헬스체크 (Health)

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/health` | 서버 상태 확인 (버전, 빌드 시간) | Public |

### API 문서

| URL | 설명 |
|-----|------|
| `/swagger-ui.html` | Swagger UI (대화형 API 문서) |
| `/v3/api-docs` | OpenAPI 3.0 JSON 스펙 |

---

## 테스트 계정

### 관리자 계정

| 항목 | 값 |
|------|-----|
| **이메일** | `admin@blog.com` |
| **비밀번호** | `admin1234` |
| **역할** | `ROLE_ADMIN` |
| **권한** | 모든 API 접근 가능, 사용자 관리, 카테고리 관리, 강제 삭제 |

**주의사항:**
- 관리자 계정으로 사용자를 영구 삭제(`DELETE /admin/users/{id}`)하면 복구 불가능합니다.
- 강제 삭제(`/force` 엔드포인트)는 데이터를 완전히 제거하므로 주의하세요.

### 일반 사용자 계정

| 항목 | 값 |
|------|-----|
| **이메일** | `user1@blog.com` ~ `user40@blog.com` |
| **비밀번호** | `user1234` |
| **역할** | `ROLE_USER` |
| **권한** | 게시글/댓글 CRUD (본인만), 좋아요, 조회 |

**사용 예시:**
```bash
# 로그인 요청
curl -X POST http://113.198.66.68/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user1@blog.com",
    "password": "user1234"
  }'

# 응답
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}

# 게시글 작성 (Access Token 사용)
curl -X POST http://113.198.66.68/posts \
  -H "Authorization: Bearer {accessToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "테스트 게시글",
    "content": "내용입니다.",
    "categoryId": 1
  }'
```

---

## 데이터베이스

### 연결 정보 (테스트/개발용)

| 항목 | 값 | 권한 범위 |
|------|-----|-----------|
| **호스트** | `localhost` (로컬) / `mysql` (Docker) | - |
| **포트** | `3306` | - |
| **데이터베이스** | `blog` | - |
| **사용자** | `app` | SELECT, INSERT, UPDATE, DELETE (blog DB만) |
| **비밀번호** | `.env` 파일 참조 | - |

**주의사항:**
- `app` 사용자는 `blog` 데이터베이스에만 접근 가능합니다 (CREATE, DROP 권한 없음).
- Root 계정은 보안상 애플리케이션에서 사용하지 않습니다.

### 데이터베이스 스키마

#### 엔티티 관계도

```
User (1) -------- (*) Post -------- (*) Comment
  |                  |                  |
  |                  |---- (*) PostLike |
  |                  |                  |
  |                  |---- (0..1) Category
  |                  |
  |---- (*) Comment  |
  |                  |
  |---- (*) PostLike |
```

#### 테이블 구조

**users** (사용자)
- Primary Key: `id`
- Unique: `email`
- Index: `provider` + `provider_id`
- Fields: email, password_hash, nickname, role, status, provider, provider_id

**posts** (게시글)
- Primary Key: `id`
- Foreign Keys: `author_id` (users), `category_id` (categories)
- Index: `author_id`, `status`, `created_at`
- Fields: title, content, view_count, status

**comments** (댓글)
- Primary Key: `id`
- Foreign Keys: `post_id` (posts), `author_id` (users)
- Index: `post_id`, `author_id`, `status`, `created_at`
- Fields: content, status

**categories** (카테고리)
- Primary Key: `id`
- Unique: `name`, `slug`
- Index: `slug`
- Fields: name, slug, description

**post_likes** (좋아요)
- Primary Key: `id`
- Foreign Keys: `post_id` (posts), `user_id` (users)
- Unique: `post_id` + `user_id` (중복 좋아요 방지)
- Index: `post_id`, `user_id`, `created_at`

#### 시드 데이터

Flyway 마이그레이션으로 자동 생성되는 데이터:

| 항목 | 개수 |
|------|------|
| **관리자** | 1명 (admin@blog.com) |
| **일반 사용자** | 40명 (user1~40@blog.com) |
| **카테고리** | 10개 (공지사항, 자유게시판, 기술블로그 등) |
| **게시글** | 100개 (다양한 카테고리, 조회수 포함) |
| **댓글** | 자동 생성 |
| **좋아요** | 자동 생성 |

---

## 성능 및 보안

### 성능 최적화

#### 1. 데이터베이스 인덱싱

| 테이블 | 인덱스 | 목적 |
|--------|--------|------|
| users | `idx_users_email` | 로그인 쿼리 성능 |
| users | `idx_users_provider` | OAuth 사용자 조회 |
| posts | `idx_author_id`, `idx_status`, `idx_created_at` | 목록 조회, 필터링 |
| comments | `idx_post_id`, `idx_author_id`, `idx_status` | 댓글 조회 |
| post_likes | `uk_post_user` (UNIQUE) | 중복 좋아요 방지 + 조회 성능 |

#### 2. N+1 문제 해결

- JPA Fetch Join 사용 (게시글 조회 시 작성자 정보 함께 로드)
- Lazy Loading 적용 (불필요한 연관 엔티티 로드 방지)

#### 3. Redis 캐싱

| 캐시 대상 | TTL | 설명 |
|-----------|-----|------|
| Refresh Token | 14일 | JWT 갱신용 토큰 저장 |

**향후 개선 예정:**
- 인기 게시글 캐싱
- 카테고리 목록 캐싱
- 사용자 프로필 캐싱

### 보안 조치

#### 1. 인증/인가

| 항목 | 구현 방식 |
|------|-----------|
| **비밀번호 해싱** | BCrypt (strength 10) |
| **JWT 알고리즘** | HS256 (HMAC SHA-256) |
| **토큰 저장** | Refresh Token: Redis, Access Token: 클라이언트 |
| **세션 관리** | Stateless (SessionCreationPolicy.STATELESS) |
| **CSRF** | Disabled (stateless API) |

#### 2. 보안 헤더

```http
Cross-Origin-Opener-Policy: same-origin-allow-popups
Cross-Origin-Embedder-Policy: unsafe-none
```

#### 3. CORS 설정

- `.env`의 `CORS_ALLOWED_ORIGINS`로 허용 도메인 제한
- Preflight 요청 지원

#### 4. 민감 정보 보호

- 환경변수로 비밀 키 관리 (`.env` 파일, Git 제외)
- 로그에서 민감 헤더 마스킹 (Authorization, Cookie, X-API-Key 등)

#### 5. SQL Injection 방어

- JPA/Hibernate Prepared Statement 자동 사용
- 쿼리 파라미터 바인딩

#### 6. 사용자 상태 관리

| 상태 | 설명 |
|------|------|
| `ACTIVE` | 정상 사용자 |
| `SUSPENDED` | 정지된 사용자 (관리자가 로그인 차단 가능) |

### 현재 미구현 기능 (보안)

아래 항목들은 프로덕션 환경에서 추가 구현이 필요합니다:

- ❌ Rate Limiting (요청 횟수 제한)
- ❌ XSS 방어 (입력 값 sanitization)
- ❌ HTTPS 강제 (현재 HTTP)
- ❌ API Key / IP 화이트리스트
- ❌ 비밀번호 정책 (최소 길이, 복잡도 검증)

---

## 한계와 개선 계획

### 현재 시스템의 한계

#### 1. 인증/인가

| 한계 | 영향 | 우선순위 |
|------|------|----------|
| Rate Limiting 미구현 | 무차별 로그인 시도 공격 취약 | 높음 |
| Access Token 강제 무효화 불가 | 로그아웃 후에도 토큰 유효 (만료까지) | 중간 |
| 비밀번호 정책 없음 | 약한 비밀번호 허용 | 중간 |

#### 2. 성능

| 한계 | 영향 | 우선순위 |
|------|------|----------|
| 캐싱 부족 | 동일 쿼리 반복 실행 | 높음 |
| 페이징만 지원 (커서 X) | 대용량 데이터 조회 시 성능 저하 | 낮음 |
| 이미지 업로드 미지원 | 게시글에 이미지 첨부 불가 | 중간 |

#### 3. 기능

| 한계 | 영향 | 우선순위 |
|------|------|----------|
| 대댓글 미구현 | 댓글 depth 1 제한 | 낮음 |
| 알림 기능 없음 | 댓글/좋아요 알림 불가 | 중간 |
| 검색 기능 단순함 | 키워드 매칭만 지원 (형태소 분석 X) | 낮음 |

#### 4. 운영

| 한계 | 영향 | 우선순위 |
|------|------|----------|
| 로그 수집/분석 미흡 | 장애 추적 어려움 | 높음 |
| 모니터링 부재 | 서버 상태 실시간 파악 불가 | 높음 |
| 백업 전략 없음 | 데이터 손실 위험 | 높음 |

### 개선 계획 (우선순위별)

#### 우선순위: 높음

1. **Rate Limiting 구현**
   - Spring Rate Limiter (Bucket4j) 적용
   - 엔드포인트별 요청 제한 (예: 로그인 5회/분)

2. **Redis 캐싱 확대**
   - 인기 게시글 목록 (TTL: 5분)
   - 카테고리 목록 (TTL: 1시간)

3. **모니터링 구축**
   - Prometheus + Grafana 연동
   - 주요 메트릭: API 응답 시간, 에러율, DB 커넥션 풀

4. **로그 수집**
   - ELK Stack (Elasticsearch, Logstash, Kibana) 또는 CloudWatch
   - 에러 로그 알림 (Slack/Email)

#### 우선순위: 중간

5. **이미지 업로드**
   - AWS S3 연동
   - 이미지 리사이징 (썸네일 생성)

6. **알림 기능**
   - WebSocket 또는 Server-Sent Events (SSE)
   - 댓글/좋아요 실시간 알림

7. **비밀번호 정책**
   - 최소 8자, 대소문자+숫자+특수문자 조합
   - 비밀번호 재설정 기능 (이메일 인증)

8. **Access Token Blacklist**
   - Redis에 무효화된 토큰 저장
   - 로그아웃 시 즉시 토큰 차단

#### 우선순위: 낮음

9. **대댓글 (계층형 댓글)**
   - Comment 엔티티에 `parent_id` 추가
   - 재귀 쿼리 또는 Closure Table 패턴

10. **고급 검색**
    - Elasticsearch 연동
    - 형태소 분석 (nori 플러그인)

11. **커서 기반 페이징**
    - 무한 스크롤 지원
    - 대용량 데이터 성능 개선

---

## 프로젝트 구조

```
blog-api/
├── .github/
│   └── workflows/
│       └── docker-publish.yml    # GitHub Actions CI/CD
├── src/
│   ├── main/
│   │   ├── java/com/wsd/blogapi/
│   │   │   ├── admin/            # 관리자 API
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AdminService.java
│   │   │   │   └── dto/
│   │   │   ├── auth/             # 인증/인가
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── JwtProvider.java
│   │   │   │   ├── KakaoAuthService.java
│   │   │   │   └── dto/
│   │   │   ├── category/         # 카테고리
│   │   │   │   ├── Category.java (Entity)
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   └── CategoryRepository.java
│   │   │   ├── comment/          # 댓글
│   │   │   │   ├── Comment.java (Entity)
│   │   │   │   ├── CommentController.java
│   │   │   │   ├── CommentManagementController.java
│   │   │   │   ├── CommentService.java
│   │   │   │   └── CommentRepository.java
│   │   │   ├── common/           # 공통 모듈
│   │   │   │   ├── error/        # 예외 처리
│   │   │   │   └── logging/      # 요청 로깅
│   │   │   ├── config/           # 설정 클래스
│   │   │   │   ├── FirebaseConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── health/           # 헬스체크
│   │   │   │   └── HealthController.java
│   │   │   ├── like/             # 좋아요
│   │   │   │   ├── PostLike.java (Entity)
│   │   │   │   ├── PostLikeController.java
│   │   │   │   ├── PostLikeService.java
│   │   │   │   └── PostLikeRepository.java
│   │   │   ├── post/             # 게시글
│   │   │   │   ├── Post.java (Entity)
│   │   │   │   ├── PostController.java
│   │   │   │   ├── PostService.java
│   │   │   │   └── PostRepository.java
│   │   │   ├── security/         # Spring Security
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   ├── FirebaseAuthFilter.java
│   │   │   │   └── AuthUser.java
│   │   │   └── user/             # 사용자
│   │   │       ├── User.java (Entity)
│   │   │       ├── UserService.java
│   │   │       ├── UserRepository.java
│   │   │       └── UserRole.java (Enum)
│   │   └── resources/
│   │       ├── db/migration/      # Flyway SQL
│   │       │   ├── V1__init_users.sql
│   │       │   ├── V2__seed_users.sql
│   │       │   ├── V3__create_posts_table.sql
│   │       │   ├── V4__create_categories_table.sql
│   │       │   ├── V5__create_comments_table.sql
│   │       │   ├── V6__create_post_likes_table.sql
│   │       │   ├── V7__seed_data.sql
│   │       │   └── V8__seed_comments_likes.sql
│   │       ├── static/           # React 빌드 파일
│   │       ├── application.yml   # 공통 설정
│   │       ├── application-local.yml  # 로컬 환경
│   │       ├── application-prod.yml   # 프로덕션 환경
│   │       └── firebase-service-account.json  # Firebase 키 (Git 제외)
│   └── test/                     # 테스트 코드
│       └── java/com/wsd/blogapi/
├── login-app/                    # React 프론트엔드
├── .env                          # 환경변수 (Git 제외)
├── .env.example                  # 환경변수 템플릿
├── .gitignore
├── build.gradle                  # Gradle 빌드 설정
├── docker-compose.yml            # 프로덕션용 Compose
├── docker-compose.dev.yml        # 로컬 개발용 Compose
├── Dockerfile                    # 멀티스테이지 빌드
├── DEPLOYMENT.md                 # 배포 가이드
├── README.md                     # 본 문서
└── SWAGGER_GUIDE.md              # Swagger 사용 가이드
```

---

## 추가 문서

- [배포 가이드 (DEPLOYMENT.md)](./DEPLOYMENT.md) - Docker, GHCR, CI/CD
- [Swagger 사용 가이드 (SWAGGER_GUIDE.md)](./SWAGGER_GUIDE.md) - API 문서 사용법
- [개발 명세서 (dev.md)](./dev.md) - 개발 세부 사항

---

## 라이센스

MIT License

---

## 문의

프로젝트 관련 문의사항은 GitHub Issues에 남겨주세요.
