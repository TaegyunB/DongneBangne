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
    docker rename $CONTAINER_NAME $BACKUP_CONTAINER_NAME || true
fi

# 3. 데이터베이스 연결 확인
echo "🗄️ 데이터베이스 연결 확인 중..."
if ! docker ps | grep -q $MYSQL_CONTAINER; then
    echo "⚠️ MySQL 컨테이너가 실행되지 않음. docker-compose로 시작 중..."
    docker-compose up -d mysql
    echo "⏳ MySQL 완전 시작 대기 중..."
    sleep 30
else
    echo "✅ MySQL 컨테이너 실행 중"
fi

# MySQL이 준비될 때까지 대기
echo "⏳ MySQL 준비 대기 중..."
sleep 15

# 4. 새 컨테이너 시작 (무중단 배포)
echo "🔄 새로운 서비스 시작 중..."
docker-compose up -d --no-deps dongnae-backend

# 5. 컨테이너 시작 확인
echo "📋 컨테이너 상태 확인..."
docker ps | grep dongnae || echo "⚠️ 컨테이너 상태 확인 필요"

# 6. 초기 로그 확인 (디버깅용)
echo "📊 초기 애플리케이션 로그 확인..."
sleep 5
docker logs $CONTAINER_NAME --tail 20 || echo "⚠️ 로그 확인 실패"

# 7. 헬스체크 (서비스가 정상 동작하는지 확인)
echo "🏥 서비스 헬스체크 중..."
RETRY_COUNT=0
MAX_RETRIES=30

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    # 컨테이너가 실행 중인지 먼저 확인
    if ! docker ps -q -f name=^${CONTAINER_NAME}$ > /dev/null 2>&1; then
        echo "❌ 컨테이너가 중지됨! 로그 확인 중..."
        docker logs $CONTAINER_NAME --tail 50
        break
    fi

    # HTTP 헬스체크
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1 || curl -f http://localhost:8080 > /dev/null 2>&1; then
        echo "✅ 서비스 정상 동작 확인!"
        break
    else
        echo "⏳ 서비스 시작 대기 중... ($((RETRY_COUNT+1))/$MAX_RETRIES)"

        # 5번마다 로그 일부 출력 (디버깅용)
        if [ $((RETRY_COUNT % 5)) -eq 4 ]; then
            echo "📊 현재 애플리케이션 상태:"
            docker logs $CONTAINER_NAME --tail 10 || echo "로그 확인 실패"
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

    # 새 컨테이너 중지 (삭제하지 않고 중지만)
    docker-compose stop dongnae-backend || true

    # 백업 컨테이너로 복원
    if docker ps -a -q -f name=^${BACKUP_CONTAINER_NAME}$ > /dev/null 2>&1; then
        docker rename $BACKUP_CONTAINER_NAME $CONTAINER_NAME
        docker start $CONTAINER_NAME
        echo "🔄 이전 버전으로 롤백 완료"
    else
        echo "⚠️ 백업 컨테이너가 없어 롤백 실패"
    fi

    exit 1
else
    echo "🎉 새 버전 배포 성공!"

    # 9. 백업 컨테이너 정리
    if docker ps -a -q -f name=^${BACKUP_CONTAINER_NAME}$ > /dev/null 2>&1; then
        echo "🗑️ 이전 버전 컨테이너 정리 중..."
        docker stop $BACKUP_CONTAINER_NAME || true
        docker rm $BACKUP_CONTAINER_NAME || true
    fi

    # 10. 사용하지 않는 이미지 정리
    echo "🧹 사용하지 않는 Docker 이미지 정리 중..."
    docker image prune -f || true

    echo "✅ 동네 앱 배포 완료!"
    echo "🌐 서비스 접속: http://i13a708.p.ssafy.io"
    echo "📊 헬스체크: http://i13a708.p.ssafy.io/actuator/health"

    # 최종 상태 확인
    echo "📋 최종 컨테이너 상태:"
    docker ps | grep dongnae
fi

echo "배포 완료 시간: $(date)"