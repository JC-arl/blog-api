# Blog API - Postman Endpoints Reference

> 실제 컨트롤러 코드를 기반으로 작성된 엔드포인트 문서입니다.

## 목차
- [1. 인증/인가 (Auth)](#1-인증인가-auth)
- [2. 게시글 (Posts)](#2-게시글-posts)
- [3. 댓글 (Comments)](#3-댓글-comments)
- [4. 좋아요 (Likes)](#4-좋아요-likes)
- [5. 카테고리 (Categories)](#5-카테고리-categories)
- [6. 관리자 (Admin)](#6-관리자-admin)
- [7. 헬스체크 (Health)](#7-헬스체크-health)

---

## Postman 환경변수 설정 추천

```
baseUrl = http://localhost:8080
accessToken = (로그인 후 받은 토큰)
refreshToken = (로그인 후 받은 리프레시 토큰)
postId = 1
commentId = 1
categoryId = 1
userId = 1
```

---

## 1. 인증/인가 (Auth)

기본 경로: `/auth`

### 1.1 회원가입
- **Method**: `POST`
- **URL**: `{{baseUrl}}/auth/signup`
- **권한**: 공개
- **설명**: 이메일과 비밀번호로 새로운 계정을 생성하며, 자동으로 JWT 토큰이 발급됩니다.
- **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "닉네임"
}
```
- **Response**: `201 Created`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 1.2 로그인
- **Method**: `POST`
- **URL**: `{{baseUrl}}/auth/login`
- **권한**: 공개
- **설명**: 이메일과 비밀번호로 로그인하여 Access Token과 Refresh Token을 발급받습니다.
- **Request Body**:
```json
{
  "email": "admin@blog.com",
  "password": "admin1234"
}
```
- **Response**: `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
- **Postman Tests 스크립트** (토큰 자동 저장):
```javascript
const json = pm.response.json();
pm.environment.set("accessToken", json.accessToken);
pm.environment.set("refreshToken", json.refreshToken);
```

### 1.3 토큰 갱신
- **Method**: `POST`
- **URL**: `{{baseUrl}}/auth/refresh`
- **권한**: 공개
- **설명**: Refresh Token으로 새로운 Access Token을 발급받습니다.
- **Request Body**:
```json
{
  "refreshToken": "{{refreshToken}}"
}
```
- **Response**: `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 1.4 로그아웃
- **Method**: `POST`
- **URL**: `{{baseUrl}}/auth/logout`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **설명**: 현재 로그인된 사용자를 로그아웃하며, Refresh Token이 무효화됩니다.
- **Response**: `200 OK`

### 1.5 카카오 로그인
- **Method**: `POST`
- **URL**: `{{baseUrl}}/auth/kakao-login`
- **권한**: 공개
- **설명**: 카카오 Access Token으로 로그인하고 Firebase Custom Token을 발급받습니다.
- **Request Body**:
```json
{
  "kakaoAccessToken": "kakao_access_token_here"
}
```
- **Response**: `200 OK`
```json
{
  "firebaseCustomToken": "custom_token_here"
}
```

---

## 2. 게시글 (Posts)

기본 경로: `/posts`

### 2.1 게시글 생성
- **Method**: `POST`
- **URL**: `{{baseUrl}}/posts`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Request Body**:
```json
{
  "title": "게시글 제목",
  "content": "게시글 내용",
  "categoryId": 1
}
```
- **Response**: `201 Created`
- **Postman Tests 스크립트** (postId 저장):
```javascript
const json = pm.response.json();
pm.environment.set("postId", json.id);
```

### 2.2 게시글 목록 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/posts?page=0&size=20&sort=createdAt,desc`
- **권한**: 공개
- **설명**: 공개된 게시글 목록을 페이징하여 조회합니다.
- **Query Parameters**:
  - `page`: 페이지 번호 (default: 0)
  - `size`: 페이지 크기 (default: 20)
  - `sort`: 정렬 기준 (default: createdAt,desc)
- **Response**: `200 OK` (Page 객체)

### 2.3 게시글 검색
- **Method**: `GET`
- **URL**: `{{baseUrl}}/posts/search?keyword=spring&page=0&size=20&sort=createdAt,desc`
- **권한**: 공개
- **설명**: 제목 또는 내용으로 게시글을 검색합니다.
- **Query Parameters**:
  - `keyword`: 검색 키워드 (필수)
  - `page`, `size`, `sort`: 페이징 파라미터
- **Response**: `200 OK` (Page 객체)

### 2.4 카테고리별 게시글 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/posts/category/{{categoryId}}?page=0&size=20&sort=createdAt,desc`
- **권한**: 공개
- **Response**: `200 OK` (Page 객체)

### 2.5 게시글 상세 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/posts/{{postId}}`
- **권한**: 공개
- **설명**: 게시글 ID로 상세 정보를 조회합니다. 조회 시 조회수가 증가합니다.
- **Response**: `200 OK`

### 2.6 내 게시글 목록
- **Method**: `GET`
- **URL**: `{{baseUrl}}/posts/my?page=0&size=20&sort=createdAt,desc`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **설명**: 로그인한 사용자의 모든 게시글을 조회합니다.
- **Response**: `200 OK` (Page 객체)

### 2.7 게시글 수정
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/posts/{{postId}}`
- **권한**: 인증 필요 (작성자 본인)
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Request Body**:
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "categoryId": 2
}
```
- **Response**: `200 OK`

### 2.8 게시글 삭제 (소프트 삭제)
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/posts/{{postId}}`
- **권한**: 인증 필요 (작성자 본인)
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `204 No Content`

### 2.9 게시글 강제 삭제
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/posts/{{postId}}/force`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **설명**: 게시글을 DB에서 완전히 삭제합니다.
- **Response**: `204 No Content`

---

## 3. 댓글 (Comments)

### 3.1 댓글 작성
- **Method**: `POST`
- **URL**: `{{baseUrl}}/posts/{{postId}}/comments`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Request Body**:
```json
{
  "content": "댓글 내용"
}
```
- **Response**: `201 Created`
- **Postman Tests 스크립트** (commentId 저장):
```javascript
const json = pm.response.json();
pm.environment.set("commentId", json.id);
```

### 3.2 댓글 목록 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/posts/{{postId}}/comments?page=0&size=20&sort=createdAt,asc`
- **권한**: 공개
- **설명**: 게시글의 댓글 목록을 조회합니다.
- **Query Parameters**:
  - `page`: 페이지 번호 (default: 0)
  - `size`: 페이지 크기 (default: 20)
  - `sort`: 정렬 기준 (default: createdAt,asc)
- **Response**: `200 OK` (Page 객체)

### 3.3 댓글 수 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/posts/{{postId}}/comments/count`
- **권한**: 공개
- **Response**: `200 OK`
```json
10
```

### 3.4 댓글 단건 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/comments/{{commentId}}`
- **권한**: 공개
- **Response**: `200 OK`

### 3.5 내 댓글 목록
- **Method**: `GET`
- **URL**: `{{baseUrl}}/comments/my?page=0&size=20&sort=createdAt,desc`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK` (Page 객체)

### 3.6 댓글 수정
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/comments/{{commentId}}`
- **권한**: 인증 필요 (작성자 본인)
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Request Body**:
```json
{
  "content": "수정된 댓글 내용"
}
```
- **Response**: `200 OK`

### 3.7 댓글 삭제 (소프트 삭제)
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/comments/{{commentId}}`
- **권한**: 인증 필요 (작성자 본인)
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `204 No Content`

### 3.8 댓글 강제 삭제
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/comments/{{commentId}}/force`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `204 No Content`

---

## 4. 좋아요 (Likes)

### 4.1 좋아요 토글
- **Method**: `POST`
- **URL**: `{{baseUrl}}/posts/{{postId}}/like`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **설명**: 게시글 좋아요/좋아요 취소를 토글합니다.
- **Response**: `200 OK`
```json
{
  "liked": true,
  "likeCount": 15
}
```

### 4.2 좋아요 상태 확인
- **Method**: `GET`
- **URL**: `{{baseUrl}}/posts/{{postId}}/like/status`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **설명**: 사용자가 해당 게시글에 좋아요를 눌렀는지 확인합니다.
- **Response**: `200 OK`
```json
{
  "liked": true
}
```

### 4.3 좋아요 수 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/posts/{{postId}}/like/count`
- **권한**: 공개
- **Response**: `200 OK`
```json
15
```

### 4.4 좋아요한 사용자 목록
- **Method**: `GET`
- **URL**: `{{baseUrl}}/posts/{{postId}}/likes?page=0&size=20&sort=createdAt,desc`
- **권한**: 공개
- **Response**: `200 OK` (Page 객체)

### 4.5 내가 좋아요한 게시글
- **Method**: `GET`
- **URL**: `{{baseUrl}}/likes/my?page=0&size=20&sort=createdAt,desc`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK` (Page 객체)

---

## 5. 카테고리 (Categories)

기본 경로: `/categories`

### 5.1 카테고리 생성
- **Method**: `POST`
- **URL**: `{{baseUrl}}/categories`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Request Body**:
```json
{
  "name": "카테고리 이름",
  "slug": "category-slug",
  "description": "카테고리 설명"
}
```
- **Response**: `201 Created`
- **Postman Tests 스크립트** (categoryId 저장):
```javascript
const json = pm.response.json();
pm.environment.set("categoryId", json.id);
```

### 5.2 카테고리 목록 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/categories`
- **권한**: 공개
- **Response**: `200 OK` (List)

### 5.3 카테고리 조회 (ID)
- **Method**: `GET`
- **URL**: `{{baseUrl}}/categories/{{categoryId}}`
- **권한**: 공개
- **Response**: `200 OK`

### 5.4 카테고리 조회 (Slug)
- **Method**: `GET`
- **URL**: `{{baseUrl}}/categories/slug/notice`
- **권한**: 공개
- **Response**: `200 OK`

### 5.5 카테고리 수정
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/categories/{{categoryId}}`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Request Body**:
```json
{
  "name": "수정된 이름",
  "slug": "updated-slug",
  "description": "수정된 설명"
}
```
- **Response**: `200 OK`

### 5.6 카테고리 삭제
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/categories/{{categoryId}}`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `204 No Content`

---

## 6. 관리자 (Admin)

기본 경로: `/admin` (모든 엔드포인트 관리자 전용)

### 6.1 통계 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/admin/statistics`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK`
```json
{
  "totalUsers": 100,
  "totalPosts": 500,
  "totalComments": 1200
}
```

### 6.2 사용자 목록 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/admin/users?page=0&size=20&sort=createdAt,desc`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK` (Page 객체)

### 6.3 상태별 사용자 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/admin/users/status/ACTIVE?page=0&size=20&sort=createdAt,desc`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Path Parameters**:
  - `status`: 사용자 상태 (예: ACTIVE, SUSPENDED)
- **Response**: `200 OK` (Page 객체)

### 6.4 사용자 상세 조회
- **Method**: `GET`
- **URL**: `{{baseUrl}}/admin/users/{{userId}}`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK`

### 6.5 사용자 정보 수정
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/admin/users/{{userId}}`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Request Body**:
```json
{
  "email": "updated@example.com",
  "nickname": "새로운닉네임",
  "role": "USER"
}
```
- **Response**: `200 OK`

### 6.6 사용자 정지
- **Method**: `POST`
- **URL**: `{{baseUrl}}/admin/users/{{userId}}/suspend`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK`

### 6.7 사용자 정지 해제
- **Method**: `POST`
- **URL**: `{{baseUrl}}/admin/users/{{userId}}/activate`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK`

### 6.8 사용자 삭제
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/admin/users/{{userId}}`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `204 No Content`

---

## 7. 헬스체크 (Health)

### 7.1 서버 상태 확인
- **Method**: `GET`
- **URL**: `{{baseUrl}}/health`
- **권한**: 공개
- **설명**: 서버가 정상적으로 동작하는지 확인합니다.
- **Response**: `200 OK`
```json
{
  "status": "UP",
  "version": "1.0.0",
  "buildTime": "2025-01-15T10:30:00Z",
  "timestamp": "2025-01-15T15:45:00Z"
}
```

---

## 부록

### A. 인증 헤더 사용법

대부분의 인증이 필요한 엔드포인트는 다음과 같은 헤더를 포함해야 합니다:

```
Authorization: Bearer {{accessToken}}
```

Postman에서 설정하는 방법:
1. **Headers** 탭에서 직접 설정
2. 또는 **Authorization** 탭 → Type: **Bearer Token** → Token: `{{accessToken}}`

### B. 페이징 파라미터

페이징이 지원되는 엔드포인트의 쿼리 파라미터:

- `page`: 페이지 번호 (0부터 시작, default: 0)
- `size`: 페이지 크기 (default: 20)
- `sort`: 정렬 기준 (예: `createdAt,desc` 또는 `title,asc`)

예시:
```
GET {{baseUrl}}/posts?page=0&size=10&sort=createdAt,desc
```

### C. 권한 레벨

- **공개**: 인증 없이 누구나 접근 가능
- **인증 필요**: 로그인한 사용자만 접근 가능
- **관리자 전용**: ADMIN 역할을 가진 사용자만 접근 가능
- **작성자 본인**: 리소스를 생성한 사용자만 수정/삭제 가능

### D. 빠른 테스트 순서

1. **헬스체크**: `GET /health`
2. **회원가입**: `POST /auth/signup`
3. **로그인**: `POST /auth/login` → accessToken 저장
4. **카테고리 생성** (ADMIN 계정): `POST /categories` → categoryId 저장
5. **게시글 작성**: `POST /posts` → postId 저장
6. **댓글 작성**: `POST /posts/{postId}/comments` → commentId 저장
7. **좋아요**: `POST /posts/{postId}/like`
8. **목록/검색 API 테스트**
9. **수정/삭제 API 테스트**

### E. 에러 응답 형식

API는 다음과 같은 형식으로 에러를 반환합니다:

```json
{
  "timestamp": "2025-01-15T15:45:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "유효성 검증 실패",
  "path": "/posts"
}
```

주요 HTTP 상태 코드:
- `200 OK`: 요청 성공
- `201 Created`: 리소스 생성 성공
- `204 No Content`: 요청 성공, 반환 데이터 없음
- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증 필요
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스를 찾을 수 없음
- `500 Internal Server Error`: 서버 오류
