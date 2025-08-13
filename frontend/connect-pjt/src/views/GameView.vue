<template>
  <div class="unity-wrapper">
    <iframe
      ref="unityFrame"
      src="/unity/index.html"
      width="2560"
      height="1440"
      frameborder="0"
      allowfullscreen
    ></iframe>
  </div>
  <div class="localCamera">
    <video ref="localVideo" id="localVideo" autoplay playsinline></video>
    <video ref="remoteVideo" id="remoteVideo" autoplay playsinline></video>
  </div>
  <canvas id="localCanvas" style="display: none"></canvas>
  <canvas id="remoteCanvas" style="display: none"></canvas>
</template>

<script>
import api from '@/api/axios'
import { Client } from '@stomp/stompjs'
export default {
  data() {
    return {
      ws: null, // WebSocket
      pc: null, // RTCPeerConnection
      localStream: null, // LocalMedia
      remoteStream: null, // RemoteMedia
      pendingCandidates: [], // 미처리 후보 목록
      isInitiator: true, // 통화 시작자 여부
      isWaiting: true, // 상대방 대기중
      peerClosed: false, // 상대방 종료 여부
      frameIntervalId: null, // 프레임 전송 반복 ID
      roomId: 'default', // 방 아이디
      localId: 'ID', // 내 아이디
      remoteId: 'ID', // 상대방 아이디
      isUnityReady: false, // Unity 준비 여부
      stompClient: null, // STOMP 클라이언트
    }
  },
  async mounted() {

    // Unity가 보낸 메시지 수신
    window.addEventListener('message', (event) => {
      console.log('✅ Unity → Vue Type:', event.type)
      console.log('✅ Unity → Vue Data:', event.data)
      
      try {
        // event.data가 문자열인 경우 JSON 파싱
        const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data
        
        if(data.type === 'unity-ready'){
          this.isUnityReady = true;
        }
        else if(data.type === 'create-room'){
          // data.data도 JSON 문자열이므로 파싱
          const roomData = typeof data.data === 'string' ? JSON.parse(data.data) : data.data
          this.handleCreateRoom(roomData);
        }

      } catch (error) {
        console.error('메시지 파싱 오류:', error)
      }
    })

    // 유저 정보 받아오기
    try {

      // Unity가 준비되었는지 확인
      if(this.isUnityReady){

        // 유저 정보 받아오기
        await this.getUserInfo();

        // 방 정보 받아오기
        await this.getRoomList();
      }
      else{
        const onUnityReady = (event) => {
            try {
              const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data
              if (data && data.type === 'unity-ready') {
                this.isUnityReady = true
                this.getUserInfo();
                this.getRoomList();
              }
            } catch (_) {}
          }
          window.addEventListener('message', onUnityReady, { once: true })
      }
    }catch(error){
      console.error('유저 정보 조회 실패:', error);
    }

    // STOMP WebSocket 연결 시작
    // this.connectStompWebSocket()

    // try {
    //   await this.initLocalMedia() // 카메라, 마이크 준비
    //   await this.connectSignalingServer() // 시그널링 서버 연결
    //   if (this.isInitiator) {
    //     await this.startAsCaller()
    //   } else {
    //     // 수신자: offer를 기다림
    //     console.log('[RTC] Waiting for offer…')
    //   }
    // } catch (err) {
    //   // 초기화 실패 시 오류 처리
    //   console.error('[Init] error:', err)
    // }
  },

  // 컴포넌트 소멸 시 리소스 해제
  beforeDestroy() {
    try {
      this.ws && this.ws.close()
    } catch {}
    try {
      this.pc && this.pc.close()
    } catch {}
    if (this.frameIntervalId) {
      clearInterval(this.frameIntervalId)
    }
    // STOMP 연결 해제
    this.disconnectStompWebSocket()
    
    this.ws = null
    this.pc = null
    this.pendingCandidates = []
  },

  methods: {
    async initLocalMedia() {
      try {
        // 카메라, 마이크 접근 권한 요청
        this.localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
        this.$refs.localVideo.srcObject = this.localStream

        const video = document.getElementById('localVideo')
        const canvas = document.getElementById('localCanvas')
        const videoSender = 'local'

        // 30FPS마다 비디오 전송
        setInterval(() => {
          this.sendVideoFrameToUnity(video, canvas, videoSender)
        }, 1000 / 30)
      } catch (error) {
        console.log('카메라.마이크 접근 실패', error)
      }
    },
    createPeerIfNeeded() {
      // 피어 생성
      if (this.pc) return

      const config = {
        iceServers: [{ urls: 'stun:stun.1.google.com:19302' }], // 포트 번호 수정
      }
      this.pc = new RTCPeerConnection(config)

      // 트랙 추가
      this.localStream?.getTracks().forEach((track) => {
        this.pc.addTrack(track, this.localStream)
      })

      this.pc.ontrack = (event) => {
        // 상대방 비디오 스트림 처리
        if (!this.remoteStream) {
          this.remoteStream = new MediaStream()
        }
        if (event.streams && event.streams[0]) {
          this.remoteStream = event.streams[0]
        } else {
          this.remoteStream.addTrack(event.track)
        }
        this.$refs.remoteVideo.srcObject = this.remoteStream
      }

      // ICE 후보 발생 시 전송
      this.pc.onicecandidate = (e) => {
        if (e.candidate) {
          this.safeSend({
            type: 'candidate',
            candidate: {
              candidate: e.candidate.candidate,
              sdpMid: e.candidate.sdpMid,
              sdpMLineIndex: e.candidate.sdpMLineIndex,
              usernameFragment: e.candidate.usernameFragment,
            },
            roomId: this.roomId,
            from: this.localId,
            to: this.remoteId,
          })
        } else {
          // (선택) end-of-candidates 신호 필요 시 보낼 수 있음
          // this.safeSend({ type: 'candidate', candidate: null, roomId: this.roomId, from: this.localId, to: this.remoteId });
        }
      }

      // ICE 연결 상태 변경 시 로깅
      this.pc.oniceconnectionstatechange = () => {
        console.log('[ICE]', this.pc.iceConnectionState)

        const state = this.pc.iceConnectionState
        if (state == 'connected') {
          const video = document.getElementById('remoteVideo')
          const canvas = document.getElementById('remoteCanvas')
          const videoSender = 'remote'

          this.frameIntervalId = setInterval(() => {
            this.sendVideoFrameToUnity(video, canvas, videoSender)
          }, 1000 / 30)
          console.log('연결 시작')
        }
        if (state === 'disconnected' || state === 'closed' || state === 'failed') {
          this.peerClosed = true
          console.log('상대방이 연결을 종료했습니다')

          if (this.frameIntervalId) {
            clearInterval(this.frameIntervalId)
            this.frameIntervalId = null
          }
        }
      }
    },

    async startAsCaller() {
      this.createPeerIfNeeded();

      const offer = await this.pc.createOffer();
      await this.pc.setLocalDescription(offer);

      this.safeSend({
        type: 'offer',
        offer: offer,
        from: this.localId,
        to: this.remoteId,
      });
      console.log('offer전송완료')
    },
    connectSignalingServer() {
      // 시그널링 서버 연결
      return new Promise((resolve, reject) => {
        this.ws = new WebSocket(`wss://i13a708.p.ssafy.io/signal`) // 변수명 수정
        this.ws.onopen = () => {
          console.log('시그널링 서버 연결 성공')
          resolve()
        };
        this.ws.onerror = (error) => {
          console.error('시그널링 서버 오류:', error);
          reject(error);
        };
        this.ws.onclose = (event) => {
          console.warn('시그널링 서버 연결 종료', event.code, event.reason);
          //reject(new Error('시그널링 서버 연결 종료'));
        };
        this.ws.onmessage = this.onSignalingMessage;
      });
    },
    safeSend(obj) {
      // 메시지 전송 함수
      const msg = JSON.stringify(obj)
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(msg)
      } else if (this.ws) {
        this.ws.addEventListener('open', () => this.ws.send(msg), { once: true })
      } else {
        console.warn('[WS] not connected, drop message:', obj)
      }
    },
    async onSignalingMessage(event) {
      const data = JSON.parse(event.data);
      switch(data.type){
        case 'offer':
          this.handleOffer(data);
          break;
        case 'answer':
          this.handleAnswer(data);
          break;
        case 'candidate':
          this.handleRemoteCandidate(data);
          break;
        case 'leave':
          this.handleLeave();
          break;
        default:
          console.warn('Unknown message type:', data.type);
          break;
      }
    },
    async handleOffer(data) { // 매개변수 수정
      this.isInitiator = false;
      this.createPeerIfNeeded();

      await this.pc.setRemoteDescription(new RTCSessionDescription(data.offer)); // data.offer로 수정
      await this.flushPendingCandidates();

      const answer = await this.pc.createAnswer();
      await this.pc.setLocalDescription(answer);

      this.safeSend({
        type: 'answer',
        answer: answer,
        from: this.localId,
        to: data.from,
      });
      console.log('answer전송완료')
    },
    async handleAnswer(data) { // 매개변수 수정
      // 피어 생성
      if (!this.pc) this.createPeerIfNeeded();

      // 상대방의 세션 설명 설정
      await this.pc.setRemoteDescription(new RTCSessionDescription(data.answer)); // data.answer로 수정

      // 미처리 후보들 적용
      await this.flushPendingCandidates();

      console.log('answer수신완료')
    },
    async handleRemoteCandidate(data) { // 매개변수 수정
      if (!data.candidate || !data.candidate.candidate) return; // data.candidate로 수정

      // Remote SDP 없으면 큐에 저장
      if(!this.pc || !this.pc.remoteDescription) {
        this.pendingCandidates.push(data.candidate); // data.candidate로 수정
        return;
      }

      try {
        await this.pc.addIceCandidate(new RTCIceCandidate(data.candidate)); // data.candidate로 수정
      }catch(error){
        console.error('ICE candidate 추가 실패:', error);

        // 추가 실패시 큐에 다시 추가
        this.pendingCandidates.push(data.candidate) // data.candidate로 수정
      }
      
      console.log('candidate 수신완료')
    },
    async flushPendingCandidates() {
      if (!this.pc || !this.pc.remoteDescription) return;
      if (!this.pendingCandidates.length) return;

      const waiting = [...this.pendingCandidates];
      this.pendingCandidates = [];
      for (const c of waiting) {
        try {
          await this.pc.addIceCandidate(new RTCIceCandidate(c));
        } catch (e) {
          console.error('[RTC] addIceCandidate failed (flush):', e, c);
        }
      }
      console.log(`[RTC] Flushed ${waiting.length} pending candidates.`);
    },
    handleLeave(){
      try { this.pc && this.pc.close(); } catch {}
      this.pc = null;
      this.pendingCandidates = [];
      console.log('[RTC] Peer closed.');
    },
    // 유저 정보 조회 함수
    async getUserInfo() {
      try {
        const response = await api.get('/api/v1/main/me')
        const userInfo = response.data
        this.sendUserInfoToUnity(userInfo)

        console.log(userInfo)
      } catch (error) {
        console.error('유저 정보 조회 실패:', error)
      }
    },
    // 방 정보 조회 함수
    async getRoomList(){
      try{
        const response = await api.get('/api/v1/game-rooms')
        const roomList = response.data
        this.sendRoomListToUnity(roomList)
      } catch(error){
        console.error('방 정보 조회 실패', error);
      }
    },
    // Unity 전송 함수
    sendVideoFrameToUnity(video, canvas, videoSender) {
      const ctx = canvas.getContext('2d')

      canvas.width = video.videoWidth
      canvas.height = video.videoHeight

      ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

      const imageData = canvas.toDataURL('image/png', 0.3)
      const unityFrame = this.$refs.unityFrame
      unityFrame.contentWindow.postMessage(
        JSON.stringify({
          type: 'video-frame',
          sender: videoSender,
          data: imageData,
        }),
        '*',
      )
    },
    sendUserInfoToUnity(userInfo) {
      const unityFrame = this.$refs.unityFrame

      unityFrame.contentWindow.postMessage(
        JSON.stringify({
          type: 'local-user-info',
          data: JSON.stringify({
            userid: userInfo.userId,
            nickname: userInfo.nickname,
            profileimage: userInfo.profileImage,
            personalpoint: userInfo.personalPoint,
          })
        }),
        '*',
      )

      console.log('Vue → Unity 유저 정보 전송: ', userInfo)
    },
    sendRoomListToUnity(roomList) {
      const unityFrame = this.$refs.unityFrame

      // Unity JsonUtility 호환을 위해 래퍼 객체로 감싸기
      const wrapper = { rooms: roomList }
      
      unityFrame.contentWindow.postMessage(
        JSON.stringify({
          type: 'room-list',
          data: JSON.stringify(wrapper)
        }),
        '*',
      )

      console.log('Vue → Unity 방 목록 전송: ', roomList)
    },
    
    // Unity에서 방 생성 요청 처리
    async handleCreateRoom(roomData) {
      try {
        console.log('Unity → Vue 방 생성 요청:', roomData)
        
        // API로 방 생성 요청
        const response = await api.post('/api/v1/game-rooms', roomData, { headers: { 'Content-Type': 'application/json' } })
        
        console.log('방 생성 성공:', response.data)
        
        // 생성된 방 정보를 Unity로 전송
        this.sendRoomCreatedToUnity(response.data)
        
      } catch (error) {
        console.error('방 생성 실패:', error)
        // 에러 정보를 Unity로 전송
        this.sendErrorToUnity('방 생성에 실패했습니다.')
      }
    },
    
    // 방 생성 성공 정보를 Unity로 전송
    sendRoomCreatedToUnity(roomInfo) {
      const unityFrame = this.$refs.unityFrame
      
      unityFrame.contentWindow.postMessage(
        JSON.stringify({
          type: 'room-created',
          data: JSON.stringify(roomInfo)
        }),
        '*'
      )
      
      console.log('Vue → Unity 방 생성 성공 전송:', roomInfo)
    },
    
    // 에러 정보를 Unity로 전송
    sendErrorToUnity(errorMessage) {
      const unityFrame = this.$refs.unityFrame
      
      unityFrame.contentWindow.postMessage(
        JSON.stringify({
          type: 'error',
          data: errorMessage
        }),
        '*'
      )
      
      console.log('Vue → Unity 에러 전송:', errorMessage)
    },

    // STOMP WebSocket 연결
    connectStompWebSocket() {
      try {
        // STOMP 클라이언트 생성
        this.stompClient = new Client({
          brokerURL: 'ws://localhost:8080/ws-game', // WebSocket 엔드포인트
          debug: function (str) {
            console.log('STOMP Debug:', str)
          },
          reconnectDelay: 5000, // 재연결 지연 시간 (5초)
          heartbeatIncoming: 4000, // 수신 하트비트
          heartbeatOutgoing: 4000, // 송신 하트비트
        })

        // 연결 성공 시 콜백
        this.stompClient.onConnect = (frame) => {
          console.log('✅ STOMP WebSocket 연결 성공:', frame)
          
          // 구독할 토픽들
          this.subscribeToTopics()
          
                  // 연결 확인 메시지 전송
        this.sendStompMessage('/pub/test', {
          message: 'STOMP 연결 테스트',
          timestamp: new Date().toISOString()
        })
        }

        // 연결 실패 시 콜백
        this.stompClient.onStompError = (frame) => {
          console.error('❌ STOMP 연결 오류:', frame)
        }

        // 연결 해제 시 콜백
        this.stompClient.onDisconnect = () => {
          console.log('🔌 STOMP WebSocket 연결 해제')
        }

        // WebSocket 연결 활성화
        this.stompClient.activate()
        
      } catch (error) {
        console.error('STOMP 클라이언트 생성 오류:', error)
      }
    },

    // STOMP 토픽 구독
    subscribeToTopics() {
      if (!this.stompClient || !this.stompClient.connected) {
        console.warn('STOMP 클라이언트가 연결되지 않았습니다.')
        return
      }

      // 연결 확인용 메시지 구독
      this.stompClient.subscribe('/sub/test', (message) => {
        console.log('✅ 연결 확인 메시지 수신:', message.body)
      })
    },

    // STOMP 메시지 전송 (/pub로 클라이언트에서 서버로)
    sendStompMessage(destination, message) {
      if (!this.stompClient || !this.stompClient.connected) {
        console.warn('STOMP 클라이언트가 연결되지 않았습니다.')
        return
      }

      try {
        // /pub 접두사 추가
        const pubDestination = destination.startsWith('/pub') ? destination : `/pub${destination}`
        
        this.stompClient.publish({
          destination: pubDestination,
          body: JSON.stringify(message),
          headers: {
            'content-type': 'application/json'
          }
        })
        console.log('📤 STOMP 메시지 전송:', pubDestination, message)
      } catch (error) {
        console.error('STOMP 메시지 전송 오류:', error)
      }
    },



    // STOMP 연결 해제
    disconnectStompWebSocket() {
      if (this.stompClient) {
        this.stompClient.deactivate()
        this.stompClient = null
        console.log('🔌 STOMP WebSocket 연결 해제 완료')
      }
    },
  },
  name: 'UnityView',
}
</script>

<style scoped>
.unity-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
}
iframe {
  border: none;
  width: 100%;
  height: 100%;
  max-width: 2560px;
  max-height: 1440px;
}
.localCamera {
  position: absolute;
  width: 1px;
  height: 1px;
  bottom: 0px;
  right: 0px;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}
.receiveCamera {
  position: absolute;
  width: 1px;
  height: 1px;
  bottom: 0px;
  right: 0px;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}
</style>
