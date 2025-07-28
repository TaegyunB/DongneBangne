# 빌드 단계
FROM node:18-alpine AS build

WORKDIR /app

# package.json과 package-lock.json 복사 (레이어 캐싱 최적화)
COPY package*.json ./

# 의존성 설치
RUN npm ci --only=production

# 소스코드 복사
COPY . .

# Vite로 Vue.js 프로덕션 빌드
RUN npm run build

# 프로덕션 단계 (Nginx로 정적 파일 서빙)
FROM nginx:alpine

# Vite 빌드 결과물을 Nginx 웹 루트로 복사
COPY --from=build /app/dist /usr/share/nginx/html

# Vue Router history mode를 위한 Nginx 설정
COPY nginx.conf /etc/nginx/nginx.conf

# 포트 80 노출
EXPOSE 80

# 헬스체크용 curl 설치
RUN apk add --no-cache curl

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]