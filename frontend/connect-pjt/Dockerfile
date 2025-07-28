# 빌드 단계 - Node.js 22 (최신) 사용
FROM node:22-alpine AS build

WORKDIR /app

# Alpine에 필요한 빌드 도구 설치
RUN apk add --no-cache python3 make g++ libc6-compat

# package.json 복사
COPY package*.json ./

# 완전히 새로운 설치 (캐시 무시)
RUN rm -rf node_modules package-lock.json .npm 2>/dev/null || true
RUN npm cache clean --force
RUN npm config set registry https://registry.npmjs.org/

# 최신 npm 사용
RUN npm install -g npm@latest

# Rollup 문제 해결을 위한 특별 설치
RUN npm install --no-optional --legacy-peer-deps

# 소스코드 복사
COPY . .

# 환경변수 설정
ENV NODE_ENV=production
ENV VITE_API_BASE_URL=http://i13a708.p.ssafy.io:8080
ENV VITE_APP_TITLE=동네앱

# Rollup을 수동으로 재설치
RUN npm install rollup@latest --save-dev --force

# 빌드 (에러 발생시 대안 시도)
RUN npm run build || (npm install vite@latest --save-dev && npm run build)

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

# curl 설치
RUN apk add --no-cache curl

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]