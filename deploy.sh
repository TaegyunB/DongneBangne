#!/bin/bash

# 동네 앱 자동 배포 스크립트 (시스템 Nginx 리버스 프록시)
# Frontend: 시스템 Nginx에서 정적 파일 + API 프록시
# Backend: 8080 포트에서 API 서버

set -e  # 에러 발생 시 스크립트 중단

echo "🚀 동네 앱 시스템 Nginx 리버스 프록시 배포 시작"
echo "배포 시간: $(date)"

# 환경 변수 설정 (Pipeline에서 전달받거나 기본값 사용)
BACKEND_IMAGE=${BACKEND_IMAGE:-"taegyunb99/dongnae-backend:latest"}
FRONTEND_IMAGE=${FRONTEND_IMAGE:-"taegyunb99/dongnae-frontend:latest"}

BACKEND_CONTAINER="dongnae"
BACKUP_BACKEND_CONTAINER="dongnae-backup"
MYSQL_CONTAINER="dongnae-mysql"

echo "📦 배포할 이미지:"
echo "  Backend:  $BACKEND_IMAGE"
echo "  Frontend: $FRONTEND_IMAGE (시스템 Nginx로 서빙)"

# Docker 명령어 함수
docker_cmd() {
    docker "$@"
}

# 1. 최신 이미지 다운로드
echo "📦 최신 Docker 이미지 다운로드 중..."
docker_cmd pull $BACKEND_IMAGE
docker_cmd pull $FRONTEND_IMAGE

# 2. Frontend 파일 업데이트 (시스템 Nginx 사용)
echo "🌐 Frontend 파일 업데이트 중..."

# 기존 Frontend 컨테이너가 있다면 정리 (더 이상 필요 없음)
if docker_cmd ps -q -f name=dongnae-frontend > /dev/null 2>&1; then
    echo "🧹 기존 Frontend 컨테이너 제거 중..."
    docker_cmd stop dongnae-frontend || true
    docker_cmd rm dongnae-frontend || true
fi

# Frontend 정적 파일 디렉토리 생성
sudo mkdir -p /var/www/dongnae-frontend
sudo chown -R $USER:$USER /var/www/dongnae-frontend

# Frontend 파일만 복사, 컨테이너 생성 안 함
echo "📁 Frontend 정적 파일 업데이트 중..."
docker_cmd create --name temp-frontend $FRONTEND_IMAGE
docker_cmd cp temp-frontend:/usr/share/nginx/html/. /var/www/dongnae-frontend/
docker_cmd rm temp-frontend

echo "✅ Frontend 파일 업데이트 완료"

# 3. Backend 배포
echo "🔧 Backend 배포 시작..."
if docker_cmd ps -q -f name=^${BACKEND_CONTAINER}$ > /dev/null 2>&1; then
    echo "💾 현재 Backend 서비스 백업 중..."
    docker_cmd rm -f $BACKUP_BACKEND_CONTAINER > /dev/null 2>&1 || true
    docker_cmd stop $BACKEND_CONTAINER || true
    docker_cmd rename $BACKEND_CONTAINER $BACKUP_BACKEND_CONTAINER || true
    echo "✅ 기존 Backend 컨테이너 백업 완료 (포트 8080 해제됨)"
else
    echo "ℹ️ 실행 중인 Backend 컨테이너가 없음"
fi

# Backend 포트 정리
echo "🔍 포트 8080 사용 상태 확인 중..."
if netstat -tlnp 2>/dev/null | grep -q ":8080 " || lsof -i :8080 2>/dev/null; then
    echo "⚠️ 포트 8080이 여전히 사용 중 - 추가 정리 진행"
    docker_cmd ps -q --filter "publish=8080" | xargs -r docker_cmd stop
    sleep 3
else
    echo "✅ 포트 8080 사용 가능"
fi

# 4. 데이터베이스 연결 확인
echo "🗄️ 데이터베이스 연결 확인 중..."
if ! docker_cmd ps | grep -q $MYSQL_CONTAINER; then
    echo "⚠️ MySQL 컨테이너가 실행되지 않음. docker-compose로 시작 중..."
    docker-compose up -d mysql
    echo "⏳ MySQL 완전 시작 대기 중..."
    sleep 45
else
    echo "✅ MySQL 컨테이너 실행 중"
fi

# MySQL 준비 상태 확인
echo "⏳ MySQL 준비 상태 확인 중..."
MYSQL_READY=0
MYSQL_RETRY=0
MAX_MYSQL_RETRIES=12

while [ $MYSQL_RETRY -lt $MAX_MYSQL_RETRIES ] && [ $MYSQL_READY -eq 0 ]; do
    if docker_cmd exec $MYSQL_CONTAINER mysqladmin ping -h localhost -u dongnaeuser -pdongnaepass --silent > /dev/null 2>&1; then
        echo "✅ MySQL 준비 완료"
        MYSQL_READY=1
    else
        echo "⏳ MySQL 준비 대기 중... ($((MYSQL_RETRY+1))/$MAX_MYSQL_RETRIES)"
        sleep 5
        MYSQL_RETRY=$((MYSQL_RETRY+1))
    fi
done

if [ $MYSQL_READY -eq 0 ]; then
    echo "❌ MySQL 준비 시간 초과"
    exit 1
fi

# 5. 새 Backend 컨테이너 시작
echo "🔄 새로운 Backend 서비스 시작 중..."

# MySQL 네트워크 확인
MYSQL_NETWORK=$(docker_cmd inspect $MYSQL_CONTAINER --format='{{range $net, $config := .NetworkSettings.Networks}}{{$net}}{{end}}' | head -1)
echo "📋 MySQL 네트워크: $MYSQL_NETWORK"

# Backend 컨테이너 시작
echo "🚀 새 Backend 컨테이너 시작 중..."
docker_cmd run -d \
  --name $BACKEND_CONTAINER \
  --network $MYSQL_NETWORK \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://dongnae-mysql:3306/dongnae?serverTimezone=UTC&characterEncoding=UTF-8" \
  -e SPRING_DATASOURCE_USERNAME="dongnaeuser" \
  -e SPRING_DATASOURCE_PASSWORD="dongnaepass" \
  -e SPRING_DATASOURCE_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver" \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO="update" \
  -e SPRING_JPA_DATABASE_PLATFORM="org.hibernate.dialect.MySQLDialect" \
  -e SPRING_SECURITY_USER_NAME="admin" \
  -e SPRING_SECURITY_USER_PASSWORD="dongnae2024!" \
  --restart unless-stopped \
  $BACKEND_IMAGE

# 6. Backend 헬스체크
echo "🏥 Backend 서비스 헬스체크 중..."
BACKEND_RETRY=0
MAX_BACKEND_RETRIES=15

while [ $BACKEND_RETRY -lt $MAX_BACKEND_RETRIES ]; do
    if ! docker_cmd ps -q -f name=^${BACKEND_CONTAINER}$ > /dev/null 2>&1; then
        echo "❌ Backend 컨테이너가 중지됨!"
        break
    fi

    if docker_cmd exec $BACKEND_CONTAINER curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Backend 서비스 정상 동작 확인!"
        break
    elif docker_cmd exec $BACKEND_CONTAINER curl -f -s http://localhost:8080 > /dev/null 2>&1; then
        echo "✅ Backend 서비스 정상 동작 확인!"
        break
    else
        echo "⏳ Backend 서비스 시작 대기 중... ($((BACKEND_RETRY+1))/$MAX_BACKEND_RETRIES)"
        sleep 10
        BACKEND_RETRY=$((BACKEND_RETRY+1))
    fi
done

# 7. 시스템 Nginx 재시작 (Frontend 파일 반영)
echo "🔄 시스템 Nginx 재시작 중..."
sudo systemctl reload nginx || echo "⚠️ Nginx 재시작 실패 - 수동 확인 필요"

# 8. Frontend 헬스체크 (시스템 Nginx)
echo "🌐 시스템 Nginx Frontend 확인 중..."
FRONTEND_READY=0
FRONTEND_RETRY=0
MAX_FRONTEND_RETRIES=5

while [ $FRONTEND_RETRY -lt $MAX_FRONTEND_RETRIES ] && [ $FRONTEND_READY -eq 0 ]; do
    if curl -f -s http://localhost/ > /dev/null 2>&1; then
        echo "✅ Frontend 서비스 정상 확인!"
        FRONTEND_READY=1
    else
        echo "⏳ Frontend 서비스 확인 중... ($((FRONTEND_RETRY+1))/$MAX_FRONTEND_RETRIES)"
        sleep 5
        FRONTEND_RETRY=$((FRONTEND_RETRY+1))
    fi
done

# 9. 리버스 프록시 연결 테스트
echo "🔗 리버스 프록시 연결 테스트 중..."
PROXY_RETRY=0
MAX_PROXY_RETRIES=5

while [ $PROXY_RETRY -lt $MAX_PROXY_RETRIES ]; do
    # 시스템에서 Backend API 직접 호출 테스트
    if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ 로컬 Backend API 접근 확인!"
        break
    else
        echo "⏳ Backend API 연결 대기 중... ($((PROXY_RETRY+1))/$MAX_PROXY_RETRIES)"
        sleep 5
        PROXY_RETRY=$((PROXY_RETRY+1))
    fi
done

# 10. 배포 결과 처리
if [ $BACKEND_RETRY -eq $MAX_BACKEND_RETRIES ]; then
    echo "❌ Backend 서비스 시작 실패! 롤백 진행..."
    
    # 실패한 Backend 컨테이너 삭제
    docker_cmd rm -f $BACKEND_CONTAINER 2>/dev/null || true
    
    # 백업 Backend 컨테이너로 복원
    if docker_cmd ps -a -q -f name=^${BACKUP_BACKEND_CONTAINER}$ > /dev/null 2>&1; then
        echo "🔄 Backend 이전 버전으로 복원..."
        docker_cmd rename $BACKUP_BACKEND_CONTAINER $BACKEND_CONTAINER
        docker_cmd start $BACKEND_CONTAINER
        echo "✅ Backend 롤백 완료"
    fi
    
    echo "❌ 배포 실패 - Backend 롤백됨"
    exit 1
else
    echo "🎉 풀스택 배포 성공!"
    
    # 백업 컨테이너 정리
    docker_cmd rm -f $BACKUP_BACKEND_CONTAINER 2>/dev/null || true
    
    # 11. 외부 접근 테스트
    echo "🌐 외부 접근성 테스트 중..."
    
    # Frontend (메인 페이지) 테스트
    if curl -f -s http://i13a708.p.ssafy.io > /dev/null 2>&1; then
        echo "✅ Frontend 외부 접근 가능 (http://i13a708.p.ssafy.io)"
    else
        echo "⚠️ Frontend 외부 접근 실패"
    fi
    
    # Backend API 직접 접근 테스트
    if curl -f -s http://i13a708.p.ssafy.io:8080 > /dev/null 2>&1; then
        echo "✅ Backend API 직접 접근 가능 (http://i13a708.p.ssafy.io:8080)"
    else
        echo "⚠️ Backend API 직접 접근 실패"
    fi
    
    # Nginx를 통한 API 프록시 테스트
    if curl -f -s http://i13a708.p.ssafy.io/api/health > /dev/null 2>&1; then
        echo "✅ Nginx 프록시를 통한 API 접근 가능 (http://i13a708.p.ssafy.io/api/*)"
    else
        echo "ℹ️ Nginx API 프록시 경로 확인 필요 (/api/* → Backend)"
    fi
    
    echo "✅ 동네 앱 시스템 Nginx 리버스 프록시 배포 완료!"
    echo ""
    echo "🌐 서비스 접속 정보:"
    echo "  Frontend: http://i13a708.p.ssafy.io (시스템 Nginx + Vue.js)"
    echo "  Backend:  http://i13a708.p.ssafy.io:8080 (Spring Boot API)"
    echo "  API via Proxy: http://i13a708.p.ssafy.io/api/* (Nginx → Backend)"
    echo ""
    
    # 최종 상태 확인
    echo "📋 최종 Backend 컨테이너 상태:"
    docker_cmd ps | grep dongnae || echo "Backend 컨테이너 확인 필요"
    
    echo "📁 Frontend 파일 위치: /var/www/dongnae-frontend/"
    ls -la /var/www/dongnae-frontend/ | head -5 || echo "Frontend 파일 확인 필요"
fi

echo "배포 완료 시간: $(date)"
##