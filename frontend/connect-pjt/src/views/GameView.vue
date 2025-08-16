<template>
  <div class="unity-wrapper">
    <iframe
      ref="unityFrame"
      src="/unity/index.html"
      width="2560px"
      height="1440px"
      frameborder="0"
      allowfullscreen
    ></iframe>
  </div>
  
  <!-- YouTube 동영상 (숨김) -->
  <div class="youtube-container" style="position: absolute; left: -9999px; top: -9999px; width: 1px; height: 1px; overflow: hidden;">
    <iframe
      ref="youtubeFrame"
      :src="youtubeSrc"
      width="1"
      height="1"
      frameborder="0"
      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
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
      videoId: 'pkc1XoilQIc', // YouTube 비디오 ID
    }
  },
  async mounted() {
    // Unity가 보낸 메시지 수신
    window.addEventListener('message', (event) => {
      // console.log('✅ Unity → Vue Type:', event.type)
      // console.log('✅ Unity → Vue Data:', event.data)

      try {
        // event.data가 문자열인 경우 JSON 파싱
        const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data

        // 메시지 핸들러 매핑
        const messageHandlers = {
          'unity-ready': () => {
            this.isUnityReady = true
          },
          'room-list': () => {
            this.getRoomList()
          },
          'create-room': () => {
            const roomData = this.parseUnityData(data.data)
            this.handleCreateRoom(roomData)
          },
          'join-room': () => {
            const roomData = this.parseUnityData(data.data)
            this.handleJoinRoom(roomData)
          },
          'leave-room': () => {
            const roomId = this.parseUnityData(data.data)
            this.handleLeaveRoom(roomId)
          },
          'user-ready': () => {
            const readyData = this.parseUnityData(data.data)
            this.handleReady(readyData)
          },
          'webrtc-connect': async () => {
            console.log('Unity → Vue WebRTC 연결 요청:')
            const roomId = this.parseUnityData(data.data)

            // roomId를 기반으로 WebRTC 연결 시작
            await this.startWebRTC(roomId)
          },
          'room-users': () => {
            console.log('Unity → Vue 방 유저 정보 전송:')
            const roomId = this.parseUnityData(data.data)

            console.log('roomId: ', roomId)

            this.getUsersInfo(roomId)
          },
          'start-game': () => {
              console.log('Unity → Vue 게임 시작 요청:')
              const jsonData = this.parseUnityData(data.data)
              const { roomId, userId } = jsonData

              this.roomId = roomId
              this.localId = userId

              // WebSocket 연결
              this.connectStompWebSocket(roomId, userId)
            },
          'answer-submit': () => {
            const answerData = this.parseUnityData(data.data)
            this.sendAnswerToServer(answerData)
          },
          'hint-request': () => {
            const hintData = this.parseUnityData(data.data)
            this.sendHintRequestToServer(hintData)
          },
          'unity-error': () => {
            console.error('Unity 오류 발생:', data.error)
            alert(
              `Unity 로딩 오류: ${data.error}\n\n브라우저를 새로고침하거나 다른 브라우저를 사용해주세요.`,
            )
          },
        }

        // 메시지 타입에 따른 핸들러 실행
        if (messageHandlers[data.type]) {
          messageHandlers[data.type]()
        } else {
          console.warn('알 수 없는 Unity 메시지 타입:', data.type)
        }
      } catch (error) {
        console.error('메시지 파싱 오류:', error)
      }
    })

    // 유저 정보 받아오기
    try {
      // Unity가 준비되었는지 확인
      if (this.isUnityReady) {
        // 유저 정보 받아오기
        await this.getUserInfo()

        // 방 정보 받아오기
        await this.getRoomList()
      } else {
        const onUnityReady = (event) => {
          try {
            const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data
            if (data && data.type === 'unity-ready') {
              this.isUnityReady = true
              this.getUserInfo()
              this.getRoomList()
            }
          } catch (_) {}
        }
        window.addEventListener('message', onUnityReady, { once: true })
      }
    } catch (error) {
      console.error('유저 정보 조회 실패:', error)
    }

    // STOMP 연결
    //this.connectStompWebSocket();

    // STOMP WebSocket 연결 시작
    // this.connectStompWebSocket() // Unity에서 start-game 메시지로 연결

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
    // Unity 데이터 파싱 헬퍼 메서드
    parseUnityData(data) {
      return typeof data === 'string' ? JSON.parse(data) : data
    },
    async startWebRTC(roomId) {
      try {
        await this.initLocalMedia() // 카메라, 마이크 준비
        await this.connectSignalingServer(roomId) // 시그널링 서버 연결
        if (this.isInitiator) {
          await this.startAsCaller()
        } else {
          // 수신자: offer를 기다림
          console.log('[RTC] Waiting for offer…')
        }
      } catch (err) {
        // 초기화 실패 시 오류 처리
        console.error('[Init] error:', err)
      }
    },
    async endWebRTC() {
      try {
        // WebRTC 연결 종료
        if (this.pc) {
          this.pc.close()
          this.pc = null
        }

        // WebSocket 연결 종료
        if (this.ws) {
          this.ws.close()
          this.ws = null
        }

        // 로컬 스트림 정리
        if (this.localStream) {
          this.localStream.getTracks().forEach((track) => track.stop())
          this.localStream = null
        }

        // 원격 스트림 정리
        if (this.remoteStream) {
          this.remoteStream.getTracks().forEach((track) => track.stop())
          this.remoteStream = null
        }

        // 비디오 요소 정리
        if (this.$refs.localVideo) {
          this.$refs.localVideo.srcObject = null
        }
        if (this.$refs.remoteVideo) {
          this.$refs.remoteVideo.srcObject = null
        }

        // Unity에 영상 전송하는 setInterval 정리
        if (this.frameIntervalId) {
          clearInterval(this.frameIntervalId)
          this.frameIntervalId = null
        }

        // 상태 초기화
        this.pendingCandidates = []
        this.isInitiator = true
        this.isWaiting = true
        this.peerClosed = false

        console.log('WebRTC 연결이 성공적으로 종료되었습니다.')
      } catch (error) {
        console.error('WebRTC 종료 중 오류 발생:', error)
      }
    },
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
      this.createPeerIfNeeded()

      const offer = await this.pc.createOffer()
      await this.pc.setLocalDescription(offer)

      this.safeSend({
        type: 'offer',
        offer: offer,
        from: this.localId,
        to: this.remoteId,
      })
      console.log('offer전송완료')
    },
    connectSignalingServer(roomId) {
      // 시그널링 서버 연결
      return new Promise((resolve, reject) => {
        this.ws = new WebSocket(`wss://i13a708.p.ssafy.io/signal/${roomId}`) // 변수명 수정
        this.ws.onopen = () => {
          console.log('시그널링 서버 연결 성공')
          resolve()
        }
        this.ws.onerror = (error) => {
          console.error('시그널링 서버 오류:', error)
          reject(error)
        }
        this.ws.onclose = (event) => {
          console.warn('시그널링 서버 연결 종료', event.code, event.reason)
          //reject(new Error('시그널링 서버 연결 종료'));
        }
        this.ws.onmessage = this.onSignalingMessage
      })
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
      const data = JSON.parse(event.data)
      switch (data.type) {
        case 'offer':
          this.handleOffer(data)
          break
        case 'answer':
          this.handleAnswer(data)
          break
        case 'candidate':
          this.handleRemoteCandidate(data)
          break
        case 'leave':
          this.handleLeave()
          break
        default:
          console.warn('Unknown message type:', data.type)
          break
      }
    },
    async handleOffer(data) {
      // 매개변수 수정
      this.isInitiator = false
      this.createPeerIfNeeded()

      await this.pc.setRemoteDescription(new RTCSessionDescription(data.offer)) // data.offer로 수정
      await this.flushPendingCandidates()

      const answer = await this.pc.createAnswer()
      await this.pc.setLocalDescription(answer)

      this.safeSend({
        type: 'answer',
        answer: answer,
        from: this.localId,
        to: data.from,
      })
      console.log('answer전송완료')
    },
    async handleAnswer(data) {
      // 매개변수 수정
      // 피어 생성
      if (!this.pc) this.createPeerIfNeeded()

      // 상대방의 세션 설명 설정
      await this.pc.setRemoteDescription(new RTCSessionDescription(data.answer)) // data.answer로 수정

      // 미처리 후보들 적용
      await this.flushPendingCandidates()

      console.log('answer수신완료')
    },
    async handleRemoteCandidate(data) {
      // 매개변수 수정
      if (!data.candidate || !data.candidate.candidate) return // data.candidate로 수정

      // Remote SDP 없으면 큐에 저장
      if (!this.pc || !this.pc.remoteDescription) {
        this.pendingCandidates.push(data.candidate) // data.candidate로 수정
        return
      }

      try {
        await this.pc.addIceCandidate(new RTCIceCandidate(data.candidate)) // data.candidate로 수정
      } catch (error) {
        console.error('ICE candidate 추가 실패:', error)

        // 추가 실패시 큐에 다시 추가
        this.pendingCandidates.push(data.candidate) // data.candidate로 수정
      }

      console.log('candidate 수신완료')
    },
    async flushPendingCandidates() {
      if (!this.pc || !this.pc.remoteDescription) return
      if (!this.pendingCandidates.length) return

      const waiting = [...this.pendingCandidates]
      this.pendingCandidates = []
      for (const c of waiting) {
        try {
          await this.pc.addIceCandidate(new RTCIceCandidate(c))
        } catch (e) {
          console.error('[RTC] addIceCandidate failed (flush):', e, c)
        }
      }
      console.log(`[RTC] Flushed ${waiting.length} pending candidates.`)
    },
    handleLeave() {
      try {
        this.pc && this.pc.close()
      } catch {}
      this.pc = null
      this.pendingCandidates = []
      console.log('[RTC] Peer closed.')
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
    async getRoomList() {
      try {
        const response = await api.get('/api/v1/game-rooms')
        const roomList = response.data
        this.sendRoomListToUnity(roomList)
      } catch (error) {
        console.error('방 정보 조회 실패', error)
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
          }),
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
          data: JSON.stringify(wrapper),
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
        const response = await api.post('/api/v1/game-rooms', roomData, {
          headers: { 'Content-Type': 'application/json' },
        })

        //console.log('방 생성 성공:', response.data)

        // 생성된 방 정보를 Unity로 전송
        this.sendRoomCreatedToUnity(response.data)
      } catch (error) {
        console.error('방 생성 실패:', error)
        // 에러 정보를 Unity로 전송
        this.sendErrorToUnity('방 생성에 실패했습니다.')
      }
    },
    async handleJoinRoom(roomData) {
      try {
        console.log('Unity → Vue 방 입장 요청:', roomData)

        const roomId = roomData.roomId

        // API로 방 참여
        const response = await api.post(`/api/v1/game-rooms/${roomId}/join`, roomData, {
          headers: { 'Content-Type': 'application/json' },
        })
        console.log('방 입장 성공:', response.data)

        this.sendJoinRoomToUnity(response.data)
      } catch (error) {
        console.error('방 입장 실패:', error)
      }
    },
    async handleLeaveRoom(roomId) {
      try {
        const response = await api.delete(`/api/v1/game-rooms/${roomId}/leave`)
        console.log('방 퇴장 성공:', response.data)

        // WebRTC 연결 종료
        await this.endWebRTC();

        this.sendLeaveRoomToUnity(roomId)
      } catch (error) {
        console.error('방 퇴장 실패:', error)
      }
    },
    async handleReady(readyData) {
      try {
        console.log('Unity → Vue 게임 준비 요청:', readyData)

        const roomId = readyData.roomId

        // API로 게임 준비
        const response = await api.put(`/api/v1/game-rooms/${roomId}/ready`, readyData)
        //console.log('게임 준비 성공:', response.data)

        this.sendReadyAnswerToUnity(response.data)
      } catch (error) {
        console.error('게임 준비 실패:', error)
      }
    },
    async getUsersInfo(roomId) {
      console.log(`/api/v1/game-rooms/${roomId}/waiting-users`)

      try {
        roomId = parseInt(roomId)
        const response = await api.get(`/api/v1/game-rooms/${roomId}/waiting-users`)
        console.log('방 유저 정보 조회 성공:', response.data)
        this.sendUsersInfoToUnity(response.data)
      } catch (error) {
        console.error('방 유저 정보 조회 실패:', error)
      }
    },

    // 방 생성 성공 정보를 Unity로 전송
    sendRoomCreatedToUnity(roomInfo) {
      const unityFrame = this.$refs.unityFrame

      unityFrame.contentWindow.postMessage(
        JSON.stringify({
          type: 'room-created',
          data: JSON.stringify(roomInfo),
        }),
        '*',
      )

      console.log('Vue → Unity 방 생성 성공 전송:', roomInfo)
    },
    // 방 입장 성공 정보를 Unity로 전송
    sendJoinRoomToUnity(roomInfo) {
      const unityFrame = this.$refs.unityFrame

      unityFrame.contentWindow.postMessage(
        JSON.stringify({
          type: 'join-room',
          data: JSON.stringify(roomInfo),
        }),
        '*',
      )
    },

    // 방 퇴장 성공 정보를 Unity로 전송
    sendLeaveRoomToUnity(roomId) {
      const unityFrame = this.$refs.unityFrame

      unityFrame.contentWindow.postMessage(
        JSON.stringify({
          type: 'leave-room',
          data: `${roomId}`,
        }),
        '*',
      )
    },

    // 게임 준비 성공 정보를 Unity로 전송
    sendReadyAnswerToUnity(readyInfo) {
      const unityFrame = this.$refs.unityFrame

      unityFrame.contentWindow.postMessage(
        JSON.stringify({
          type: 'ready-answer',
          data: JSON.stringify(readyInfo),
        }),
        '*',
      )
    },

    sendUsersInfoToUnity(roomUsers) {
      const unityFrame = this.$refs.unityFrame

      const wrapper = { roomUsers: roomUsers }
    
      unityFrame.contentWindow.postMessage(
        JSON.stringify({
          type: 'users-info',
          data: JSON.stringify(wrapper),
        }),
        '*',
      )
    },
    // 에러 정보를 Unity로 전송
    sendErrorToUnity(errorMessage) {
      const unityFrame = this.$refs.unityFrame

      unityFrame.contentWindow.postMessage(
        JSON.stringify({
          type: 'error',
          data: errorMessage,
        }),
        '*',
      )

      console.log('Vue → Unity 에러 전송:', errorMessage)
    },

    // STOMP WebSocket 연결
    connectStompWebSocket(roomId, userId) {
      try {
        // STOMP 클라이언트 생성
        console.log('1. STOMP 클라이언트 생성')
        this.stompClient = new Client({
          webSocketFactory: () => new WebSocket(`wss://i13a708.p.ssafy.io/ws-game`),
          reconnectDelay: 5000, // 재연결 지연 시간 (5초)
          heartbeatIncoming: 4000, // 수신 하트비트
          heartbeatOutgoing: 4000, // 송신 하트비트
        })

        // 연결 성공 시 콜백
        this.stompClient.onConnect = (frame) => {
          console.log('✅ STOMP WebSocket 연결 성공:', frame)

          // 구독할 토픽들
          this.subscribeToTopics(roomId, userId)

          // 연결 성공 로그만 출력
          console.log('🎮 STOMP 연결 완료 - 게임 준비됨')
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
    subscribeToTopics(roomId, userId) {
      if (!this.stompClient || !this.stompClient.connected) {
        console.warn('STOMP 클라이언트가 연결되지 않았습니다.')
        return
      }

      console.log(`roomId: ${this.roomId}, userId: ${this.userId}`)
      try {
        // 1. 기본 구독 경로 (/sub)
        this.stompClient.subscribe('/sub', (message) => {
          console.log('✅ 기본 메시지 수신:', message.body)
        })

        if (roomId && roomId !== 'default') {
          // 2. 특정 게임방 구독 (/sub/games/{roomId})
          this.stompClient.subscribe(`/sub/games/${roomId}`, (message) => {
            console.log('🎮 게임방 메시지 수신:', message.body)
            this.handleGameMessage(JSON.parse(message.body))
          })

          // 3. 힌트 메시지 구독 (/queue/hint/{userId})
          this.stompClient.subscribe(`/queue/hint/${userId}`, (message) => {
            console.log('💡 힌트 메시지 수신:', message.body)
            this.handleHintMessage(JSON.parse(message.body))
          })
        }
        console.log('📡 STOMP 토픽 구독 완료')
        console.log(`roomId: ${this.roomId}, userId: ${this.userId}`)
      } catch (error) {
        console.error('STOMP 토픽 구독 오류:', error)
      }
    },

    // 게임 메시지 처리
    handleGameMessage(message) {
      console.log('🎮 게임 메시지 처리:', message)

      try {
        const { type, data } = message

        switch (type) {
          case 'GAME_START':            // 게임 시작
            this.handleGameStart(data)
            break
          case 'ROUND_QUESTION':        // 문제 전송
            this.handleRoundQuestion(data)
            break
          case 'ROUND_END':             // 라운드 종료
            this.handleRoundEnd(data)
            break
          case 'GAME_END':              // 게임 종료
            this.handleGameEnd(data)
            break
          case 'ANSWER_RESULT':         // 정답 결과
            this.handleAnswerResult(data)
            break
          case 'ANSWER_REJECTED':       // 정답 거부
            this.handleAnswerRejected(data)
            break
          default:
            console.warn('알 수 없는 게임 메시지 타입:', type)
        }
      } catch (error) {
        console.error('게임 메시지 처리 오류:', error)
      }
    },

    handleHintMessage(message) {
      console.log('💡 힌트 메시지 처리:', message)
      try {
        const { type, data } = message

        switch (type) {
          case 'HINT_RESPONSE':         // 힌트 제공공
            this.handleHintResponse(data)
            break
          case 'HINT_REJECTED':         // 힌트 제공 불가
            this.handleHintRejected(data)
            break
          default:
            console.warn('알 수 없는 게임 메시지 타입:', type)
        }
      } catch (error) {
        console.error('게임 메시지 처리 오류:', error)
      }
    },

    // 게임 시작 처리
    handleGameStart(data) {
      console.log('🎮 게임 시작:', data)
      this.sendToUnity('game-start', data)
    },

    // 라운드 문제 처리
    handleRoundQuestion(data) {
      console.log('❓ 라운드 문제:', data)

      // 영상 재생
      const videoId = data.videoId
      this.changeYouTubeVideo(videoId)
      this.playYouTubeVideo()

      // 라운드 시작을 알림
      this.sendToUnity('round-question', data)
    },

    // 라운드 종료 처리
    handleRoundEnd(data) {
      console.log('🏁 라운드 종료:', data)
      this.sendToUnity('round-end', data)
    },

    // 게임 종료 처리
    handleGameEnd(data) {
      console.log('🎯 게임 종료:', data)
      this.sendToUnity('game-end', data)
    },

    // 정답 결과 처리
    handleAnswerResult(data) {
      console.log('✅ 정답 결과:', data)
      this.sendToUnity('answer-result', data)
    },

    // 정답 거부 처리
    handleAnswerRejected(data) {
      console.log('❌ 정답 거부:', data)
      this.sendToUnity('answer-rejected', data)
    },

    // 힌트 응답 처리
    handleHintResponse(data) {
      console.log('💡 힌트 응답:', data)
      this.sendToUnity('hint-response', data)
    },

    // 힌트 거부 처리
    handleHintRejected(data) {
      console.log('🚫 힌트 거부:', data)
      this.sendToUnity('hint-rejected', data)
    },

    // Unity로 메시지 전송
    sendToUnity(type, data) {
      const unityFrame = this.$refs.unityFrame
      if (unityFrame && unityFrame.contentWindow) {
        unityFrame.contentWindow.postMessage(
          JSON.stringify({
            type: type,
            data: JSON.stringify(data),
          }),
          '*',
        )
        console.log('🎮 Vue → Unity 전송:', type, data)
      }
    },

    // 정답 제출 (클라이언트 → 서버)
    sendAnswerToServer(answerData) {
      try {
        const message = {
          type: 'ANSWER_SUBMIT',
          data: {
            roomId: this.roomId,
            userId: this.localId,
            answer: answerData.answer,
            timestamp: new Date().toISOString(),
          },
        }

        this.sendStompMessage('/games/answer', message)
        console.log('📤 정답 제출 전송:', message)
      } catch (error) {
        console.error('정답 제출 전송 오류:', error)
      }
    },

    // 힌트 요청 (클라이언트 → 서버)
    sendHintRequestToServer(hintData) {
      try {
        const message = {
          type: 'HINT_REQUEST',
          data: {
            roomId: this.roomId,
            userId: this.localId,
            hintType: hintData.hintType,
            timestamp: new Date().toISOString(),
          },
        }

        this.sendStompMessage('/games/hint', message)
        console.log('💡 힌트 요청 전송:', message)
      } catch (error) {
        console.error('힌트 요청 전송 오류:', error)
      }
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
            'content-type': 'application/json',
          },
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
    
    // YouTube 비디오 ID 변경
    changeYouTubeVideo(newVideoId) {
      const iframe = this.youtubeIframe;
        if (iframe) {
          iframe.src = `https://youtube.com/embed/${newVideoId}?si=8IsRoXmN3OS1AwUH&enablejsapi=1`;
        }

      console.log('YouTube 비디오 ID 변경:', newVideoId)
    },
    
    // YouTube 동영상 재생
    playYouTubeVideo() {
      const iframe = this.$refs.youtubeFrame
      if (iframe) {
        try {
          iframe.contentWindow?.postMessage('{"event":"command","func":"playVideo","args":""}', 'https://www.youtube.com')
          console.log('YouTube 동영상 재생')
        } catch (error) {
          console.error('YouTube 재생 명령 전송 중 오류:', error)
        }
      }
    },
    
    // YouTube 동영상 정지
    pauseYouTubeVideo() {
      const iframe = this.$refs.youtubeFrame
      if (iframe) {
        try {
          iframe.contentWindow?.postMessage('{"event":"command","func":"pauseVideo","args":""}', 'https://www.youtube.com')
          console.log('YouTube 동영상 정지')
        } catch (error) {
          console.error('YouTube 정지 명령 전송 중 오류:', error)
        }
      }
    },
  },
  
  computed: {
    // YouTube iframe src 계산
    youtubeSrc() {
      return `https://youtube.com/embed/${this.videoId}?si=8IsRoXmN3OS1AwUH&enablejsapi=1`
    }
  },
  
  name: 'UnityView',
}
</script>

<style scoped>
.unity-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background-color: #000;
  overflow: hidden;
}
iframe {
  border: none;
  /* 화면 가득히 배치하는 옵션 */
  width: 100vw;
  height: 100vh;
  
  /* 정중앙에 배치하는 옵션 (주석 처리) */
  /* width: 100%;
  height: 100%;
  max-width: 2560px;
  max-height: 1440px;
  object-fit: contain; */
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

.youtube-container {
  position: absolute;
  left: -9999px;
  top: -9999px;
  width: 1px;
  height: 1px;
  overflow: hidden;
}
</style>
