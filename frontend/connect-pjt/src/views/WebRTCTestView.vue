<template>
  <div class="websocket-test-container">
    <h1>WebSocket STOMP 메시지 테스트</h1>
    
    <!-- 연결 상태 -->
    <div class="connection-status">
      <h3>연결 상태</h3>
      <div class="status-indicator" :class="{ connected: isConnected }">
        {{ isConnected ? '연결됨' : '연결 안됨' }}
      </div>
      <button @click="connectWebSocket" :disabled="isConnected">연결</button>
      <button @click="disconnectWebSocket" :disabled="!isConnected">연결 해제</button>
    </div>

    <!-- 설정 -->
    <div class="settings">
      <h3>설정</h3>

      <div class="input-group">
        <label>Room ID:</label>
        <input v-model="roomId" placeholder="1" />
      </div>
      <div class="input-group">
        <label>User ID:</label>
        <input v-model="userId" placeholder="user123" />
      </div>
    </div>

    <!-- 메시지 전송 -->
    <div class="message-sender">
      <h3>메시지 전송</h3>
      
      <div class="message-section">
        <h4>클라이언트 → 서버 메시지 전송</h4>
        <div class="input-group">
          <label>메시지 타입:</label>
          <input v-model="messageType" placeholder="예: GAME_START, ANSWER_SUBMIT, HINT_REQUEST" />
        </div>
        <div class="input-group">
          <label>메시지 Body (JSON):</label>
          <textarea v-model="messageBody" placeholder='{"roomId": "room123", "userId": "user123", "content": "메시지 내용"}'></textarea>
        </div>
        <div class="input-group">
          <label>전송 경로:</label>
          <input v-model="sendDestination" placeholder="/pub/games 또는 /pub/games/answer 등" />
        </div>
        <button @click="sendMessage" class="send-button">메시지 전송</button>
      </div>
    </div>

    <!-- YouTube 동영상 -->
    <div class="youtube-section">
      <h3>YouTube 동영상</h3>
      <div class="youtube-controls">
        <input 
          v-model="videoId" 
          placeholder="YouTube 비디오 ID를 입력하세요"
          @keyup.enter="changeVideo"
          class="video-input"
        />
        <button @click="changeVideo" class="change-btn">비디오 변경</button>
      </div>
      
      <div id="player"></div>
      
      <div class="playback-controls">
        <button @click="playVideo" class="play-btn">재생</button>
        <button @click="pauseVideo" class="pause-btn">일시정지</button>
      </div>
    </div>

    <!-- 메시지 로그 -->
    <div class="message-log">
      <h3>메시지 로그</h3>
      <div class="log-controls">
        <button @click="clearLog">로그 지우기</button>
        <button @click="exportLog">로그 내보내기</button>
      </div>
      <div class="log-container">
        <div v-for="(log, index) in messageLog" :key="index" class="log-entry" :class="log.type">
          <div class="log-timestamp">{{ log.timestamp }}</div>
          <div class="log-direction">{{ log.direction }}</div>
          <div class="log-type">{{ log.messageType }}</div>
          <div class="log-content">{{ log.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Stomp } from '@stomp/stompjs'

export default {
  name: 'WebRTCTestView',
  data() {
    return {

      roomId: '1',
      userId: 'user123',
      messageType: '',
      messageBody: '',
      sendDestination: '/pub/games',
      videoId: 'pkc1XoilQIc',
      
      // WebSocket 관련
      stompClient: null,
      isConnected: false,
      
      // YouTube 관련
      youtubeIframe: null,
      player: null,
      isPlayerReady: false,
      
      // 메시지 로그
      messageLog: []
    }
  },
  methods: {
    // WebSocket 연결
    connectWebSocket() {
      try {
        // WebSocket 직접 연결 (URL 고정)
        const socket = new WebSocket('wss://i13a708.p.ssafy.io/ws-game')
        this.stompClient = Stomp.over(socket)
        
        // 쿠키를 헤더에 포함
        const headers = {
          'Cookie': document.cookie
        }
        
        this.stompClient.connect(headers, 
          (frame) => {
            console.log('Connected to WebSocket:', frame)
            this.isConnected = true
            this.addLog('연결', '연결됨', 'CONNECTION', 'WebSocket 연결 성공')
            
            // 구독 설정
            this.setupSubscriptions()
          },
          (error) => {
            console.error('WebSocket 연결 실패:', error)
            this.isConnected = false
            this.addLog('연결', '연결 실패', 'ERROR', error.toString())
          }
        )
      } catch (error) {
        console.error('WebSocket 연결 중 오류:', error)
        this.addLog('연결', '연결 오류', 'ERROR', error.toString())
      }
    },
    
    // WebSocket 연결 해제
    disconnectWebSocket() {
      if (this.stompClient) {
        this.stompClient.disconnect(() => {
          console.log('WebSocket 연결 해제됨')
          this.isConnected = false
          this.addLog('연결', '연결 해제', 'DISCONNECT', 'WebSocket 연결 해제됨')
        })
      }
    },
    
    // 구독 설정
    setupSubscriptions() {
      if (!this.stompClient || !this.isConnected) return
      
      // 게임 메시지 구독 (/sub/games/{roomId})
      this.stompClient.subscribe(`/sub/games/${this.roomId}`, (message) => {
        try {
          const messageData = JSON.parse(message.body)
          this.addLog('수신', messageData.type || 'UNKNOWN', 'RECEIVED', messageData)
        } catch (error) {
          this.addLog('수신', 'PARSE_ERROR', 'ERROR', `JSON 파싱 실패: ${message.body}`)
        }
      })
      
      // 힌트 메시지 구독 (/user/queue/hint)
      this.stompClient.subscribe(`/user/queue/hint`, (message) => {
        try {
          const messageData = JSON.parse(message.body)
          this.addLog('수신', messageData.type || 'HINT', 'RECEIVED', messageData)
        } catch (error) {
          this.addLog('수신', 'PARSE_ERROR', 'ERROR', `JSON 파싱 실패: ${message.body}`)
        }
      })
      
      // 모든 /sub 메시지 구독 (디버깅용)
      this.stompClient.subscribe('/sub/#', (message) => {
        try {
          const messageData = JSON.parse(message.body)
          this.addLog('수신', messageData.type || 'SUB_ALL', 'RECEIVED', messageData)
        } catch (error) {
          this.addLog('수신', 'PARSE_ERROR', 'ERROR', `JSON 파싱 실패: ${message.body}`)
        }
      })
    },
    
    // 메시지 전송
    sendMessage() {
      if (!this.stompClient || !this.isConnected) {
        alert('WebSocket이 연결되지 않았습니다.')
        return
      }
      
      if (!this.messageType.trim()) {
        alert('메시지 타입을 입력해주세요.')
        return
      }
      
      let message
      try {
        // JSON 파싱 시도
        if (this.messageBody.trim()) {
          message = JSON.parse(this.messageBody)
        } else {
          message = {}
        }
      } catch (error) {
        alert('메시지 Body가 올바른 JSON 형식이 아닙니다.')
        return
      }
      
      // 기본 필드 추가
      message.type = this.messageType
      message.roomId = this.roomId
      message.userId = this.userId
      message.timestamp = new Date().toISOString()
      
      // 쿠키를 헤더에 포함하여 전송
      const headers = {
        'Cookie': document.cookie
      }
      
      this.stompClient.send(this.sendDestination, headers, JSON.stringify(message))
      this.addLog('전송', this.messageType, 'SENT', message)
      
      // 입력 필드 초기화
      this.messageType = ''
      this.messageBody = ''
    },
    
    // YouTube 동영상 변경
    changeVideo() {
      if (this.videoId && this.player && this.isPlayerReady) {
        this.player.loadVideoById(this.videoId);
      } else {
        const iframe = this.youtubeIframe;
        if (iframe) {
          iframe.src = `https://youtube.com/embed/${this.videoId}?si=8IsRoXmN3OS1AwUH&enablejsapi=1`;
        }
      }
    },

    // YouTube 동영상 재생
    playVideo() {
      if (this.player && this.isPlayerReady) {
        this.player.playVideo();
      } else {
        const iframe = this.youtubeIframe;
        if (iframe) {
          try {
            iframe.contentWindow?.postMessage('{"event":"command","func":"playVideo","args":""}', '*');
          } catch (error) {
            console.log('재생 명령 전송 중 오류:', error);
          }
        }
      }
    },

    // YouTube 동영상 일시정지
    pauseVideo() {
      if (this.player && this.isPlayerReady) {
        this.player.pauseVideo();
      } else {
        const iframe = this.youtubeIframe;
        if (iframe) {
          try {
            iframe.contentWindow?.postMessage('{"event":"command","func":"pauseVideo","args":""}', '*');
          } catch (error) {
            console.log('일시정지 명령 전송 중 오류:', error);
          }
        }
      }
    },
    
    // 로그 추가
    addLog(direction, messageType, type, content) {
      this.messageLog.unshift({
        timestamp: new Date().toLocaleTimeString(),
        direction,
        messageType,
        type,
        content: typeof content === 'object' ? JSON.stringify(content, null, 2) : content
      })
      
      // 로그가 너무 많아지면 오래된 것부터 제거
      if (this.messageLog.length > 100) {
        this.messageLog = this.messageLog.slice(0, 100)
      }
    },
    
    // 로그 지우기
    clearLog() {
      this.messageLog = []
    },
    
    // 로그 내보내기
    exportLog() {
      const logText = this.messageLog.map(log => 
        `[${log.timestamp}] ${log.direction} - ${log.messageType}: ${log.content}`
      ).join('\n')
      
      const blob = new Blob([logText], { type: 'text/plain' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `websocket-log-${new Date().toISOString().slice(0, 19)}.txt`
      a.click()
      URL.revokeObjectURL(url)
    }
  },
  
  // 컴포넌트 마운트 시 YouTube API 로드
  mounted() {
    // YouTube iframe API 로드
    const tag = document.createElement('script');
    tag.src = 'https://www.youtube.com/iframe_api';
    const firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode?.insertBefore(tag, firstScriptTag);

    // YouTube API 준비되면 player 초기화
    window.onYouTubeIframeAPIReady = () => {
      this.player = new window.YT.Player('player', {
        height: '500',
        width: '500',
        videoId: this.videoId,
        playerVars: {
          'enablejsapi': 1,
          'autoplay': 0,
          'controls': 1,
        },
        events: {
          'onReady': (event) => {
            this.isPlayerReady = true;
            console.log('YouTube Player 준비 완료');
          },
          'onStateChange': (event) => {
            console.log('Player 상태 변경:', event.data);
          }
        }
      });
    };
  },
  
  // 컴포넌트 언마운트 시 연결 해제
  beforeUnmount() {
    this.disconnectWebSocket()
  }
}
</script>

<style scoped>
.websocket-test-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Arial', sans-serif;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

h3 {
  color: #555;
  border-bottom: 2px solid #007bff;
  padding-bottom: 10px;
  margin-bottom: 20px;
}

h4 {
  color: #666;
  margin-bottom: 15px;
}

.connection-status {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.status-indicator {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
  margin-right: 15px;
  background: #dc3545;
  color: white;
}

.status-indicator.connected {
  background: #28a745;
}

button {
  background: #007bff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  margin: 5px;
  font-size: 14px;
}

button:hover {
  background: #0056b3;
}

button:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.settings {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.input-group {
  margin-bottom: 15px;
}

.input-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
}

.input-group input,
.input-group textarea,
.input-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
}

.input-group textarea {
  height: 120px;
  resize: vertical;
  font-family: 'Courier New', monospace;
}

.youtube-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.youtube-controls {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.video-input {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  width: 250px;
}

.change-btn {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.change-btn:hover {
  background-color: #0056b3;
}

#player {
  margin-bottom: 20px;
  text-align: center;
}

.playback-controls {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.play-btn, .pause-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
}

.play-btn {
  background-color: #28a745;
  color: white;
}

.play-btn:hover {
  background-color: #218838;
}

.pause-btn {
  background-color: #dc3545;
  color: white;
}

.pause-btn:hover {
  background-color: #c82333;
}

.message-sender {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.message-section {
  margin-bottom: 25px;
  padding: 15px;
  background: white;
  border-radius: 5px;
  border-left: 4px solid #007bff;
}

.send-button {
  background: #28a745;
  font-weight: bold;
  padding: 12px 24px;
  font-size: 16px;
}

.send-button:hover {
  background: #218838;
}

.message-log {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.log-controls {
  margin-bottom: 15px;
}

.log-controls button {
  background: #6c757d;
  margin-right: 10px;
}

.log-controls button:hover {
  background: #545b62;
}

.log-container {
  max-height: 400px;
  overflow-y: auto;
  background: white;
  border: 1px solid #ddd;
  border-radius: 5px;
  padding: 10px;
}

.log-entry {
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 5px;
  border-left: 4px solid #ddd;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.log-entry.SENT {
  border-left-color: #28a745;
  background: #f8fff9;
}

.log-entry.RECEIVED {
  border-left-color: #007bff;
  background: #f8f9ff;
}

.log-entry.ERROR {
  border-left-color: #dc3545;
  background: #fff8f8;
}

.log-entry.CONNECTION {
  border-left-color: #ffc107;
  background: #fffdf8;
}

.log-entry.DISCONNECT {
  border-left-color: #6c757d;
  background: #f8f9fa;
}

.log-timestamp {
  color: #666;
  font-weight: bold;
}

.log-direction {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: bold;
  margin: 0 10px;
  background: #007bff;
  color: white;
}

.log-type {
  font-weight: bold;
  color: #333;
  margin: 5px 0;
}

.log-content {
  color: #555;
  white-space: pre-wrap;
  word-break: break-all;
}

@media (max-width: 768px) {
  .websocket-test-container {
    padding: 10px;
  }
  
  .send-button {
    width: 100%;
  }
}
</style>
