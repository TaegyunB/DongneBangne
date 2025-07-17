## WebRTC란

WebRTC(Web Real-Time Communication)는 웹 브라우저 간에 플러그인 설치 없이 실시간 음성, 영상, 데이터 통신을 가능하게 하는 오픈소스 프로젝트다. 구글이 개발했으며, 현재 대부분의 모던 브라우저에서 지원하고 있다.

## WebRTC가 해결하는 문제

기존에 웹에서 실시간 통신을 구현하려면 Flash나 별도의 플러그인이 필요했다. 하지만 WebRTC는 브라우저 자체에서 이러한 기능을 제공하여, 개발자들이 쉽게 실시간 통신 애플리케이션을 만들 수 있게 해준다.

## 구현 방법 
### 1. MediaStream API
사용자의 카메라와 마이크에 접근

```javascript
navigator.mediaDevices.getUserMedia({ video: true, audio: true })
  .then(stream => {
    // 스트림 처리
  })
  .catch(error => {
    console.error('미디어 접근 실패:', error);
  });
```

### 2. RTCPeerConnection
두 브라우저 간의 직접적인 연결

```javascript
const peerConnection = new RTCPeerConnection({
  iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
});
```

### 3. RTCDataChannel
텍스트나 바이너리 데이터를 실시간 전송

```javascript
const dataChannel = peerConnection.createDataChannel('messages');
dataChannel.onmessage = event => {
  console.log('받은 메시지:', event.data);
};
```

## 작동 순서
1. Offer 생성: 발신자가 연결 제안을 만든다
2. Answer 생성: 수신자가 연결 응답을 만든다
3. ICE 후보 교환: 네트워크 경로 정보를 교환한다
4. 연결 완료: P2P 연결이 성립된다

```javascript
// Offer 생성
const offer = await peerConnection.createOffer();
await peerConnection.setLocalDescription(offer);

// Answer 생성 (상대방에서)
await peerConnection.setRemoteDescription(offer);
const answer = await peerConnection.createAnswer();
await peerConnection.setLocalDescription(answer);
```

## 활용 사례
### 화상 회의 시스템
- Zoom, Google Meet: 대규모 화상 회의
- Discord: 음성 채팅 및 화면 공유

### 실시간 협업 도구
- Figma: 실시간 디자인 협업
- Notion: 동시 편집 기능

### 게임 및 엔터테인먼트
- 온라인 멀티플레이어 게임: 낮은 지연시간 통신
- 라이브 스트리밍: 실시간 방송

## WebRTC의 장점과 단점

### 장점
- 플러그인 불필요: 브라우저 네이티브 지원
- 낮은 지연시간: P2P 직접 연결
- 보안: DTLS 암호화 기본 제공
- 크로스 플랫폼: 모든 주요 브라우저 지원

### 단점
- NAT/방화벽 문제: STUN/TURN 서버 필요
- 시그널링 서버: 초기 연결을 위한 별도 서버 필요
- 대역폭 제한: 네트워크 상황에 따른 품질 저하

## 개발 시 고려사항

### 시그널링 서버 구축
WebRTC는 미디어 전송만 담당하므로, 초기 연결 설정을 위한 시그널링 서버가 필요하다.

```javascript
// Socket.IO를 활용한 시그널링 예시
socket.on('offer', async (offer) => {
  await peerConnection.setRemoteDescription(offer);
  const answer = await peerConnection.createAnswer();
  await peerConnection.setLocalDescription(answer);
  socket.emit('answer', answer);
});
```

### STUN/TURN 서버 설정
NAT 환경에서의 연결을 위해선 STUN 서버가 필요하다. 직접 연결이 불가능한 경우 TURN 서버가 필요하다.

### 브라우저 호환성
대부분의 브라우저에서 지원하지만, 구형 브라우저에서는 polyfill이 필요하다.

## 정리
WebRTC는 실시간 통신이 필요한 웹 애플리케이션 개발에 혁신적인 기술이다. 초기 설정은 복잡할 수 있지만, 한 번 구축하면 강력한 실시간 통신 기능을 제공한다. 

