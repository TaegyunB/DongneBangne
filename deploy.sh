#!/bin/bash

# 동네 앱 풀스택 자동 배포 스크립트 (옵션 1: Frontend nginx 프록시)
set -e  # 에러 발생 시 스크립트 중단

echo "🚀 동네 앱 풀스택 자동 배포 시작"
echo "배포 시간: $(date)"

# 환경 변수 설정
BACKEND_IMAGE=${BACKEND_IMAGE:-"taegyunb99/dongnae-backend:latest"}
FRONTEND_IMAGE=${FRONTEND_IMAGE:-"taegyunb99/dongnae-frontend:latest"}

echo "📦 배포할 이미지:"
echo "  Backend:  $BACKEND_IMAGE"
echo "  Frontend: $FRONTEND_IMAGE"

# 1. 최신 이미지 다운로드
echo "📦 최신 Docker 이미지 다운로드 중..."
docker pull $BACKEND_IMAGE
docker pull $FRONTEND_IMAGE

# 2. 기존 컨테이너 완전 정리
echo "🧹 기존 컨테이너 정리 중..."

# Frontend 컨테이너 정리 (포트 80)
if docker ps -q --filter "name=dongnae-frontend" | grep -q .; then
    echo "🛑 기존 Frontend 컨테이너 중지 중..."
    docker stop dongnae-frontend
    docker rm dongnae-frontend
fi

# Backend 컨테이너 정리
if docker ps -q --filter "name=dongnae-backend" | grep -q .; then
    echo "🛑 기존 Backend 컨테이너 중지 중..."
    docker stop dongnae-backend
    docker rm dongnae-backend
fi

# 포트 80 사용 컨테이너 정리
echo "🔍 포트 80 사용 컨테이너 정리..."
PORT_80_CONTAINERS=$(docker ps --filter "publish=80" -q)
if [ ! -z "$PORT_80_CONTAINERS" ]; then
    echo "⚠️ 포트 80을 사용하는 다른 컨테이너 발견, 중지 중..."
    echo $PORT_80_CONTAINERS | xargs docker stop
    echo $PORT_80_CONTAINERS | xargs docker rm
fi

# 잠시 대기 (포트 해제 완료 대기)
echo "⏳ 포트 해제 완료 대기 중..."
sleep 5

# 3. MySQL 컨테이너 확인/시작
echo "🗄️ MySQL 컨테이너 확인 중..."
if ! docker ps | grep -q "dongnae-mysql"; then
    echo "🚀 MySQL 컨테이너 시작 중..."
    docker-compose up -d mysql
    
    echo "⏳ MySQL 완전 시작 대기 중..."
    for i in {1..30}; do
        if docker exec dongnae-mysql mysqladmin ping -h localhost -u dongnaeuser -pdongnaepass --silent >/dev/null 2>&1; then
            echo "✅ MySQL 준비 완료"
            break
        fi
        if [ $i -eq 30 ]; then
            echo "❌ MySQL 시작 실패"
            exit 1
        fi
        echo "⏳ MySQL 준비 대기 중... ($i/30)"
        sleep 2
    done
else
    echo "✅ MySQL 이미 실행 중"
fi

# 4. Backend 컨테이너 시작
echo "🔧 Backend 컨테이너 시작 중..."
docker run -d \
    --name dongnae-backend \
    --network s13p11a708-pipeline_dongnae-network \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e JAVA_OPTS="-Xmx512m -Xms256m" \
    -e SPRING_DATASOURCE_URL="jdbc:mysql://dongnae-mysql:3306/dongnae?serverTimezone=UTC&characterEncoding=UTF-8" \
    -e SPRING_DATASOURCE_USERNAME="dongnaeuser" \
    -e SPRING_DATASOURCE_PASSWORD="dongnaepass" \
    -e SPRING_DATASOURCE_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver" \
    -e SPRING_JPA_HIBERNATE_DDL_AUTO="update" \
    -e SPRING_JPA_DATABASE_PLATFORM="org.hibernate.dialect.MySQLDialect" \
    -e SPRING_SECURITY_USER_NAME="admin" \
    -e SPRING_SECURITY_USER_PASSWORD="dongnae2024!" \
    -e SPRING_WEB_CORS_ALLOWED_ORIGINS="http://dongnae-frontend,http://i13a708.p.ssafy.io" \
    --restart unless-stopped \
    $BACKEND_IMAGE

# 5. Backend 헬스체크
echo "🏥 Backend 헬스체크 중..."
for i in {1..30}; do
    if docker exec dongnae-backend curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
        echo "✅ Backend 정상 동작 확인"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Backend 헬스체크 실패"
        docker logs dongnae-backend --tail 20
        exit 1
    fi
    echo "⏳ Backend 시작 대기 중... ($i/30)"
    sleep 3
done

# 6. Frontend 컨테이너 시작
echo "🎨 Frontend 컨테이너 시작 중..."
docker run -d \
    --name dongnae-frontend \
    --network s13p11a708-pipeline_dongnae-network \
    -p 80:80 \
    -e NODE_ENV=production \
    --restart unless-stopped \
    $FRONTEND_IMAGE

# 7. Frontend 헬스체크
echo "🏥 Frontend 헬스체크 중..."
for i in {1..15}; do
    if curl -f -s http://localhost >/dev/null 2>&1; then
        echo "✅ Frontend 정상 동작 확인"
        break
    fi
    if [ $i -eq 15 ]; then
        echo "❌ Frontend 헬스체크 실패"
        docker logs dongnae-frontend --tail 20
        exit 1
    fi
    echo "⏳ Frontend 시작 대기 중... ($i/15)"
    sleep 2
done

# 8. API 프록시 테스트
echo "🔗 API 프록시 연결 테스트 중..."
for i in {1..10}; do
    if curl -f -s http://localhost/api/actuator/health >/dev/null 2>&1; then
        echo "✅ API 프록시 연결 확인"
        break
    fi
    if [ $i -eq 10 ]; then
        echo "⚠️ API 프록시 연결 확인 실패 (정상적일 수 있음)"
    fi
    echo "⏳ API 프록시 연결 대기 중... ($i/10)"
    sleep 2
done

# 9. 배포 완료
echo "🎉 풀스택 배포 완료!"
echo ""
echo "🌐 서비스 접속 정보:"
echo "  Frontend: http://i13a708.p.ssafy.io (Vue.js + nginx)"
echo "  Backend API: http://i13a708.p.ssafy.io/api/* (nginx → Backend 프록시)"
echo ""

# 컨테이너 상태 확인
echo "📋 컨테이너 상태:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" --filter "name=dongnae"

# 불필요한 이미지 정리
echo "🧹 이전 이미지 정리 중..."
docker image prune -f

echo "✅ 옵션 1 구조 배포 완료!"
echo "배포 완료 시간: $(date)"