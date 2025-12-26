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

서버의 `.env` 파일에서 Docker 이미지 설정:

```bash
# .env 파일
DOCKER_IMAGE=ghcr.io/YOUR_GITHUB_USERNAME/blog-api:latest

# 나머지 환경변수 설정
SPRING_PROFILES_ACTIVE=prod
MYSQL_ROOT_PASSWORD=your_secure_password
# ... 기타 설정
```

#### 3-2. GHCR 로그인 (최초 1회)

프라이빗 저장소인 경우 GHCR 로그인 필요:

```bash
# GitHub Personal Access Token 생성 (Settings → Developer settings → Personal access tokens)
# 권한: read:packages

echo YOUR_GITHUB_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

퍼블릭 저장소인 경우 로그인 불필요.

#### 3-3. 컨테이너 실행

```bash
# 최신 이미지 pull 및 실행
docker compose pull
docker compose up -d

# 로그 확인
docker compose logs -f app

# 상태 확인
docker compose ps
```

## 업데이트 배포

새로운 코드를 배포할 때:

```bash
# 1. GitHub에 푸시 (GitHub Actions가 자동으로 빌드)
git push origin main

# 2. GitHub Actions 빌드 완료 후, 서버에서 실행
docker compose pull      # 최신 이미지 다운로드
docker compose up -d     # 컨테이너 재시작
```

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

## CI/CD 파이프라인 커스터마이징

`.github/workflows/docker-publish.yml` 파일을 수정하여:

- 빌드 트리거 브랜치 변경
- 다중 플랫폼 빌드 (ARM64 추가)
- 자동 배포 추가
- 테스트 자동화

등을 설정할 수 있습니다.
