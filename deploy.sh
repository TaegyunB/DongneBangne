#!/bin/bash

# 동네 앱 풀스택 자동 배포 스크립트 (시스템 nginx 활용)
set -e

echo "🚀 동네 앱 시스템 nginx 리버스 프록시 배포 시작"
echo "배포 시간: $(date)"

# 환경 변수 설정
BACKEND_IMAGE=${BACKEND_IMAGE:-"taegyunb99/dongnae-backend:latest"}
FRONTEND_IMAGE=${FRONTEND_IMAGE:-"taegyunb99/dongnae-frontend:latest"}

echo "📦 배포할 이미지:"
echo "  Backend:  $BACKEND_IMAGE (포트 8080)"
echo "  Frontend: $FRONTEND_IMAGE (포트 3000 → 시스템 nginx에서 프록시)"

# 최신 이미지 다운로드
echo "📦 최신 Docker 이미지 다운로드 중..."
docker pull $BACKEND_IMAGE
docker pull $FRONTEND_IMAGE

# 기존 dongnae 컨테이너들 완전 정리 (시스템 nginx는 그대로 둠)
echo "🧹 기존 dongnae 컨테이너 완전 정리 중..."

# 현재 실행 중인 컨테이너 확인
echo "📋 현재 실행 중인 컨테이너:"
docker ps --format "table {{.Names}}\t{{.Ports}}"

# Backend 관련 컨테이너들 중지 (MySQL 제외)
echo "🛑 Backend 관련 컨테이너 중지 중..."
docker stop dongnae 2>/dev/null || echo "dongnae 컨테이너 없음"
docker stop dongnae-backend 2>/dev/null || echo "dongnae-backend 컨테이너 없음"
docker stop dongnae-frontend 2>/dev/null || echo "dongnae-frontend 컨테이너 없음"

# Backend 관련 컨테이너들 삭제 (MySQL 제외)
echo "🗑️ Backend 관련 컨테이너 삭제 중..."
docker rm dongnae 2>/dev/null || echo "dongnae 컨테이너 삭제 완료"
docker rm dongnae-backend 2>/dev/null || echo "dongnae-backend 컨테이너 삭제 완료"
docker rm dongnae-frontend 2>/dev/null || echo "dongnae-frontend 컨테이너 삭제 완료"

# 포트 8080 사용 컨테이너 정리
echo "🔍 포트 8080 사용 컨테이너 정리..."
PORT_8080_CONTAINERS=$(docker ps --filter "publish=8080" -q)
if [ ! -z "$PORT_8080_CONTAINERS" ]; then
    echo "⚠️ 포트 8080을 사용하는 다른 컨테이너 발견:"
    docker ps --filter "publish=8080" --format "table {{.Names}}\t{{.Ports}}"
    echo "중지 및 삭제 중..."
    echo $PORT_8080_CONTAINERS | xargs docker stop
    echo $PORT_8080_CONTAINERS | xargs docker rm
fi

# 포트 3000 사용 컨테이너 정리
echo "🔍 포트 3000 사용 컨테이너 정리..."
PORT_3000_CONTAINERS=$(docker ps --filter "publish=3000" -q)
if [ ! -z "$PORT_3000_CONTAINERS" ]; then
    echo "⚠️ 포트 3000을 사용하는 다른 컨테이너 발견:"
    docker ps --filter "publish=3000" --format "table {{.Names}}\t{{.Ports}}"
    echo "중지 및 삭제 중..."
    echo $PORT_3000_CONTAINERS | xargs docker stop
    echo $PORT_3000_CONTAINERS | xargs docker rm
fi

# 포트 해제 대기
echo "⏳ 포트 해제 완료 대기 중..."
sleep 5

# 최종 포트 상태 확인
echo "📊 포트 상태 확인:"
if netstat -tlnp 2>/dev/null | grep -q ":8080 "; then
    echo "❌ 포트 8080이 여전히 사용 중:"
    netstat -tlnp | grep ":8080 "
    exit 1
fi

if netstat -tlnp 2>/dev/null | grep -q ":3000 "; then
    echo "❌ 포트 3000이 여전히 사용 중:"
    netstat -tlnp | grep ":3000 "
    exit 1
fi

echo "✅ 포트 8080, 3000 모두 해제 완료"

# MySQL 컨테이너 및 네트워크 확인/설정
echo "🗄️ MySQL 컨테이너 및 네트워크 확인 중..."

if docker ps | grep -q "dongnae-mysql"; then
    echo "✅ MySQL 컨테이너 이미 실행 중"
    
    # 기존 MySQL 컨테이너의 네트워크 확인 (NetworkMode에서 추출)
    echo "🔍 MySQL 컨테이너 네트워크 상태 확인..."
    MYSQL_NETWORK=$(docker inspect dongnae-mysql --format='{{.HostConfig.NetworkMode}}')
    
    echo "현재 MySQL 네트워크: $MYSQL_NETWORK"
    
    # 동일한 네트워크 사용을 위해 변수 설정
    DOCKER_NETWORK="$MYSQL_NETWORK"
    
else
    echo "🚀 MySQL 컨테이너 시작 중..."
    
    # 기본 네트워크 이름 설정
    DOCKER_NETWORK="s13p11a708-pipeline_dongnae-network"
    
    # 네트워크 생성
    docker network create $DOCKER_NETWORK 2>/dev/null || true
    
    docker run -d \
        --name dongnae-mysql \
        --network $DOCKER_NETWORK \
        -e MYSQL_ROOT_PASSWORD=bangnae \
        -e MYSQL_DATABASE=dongnae \
        -e MYSQL_USER=dongnaeuser \
        -e MYSQL_PASSWORD=dongnaepass \
        -p 3307:3306 \
        mysql:8.0 \
        --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    
    echo "⏳ MySQL 완전 시작 대기 중..."
    sleep 30
fi

echo "🌐 사용할 Docker 네트워크: $DOCKER_NETWORK"

# 네트워크 존재 확인
if ! docker network ls | grep -q "$DOCKER_NETWORK"; then
    echo "❌ 네트워크 '$DOCKER_NETWORK'를 찾을 수 없습니다."
    echo "📋 현재 존재하는 네트워크 목록:"
    docker network ls
    exit 1
fi

# MySQL 연결 테스트
echo "🏥 MySQL 연결 테스트..."
for i in {1..10}; do
    if docker exec dongnae-mysql mysqladmin ping -h localhost -u dongnaeuser -pdongnaepass --silent >/dev/null 2>&1; then
        echo "✅ MySQL 연결 가능"
        break
    fi
    if [ $i -eq 10 ]; then
        echo "❌ MySQL 연결 실패"
        exit 1
    fi
    echo "⏳ MySQL 연결 대기 중... ($i/10)"
    sleep 3
done

# Backend 컨테이너 시작 (MySQL과 같은 네트워크 사용)
echo "🔧 Backend 컨테이너 시작 중..."
docker run -d \
    --name dongnae-backend \
    --network $DOCKER_NETWORK \
    -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e SPRING_DATASOURCE_URL="jdbc:mysql://dongnae-mysql:3306/dongnae?serverTimezone=UTC&characterEncoding=UTF-8" \
    -e SPRING_DATASOURCE_USERNAME="dongnaeuser" \
    -e SPRING_DATASOURCE_PASSWORD="dongnaepass" \
    -e SPRING_DATASOURCE_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver" \
    -e SPRING_JPA_HIBERNATE_DDL_AUTO="update" \
    -e SPRING_JPA_DATABASE_PLATFORM="org.hibernate.dialect.MySQLDialect" \
    --restart unless-stopped \
    $BACKEND_IMAGE

# Frontend 컨테이너 시작 (같은 네트워크)
echo "🎨 Frontend 컨테이너 시작 중... (포트 3000)"
docker run -d \
    --name dongnae-frontend \
    --network $DOCKER_NETWORK \
    -p 3000:80 \
    -e NODE_ENV=production \
    --restart unless-stopped \
    $FRONTEND_IMAGE

# 헬스체크
echo "🏥 Backend 헬스체크 중..."
for i in {1..30}; do
    if curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
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

echo "🏥 Frontend 헬스체크 중..."
for i in {1..15}; do
    if curl -f -s http://localhost:3000 >/dev/null 2>&1; then
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

# 시스템 nginx 설정 업데이트
echo "🔧 시스템 nginx 설정 업데이트 중..."

# nginx 설정 파일 생성
sudo tee /etc/nginx/sites-available/dongnae > /dev/null << 'EOF'
server {
    listen 80;
    server_name i13a708.p.ssafy.io localhost;

    # Frontend (Vue.js) - 포트 3000으로 프록시
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket 지원 (필요시)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Backend API - 포트 8080으로 프록시
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS 헤더
        add_header 'Access-Control-Allow-Origin' '*' always;
        add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
        add_header 'Access-Control-Allow-Headers' 'Accept,Authorization,Cache-Control,Content-Type' always;
    }

    # Backend 직접 접근 (선택사항)
    location /backend/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 헬스체크
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
EOF

# nginx 사이트 활성화
sudo ln -sf /etc/nginx/sites-available/dongnae /etc/nginx/sites-enabled/dongnae

# 기본 nginx 페이지 비활성화 (선택사항)
sudo rm -f /etc/nginx/sites-enabled/default

# nginx 설정 테스트
echo "🧪 nginx 설정 테스트 중..."
if sudo nginx -t; then
    echo "✅ nginx 설정 검증 완료"
    
    # nginx 재시작
    echo "🔄 nginx 재시작 중..."
    sudo systemctl reload nginx
    echo "✅ nginx 재시작 완료"
else
    echo "❌ nginx 설정 오류"
    exit 1
fi

# 최종 연결 테스트
echo "🔗 최종 연결 테스트 중..."
sleep 5

# Frontend 테스트
if curl -f -s http://localhost/ >/dev/null 2>&1; then
    echo "✅ Frontend 프록시 연결 확인"
else
    echo "⚠️ Frontend 프록시 연결 실패"
fi

# Backend API 테스트
if curl -f -s http://localhost/api/actuator/health >/dev/null 2>&1; then
    echo "✅ Backend API 프록시 연결 확인"
elif curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "✅ Backend 직접 연결 확인 (프록시 경로 확인 필요)"
else
    echo "⚠️ Backend 연결 확인 필요"
fi

echo "🎉 시스템 nginx 리버스 프록시 배포 완료!"
echo ""
echo "🌐 서비스 접속 정보:"
echo "  메인 사이트: https://i13a708.p.ssafy.io (시스템 nginx → Frontend)"
echo "  API 엔드포인트: https://i13a708.p.ssafy.io/api/* (시스템 nginx → Backend)"
echo "  Backend 직접: http://i13a708.p.ssafy.io:8080 (개발/디버깅용)"
echo ""

# 컨테이너 상태 확인
echo "📋 컨테이너 상태:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" --filter "name=dongnae"

echo "✅ 배포 완료!"
echo "배포 완료 시간: $(date)"