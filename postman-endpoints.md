# Postman 테스트용 엔드포인트 메서드 정리 (blog-api)

> Base URL 예시: `http://localhost:8080`  
> 인증 필요 API는 **Headers**에 `Authorization: Bearer {{accessToken}}` 를 넣는다.

---

## 0) Postman 환경변수 추천

- `baseUrl` = `http://localhost:8080`
- `accessToken` = (로그인/소셜로그인 성공 시 받은 토큰)
- `postId` = 1
- `commentId` = 1
- `categoryId` = 1
- `userId` = 1
- `keyword` = `spring`

---

## 1) 공통 설정 (Postman)

### 공통 Headers
- `Content-Type: application/json`
- (인증 필요 시) `Authorization: Bearer {{accessToken}}`

### Raw 데이터 전송 방법
- Postman → **Body** → **raw** → **JSON** 선택
- 아래 예시 JSON 그대로 붙여넣고 요청

---

## 2) 인증 (Authentication) — `/auth`

### 2.1 회원가입
- **Method**: `POST`
- **Endpoint**: `{{base_url}}/auth/register`
- **Auth**: ❌
- **Body (raw / JSON)**:
```json
{
  "email": "user1@example.com",
  "password": "P@ssw0rd!",
  "nickname": "user1"
}
```

### 2.2 이메일 로그인
- **Method**: `POST`
- **Endpoint**: `{{base_url}}/auth/login`
- **Auth**: ❌
- **Body (raw / JSON)**:
```json
{
  "email": "user1@example.com",
  "password": "P@ssw0rd!"
}
```

### 2.3 Firebase 소셜 로그인
- **Method**: `POST`
- **Endpoint**: `{{base_url}}/auth/firebase`
- **Auth**: ❌
- **Body (raw / JSON)**:
```json
{
  "idToken": "FIREBASE_ID_TOKEN_HERE",
  "provider": "GOOGLE"
}
```
> `idToken`은 프론트(또는 Firebase CLI/SDK)에서 로그인 후 받은 **Firebase ID Token**을 넣는다.  
> provider 값은 프로젝트 구현에 맞춰 `GOOGLE | KAKAO | FIREBASE` 중 사용.

### 2.4 내 정보 조회
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/auth/me`
- **Auth**: ✅

### 2.5 내 정보 수정
- **Method**: `PUT`
- **Endpoint**: `{{base_url}}/auth/me`
- **Auth**: ✅
- **Body (raw / JSON)**:
```json
{
  "nickname": "newNickname"
}
```

---

## 3) 게시글 (Posts) — `/posts`

### 3.1 게시글 작성
- **Method**: `POST`
- **Endpoint**: `{{base_url}}/posts`
- **Auth**: ✅ (ROLE_USER)
- **Body (raw / JSON)**:
```json
{
  "title": "첫 글",
  "content": "내용입니다",
  "categoryId": 1,
  "status": "PUBLISHED"
}
```

### 3.2 게시글 목록 (PUBLISHED만)
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/posts?page=0&size=20&sort=createdAt,desc`
- **Auth**: ❌

### 3.3 게시글 검색
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/posts/search?keyword={{keyword}}&page=0&size=20&sort=createdAt,desc`
- **Auth**: ❌

### 3.4 카테고리별 게시글
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/posts/category/{{categoryId}}?page=0&size=20&sort=createdAt,desc`
- **Auth**: ❌

### 3.5 게시글 상세 (조회수+1)
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/posts/{{postId}}`
- **Auth**: ❌

### 3.6 내 게시글 목록
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/posts/my?page=0&size=20&sort=createdAt,desc`
- **Auth**: ✅

### 3.7 게시글 수정
- **Method**: `PUT`
- **Endpoint**: `{{base_url}}/posts/{{postId}}`
- **Auth**: ✅ (OWNER)
- **Body (raw / JSON)**:
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "categoryId": 1,
  "status": "PUBLISHED"
}
```
> 부분 수정이면 필요한 필드만 넣어도 된다.

### 3.8 게시글 삭제 (소프트)
- **Method**: `DELETE`
- **Endpoint**: `{{base_url}}/posts/{{postId}}`
- **Auth**: ✅ (OWNER)
- **Body**: 없음

### 3.9 게시글 강제 삭제 (관리자)
- **Method**: `DELETE`
- **Endpoint**: `{{base_url}}/posts/{{postId}}/force`
- **Auth**: ✅ (ADMIN)
- **Body**: 없음

---

## 4) 카테고리 (Categories) — `/categories`

### 4.1 카테고리 생성
- **Method**: `POST`
- **Endpoint**: `{{base_url}}/categories`
- **Auth**: ✅ (ADMIN)
- **Body (raw / JSON)**:
```json
{
  "name": "공지",
  "slug": "notice",
  "description": "공지사항 카테고리"
}
```

### 4.2 카테고리 목록
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/categories`
- **Auth**: ❌

### 4.3 카테고리 조회 (ID)
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/categories/{{categoryId}}`
- **Auth**: ❌

### 4.4 카테고리 조회 (Slug)
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/categories/slug/notice`
- **Auth**: ❌

### 4.5 카테고리 수정
- **Method**: `PUT`
- **Endpoint**: `{{base_url}}/categories/{{categoryId}}`
- **Auth**: ✅ (ADMIN)
- **Body (raw / JSON)**:
```json
{
  "name": "공지(수정)",
  "slug": "notice",
  "description": "설명 수정"
}
```

### 4.6 카테고리 삭제
- **Method**: `DELETE`
- **Endpoint**: `{{base_url}}/categories/{{categoryId}}`
- **Auth**: ✅ (ADMIN)
- **Body**: 없음

---

## 5) 댓글 (Comments)

### 5.1 게시글에 댓글 작성 — `/posts/{postId}/comments`
- **Method**: `POST`
- **Endpoint**: `{{base_url}}/posts/{{postId}}/comments`
- **Auth**: ✅
- **Body (raw / JSON)**:
```json
{
  "content": "댓글 내용입니다"
}
```

### 5.2 게시글 댓글 목록 — `/posts/{postId}/comments`
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/posts/{{postId}}/comments?page=0&size=20&sort=createdAt,asc`
- **Auth**: ❌

### 5.3 게시글 댓글 수 — `/posts/{postId}/comments/count`
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/posts/{{postId}}/comments/count`
- **Auth**: ❌

---

### 5.4 댓글 단건 조회 — `/comments/{id}`
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/comments/{{commentId}}`
- **Auth**: ❌

### 5.5 내 댓글 목록 — `/comments/my`
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/comments/my?page=0&size=20&sort=createdAt,desc`
- **Auth**: ✅

### 5.6 댓글 수정 — `/comments/{id}`
- **Method**: `PUT`
- **Endpoint**: `{{base_url}}/comments/{{commentId}}`
- **Auth**: ✅ (OWNER)
- **Body (raw / JSON)**:
```json
{
  "content": "댓글 내용 수정"
}
```

### 5.7 댓글 삭제 (소프트) — `/comments/{id}`
- **Method**: `DELETE`
- **Endpoint**: `{{base_url}}/comments/{{commentId}}`
- **Auth**: ✅ (OWNER)
- **Body**: 없음

### 5.8 댓글 강제 삭제 (관리자) — `/comments/{id}/force`
- **Method**: `DELETE`
- **Endpoint**: `{{base_url}}/comments/{{commentId}}/force`
- **Auth**: ✅ (ADMIN)
- **Body**: 없음

---

## 6) 좋아요 (Likes)

### 6.1 좋아요 토글
- **Method**: `POST`
- **Endpoint**: `{{base_url}}/posts/{{postId}}/like`
- **Auth**: ✅
- **Body**: 없음

### 6.2 좋아요 상태 확인
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/posts/{{postId}}/like/status`
- **Auth**: ✅

### 6.3 좋아요 수 조회
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/posts/{{postId}}/like/count`
- **Auth**: ❌

### 6.4 좋아요한 사용자 목록
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/posts/{{postId}}/likes?page=0&size=20&sort=createdAt,desc`
- **Auth**: ❌

### 6.5 내가 좋아요한 게시글
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/likes/my?page=0&size=20&sort=createdAt,desc`
- **Auth**: ✅

---

## 7) 관리자 (Admin) — `/admin` (모두 ADMIN 권한)

### 7.1 전체 통계 조회
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/admin/statistics`
- **Auth**: ✅ (ADMIN)

### 7.2 사용자 목록
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/admin/users?page=0&size=20&sort=createdAt,desc`
- **Auth**: ✅ (ADMIN)

### 7.3 상태별 사용자 목록
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/admin/users/status/ACTIVE?page=0&size=20&sort=createdAt,desc`
- **Auth**: ✅ (ADMIN)

### 7.4 사용자 상세 조회
- **Method**: `GET`
- **Endpoint**: `{{base_url}}/admin/users/{{userId}}`
- **Auth**: ✅ (ADMIN)

### 7.5 사용자 정보 수정
- **Method**: `PUT`
- **Endpoint**: `{{base_url}}/admin/users/{{userId}}`
- **Auth**: ✅ (ADMIN)
- **Body (raw / JSON)**:
```json
{
  "nickname": "관리자가수정",
  "role": "ROLE_USER",
  "status": "ACTIVE"
}
```

### 7.6 사용자 정지
- **Method**: `POST`
- **Endpoint**: `{{base_url}}/admin/users/{{userId}}/suspend`
- **Auth**: ✅ (ADMIN)
- **Body**: 없음

### 7.7 사용자 정지 해제
- **Method**: `POST`
- **Endpoint**: `{{base_url}}/admin/users/{{userId}}/activate`
- **Auth**: ✅ (ADMIN)
- **Body**: 없음

### 7.8 사용자 강제 삭제
- **Method**: `DELETE`
- **Endpoint**: `{{base_url}}/admin/users/{{userId}}`
- **Auth**: ✅ (ADMIN)
- **Body**: 없음

---

## 8) (선택) Postman Tests 탭 스니펫 예시

### 8.1 로그인 응답에서 accessToken 저장 (예시)
> 로그인/소셜로그인 응답 JSON에 `accessToken` 필드가 있다고 가정
```javascript
const json = pm.response.json();
pm.environment.set("accessToken", json.accessToken);
```

### 8.2 생성 응답에서 id 저장 (예시: 게시글 생성 후 postId)
```javascript
const json = pm.response.json();
pm.environment.set("postId", json.id);
```

---

## 9) 체크리스트 (빠른 테스트 순서)

1. `POST /auth/register`
2. `POST /auth/login` → token 저장
3. `POST /categories` (ADMIN 계정으로) → categoryId 저장
4. `POST /posts` → postId 저장
5. `POST /posts/{postId}/comments` → commentId 저장(댓글 응답에 id가 있으면)
6. `POST /posts/{postId}/like`
7. 목록/검색/카운트 API들 점검
8. OWNER/ADMIN 권한 엔드포인트(수정/삭제/force) 점검
