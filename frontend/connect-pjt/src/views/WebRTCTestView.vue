<template>
  <div class="chat-container">
    <div class="video-chat-minimal">
      <div class="videos-container">
        <div class="remote-video-container">
          <video ref="remoteVideo" autoplay playsinline></video>
          <!-- <div class="floating-name">
            <span v-if="parentType === 'healthData'">{{ userName }}</span>
            <span v-else>{{ userNickname }}</span>
          </div> -->
          <div v-if="isWaiting" class="waiting-message">상대방의 화상통화 수신을 기다리고 있습니다.</div>
          <div v-if="peerClosed" class="call-ended-message">
            <div class="message-content">
              <i class="fas fa-phone-slash message-icon"></i>
              <p>상대방이 통화를 종료했습니다</p>
              <button @click="closeAndReturn" class="close-btn">화상통화 나가기</button>
            </div>
          </div>
        </div>
        <div class="local-video-container">
          <video ref="localVideo" autoplay playsinline muted></video>
          <div class="floating-name local">나</div>
        </div>
      </div>
      <div class="minimal-controls">
        <button class="icon-btn" @click="toggleMicrophone"><i class="fas fa-microphone"></i></button>
        <button class="icon-btn" @click="toggleCamera"><i class="fas fa-video"></i></button>
        <button class="icon-btn end" @click="endCall"><i class="fas fa-phone-slash"></i></button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
export default {
  data() {
    return {
      ws: null,                 // WebSocket
      pc: null,
      localStream: null,        // 내 카메라, 마이크 MediaStream
      peerConnection: null,     // RTCPeerConnection 인스턴스
      signalingServer: null,    // SignalingServer 연결 객체
      isWaiting: true,          // 상대방을 기다리고 있는지 여부
      peerClosed: false,        // 상대방이 종료했는지 여부
    };
  },
  mounted: async function () {
    await this.initLocalMedia();            // 카메라, 마이크 준비
    await this.connectSignalingServer();    // Signaling
    await this.startCall();                 // 통화 시작
    //await this.getNameInfo();
  },
  methods: {
    async initLocalMedia() {
      try {
        // navigator.mediaDevices.getUserMedia()는 브라우저에서 카메라와 마이크 권한을 요청하고, MediaStream객체를 호출하는 함수
        this.localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
        // localVideo에 내 스트림을 연결하는 부분으로 내 화면을 내 비디오에 바로 띄우는 작업
        this.$refs.localVideo.srcObject = this.localStream
      } catch (error) {
        console.log('카메라.마이크 접근 실패', error)
      }
    },
    //  -->startCall()함수는 RTCPeerConnection을 생성하고, 내 스트림을 상대에게 연결하고, SDP Offer와 ICE Candidate를 signaling 서버로 보내서 연결을 성립하는 함수
    async startCall() {
      try {
        // 0. RTCPeerConnection객체의 생성자에 들어갈 옵션객체 정의로, key가 iceServers고 value가 배열인 형태임
        // iceServers는 NAT뒤에 있는 클라이언트끼리 연결 할 수 있도록 STUN/TURN서버 정보를 넣어줌. 구글의 STUN서버 사용
        const configuration = {
          iceServers: [
            { urls: 'stun:stun.1.google.com.19302' }
          ]
        }

        // 1.RTCPeerConnection객체 생성
        this.peerConnection = new RTCPeerConnection(configuration)
        console.log("1피어커넥션객체생성")

        // 2. 내 MediaStream(내가 getUserMedia()로 가져온 객체로 비디오트랙과 오디오트랙이 담겨있음)을 피어커넥션 객체에 추가
        this.localStream.getTracks().forEach(
          track => {
            this.peerConnection.addTrack(track, this.localStream)
          }
        )
        console.log("2내미디어스트림추가")

        // 3.상대방으로부터 미디어 트랙을 받으면 remoteVideo에 연결(peerConnection.ontrack은 상대방이 addTrack()으로 트랙을 전송했을때 브라우저가 자동으로 발생시키는 이벤트)
        //  event.streams는 MediaStream객체들의 배열
        this.peerConnection.ontrack = (event) => {
          console.log('상대방 스트림 수신')
          this.$refs.remoteVideo.srcObject = event.streams[0]
          this.isWaiting = false;//상대방 스트림 수신시 상대방이 채팅에 들어왔다는 거니까 기다리는 상태를 false로 변경
          console.log("상대방 스트림 수신 후 기다리는 상태", this.isWaiting)
        }

        // 연결 상태 변경 감지 - 상대방이 연결을 끊었을 때 감지
        this.peerConnection.oniceconnectionstatechange = () => {
          console.log("ICE 연결 상태 변경:", this.peerConnection.iceConnectionState);
          if (this.peerConnection.iceConnectionState === 'disconnected' ||
            this.peerConnection.iceConnectionState === 'closed' ||
            this.peerConnection.iceConnectionState === 'failed') {
            this.peerClosed = true;
            console.log("상대방이 연결을 종료했습니다");
          }
        };

        // 4.ICE Candidate가 생성될 때마다 시그널링 서버로 보낸다(ice후보는 한번에 생성되는게 아니라 그때그때마다 찾아짐 찾아질때마다 상대방(여기선 this.loginId)에게 보내줘야함)
        // this.peerConnection.onicecandidate는 ICE후보가 생성될때마다 호출되는 이벤트 핸들러
        this.peerConnection.onicecandidate = (event) => {

          if (event.candidate && this.signalingServer.readyState === WebSocket.OPEN) {

            this.signalingServer.send(JSON.stringify({
              type: 'candidate',
              candidate: event.candidate,
              to: this.loginId,
              from: this.myId
            }))

          } else {
            console.log("시그널링 서버 연결 대기중")
          }


        }
        // 5.SDP Offer생성후 전송(상대방은 이걸 받고 Answer을 만들어야 연결이 성립된다)
        const offer = await this.peerConnection.createOffer()
        await this.peerConnection.setLocalDescription(offer)

        this.signalingServer.send(JSON.stringify(
          {
            type: 'offer',
            offer: offer,
            from: this.myId,
            to: this.loginId
            //여기서 to~는 반드시 있어야함 백엔드에서 이 메시지의 'to'키값을 찾아서 상대방 로그인아이디를 찾도록 로직처리했으므로
          }))

      } catch (error) {
        console.error('startCall 오류:', error)
      }
    },
    //////// startCall()끝
    async connectSignalingServer() {
      // 1.시그널링 서버 연결(백엔드에서 맞춰놓은 쿼리파라미터 형식으로)
      this.signalingServer = new WebSocket(`ws://localhost:5000/`) //WebSocketConfig에 설정한 url과 경로 맞추어야함

      //2. 연결이 열리면 실행
      this.signalingServer.onopen = () => {
        console.log('시그널링 서버 연결 성공')
      }

      //3. 상대방이 보내온 메시지를 처리하는 부분
      this.signalingServer.onmessage = async (message) => {
        const data = JSON.parse(message.data)

        if (data.type === 'answer') {
          this.handleAnswer(data)
        } else if (data.type === 'candidate') {
          // 상대방이 알려준 후보군을 내 peerConnection에 등록
          const candidate = new RTCIceCandidate(data.candidate)
          await this.peerConnection.addIceCandidate(candidate)
        } else if (data.type === 'offer') {
          try {
            //   // this.startCall()은 내가 offer를 생성해서 보내는 거고 여기서는 상대방의 offer를 받아서 answer를 보내는 로직이 들어가야하는 것
            //  1.내 RTCpeerConnection객체 생성
            const configuration = {
              iceServers: [{ urls: 'stun:stun.1.google.com.19302' }]
            };
            this.peerConnection = new RTCPeerConnection(configuration);
            // 2. 내 localStream을 트랙에 추가
            this.localStream.getTracks().forEach(track => {
              this.peerConnection.addTrack(track, this.localStream);
            });
            // 3.상대방의 트랙 수신 처리
            this.peerConnection.ontrack = (event) => {
              console.log('상대방 스트림 수신!');
              this.$refs.remoteVideo.srcObject = event.streams[0];
              this.isWaiting = false; // 상대방 스트림 수신 시 대기 상태 해제
            };

            // 연결 상태 변경 감지
            this.peerConnection.oniceconnectionstatechange = () => {
              console.log("ICE 연결 상태 변경:", this.peerConnection.iceConnectionState);
              if (this.peerConnection.iceConnectionState === 'disconnected' ||
                this.peerConnection.iceConnectionState === 'closed' ||
                this.peerConnection.iceConnectionState === 'failed') {
                this.peerClosed = true;
                console.log("상대방이 연결을 종료했습니다");
              }
            };

            // 4. ICE 후보 발견시 전송
            this.peerConnection.onicecandidate = (event) => {
              if (event.candidate) {
                this.signalingServer.send(JSON.stringify({
                  type: 'candidate',
                  candidate: event.candidate,
                  to: this.loginId,
                  from: this.myId
                }));
              }
            };
            // 5.상대 offer 등록
            await this.peerConnection.setRemoteDescription(new RTCSessionDescription(data.offer));

            // 6.answer생성 및 전송
            const answer = await this.peerConnection.createAnswer();
            await this.peerConnection.setLocalDescription(answer);
            this.signalingServer.send(JSON.stringify({
              type: 'answer',
              answer: answer,
              to: this.loginId,
              from: this.myId
            }));

            console.log('answer전송완료');
          } catch (error) {
            console.log('offer처리 중 오류 발생', error)
          }
        } else if (data.type === 'leave') {
          // 상대방이 통화를 종료했음을 알리는 메시지
          this.peerClosed = true;
          console.log("상대방이 통화를 종료했습니다");
        }
      }
      //4. 연결 종료 또는 오류 핸들링도 추가 가능
      this.signalingServer.onerror = (error) => {
        console.error('시그널링 서버 오류:', error)
      }
    },
    //////
    async handleAnswer(data) {
      try {
        console.log('받은 answer :', data.answer)
        // 상대방이 보낸 answer데이터를 RTCSessionDescription 객체로 변환
        const remoteDesc = new RTCSessionDescription(data.answer)
        // 이 answer를 peerConnection의 원격 설명으로 설정해서 연결 정보를 등록
        await this.peerConnection.setRemoteDescription(remoteDesc)
        console.log('상대방의 answer를 성공적으로 등록')
      } catch (error) {
        console.error('answer등록 실패')
      }
    },
    async getNameInfo() {
      const OpponentId = { opponentId: this.loginId }
      try {
        const response = await axios.post(`${process.env.VUE_APP_API_BASE_URL}/user-service/silverpotion/user/whatisyourname`, OpponentId);
        console.log("이름정보", response)
        this.userName = response.data.result[0]
        this.userNickname = response.data.result[1]
      } catch (error) {
        console.error('이름 정보 조회 실패', error)
      }
    },
    //마이크 켜고 끄는 함수
    toggleMicrophone() {
      this.localStream.getAudioTracks().forEach(track => track.enabled = !track.enabled)
    },
    //카메라 켜고 끄는 함수
    toggleCamera() {
      this.localStream.getVideoTracks().forEach(track => track.enabled = !track.enabled)
    },
    //통화 종료 함수
    endCall() {
      if (confirm('통화를 종료하시겠습니까?')) {
        // 상대방에게 통화 종료 메시지 전송
        if (this.signalingServer && this.signalingServer.readyState === WebSocket.OPEN) {
          this.signalingServer.send(JSON.stringify({
            type: 'leave',
            from: this.myId,
            to: this.loginId
          }));
        }

        this.peerConnection?.close();
        this.signalingServer?.close();
        //바로 전화면으로 돌아가는 것
        this.$router.go(-1);
      }
    },
    // 상대방이 통화를 종료했을 때 나가기 버튼
    closeAndReturn() {
      this.peerConnection?.close();
      this.signalingServer?.close();
      this.$router.go(-1);
    }
  }
}
</script>

<style scoped>
/* 공통 스타일 */
.chat-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  overflow: hidden;
  font-family: 'Noto Sans KR', sans-serif;
}

video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

/* 버전 3: 미니멀리스트 디자인 */
.video-chat-minimal {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

.videos-container {
  position: relative;
  flex: 1;
}

.remote-video-container {
  width: 100%;
  height: 100%;
}

.local-video-container {
  position: absolute;
  width: 180px;
  height: 135px;
  bottom: 20px;
  right: 20px;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.floating-name {
  position: absolute;
  bottom: 15px;
  left: 15px;
  background: white;
  color: #333;
  padding: 5px 15px;
  border-radius: 30px;
  font-size: 15px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.floating-name.local {
  bottom: 5px;
  left: 5px;
  font-size: 12px;
  padding: 2px 8px;
}

.minimal-controls {
  display: flex;
  justify-content: center;
  padding: 15px 0;
  gap: 30px;
  background: white;
  border-top: 1px solid #eee;
}

.icon-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #555;
  cursor: pointer;
  padding: 10px;
  border-radius: 50%;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: #f5f5f5;
}

.icon-btn.end {
  color: #e74c3c;
}

.icon-btn.end:hover {
  background: #fee;
}


.waiting-message {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  padding: 15px 30px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 500;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  z-index: 2;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(0, 0, 0, 0.05);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  }

  50% {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
  }

  100% {
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  }
}

.call-ended-message {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  backdrop-filter: blur(5px);
}

.message-content {
  background: white;
  padding: 30px;
  border-radius: 15px;
  text-align: center;
  box-shadow: 0 5px 30px rgba(0, 0, 0, 0.2);
  max-width: 400px;
  width: 80%;
}

.message-icon {
  font-size: 36px;
  color: #e74c3c;
  margin-bottom: 15px;
}

.message-content p {
  margin: 15px 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 10px 20px;
  font-size: 16px;
  border-radius: 30px;
  cursor: pointer;
  margin-top: 10px;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
</style>