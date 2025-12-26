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
- [8. 에러 케이스 테스트](#8-에러-케이스-테스트)
- [부록](#부록)
  - [A. 인증 헤더 사용법](#a-인증-헤더-사용법)
  - [B. 페이징 파라미터](#b-페이징-파라미터)
  - [C. 권한 레벨](#c-권한-레벨)
  - [D. 빠른 테스트 순서](#d-빠른-테스트-순서)
  - [E. 에러 응답 형식](#e-에러-응답-형식)

---

## Postman 환경변수 설정 추천

```
base_url = http://localhost:8080
accessToken = (로그인 후 받은 토큰)
refreshToken = (로그인 후 받은 리프레시 토큰)
postId = 1
commentId = 1
categoryId = 1
userId = 1
```

**참고**: 문서의 모든 엔드포인트 URL에서 `{{base_url}}`을 사용합니다.

---

## 1. 인증/인가 (Auth)

기본 경로: `/auth`

### 1.1 회원가입
- **Method**: `POST`
- **URL**: `{{base_url}}/auth/signup`
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
- **URL**: `{{base_url}}/auth/login`
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

#### Postman Tests 스크립트
```javascript
// 로그인 성공 시 토큰을 환경변수에 자동 저장
pm.test('로그인 성공 - 200 OK', function () {
    pm.response.to.have.status(200);
});

pm.test('응답에 토큰 포함 확인', function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('accessToken');
    pm.expect(json).to.have.property('refreshToken');
});

// 토큰 환경변수에 저장
if (pm.response.code === 200) {
    const json = pm.response.json();
    pm.environment.set('accessToken', json.accessToken);
    pm.environment.set('refreshToken', json.refreshToken);
    console.log('✅ 토큰 저장 완료');
}
```

### 1.3 토큰 갱신
- **Method**: `POST`
- **URL**: `{{base_url}}/auth/refresh`
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
- **URL**: `{{base_url}}/auth/logout`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **설명**: 현재 로그인된 사용자를 로그아웃하며, Refresh Token이 무효화됩니다.
- **Response**: `200 OK`

#### Postman Pre-request Script
```javascript
// Authorization 헤더 자동 주입
const token = pm.environment.get('accessToken');
if (token) {
    pm.request.headers.add({
        key: 'Authorization',
        value: 'Bearer ' + token
    });
}
```

#### Postman Tests 스크립트
```javascript
// 로그아웃 성공 시 환경변수에서 토큰 삭제
pm.test('로그아웃 성공 - 200 OK', function () {
    pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
    pm.environment.unset('accessToken');
    pm.environment.unset('refreshToken');
    console.log('✅ 로그아웃 완료 - 토큰 삭제됨');
}
```

### 1.5 카카오 로그인
- **Method**: `POST`
- **URL**: `{{base_url}}/auth/kakao-login`
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
- **URL**: `{{base_url}}/posts`
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

#### Postman Pre-request Script
```javascript
// Authorization 헤더 자동 주입
const token = pm.environment.get('accessToken');
if (token) {
    pm.request.headers.add({
        key: 'Authorization',
        value: 'Bearer ' + token
    });
    console.log('✅ 토큰 자동 주입 완료');
}
```

#### Postman Tests 스크립트
```javascript
// 게시글 생성 후 postId를 환경변수에 저장
pm.test('게시글 생성 성공 - 201 Created', function () {
    pm.response.to.have.status(201);
});

pm.test('게시글 데이터 검증', function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('id');
    pm.expect(json).to.have.property('title');
    pm.expect(json).to.have.property('content');
});

// postId 저장
if (pm.response.code === 201) {
    const json = pm.response.json();
    pm.environment.set('postId', json.id);
    console.log('✅ postId 저장: ' + json.id);
}
```

### 2.2 게시글 목록 조회
- **Method**: `GET`
- **URL**: `{{base_url}}/posts?page=0&size=20&sort=createdAt,desc`
- **권한**: 공개
- **설명**: 공개된 게시글 목록을 페이징하여 조회합니다.
- **Query Parameters**:
  - `page`: 페이지 번호 (default: 0)
  - `size`: 페이지 크기 (default: 20)
  - `sort`: 정렬 기준 (default: createdAt,desc)
- **Response**: `200 OK` (Page 객체)

#### Postman Tests 스크립트
```javascript
// 페이징 응답의 구조와 성능 검증
pm.test('게시글 목록 조회 성공 - 200 OK', function () {
    pm.response.to.have.status(200);
});

pm.test('페이징 데이터 구조 검증', function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('content');
    pm.expect(json).to.have.property('totalElements');
    pm.expect(json).to.have.property('totalPages');
    pm.expect(json.content).to.be.an('array');
});

pm.test('응답 시간 확인 (2초 이내)', function () {
    pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### 2.3 게시글 검색
- **Method**: `GET`
- **URL**: `{{base_url}}/posts/search?keyword=spring&page=0&size=20&sort=createdAt,desc`
- **권한**: 공개
- **설명**: 제목 또는 내용으로 게시글을 검색합니다.
- **Query Parameters**:
  - `keyword`: 검색 키워드 (필수)
  - `page`, `size`, `sort`: 페이징 파라미터
- **Response**: `200 OK` (Page 객체)

### 2.4 카테고리별 게시글 조회
- **Method**: `GET`
- **URL**: `{{base_url}}/posts/category/{{categoryId}}?page=0&size=20&sort=createdAt,desc`
- **권한**: 공개
- **Response**: `200 OK` (Page 객체)

### 2.5 게시글 상세 조회
- **Method**: `GET`
- **URL**: `{{base_url}}/posts/{{postId}}`
- **권한**: 공개
- **설명**: 게시글 ID로 상세 정보를 조회합니다. 조회 시 조회수가 증가합니다.
- **Response**: `200 OK`

### 2.6 내 게시글 목록
- **Method**: `GET`
- **URL**: `{{base_url}}/posts/my?page=0&size=20&sort=createdAt,desc`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **설명**: 로그인한 사용자의 모든 게시글을 조회합니다.
- **Response**: `200 OK` (Page 객체)

### 2.7 게시글 수정
- **Method**: `PUT`
- **URL**: `{{base_url}}/posts/{{postId}}`
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
- **URL**: `{{base_url}}/posts/{{postId}}`
- **권한**: 인증 필요 (작성자 본인)
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `204 No Content`

### 2.9 게시글 강제 삭제
- **Method**: `DELETE`
- **URL**: `{{base_url}}/posts/{{postId}}/force`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **설명**: 게시글을 DB에서 완전히 삭제합니다.
- **Response**: `204 No Content`

---

## 3. 댓글 (Comments)

### 3.1 댓글 작성
- **Method**: `POST`
- **URL**: `{{base_url}}/posts/{{postId}}/comments`
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

#### Postman Pre-request Script
```javascript
// Authorization 헤더 자동 주입
const token = pm.environment.get('accessToken');
if (token) {
    pm.request.headers.add({
        key: 'Authorization',
        value: 'Bearer ' + token
    });
}
```

#### Postman Tests 스크립트
```javascript
// 댓글 생성 후 commentId를 환경변수에 저장
pm.test('댓글 생성 성공 - 201 Created', function () {
    pm.response.to.have.status(201);
});

pm.test('댓글 데이터 검증', function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('id');
    pm.expect(json).to.have.property('content');
});

// commentId 저장
if (pm.response.code === 201) {
    const json = pm.response.json();
    pm.environment.set('commentId', json.id);
    console.log('✅ commentId 저장: ' + json.id);
}
```

### 3.2 댓글 목록 조회
- **Method**: `GET`
- **URL**: `{{base_url}}/posts/{{postId}}/comments?page=0&size=20&sort=createdAt,asc`
- **권한**: 공개
- **설명**: 게시글의 댓글 목록을 조회합니다.
- **Query Parameters**:
  - `page`: 페이지 번호 (default: 0)
  - `size`: 페이지 크기 (default: 20)
  - `sort`: 정렬 기준 (default: createdAt,asc)
- **Response**: `200 OK` (Page 객체)

### 3.3 댓글 수 조회
- **Method**: `GET`
- **URL**: `{{base_url}}/posts/{{postId}}/comments/count`
- **권한**: 공개
- **Response**: `200 OK`
```json
10
```

### 3.4 댓글 단건 조회
- **Method**: `GET`
- **URL**: `{{base_url}}/comments/{{commentId}}`
- **권한**: 공개
- **Response**: `200 OK`

### 3.5 내 댓글 목록
- **Method**: `GET`
- **URL**: `{{base_url}}/comments/my?page=0&size=20&sort=createdAt,desc`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK` (Page 객체)

### 3.6 댓글 수정
- **Method**: `PUT`
- **URL**: `{{base_url}}/comments/{{commentId}}`
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
- **URL**: `{{base_url}}/comments/{{commentId}}`
- **권한**: 인증 필요 (작성자 본인)
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `204 No Content`

### 3.8 댓글 강제 삭제
- **Method**: `DELETE`
- **URL**: `{{base_url}}/comments/{{commentId}}/force`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `204 No Content`

---

## 4. 좋아요 (Likes)

### 4.1 좋아요 토글
- **Method**: `POST`
- **URL**: `{{base_url}}/posts/{{postId}}/like`
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
- **URL**: `{{base_url}}/posts/{{postId}}/like/status`
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
- **URL**: `{{base_url}}/posts/{{postId}}/like/count`
- **권한**: 공개
- **Response**: `200 OK`
```json
15
```

### 4.4 좋아요한 사용자 목록
- **Method**: `GET`
- **URL**: `{{base_url}}/posts/{{postId}}/likes?page=0&size=20&sort=createdAt,desc`
- **권한**: 공개
- **Response**: `200 OK` (Page 객체)

### 4.5 내가 좋아요한 게시글
- **Method**: `GET`
- **URL**: `{{base_url}}/likes/my?page=0&size=20&sort=createdAt,desc`
- **권한**: 인증 필요
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK` (Page 객체)

---

## 5. 카테고리 (Categories)

기본 경로: `/categories`

### 5.1 카테고리 생성
- **Method**: `POST`
- **URL**: `{{base_url}}/categories`
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
- **URL**: `{{base_url}}/categories`
- **권한**: 공개
- **Response**: `200 OK` (List)

### 5.3 카테고리 조회 (ID)
- **Method**: `GET`
- **URL**: `{{base_url}}/categories/{{categoryId}}`
- **권한**: 공개
- **Response**: `200 OK`

### 5.4 카테고리 조회 (Slug)
- **Method**: `GET`
- **URL**: `{{base_url}}/categories/slug/notice`
- **권한**: 공개
- **Response**: `200 OK`

### 5.5 카테고리 수정
- **Method**: `PUT`
- **URL**: `{{base_url}}/categories/{{categoryId}}`
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
- **URL**: `{{base_url}}/categories/{{categoryId}}`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `204 No Content`

---

## 6. 관리자 (Admin)

기본 경로: `/admin` (모든 엔드포인트 관리자 전용)

### 6.1 통계 조회
- **Method**: `GET`
- **URL**: `{{base_url}}/admin/statistics`
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
- **URL**: `{{base_url}}/admin/users?page=0&size=20&sort=createdAt,desc`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK` (Page 객체)

### 6.3 상태별 사용자 조회
- **Method**: `GET`
- **URL**: `{{base_url}}/admin/users/status/ACTIVE?page=0&size=20&sort=createdAt,desc`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Path Parameters**:
  - `status`: 사용자 상태 (예: ACTIVE, SUSPENDED)
- **Response**: `200 OK` (Page 객체)

### 6.4 사용자 상세 조회
- **Method**: `GET`
- **URL**: `{{base_url}}/admin/users/{{userId}}`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK`

### 6.5 사용자 정보 수정
- **Method**: `PUT`
- **URL**: `{{base_url}}/admin/users/{{userId}}`
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
- **URL**: `{{base_url}}/admin/users/{{userId}}/suspend`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK`

### 6.7 사용자 정지 해제
- **Method**: `POST`
- **URL**: `{{base_url}}/admin/users/{{userId}}/activate`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `200 OK`

### 6.8 사용자 삭제
- **Method**: `DELETE`
- **URL**: `{{base_url}}/admin/users/{{userId}}`
- **권한**: 관리자 전용
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `204 No Content`

---

## 7. 헬스체크 (Health)

### 7.1 서버 상태 확인
- **Method**: `GET`
- **URL**: `{{base_url}}/health`
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

## 8. 에러 케이스 테스트

> **중요**: Postman 테스트 시 각 대표 에러케이스를 실제로 검증하는 요청들입니다.

### 8.1 인증 오류 (401 Unauthorized)

#### 8.1.1 토큰 없이 인증 필요 엔드포인트 호출
- **Method**: `POST`
- **URL**: `{{base_url}}/posts`
- **Headers**: Authorization 헤더 없음
- **Request Body**:
```json
{
  "title": "테스트 게시글",
  "content": "내용",
  "categoryId": 1
}
```
- **Expected Response**: `401 Unauthorized`

**Postman Tests 스크립트**:
```javascript
// 401 에러 검증
pm.test('401 Unauthorized 에러 발생', function () {
    pm.response.to.have.status(401);
});

pm.test('에러 응답 구조 검증', function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('status');
    pm.expect(json).to.have.property('error');
    pm.expect(json.status).to.eql(401);
});

console.log('✅ 401 에러 케이스 검증 완료: 인증 토큰 없음');
```

#### 8.1.2 잘못된 토큰으로 API 호출
- **Method**: `POST`
- **URL**: `{{base_url}}/posts/{{postId}}/comments`
- **Headers**:
  - `Authorization: Bearer invalid_token_here`
- **Request Body**:
```json
{
  "content": "댓글 내용"
}
```
- **Expected Response**: `401 Unauthorized`

#### 8.1.3 틀린 비밀번호로 로그인 시도
- **Method**: `POST`
- **URL**: `{{base_url}}/auth/login`
- **Request Body**:
```json
{
  "email": "admin@blog.com",
  "password": "wrong_password"
}
```
- **Expected Response**: `401 Unauthorized`
```json
{
  "timestamp": "2025-01-15T15:45:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "이메일 또는 비밀번호가 올바르지 않습니다",
  "path": "/auth/login"
}
```

#### 8.1.4 존재하지 않는 이메일로 로그인 시도
- **Method**: `POST`
- **URL**: `{{base_url}}/auth/login`
- **Request Body**:
```json
{
  "email": "nonexistent@example.com",
  "password": "password123"
}
```
- **Expected Response**: `401 Unauthorized`

### 8.2 권한 오류 (403 Forbidden)

#### 8.2.1 일반 사용자가 관리자 전용 카테고리 생성 시도
- **Method**: `POST`
- **URL**: `{{base_url}}/categories`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}` (일반 사용자 토큰)
- **Request Body**:
```json
{
  "name": "새 카테고리",
  "slug": "new-category",
  "description": "설명"
}
```
- **Expected Response**: `403 Forbidden`
```json
{
  "timestamp": "2025-01-15T15:45:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "접근 권한이 없습니다",
  "path": "/categories"
}
```

#### 8.2.2 다른 사용자의 게시글 수정 시도
- **Method**: `PUT`
- **URL**: `{{base_url}}/posts/{{postId}}`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}` (작성자가 아닌 다른 사용자 토큰)
- **Request Body**:
```json
{
  "title": "수정 시도",
  "content": "내용",
  "categoryId": 1
}
```
- **Expected Response**: `403 Forbidden`

#### 8.2.3 일반 사용자가 사용자 목록 조회 시도
- **Method**: `GET`
- **URL**: `{{base_url}}/admin/users`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}` (일반 사용자 토큰)
- **Expected Response**: `403 Forbidden`

### 8.3 잘못된 요청 (400 Bad Request)

#### 8.3.1 필수 필드 누락 - 제목 없이 게시글 작성
- **Method**: `POST`
- **URL**: `{{base_url}}/posts`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Request Body**:
```json
{
  "content": "내용만 있고 제목이 없음",
  "categoryId": 1
}
```
- **Expected Response**: `400 Bad Request`

**Postman Tests 스크립트**:
```javascript
// 400 에러 검증
pm.test('400 Bad Request 에러 발생', function () {
    pm.response.to.have.status(400);
});

pm.test('유효성 검증 실패 확인', function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('status');
    pm.expect(json.status).to.eql(400);
});

console.log('✅ 400 에러 케이스 검증 완료: 필수 필드 누락');
```

#### 8.3.2 이메일 형식 오류로 회원가입 시도
- **Method**: `POST`
- **URL**: `{{base_url}}/auth/signup`
- **Request Body**:
```json
{
  "email": "invalid-email-format",
  "password": "password123",
  "nickname": "테스트"
}
```
- **Expected Response**: `400 Bad Request`
```json
{
  "timestamp": "2025-01-15T15:45:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "올바른 이메일 형식이 아닙니다",
  "path": "/auth/signup"
}
```

#### 8.3.3 이미 존재하는 이메일로 회원가입 시도
- **Method**: `POST`
- **URL**: `{{base_url}}/auth/signup`
- **Request Body**:
```json
{
  "email": "admin@blog.com",
  "password": "password123",
  "nickname": "테스트"
}
```
- **Expected Response**: `400 Bad Request`
```json
{
  "timestamp": "2025-01-15T15:45:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "이미 사용 중인 이메일입니다",
  "path": "/auth/signup"
}
```

#### 8.3.4 빈 내용으로 댓글 작성 시도
- **Method**: `POST`
- **URL**: `{{base_url}}/posts/{{postId}}/comments`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Request Body**:
```json
{
  "content": ""
}
```
- **Expected Response**: `400 Bad Request`

#### 8.3.5 유효하지 않은 Refresh Token으로 토큰 갱신 시도
- **Method**: `POST`
- **URL**: `{{base_url}}/auth/refresh`
- **Request Body**:
```json
{
  "refreshToken": "invalid_or_expired_refresh_token"
}
```
- **Expected Response**: `400 Bad Request`

### 8.4 리소스 없음 (404 Not Found)

#### 8.4.1 존재하지 않는 게시글 조회
- **Method**: `GET`
- **URL**: `{{base_url}}/posts/99999`
- **Expected Response**: `404 Not Found`

**Postman Tests 스크립트**:
```javascript
// 404 에러 검증
pm.test('404 Not Found 에러 발생', function () {
    pm.response.to.have.status(404);
});

pm.test('리소스 없음 확인', function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('status');
    pm.expect(json.status).to.eql(404);
});

console.log('✅ 404 에러 케이스 검증 완료: 존재하지 않는 리소스');
```

#### 8.4.2 존재하지 않는 카테고리로 게시글 작성
- **Method**: `POST`
- **URL**: `{{base_url}}/posts`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Request Body**:
```json
{
  "title": "테스트 게시글",
  "content": "내용",
  "categoryId": 99999
}
```
- **Expected Response**: `404 Not Found`
```json
{
  "timestamp": "2025-01-15T15:45:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "카테고리를 찾을 수 없습니다",
  "path": "/posts"
}
```

#### 8.4.3 존재하지 않는 댓글 수정 시도
- **Method**: `PUT`
- **URL**: `{{base_url}}/comments/99999`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Request Body**:
```json
{
  "content": "수정된 내용"
}
```
- **Expected Response**: `404 Not Found`

#### 8.4.4 존재하지 않는 카테고리 조회 (Slug)
- **Method**: `GET`
- **URL**: `{{base_url}}/categories/slug/nonexistent-category`
- **Expected Response**: `404 Not Found`

#### 8.4.5 존재하지 않는 사용자 조회 (관리자)
- **Method**: `GET`
- **URL**: `{{base_url}}/admin/users/99999`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}` (관리자 토큰)
- **Expected Response**: `404 Not Found`

### 8.5 에러 케이스 테스트 체크리스트

Postman에서 다음 에러 케이스들을 모두 테스트하여 올바른 에러 응답이 반환되는지 확인하세요:

- [ ] **401 Unauthorized (4개)**
  - [ ] 토큰 없이 게시글 작성
  - [ ] 잘못된 토큰으로 댓글 작성
  - [ ] 틀린 비밀번호로 로그인
  - [ ] 존재하지 않는 이메일로 로그인

- [ ] **403 Forbidden (3개)**
  - [ ] 일반 사용자가 카테고리 생성
  - [ ] 다른 사용자의 게시글 수정
  - [ ] 일반 사용자가 관리자 엔드포인트 호출

- [ ] **400 Bad Request (5개)**
  - [ ] 필수 필드 누락 (제목 없이 게시글 작성)
  - [ ] 이메일 형식 오류로 회원가입
  - [ ] 중복 이메일로 회원가입
  - [ ] 빈 내용으로 댓글 작성
  - [ ] 유효하지 않은 Refresh Token

- [ ] **404 Not Found (5개)**
  - [ ] 존재하지 않는 게시글 조회
  - [ ] 존재하지 않는 카테고리로 게시글 작성
  - [ ] 존재하지 않는 댓글 수정
  - [ ] 존재하지 않는 카테고리 조회 (Slug)
  - [ ] 존재하지 않는 사용자 조회

**총 17개의 에러 케이스 테스트 항목**

### 8.6 Postman Tests 스크립트 (에러 검증용)

각 에러 케이스 요청의 **Tests** 탭에 다음과 같은 스크립트를 추가하여 자동 검증할 수 있습니다:

#### 401 에러 검증
```javascript
pm.test("Status code is 401", function () {
    pm.response.to.have.status(401);
});

pm.test("Error message exists", function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('error');
    pm.expect(json.error).to.eql('Unauthorized');
});
```

#### 403 에러 검증
```javascript
pm.test("Status code is 403", function () {
    pm.response.to.have.status(403);
});

pm.test("Error message exists", function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('error');
    pm.expect(json.error).to.eql('Forbidden');
});
```

#### 400 에러 검증
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Error message exists", function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('error');
    pm.expect(json.error).to.eql('Bad Request');
});
```

#### 404 에러 검증
```javascript
pm.test("Status code is 404", function () {
    pm.response.to.have.status(404);
});

pm.test("Error message exists", function () {
    const json = pm.response.json();
    pm.expect(json).to.have.property('error');
    pm.expect(json.error).to.eql('Not Found');
});
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
GET {{base_url}}/posts?page=0&size=10&sort=createdAt,desc
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
10. **에러 케이스 테스트** (401, 400, 404 등)

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
