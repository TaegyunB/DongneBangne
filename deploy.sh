#!/bin/bash

set -e  # 에러 발생 시 스크립트 중단

echo "🚀 동네 앱 자동 배포 시작"
echo "배포 시간: $(date)"

DOCKER_IMAGE="taegyunb99/dongnae-backend"
CONTAINER_NAME="dongnae"
BACKUP_CONTAINER_NAME="dongnae-backup"
MYSQL_CONTAINER="dongnae-mysql"

docker_cmd() {
    sudo docker "$@"
}

docker_cmd_compose() {
    sudo docker-compose "$@"
}

echo "📦 최신 Docker 이미지 다운로드 중..."
docker_cmd pull $DOCKER_IMAGE:latest

echo "💾 현재 서비스 백업 중..."
if docker_cmd ps -q -f name=^${CONTAINER_NAME}$ > /dev/null 2>&1; then
    echo "기존 컨테이너를 백업용으로 중지 및 이름 변경"
    docker_cmd rm -f $BACKUP_CONTAINER_NAME > /dev/null 2>&1 || true
    docker_cmd stop $CONTAINER_NAME || true
    docker_cmd rename $CONTAINER_NAME $BACKUP_CONTAINER_NAME || true
    echo "✅ 기존 컨테이너 백업 완료"
else
    echo "ℹ️ 실행 중인 컨테이너가 없음"
fi

echo "🔍 포트 8080 사용 상태 확인 중..."
if lsof -i :8080 2>/dev/null | grep -q LISTEN || netstat -tlnp 2>/dev/null | grep -q ":8080 "; then
    echo "⚠️ 포트 8080이 여전히 사용 중 - 정리 진행"
    docker_cmd ps -a --filter "name=dongnae" --format "table {{.Names}}\t{{.Status}}" | grep -v "NAMES"
    docker_cmd stop $(docker_cmd ps -q --filter "name=dongnae") 2>/dev/null || true
    sleep 3
else
    echo "✅ 포트 8080 사용 가능"
fi

echo "🗄️ 데이터베이스 연결 확인 중..."
if ! docker_cmd ps | grep -q $MYSQL_CONTAINER; then
    echo "⚠️ MySQL 컨테이너가 실행되지 않음. docker-compose로 시작 중..."
    docker_cmd_compose up -d mysql
    echo "⏳ MySQL 완전 시작 대기 중..."
    sleep 45
else
    echo "✅ MySQL 컨테이너 실행 중"
fi

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

echo "🔄 새로운 서비스 시작 중..."
MYSQL_NETWORK=$(docker_cmd inspect $MYSQL_CONTAINER --format='{{range $net, $config := .NetworkSettings.Networks}}{{$net}}{{end}}' | head -1)
echo "📋 MySQL 네트워크: $MYSQL_NETWORK"

echo "🔍 최종 포트 8080 확인..."
if docker_cmd ps --filter "publish=8080" --format "{{.Names}}" | grep -q .; then
    echo "❌ 포트 8080 사용 중인 컨테이너 있음 → 강제 정리"
    docker_cmd ps --filter "publish=8080" --format "table {{.Names}}\t{{.Ports}}\t{{.Status}}"
    docker_cmd ps --filter "publish=8080" -q | xargs -r docker_cmd stop
    docker_cmd ps -a --filter "publish=8080" -q | xargs -r docker_cmd rm
    sleep 2
fi

docker_cmd run -d \
  --name $CONTAINER_NAME \
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
  $DOCKER_IMAGE:latest

sleep 5
docker_cmd ps | grep dongnae || echo "⚠️ 컨테이너 상태 확인 필요"

echo "📊 초기 애플리케이션 로그 확인..."
sleep 10
docker_cmd logs $CONTAINER_NAME --tail 20 || echo "⚠️ 로그 확인 실패"

echo "🏥 서비스 헬스체크 중..."
RETRY_COUNT=0
MAX_RETRIES=15

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if ! docker_cmd ps -q -f name=^${CONTAINER_NAME}$ > /dev/null 2>&1; then
        echo "❌ 컨테이너가 중지됨! 로그 확인:"
        docker_cmd logs $CONTAINER_NAME --tail 50 || true
        break
    fi

    if docker_cmd exec $CONTAINER_NAME curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1 || \
       docker_cmd exec $CONTAINER_NAME curl -f -s http://localhost:8080 > /dev/null 2>&1 || \
       docker_cmd exec $CONTAINER_NAME sh -c "curl -s http://localhost:8080 | grep -q 'login\\|Spring\\|<!DOCTYPE\\|Whitelabel'" > /dev/null 2>&1 || \
       docker_cmd logs $CONTAINER_NAME 2>&1 | grep -q "Started.*Application.*in.*seconds"; then
        echo "✅ 서비스 정상 동작 확인"
        break
    else
        echo "⏳ 서비스 시작 대기 중... ($((RETRY_COUNT+1))/$MAX_RETRIES)"
        if [ $((RETRY_COUNT % 3)) -eq 2 ]; then
            docker_cmd logs $CONTAINER_NAME --tail 3 || true
        fi
        sleep 10
        RETRY_COUNT=$((RETRY_COUNT+1))
    fi
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ 서비스 시작 실패 → 롤백 중..."
    docker_cmd logs $CONTAINER_NAME || true
    docker_cmd rm -f $CONTAINER_NAME || true
    if docker_cmd ps -a -q -f name=^${BACKUP_CONTAINER_NAME}$ > /dev/null 2>&1; then
        docker_cmd rename $BACKUP_CONTAINER_NAME $CONTAINER_NAME
        docker_cmd start $CONTAINER_NAME
        echo "✅ 롤백 완료"
    else
        echo "⚠️ 백업 컨테이너 없음 → 수동 확인 필요"
    fi
    docker_cmd ps -a | grep dongnae
    exit 1
else
    echo "🎉 새 버전 배포 성공!"
    if docker_cmd ps -a -q -f name=^${BACKUP_CONTAINER_NAME}$ > /dev/null 2>&1; then
        docker_cmd rm -f $BACKUP_CONTAINER_NAME || true
        echo "✅ 백업 컨테이너 정리 완료"
    fi
    echo "🌐 외부 접근성 테스트 중..."
    if curl -f -s http://i13a708.p.ssafy.io:8080 > /dev/null 2>&1; then
        echo "✅ 외부 서비스 정상 동작"
    else
        echo "⚠️ 외부 접근 실패 (도메인/방화벽 확인 필요)"
    fi

    echo "✅ 동네 앱 배포 완료!"
    echo "🌐 접속: http://i13a708.p.ssafy.io:8080"
    echo "📊 헬스체크: http://i13a708.p.ssafy.io:8080/actuator/health"
    docker_cmd ps | grep dongnae
fi

echo "배포 완료 시간: $(date)"
