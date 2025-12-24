# dev.md — 동아리 블로그 백엔드 개발 명세

## 0. 개요

본 문서는 **동아리 블로그 백엔드 API 서버** 구현을 위한 개발 명세서이다.  
Spring Boot + MySQL 기반으로 REST API 서버를 구축하며,  
인증·인가(JWT + Social Login), Redis, Docker 기반 배포, Swagger 문서화까지 포함한다.

> ⚠️ 프론트엔드는 범위 외이며, **백엔드 단독 서버**만 구현 대상이다.

---

## 1. 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| ORM | Spring Data JPA (Hibernate) |
| DB | MySQL 8 |
| Cache / Infra | Redis |
| Auth | JWT, OAuth2 (Google/Kakao/Naver 중 택1), Firebase Auth |
| Docs | Swagger(OpenAPI 3), Postman |
| Container | Docker, docker-compose |
| Deploy | JCloud (VM 기반) |
| Test | JUnit5, MockMvc |

---

## 2. 핵심 요구사항 요약

- **엔드포인트 30개 이상**
- **JWT 인증/인가 + RBAC (ROLE_USER / ROLE_ADMIN)**
- **Social Login 최소 2개**
    - 일반 OAuth2 (Google/Kakao/Naver 중 1)
    - Firebase Auth 기반 1
- **Redis 필수 사용**
- **Swagger 자동 문서화**
- **Postman Collection 제출**
- **MySQL + Flyway**
- **시드 데이터 200건 이상**
- **Docker 기반 배포**
- **헬스체크 엔드포인트 제공**
- **자동화 테스트 20개 이상**

---

## 3. 도메인(리소스) 설계

### 3.1 필수 리소스 (최소 4개)

| 리소스 | 설명 |
|---|---|
| users | 사용자 정보 |
| auth | 인증/인가 |
| posts | 게시글 |
| comments | 댓글 |
| categories | 게시글 분류 |
| likes | 좋아요 |

> 모든 리소스는 CRUD 제공  
> (auth는 인증/인가 전용)

---

## 4. 인증 & 인가

### 4.1 JWT

- Access Token / Refresh Token 구조
- 토큰 만료, 위조, 재사용 방지 처리
- Refresh Token은 Redis 저장 권장

#### 필수 엔드포인트
POST /auth/login
POST /auth/refresh
POST /auth/logout
GET /auth/google
GET /auth/firebase

yaml
코드 복사

### 4.2 RBAC

| Role | 설명 |
|---|---|
| ROLE_USER | 일반 사용자 |
| ROLE_ADMIN | 관리자 |

- 관리자 전용 API **최소 3개**
    - 사용자 정지
    - 통계 조회
    - 게시글 강제 삭제 등

---

## 5. API 규칙

### 5.1 엔드포인트 수

- CRUD: 4 리소스 × 4 = 16
- Sub-resource, 검색, 통계, 인증 API 포함
- **총 30개 이상**

### 5.2 목록 조회 규칙 (공통)

GET /posts?page=0&size=20&sort=createdAt,DESC&keyword=java

css
코드 복사

응답 예시:
```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 153,
  "totalPages": 8,
  "sort": "createdAt,DESC"
}
```
6. 에러 처리 규격
   6.1 공통 에러 응답 포맷
```json
{
  "timestamp": "2025-03-05T12:34:56Z",
  "path": "/api/posts/1",
  "status": 400,
  "code": "POST_TITLE_TOO_LONG",
  "message": "게시글 제목은 1~100자 이내여야 합니다.",
  "details": {
    "title": "현재 길이 150자"
  }
}
```

6.2 표준 에러 코드

|HTTP|Code|설명|
|--|--|--|
| 400 | BAD_REQUEST | 요청 형식 오류|
|400	|VALIDATION_FAILED	|입력 검증 실패|
|400|	INVALID_QUERY_PARAM	잘못된 |쿼리|
|401|	UNAUTHORIZED|	인증 실패|
|401|	TOKEN_EXPIRED|	토큰 만료|
|403|	FORBIDDEN|	권한 없음|
|404|	RESOURCE_NOT_FOUND|	리소스 없음|
|404|	USER_NOT_FOUND|	사용자 없음|
|409|	DUPLICATE_RESOURCE	중복|
|409|	STATE_CONFLICT|	상태 충돌|
|422|	UNPROCESSABLE_ENTITY|	논리 오류|
|429|	TOO_MANY_REQUESTS|	요청 초과|
|500|	INTERNAL_SERVER_ERROR|	서버 오류|
|500|	DATABASE_ERROR|	DB 오류|

## 7. DB 설계
- MySQL 사용
- FK, Index 필수
- N+1 문제 방지
- Flyway 마이그레이션
- 시드 데이터 200건 이상
- users / posts / comments / likes 분산

## 8. Redis 사용처
- Refresh Token 저장
- Rate Limiting
- 인증 없는 API 보호
- 캐시 (게시글 목록 등)

## 9. 보안
- .env 기반 환경변수 관리
- .env.example 필수 제공
- 비밀번호 bcrypt / Argon2 해시
- CORS 제한
- 요청 크기 제한 또는 Rate Limit 적용

## 10. 헬스체크
```bash
GET /health
```

응답 예시:
```json
{
  "status": "UP",
  "version": "1.0.0",
  "buildTime": "2025-03-01T10:00:00Z"
}
```
- 인증 없음

- 200 반환 필수

## 11. 문서화
### 11.1 Swagger
- /swagger-ui 또는 /docs
- 모든 엔드포인트 요청/응답 명시
- 에러 응답(400,401,403,404,422,500) 포함

## 11.2 Postman
- 환경변수 사용
- Pre-request Script (토큰 저장)
- Test Script 최소 5개
- 성공/실패 시나리오 포함

## 12. 테스트
- 자동화 테스트 20개 이상
- 인증 포함 테스트 필수
- 성공/실패 케이스 모두 포함

## 13. 컨테이너 & 배포
### 13.1 Docker
- Dockerfile 작성
- docker-compose.yml
- app
- mysql
- redis

### 13.2 배포
- JCloud VM
- docker compose up -d 로 실행
- 서버 재시작 후 자동 복구
- Health 체크 스크린샷 제출

## 14. 레포 구조
```css
repo-root/
├─ README.md
├─ dev.md
├─ .env.example
├─ Dockerfile
├─ docker-compose.yml
├─ docs/
├─ postman/
├─ src/
├─ migrations/
├─ seed/
└─ tests/
```

## 15. 평가 포인트 체크리스트
-  30+ 엔드포인트
-  JWT + RBAC
-  Social Login 2종
-  Redis 사용
-  Swagger 정상 동작
-  Postman 테스트 통과
-  Health 200 OK
-  Docker 배포 성공
-  테스트 20개 이상

## 16. 한계 및 개선 방향
- 알림 시스템
- 이미지 업로드
- 검색 성능 개선 (ElasticSearch)
- Frontend 연동