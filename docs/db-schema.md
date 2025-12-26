# Database Schema 문서 (Blog API)

## 목차
- [ERD 개요](#erd-개요)
- [HW1과의 스키마 차이](#hw1과의-스키마-차이)
- [테이블 상세](#테이블-상세)
- [관계 설명](#관계-설명)
- [인덱스 전략](#인덱스-전략)
- [제약조건](#제약조건)

## ERD 개요

### 엔티티 관계도 (텍스트 형식)

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

## HW1과의 스키마 차이

### HW1 (과제1) - 온라인 서점 (8개 테이블)

```
users
  ├── orders (1:N)
  │     └── order_items (1:N) → books
  ├── reviews (1:N) → books
  ├── favorites (1:N) → books
  └── cart (1:1)
        └── cart_items (1:N) → books
```

**총 8개 테이블**: users, books, orders, order_items, reviews, favorites, carts, cart_items

### 현재 구현 - 블로그 (5개 테이블)

```
users
  ├── posts (1:N)
  │     ├── comments (1:N)
  │     ├── post_likes (N:M via join table)
  │     └── category (N:1)
  └── categories (관리자가 생성)
```

**총 5개 테이블**: users, posts, categories, comments, post_likes

### 스키마 변경 요약

| 변경 유형 | 테이블 | 설명 |
|----------|--------|------|
| **삭제** | books | 도서 정보 (온라인 서점 전용) |
| **삭제** | orders | 주문 정보 (전자상거래 전용) |
| **삭제** | order_items | 주문 상세 (전자상거래 전용) |
| **삭제** | favorites | 찜 기능 (전자상거래 전용) |
| **삭제** | carts | 장바구니 (전자상거래 전용) |
| **삭제** | cart_items | 장바구니 항목 (전자상거래 전용) |
| **추가** | posts | 게시글 (블로그 핵심) |
| **추가** | categories | 카테고리 (독립 테이블) |
| **추가** | comments | 댓글 (블로그 상호작용) |
| **추가** | post_likes | 좋아요 (SNS 패턴) |
| **유지** | users | 사용자 (provider 확장) |

## 테이블 상세

### 1. users (사용자)

| 컬럼명 | 타입 | NULL | 키 | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 사용자 ID |
| email | VARCHAR(255) | NO | UK | - | 이메일 (로그인 ID) |
| password_hash | VARCHAR(255) | YES | - | NULL | 비밀번호 (BCrypt, 소셜 로그인은 NULL) |
| nickname | VARCHAR(50) | NO | - | - | 닉네임 |
| role | VARCHAR(20) | NO | - | 'ROLE_USER' | 권한 (ROLE_USER, ROLE_ADMIN) |
| status | VARCHAR(20) | NO | - | 'ACTIVE' | 계정 상태 (ACTIVE, SUSPENDED) |
| provider | VARCHAR(20) | NO | IDX | 'LOCAL' | 인증 제공자 (LOCAL, GOOGLE, FIREBASE, KAKAO) |
| provider_id | VARCHAR(255) | YES | IDX | NULL | 소셜 로그인 제공자 ID |
| created_at | DATETIME | NO | - | CURRENT_TIMESTAMP | 생성일시 |
| updated_at | DATETIME | NO | - | CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**HW1 대비 변경사항**:
- ✅ 추가: `provider` - 인증 제공자 (HW1: LOCAL only → 현재: LOCAL, GOOGLE, FIREBASE, KAKAO)
- ✅ 추가: `provider_id` - 소셜 로그인 고유 ID
- ✅ 변경: `password` → `password_hash` (명확한 네이밍)
- ✅ 변경: `name` → `nickname` (블로그 특성 반영)
- ✅ 추가: `status` - 계정 상태 (HW1: ACTIVE/INACTIVE → 현재: ACTIVE/SUSPENDED)

**제약조건**:
- UNIQUE: email
- CHECK: role IN ('ROLE_USER', 'ROLE_ADMIN')
- CHECK: status IN ('ACTIVE', 'SUSPENDED')
- CHECK: provider IN ('LOCAL', 'GOOGLE', 'FIREBASE', 'KAKAO')

**인덱스**:
- PRIMARY KEY (id)
- UNIQUE INDEX (email)
- INDEX (provider, provider_id) - 소셜 로그인 조회 최적화

### 2. posts (게시글)

| 컬럼명 | 타입 | NULL | 키 | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 게시글 ID |
| title | VARCHAR(200) | NO | - | - | 제목 |
| content | TEXT | NO | - | - | 내용 |
| author_id | BIGINT | NO | FK, IDX | - | 작성자 ID |
| category_id | BIGINT | YES | FK, IDX | NULL | 카테고리 ID |
| view_count | INT | NO | - | 0 | 조회수 |
| status | VARCHAR(20) | NO | IDX | 'PUBLISHED' | 게시글 상태 |
| created_at | DATETIME | NO | IDX | - | 작성일시 |
| updated_at | DATETIME | NO | - | - | 수정일시 |

**HW1 books 테이블과 비교**:
- HW1 books: title, author, publisher, isbn, category(VARCHAR), price, stock_quantity, description, published_at
- 현재 posts: title, content, author_id(FK), category_id(FK), view_count, status

**제약조건**:
- FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
- FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
- CHECK: status IN ('PUBLISHED', 'DRAFT', 'ARCHIVED')
- CHECK: view_count >= 0

**인덱스**:
- PRIMARY KEY (id)
- INDEX (author_id) - 작성자별 게시글 조회
- INDEX (category_id) - 카테고리별 필터링
- INDEX (status) - 상태별 필터링
- INDEX (created_at) - 최신순 정렬

**HW1 대비 주요 차이**:
- ❌ 제거: isbn, publisher, price, stock_quantity (전자상거래 전용)
- ✅ 추가: view_count - 조회수 추적 (블로그 핵심 지표)
- ✅ 추가: status - DRAFT(임시저장) 기능
- ✅ 변경: category - VARCHAR → FK (독립 테이블)

### 3. categories (카테고리)

| 컬럼명 | 타입 | NULL | 키 | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 카테고리 ID |
| name | VARCHAR(50) | NO | UK | - | 카테고리 이름 |
| slug | VARCHAR(100) | NO | UK, IDX | - | URL 친화적 슬러그 |
| description | VARCHAR(200) | YES | - | NULL | 카테고리 설명 |
| created_at | DATETIME | NO | - | - | 생성일시 |
| updated_at | DATETIME | NO | - | - | 수정일시 |

**HW1 대비 변경**:
- HW1: books.category VARCHAR(50) - 고정값 (PROGRAMMING, AI, DATABASE 등)
- 현재: 독립 테이블로 관리, 동적 생성/수정/삭제 가능

**제약조건**:
- UNIQUE: name
- UNIQUE: slug

**인덱스**:
- PRIMARY KEY (id)
- UNIQUE INDEX (name)
- UNIQUE INDEX (slug) - URL 라우팅 최적화

**추가된 이유**:
- 카테고리 동적 관리
- SEO 친화적 URL (slug)
- 카테고리별 메타데이터 (description)

### 4. comments (댓글)

| 컬럼명 | 타입 | NULL | 키 | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 댓글 ID |
| content | TEXT | NO | - | - | 댓글 내용 |
| post_id | BIGINT | NO | FK, IDX | - | 게시글 ID |
| author_id | BIGINT | NO | FK, IDX | - | 작성자 ID |
| status | VARCHAR(20) | NO | IDX | 'ACTIVE' | 댓글 상태 |
| created_at | DATETIME | NO | IDX | - | 작성일시 |
| updated_at | DATETIME | NO | - | - | 수정일시 |

**HW1 reviews 테이블과 비교**:
- HW1 reviews: book_id, user_id, rating (1-5), content
  - 한 사용자당 도서 1개 리뷰 제한 (UK: user_id, book_id)
- 현재 comments: post_id, author_id, content
  - 제한 없음 (여러 댓글 가능)
  - rating 없음 (좋아요로 대체)

**제약조건**:
- FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
- FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
- CHECK: status IN ('ACTIVE', 'DELETED', 'HIDDEN')

**인덱스**:
- PRIMARY KEY (id)
- INDEX (post_id) - 게시글별 댓글 조회
- INDEX (author_id) - 사용자별 댓글 조회
- INDEX (status) - 상태별 필터링
- INDEX (created_at) - 최신순 정렬

**HW1 대비 주요 차이**:
- ❌ 제거: rating (1-5 평점) → 좋아요 시스템으로 대체
- ❌ 제거: UNIQUE(user_id, book_id) → 자유로운 댓글 작성
- ✅ 추가: status - 댓글 상태 관리 (삭제/숨김)

### 5. post_likes (게시글 좋아요)

| 컬럼명 | 타입 | NULL | 키 | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 좋아요 ID |
| post_id | BIGINT | NO | FK, IDX | - | 게시글 ID |
| user_id | BIGINT | NO | FK, IDX | - | 사용자 ID |
| created_at | DATETIME | NO | IDX | - | 생성일시 |

**HW1 favorites 테이블과 비교**:
- HW1 favorites: user_id, book_id (찜 기능, 장바구니 전 단계)
- 현재 post_likes: post_id, user_id (좋아요, SNS 패턴)

**제약조건**:
- FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
- UNIQUE KEY (post_id, user_id) - 중복 좋아요 방지

**인덱스**:
- PRIMARY KEY (id)
- UNIQUE INDEX (post_id, user_id) - 중복 방지
- INDEX (post_id) - 게시글별 좋아요 수 집계
- INDEX (user_id) - 사용자별 좋아요 목록
- INDEX (created_at) - 최근 좋아요 추적

**HW1 대비 주요 차이**:
- 유사점: 중복 방지 (UNIQUE), N:M 관계
- 차이점: 목적 (찜/저장 → 선호도 표시)

## 관계 설명

### 1. users ↔ posts (1:N)
- 한 사용자는 여러 게시글을 작성할 수 있음
- 사용자 삭제 시 게시글도 삭제 (CASCADE)

**HW1 비교**: users ↔ orders (사용자 삭제 시 주문 유지)

### 2. users ↔ comments (1:N)
- 한 사용자는 여러 댓글을 작성할 수 있음
- 사용자 삭제 시 댓글도 삭제 (CASCADE)

**HW1 비교**: users ↔ reviews (동일 패턴)

### 3. posts ↔ comments (1:N)
- 한 게시글은 여러 댓글을 가질 수 있음
- 게시글 삭제 시 댓글도 삭제 (CASCADE)

**HW1 비교**: books ↔ reviews (동일 패턴)

### 4. posts ↔ post_likes ↔ users (N:M)
- 한 게시글은 여러 사용자에게 좋아요를 받을 수 있음
- 한 사용자는 여러 게시글에 좋아요를 누를 수 있음
- 중복 좋아요 방지 (UNIQUE: post_id, user_id)

**HW1 비교**: books ↔ favorites ↔ users (구조 동일, 목적 다름)

### 5. categories ↔ posts (1:N)
- 한 카테고리는 여러 게시글을 가질 수 있음
- 카테고리 삭제 시 게시글의 category_id는 NULL로 설정 (SET NULL)

**HW1 비교**: books.category VARCHAR (고정값) → 현재: 독립 테이블

## 인덱스 전략

### Primary Key Indexes
모든 테이블의 `id` 컬럼에 자동 생성

### Foreign Key Indexes
- `posts.author_id` (users)
- `posts.category_id` (categories)
- `comments.post_id` (posts)
- `comments.author_id` (users)
- `post_likes.post_id` (posts)
- `post_likes.user_id` (users)

### Unique Indexes
- `users.email` - 로그인 ID 중복 방지
- `categories.name` - 카테고리 이름 중복 방지
- `categories.slug` - URL 슬러그 중복 방지
- `post_likes(post_id, user_id)` - 중복 좋아요 방지

### Composite Indexes
- `users(provider, provider_id)` - 소셜 로그인 조회 최적화
- `post_likes(post_id, user_id)` - 좋아요 여부 확인 최적화

### Performance Indexes
- `posts.status` - 게시글 상태 필터링
- `posts.created_at` - 최신순 정렬
- `comments.created_at` - 댓글 정렬
- `categories.slug` - URL 라우팅
- `post_likes.created_at` - 최근 좋아요 추적

### HW1 대비 인덱스 전략 변경
- ❌ 제거: books.title, books.author (검색 인덱스)
- ❌ 제거: orders.status, orders.created_at
- ✅ 추가: users(provider, provider_id) - 소셜 로그인
- ✅ 추가: posts.view_count - 인기 게시글 정렬 (향후 추가 가능)
- ✅ 추가: categories.slug - SEO 최적화

## 제약조건

### CHECK 제약조건

```sql
-- users
CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
CHECK (provider IN ('LOCAL', 'GOOGLE', 'FIREBASE', 'KAKAO'))
CHECK (status IN ('ACTIVE', 'SUSPENDED'))

-- posts
CHECK (status IN ('PUBLISHED', 'DRAFT', 'ARCHIVED'))
CHECK (view_count >= 0)

-- comments
CHECK (status IN ('ACTIVE', 'DELETED', 'HIDDEN'))
```

**HW1 대비 변경**:
- ❌ 제거: books (price >= 0, stock_quantity >= 0)
- ❌ 제거: orders (total_amount >= 0)
- ❌ 제거: reviews (rating BETWEEN 1 AND 5)
- ✅ 추가: posts.status (PUBLISHED, DRAFT, ARCHIVED)
- ✅ 추가: users.provider (4가지 타입)

### CASCADE 규칙

**ON DELETE CASCADE**:
- `posts` → `users`
- `comments` → `users`, `posts`
- `post_likes` → `users`, `posts`

**ON DELETE SET NULL**:
- `posts.category_id` → `categories`

**HW1 비교**:
- HW1: orders → users (RESTRICT, 주문 기록 보존)
- 현재: posts → users (CASCADE, 사용자 완전 삭제)

## 마이그레이션 관리

### Flyway 마이그레이션 파일

1. **V1__init_users.sql**
   - users 테이블 생성
   - 인덱스 생성

2. **V2__seed_users.sql**
   - 초기 사용자 데이터 (admin@example.com)

3. **V3__create_posts_table.sql**
   - posts 테이블 생성

4. **V4__create_categories_table.sql**
   - categories 테이블 생성
   - posts.category_id 컬럼 추가

5. **V5__create_comments_table.sql**
   - comments 테이블 생성

6. **V6__create_post_likes_table.sql**
   - post_likes 테이블 생성

7. **V7__seed_data.sql**
   - 샘플 게시글, 카테고리 데이터

8. **V8__seed_comments_likes.sql**
   - 샘플 댓글, 좋아요 데이터

### HW1 마이그레이션과 비교
- HW1: 2개 파일 (V1__baseline.sql, V2__seed_data.sql)
- 현재: 8개 파일 (점진적 스키마 변경)

## 데이터 무결성

### HW1과 동일한 무결성
1. **참조 무결성**: 외래키 제약조건으로 보장
2. **도메인 무결성**: CHECK 제약조건으로 보장
3. **엔티티 무결성**: PRIMARY KEY로 보장
4. **사용자 정의 무결성**: UNIQUE 제약조건으로 보장

### 추가된 무결성
- 소셜 로그인 제공자 검증 (provider + provider_id)
- 게시글 상태 관리 (PUBLISHED, DRAFT, ARCHIVED)
- 댓글 상태 관리 (ACTIVE, DELETED, HIDDEN)

## N+1 문제 해결

### HW1과 동일한 해결 방법
1. **@EntityGraph** 사용
2. **Fetch Join** 쿼리
3. **DTO Projection** 활용
4. **Lazy Loading** 전략

### 추가 최적화
- 게시글 목록 조회 시 author, category JOIN
- 댓글 목록 조회 시 author JOIN
- 좋아요 수 집계 쿼리 최적화

## 확장 고려사항

### HW1 대비 추가 고려사항
1. **파티셔닝**: posts 테이블 날짜별 파티셔닝 (대용량 게시글)
2. **Full-Text Search**: posts.content 전문 검색 인덱스
3. **조회수 성능**: Redis 카운터 사용 (실시간 집계)
4. **좋아요 집계**: 비정규화 (posts.like_count 컬럼 추가)
5. **댓글 계층 구조**: parent_id 추가 (대댓글 기능)

### HW1 확장 고려사항 (제거됨)
- ❌ 주문 파티셔닝
- ❌ 재고 관리 최적화
- ❌ 결제 시스템 통합

## DDL (Data Definition Language)

### 전체 스키마 생성 SQL

```sql
-- 1. users 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    nickname VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'ROLE_USER, ROLE_ADMIN',
    status VARCHAR(20) NOT NULL COMMENT 'ACTIVE, SUSPENDED',
    provider VARCHAR(20) NOT NULL COMMENT 'LOCAL, GOOGLE, FIREBASE, KAKAO',
    provider_id VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_provider (provider, provider_id),
    CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN')),
    CHECK (provider IN ('LOCAL', 'GOOGLE', 'FIREBASE', 'KAKAO')),
    CHECK (status IN ('ACTIVE', 'SUSPENDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. categories 테이블
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. posts 테이블
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    category_id BIGINT,
    view_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_author_id (author_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    CHECK (status IN ('PUBLISHED', 'DRAFT', 'ARCHIVED')),
    CHECK (view_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. comments 테이블
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_author_id (author_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    CHECK (status IN ('ACTIVE', 'DELETED', 'HIDDEN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. post_likes 테이블
CREATE TABLE post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_post_user (post_id, user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## 테이블 통계

| 테이블 | HW1 | 현재 | 변화 |
|--------|-----|------|------|
| 사용자 | users | users | 유지 (확장) |
| 콘텐츠 | books | posts | 대체 |
| 카테고리 | books.category | categories | 독립 테이블화 |
| 상호작용 | reviews | comments + post_likes | 분리 |
| 전자상거래 | orders, order_items, carts, cart_items, favorites | - | 제거 |
| **총계** | **8개** | **5개** | **-3개** |

## 결론

Blog API의 데이터베이스 스키마는 HW1의 온라인 서점 시스템을 블로그 플랫폼에 맞게 재설계되었습니다. 전자상거래 관련 테이블(주문, 장바구니)을 제거하고, 콘텐츠 중심 테이블(게시글, 댓글, 좋아요)을 추가하였습니다. 또한 소셜 로그인 지원을 위해 users 테이블에 provider 관련 컬럼을 추가하여 다양한 인증 방식을 지원합니다.
