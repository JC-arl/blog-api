# Blog API

Spring Boot 기반 블로그 플랫폼 REST API 서버입니다.
JWT + 소셜 로그인(Firebase/Google, Kakao) 인증, Redis 캐싱, Docker 배포, Swagger 문서화를 지원합니다.

**🎯 과제1(HW1) 온라인 서점 시스템을 블로그 플랫폼으로 전환**
전자상거래 도메인(도서, 주문, 장바구니)을 콘텐츠 중심 도메인(게시글, 댓글, 좋아요)으로 재설계하였으며, Firebase와 Kakao OAuth를 통한 소셜 로그인 기능을 추가했습니다.

---

## 📑 목차

- [기술 스택](#기술-스택)
- [배포 주소](#배포-주소)
- [주요 기능](#주요-기능)
- [시작하기](#시작하기)
- [API 엔드포인트](#api-엔드포인트)
- [인증/인가](#인증인가)
- [데이터베이스](#데이터베이스)
- [프로젝트 구조](#프로젝트-구조)
- [배포 가이드](#배포-가이드)
- [문서](#문서)

---

## 🛠 기술 스택

### Backend
| 분류 | 기술 | 버전 |
|------|------|------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.4.1 |
| **ORM** | Spring Data JPA | (Hibernate 6.4) |
| **Security** | Spring Security | 6.x |
| **Database** | MySQL | 8.0 |
| **Cache** | Redis | 7-alpine |
| **Migration** | Flyway | 10.20.1 |
| **Build Tool** | Gradle | 8.5 |

### Authentication & External Services
| 분류 | 기술 |
|------|------|
| **JWT** | HS256 (Access: 15min, Refresh: 14days) |
| **Password** | BCrypt (strength 10) |
| **Firebase** | Admin SDK 9.4.2 (Google 로그인) |
| **Kakao** | OAuth 2.0 REST API |

### Frontend
| 분류 | 기술 | 버전 |
|------|------|------|
| **Framework** | React | 19.2.0 |
| **Language** | TypeScript | 4.9.5 |
| **Firebase** | Firebase SDK | 12.6.0 |
| **Build** | React Scripts | 5.0.1 |

### DevOps & Documentation
| 분류 | 기술 |
|------|------|
| **Container** | Docker, Docker Compose |
| **CI/CD** | GitHub Actions |
| **Registry** | GHCR (GitHub Container Registry) |
| **API Docs** | SpringDoc OpenAPI 3 (Swagger UI) |

---
## 헬스체크


## 🌐 배포 주소
![헬스체크](healthcheck_200.png)
 
### 프로덕션 환경

| 항목 | URL                                          | 설명 |
|------|----------------------------------------------|------|
| **Base URL** | `http://113.198.66.68`                       | API 기본 주소 (포트 18132) |
| **Swagger UI** | `http://113.198.66.68:18132/swagger-ui/index.html` | API 문서 및 테스트 |
| **Health Check** | `http://113.198.66.68:18132/health`                | 서버 상태 확인 |
| **React App** | `http://113.198.66.68`                       | 프론트엔드 (login-app) |

### 로컬 개발 환경

| 항목 | URL |
|------|-----|
| **Base URL** | `http://localhost:8080` |
| **Swagger UI** | `http://locahost:8080/swagger-ui/index.html` |
| **React Dev Server** | `http://localhost:3000` (npm start) |

---

## ✨ 주요 기능

### 1. 다중 인증 시스템 (3가지 방식)

#### 로컬 인증 (Email/Password)
- 회원가입 및 로그인
- BCrypt 비밀번호 해싱
- JWT Access/Refresh Token 발급

#### Firebase 인증 (Google 소셜 로그인)
- Google 계정으로 원클릭 로그인
- Firebase ID Token 검증
- 자동 사용자 생성

#### Kakao OAuth 인증
- Kakao 계정으로 로그인
- Kakao Access Token → Firebase Custom Token 변환
- 팝업 기반 OAuth 플로우

### 2. 콘텐츠 관리

#### 게시글 (Posts)
- ✅ CRUD 작업 (생성, 조회, 수정, 삭제)
- ✅ 조회수 자동 증가
- ✅ 카테고리별 분류
- ✅ 제목/내용 검색
- ✅ 게시글 상태 관리 (PUBLISHED, DRAFT, DELETED)
- ✅ Soft Delete (작성자) / Hard Delete (관리자)

#### 댓글 (Comments)
- ✅ 게시글별 댓글 작성
- ✅ 댓글 수정/삭제
- ✅ 작성자 본인만 수정 가능
- ✅ 관리자 강제 삭제

#### 좋아요 (Likes)
- ✅ 게시글 좋아요/취소 토글
- ✅ 중복 좋아요 방지
- ✅ 좋아요 수 집계
- ✅ 내가 좋아요한 게시글 목록

#### 카테고리 (Categories)
- ✅ 동적 카테고리 생성/수정/삭제 (관리자)
- ✅ SEO 친화적 Slug
- ✅ 카테고리별 게시글 필터링

### 3. 사용자 관리

#### 프로필
- ✅ GET /auth/me - 내 정보 조회
- ✅ PATCH /auth/me - 프로필 수정 (닉네임)
- ✅ 다중 제공자 지원 (LOCAL, GOOGLE, FIREBASE, KAKAO)

#### 관리자 기능
- ✅ 전체 사용자 관리 (조회, 수정, 삭제)
- ✅ 사용자 정지/활성화
- ✅ 플랫폼 통계 (사용자, 게시글, 댓글, 좋아요 수)
- ✅ 콘텐츠 강제 삭제

### 4. 성능 최적화

- ✅ Redis 기반 Refresh Token 저장
- ✅ 페이지네이션 (모든 목록 조회)
- ✅ 데이터베이스 인덱스 최적화
- ✅ N+1 문제 방지 (JPA 최적화)

### 5. 보안

- ✅ JWT 기반 Stateless 인증
- ✅ BCrypt 비밀번호 해싱
- ✅ CORS 설정 (화이트리스트)
- ✅ 요청 로깅 (민감 정보 마스킹)
- ✅ Role 기반 접근 제어 (RBAC)
- ✅ Firebase 서비스 계정 키 분리 (`secrets/` 디렉토리)

---

## 🚀 시작하기

### 1. 필수 요구사항

- **Java 21+**
- **Docker & Docker Compose**
- **Gradle 8.5+** (optional, wrapper 포함)
- **Git**

### 2. 저장소 클론

```bash
git clone https://github.com/jc-arl/blog-api.git
cd blog-api
```

### 3. Firebase 서비스 계정 키 설정

Firebase Console에서 서비스 계정 키를 다운로드하여 `secrets/` 디렉토리에 배치:

```bash
mkdir -p secrets
# Firebase Console > Project Settings > Service Accounts > Generate new private key
# 다운로드한 파일을 secrets/firebase-service-account.json으로 저장
cp ~/Downloads/firebase-service-account.json secrets/
chmod 600 secrets/firebase-service-account.json
```

### 4. 환경 변수 설정

`.env.example`을 복사하여 `.env` 파일 생성:

```bash
cp .env.example .env
```

`.env` 파일 편집:

```properties
# 로컬 개발
APP_PORT=8080
SPRING_PROFILES_ACTIVE=local

# 데이터베이스
MYSQL_ROOT_PASSWORD=your-password
MYSQL_DATABASE=blog
MYSQL_USER=app
MYSQL_PASSWORD=your-password

# JWT
JWT_SECRET=your-super-secret-jwt-key-at-least-32-bytes-long

# Firebase
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_SERVICE_ACCOUNT_PATH=secrets/firebase-service-account.json

# Kakao
KAKAO_REST_API_KEY=your-kakao-rest-api-key

# React (로컬 개발)
REACT_APP_BACKEND_URL=http://localhost:8080
REACT_APP_FIREBASE_API_KEY=your-firebase-api-key
REACT_APP_FIREBASE_AUTH_DOMAIN=your-project.firebaseapp.com
REACT_APP_FIREBASE_PROJECT_ID=your-firebase-project-id
# ... (나머지 Firebase 설정)
```

### 5. Docker Compose로 실행

#### 로컬 개발 (소스 빌드)

```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
```

#### 프로덕션 (GHCR 이미지 사용)

```bash
docker-compose up -d
```

### 6. 서버 확인

```bash
# Health Check
curl http://localhost:8080/health

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

### 7. React 프론트엔드 개발 (선택)

```bash
cd login-app
npm install
npm start
# 브라우저에서 http://localhost:3000 열림
```

---

## 📡 API 엔드포인트

### 인증 (Auth)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/auth/signup` | 회원가입 (LOCAL) | ❌ |
| POST | `/auth/login` | 로그인 (LOCAL) | ❌ |
| POST | `/auth/refresh` | Access Token 갱신 | ❌ |
| POST | `/auth/logout` | 로그아웃 | ✅ JWT |
| POST | `/auth/kakao-login` | Kakao 로그인 | ❌ |
| GET | `/auth/me` | 내 정보 조회 | ✅ JWT |
| PATCH | `/auth/me` | 프로필 수정 (닉네임) | ✅ JWT |

### 게시글 (Posts)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/posts` | 게시글 작성 | ✅ JWT |
| GET | `/posts` | 게시글 목록 (페이징) | ❌ |
| GET | `/posts/search?keyword=검색어` | 게시글 검색 | ❌ |
| GET | `/posts/category/{categoryId}` | 카테고리별 게시글 | ❌ |
| GET | `/posts/{id}` | 게시글 상세 (조회수↑) | ❌ |
| GET | `/posts/my` | 내 게시글 목록 | ✅ JWT |
| PUT | `/posts/{id}` | 게시글 수정 | ✅ 작성자 |
| DELETE | `/posts/{id}` | 게시글 삭제 (soft) | ✅ 작성자 |
| DELETE | `/posts/{id}/force` | 게시글 강제 삭제 | ✅ ADMIN |

### 댓글 (Comments)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/posts/{postId}/comments` | 댓글 작성 | ✅ JWT |
| GET | `/posts/{postId}/comments` | 게시글 댓글 목록 | ❌ |
| GET | `/posts/{postId}/comments/count` | 댓글 수 조회 | ❌ |
| GET | `/comments/{id}` | 댓글 상세 조회 | ❌ |
| GET | `/comments/my` | 내 댓글 목록 | ✅ JWT |
| PUT | `/comments/{id}` | 댓글 수정 | ✅ 작성자 |
| DELETE | `/comments/{id}` | 댓글 삭제 (soft) | ✅ 작성자 |
| DELETE | `/comments/{id}/force` | 댓글 강제 삭제 | ✅ ADMIN |

### 좋아요 (Likes)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/posts/{postId}/like` | 좋아요 토글 | ✅ JWT |
| GET | `/posts/{postId}/like/status` | 좋아요 여부 확인 | ✅ JWT |
| GET | `/posts/{postId}/like/count` | 좋아요 수 조회 | ❌ |
| GET | `/posts/{postId}/likes` | 좋아요 사용자 목록 | ❌ |
| GET | `/likes/my` | 내가 좋아요한 게시글 | ✅ JWT |

### 카테고리 (Categories)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/categories` | 카테고리 생성 | ✅ ADMIN |
| GET | `/categories` | 카테고리 목록 | ❌ |
| GET | `/categories/{id}` | 카테고리 상세 | ❌ |
| GET | `/categories/slug/{slug}` | Slug로 카테고리 조회 | ❌ |
| PUT | `/categories/{id}` | 카테고리 수정 | ✅ ADMIN |
| DELETE | `/categories/{id}` | 카테고리 삭제 | ✅ ADMIN |

### 관리자 (Admin)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/admin/statistics` | 플랫폼 통계 | ✅ ADMIN |
| GET | `/admin/users` | 사용자 목록 | ✅ ADMIN |
| GET | `/admin/users/{id}` | 사용자 상세 | ✅ ADMIN |
| PUT | `/admin/users/{id}` | 사용자 정보 수정 | ✅ ADMIN |
| POST | `/admin/users/{id}/suspend` | 사용자 정지 | ✅ ADMIN |
| POST | `/admin/users/{id}/activate` | 사용자 활성화 | ✅ ADMIN |
| DELETE | `/admin/users/{id}` | 사용자 삭제 | ✅ ADMIN |

### 헬스체크

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/health` | 서버 상태 확인 | ❌ |

**총 엔드포인트 수: 45개** (HW1: 44개)

---

## 🔐 인증/인가

### 인증 방식 (3가지)

#### 1. LOCAL 인증 (Email/Password)

```http
POST /auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "홍길동"
}
```

**응답:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 2. Firebase 인증 (Google)

```
Client → Firebase signInWithGoogle()
      → Get Firebase ID Token
      → POST /auth/firebase-login (idToken)
      → Server: Verify with Firebase Admin SDK
      → Server: Create/Find User
      → Server: Issue JWT tokens
```

#### 3. Kakao OAuth 인증

```
Client → Kakao Login Popup
      → Redirect with authorization code
      → POST /auth/kakao-login (kakaoAccessToken)
      → Server: Fetch user info from Kakao API
      → Server: Create/Find User
      → Server: Generate Firebase Custom Token
      → Client: Sign into Firebase with custom token
      → Client: Use Firebase ID Token for API calls
```

### JWT 토큰 사용

```http
GET /posts/my
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 권한 체계

| Role | 설명 | 권한 |
|------|------|------|
| **ROLE_USER** | 일반 사용자 | 게시글/댓글/좋아요 작성, 본인 콘텐츠 수정/삭제 |
| **ROLE_ADMIN** | 관리자 | 모든 USER 권한 + 사용자 관리, 카테고리 관리, 강제 삭제 |

### Provider 타입

- `LOCAL` - 이메일/비밀번호 회원가입
- `GOOGLE` - Google 계정 (Firebase Auth)
- `FIREBASE` - Firebase 인증
- `KAKAO` - Kakao 계정

---

## 🗄 데이터베이스

### ERD

```
┌──────────────┐
│    users     │
│  (사용자)     │
└──────┬───────┘
       │ 1
       │
       ├─────────────────┐
       │ N               │ N
┌──────▼────────┐  ┌────▼──────────┐
│    posts      │  │   comments    │
│   (게시글)     │  │    (댓글)      │
└──────┬────────┘  └────┬──────────┘
       │ N              │ N
       │                │
┌──────▼────────┐  ┌────▼──────────┐
│  post_likes   │  │     posts     │
│   (좋아요)     │  │   (게시글)     │
└───────────────┘  └───────────────┘

┌──────────────┐
│  categories  │
│  (카테고리)   │
└──────┬───────┘
       │ 1
       │ N
┌──────▼────────┐
│    posts      │
│   (게시글)     │
└───────────────┘
```

### 테이블 목록 (5개)

| 테이블 | 설명 | 주요 컬럼 |
|--------|------|----------|
| **users** | 사용자 | id, email, nickname, role, provider |
| **posts** | 게시글 | id, title, content, author_id, category_id, view_count |
| **categories** | 카테고리 | id, name, slug, description |
| **comments** | 댓글 | id, content, post_id, author_id, status |
| **post_likes** | 좋아요 | id, post_id, user_id, created_at |

### Flyway 마이그레이션

| 파일 | 설명 |
|------|------|
| `V1__init_users.sql` | users 테이블 생성 |
| `V2__seed_users.sql` | 관리자 계정 생성 (admin@blog.com) |
| `V3__create_posts_table.sql` | posts 테이블 생성 |
| `V4__create_categories_table.sql` | categories 테이블 생성 |
| `V5__create_comments_table.sql` | comments 테이블 생성 |
| `V6__create_post_likes_table.sql` | post_likes 테이블 생성 |
| `V7__seed_data.sql` | 테스트 데이터 (카테고리 10개, 사용자 40명, 게시글 100개) |
| `V8__seed_comments_likes.sql` | 댓글/좋아요 테스트 데이터 |

---

## 📂 프로젝트 구조

```
blog-api/
├── src/main/java/com/wsd/blogapi/
│   ├── auth/                      # 인증 도메인
│   │   ├── AuthController.java   # 로그인, 회원가입, Firebase, Kakao
│   │   ├── AuthService.java
│   │   ├── KakaoAuthService.java
│   │   ├── RedisTokenService.java
│   │   └── dto/                   # Auth DTOs
│   ├── user/                      # 사용자 도메인
│   │   ├── User.java              # @Entity
│   │   ├── UserRepository.java
│   │   └── UserRole.java
│   ├── post/                      # 게시글 도메인
│   │   ├── Post.java
│   │   ├── PostController.java
│   │   ├── PostService.java
│   │   └── PostRepository.java
│   ├── comment/                   # 댓글 도메인
│   │   ├── Comment.java
│   │   ├── CommentController.java
│   │   └── CommentManagementController.java
│   ├── like/                      # 좋아요 도메인
│   │   ├── PostLike.java
│   │   └── PostLikeController.java
│   ├── category/                  # 카테고리 도메인
│   │   ├── Category.java
│   │   └── CategoryController.java
│   ├── admin/                     # 관리자 도메인
│   │   ├── AdminController.java
│   │   └── AdminService.java
│   ├── security/                  # 보안
│   │   ├── JwtProvider.java
│   │   ├── JwtAuthFilter.java
│   │   ├── FirebaseAuthFilter.java
│   │   ├── SecurityConfig.java
│   │   └── AuthUser.java
│   ├── common/                    # 공통 기능
│   │   ├── config/
│   │   ├── error/
│   │   └── logging/
│   └── health/                    # 헬스체크
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-local.yml
│   ├── application-prod.yml
│   └── db/migration/              # Flyway SQL
│
├── login-app/                     # React 프론트엔드
│   ├── src/
│   │   ├── firebaseConfig.ts
│   │   ├── kakaoConfig.ts
│   │   └── App.tsx
│   ├── package.json
│   └── public/
│
├── secrets/                       # Firebase 서비스 계정 키
│   └── firebase-service-account.json
│
├── docs/                          # 프로젝트 문서
│   ├── api-design.md
│   ├── db-schema.md
│   └── architecture.md
│
├── docker-compose.yml
├── docker-compose.dev.yml
├── Dockerfile
├── .env.example
└── README.md
```

---

## 🧪 테스트 계정

### 관리자

- **Email**: `admin@blog.com`
- **Password**: `admin1234`
- **Role**: `ROLE_ADMIN`

### 일반 사용자

- **Email**: `user1@blog.com` ~ `user40@blog.com`
- **Password**: `user1234`
- **Role**: `ROLE_USER`

---

## 📦 배포 가이드

자세한 배포 가이드는 [DEPLOYMENT.md](./DEPLOYMENT.md)를 참고하세요.

### 간단 배포 (GitHub Actions → GHCR)

```bash
# 1. 코드 변경 후 커밋 & 푸시
git add .
git commit -m "Feature: Add new feature"
git push origin main

# 2. GitHub Actions 자동 빌드 (React + Spring Boot + Docker)
# 3. GHCR에 이미지 푸시 (ghcr.io/jc-arl/blog-api:latest)

# 4. 서버에서 배포
docker compose pull
docker compose up -d
```

---

## 📚 문서

| 문서 | 설명 |
|------|------|
| [API 설계 문서](./docs/api-design.md) | HW1과의 API 차이점, 전체 엔드포인트 목록 |
| [DB 스키마 문서](./docs/db-schema.md) | ERD, 테이블 상세, 인덱스 전략 |
| [아키텍처 문서](./docs/architecture.md) | 시스템 구조, 레이어, 보안, 배포 |
| [배포 가이드](./DEPLOYMENT.md) | 프로덕션 배포 체크리스트, 환경 설정 |

---

## 🔧 환경변수 설명

주요 환경변수 목록입니다. 전체 목록은 `.env.example`을 참고하세요.

### 서버 설정

| 변수 | 설명 | 예시 |
|------|------|------|
| `APP_PORT` | 서버 포트 | `80` (프로덕션), `8080` (로컬) |
| `SPRING_PROFILES_ACTIVE` | Spring Profile | `prod` 또는 `local` |
| `PUBLISHED_URL` | 서버 공인 IP/도메인 | `113.198.66.68` |

### 데이터베이스

| 변수 | 설명 | 예시 |
|------|------|------|
| `MYSQL_HOST` | MySQL 호스트 | `mysql` (Docker), `localhost` |
| `MYSQL_PORT` | MySQL 포트 | `3306` |
| `MYSQL_DATABASE` | 데이터베이스 이름 | `blog` |
| `MYSQL_USER` | MySQL 사용자 | `app` |
| `MYSQL_PASSWORD` | MySQL 비밀번호 | `your-password` |

### JWT

| 변수 | 설명 | 예시 |
|------|------|------|
| `JWT_SECRET` | JWT 서명 키 (32자 이상) | `your-secret-key-32-bytes` |
| `JWT_ACCESS_EXP` | Access Token 만료 시간 (초) | `900` (15분) |
| `JWT_REFRESH_EXP` | Refresh Token 만료 시간 (초) | `1209600` (14일) |

### Firebase

| 변수 | 설명 | 예시 |
|------|------|------|
| `FIREBASE_PROJECT_ID` | Firebase 프로젝트 ID | `wsd-blogapi` |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | 서비스 계정 키 경로 | `secrets/firebase-service-account.json` |

### Kakao

| 변수 | 설명 | 예시 |
|------|------|------|
| `KAKAO_REST_API_KEY` | Kakao REST API 키 | `your-kakao-api-key` |

### React (프론트엔드)

| 변수 | 설명 |
|------|------|
| `REACT_APP_BACKEND_URL` | 백엔드 API URL |
| `REACT_APP_FIREBASE_API_KEY` | Firebase API 키 |
| `REACT_APP_KAKAO_REST_API_KEY` | Kakao REST API 키 |

---

## 🔒 보안 기능

- ✅ **JWT 인증**: Stateless 인증 (Access 15분, Refresh 14일)
- ✅ **BCrypt**: 비밀번호 해싱 (strength 10)
- ✅ **CORS**: 화이트리스트 기반 허용
- ✅ **Secrets 분리**: Firebase 키 별도 관리 (`secrets/`)
- ✅ **Role 기반 권한**: `@PreAuthorize` 메서드 레벨 보안
- ✅ **요청 로깅**: 민감 정보 마스킹 (Authorization, Cookie 등)
- ✅ **Firebase 토큰 검증**: Admin SDK 활용
- ✅ **Kakao OAuth**: 표준 OAuth 2.0 플로우

---

## 📈 성능 최적화

- ✅ **페이지네이션**: Spring Data Pageable 활용
- ✅ **인덱스**: 모든 FK, 검색 필드 인덱스
- ✅ **Redis 캐싱**: Refresh Token 저장
- ✅ **N+1 방지**: JPA Fetch Join, EntityGraph
- ✅ **Connection Pool**: HikariCP (기본)

---

## 📞 문의

- **Repository**: [https://github.com/jc-arl/blog-api](https://github.com/jc-arl/blog-api)
- **Issues**: GitHub Issues 탭 활용

---

**🎓 과제 요구사항**: HW1 (온라인 서점) → HW2 (블로그 플랫폼) 전환 완료
**🚀 배포**: GitHub Actions + GHCR + Docker Compose
**📚 문서**: Swagger UI + 상세 .md 문서
**🔐 인증**: JWT + Firebase + Kakao OAuth (3-way)
