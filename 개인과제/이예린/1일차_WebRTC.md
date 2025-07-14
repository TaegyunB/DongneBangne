## WebRTC란?
- WebRTC (Web Real-Time Communication)
- 다양한 플랫폼에서 가능한 실시간 통신 기술
- 화상 채팅, 음성 통화, 파일 공유 등등을 웹 브라우저, 모바일 앱, 데스크탑 앱에서도 상요할 수 있는 오픈소스 프로젝트임
- WebRTC를 사용하는 유명한 서비스들 중 Google Meet, Discord, Zoom 등 존재함
- 서버 없이도 P2P (Peer To Peer)로 연결되어 데이터를 주고받을 수 있음
  - P2P로 연결되기 위한 과정에서 `중계 서버`는 필요함

## WebRTC 구성 방식
- `MediaStream`: 카메라와 마이크 등으 ㅣ데이터 스트림 접근
- `RTCeerConnection`: 암호화 및 대역폭 관리 및 오디오, 비디오 연결
- `RTCDataChannel`: 일반적인 데이터의 P2P 통신
이 3가지 API를 통해서 데이터 교환이 이루어지며 `RTCPeerConnection`들이 적절하게 데이터를 교환할 수 있게 처리해주는 과정을 `시그널링`이라고 한다.
![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fvelog.velcdn.com%2Fimages%2Fjsb100800%2Fpost%2Feb72ec7d-c0b9-4038-9851-df1b27524b7e%2Fimage.webp)

## 주요 개념 및 용어
- 시그널링
- SDP (Session Description Protocol)
- ICE (Interactive Connectivity Establishment)
- STUN/TURN

### 시그널링
P2P로 연결하기 위해 중계 서버가 필요하다.
이 중계서버를 통해서 서로를 찾고 연결을 시작하는 과정이 바로 `시그널링(Signaling)`이다.
소개팅 주선자처럼 양쪽에서 서로의 정보를 전달해주는 역할을 한다.
> 이 시그널링은 서버를 통해 이루어지게 되고, 어떻게 구현할지는 자유다.
`WebSocket`, `HTTP` 등 적절한 프로토콜을 개발자가 선택하면 된다.

> 덧붙이자면, 이 시그널링 서버는 연결 과정에서만 필요하고 더 이상 P2P 연결을 위한 시그널링 서버는 필요하지 않는다.
이후 통신은 webRTC를 통해 P2P로 통신을 하게 된다.

### SDP (Session Description Protocol)
SDP는 `"무엇으로 소통할지, 어떻게 소통할지"`에 대한 정보가 담겨있는 양식이다. 즉, `"나는 이런 오디오, 비디오를 지원해"`라고 알려주는 `자기소개서`와 같다고 보면 된다.

- 세션 정보
- 네트워크 정보
- 미디어의 종류
- 코덱
- 스트림의 속성
등등

연결하는 쪽이 `Offer SDP`를 생성하고, 받는 쪽에서는 `Answer SDP`로 응답한다.

이 정보들을 통해서 양쪽 피어가 지원하는 코덱, 해상도 등을 협상하여 최적의 설정을 결정하게 되는 것이다.

``` shell
v=0
o=- 1234567890 1234567890 IN IP4 127.0.0.1
s=-
t=0 0
a=group:BUNDLE audio video
m=audio 9 UDP/TLS/RTP/SAVPF 111
c=IN IP4 0.0.0.0
a=rtcp:9 IN IP4 0.0.0.0
a=ice-ufrag:someufrag
a=ice-pwd:someicepwd
a=fingerprint:sha-256 somefingerprint
...
```

### ICE (Interactive Connectivity Establishment)
자기소개서를 작성했으면 이제 어떻게 연락을 해야할지 정해야 한다.
ICE는 `"나한테 연락하려면 이 주소로 연락해!"`라고 알려주는 과정이다.
즉, 두 피어 간의 `최적의 통신 경로`를 찾기 위해 사용되는 프레임워크이다.

`NAT(Network Address TRanslatin)`와 방화벽 등의 네트워크 장벽을 극복하고 직접적인 연결을 가능하게 하는 중요한 역할을 한다.
- 연결 후보 수집
  - 가능한 모든 연결 방법(후보)을 찾아냄
- 연결성 검사
    - 각 후보에 대해 실제 연결 가능 여부를 테스트
- 우선순위 결정
    - 가장 효율적인 연결 경로 (지연시간이 가장 짧은 연결을 선호)를 선택
- NAT 및 방화벽 통과
    - 다양한 네트워크 환경에서의 연결을 가능하게 함


지연 시간이 가장 짧은 경로를 찾고, 아래 옵션을 순서대로 시도한다.
> 1. 직접 UDP 연결, 사설 IP (이 경우에만 STUN 서버는 피어의 네트워크 연결 주소를 찾는데 사용됨)
> 2. HTTP 포트를 통한 직접 TCP 연결
> 3. HTTPS 포트를 통한 직접 TCP 연결
> 4. relay/TURN 서버를 통한 간접 연결 (직접 연결이 실패한 경우, 예를 들어, 한 피어가 NAT 통과를 차단하는 방화벽 뒤에 있는 경우)

### STUN / TURN

![alt text](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdna%2FkSjlq%2FbtsJzMY9Y9k%2FAAAAAAAAAAAAAAAAAAAAAMaC5Ky7Uf8rpWzPmKyFh0FCzyCKwIXX__7xy8oTdpI_%2Fimg.png%3Fcredential%3DyqXZFxpELC7KVnFOS48ylbz2pIh7yKj8%26expires%3D1753973999%26allow_ip%3D%26allow_referer%3D%26signature%3DwUIELqGoy%252BqVPoiS%252BT4f8oavCLA%253D)
일반적으로 같은 네트워크를 사용하는 상황보다 완전히 다른 환경에서 다른 피어와 연결하는 상황이 더 많다.

`같은 네트워크 망`에 있으면 `사설 IP로 P2P 연결이 가능`하나, `서로 다른 네트워크 환경`에서 각자의 `사설 IP로는 연결이 불가능`하다.

이런 상황에서 `STUN` 서버와 `TURN` 서버가 사용된다.

### STUN
- STUN (Session Traversal Utilities for NAT)
- 우리의 컴퓨터가 `"나 지금 어디 있는 거야?"`라고 물어볼 때 대답해주는 서버
- NAT 환경에서 Private IP를 별도로 가지고 있기 때문에 P2P 통신이 불가능하다.

  따라서 클라이언트는 `자신의 Public IP를 확인`하기 위해 `STUN 서버로 요청`을 보내고 서버로부터 자신의 Public IP를 받게 된다.

### TURN
- TURN (Traversal Using Relays around NAT)
- `"너희 둘이 만나기 어려우면, 내가 중간에서 메시지를 전해줄게!"`라고 하는 중계자
- `STUN 서버로 해결할 수 없는 상황`에서 사용됨
- 일부 고집이 센 NAT 구성에서는 외부 서버와의 직접적인 P2P 연결을 허용하지 않기 때문

그래서 TURN 서버의 특징으로는 완벽한 중계 서비스를 제공하지만, 상대적으로 느리고 리소스를 많이 사용한다.

그래서 통 WebRTC는 아래와 같이 행동한다.
1. 먼저 STUN으로 해보자. 잘 되면 이걸로 간다.
2. STUN이 안 된다. 그럼 TURN한테 부탁하자.

즉, `STUN` 서버로 먼저 시도하고 안되면 `TURN` 서버로 시도하게 된다.

``` js
const peerConnection = new RTCPeerConnection({
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' }, // STUN 서버
    { 
      urls: 'turn:your-turn-server.com',      // TURN 서버
      username: '유저네임',
      credential: '비밀번호'
    }
  ]
});
```


## 주의 및 Media Server
WebRTC의 구현 방식에는 크게 3가지로 Mesh, SFU, MCU 방식이 있다.
지금까지 위에서 말한 방식은 Mesh 방식으로 서버의 자원이 적게 들지만 Peer 수가 늘어날 수록 Client 사이드의 과부하가 급격하게 증가하는 방식이다.

만약 N명이 참여하면 N(N - 1) / 2개의 연결이 필요하게 된다.
- 참가자가 10명이면 45개의 연결이 생성되게 됨
- 이 경우 CPU에 과부하가 올 가능성이 매우 높음
- 대신 보통 P2P 구현은 서버에 별도의 부하가 없고, Peer간의 직접적인 연결로 실시간성이 보장됨
  - 그래서 매우 적은 사용자들만 필요로 할 때 사용하게 되는 구현 방식


`Mesh` 방식에서는 `Media Server`가 필요하지 않다.
`Media Server`는 `SFU`, `MCU` 방식의 WebRTC에서 필요한 서버로 각각의 Peer들은 Media Server에게 미디어 스트림들을 쏴주고 Media Server에서 미디어 트래픽을 관리하여 각각의 Peer에게 다시 배포해주는 멀티미디어 미들웨어이다.

즉, WebRTC의 특징은 p2p 통신이 아닌게 되는 것은 TURN Server와 유사하다고 할 수 있고 클라이언트에 부하가 현저히 줄어드는 대신 서버의 부하가 커지며 구현의 난도가 상당히 높다
![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fvelog.velcdn.com%2Fimages%2Fjsb100800%2Fpost%2F4747ddfb-106c-48d2-9de4-e3bcd8efbed5%2Fimage.png)

### SFU (Selective Forwarding Unit)
- 영리한 미디어 라우터
- 미디어 간의 트래픽을 중계하는 중앙 서버이며 서버와 클라이언트 간의 Peer을 연결함
    - 각 참가자로부터 하나의 스트림만 받아서 필요한 곳에 전달함
    - 참가자들의 대역폭과 성능에 따라 영상 품질을 조절할 수 있음
    - 확장성이 뛰어나고, 지연 시간도 비교적 짧음

작동 방식
> 1. A가 영상을 SFU로 한 번만 전송한다.
> 2. SFU가 이 영상을 B, C, D에게 각각 전달한다.
> 3. A는 업로드를 한 번만 하면 된다.
그래서 1:N 형식 또는 소규모 N:M 형식의 실시간 스트리밍이 필요한 상황에서 주로 채택된다.

### MCU (Multipoint ControlUnit)
- 영상을 받아서 섞은 다음에 `하나의 스트림`으로 만들어 보내게 됨
- 클라이언트에 부담이 가장 적고, 일관된 경험을 제공하지만 서버 리소스를 가장 많이 사용함
- 최대 단점으로는 webRTC의 최대 장점인 `실시간성이 보장되지 않을 수 있음`

작동 방식
> 1. A, B, C가 각자의 영상을 MCU로 보냄
> 2. MUC가 이 영상들을 하나의 스트림으로 만듦
> 3. 이 통합된 스트림을 모든 참가자에게 보내줌

- 비교
  - SFU: 유연하고 확장성이 좋다. 대규모 회의에 적합하다. (ex) Zoom
  - MCU: 일관된 경험을 제공하고 클라이언트 부담이 적다. 작은 회의나 낮은 성능의 기기에 좋다. (ex) GoogleMeet