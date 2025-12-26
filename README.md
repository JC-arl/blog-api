# Blog API

본 문서는 **동아리 블로그 백엔드 API 서버** 구현을 위한 개발 명세서이다.
Spring Boot + MySQL 기반으로 REST API 서버를 구축하며,
인증·인가(JWT + Social Login), Redis, Docker 기반 배포, Swagger 문서화까지 포함한다.

## 목차

- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [API 문서](#api-문서)
- [주요 기능](#주요-기능)
- [프로젝트 구조](#프로젝트-구조)

## 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.9
- **ORM**: Spring Data JPA (Hibernate)
- **Database**: MySQL 8
- **Cache**: Redis
- **Auth**: JWT, OAuth2 (Kakao), Firebase Auth
- **API Docs**: Swagger/OpenAPI 3.0
- **Container**: Docker, docker-compose
- **Migration**: Flyway

## 시작하기

### 1. 환경 변수 설정

`.env.example` 파일을 복사하여 `.env` 파일 생성:

```bash
cp .env.example .env
```

`.env` 파일 수정:
```properties
MYSQL_USER=your_db_user
MYSQL_PASSWORD=your_db_password
MYSQL_DATABASE=blog
MYSQL_PORT=3306

REDIS_PORT=6379

JWT_SECRET=your-secret-key-here
JWT_ACCESS_EXP=900
JWT_REFRESH_EXP=1209600

FIREBASE_PROJECT_ID=your-project-id
FIREBASE_SERVICE_ACCOUNT_PATH=path/to/serviceAccountKey.json

KAKAO_REST_API_KEY=your-kakao-api-key
```

### 2. 데이터베이스 실행

Docker Compose로 MySQL과 Redis 실행:

```bash
docker-compose up -d mysql redis
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
./gradlew build
java -jar build/libs/blog-api-0.0.1-SNAPSHOT.jar
```

## API 문서

### Swagger UI

애플리케이션 실행 후 다음 URL로 접속:

**http://localhost:8080/swagger-ui.html**

### 주요 엔드포인트

#### 인증 (Auth)
- `POST /auth/signup` - 회원가입
- `POST /auth/login` - 로그인
- `POST /auth/refresh` - 토큰 갱신
- `POST /auth/logout` - 로그아웃
- `POST /auth/kakao-login` - 카카오 로그인

#### 게시글 (Posts)
- `GET /posts` - 게시글 목록 조회
- `POST /posts` - 게시글 작성
- `GET /posts/{id}` - 게시글 상세 조회
- `PUT /posts/{id}` - 게시글 수정
- `DELETE /posts/{id}` - 게시글 삭제
- `GET /posts/search` - 게시글 검색
- `GET /posts/my` - 내 게시글 목록

#### 댓글 (Comments)
- `GET /posts/{postId}/comments` - 댓글 목록 조회
- `POST /posts/{postId}/comments` - 댓글 작성
- `PUT /comments/{id}` - 댓글 수정
- `DELETE /comments/{id}` - 댓글 삭제

#### 좋아요 (Likes)
- `POST /posts/{postId}/like` - 좋아요 토글
- `GET /posts/{postId}/likes/count` - 좋아요 수 조회

#### 카테고리 (Categories)
- `GET /categories` - 카테고리 목록
- `POST /categories` - 카테고리 생성 (관리자)
- `PUT /categories/{id}` - 카테고리 수정 (관리자)
- `DELETE /categories/{id}` - 카테고리 삭제 (관리자)

#### 관리자 (Admin)
- `GET /admin/users` - 사용자 목록 조회
- `PUT /admin/users/{id}/suspend` - 사용자 정지
- `GET /admin/stats` - 통계 조회

#### 헬스체크
- `GET /health` - 서버 상태 확인

### 테스트 계정

#### 관리자
- 이메일: `admin@blog.com`
- 비밀번호: `admin1234`

#### 일반 사용자
- 이메일: `user1@blog.com` ~ `user40@blog.com`
- 비밀번호: `user1234`

## 주요 기능

### 1. 인증/인가
- JWT 기반 Access Token / Refresh Token
- Firebase Authentication 연동
- Kakao OAuth2 소셜 로그인
- 역할 기반 접근 제어 (RBAC: ROLE_USER, ROLE_ADMIN)

### 2. 게시글 관리
- 게시글 CRUD
- 페이징, 정렬, 검색
- 카테고리별 조회
- 조회수 증가

### 3. 댓글 및 좋아요
- 계층형 댓글 (대댓글)
- 좋아요 토글 기능
- 중복 좋아요 방지

### 4. 관리자 기능
- 사용자 관리 (정지/활성화)
- 통계 조회 (사용자, 게시글, 댓글 수)
- 게시글 강제 삭제

### 5. 캐싱 및 성능
- Redis 캐시 활용
- Refresh Token 저장
- N+1 문제 해결 (Fetch Join)

## 프로젝트 구조

```
blog-api/
├── src/
│   ├── main/
│   │   ├── java/com/wsd/blogapi/
│   │   │   ├── auth/           # 인증/인가
│   │   │   ├── user/           # 사용자 관리
│   │   │   ├── post/           # 게시글
│   │   │   ├── comment/        # 댓글
│   │   │   ├── like/           # 좋아요
│   │   │   ├── category/       # 카테고리
│   │   │   ├── admin/          # 관리자
│   │   │   ├── security/       # 보안 설정
│   │   │   ├── config/         # 설정
│   │   │   └── health/         # 헬스체크
│   │   └── resources/
│   │       ├── db/migration/   # Flyway 마이그레이션
│   │       ├── application.properties
│   │       ├── application-local.yml
│   │       └── application-prod.yml
│   └── test/                   # 테스트 코드
├── login-app/                  # React 프론트엔드
├── docker-compose.yml          # Docker 설정
├── build.gradle                # Gradle 빌드 설정
├── .env.example                # 환경 변수 예시
├── SWAGGER_GUIDE.md            # Swagger 사용 가이드
└── README.md                   # 본 문서

```

## 추가 문서

- [개발 명세서 (dev.md)](./dev.md)
- [Swagger 사용 가이드 (SWAGGER_GUIDE.md)](./SWAGGER_GUIDE.md)


## 라이센스

MIT License