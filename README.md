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

### Frontend
- **Vue.js 3** 
- **HTML5/CSS3** 
- **JavaScript ES6+** 

### Backend
- **Spring Boot 2.7** 
- **Spring Security** 
- **Spring Data JPA** 

### Game & Real-time Communication
- **Redis 7** 
- **WebSocket (STOMP)** 
- **WebRTC** 

### AI & External API
- **OpenAI GPT-4o-mini** 
- **Custom Prompt Engineering** 

### Database & Infrastructure
- **MySQL 8** 
- **AWS S3** 
- **Docker**

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

`

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

## 🔧 사용자 경험(UX) 기술적 구현

### 노인 친화적 접근성 기능
#### 1. 전역 돋보기 시스템
```vue
<!-- 모든 페이지에서 사용 가능한 돋보기 컴포넌트 -->
<template>
  <div class="magnifier-lens" :style="lensStyle">
    <div class="magnifier-content" :style="contentStyle"></div>
    <div class="magnifier-crosshair"></div>
  </div>
</template>

<script>
// 실시간 DOM 클로닝 및 2배 확대 렌더링
createMagnifierContent() {
  const htmlClone = document.documentElement.cloneNode(true);
  // 스크립트 제거 및 이벤트 차단으로 안정성 확보
  const scripts = htmlClone.querySelectorAll('script');
  scripts.forEach(script => script.remove());
}
</script>
```

#### 2. 반응형 UI 시스템
- **터치 친화적 버튼**: 최소 44px 터치 영역 보장
- **고대비 색상**: WCAG 2.1 AA 레벨 준수

## 🏗️ System Architecture

```
DongneBoangne Platform
├── Frontend (Vue.js 3)
│   ├── Component-based UI
│   ├── Pinia State Management
│   ├── Magnifier Accessibility Module
│   └── WebRTC P2P Communication
├── Backend (Spring Boot 3)
│   ├── RESTful API Server
│   ├── JWT Authentication & Authorization
│   ├── WebSocket Game Server
│   └── OpenAI API Integration
├── Cache & Session (Redis 7)
│   ├── Game Session Management
│   ├── Real-time Matching Queue
│   └── User State Caching
└── Database (MySQL 8)
    ├── User & Senior Center Data
    ├── Game Records & Rankings
    ├── Mission & Challenge Data
    └── AI News Archive
```

## 📊 성능 최적화 기술

### 1. Redis 캐싱 전략
```java
@Cacheable(value = "gameRooms", key = "#roomId")
public GameRoom getGameRoom(String roomId) {
    return gameRoomRepository.findById(roomId)
        .orElseThrow(() -> new GameRoomNotFoundException());
}
```

### 2. 데이터베이스 최적화
- **Connection Pooling**: HikariCP 사용으로 연결 효율성 극대화
- **쿼리 최적화**: N+1 문제 해결을 위한 Fetch Join 적용
- **인덱싱**: 게임 기록 및 랭킹 조회 성능 향상

### 3. 실시간 통신 최적화
- **메시지 압축**: STOMP 프로토콜 레벨 압축 적용
- **연결 풀 관리**: WebSocket 연결 수 동적 조절



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

## 👥 팀 E1I4

5명의 팀원이 7주간 협력하여 고령자를 위한 디지털 소통 플랫폼을 개발했습니다.

- **Frontend Developer**: 심양관, 문병화, 조현아
- **Backend Developer**: 주연우, 백태균
- **Game Developer**: 문병화

