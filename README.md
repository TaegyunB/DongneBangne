# 🏠 동네방네 (DongneBoangne)

**지역 경로당 간 교류와 프로그램 활성화를 위한 디지털 플랫폼**

![Vue.js](https://img.shields.io/badge/Vue.js-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=websocket&logoColor=white)

## 📋 프로젝트 개요

| 구분 | 내용 |
|------|------|
| **팀명** | E1I4 |
| **개발기간** | 7주 (2025.07.14~2025.08.18) |
| **팀원수** | 5명 |
| **서비스명** | 동네방네 |

## 🎯 프로젝트 배경

### 💡 고령화 사회에서 경로당의 역할
최근 한국은 노인 인구가 1,000만명을 넘어서는 초고령사회로 접어들고 있습니다. 이러한 사회적 변화는 노년층의 복지와 생활의 질을 높이기 위한 다양한 정책과 노력이 요구됩니다. 특히, 노인복지의 핵심 거점으로 자리 잡고 있는 **경로당은 지역사회에서 노인들에게 사회적, 정서적 경험을 제공하는 중요한 공간**으로 그 역할이 더욱 중요해지고 있습니다.

### ❗ 경로당의 문제점
최근 경로당이 **단순 모임 장소로만 기능하는 문제**가 발생하고 있습니다. 전국 경로당의 28.1%는 노인을 위한 프로그램을 제공하고 있지 않습니다. 기존 해결책은 폐쇄된 경로당 시스템으로 인해 발생하는 **반복되는 일상과 고립감의 근본적인 해결책이 되지 못합니다**.

### 🌟 우리의 솔루션
본 프로젝트는 이러한 문제의식을 바탕으로, **지역 경로당을 디지털로 연결하고, 소통과 참여 중심의 커뮤니티로 전환**하고자 기획되었습니다. 특히 WebRTC 기술을 활용해 **경로당 간 실시간 연결**을 가능하게 하고, **게임형 미션과 챌린지 기능**을 통해 흥미 요소를 강화함으로써, 어르신들에게 새로운 형태의 일상 경험을 제공합니다.

## 🛠️ 기술 스택

| 분류 | 기술 |
|------|------|
| **Frontend** | ![Vue.js](https://img.shields.io/badge/Vue.js_3-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white) ![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white) ![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white) ![JavaScript](https://img.shields.io/badge/JavaScript_ES6+-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black) |
| **Backend** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white) ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white) |
| **Real-time** | ![Redis](https://img.shields.io/badge/Redis_7-DC382D?style=for-the-badge&logo=redis&logoColor=white) ![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=websocket&logoColor=white) ![WebRTC](https://img.shields.io/badge/WebRTC-333333?style=for-the-badge&logo=webrtc&logoColor=white) |
| **AI** | ![OpenAI](https://img.shields.io/badge/OpenAI_GPT--4o--mini-412991?style=for-the-badge&logo=openai&logoColor=white) |
| **Database** | ![MySQL](https://img.shields.io/badge/MySQL_8-4479A1?style=for-the-badge&logo=mysql&logoColor=white) ![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazon-s3&logoColor=white) |
| **Infrastructure** | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white) ![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white) |

## ✨ 핵심 기능 (Core Features)

### 1. 🎮 트로트 노래 듣고 빨리 맞추기 게임
### 게임 상세
- 화상으로 다른 경로당 사람들과 함께 게임을 할 수 있음
- 트로트 노래가 흘러나오면 빠르게 제목을 맞추는 경로당이 승리
- 게임 승리 시 포인트가 주어지며 해당 포인트는 경로당 랭킹에 반영 됨

#### 기술적 특징
- **Redis 기반 매칭 시스템**: 실시간 게임 방 생성 및 참가자 매칭
- **WebSocket STOMP 프로토콜**: 양방향 실시간 통신으로 지연 없는 게임 진행
- **점수 계산 알고리즘**: 반응 속도와 정확도 기반 동적 점수 산정


### 2. 🏆 미션 기반 도전 시스템
### 도전 상세
- 월별 서비스가 제공하는 맞춤형 도전을 2개 수행할 수 있다.
- 경로당 관리자가 2개를 생성할 수 있다.
- 해당 도전을 수행 후, 인증을 하면 점수가 부여되어 랭킹에 반영된다.
- 도전 생성과 인증은 경로당 관리자만 가능하다. 

#### 기술적 특징
- **역할별 UI 차별화**: ADMIN, USER 별로 버튼 및 모달 구분 
- **이미지 업로드 & 압축**: Multer 기반 파일 처리 및 자동 리사이징


### 3. 📰 AI 기반 자동 신문 생성
- **OpenAI API 연동**: GPT-4o-mini 모델을 활용한 맞춤형 콘텐츠 생성
- **프롬프트 엔지니어링**: 어르신 친화적 언어로 변환하는 전용 프롬프트 설계
- **PDF 자동 생성**: HTML2Canvas + jsPDF를 이용한 클라이언트 사이드 PDF 변환


### 4.🏛️ 경로당 간 커뮤니티
- **경로당 인증 시스템**: 관리자 승인 기반 경로당 등록
- **게시글 관리**: 텍스트 에디터 및 이미지 업로드
- **인기글 알고리즘**: 시간 가중 반응수 기반 랭킹

### 5. 📊 실시간 랭킹 시스템
- **Redis Sorted Set** 활용 실시간 순위 계산
- **차트 시각화**: Chart.js 기반 동적 그래프 생성

---

## 🌈 기대 효과

### 🤝 사회적 연결성 확대
- **경로당 간 네트워킹**: 지역을 넘나드는 새로운 인연 형성
- **사회적 고립감 해소**: 확장된 커뮤니티를 통한 소속감 증진
- **디지털 접근성 향상**: 웹 서비스 이용을 통한 디지털 역량 강화

### 💪 활동적인 생활
- **미션 기반 외부 활동**: 지역 탐방과 새로운 경험 창출
- **건강 증진**: 외부 활동을 통한 체력 및 건강 개선
- **게이미피케이션 효과**: 포인트와 랭킹을 통한 참여 동기 부여

### 🧠 인지적 건강 증진
- **두뇌 활성화**: 다양한 인지 게임을 통한 기억력 및 사고력 훈련
- **치매 예방 효과**: 정기적 인지 활동과 사회활동을 통한 예방 효과

### 📚 기록과 추억의 보존
- **활동 아카이브**: AI 신문을 통한 체계적 활동 기록
- **PDF 소장**: 물리적 출력 가능한 디지털 기념품 제작


---


## 🚀 로컬 실행 방법

### 1️⃣ Backend 실행

#### Step 1: 애플리케이션 실행

```bash
# Gradle Wrapper를 이용한 실행 
./gradlew clean build
./gradlew bootRun

# 빌드된 JAR 파일로 실행
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

#### 실행 확인

```bash
# 서버 상태 확인
curl http://localhost:8080/
curl http://localhost:8080/actuator/health
```

---

### 2️⃣ Frontend 실행

#### Step 1: 환경 변수 설정

```bash
cd frontend/connect-pjt
```

#### Step 2: 의존성 설치

```bash
# npm을 이용한 의존성 설치
npm install
```

#### Step 3: 개발 서버 실행

```bash
# Vite 개발 서버 실행
npm run dev
```

#### 실행 확인

**Frontend 서버가 http://localhost:5173 에서 실행됩니다.**

---

### 📦 프로덕션 빌드

#### Backend
```bash
cd backend
./gradlew clean build
# JAR 파일 생성: build/libs/backend-0.0.1-SNAPSHOT.jar
```

#### Frontend
```bash
cd frontend/connect-pjt
npm run build
# 빌드 파일 생성: dist/
```

---

## 👥 팀 E1I4

- **Infra Developer**: 백태균
- **Frontend Developer**: 심양관, 문병화, 조현아
- **Backend Developer**: 주연우, 백태균
- **Game Developer**: 문병화

