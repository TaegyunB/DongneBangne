#!/bin/bash

# 동네 앱 자동 배포 스크립트
# 무중단 배포를 위한 블루-그린 배포 방식

set -e  # 에러 발생 시 스크립트 중단

echo "🚀 동네 앱 자동 배포 시작"
echo "배포 시간: $(date)"

# 환경 변수 설정
DOCKER_IMAGE="taegyunb99/dongnae-backend"
CONTAINER_NAME="dongnae"
BACKUP_CONTAINER_NAME="dongnae-backup"
MYSQL_CONTAINER="dongnae-mysql"

# 1. 최신 이미지 다운로드
echo "📦 최신 Docker 이미지 다운로드 중..."
docker pull $DOCKER_IMAGE:latest

# 2. 현재 실행 중인 컨테이너 백업
echo "💾 현재 서비스 백업 중..."
if docker ps -q -f name=^${CONTAINER_NAME}$ > /dev/null 2>&1; then
    echo "기존 컨테이너를 백업용으로 이름 변경"
    # 기존 백업 컨테이너가 있다면 먼저 삭제
    docker rm -f $BACKUP_CONTAINER_NAME > /dev/null 2>&1 || true
    docker rename $CONTAINER_NAME $BACKUP_CONTAINER_NAME || true
fi

# 3. 데이터베이스 연결 확인
echo "🗄️ 데이터베이스 연결 확인 중..."
if ! docker ps | grep -q $MYSQL_CONTAINER; then
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
    if docker exec $MYSQL_CONTAINER mysqladmin ping -h localhost -u dongnaeuser -pdongnaepass --silent > /dev/null 2>&1; then
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

# 4. 새 컨테이너 시작
echo "🔄 새로운 서비스 시작 중..."

# MySQL 네트워크 확인
MYSQL_NETWORK=$(docker inspect $MYSQL_CONTAINER --format='{{range $net, $config := .NetworkSettings.Networks}}{{$net}}{{end}}' | head -1)
echo "📋 MySQL 네트워크: $MYSQL_NETWORK"

# 새 컨테이너 시작
docker run -d \
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

# 5. 컨테이너 시작 확인
echo "📋 컨테이너 상태 확인..."
sleep 5
docker ps | grep dongnae || echo "⚠️ 컨테이너 상태 확인 필요"

# 6. 초기 로그 확인 (디버깅용)
echo "📊 초기 애플리케이션 로그 확인..."
sleep 10
docker logs $CONTAINER_NAME --tail 20 || echo "⚠️ 로그 확인 실패"

# 7. 헬스체크 (Docker exec을 통한 내부 접근)
echo "🏥 서비스 헬스체크 중..."
RETRY_COUNT=0
MAX_RETRIES=15  # 2.5분 대기

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    # 컨테이너가 실행 중인지 먼저 확인
    if ! docker ps -q -f name=^${CONTAINER_NAME}$ > /dev/null 2>&1; then
        echo "❌ 컨테이너가 중지됨! 로그 확인 중..."
        docker logs $CONTAINER_NAME --tail 50
        break
    fi

    # Docker exec을 통한 내부 헬스체크 (네트워크 격리 문제 해결)
    if docker exec $CONTAINER_NAME curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ 서비스 정상 동작 확인! (Actuator 헬스체크 응답)"
        break
    elif docker exec $CONTAINER_NAME curl -f -s http://localhost:8080 > /dev/null 2>&1; then
        echo "✅ 서비스 정상 동작 확인! (HTTP 200 응답)"
        break
    elif docker exec $CONTAINER_NAME sh -c "curl -s http://localhost:8080 | grep -q 'login\|Spring\|<!DOCTYPE\|Whitelabel'" > /dev/null 2>&1; then
        echo "✅ 서비스 정상 동작 확인! (Spring 애플리케이션 응답)"
        break
    elif docker logs $CONTAINER_NAME 2>&1 | grep -q "Started.*Application.*in.*seconds" > /dev/null 2>&1; then
        echo "✅ 서비스 정상 동작 확인! (Spring Boot 시작 완료 로그 확인)"
        break
    else
        echo "⏳ 서비스 시작 대기 중... ($((RETRY_COUNT+1))/$MAX_RETRIES)"

        # 3번마다 로그 일부 출력 (디버깅용)
        if [ $((RETRY_COUNT % 3)) -eq 2 ]; then
            echo "📊 현재 애플리케이션 상태:"
            docker logs $CONTAINER_NAME --tail 3 || echo "로그 확인 실패"
        fi

        sleep 10
        RETRY_COUNT=$((RETRY_COUNT+1))
    fi
done

# 8. 헬스체크 결과에 따른 처리
if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ 서비스 시작 실패! 상세 로그 출력 중..."

    # 실패 시 전체 로그 출력
    echo "📊 전체 애플리케이션 로그:"
    docker logs $CONTAINER_NAME

    echo "🔄 롤백 진행..."

    # 새 컨테이너 완전 삭제
    echo "🗑️ 실패한 컨테이너 삭제 중..."
    docker rm -f $CONTAINER_NAME || true

    # 백업 컨테이너로 복원
    if docker ps -a -q -f name=^${BACKUP_CONTAINER_NAME}$ > /dev/null 2>&1; then
        echo "🔄 이전 버전 컨테이너로 복원 중..."
        docker rename $BACKUP_CONTAINER_NAME $CONTAINER_NAME
        docker start $CONTAINER_NAME
        echo "✅ 이전 버전으로 롤백 완료"
    else
        echo "⚠️ 백업 컨테이너가 없어 롤백 불가능"
        echo "📋 새 컨테이너 수동 확인 필요"
    fi

    echo "❌ 배포 실패"
    echo "📋 현재 컨테이너 상태:"
    docker ps -a | grep dongnae

    exit 1
else
    echo "🎉 새 버전 배포 성공!"

    # 9. 백업 컨테이너 정리
    if docker ps -a -q -f name=^${BACKUP_CONTAINER_NAME}$ > /dev/null 2>&1; then
        echo "🗑️ 이전 버전 컨테이너 삭제 중..."
        docker rm -f $BACKUP_CONTAINER_NAME || true
        echo "✅ 이전 버전 컨테이너 정리 완료"
    fi

    # 10. 외부 접근 테스트 (옵션)
    echo "🌐 외부 접근성 테스트 중..."
    if curl -f -s http://i13a708.p.ssafy.io:8080 > /dev/null 2>&1; then
        echo "✅ 외부에서 서비스 접근 가능"
    else
        echo "⚠️ 외부 접근 실패 (방화벽/네트워크 설정 확인 필요)"
    fi

    echo "✅ 동네 앱 배포 완료!"
    echo "🌐 서비스 접속: http://i13a708.p.ssafy.io:8080"
    echo "📊 헬스체크: http://i13a708.p.ssafy.io:8080/actuator/health"

    # 최종 상태 확인
    echo "📋 최종 컨테이너 상태:"
    docker ps | grep dongnae
fi

echo "배포 완료 시간: $(date)"