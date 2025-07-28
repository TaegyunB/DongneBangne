# 빌드 단계 - Node.js 22 (최신) 사용
FROM node:22-alpine AS build

WORKDIR /app

# Alpine에 필요한 빌드 도구 설치
RUN apk add --no-cache python3 make g++ libc6-compat

# package.json 복사
COPY package*.json ./

# 기존 파일들 정리
RUN rm -rf node_modules package-lock.json .npm 2>/dev/null || true

# npm 캐시 정리 및 설정
RUN npm cache clean --force
RUN npm config set registry https://registry.npmjs.org/

# 최신 npm 사용
RUN npm install -g npm@latest

# 의존성 설치 (vite 포함)
RUN npm install

# 소스코드 복사
COPY . .

# 환경변수 설정
ENV NODE_ENV=production
ENV VITE_API_BASE_URL=http://i13a708.p.ssafy.io:8080
ENV VITE_APP_TITLE=동네앱

# vite가 설치되었는지 확인하고 빌드
RUN npx vite build

# 빌드 결과 확인
RUN ls -la dist/

# 프로덕션 단계 (Nginx)
FROM nginx:alpine

# 빌드 결과물을 Nginx 웹 루트로 복사
COPY --from=build /app/dist /usr/share/nginx/html

# Nginx 설정
COPY nginx.conf /etc/nginx/nginx.conf

# 포트 80 노출
EXPOSE 80

# curl 설치 (헬스체크용)
RUN apk add --no-cache curl

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]