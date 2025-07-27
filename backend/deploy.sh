#!/bin/bash

# 경로단 간 커뮤니티 웹 자동 배포 스크립트
# 무중단 배포를 위한 블루-그린 배포 방식

set -e  # 에러 발생 시 스크립트 중단

echo "🚀 어르신 케어 앱 자동 배포 시작"
echo "배포 시간: $(date)"

# 환경 변수 설정
DOCKER_IMAGE="taegyunb99/dongnae-backend"
CONTAINER_NAME="elderly-care-app"
BACKUP_CONTAINER_NAME="elderly-care-app-backup"
COMPOSE_FILE="docker-compose.yml"
MYSQL_CONTAINER="eldercare-mysql"

# 1. 최신 이미지 다운로드
echo "📦 최신 Docker 이미지 다운로드 중..."
docker pull $DOCKER_IMAGE:latest

# 2. 현재 실행 중인 컨테이너 백업
echo "💾 현재 서비스 백업 중..."
if docker ps -q -f name=$CONTAINER_NAME > /dev/null 2>&1; then
    echo "기존 컨테이너를 백업용으로 이름 변경"
    docker rename $CONTAINER_NAME $BACKUP_CONTAINER_NAME || true
fi

# 3. 데이터베이스 연결 확인
echo "🗄️ 데이터베이스 연결 확인 중..."
if ! docker ps | grep -q $MYSQL_CONTAINER; then
    echo "⚠️ MySQL 컨테이너가 실행되지 않음. 시작 중..."
    docker start $MYSQL_CONTAINER || docker-compose up -d mysql
fi

# MySQL이 준비될 때까지 대기
echo "⏳ MySQL 준비 대기 중..."
sleep 10

# 4. 새 컨테이너 시작 (무중단 배포)
echo "🔄 새로운 서비스 시작 중..."
docker-compose up -d --no-deps elderly-care-backend

# 4. 헬스체크 (서비스가 정상 동작하는지 확인)
echo "🏥 서비스 헬스체크 중..."
RETRY_COUNT=0
MAX_RETRIES=30

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ 서비스 정상 동작 확인!"
        break
    else
        echo "⏳ 서비스 시작 대기 중... ($((RETRY_COUNT+1))/$MAX_RETRIES)"
        sleep 10
        RETRY_COUNT=$((RETRY_COUNT+1))
    fi
done

# 5. 헬스체크 결과에 따른 처리
if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ 서비스 시작 실패! 롤백 진행..."

    # 새 컨테이너 중지
    docker-compose stop elderly-care-backend
    docker rm $CONTAINER_NAME || true

    # 백업 컨테이너로 복원
    if docker ps -a -q -f name=$BACKUP_CONTAINER_NAME > /dev/null 2>&1; then
        docker rename $BACKUP_CONTAINER_NAME $CONTAINER_NAME
        docker start $CONTAINER_NAME
        echo "🔄 이전 버전으로 롤백 완료"
    fi

    exit 1
else
    echo "🎉 새 버전 배포 성공!"

    # 6. 백업 컨테이너 정리
    if docker ps -a -q -f name=$BACKUP_CONTAINER_NAME > /dev/null 2>&1; then
        echo "🗑️ 이전 버전 컨테이너 정리 중..."
        docker stop $BACKUP_CONTAINER_NAME || true
        docker rm $BACKUP_CONTAINER_NAME || true
    fi

    # 7. 사용하지 않는 이미지 정리
    echo "🧹 사용하지 않는 Docker 이미지 정리 중..."
    docker image prune -f || true

    echo "✅ 어르신 케어 앱 배포 완료!"
    echo "🌐 서비스 접속: http://i13a708.p.ssafy.io:8080"
    echo "📊 헬스체크: http://i13a708.p.ssafy.io:8080/actuator/health"
fi

echo "배포 완료 시간: $(date)"