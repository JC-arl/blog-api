# Architecture 문서 (Blog API)

## 목차
- [시스템 아키텍처](#시스템-아키텍처)
- [HW1과의 아키텍처 차이](#hw1과의-아키텍처-차이)
- [계층 구조](#계층-구조)
- [패키지 구조](#패키지-구조)
- [기술 스택](#기술-스택)
- [보안 아키텍처](#보안-아키텍처)
- [배포 아키텍처](#배포-아키텍처)

## 시스템 아키텍처

### 전체 시스템 구조

```
┌─────────────────────────────────────────────────────────┐
│                    Client Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Browser    │  │  React App   │  │   Postman    │  │
│  │  (Desktop)   │  │  (login-app) │  │  (Testing)   │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
└─────────┼──────────────────┼──────────────────┼──────────┘
          │                  │                  │
          │ HTTP/HTTPS       │                  │
          └──────────────────┴──────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────┐
│              External Auth Services                      │
│  ┌──────────────┐         ┌──────────────┐              │
│  │   Firebase   │         │    Kakao     │              │
│  │     Auth     │         │    OAuth     │              │
│  └──────┬───────┘         └──────┬───────┘              │
└─────────┼────────────────────────┼───────────────────────┘
          │ ID Token               │ Access Token
          └────────────────────────┘
                    │
┌───────────────────▼──────────────────────────────────────┐
│              Spring Boot Application                     │
│  ┌────────────────────────────────────────────────────┐  │
│  │              Filter Layer                          │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  RequestLoggingFilter                        │  │  │
│  │  │  → RateLimitingFilter (Redis)                │  │  │
│  │  │  → JwtAuthenticationFilter                   │  │  │
│  │  │  → SecurityFilterChain (Spring Security)     │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  └─────────────────────┬──────────────────────────────┘  │
│                        │                                  │
│  ┌─────────────────────▼──────────────────────────────┐  │
│  │           Controller Layer                         │  │
│  │  ┌─────────┐ ┌─────────┐ ┌──────────┐ ┌─────────┐ │  │
│  │  │  Auth   │ │  Post   │ │ Comment  │ │  Like   │ │  │
│  │  │  Admin  │ │Category │ │ Health   │ │   ...   │ │  │
│  │  └────┬────┘ └────┬────┘ └────┬─────┘ └────┬────┘ │  │
│  └───────┼───────────┼───────────┼──────────────┼──────┘  │
│          │           │           │              │          │
│  ┌───────▼───────────▼───────────▼──────────────▼──────┐  │
│  │              Service Layer                          │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐            │  │
│  │  │AuthService│ PostService│ LikeService│            │  │
│  │  │(Firebase)│ │(CRUD)    │ │(Toggle)  │            │  │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘            │  │
│  └───────┼────────────┼────────────┼────────────────────┘  │
│          │            │            │                        │
│  ┌───────▼────────────▼────────────▼────────────────────┐  │
│  │           Repository Layer (JPA)                     │  │
│  │  UserRepository, PostRepository, LikeRepository      │  │
│  └───────┬────────────────────────────────────────────────┘  │
└──────────┼──────────────────────────────────────────────────┘
           │
   ┌───────┴────────┐
   │                │
┌──▼──────────┐  ┌─▼───────┐
│    MySQL    │  │  Redis  │
│ (Database)  │  │ (Cache) │
│             │  │         │
│ - users     │  │ - Tokens│
│ - posts     │  │ - Rate  │
│ - comments  │  │   Limit │
│ - likes     │  │ - Session│
└─────────────┘  └─────────┘
```

## HW1과의 아키텍처 차이

### 시스템 구조 비교

| 항목 | HW1 (과제1) | 현재 구현 (Blog API) |
|------|------------|---------------------|
| **클라이언트** | 없음 (API only) | React App (login-app) 포함 |
| **인증 서비스** | 없음 (JWT only) | Firebase + Kakao OAuth |
| **필터 수** | 3개 | 3개 (동일) |
| **도메인 수** | 8개 | 5개 |
| **외부 연동** | 없음 | Firebase Admin SDK, Kakao API |

### 레이어 구조 비교

```
HW1 (과제1):
Controller → Service → Repository → MySQL/Redis
(Books, Orders, Cart, Reviews, Favorites)

현재 (Blog API):
Controller → Service → Repository → MySQL/Redis
                      ↓
              Firebase Admin SDK
              Kakao OAuth API
(Posts, Comments, Likes, Categories)
```

## 계층 구조

### 1. Filter Layer (필터 계층) - HW1과 동일

HTTP 요청이 컨트롤러에 도달하기 전 처리

**필터 체인**:
```
Request
   │
   ├─► RequestLoggingFilter (모든 요청 로깅)
   │    - 요청 URL, 메서드, IP 주소 기록
   │    - 응답 시간 측정
   │
   ├─► RateLimitingFilter (레이트 리미팅)
   │    - Redis 기반 IP별 카운팅
   │    - /auth/login, /auth/signup만 적용
   │    - 분당 30회 제한
   │
   ├─► JwtAuthenticationFilter (JWT 토큰 검증)
   │    - Authorization 헤더에서 토큰 추출
   │    - JWT 서명 및 만료 검증
   │    - Redis 블랙리스트 확인
   │    - SecurityContext 설정
   │    - 제외: /health, /swagger-ui/*, /auth/*
   │
   ├─► SecurityFilterChain (Spring Security)
   │    - 엔드포인트별 권한 검사 (ROLE_USER, ROLE_ADMIN)
   │    - 인증 여부 확인
   │    - CSRF 비활성화 (Stateless API)
   │
   └─► Controller (비즈니스 로직)
```

**HW1 대비 변경 없음**: 필터 구조 동일

### 2. Controller Layer (표현 계층)

HTTP 요청/응답 처리, DTO 변환

**주요 컴포넌트**:
- `@RestController`: REST API 엔드포인트
- `@RequestMapping`: URL 매핑
- `@Valid`: 입력 검증
- `ResponseEntity<T>`: HTTP 응답

**Controller 목록**:

| Controller | 경로 | 주요 기능 |
|-----------|------|----------|
| AuthController | /auth/* | 로그인, 회원가입, Firebase, Kakao |
| PostController | /posts/* | 게시글 CRUD, 조회수 |
| CommentController | /posts/{id}/comments | 댓글 CRUD |
| PostLikeController | /posts/{id}/likes | 좋아요 추가/취소 |
| CategoryController | /categories | 카테고리 관리 |
| AdminController | /admin/* | 관리자 기능 |
| HealthController | /health | 헬스체크 |

**HW1 대비 변경**:
- ❌ 제거: BookController, OrderController, CartController, FavoriteController
- ✅ 추가: PostController, CommentController, PostLikeController, CategoryController
- ✅ 변경: AuthController에 Firebase, Kakao OAuth 추가

### 3. Service Layer (비즈니스 로직 계층)

핵심 비즈니스 로직 구현, 트랜잭션 관리

**주요 컴포넌트**:
- `@Service`: 비즈니스 로직
- `@Transactional`: 트랜잭션 관리
- DTO 변환
- 예외 처리

**Service 목록 및 주요 로직**:

| Service | 주요 로직 |
|---------|----------|
| **AuthService** | • 회원가입 (BCrypt 해싱)<br>• 로그인 (JWT 발급)<br>• Firebase ID Token 검증<br>• Kakao OAuth 토큰 교환<br>• Refresh Token 갱신<br>• 로그아웃 (블랙리스트 등록) |
| **PostService** | • 게시글 CRUD<br>• 조회수 증가 (비관적 락 또는 비동기)<br>• 페이지네이션<br>• 카테고리별 필터링 |
| **CommentService** | • 댓글 작성/수정/삭제<br>• 게시글별 댓글 조회<br>• 소유권 검증 |
| **PostLikeService** | • 좋아요 추가/취소 (토글)<br>• 중복 방지 (UNIQUE 제약)<br>• 좋아요 수 집계 |
| **CategoryService** | • 카테고리 CRUD<br>• Slug 자동 생성 |

**HW1 대비 변경**:
- ❌ 제거: BookService (재고 관리), OrderService (주문 상태 관리), CartService (장바구니 계산)
- ✅ 추가: PostService (조회수), PostLikeService (토글), CategoryService (Slug)
- ✅ 확장: AuthService (Firebase, Kakao 통합)

**트랜잭션 전략**:
```java
// 읽기 전용 최적화
@Transactional(readOnly = true)
public PostResponse getPost(Long id) { ... }

// 쓰기 작업
@Transactional
public PostResponse createPost(PostCreateRequest request) { ... }
```

### 4. Repository Layer (데이터 접근 계층)

JPA 기반 데이터베이스 CRUD

**주요 컴포넌트**:
- `JpaRepository<Entity, ID>`: Spring Data JPA
- Query Methods: `findByXXX`, `existsByXXX`
- `@Query`: Custom JPQL

**Repository 목록**:

| Repository | Custom Query 예시 |
|-----------|------------------|
| UserRepository | `findByEmail(String email)`<br>`findByProviderAndProviderId(Provider, String)` |
| PostRepository | `findByAuthorId(Long authorId)`<br>`findByCategoryId(Long categoryId, Pageable)` |
| CommentRepository | `findByPostId(Long postId, Pageable)` |
| PostLikeRepository | `findByPostIdAndUserId(Long, Long)`<br>`countByPostId(Long postId)` |
| CategoryRepository | `findBySlug(String slug)` |

**HW1 대비 변경**:
- ❌ 제거: BookRepository, OrderRepository, CartRepository, FavoriteRepository
- ✅ 추가: PostRepository, CommentRepository, PostLikeRepository, CategoryRepository
- ✅ 변경: UserRepository에 provider 기반 조회 추가

## 패키지 구조

### 현재 구조 (도메인 중심)

```
src/main/java/com/wsd/blogapi/
├── auth/                      # 인증 도메인
│   ├── AuthController.java   # 로그인, 회원가입, Firebase, Kakao
│   ├── AuthService.java       # 인증 로직
│   └── dto/                   # Auth DTOs
│
├── user/                      # 사용자 도메인
│   ├── User.java              # @Entity
│   ├── UserRepository.java    # JPA Repository
│   └── UserService.java       # 사용자 관리
│
├── post/                      # 게시글 도메인
│   ├── Post.java              # @Entity
│   ├── PostRepository.java
│   ├── PostService.java
│   ├── PostController.java
│   └── dto/                   # Post DTOs
│
├── comment/                   # 댓글 도메인
│   ├── Comment.java           # @Entity
│   ├── CommentRepository.java
│   ├── CommentService.java
│   ├── CommentController.java
│   └── CommentManagementController.java
│
├── like/                      # 좋아요 도메인
│   ├── PostLike.java          # @Entity
│   ├── PostLikeRepository.java
│   ├── PostLikeService.java
│   └── PostLikeController.java
│
├── category/                  # 카테고리 도메인
│   ├── Category.java          # @Entity
│   ├── CategoryRepository.java
│   ├── CategoryService.java
│   └── CategoryController.java
│
├── admin/                     # 관리자 도메인
│   ├── AdminController.java   # 사용자/게시글/댓글 관리
│   └── AdminService.java
│
├── common/                    # 공통 기능
│   ├── config/                # 설정
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   ├── FirebaseConfig.java
│   │   └── SwaggerConfig.java
│   ├── security/              # 보안
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── UserPrincipal.java
│   │   └── SecurityUtil.java
│   ├── error/                 # 에러 처리
│   │   ├── ErrorCode.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── BusinessException.java
│   ├── logging/               # 로깅
│   │   └── RequestLoggingFilter.java
│   └── util/                  # 유틸리티
│
├── health/                    # 헬스체크
│   └── HealthController.java
│
└── BlogApiApplication.java    # Main

src/main/resources/
├── application.yml             # 공통 설정
├── application-local.yml       # 로컬 환경
├── application-prod.yml        # 프로덕션 환경
├── db/migration/               # Flyway 마이그레이션
│   ├── V1__init_users.sql
│   ├── V2__seed_users.sql
│   ├── V3__create_posts_table.sql
│   ├── V4__create_categories_table.sql
│   ├── V5__create_comments_table.sql
│   ├── V6__create_post_likes_table.sql
│   ├── V7__seed_data.sql
│   └── V8__seed_comments_likes.sql
└── firebase-service-account.json  # (로컬만, 프로덕션은 secrets/)
```

### HW1 패키지 구조와 비교

**HW1 (과제1) - 계층형 구조**:
```
bookstoreapi/
├── domain/
│   ├── auth/
│   ├── user/
│   ├── book/      ❌ 제거
│   ├── order/     ❌ 제거
│   ├── review/    ❌ 제거
│   ├── favorite/  ❌ 제거
│   └── cart/      ❌ 제거
├── global/
│   ├── api/
│   ├── config/
│   ├── error/
│   ├── security/
│   ├── logging/
│   └── rate/
```

**현재 (Blog API) - 도메인 중심**:
```
blogapi/
├── auth/          ✅ 확장 (Firebase, Kakao)
├── user/          ✅ 유지
├── post/          ✅ 추가
├── comment/       ✅ 추가
├── like/          ✅ 추가
├── category/      ✅ 추가
├── admin/         ✅ 단순화
├── common/        ✅ 유지 (Firebase 설정 추가)
└── health/        ✅ 유지
```

**구조 철학 변경**:
- HW1: 계층형 (domain/controller, domain/service, domain/repository 분리)
- 현재: 도메인 중심 (각 도메인별로 controller, service, repository 응집)

## 기술 스택

### Backend Framework

| 항목 | HW1 (과제1) | 현재 (Blog API) | 비고 |
|------|-----------|----------------|------|
| Spring Boot | 3.4.0 | 3.4.0 | 동일 |
| Spring Security | ✅ | ✅ | 동일 |
| Spring Data JPA | ✅ | ✅ | 동일 |
| Spring Validation | ✅ | ✅ | 동일 |

### Database

| 항목 | HW1 (과제1) | 현재 (Blog API) | 비고 |
|------|-----------|----------------|------|
| MySQL | 8.0 | 8.0 | 동일 |
| Redis | 7-alpine | 7-alpine | 동일 |
| Flyway | ✅ | ✅ | 동일 |

### Security

| 항목 | HW1 (과제1) | 현재 (Blog API) | 비고 |
|------|-----------|----------------|------|
| JWT | ✅ | ✅ | 동일 |
| BCrypt | ✅ | ✅ | 동일 |
| CORS | ✅ | ✅ | 동일 |
| Firebase Admin SDK | ❌ | ✅ | **추가** |
| Kakao OAuth | ❌ | ✅ | **추가** |

### Documentation

| 항목 | HW1 (과제1) | 현재 (Blog API) | 비고 |
|------|-----------|----------------|------|
| SpringDoc OpenAPI | ✅ | ✅ | 동일 |
| Swagger UI | ✅ | ✅ | 동일 |

### Build & Deploy

| 항목 | HW1 (과제1) | 현재 (Blog API) | 비고 |
|------|-----------|----------------|------|
| Gradle | ✅ | ✅ | 동일 |
| Docker | ✅ | ✅ | 동일 |
| Docker Compose | ✅ | ✅ | 동일 |
| GitHub Actions | ❌ | ✅ | **추가** |
| GHCR | ❌ | ✅ | **추가** |

### Frontend

| 항목 | HW1 (과제1) | 현재 (Blog API) | 비고 |
|------|-----------|----------------|------|
| React | ❌ | ✅ (login-app) | **추가** |
| Firebase Auth (Client) | ❌ | ✅ | **추가** |
| Kakao SDK (JS) | ❌ | ✅ | **추가** |

## 보안 아키텍처

### 인증 흐름 (HW1 대비 확장)

#### 1. LOCAL 인증 (HW1과 동일)

```
Client → POST /auth/login (email, password)
      → AuthService 비밀번호 검증 (BCrypt)
      → JwtTokenProvider Access/Refresh Token 발급
      → Redis Refresh Token 저장
      → Client (tokens)
```

#### 2. Firebase 인증 (신규 추가)

```
Client → Firebase Auth (Google Login)
      → Get Firebase ID Token
      → POST /auth/firebase (idToken)
      → AuthService Firebase Admin SDK 토큰 검증
      → User 조회 또는 생성 (provider=GOOGLE)
      → JwtTokenProvider Access/Refresh Token 발급
      → Redis Refresh Token 저장
      → Client (tokens)
```

**Firebase 통합 흐름**:
```
┌─────────┐              ┌─────────────┐              ┌─────────────┐
│ Client  │              │  Firebase   │              │   Server    │
└────┬────┘              └──────┬──────┘              └──────┬──────┘
     │ Google Login            │                             │
     │────────────────────────>│                             │
     │ ID Token                │                             │
     │<────────────────────────│                             │
     │                         │                             │
     │ POST /auth/firebase (idToken)                         │
     │──────────────────────────────────────────────────────>│
     │                         │                             │
     │                         │ Verify Token                │
     │                         │<────────────────────────────│
     │                         │ User Info                   │
     │                         │─────────────────────────────>│
     │                         │                             │
     │                         │                             │
     │ JWT Access/Refresh Tokens                             │
     │<──────────────────────────────────────────────────────│
```

#### 3. Kakao OAuth 인증 (신규 추가)

```
Client → Kakao Login Popup
      → Redirect to /oauth/kakao/callback?code=xxx
      → POST /oauth/kakao (code)
      → AuthService Kakao API 토큰 교환
      → Kakao API 사용자 정보 조회
      → User 조회 또는 생성 (provider=KAKAO)
      → JwtTokenProvider Access/Refresh Token 발급
      → Redis Refresh Token 저장
      → Client (tokens)
```

**Kakao OAuth 흐름**:
```
┌─────────┐         ┌─────────────┐         ┌─────────────┐
│ Client  │         │    Kakao    │         │   Server    │
└────┬────┘         └──────┬──────┘         └──────┬──────┘
     │ Login Popup        │                        │
     │───────────────────>│                        │
     │ Auth Code (redirect)                        │
     │<───────────────────│                        │
     │                    │                        │
     │ POST /oauth/kakao (code)                    │
     │────────────────────────────────────────────>│
     │                    │                        │
     │                    │ Exchange Token         │
     │                    │<───────────────────────│
     │                    │ Access Token           │
     │                    │────────────────────────>│
     │                    │                        │
     │                    │ Get User Info          │
     │                    │<───────────────────────│
     │                    │ User Data              │
     │                    │────────────────────────>│
     │                    │                        │
     │ JWT Tokens                                  │
     │<────────────────────────────────────────────│
```

### JWT 토큰 관리 (HW1과 동일)

| 항목 | 값 |
|------|-----|
| Access Token 만료 | 15분 (900초) |
| Refresh Token 만료 | 14일 (1209600초) |
| 서명 알고리즘 | HS256 |
| 블랙리스트 저장소 | Redis (로그아웃 시) |

### CORS 설정 (HW1 대비 확장)

**HW1**:
```
localhost:3000, localhost:8080, localhost:9090
```

**현재 (Blog API)**:
```
localhost:3000 (React Dev Server)
localhost:8080 (Local Spring Boot)
113.198.66.68 (Production Server)
```

### 레이트 리미팅 (HW1과 동일)

- **대상**: `/auth/login`, `/auth/signup`
- **제한**: IP당 분당 30회
- **저장소**: Redis
- **처리**: 429 Too Many Requests

## 배포 아키텍처

### HW1 (과제1) - 로컬 빌드 방식

```
개발자 PC
   │
   ├── docker-compose up --build
   │    ├── Build Spring Boot JAR (Gradle)
   │    └── Create Docker Image
   │
   └── Run Containers (로컬)
        ├── bookstore-app:9090
        ├── mysql:3306
        └── redis:6379
```

**문제점**:
- 서버에서 빌드 시간 소요 (Gradle + Docker)
- 일관된 빌드 환경 보장 어려움
- 프론트엔드 없음

### 현재 (Blog API) - GHCR CI/CD 방식

```
┌──────────────────────────────────────────────────────────┐
│                   Development Workflow                   │
└──────────────────────────────────────────────────────────┘

개발자 PC
   │
   ├── git commit & push (main or blog branch)
   │
   ▼
┌──────────────────────────────────────────────────────────┐
│              GitHub Actions CI/CD Pipeline               │
├──────────────────────────────────────────────────────────┤
│ 1. Checkout code                                         │
│ 2. Set up Java 21                                        │
│ 3. Install Node.js 20 (React 빌드)                       │
│ 4. Build React (npm install && npm run build)           │
│ 5. Copy React build → src/main/resources/static/        │
│ 6. Build Spring Boot (Gradle bootJar)                   │
│ 7. Build Docker Image (Multi-stage)                     │
│ 8. Push to GHCR (ghcr.io/jc-arl/blog-api)              │
└──────────────────────────────────────────────────────────┘
   │
   │ Image: latest, main-{sha}, v1.0.0 (if tagged)
   ▼
┌──────────────────────────────────────────────────────────┐
│          GitHub Container Registry (GHCR)                │
│     ghcr.io/jc-arl/blog-api:latest                       │
└──────────────────────────────────────────────────────────┘
   │
   │ docker compose pull
   ▼
┌──────────────────────────────────────────────────────────┐
│            Production Server (113.198.66.68)             │
├──────────────────────────────────────────────────────────┤
│ $ docker compose pull                                    │
│ $ docker compose up -d                                   │
│                                                           │
│ ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│ │  blog-api    │  │ blog-mysql   │  │ blog-redis   │   │
│ │  (Port 80)   │  │ (Port 3306)  │  │ (Port 6379)  │   │
│ └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                           │
│ Volumes:                                                  │
│ - mysql_data (영속성)                                     │
│ - redis_data (영속성)                                     │
│ - ./secrets/firebase-service-account.json (마운트)       │
└──────────────────────────────────────────────────────────┘
```

### 배포 환경 비교

| 항목 | HW1 (과제1) | 현재 (Blog API) |
|------|-----------|----------------|
| **빌드 위치** | 서버 (Docker 내부) | GitHub Actions (클라우드) |
| **이미지 저장소** | 없음 (로컬 빌드) | GHCR (ghcr.io/jc-arl/blog-api) |
| **배포 방식** | `docker-compose up --build` | `docker compose pull && up` |
| **포트** | 9090 | 80 (프로덕션) / 8080 (로컬) |
| **프론트엔드** | 없음 | React (login-app) 포함 |
| **Secrets 관리** | 없음 | `secrets/` 디렉토리 분리 |
| **환경 분리** | 없음 | application-{local/prod}.yml |
| **자동화** | 수동 빌드 | GitHub Actions 자동 빌드 |

### Docker Compose 구조

**현재 (Blog API)**:
```yaml
services:
  mysql:
    image: mysql:8.0
    ports: ["3306:3306"]
    volumes: [mysql_data:/var/lib/mysql]
    healthcheck: mysqladmin ping

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]
    volumes: [redis_data:/data]
    healthcheck: redis-cli ping

  app:
    image: ${DOCKER_IMAGE:-ghcr.io/jc-arl/blog-api:latest}
    pull_policy: always  # 항상 최신 이미지 pull
    depends_on:
      mysql: {condition: service_healthy}
      redis: {condition: service_healthy}
    ports: ["${APP_PORT:-8080}:${APP_PORT:-8080}"]
    environment:
      - SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
      - APP_PORT=${APP_PORT:-8080}
      - MYSQL_HOST=mysql
      - REDIS_HOST=redis
      - FIREBASE_PROJECT_ID=${FIREBASE_PROJECT_ID}
      - KAKAO_REST_API_KEY=${KAKAO_REST_API_KEY}
    volumes:
      - ./secrets/firebase-service-account.json:/app/firebase-service-account.json:ro
    healthcheck: wget http://localhost:${APP_PORT}/health

volumes:
  mysql_data:
  redis_data:

networks:
  blog-network:
    driver: bridge
```

**HW1 대비 주요 변경**:
- ✅ 추가: `pull_policy: always` (GHCR에서 항상 최신 이미지)
- ✅ 추가: `volumes: ./secrets/firebase-service-account.json` (Secrets 마운트)
- ✅ 추가: 환경변수 `FIREBASE_PROJECT_ID`, `KAKAO_REST_API_KEY`
- ✅ 변경: 포트 80 (프로덕션) / 8080 (로컬)
- ✅ 변경: `SPRING_PROFILES_ACTIVE` 환경별 분리

### Dockerfile 구조 (Multi-stage Build)

```dockerfile
# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS builder

# Install Node.js 20 for React build
RUN apt-get update && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

WORKDIR /app

# Gradle 의존성 캐싱
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src
COPY login-app ./login-app

# React + Spring Boot 빌드
RUN gradle clean bootJar -x test --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 비root 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring

# JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# Note: Firebase 키는 docker-compose.yml volume 마운트
# secrets/firebase-service-account.json → /app/firebase-service-account.json

RUN chown -R spring:spring /app
USER spring

EXPOSE 8080 80

# JVM 최적화
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**HW1 대비 주요 변경**:
- ✅ 추가: Node.js 20 설치 (React 빌드)
- ✅ 추가: `login-app` 디렉토리 복사
- ✅ 추가: React 빌드 포함 (`gradle bootJar`)
- ✅ 변경: `EXPOSE 80` 추가
- ✅ 제거: Firebase 키 COPY (volume 마운트로 대체)

## 성능 최적화

### HW1과 동일한 최적화

1. **N+1 문제 방지**
   - `@EntityGraph` 사용
   - Fetch Join 쿼리

2. **페이지네이션**
   - Spring Data Pageable
   - 대량 데이터 조회 최적화

3. **Connection Pool**
   - HikariCP (Spring Boot 기본)
   - 최적화된 커넥션 관리

### 추가 최적화 (Blog API)

1. **조회수 증가 최적화**
   - 비관적 락 (`@Lock(PESSIMISTIC_WRITE)`)
   - 또는 비동기 처리 (`@Async`)
   - Redis 카운터 사용 고려

2. **좋아요 집계 최적화**
   - `COUNT(*)` 쿼리 최적화
   - 비정규화 고려 (posts.like_count 컬럼)

3. **Firebase 토큰 검증 캐싱**
   - Redis에 검증된 토큰 캐싱 (TTL)

4. **카테고리 슬러그 조회**
   - Slug 인덱스 활용
   - SEO 친화적 URL

## 모니터링 및 로깅

### HW1과 동일

- **RequestLoggingFilter**: 모든 HTTP 요청/응답 로깅
- **Spring Boot Actuator**: Health 엔드포인트
- **Docker Logs**: `docker compose logs -f app`

### 추가 모니터링 (Blog API)

- **Firebase Admin SDK 로그**: Firebase 인증 실패 추적
- **Kakao OAuth 로그**: Kakao API 호출 추적
- **조회수 증가 로그**: 조회수 증가 로직 모니터링

## 확장 가능성

### HW1 확장 고려사항 (제거됨)
- ❌ 결제 시스템 통합
- ❌ 재고 관리 최적화
- ❌ 주문 알림

### Blog API 확장 고려사항

1. **댓글 계층 구조** (대댓글)
   - comments.parent_id 컬럼 추가
   - 재귀 쿼리 또는 계층형 구조

2. **전문 검색** (Full-Text Search)
   - MySQL Full-Text Index
   - Elasticsearch 통합

3. **이미지 업로드**
   - AWS S3 통합
   - 이미지 최적화 (리사이징, WebP)

4. **실시간 알림**
   - WebSocket 또는 Server-Sent Events (SSE)
   - Redis Pub/Sub

5. **소셜 공유**
   - Open Graph 메타 태그
   - Twitter Card

## 결론

Blog API의 아키텍처는 HW1 (과제1)의 온라인 서점 시스템을 블로그 플랫폼에 맞게 재설계되었습니다. 전자상거래 도메인(도서, 주문, 장바구니)을 제거하고 콘텐츠 중심 도메인(게시글, 댓글, 좋아요)으로 전환하였으며, Firebase와 Kakao OAuth를 통한 소셜 로그인 기능을 추가하였습니다. 또한 GitHub Actions와 GHCR을 활용한 CI/CD 파이프라인을 구축하여 자동화된 배포 환경을 구현하였습니다.
