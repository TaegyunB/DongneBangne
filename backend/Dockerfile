# OpenJDK 17 기반 경량 이미지 사용
FROM openjdk:17-jdk-slim

# 어르신 케어 서비스 메타데이터
LABEL description="경로단 간 커뮤니티 서비스 백엔드"

# 작업 디렉토리 설정
WORKDIR /app

# .env 파일 복사 (Jenkins에서 제공)
COPY .env .env

# JAR 파일 복사 (빌드 결과물)
COPY build/libs/backend-0.0.1-SNAPSHOT.jar backend.jar

# 포트 노출 (추가하면 좋음)
EXPOSE 8080

# 애플리케이션 실행
CMD ["java", "-jar", "backend.jar"]