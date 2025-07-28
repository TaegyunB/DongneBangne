#!/bin/bash

# 동네 앱 자동 배포 스크립트 (Frontend + Backend)
# 무중단 배포를 위한 블루-그린 배포 방식

set -e  # 에러 발생 시 스크립트 중단

echo "🚀 동네 앱 풀스택 자동 배포 시작"
echo "배포 시간: $(date)"

# 환경 변수 설정 (Pipeline에서 전달받거나 기본값 사용)
BACKEND_IMAGE=${BACKEND_IMAGE:-"taegyunb99/dongnae-backend:latest"}
FRONTEND_IMAGE=${FRONTEND_IMAGE:-"taegyunb99/dongnae-frontend:latest"}

BACKEND_CONTAINER="dongnae"
FRONTEND_CONTAINER="dongnae-frontend"
BACKUP_BACKEND_CONTAINER="dongnae-backup"
BACKUP_FRONTEND_CONTAINER="dongnae-frontend-backup"
MYSQL_CONTAINER="dongnae-mysql"

echo "📦 배포할 이미지:"
echo "  Backend:  $BACKEND_IMAGE"
echo "  Frontend: $FRONTEND_IMAGE"

# Docker 명령어 함수 (sudo 제거)
docker_cmd() {
    docker "$@"
}

# 1. 최신 이미지 다운로드
echo "📦 최신 Docker 이미지 다운로드 중..."
docker_cmd pull $BACKEND_IMAGE
docker_cmd pull $FRONTEND_IMAGE

# 2. Frontend 컨테이너 백업 및 배포
echo "🎨 Frontend 배포 시작..."
if docker_cmd ps -q -f name=^${FRONTEND_CONTAINER}$ > /dev/null 2>&1; then
    echo "💾 기존 Frontend 컨테이너 백업 중..."
    docker_cmd rm -f $BACKUP_FRONTEND_CONTAINER > /dev/null 2>&1 || true
    docker_cmd stop $FRONTEND_CONTAINER || true
    docker_cmd rename $FRONTEND_CONTAINER $BACKUP_FRONTEND_CONTAINER || true
    echo "✅ Frontend 컨테이너 백업 완료 (포트 80 해제됨)"
else
    echo "ℹ️ 실행 중인 Frontend 컨테이너가 없음"
fi

# Frontend 포트 정리
echo "🔍 포트 80 사용 상태 확인 중..."
if netstat -tlnp 2>/dev/null | grep -q ":80 " || lsof -i :80 2>/dev/null; then
    echo "⚠️ 포트 80이 여전히 사용 중 - 추가 정리 진행"
    docker_cmd ps -q --filter "publish=80" | xargs -r docker_cmd stop
    sleep 3
else
    echo "✅ 포트 80 사용 가능"
fi

# 새 Frontend 컨테이너 시작
echo "🚀 새 Frontend 컨테이너 시작 중..."
docker_cmd run -d \
  --name $FRONTEND_CONTAINER \
  -p 80:80 \
  --restart unless-stopped \
  $FRONTEND_IMAGE

# 3. Backend 컨테이너 백업 및 배포 (기존 로직 유지)
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

# Backend 포트 사용 상태 확인 및 정리
echo "🔍 포트 8080 사용 상태 확인 중..."
if netstat -tlnp 2>/dev/null | grep -q ":8080 " || lsof -i :8080 2>/dev/null; then
    echo "⚠️ 포트 8080이 여전히 사용 중 - 추가 정리 진행"
    docker_cmd ps -q --filter "publish=8080" | xargs -r docker_cmd stop
    sleep 3
else
    echo "✅ 포트 8080 사용 가능"
fi

# 4. 데이터베이스 연결 확인 (기존 로직 유지)
echo "🗄️ 데이터베이스 연결 확인 중..."
if ! docker_cmd ps | grep -q $MYSQL_CONTAINER; then
    echo "⚠️ MySQL 컨테이너가 실행되지 않음. docker-compose로 시작 중..."
    docker-compose up -d mysql
    echo "⏳ MySQL 완전 시작 대기 중..."
    sleep 45
else
    echo "✅ MySQL 컨테이너 실행 중"
fi

# MySQL이 완전히 준비될 때까지 대기
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

# 새 Backend 컨테이너 시작
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

# 6. 컨테이너 시작 확인
echo "📋 컨테이너 상태 확인..."
sleep 5
docker_cmd ps | grep dongnae || echo "⚠️ 컨테이너 상태 확인 필요"

# 7. Frontend 헬스체크
echo "🌐 Frontend 헬스체크 중..."
FRONTEND_READY=0
FRONTEND_RETRY=0
MAX_FRONTEND_RETRIES=6

while [ $FRONTEND_RETRY -lt $MAX_FRONTEND_RETRIES ] && [ $FRONTEND_READY -eq 0 ]; do
    if docker_cmd exec $FRONTEND_CONTAINER curl -f -s http://localhost/ > /dev/null 2>&1; then
        echo "✅ Frontend 서비스 정상 동작 확인!"
        FRONTEND_READY=1
    else
        echo "⏳ Frontend 시작 대기 중... ($((FRONTEND_RETRY+1))/$MAX_FRONTEND_RETRIES)"
        sleep 5
        FRONTEND_RETRY=$((FRONTEND_RETRY+1))
    fi
done

# 8. Backend 헬스체크 (기존 로직 유지)
echo "🏥 Backend 서비스 헬스체크 중..."
RETRY_COUNT=0
MAX_RETRIES=15

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if ! docker_cmd ps -q -f name=^${BACKEND_CONTAINER}$ > /dev/null 2>&1; then
        echo "❌ Backend 컨테이너가 중지됨! 로그 확인 중..."
        docker_cmd logs $BACKEND_CONTAINER --tail 50
        break
    fi

    if docker_cmd exec $BACKEND_CONTAINER curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Backend 서비스 정상 동작 확인!"
        break
    elif docker_cmd exec $BACKEND_CONTAINER curl -f -s http://localhost:8080 > /dev/null 2>&1; then
        echo "✅ Backend 서비스 정상 동작 확인!"
        break
    else
        echo "⏳ Backend 서비스 시작 대기 중... ($((RETRY_COUNT+1))/$MAX_RETRIES)"
        sleep 10
        RETRY_COUNT=$((RETRY_COUNT+1))
    fi
done

# 9. 배포 결과 처리
if [ $RETRY_COUNT -eq $MAX_RETRIES ] || [ $FRONTEND_READY -eq 0 ]; then
    echo "❌ 서비스 시작 실패! 롤백 진행..."

    # 실패한 컨테이너들 삭제
    docker_cmd rm -f $FRONTEND_CONTAINER $BACKEND_CONTAINER 2>/dev/null || true

    # 백업 컨테이너들로 복원
    if docker_cmd ps -a -q -f name=^${BACKUP_FRONTEND_CONTAINER}$ > /dev/null 2>&1; then
        docker_cmd rename $BACKUP_FRONTEND_CONTAINER $FRONTEND_CONTAINER
        docker_cmd start $FRONTEND_CONTAINER
    fi

    if docker_cmd ps -a -q -f name=^${BACKUP_BACKEND_CONTAINER}$ > /dev/null 2>&1; then
        docker_cmd rename $BACKUP_BACKEND_CONTAINER $BACKEND_CONTAINER
        docker_cmd start $BACKEND_CONTAINER
    fi

    echo "❌ 풀스택 배포 실패 - 이전 버전으로 롤백됨"
    exit 1
else
    echo "🎉 풀스택 배포 성공!"

    # 백업 컨테이너들 정리
    docker_cmd rm -f $BACKUP_FRONTEND_CONTAINER $BACKUP_BACKEND_CONTAINER 2>/dev/null || true

    # 외부 접근 테스트
    echo "🌐 외부 접근성 테스트 중..."
    if curl -f -s http://i13a708.p.ssafy.io > /dev/null 2>&1; then
        echo "✅ Frontend 외부 접근 가능"
    else
        echo "⚠️ Frontend 외부 접근 실패"
    fi

    if curl -f -s http://i13a708.p.ssafy.io:8080 > /dev/null 2>&1; then
        echo "✅ Backend 외부 접근 가능"
    else
        echo "⚠️ Backend 외부 접근 실패"
    fi

    echo "✅ 동네 앱 풀스택 배포 완료!"
    echo "🌐 Frontend 접속: http://i13a708.p.ssafy.io"
    echo "🌐 Backend 접속: http://i13a708.p.ssafy.io:8080"

    # 최종 상태 확인
    echo "📋 최종 컨테이너 상태:"
    docker_cmd ps | grep dongnae
fi

echo "배포 완료 시간: $(date)"