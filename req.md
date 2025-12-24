# req.md - 필수 설정 및 요구사항

## 📋 목차
1. [환경 요구사항](#환경-요구사항)
2. [환경변수 설정](#환경변수-설정)
3. [OAuth2 설정](#oauth2-설정)
4. [데이터베이스 설정](#데이터베이스-설정)
5. [Redis 설정](#redis-설정)
6. [실행 방법](#실행-방법)

---

## 🖥️ 환경 요구사항

### 필수 설치 항목
- **Java**: 21 이상
- **Gradle**: 8.x (Wrapper 포함)
- **Docker**: 최신 버전
- **Docker Compose**: 최신 버전

### 권장 개발 환경
- **IDE**: IntelliJ IDEA / VS Code
- **OS**: Windows 10+, macOS, Linux
- **Git**: 최신 버전

---

## 🔐 환경변수 설정

### 1. .env 파일 생성

프로젝트 루트에 `.env` 파일을 생성합니다:

```bash
cp .env.example .env
```

### 2. 환경변수 설명

#### 애플리케이션 설정
```env
# 서버 포트 (기본값: 8080)
APP_PORT=8080
```

#### MySQL 데이터베이스
```env
# MySQL Root 비밀번호 (Docker용)
MYSQL_ROOT_PASSWORD=your_secure_root_password

# 데이터베이스 이름
MYSQL_DATABASE=blog

# 애플리케이션 DB 사용자
MYSQL_USER=app

# 애플리케이션 DB 비밀번호
MYSQL_PASSWORD=your_secure_app_password

# MySQL 포트 (기본값: 3306)
MYSQL_PORT=3306
```

#### Spring Profile
```env
# 활성화할 프로파일 (local, dev, prod)
SPRING_PROFILES_ACTIVE=local
```

#### JWT 설정
```env
# JWT Secret Key (최소 32바이트 이상 필수!)
# 예시: openssl rand -base64 32
JWT_SECRET=your-super-secret-jwt-key-at-least-32-bytes-long-change-this

# Access Token 만료 시간 (초) - 기본: 900초 (15분)
JWT_ACCESS_EXP=900

# Refresh Token 만료 시간 (초) - 기본: 1209600초 (14일)
JWT_REFRESH_EXP=1209600
```

**⚠️ 중요: JWT_SECRET은 반드시 32바이트 이상이어야 합니다!**

JWT Secret 생성 명령어:
```bash
# Linux/Mac
openssl rand -base64 32

# Windows (PowerShell)
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))

# 또는 온라인 도구 사용
# https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx
```

#### Redis 설정
```env
# Redis 호스트 (Docker 사용 시: redis, 로컬: localhost)
REDIS_HOST=redis

# Redis 포트 (기본값: 6379)
REDIS_PORT=6379
```

#### OAuth2 - Google
```env
# Google OAuth2 Client ID
OAUTH_GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com

# Google OAuth2 Client Secret
OAUTH_GOOGLE_CLIENT_SECRET=your-google-client-secret
```

#### OAuth2 - Kakao
```env
# Kakao REST API 키
OAUTH_KAKAO_CLIENT_ID=your-kakao-rest-api-key

# Kakao Client Secret (선택사항, 보안 강화 시 사용)
OAUTH_KAKAO_CLIENT_SECRET=your-kakao-client-secret
```

---

## 🔑 OAuth2 설정

### Google OAuth2 설정

#### 1. Google Cloud Console 접속
https://console.cloud.google.com/

#### 2. 프로젝트 생성
1. 새 프로젝트 생성 또는 기존 프로젝트 선택
2. 프로젝트 이름: `blog-api` (예시)

#### 3. OAuth 동의 화면 구성
1. **API 및 서비스** > **OAuth 동의 화면** 선택
2. **User Type**: 외부 선택
3. **앱 정보** 입력:
   - 앱 이름: `Blog API`
   - 사용자 지원 이메일: 본인 이메일
   - 개발자 연락처 정보: 본인 이메일
4. **범위 추가**:
   - `userinfo.email`
   - `userinfo.profile`
5. 저장 후 계속

#### 4. OAuth 2.0 Client ID 생성
1. **API 및 서비스** > **사용자 인증 정보** 선택
2. **사용자 인증 정보 만들기** > **OAuth 클라이언트 ID** 선택
3. **애플리케이션 유형**: 웹 애플리케이션
4. **이름**: `Blog API Web Client`
5. **승인된 리디렉션 URI** 추가:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
   **프로덕션 환경 추가 시**:
   ```
   https://api.yourdomain.com/login/oauth2/code/google
   ```
6. **만들기** 클릭
7. **Client ID**와 **Client Secret** 복사하여 `.env` 파일에 저장

#### 5. .env 파일 업데이트
```env
OAUTH_GOOGLE_CLIENT_ID=123456789-abcdefg.apps.googleusercontent.com
OAUTH_GOOGLE_CLIENT_SECRET=GOCSPX-xxxxxxxxxxxxx
```

---

### Kakao OAuth2 설정

#### 1. Kakao Developers 접속
https://developers.kakao.com/

#### 2. 애플리케이션 추가
1. **내 애플리케이션** > **애플리케이션 추가하기**
2. **앱 이름**: `Blog API`
3. **사업자명**: 개인 또는 회사명
4. 저장

#### 3. 앱 키 확인
1. 생성한 앱 선택
2. **앱 설정** > **앱 키**
3. **REST API 키** 복사

#### 4. 플랫폼 설정
1. **앱 설정** > **플랫폼** > **Web 플랫폼 등록**
2. **사이트 도메인**: `http://localhost:8080`

#### 5. Redirect URI 설정
1. **제품 설정** > **카카오 로그인** 활성화
2. **Redirect URI** 등록:
   ```
   http://localhost:8080/login/oauth2/code/kakao
   ```
   **프로덕션 환경 추가 시**:
   ```
   https://api.yourdomain.com/login/oauth2/code/kakao
   ```

#### 6. 동의항목 설정
1. **제품 설정** > **카카오 로그인** > **동의항목**
2. 다음 항목을 **필수 동의**로 설정:
   - **닉네임** (profile_nickname)
   - **카카오계정(이메일)** (account_email)
3. 저장

#### 7. 보안 설정 (선택사항, 권장)
1. **제품 설정** > **카카오 로그인** > **보안**
2. **Client Secret** 발급
3. **활성화 상태**로 변경

#### 8. .env 파일 업데이트
```env
OAUTH_KAKAO_CLIENT_ID=your-rest-api-key
OAUTH_KAKAO_CLIENT_SECRET=your-client-secret  # Client Secret 사용 시
```

---

## 🗄️ 데이터베이스 설정

### MySQL Docker 실행

#### docker-compose.yml (이미 존재)
```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: blog-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "${MYSQL_PORT}:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
```

#### MySQL 실행
```bash
docker-compose up -d mysql
```

#### 연결 확인
```bash
# Docker 컨테이너 내부 접속
docker exec -it blog-mysql mysql -u app -p

# 비밀번호 입력 후
mysql> SHOW DATABASES;
mysql> USE blog;
mysql> SHOW TABLES;
```

---

## 💾 Redis 설정

### Redis Docker 실행

#### docker-compose.yml (이미 존재)
```yaml
services:
  redis:
    image: redis:7-alpine
    container_name: blog-redis
    ports:
      - "${REDIS_PORT}:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
```

#### Redis 실행
```bash
docker-compose up -d redis
```

#### 연결 확인
```bash
# Redis CLI 접속
docker exec -it blog-redis redis-cli

# Redis 명령어
127.0.0.1:6379> PING
PONG
127.0.0.1:6379> KEYS *
(empty array)
```

---

## 🚀 실행 방법

### 1. Docker 컨테이너 실행
```bash
# MySQL + Redis 동시 실행
docker-compose up -d

# 컨테이너 상태 확인
docker ps

# 로그 확인
docker-compose logs -f
```

### 2. 애플리케이션 실행

#### Gradle 사용
```bash
# 빌드 (테스트 제외)
./gradlew build -x test

# 실행
./gradlew bootRun
```

#### IDE 사용 (IntelliJ IDEA)
1. `BlogApiApplication.java` 파일 열기
2. `main` 메서드 옆의 실행 버튼 클릭
3. 또는 `Shift + F10`

### 3. 실행 확인

#### 헬스체크
```bash
curl http://localhost:8080/health
```

**응답**:
```json
{
  "status": "UP",
  "version": "1.0.0",
  "buildTime": "2025-03-01T10:00:00Z",
  "timestamp": "2025-03-01T15:30:45.123Z"
}
```

#### Swagger UI 접속
```
http://localhost:8080/swagger-ui/index.html
```

---

## 🧪 테스트

### OAuth2 로그인 테스트

#### Google 로그인
1. 브라우저에서 접속:
   ```
   http://localhost:8080/oauth2/authorization/google
   ```
2. Google 계정으로 로그인
3. 동의 화면에서 승인
4. 리다이렉트 URL로 이동 (Access Token, Refresh Token 포함)

#### Kakao 로그인
1. 브라우저에서 접속:
   ```
   http://localhost:8080/oauth2/authorization/kakao
   ```
2. Kakao 계정으로 로그인
3. 동의 화면에서 승인
4. 리다이렉트 URL로 이동 (Access Token, Refresh Token 포함)

### 일반 로그인 테스트

#### cURL 사용
```bash
# 회원가입
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "nickname": "테스트유저"
  }'

# 로그인
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# 인증이 필요한 API 호출 (예시)
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 🔧 트러블슈팅

### 1. JWT Secret 길이 오류
**에러**: `The specified key byte array is X bits which is not secure enough`

**해결**:
```bash
# .env 파일에서 JWT_SECRET을 32바이트 이상으로 변경
JWT_SECRET=$(openssl rand -base64 32)
```

### 2. MySQL 연결 실패
**에러**: `Communications link failure`

**해결**:
```bash
# Docker 컨테이너 확인
docker ps | grep mysql

# 컨테이너 재시작
docker-compose restart mysql

# 로그 확인
docker logs blog-mysql
```

### 3. Redis 연결 실패
**에러**: `Unable to connect to Redis`

**해결**:
```bash
# Docker 컨테이너 확인
docker ps | grep redis

# 컨테이너 재시작
docker-compose restart redis

# 연결 테스트
docker exec -it blog-redis redis-cli ping
```

### 4. OAuth2 리다이렉트 오류
**에러**: `redirect_uri_mismatch`

**해결**:
- Google/Kakao Developers Console에서 Redirect URI 확인
- 정확한 URI 입력:
  - Google: `http://localhost:8080/login/oauth2/code/google`
  - Kakao: `http://localhost:8080/login/oauth2/code/kakao`

### 5. 포트 충돌
**에러**: `Port 8080 is already in use`

**해결**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /F /PID <PID>

# Mac/Linux
lsof -i :8080
kill -9 <PID>

# 또는 .env에서 포트 변경
APP_PORT=8081
```

---

## 📚 참고 문서

### OAuth2
- [Google OAuth2 문서](https://developers.google.com/identity/protocols/oauth2)
- [Kakao OAuth2 문서](https://developers.kakao.com/docs/latest/ko/kakaologin/common)
- [Spring Security OAuth2 문서](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)

### Spring Boot
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)

### 기타
- [JWT.io](https://jwt.io/) - JWT 디버깅
- [Swagger/OpenAPI](https://swagger.io/specification/)
- [Flyway](https://flywaydb.org/documentation/)

---

## ✅ 설정 완료 체크리스트

프로젝트를 실행하기 전에 다음 항목을 확인하세요:

### 환경 설정
- [ ] Java 21 설치 확인 (`java -version`)
- [ ] Docker 설치 확인 (`docker --version`)
- [ ] Docker Compose 설치 확인 (`docker-compose --version`)

### 환경변수
- [ ] `.env` 파일 생성 완료
- [ ] `JWT_SECRET` 32바이트 이상으로 설정
- [ ] MySQL 비밀번호 설정
- [ ] OAuth2 Client ID/Secret 설정

### Docker 컨테이너
- [ ] MySQL 컨테이너 실행 중
- [ ] Redis 컨테이너 실행 중
- [ ] 컨테이너 헬스체크 통과

### OAuth2 설정
- [ ] Google Cloud Console에서 OAuth2 Client 생성
- [ ] Google Redirect URI 등록
- [ ] Kakao Developers에서 앱 생성
- [ ] Kakao Redirect URI 등록
- [ ] Kakao 동의항목 설정 (이메일, 닉네임)

### 실행 확인
- [ ] 애플리케이션 정상 실행
- [ ] `/health` 엔드포인트 200 OK
- [ ] Swagger UI 접속 가능
- [ ] Google 로그인 테스트 성공
- [ ] Kakao 로그인 테스트 성공

---

## 🔒 보안 주의사항

### 절대 커밋하지 말 것
- `.env` 파일 (실제 환경변수)
- OAuth2 Client Secret
- JWT Secret Key
- 데이터베이스 비밀번호

### .gitignore 확인
```gitignore
.env
*.log
target/
build/
.idea/
.vscode/
```

### 프로덕션 배포 시
- [ ] JWT_SECRET 강력한 키로 변경
- [ ] 데이터베이스 비밀번호 변경
- [ ] OAuth2 Redirect URI를 프로덕션 도메인으로 변경
- [ ] HTTPS 적용
- [ ] CORS 설정 확인
- [ ] Rate Limiting 적용 고려
