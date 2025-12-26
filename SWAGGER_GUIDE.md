# Swagger UI 사용 가이드

## 접속 방법

애플리케이션 실행 후 다음 URL로 접속:

```
http://localhost:8080/swagger-ui.html
```

또는

```
http://localhost:8080/swagger-ui/index.html
```

## API 문서 JSON

OpenAPI 3.0 스펙 JSON은 다음 URL에서 확인:

```
http://localhost:8080/v3/api-docs
```

## 인증 방법

### 1. JWT 토큰 인증

1. **로그인 API 호출**
   - `POST /auth/login` 엔드포인트 실행
   - Request Body:
     ```json
     {
       "email": "admin@blog.com",
       "password": "admin1234"
     }
     ```

2. **Access Token 복사**
   - 응답에서 `accessToken` 값 복사

3. **Authorize 버튼 클릭**
   - Swagger UI 상단의 `Authorize` 버튼 클릭
   - JWT 섹션에 복사한 토큰 입력 (Bearer 제외)
   - `Authorize` 버튼 클릭

4. **인증 완료**
   - 이제 모든 API 호출에 자동으로 토큰이 포함됩니다

### 2. Firebase 인증

1. **Firebase Authentication으로 로그인**
   - 프론트엔드 또는 Firebase SDK로 로그인

2. **ID Token 획득**
   - `firebase.auth().currentUser.getIdToken()`

3. **Authorize 버튼에 입력**
   - Firebase 섹션에 ID Token 입력

## 테스트 계정

### 관리자 계정
- **이메일**: `admin@blog.com`
- **비밀번호**: `admin1234`
- **권한**: ROLE_ADMIN

### 일반 사용자 계정
- **이메일**: `user1@blog.com` ~ `user40@blog.com`
- **비밀번호**: `user1234`
- **권한**: ROLE_USER

## 주요 API 그룹

### Auth (인증/인가)
- POST `/auth/login` - 일반 로그인
- POST `/auth/refresh` - 토큰 갱신
- POST `/auth/logout` - 로그아웃
- GET `/auth/google` - Google OAuth2 로그인
- POST `/auth/firebase` - Firebase 인증

### Posts (게시글)
- POST `/posts` - 게시글 작성 (인증 필요)
- GET `/posts` - 게시글 목록 조회
- GET `/posts/{id}` - 게시글 상세 조회
- PUT `/posts/{id}` - 게시글 수정 (인증 필요)
- DELETE `/posts/{id}` - 게시글 삭제 (인증 필요)
- GET `/posts/search` - 게시글 검색
- GET `/posts/my` - 내 게시글 목록 (인증 필요)

### Comments (댓글)
- POST `/posts/{postId}/comments` - 댓글 작성 (인증 필요)
- GET `/posts/{postId}/comments` - 댓글 목록 조회
- PUT `/comments/{id}` - 댓글 수정 (인증 필요)
- DELETE `/comments/{id}` - 댓글 삭제 (인증 필요)

### Likes (좋아요)
- POST `/posts/{postId}/like` - 좋아요 토글 (인증 필요)
- GET `/posts/{postId}/likes/count` - 좋아요 수 조회

### Categories (카테고리)
- GET `/categories` - 카테고리 목록 조회
- POST `/categories` - 카테고리 생성 (관리자)
- PUT `/categories/{id}` - 카테고리 수정 (관리자)
- DELETE `/categories/{id}` - 카테고리 삭제 (관리자)

### Admin (관리자)
- GET `/admin/users` - 사용자 목록 조회 (관리자)
- PUT `/admin/users/{id}/suspend` - 사용자 정지 (관리자)
- GET `/admin/stats` - 통계 조회 (관리자)

### Health (헬스체크)
- GET `/health` - 서버 상태 확인

## Swagger UI 설정

### 정렬 옵션
- **태그 정렬**: 알파벳순
- **작업 정렬**: 알파벳순
- **요청 시간 표시**: 활성화
- **Try it out**: 모든 API에서 테스트 가능

### 페이징 파라미터

목록 조회 API에서 사용 가능한 파라미터:

- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기 (기본값: 20)
- `sort`: 정렬 기준 (예: `createdAt,DESC`)

예시:
```
GET /posts?page=0&size=10&sort=createdAt,DESC
```

## 문제 해결

### 인증 오류 (401)
- JWT 토큰이 만료되었을 수 있습니다
- `/auth/refresh`로 토큰을 갱신하세요
- 또는 다시 로그인하세요

### 권한 오류 (403)
- 관리자 권한이 필요한 API입니다
- 관리자 계정으로 로그인하세요

### CORS 오류
- 로컬 환경에서는 CORS가 허용됩니다
- 프론트엔드 URL이 환경변수에 설정되어 있는지 확인하세요

## 추가 정보

### 에러 응답 형식

```json
{
  "timestamp": "2025-12-26T03:00:00Z",
  "path": "/posts/999",
  "status": 404,
  "code": "POST_NOT_FOUND",
  "message": "게시글을 찾을 수 없습니다.",
  "details": {
    "postId": "999"
  }
}
```

### 페이징 응답 형식

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 153,
  "totalPages": 8,
  "sort": "createdAt,DESC"
}
```
