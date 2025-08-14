<template>
  <div class="stomp-test-container">
    <h1>STOMP WebSocket 연결 테스트</h1>
    
    <!-- 연결 상태 표시 -->
    <div class="connection-status">
      <h2>연결 상태</h2>
      <div class="status-indicator" :class="{ connected: isConnected, disconnected: !isConnected }">
        {{ isConnected ? '🟢 연결됨' : '🔴 연결 안됨' }}
      </div>
      <div class="connection-info">
        <p><strong>서버 URL:</strong> {{ brokerURL }}</p>
        <p><strong>연결 시간:</strong> {{ connectionTime || '연결 안됨' }}</p>
        <p><strong>재연결 시도:</strong> {{ reconnectCount }}회</p>
      </div>
    </div>

    <!-- 연결 제어 -->
    <div class="connection-controls">
      <h2>연결 제어</h2>
      <div class="button-group">
        <button @click="connectStomp" :disabled="isConnected" class="btn btn-primary">
          🔌 STOMP 연결
        </button>
        <button @click="disconnectStomp" :disabled="!isConnected" class="btn btn-danger">
          🔌 연결 해제
        </button>
        <button @click="testConnection" :disabled="!isConnected" class="btn btn-success">
          🧪 연결 테스트
        </button>
      </div>
      
      <!-- WebRTC Signaling 테스트 -->
      <div class="webrtc-test-section">
        <h3>WebRTC Signaling 테스트</h3>
        <div class="input-group">
          <label>방 ID:</label>
          <input v-model="webrtcRoomId" placeholder="1" class="form-input" style="width: 100px;">
        </div>
        <div class="button-group">
          <button @click="connectWebRTCSignaling" :disabled="isWebRTCConnected" class="btn btn-info">
            📡 WebRTC Signaling 연결
          </button>
          <button @click="disconnectWebRTCSignaling" :disabled="!isWebRTCConnected" class="btn btn-warning">
            📡 WebRTC 연결 해제
          </button>
          <button @click="testWebRTCConnection" :disabled="!isWebRTCConnected" class="btn btn-secondary">
            🧪 WebRTC 테스트
          </button>
        </div>
      </div>
    </div>

    <!-- 메시지 전송 테스트 -->
    <div class="message-test" v-if="isConnected">
      <h2>메시지 전송 테스트</h2>
      <div class="test-messages">
        <button @click="sendTestMessage('/games/answer')" class="btn btn-info">
          📤 정답 제출 테스트
        </button>
        <button @click="sendTestMessage('/games/hint')" class="btn btn-warning">
          💡 힌트 요청 테스트
        </button>
        <button @click="sendCustomMessage" class="btn btn-secondary">
          ✏️ 커스텀 메시지
        </button>
      </div>
      
      <!-- 커스텀 메시지 입력 -->
      <div class="custom-message" v-if="showCustomMessage">
        <h3>커스텀 메시지 전송</h3>
        <div class="input-group">
          <label>목적지:</label>
          <input v-model="customDestination" placeholder="/pub/test" class="form-input">
        </div>
        <div class="input-group">
          <label>메시지:</label>
          <textarea v-model="customMessage" placeholder='{"type": "TEST", "data": "test message"}' class="form-textarea"></textarea>
        </div>
        <button @click="sendCustomMessageToServer" class="btn btn-primary">전송</button>
      </div>
    </div>

    <!-- 구독 관리 -->
    <div class="subscription-management" v-if="isConnected">
      <h2>구독 관리</h2>
      <div class="subscription-controls">
        <div class="input-group">
          <label>구독할 토픽:</label>
          <input v-model="subscribeTopic" placeholder="/sub/games/1" class="form-input">
        </div>
        <button @click="subscribeToTopic" class="btn btn-success">📡 구독</button>
      </div>
      
      <div class="active-subscriptions">
        <h3>활성 구독 목록</h3>
        <ul>
          <li v-for="topic in activeSubscriptions" :key="topic" class="subscription-item">
            {{ topic }}
            <button @click="unsubscribeFromTopic(topic)" class="btn btn-sm btn-danger">구독 해제</button>
          </li>
        </ul>
      </div>
    </div>

    <!-- 로그 출력 -->
    <div class="log-section">
      <h2>연결 로그</h2>
      <div class="log-controls">
        <button @click="clearLogs" class="btn btn-secondary">🗑️ 로그 지우기</button>
        <button @click="exportLogs" class="btn btn-info">📥 로그 다운로드</button>
      </div>
      <div class="log-container" ref="logContainer">
        <div v-for="(log, index) in logs" :key="index" class="log-entry" :class="log.type">
          <span class="log-time">{{ log.time }}</span>
          <span class="log-level">{{ log.level }}</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
      </div>
    </div>

    <!-- CSP 정보 -->
    <div class="csp-info">
      <h2>CSP (Content Security Policy) 정보</h2>
      <div class="csp-details">
        <p><strong>현재 도메인:</strong> {{ currentDomain }}</p>
        <p><strong>WebSocket URL:</strong> {{ brokerURL }}</p>
        <p><strong>CSP 우회 방법:</strong> nginx 프록시를 통한 WSS 연결</p>
        <div class="csp-status" :class="{ success: isConnected, error: !isConnected && hasAttemptedConnection }">
          {{ getCspStatus() }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export default {
  name: 'WebRTCTestView',
  data() {
    return {
      // STOMP 클라이언트
      stompClient: null,
      isConnected: false,
      connectionTime: null,
      reconnectCount: 0,
      hasAttemptedConnection: false,
      
      // 연결 설정
      brokerURL: 'wss://i13a708.p.ssafy.io/ws-game',
      useSockJS: false, // SockJS 사용 여부
      
      // 구독 관리
      subscribeTopic: '/sub/games/1',
      activeSubscriptions: [],
      
      // 커스텀 메시지
      showCustomMessage: false,
      customDestination: '/pub/test',
      customMessage: '{"type": "TEST", "data": "test message"}',
      
      // 로그 관리
      logs: [],
      maxLogs: 100,
      
      // 현재 도메인
      currentDomain: window.location.origin,
      
      // WebRTC Signaling
      webrtcWebSocket: null,
      isWebRTCConnected: false,
      webrtcRoomId: '1',
      webrtcConnectionTime: null
    }
  },
  
  mounted() {
    this.addLog('info', 'STOMP WebSocket 테스트 페이지 로드됨')
    this.addLog('info', `현재 도메인: ${this.currentDomain}`)
    this.addLog('info', `WebSocket URL: ${this.brokerURL}`)
  },
  
  beforeDestroy() {
    this.disconnectStomp()
    this.disconnectWebRTCSignaling()
  },
  
  methods: {
    // STOMP 연결
    connectStomp() {
      if (this.isConnected) {
        this.addLog('warn', '이미 연결되어 있습니다.')
        return
      }
      
      this.addLog('info', 'STOMP WebSocket 연결 시도 중...')
      this.hasAttemptedConnection = true
      
      try {
        // STOMP 클라이언트 생성
        this.stompClient = new Client({
          brokerURL: this.brokerURL,
          debug: (str) => {
            this.addLog('debug', `STOMP Debug: ${str}`)
          },
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
        })

        // 연결 성공 콜백
        this.stompClient.onConnect = (frame) => {
          this.isConnected = true
          this.connectionTime = new Date().toLocaleString()
          this.addLog('success', `STOMP WebSocket 연결 성공! Frame: ${frame.command}`)
          
          // 기본 구독
          this.subscribeToDefaultTopics()
        }

        // 연결 실패 콜백
        this.stompClient.onStompError = (frame) => {
          this.isConnected = false
          this.addLog('error', `STOMP 연결 오류: ${frame.headers.message || '알 수 없는 오류'}`)
        }

        // 연결 해제 콜백
        this.stompClient.onDisconnect = () => {
          this.isConnected = false
          this.connectionTime = null
          this.activeSubscriptions = []
          this.addLog('info', 'STOMP WebSocket 연결 해제됨')
        }

        // WebSocket 연결 활성화
        this.stompClient.activate()
        
      } catch (error) {
        this.addLog('error', `STOMP 클라이언트 생성 오류: ${error.message}`)
      }
    },

    // STOMP 연결 해제
    disconnectStomp() {
      if (this.stompClient) {
        try {
          this.stompClient.deactivate()
          this.stompClient = null
          this.isConnected = false
          this.connectionTime = null
          this.activeSubscriptions = []
          this.addLog('info', 'STOMP WebSocket 연결 해제 완료')
        } catch (error) {
          this.addLog('error', `연결 해제 오류: ${error.message}`)
        }
      }
    },

    // 기본 토픽 구독
    subscribeToDefaultTopics() {
      if (!this.stompClient || !this.stompClient.connected) {
        this.addLog('warn', 'STOMP 클라이언트가 연결되지 않았습니다.')
        return
      }

      try {
        // 기본 구독 경로
        this.stompClient.subscribe('/sub', (message) => {
          this.addLog('info', `기본 메시지 수신: ${message.body}`)
        })
        this.activeSubscriptions.push('/sub')

        this.addLog('success', '기본 토픽 구독 완료')
      } catch (error) {
        this.addLog('error', `기본 토픽 구독 오류: ${error.message}`)
      }
    },

    // 특정 토픽 구독
    subscribeToTopic() {
      if (!this.stompClient || !this.stompClient.connected) {
        this.addLog('warn', 'STOMP 클라이언트가 연결되지 않았습니다.')
        return
      }

      if (!this.subscribeTopic) {
        this.addLog('warn', '구독할 토픽을 입력해주세요.')
        return
      }

      try {
        this.stompClient.subscribe(this.subscribeTopic, (message) => {
          this.addLog('info', `토픽 메시지 수신 [${this.subscribeTopic}]: ${message.body}`)
        })
        
        this.activeSubscriptions.push(this.subscribeTopic)
        this.addLog('success', `토픽 구독 완료: ${this.subscribeTopic}`)
        this.subscribeTopic = ''
      } catch (error) {
        this.addLog('error', `토픽 구독 오류: ${error.message}`)
      }
    },

    // 토픽 구독 해제
    unsubscribeFromTopic(topic) {
      if (!this.stompClient || !this.stompClient.connected) {
        this.addLog('warn', 'STOMP 클라이언트가 연결되지 않았습니다.')
        return
      }

      try {
        // 구독 해제 (실제로는 subscription 객체를 저장해야 하지만, 여기서는 간단히 처리)
        const index = this.activeSubscriptions.indexOf(topic)
        if (index > -1) {
          this.activeSubscriptions.splice(index, 1)
          this.addLog('info', `토픽 구독 해제: ${topic}`)
        }
      } catch (error) {
        this.addLog('error', `토픽 구독 해제 오류: ${error.message}`)
      }
    },

    // 연결 테스트
    testConnection() {
      if (!this.isConnected) {
        this.addLog('warn', '연결되지 않은 상태입니다.')
        return
      }

      this.addLog('info', '연결 테스트 시작...')
      
      // 간단한 테스트 메시지 전송
      this.sendStompMessage('/test', {
        type: 'CONNECTION_TEST',
        data: {
          timestamp: new Date().toISOString(),
          client: 'Vue.js STOMP Test'
        }
      })
    },

    // 테스트 메시지 전송
    sendTestMessage(destination) {
      const testMessages = {
        '/games/answer': {
          type: 'ANSWER_SUBMIT',
          data: {
            roomId: 1,
            userId: 'test-user',
            answer: '테스트 정답',
            timestamp: new Date().toISOString()
          }
        },
        '/games/hint': {
          type: 'HINT_REQUEST',
          data: {
            roomId: 1,
            userId: 'test-user',
            hintType: 'FIRST_LETTER',
            timestamp: new Date().toISOString()
          }
        }
      }

      const message = testMessages[destination]
      if (message) {
        this.sendStompMessage(destination, message)
      }
    },

    // 커스텀 메시지 토글
    sendCustomMessage() {
      this.showCustomMessage = !this.showCustomMessage
    },

    // 커스텀 메시지 전송
    sendCustomMessageToServer() {
      if (!this.customDestination || !this.customMessage) {
        this.addLog('warn', '목적지와 메시지를 모두 입력해주세요.')
        return
      }

      try {
        const message = JSON.parse(this.customMessage)
        this.sendStompMessage(this.customDestination, message)
      } catch (error) {
        this.addLog('error', `JSON 파싱 오류: ${error.message}`)
      }
    },

    // STOMP 메시지 전송
    sendStompMessage(destination, message) {
      if (!this.stompClient || !this.stompClient.connected) {
        this.addLog('warn', 'STOMP 클라이언트가 연결되지 않았습니다.')
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
        
        this.addLog('success', `메시지 전송 완료: ${pubDestination}`)
        this.addLog('info', `전송 내용: ${JSON.stringify(message, null, 2)}`)
      } catch (error) {
        this.addLog('error', `메시지 전송 오류: ${error.message}`)
      }
    },

    // 로그 추가
    addLog(level, message) {
      const log = {
        time: new Date().toLocaleTimeString(),
        level: level.toUpperCase(),
        message: message,
        type: level
      }
      
      this.logs.unshift(log)
      
      // 최대 로그 수 제한
      if (this.logs.length > this.maxLogs) {
        this.logs = this.logs.slice(0, this.maxLogs)
      }
      
      // 자동 스크롤
      this.$nextTick(() => {
        if (this.$refs.logContainer) {
          this.$refs.logContainer.scrollTop = 0
        }
      })
    },

    // 로그 지우기
    clearLogs() {
      this.logs = []
      this.addLog('info', '로그가 지워졌습니다.')
    },

    // 로그 다운로드
    exportLogs() {
      const logText = this.logs.map(log => 
        `[${log.time}] ${log.level}: ${log.message}`
      ).join('\n')
      
      const blob = new Blob([logText], { type: 'text/plain' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `stomp-test-logs-${new Date().toISOString().slice(0, 19)}.txt`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      
      this.addLog('info', '로그가 다운로드되었습니다.')
    },

         // CSP 상태 확인
     getCspStatus() {
       if (!this.hasAttemptedConnection) {
         return '연결 시도 전'
       }
       
       if (this.isConnected) {
         return '✅ CSP 우회 성공 - WebSocket 연결됨'
       } else {
         return '❌ CSP 우회 실패 - 연결되지 않음'
       }
     },

     // WebRTC Signaling 연결
     connectWebRTCSignaling() {
       if (this.isWebRTCConnected) {
         this.addLog('warn', 'WebRTC Signaling이 이미 연결되어 있습니다.')
         return
       }
       
       this.addLog('info', 'WebRTC Signaling 연결 시도 중...')
       
       try {
         const signalingURL = `wss://i13a708.p.ssafy.io/signal/${this.webrtcRoomId}`
         this.webrtcWebSocket = new WebSocket(signalingURL)
         
         this.webrtcWebSocket.onopen = () => {
           this.isWebRTCConnected = true
           this.webrtcConnectionTime = new Date().toLocaleString()
           this.addLog('success', `WebRTC Signaling 연결 성공! Room: ${this.webrtcRoomId}`)
         }
         
         this.webrtcWebSocket.onmessage = (event) => {
           try {
             const data = JSON.parse(event.data)
             this.addLog('info', `WebRTC 메시지 수신: ${JSON.stringify(data)}`)
           } catch (error) {
             this.addLog('info', `WebRTC 메시지 수신: ${event.data}`)
           }
         }
         
         this.webrtcWebSocket.onerror = (error) => {
           this.addLog('error', `WebRTC Signaling 연결 오류: ${error}`)
         }
         
         this.webrtcWebSocket.onclose = (event) => {
           this.isWebRTCConnected = false
           this.webrtcConnectionTime = null
           this.addLog('info', `WebRTC Signaling 연결 종료: ${event.code} - ${event.reason}`)
         }
         
       } catch (error) {
         this.addLog('error', `WebRTC Signaling 연결 생성 오류: ${error.message}`)
       }
     },

     // WebRTC Signaling 연결 해제
     disconnectWebRTCSignaling() {
       if (this.webrtcWebSocket) {
         try {
           this.webrtcWebSocket.close()
           this.webrtcWebSocket = null
           this.isWebRTCConnected = false
           this.webrtcConnectionTime = null
           this.addLog('info', 'WebRTC Signaling 연결 해제 완료')
         } catch (error) {
           this.addLog('error', `WebRTC 연결 해제 오류: ${error.message}`)
         }
       }
     },

     // WebRTC 연결 테스트
     testWebRTCConnection() {
       if (!this.isWebRTCConnected) {
         this.addLog('warn', 'WebRTC Signaling이 연결되지 않았습니다.')
         return
       }

       this.addLog('info', 'WebRTC 연결 테스트 시작...')
       
       // 간단한 테스트 메시지 전송
       const testMessage = {
         type: 'test',
         roomId: this.webrtcRoomId,
         timestamp: new Date().toISOString(),
         client: 'Vue.js WebRTC Test'
       }
       
       try {
         this.webrtcWebSocket.send(JSON.stringify(testMessage))
         this.addLog('success', 'WebRTC 테스트 메시지 전송 완료')
       } catch (error) {
         this.addLog('error', `WebRTC 테스트 메시지 전송 오류: ${error.message}`)
       }
     }
  }
}
</script>

<style scoped>
.stomp-test-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 30px;
  border-bottom: 3px solid #3498db;
  padding-bottom: 10px;
}

h2 {
  color: #34495e;
  margin-top: 30px;
  margin-bottom: 15px;
  border-left: 4px solid #3498db;
  padding-left: 15px;
}

h3 {
  color: #7f8c8d;
  margin-top: 20px;
  margin-bottom: 10px;
}

/* 연결 상태 */
.connection-status {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e9ecef;
}

.status-indicator {
  font-size: 18px;
  font-weight: bold;
  padding: 10px;
  border-radius: 5px;
  text-align: center;
  margin-bottom: 15px;
}

.status-indicator.connected {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-indicator.disconnected {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.connection-info p {
  margin: 5px 0;
  font-family: 'Courier New', monospace;
  background: #fff;
  padding: 5px 10px;
  border-radius: 3px;
  border: 1px solid #dee2e6;
}

/* 버튼 스타일 */
.button-group, .test-messages {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0056b3;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #c82333;
}

.btn-success {
  background: #28a745;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background: #218838;
}

.btn-info {
  background: #17a2b8;
  color: white;
}

.btn-info:hover:not(:disabled) {
  background: #138496;
}

.btn-warning {
  background: #ffc107;
  color: #212529;
}

.btn-warning:hover:not(:disabled) {
  background: #e0a800;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #545b62;
}

.btn-sm {
  padding: 5px 10px;
  font-size: 12px;
}

/* 입력 폼 */
.input-group {
  margin-bottom: 15px;
}

.input-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #495057;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
  font-family: 'Courier New', monospace;
}

.form-textarea {
  height: 100px;
  resize: vertical;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #80bdff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

/* 커스텀 메시지 */
.custom-message {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-top: 15px;
  border: 1px solid #e9ecef;
}

/* 구독 관리 */
.subscription-controls {
  display: flex;
  gap: 10px;
  align-items: end;
  margin-bottom: 20px;
}

.subscription-controls .input-group {
  flex: 1;
  margin-bottom: 0;
}

.active-subscriptions ul {
  list-style: none;
  padding: 0;
}

.subscription-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #fff;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  margin-bottom: 5px;
  font-family: 'Courier New', monospace;
}

/* 로그 섹션 */
.log-section {
  margin-top: 30px;
}

.log-controls {
  margin-bottom: 15px;
}

.log-container {
  height: 400px;
  overflow-y: auto;
  background: #1e1e1e;
  color: #fff;
  padding: 15px;
  border-radius: 5px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
}

.log-entry {
  margin-bottom: 5px;
  padding: 2px 0;
}

.log-time {
  color: #888;
  margin-right: 10px;
}

.log-level {
  font-weight: bold;
  margin-right: 10px;
  min-width: 60px;
  display: inline-block;
}

.log-level.info {
  color: #17a2b8;
}

.log-level.success {
  color: #28a745;
}

.log-level.warn {
  color: #ffc107;
}

.log-level.error {
  color: #dc3545;
}

.log-level.debug {
  color: #6c757d;
}

/* CSP 정보 */
.csp-info {
  background: #e3f2fd;
  padding: 20px;
  border-radius: 8px;
  margin-top: 30px;
  border: 1px solid #bbdefb;
}

.csp-details p {
  margin: 5px 0;
  font-family: 'Courier New', monospace;
}

.csp-status {
  margin-top: 15px;
  padding: 10px;
  border-radius: 5px;
  font-weight: bold;
  text-align: center;
}

.csp-status.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.csp-status.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .stomp-test-container {
    padding: 10px;
  }
  
  .button-group, .test-messages {
    flex-direction: column;
  }
  
  .subscription-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .log-container {
    height: 300px;
  }
}
</style>
