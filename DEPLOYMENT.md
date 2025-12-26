# 배포 가이드

이 문서는 Blog API를 프로덕션 서버에 배포하는 방법을 설명합니다.

## 배포 아키텍처

```
개발자 로컬 → GitHub Push → GitHub Actions (빌드) → GHCR (이미지 저장)
                                                          ↓
                                              서버 (pull & 실행)
```

## 사전 준비

### 1. GitHub Container Registry (GHCR) 설정

GitHub 저장소에서 자동으로 GHCR을 사용할 수 있습니다. 별도 설정 불필요.

### 2. 서버 요구사항

- Docker 및 Docker Compose 설치
- 포트 80, 3306, 6379 오픈
- `.env` 파일 준비 (환경변수 설정)

## 로컬 개발

로컬에서 개발할 때는 직접 빌드하여 실행:

```bash
# 개발용 docker-compose 사용
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build

# 로그 확인
docker compose logs -f app

# 중지
docker compose down
```

## 프로덕션 배포

### 1단계: GitHub에 코드 푸시

```bash
# main 또는 blog 브랜치에 푸시하면 자동으로 이미지 빌드
git add .
git commit -m "Your commit message"
git push origin main
```

### 2단계: GitHub Actions 확인

- GitHub 저장소의 **Actions** 탭에서 빌드 진행 상황 확인
- 빌드가 완료되면 `ghcr.io/YOUR_USERNAME/blog-api:latest` 이미지가 생성됩니다

### 3단계: 서버에서 배포

#### 3-1. .env 파일 설정

서버의 `.env` 파일에서 필수 환경변수 설정:

```bash
# .env 파일

# Docker 이미지 설정
DOCKER_IMAGE=ghcr.io/YOUR_GITHUB_USERNAME/blog-api:latest

# 서버 배포 주소 (서버의 공인 IP 또는 도메인)
APP_PORT=80
PUBLISHED_URL=113.198.66.68

# URL 설정 (PUBLISHED_URL을 반영)
FRONTEND_URL=http://113.198.66.68
BACKEND_URL=http://113.198.66.68
CORS_ALLOWED_ORIGINS=http://113.198.66.68,http://localhost:3000,http://localhost:8080

# Spring 환경
SPRING_PROFILES_ACTIVE=prod

# 데이터베이스 설정
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=blog
MYSQL_USER=app
MYSQL_PASSWORD=your_secure_password

# JWT 설정
JWT_SECRET=your-super-secret-jwt-key-at-least-32-bytes-long

# Firebase 설정
FIREBASE_PROJECT_ID=your-firebase-project-id
# Firebase 경로는 docker-compose.yml의 기본값 사용 (주석 처리 유지)

# Kakao 설정
KAKAO_REST_API_KEY=your-kakao-rest-api-key
```

**중요**:
- `PUBLISHED_URL`에 서버의 공인 IP 주소나 도메인을 설정하세요
- `FRONTEND_URL`, `BACKEND_URL`, `CORS_ALLOWED_ORIGINS`에 자동으로 반영됩니다
- 보안을 위해 비밀번호와 키는 반드시 변경하세요

#### 3-2. GHCR 로그인 (최초 1회)

프라이빗 저장소인 경우 GHCR 로그인 필요:

```bash
# GitHub Personal Access Token 생성 (Settings → Developer settings → Personal access tokens)
# 권한: read:packages

echo YOUR_GITHUB_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

퍼블릭 저장소인 경우 로그인 불필요.

#### 3-3. Firebase 서비스 계정 키 배치

**중요:** Docker 이미지에는 민감한 정보가 포함되지 않으므로, Firebase 키를 서버에 별도로 배치해야 합니다.

```bash
# secrets 디렉토리 생성
mkdir -p ~/blog-api/secrets

# Firebase 서비스 계정 키 파일 업로드
# 방법 1: SCP로 로컬에서 서버로 전송
scp /path/to/firebase-service-account.json user@113.198.66.68:~/blog-api/secrets/

# 방법 2: 서버에서 직접 생성
nano ~/blog-api/secrets/firebase-service-account.json
# Firebase Console에서 다운로드한 JSON 내용 붙여넣기
# Ctrl+O (저장), Enter, Ctrl+X (종료)

# 권한 설정 (읽기 전용, 소유자만)
chmod 600 ~/blog-api/secrets/firebase-service-account.json

# 파일 확인
ls -la ~/blog-api/secrets/
```

**파일 구조 확인:**
```
~/blog-api/
├── docker-compose.yml
├── .env
└── secrets/
    └── firebase-service-account.json  ← 이 파일이 있어야 함
```

#### 3-4. Firebase 승인된 도메인 설정

**중요:** Google 로그인 등 Firebase Auth를 사용하려면 서버 IP/도메인을 Firebase Console에 추가해야 합니다.

**설정 방법:**

1. [Firebase Console](https://console.firebase.google.com/) 접속
2. 프로젝트 선택 (`wsd-blogapi` 등)
3. **Authentication** → **Settings** → **Authorized domains**
4. **Add domain** 버튼 클릭
5. 서버 IP 추가: `113.198.66.68` (또는 도메인)
6. 저장

**추가할 도메인 목록:**
- `localhost` (기본 포함)
- `113.198.66.68` (프로덕션 서버)
- `yourdomain.com` (커스텀 도메인 사용 시)

**Google OAuth 사용 시 추가 설정 (선택사항):**

Google Cloud Console에서도 설정이 필요할 수 있습니다:

1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. Firebase 연결된 프로젝트 선택
3. **APIs & Services** → **Credentials**
4. OAuth 2.0 클라이언트 ID 선택
5. **승인된 리디렉션 URI** 추가:
   - `http://113.198.66.68/__/auth/handler`
   - `http://113.198.66.68`

#### 3-5. 컨테이너 실행

```bash
# 최신 이미지 pull 및 실행
docker compose pull
docker compose up -d

# 로그 확인 (실시간)
docker compose logs -f app

# 상태 확인
docker compose ps
# 출력 예시:
# NAME         STATUS          PORTS
# blog-api     Up (healthy)    0.0.0.0:80->80/tcp
# blog-mysql   Up (healthy)    0.0.0.0:3306->3306/tcp
# blog-redis   Up (healthy)    0.0.0.0:6379->6379/tcp

# Health 체크
curl http://113.198.66.68/health
```

**배포 확인:**

1. **Health Check**: `curl http://113.198.66.68/health`
2. **Swagger UI**: `http://113.198.66.68/swagger-ui.html`
3. **API 테스트**: Swagger에서 로그인 테스트
4. **Google 로그인**: 프론트엔드에서 Google 로그인 테스트

## 업데이트 배포

새로운 코드를 배포할 때:

```bash
# 1. GitHub에 푸시 (GitHub Actions가 자동으로 빌드)
git push origin main

# 2. GitHub Actions 빌드 완료 후, 서버에서 실행
docker compose pull      # 최신 이미지 다운로드
docker compose up -d     # 컨테이너 재시작
```

## 프론트엔드(React) 업데이트

프론트엔드 코드를 수정한 경우:

### 로컬 개발 (핫 리로드)

빠른 개발을 위해 React 개발 서버 사용:

```bash
cd login-app
npm install
npm start
# React 개발 서버가 localhost:3000 에서 실행 (핫 리로드 지원)
```

### 로컬에서 통합 테스트

React를 빌드하여 Spring Boot와 통합 테스트:

```bash
# React 빌드 및 static 폴더로 복사
./gradlew copyReactBuild

# Spring Boot 실행
./gradlew bootRun
```

### 프로덕션 배포

프론트엔드 변경사항은 백엔드와 동일한 프로세스로 배포됩니다:

```bash
# 1. 프론트엔드 코드 수정
# login-app/ 디렉토리의 React 코드 수정

# 2. 커밋 및 푸시
git add login-app/
git commit -m "Update frontend: 변경 내용"
git push origin main

# 3. GitHub Actions가 자동으로:
#    - Node.js 설치
#    - React 빌드 (npm install && npm run build)
#    - Spring Boot JAR에 포함
#    - Docker 이미지 빌드 및 GHCR에 푸시

# 4. 서버에서 배포
docker compose pull
docker compose up -d
```

**중요:**
- Docker 빌드 과정에서 React가 자동으로 빌드됩니다
- `login-app/build/` 디렉토리는 `.gitignore`에 포함되어 Git에 커밋되지 않습니다
- 프로덕션 빌드는 항상 GitHub Actions를 통해 수행됩니다

## 롤백

이전 버전으로 롤백하려면:

```bash
# 특정 SHA 태그로 롤백
export DOCKER_IMAGE=ghcr.io/YOUR_USERNAME/blog-api:main-abc123def

docker compose pull
docker compose up -d
```

## 버전 태깅

시맨틱 버전을 사용하여 릴리스:

```bash
# 태그 생성 및 푸시
git tag v1.0.0
git push origin v1.0.0

# GitHub Actions가 자동으로 빌드하여 다음 이미지 생성:
# - ghcr.io/YOUR_USERNAME/blog-api:latest
# - ghcr.io/YOUR_USERNAME/blog-api:v1.0.0
# - ghcr.io/YOUR_USERNAME/blog-api:1.0
# - ghcr.io/YOUR_USERNAME/blog-api:1
```

서버에서 특정 버전 사용:

```bash
export DOCKER_IMAGE=ghcr.io/YOUR_USERNAME/blog-api:v1.0.0
docker compose up -d
```

## 이미지 공개 설정

GHCR 이미지를 퍼블릭으로 만들기:

1. GitHub → Packages → blog-api 선택
2. Package settings → Change visibility → Public

퍼블릭으로 설정하면 `docker login` 없이도 이미지를 pull 할 수 있습니다.

## 모니터링

```bash
# 로그 실시간 확인
docker compose logs -f

# 특정 서비스만
docker compose logs -f app

# 컨테이너 리소스 사용량
docker stats

# 컨테이너 상태
docker compose ps
```

## 문제 해결

### 이미지를 pull 할 수 없는 경우

```bash
# GHCR 로그인 확인
docker login ghcr.io

# 이미지 존재 확인
docker pull ghcr.io/YOUR_USERNAME/blog-api:latest
```

### 컨테이너가 시작되지 않는 경우

```bash
# 로그 확인
docker compose logs app

# 환경변수 확인
docker compose config
```

### 데이터베이스 연결 실패

- `.env` 파일의 MySQL 환경변수 확인
- `SPRING_PROFILES_ACTIVE=prod` 설정 확인
- `MYSQL_HOST=mysql` (Docker 네트워크 이름 사용)

### Firebase 인증 오류

**에러: `Firebase: Error (auth/unauthorized-domain)`**

**원인:** Firebase Authorized domains에 서버 IP/도메인이 추가되지 않음

**해결:**
1. [Firebase Console](https://console.firebase.google.com/) 접속
2. Authentication → Settings → Authorized domains
3. `113.198.66.68` 추가 후 저장
4. 브라우저 캐시 삭제 (F12 → Application → Clear storage)
5. 재로그인 시도

**에러: `Firebase 서비스 계정 파일을 찾을 수 없습니다`**

**원인:** `secrets/firebase-service-account.json` 파일이 없음

**해결:**
```bash
# 파일 존재 확인
ls -la ~/blog-api/secrets/firebase-service-account.json

# 없다면 업로드
scp /path/to/firebase-service-account.json user@113.198.66.68:~/blog-api/secrets/

# 권한 설정
chmod 600 ~/blog-api/secrets/firebase-service-account.json

# 컨테이너 재시작
docker compose restart app
```

### GHCR Pull 권한 오류

**에러: `unauthorized: Head "https://ghcr.io/v2/.../manifests/latest": unauthorized`**

**원인:** Private 저장소이거나 GitHub Actions가 아직 실행되지 않음

**해결:**

1. **GitHub Actions 확인**
   ```bash
   # https://github.com/YOUR_USERNAME/blog-api/actions
   # 빌드가 완료되었는지 확인
   ```

2. **GHCR 로그인 (Private 저장소)**
   ```bash
   # GitHub Personal Access Token 생성 (read:packages 권한)
   echo YOUR_TOKEN | docker login ghcr.io -u YOUR_USERNAME --password-stdin
   docker compose pull
   ```

3. **이미지 Public 설정**
   ```bash
   # GitHub → Packages → blog-api → Package settings → Public
   ```

---

## 배포 체크리스트

프로덕션 배포 전 확인 사항:

### 로컬 준비

- [ ] 코드 변경사항 커밋 및 푸시
- [ ] GitHub Actions 빌드 성공 확인
- [ ] GHCR에 이미지 업로드 확인

### 서버 준비

- [ ] `.env` 파일 작성 완료
  - [ ] `DOCKER_IMAGE` 설정
  - [ ] `APP_PORT` 설정 (80 권장)
  - [ ] `PUBLISHED_URL` 설정
  - [ ] `MYSQL_*` 설정
  - [ ] `JWT_SECRET` 설정 (32자 이상)
  - [ ] `FIREBASE_PROJECT_ID` 설정
  - [ ] `KAKAO_REST_API_KEY` 설정

- [ ] `secrets/firebase-service-account.json` 업로드
  - [ ] 파일 권한 `600` 설정

- [ ] Docker 및 Docker Compose 설치
  - [ ] `docker --version` 확인
  - [ ] `docker compose version` 확인

### Firebase 설정

- [ ] Firebase Authorized domains에 서버 IP 추가
  - [ ] `113.198.66.68` (또는 도메인)

- [ ] Google OAuth (사용 시)
  - [ ] Google Cloud Console 리디렉션 URI 추가

### 배포 실행

- [ ] `docker compose pull` 성공
- [ ] `docker compose up -d` 성공
- [ ] 컨테이너 상태 확인 (`docker compose ps`)
  - [ ] `blog-api`: healthy
  - [ ] `blog-mysql`: healthy
  - [ ] `blog-redis`: healthy

### 배포 검증

- [ ] Health Check 응답: `curl http://113.198.66.68/health`
- [ ] Swagger UI 접속: `http://113.198.66.68/swagger-ui.html`
- [ ] 로그인 테스트 (Swagger)
  - [ ] 회원가입: `POST /auth/signup`
  - [ ] 로그인: `POST /auth/login`

- [ ] Google 로그인 테스트 (프론트엔드)
  - [ ] Firebase Auth 정상 작동

- [ ] API 기능 테스트
  - [ ] 게시글 목록 조회: `GET /posts`
  - [ ] 게시글 작성: `POST /posts` (인증 필요)

---

## CI/CD 파이프라인 커스터마이징

`.github/workflows/docker-publish.yml` 파일을 수정하여:

- 빌드 트리거 브랜치 변경
- 다중 플랫폼 빌드 (ARM64 추가)
- 자동 배포 추가
- 테스트 자동화

등을 설정할 수 있습니다.
