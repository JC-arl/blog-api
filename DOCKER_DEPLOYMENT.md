# Docker 배포 가이드

이 문서는 Blog API를 Docker와 Docker Compose를 사용하여 배포하는 방법을 설명합니다.

## 목차
- [사전 요구사항](#사전-요구사항)
- [로컬 환경에서 실행](#로컬-환경에서-실행)
- [프로덕션 서버 배포](#프로덕션-서버-배포)
- [환경변수 설정](#환경변수-설정)
- [유용한 Docker 명령어](#유용한-docker-명령어)
- [트러블슈팅](#트러블슈팅)

---

## 사전 요구사항

- Docker 20.10 이상
- Docker Compose 2.0 이상
- Firebase Service Account JSON 파일

---

## 로컬 환경에서 실행

### 1. 환경변수 설정

```bash
# .env.example을 복사하여 .env 파일 생성
cp .env.example .env

# .env 파일을 편집하여 필요한 값 설정
# 특히 다음 항목들을 반드시 변경하세요:
# - MYSQL_ROOT_PASSWORD
# - MYSQL_PASSWORD
# - JWT_SECRET
# - Firebase 관련 설정
```

### 2. Firebase Service Account 설정

```bash
# Firebase Console에서 다운로드한 서비스 계정 JSON 파일을
# 다음 경로에 저장하세요:
src/main/resources/firebase-service-account.json
```

### 3. Docker Compose로 실행

```bash
# 모든 서비스(MySQL, Redis, App) 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 특정 서비스만 재시작
docker-compose restart app
```

### 4. 서비스 확인

```bash
# 헬스 체크
curl http://localhost:8080/health

# Swagger UI 접속
# 브라우저에서 http://localhost:8080/swagger-ui.html 열기
```

### 5. 서비스 중지

```bash
# 모든 서비스 중지 (데이터 보존)
docker-compose stop

# 모든 서비스 중지 및 컨테이너 삭제 (데이터 보존)
docker-compose down

# 모든 서비스 중지, 컨테이너 삭제, 볼륨 삭제 (데이터 삭제)
docker-compose down -v
```

---

## 프로덕션 서버 배포

### 1. 서버 준비

```bash
# Docker 설치 (Ubuntu 예시)
sudo apt-get update
sudo apt-get install -y docker.io docker-compose

# Docker 서비스 시작
sudo systemctl start docker
sudo systemctl enable docker

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER
```

### 2. 프로젝트 클론 및 설정

```bash
# 프로젝트 클론
git clone https://github.com/your-repo/blog-api.git
cd blog-api

# 환경변수 설정
cp .env.example .env
nano .env  # 프로덕션 설정으로 변경

# 중요: 다음 항목들을 프로덕션 값으로 변경하세요
# - SPRING_PROFILES_ACTIVE=prod
# - FRONTEND_URL=https://yourdomain.com
# - BACKEND_URL=https://api.yourdomain.com
# - MYSQL_HOST=localhost (Docker Compose 사용시 'mysql'로 자동 변경됨)
# - REDIS_HOST=localhost (Docker Compose 사용시 'redis'로 자동 변경됨)
# - JWT_SECRET (강력한 시크릿으로 변경)
# - 모든 비밀번호 변경
```

### 3. Firebase Service Account 업로드

```bash
# Firebase 서비스 계정 JSON 파일 업로드
# 서버에 파일을 업로드하고 올바른 위치에 배치
scp firebase-service-account.json user@server:/path/to/blog-api/src/main/resources/

# 또는 서버에서 직접 생성
nano src/main/resources/firebase-service-account.json
# JSON 내용 붙여넣기
```

### 4. Docker 이미지 빌드 및 실행

```bash
# 프로덕션 모드로 빌드 및 실행
docker-compose up -d --build

# 로그 확인
docker-compose logs -f

# 서비스 상태 확인
docker-compose ps
```

### 5. Nginx 리버스 프록시 설정 (선택사항)

```nginx
# /etc/nginx/sites-available/blog-api
server {
    listen 80;
    server_name api.yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
# Nginx 설정 활성화
sudo ln -s /etc/nginx/sites-available/blog-api /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx

# SSL 인증서 설정 (Let's Encrypt)
sudo apt-get install certbot python3-certbot-nginx
sudo certbot --nginx -d api.yourdomain.com
```

---

## 환경변수 설정

### 필수 환경변수

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `SPRING_PROFILES_ACTIVE` | Spring 프로필 (local/prod) | `prod` |
| `MYSQL_ROOT_PASSWORD` | MySQL root 비밀번호 | `strong_password` |
| `MYSQL_DATABASE` | 데이터베이스 이름 | `blog` |
| `MYSQL_USER` | 애플리케이션 DB 사용자 | `app` |
| `MYSQL_PASSWORD` | 애플리케이션 DB 비밀번호 | `app_password` |
| `JWT_SECRET` | JWT 서명 키 (최소 32바이트) | `your-secret-key` |
| `FIREBASE_PROJECT_ID` | Firebase 프로젝트 ID | `my-project-id` |

### Docker Compose 환경에서의 자동 설정

Docker Compose를 사용할 때, 다음 환경변수들은 자동으로 컨테이너 서비스 이름으로 오버라이드됩니다:

- `MYSQL_HOST` → `mysql`
- `REDIS_HOST` → `redis`

---

## 유용한 Docker 명령어

### 컨테이너 관리

```bash
# 실행 중인 컨테이너 확인
docker-compose ps

# 컨테이너 로그 확인
docker-compose logs -f app

# 컨테이너 재시작
docker-compose restart app

# 컨테이너 내부 접속
docker-compose exec app sh
```

### 데이터베이스 관리

```bash
# MySQL 컨테이너 접속
docker-compose exec mysql mysql -u root -p

# MySQL 데이터베이스 백업
docker-compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} blog > backup.sql

# MySQL 데이터베이스 복원
docker-compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} blog < backup.sql

# Redis 컨테이너 접속
docker-compose exec redis redis-cli
```

### 이미지 관리

```bash
# 이미지 빌드
docker-compose build

# 이미지 빌드 (캐시 무시)
docker-compose build --no-cache

# 사용하지 않는 이미지 삭제
docker image prune -a
```

### 볼륨 관리

```bash
# 볼륨 목록 확인
docker volume ls

# 볼륨 상세 정보
docker volume inspect blog-api_mysql_data

# 사용하지 않는 볼륨 삭제
docker volume prune
```

---

## 트러블슈팅

### 1. 애플리케이션이 시작되지 않음

```bash
# 로그 확인
docker-compose logs app

# MySQL/Redis가 준비될 때까지 대기하는지 확인
# docker-compose.yml에 healthcheck와 depends_on 설정이 있는지 확인
```

### 2. 데이터베이스 연결 오류

```bash
# MySQL 컨테이너 상태 확인
docker-compose ps mysql

# MySQL 로그 확인
docker-compose logs mysql

# 네트워크 확인
docker network ls
docker network inspect blog-api_blog-network
```

### 3. 포트 충돌

```bash
# 포트 사용 중인 프로세스 확인
sudo lsof -i :8080
sudo lsof -i :3306

# .env 파일에서 포트 변경
APP_PORT=8081
MYSQL_PORT=3307
```

### 4. 메모리 부족

```bash
# Docker 메모리 사용량 확인
docker stats

# docker-compose.yml에 메모리 제한 추가
services:
  app:
    deploy:
      resources:
        limits:
          memory: 1G
```

### 5. Flyway 마이그레이션 실패

```bash
# 애플리케이션 로그에서 Flyway 오류 확인
docker-compose logs app | grep -i flyway

# 필요시 데이터베이스 초기화
docker-compose down -v  # 주의: 모든 데이터 삭제됨
docker-compose up -d
```

### 6. Firebase 인증 오류

```bash
# Firebase 서비스 계정 파일 경로 확인
docker-compose exec app ls -la /app/firebase-service-account.json

# 환경변수 확인
docker-compose exec app env | grep FIREBASE
```

---

## 보안 권장사항

1. **비밀번호 관리**
   - 프로덕션 환경에서는 강력한 비밀번호 사용
   - `.env` 파일을 Git에 커밋하지 않기
   - AWS Secrets Manager, HashiCorp Vault 등 사용 고려

2. **네트워크 보안**
   - 필요한 포트만 외부에 노출
   - 방화벽 설정 (ufw, firewalld 등)
   - SSL/TLS 인증서 사용

3. **컨테이너 보안**
   - 정기적인 이미지 업데이트
   - 최소 권한 원칙 (non-root user)
   - 보안 스캔 도구 사용 (Trivy, Clair 등)

4. **데이터 백업**
   - 정기적인 데이터베이스 백업
   - 백업 자동화 스크립트 작성
   - 백업 복원 테스트

---

## CI/CD (GitHub Actions 예시)

다음 단계로 GitHub Actions를 설정하여 자동 배포를 구성할 수 있습니다.
자세한 내용은 `.github/workflows/` 디렉토리를 참조하세요.

기본적인 CI/CD 파이프라인:
1. 코드 푸시
2. 테스트 실행
3. Docker 이미지 빌드
4. Docker Hub/GitHub Container Registry에 푸시
5. 서버에 배포

---

## 참고 자료

- [Docker 공식 문서](https://docs.docker.com/)
- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [Spring Boot Docker 가이드](https://spring.io/guides/topicals/spring-boot-docker/)
- [MySQL Docker 가이드](https://hub.docker.com/_/mysql)
- [Redis Docker 가이드](https://hub.docker.com/_/redis)
