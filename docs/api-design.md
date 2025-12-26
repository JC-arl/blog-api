# API 설계 문서 (Blog API)

## 목차
- [개요](#개요)
- [HW1 (과제1)과의 주요 차이점](#hw1-과제1과의-주요-차이점)
- [엔드포인트 목록](#엔드포인트-목록)
- [인증/인가](#인증인가)
- [요청/응답 형식](#요청응답-형식)
- [에러 처리](#에러-처리)

## 개요

본 API는 블로그 플랫폼을 위한 RESTful API입니다.

- **Base URL**: `http://{SERVER_URL}:{PORT}`
- **인증 방식**: JWT Bearer Token + Firebase Auth + Kakao OAuth
- **응답 형식**: JSON
- **문자 인코딩**: UTF-8

## HW1 (과제1)과의 주요 차이점

### 1. 도메인 변경: 온라인 서점 → 블로그 플랫폼

| 항목 | HW1 (온라인 서점) | 현재 구현 (블로그) |
|------|------------------|-------------------|
| **핵심 엔티티** | Books, Orders, Cart | Posts, Comments, Categories |
| **주요 기능** | 도서 판매, 주문, 장바구니 | 게시글 작성, 댓글, 좋아요 |
| **사용자 활동** | 구매, 리뷰, 찜 | 글쓰기, 댓글, 좋아요 |

### 2. 데이터베이스 스키마 차이

#### HW1 (과제1) - 8개 테이블
```
users, books, orders, order_items, reviews, favorites, carts, cart_items
```

#### 현재 구현 - 5개 테이블
```
users, posts, categories, comments, post_likes
```

**삭제된 테이블**: `books`, `orders`, `order_items`, `favorites`, `carts`, `cart_items`
**추가된 테이블**: `posts`, `categories`, `comments`, `post_likes`

### 3. 인증/인가 시스템 차이

| 기능 | HW1 (과제1) | 현재 구현 (블로그) |
|------|------------|-------------------|
| **인증 방식** | JWT only | JWT + Firebase + Kakao OAuth |
| **Provider** | LOCAL only | LOCAL, GOOGLE, FIREBASE, KAKAO |
| **비밀번호 해싱** | BCrypt | BCrypt |
| **토큰 관리** | Redis (Refresh Token) | Redis (Refresh Token + Firebase Token) |
| **소셜 로그인** | 없음 | Google (Firebase), Kakao |

### 4. API 엔드포인트 수

| | HW1 (과제1) | 현재 구현 (블로그) | 변화 |
|---|------------|-------------------|------|
| **인증 API** | 5개 | 9개 | +4 (Firebase, Kakao, Profile) |
| **사용자 API** | 5개 | 0개 | -5 (Auth로 통합) |
| **관리자 API** | 12개 | 7개 | -5 (관리 기능 축소) |
| **콘텐츠 API** | 14개 (Books, Reviews) | 17개 (Posts, Comments) | +3 |
| **부가 기능 API** | 7개 (Cart, Favorites) | 11개 (Likes, Categories) | +4 |
| **헬스체크** | 1개 | 1개 | - |
| **총계** | **44개** | **45개** | **+1개** |

### 5. 주요 기능 변경사항

#### 제거된 기능 (HW1 → 현재)
- 도서 관리 (Books CRUD)
- 주문 관리 (Orders, Order Items)
- 장바구니 (Cart, Cart Items)
- 찜 (Favorites)
- 재고 관리 (Stock Management)

#### 추가된 기능 (HW1 → 현재)
- 게시글 관리 (Posts CRUD)
- 댓글 시스템 (Comments)
- 게시글 좋아요 (Post Likes)
- 카테고리 관리 (Categories)
- 조회수 추적 (View Count)
- Firebase 인증 통합
- Kakao OAuth 로그인
- 소셜 로그인 콜백 처리

### 6. 비즈니스 로직 차이

#### HW1 (과제1) - 전자상거래 중심
- 재고 관리 및 검증
- 주문 생성 시 재고 차감
- 주문 상태 관리 (PENDING, SHIPPED, DELIVERED, CANCELED)
- 장바구니 → 주문 변환 로직
- 가격 계산 (unit_price, line_total, total_amount)

#### 현재 구현 - 콘텐츠 플랫폼 중심
- 게시글 작성 및 수정
- 조회수 증가 로직
- 댓글 작성 및 삭제
- 좋아요 토글 (추가/취소)
- 게시글 상태 관리 (PUBLISHED, DRAFT, ARCHIVED)
- 카테고리별 필터링

## 엔드포인트 목록

### 1. 인증 (Auth)

| HTTP | Path | Description | Auth |
|------|------|-------------|------|
| POST | /auth/signup | 회원가입 (LOCAL) | 불필요 |
| POST | /auth/login | 로그인 (LOCAL) | 불필요 |
| POST | /auth/refresh | 토큰 갱신 | 불필요 |
| POST | /auth/logout | 로그아웃 | 필요 |
| POST | /auth/firebase | Firebase 인증 | 불필요 |
| GET | /auth/me | 내 정보 조회 | 필요 |
| PATCH | /auth/me | 프로필 수정 | 필요 |
| GET | /oauth/kakao/callback | Kakao 콜백 | 불필요 |
| POST | /oauth/kakao | Kakao 토큰 교환 | 불필요 |

**HW1 대비 변경**:
- ✅ 추가: `/auth/firebase` (Google 소셜 로그인)
- ✅ 추가: `/oauth/kakao/*` (Kakao 소셜 로그인)
- ✅ 추가: `/auth/me` (GET, PATCH - 프로필 관리)
- ❌ 제거: `/auth/test-bcrypt` (개발용)

### 2. 게시글 (Posts)

| HTTP | Path | Description | Auth | Role |
|------|------|-------------|------|------|
| GET | /posts | 게시글 목록 조회 (페이지네이션) | 불필요 | - |
| GET | /posts/{id} | 게시글 상세 조회 | 불필요 | - |
| POST | /posts | 게시글 작성 | 필요 | USER |
| PUT | /posts/{id} | 게시글 수정 | 필요 | OWNER |
| DELETE | /posts/{id} | 게시글 삭제 | 필요 | OWNER/ADMIN |
| GET | /posts/user/{userId} | 특정 사용자 게시글 목록 | 불필요 | - |
| GET | /posts/category/{categoryId} | 카테고리별 게시글 목록 | 불필요 | - |
| GET | /posts/search/{keyword} | 키워드 검색 | 불필요 | - |
| PATCH | /posts/{id}/status | 게시글 상태 변경 | 필요 | OWNER/ADMIN |

**검색 파라미터**:
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기 (기본 10)
- `sort`: 정렬 (createdAt,DESC / viewCount,DESC)
- `categoryId`: 카테고리 필터
- `keyword`: 제목/내용 검색

**HW1 Books API와 비교**:
- HW1: 도서 조회 (title, author, publisher, isbn, category, price, stock)
- 현재: 게시글 조회 (title, content, author, category, viewCount)

### 3. 댓글 (Comments)

| HTTP | Path | Description | Auth | Role |
|------|------|-------------|------|------|
| GET | /comments | 전체 댓글 목록 (관리자용) | 필요 | ADMIN |
| GET | /comments/{id} | 댓글 상세 조회 | 불필요 | - |
| GET | /posts/{postId}/comments | 게시글의 댓글 목록 | 불필요 | - |
| GET | /comments/user/{userId} | 특정 사용자 댓글 목록 | 불필요 | - |
| POST | /posts/{postId}/comments | 댓글 작성 | 필요 | USER |
| POST | /comments/{parentId}/replies | 대댓글 작성 | 필요 | USER |
| PUT | /comments/{id} | 댓글 수정 | 필요 | OWNER |
| DELETE | /comments/{id} | 댓글 삭제 | 필요 | OWNER/ADMIN |

**HW1 Reviews API와 비교**:
- HW1: 리뷰 (book_id, user_id, rating 1-5, content) - 도서당 1개 제한
- 현재: 댓글 (post_id, author_id, content) - 제한 없음, rating 없음

### 4. 좋아요 (Likes)

| HTTP | Path | Description | Auth | Role |
|------|------|-------------|------|------|
| POST | /posts/{postId}/likes | 좋아요 추가 | 필요 | USER |
| DELETE | /posts/{postId}/likes | 좋아요 취소 | 필요 | USER |
| POST | /posts/{postId}/likes/toggle | 좋아요 토글 | 필요 | USER |
| GET | /posts/{postId}/likes/count | 좋아요 수 조회 | 불필요 | - |
| GET | /posts/{postId}/likes/check | 내 좋아요 여부 확인 | 필요 | USER |

**HW1 Favorites와 비교**:
- HW1: 찜 (도서를 저장, 장바구니 추가 전 단계)
- 현재: 좋아요 (게시글 선호도 표시, SNS 패턴)

### 5. 카테고리 (Categories)

| HTTP | Path | Description | Auth | Role |
|------|------|-------------|------|------|
| GET | /categories | 카테고리 목록 조회 | 불필요 | - |
| GET | /categories/{id} | 카테고리 상세 조회 | 불필요 | - |
| POST | /categories | 카테고리 생성 | 필요 | ADMIN |
| PUT | /categories/{id} | 카테고리 수정 | 필요 | ADMIN |
| DELETE | /categories/{id} | 카테고리 삭제 | 필요 | ADMIN |
| GET | /categories/{id}/posts | 카테고리별 게시글 목록 | 불필요 | - |

**HW1과 비교**:
- HW1: 카테고리가 books 테이블의 VARCHAR 컬럼 (고정값)
- 현재: 독립 테이블로 관리, 동적 생성/수정/삭제 가능

### 6. 관리자 (Admin)

| HTTP | Path | Description | Auth | Role |
|------|------|-------------|------|------|
| GET | /admin/users | 사용자 목록 조회 | 필요 | ADMIN |
| GET | /admin/users/{id} | 사용자 상세 조회 | 필요 | ADMIN |
| PATCH | /admin/users/{id}/status | 사용자 상태 변경 | 필요 | ADMIN |
| GET | /admin/posts | 전체 게시글 관리 | 필요 | ADMIN |
| DELETE | /admin/posts/{id} | 게시글 강제 삭제 | 필요 | ADMIN |
| GET | /admin/comments | 전체 댓글 관리 | 필요 | ADMIN |
| DELETE | /admin/comments/{id} | 댓글 강제 삭제 | 필요 | ADMIN |

**HW1 대비 변경**:
- ❌ 제거: 도서 관리 (Books CRUD)
- ❌ 제거: 주문 관리 (Orders 상태 변경)
- ❌ 제거: 사용자 활성화/비활성화
- ✅ 단순화: 기본 관리 기능만 유지

### 7. 헬스체크

| HTTP | Path | Description | Auth |
|------|------|-------------|------|
| GET | /health | 서버 상태 확인 | 불필요 |

**총 엔드포인트 수: 45개** (HW1: 44개, +1개)

## 인증/인가

### JWT 기반 인증 (HW1과 동일)

1. **로그인**: `POST /auth/login`
   - 이메일/비밀번호로 로그인
   - 성공 시 Access Token, Refresh Token 반환

2. **인증 헤더 형식**:
   ```
   Authorization: Bearer {access_token}
   ```

3. **토큰 갱신**: `POST /auth/refresh`
   - Refresh Token으로 새로운 Access Token 발급

4. **로그아웃**: `POST /auth/logout`
   - 토큰 블랙리스트 등록 (Redis)

### Firebase 인증 (신규 추가)

1. **Firebase 로그인**: `POST /auth/firebase`
   - Google 계정으로 로그인
   - Firebase ID Token 전송
   - 백엔드에서 Firebase Admin SDK로 토큰 검증
   - JWT Access/Refresh Token 발급

**흐름**:
```
Client → Firebase Auth (Google Login)
      → Get Firebase ID Token
      → POST /auth/firebase (idToken)
      → Server validates with Firebase Admin SDK
      → Issue JWT tokens
```

### Kakao OAuth 인증 (신규 추가)

1. **Kakao 로그인**: `GET /oauth/kakao/callback`
   - Kakao OAuth 인가 코드 받기

2. **토큰 교환**: `POST /oauth/kakao`
   - 인가 코드로 Access Token 교환
   - Kakao API로 사용자 정보 조회
   - JWT Access/Refresh Token 발급

**흐름**:
```
Client → Kakao Login Popup
      → Redirect to /oauth/kakao/callback?code=xxx
      → POST /oauth/kakao (code)
      → Server exchanges code for Kakao access token
      → Fetch user info from Kakao API
      → Issue JWT tokens
```

### 역할 기반 접근 제어 (RBAC) - HW1과 동일

- **ROLE_USER**: 일반 사용자
  - 게시글 작성, 댓글, 좋아요

- **ROLE_ADMIN**: 관리자
  - 모든 USER 권한 + 게시글/댓글/사용자 관리

### Provider 타입 (HW1과 차이)

- **HW1**: `LOCAL` only
- **현재**: `LOCAL`, `GOOGLE` (Firebase), `KAKAO`

## 요청/응답 형식

### 성공 응답 (HW1과 유사)

```json
{
  "success": true,
  "message": "요청이 성공했습니다",
  "data": {
    // 응답 데이터
  }
}
```

### 페이지네이션 응답 (HW1과 동일)

```json
{
  "success": true,
  "message": "조회 성공",
  "data": {
    "content": [ /* 데이터 배열 */ ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 50,
    "totalPages": 5,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

### 게시글 응답 예시

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Spring Boot 학습 가이드",
    "content": "Spring Boot를 배우는 방법...",
    "authorId": 5,
    "authorNickname": "개발자홍길동",
    "categoryId": 3,
    "categoryName": "프로그래밍",
    "viewCount": 152,
    "likeCount": 23,
    "commentCount": 8,
    "status": "PUBLISHED",
    "createdAt": "2025-12-20T10:30:00",
    "updatedAt": "2025-12-21T15:45:00"
  }
}
```

## 에러 처리

### 에러 응답 형식 (HW1과 동일)

```json
{
  "timestamp": "2025-12-26T12:34:56Z",
  "path": "/posts/999",
  "status": 404,
  "error": "Not Found",
  "message": "게시글을 찾을 수 없습니다"
}
```

### 표준 에러 코드 (HW1과 유사)

| HTTP | Code | Description |
|------|------|-------------|
| 400 | BAD_REQUEST | 잘못된 요청 |
| 400 | VALIDATION_FAILED | 입력값 검증 실패 |
| 401 | UNAUTHORIZED | 인증 필요 |
| 401 | TOKEN_EXPIRED | 토큰 만료 |
| 401 | INVALID_FIREBASE_TOKEN | Firebase 토큰 오류 |
| 401 | KAKAO_AUTH_FAILED | Kakao 인증 실패 |
| 403 | FORBIDDEN | 접근 권한 없음 |
| 404 | NOT_FOUND | 리소스 없음 |
| 404 | POST_NOT_FOUND | 게시글 없음 |
| 404 | COMMENT_NOT_FOUND | 댓글 없음 |
| 409 | DUPLICATE_RESOURCE | 중복 리소스 |
| 409 | ALREADY_LIKED | 이미 좋아요한 게시글 |
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류 |

## API 버전 및 변경 이력

### v1.0.0 (2025-12-13) - HW1 (과제1)
- 온라인 서점 시스템
- 44개 엔드포인트
- JWT 인증 (LOCAL only)
- 도서, 주문, 리뷰, 찜, 장바구니

### v2.0.0 (2025-12-26) - 현재 구현
- 블로그 플랫폼으로 도메인 변경
- 33개 엔드포인트 (-11개)
- JWT + Firebase + Kakao OAuth 인증
- 게시글, 댓글, 좋아요, 카테고리
- 소셜 로그인 통합
- 조회수 추적 기능 추가

## 성능 최적화

### HW1과 동일한 기능
- 페이지네이션 (Spring Data Pageable)
- 인덱스 최적화 (Flyway DDL)
- N+1 문제 방지 (@EntityGraph, Fetch Join)
- Connection Pool (HikariCP)

### 추가된 최적화
- Redis 캐싱 확장 (Firebase Token, Kakao Token)
- 조회수 증가 로직 최적화 (비동기 또는 배치)

## 배포 환경 차이

| 항목 | HW1 (과제1) | 현재 구현 (블로그) |
|------|-----------|-------------------|
| **포트** | 9090 | 80 (프로덕션) / 8080 (로컬) |
| **Docker 이미지** | 로컬 빌드 | GHCR (jc-arl/blog-api) |
| **배포 방식** | docker-compose up --build | GitHub Actions → GHCR → Pull |
| **서버** | 로컬 전용 | 프로덕션 서버 (113.198.66.68) |
| **프론트엔드** | 없음 | React (login-app) 포함 |
| **외부 서비스** | 없음 | Firebase, Kakao |

## 보안 강화 사항

### HW1 대비 추가된 보안 기능
1. **Firebase 인증**: Google 계정 기반 인증
2. **Kakao OAuth**: Kakao 계정 기반 인증
3. **Provider 검증**: 인증 제공자별 토큰 검증
4. **Secrets 관리**: Firebase 서비스 계정 키 분리 (secrets/)
5. **CORS 설정**: 프로덕션 도메인 화이트리스트

## 결론

Blog API는 HW1 (과제1)의 온라인 서점 시스템을 기반으로 블로그 플랫폼으로 재설계되었습니다. 전자상거래 기능(주문, 장바구니)을 제거하고 콘텐츠 중심 기능(게시글, 댓글, 좋아요)으로 전환하였으며, Firebase와 Kakao OAuth를 통한 소셜 로그인 기능을 추가하여 사용자 편의성을 향상시켰습니다.
