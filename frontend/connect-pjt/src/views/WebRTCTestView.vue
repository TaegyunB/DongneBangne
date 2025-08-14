<template>
  <div class="game-lobby">
    <!-- 나눔스퀘어라운드 폰트 -->
    <link href="https://hangeul.pstatic.net/hangeul_static/css/nanum-square-round.css" rel="stylesheet">
    <!-- 왼쪽 섹션: 방 목록 -->
    <div class="left-section">
      <div class="header">
        <div class="title-section">
          <div class="title">
            <h1>게임 참가하기</h1>
            <p>다른 사람의 방에 참가해보아요</p>
          </div>
          <div class="action-buttons">
            <button class="btn btn-danger" @click="exitGame">
              게임 종료
            </button>
            <button class="btn btn-primary" @click="createRoom">
              방 만들기
            </button>
          </div>
        </div>
      </div>

      <div class="room-list">
        <div 
          v-for="room in roomList" 
          :key="room.gameRoomId"
          class="room-card"
          :class="{ selected: selectedRoom?.gameRoomId === room.gameRoomId }"
          @click="selectRoom(room)"
        >
          <div class="room-title">{{ room.roomTitle }}</div>
          <div class="room-participants">
            <span class="icon">👤</span>
            {{ room.participantCount }}/2
          </div>
        </div>
      </div>

      <div class="status-text">Logging...</div>
    </div>

    <!-- 오른쪽 섹션: 선택된 방 정보 -->
    <div class="right-section" v-if="selectedRoom">
      <div class="user-info">
        <div class="avatar">👨‍🦳</div>
        <div class="user-details">
          <div class="username">{{ userInfo.nickname || '유저 이름' }}</div>
          <div class="points">포인트: {{ userInfo.personalPoint || 0 }}p</div>
        </div>
      </div>

      <div class="room-details-card">
        <div class="room-header">
          <div class="header-content">
            <span class="music-icon">🎵</span>
            <div class="round-info">
              <div class="round-label">최대 라운드</div>
              <div class="round-number">{{ selectedRoom.gameRound }} 라운드</div>
            </div>
          </div>
        </div>
        <div class="room-title">{{ selectedRoom.roomTitle }}</div>
      </div>

      <button class="btn btn-success join-btn" @click="joinRoom">
        참가하기
      </button>
    </div>

    <!-- 방이 선택되지 않았을 때 -->
    <div class="right-section empty" v-else>
      <div class="empty-state">
        <div class="empty-icon">🎮</div>
        <h3>방을 선택해주세요</h3>
        <p>왼쪽에서 참가하고 싶은 방을 클릭하세요</p>
      </div>
    </div>
  </div>

  <!-- 방 만들기 팝업 -->
  <div class="modal-overlay" v-if="showCreateRoomModal" @click="closeCreateRoomModal">
    <div class="modal-content" @click.stop>
      <button class="close-btn" @click="closeCreateRoomModal">×</button>
      
      <h2 class="modal-title">방 만들기</h2>
      
      <div class="input-group">
        <input 
          v-model="newRoom.roomTitle" 
          type="text" 
          placeholder="방 이름을 입력해주세요"
          maxlength="10"
          class="form-input"
        >
        <div class="char-count">{{ newRoom.roomTitle.length }}/10</div>
      </div>
      
      <div class="input-group">
        <select v-model="newRoom.gameRound" class="form-select">
          <option value="" disabled>라운드를 선택하세요</option>
          <option value="3">3 라운드</option>
          <option value="5">5 라운드</option>
          <option value="7">7 라운드</option>
        </select>
      </div>
      
      <button class="btn btn-primary create-room-btn" @click="submitCreateRoom">
        방 만들기
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'WebRTCTestView',
  data() {
    return {
      // 사용자 정보
      userInfo: {
        nickname: '유저 이름',
        personalPoint: 0
      },
      
      // 방 목록 (더미 데이터)
      roomList: [
        {
          gameRoomId: 1,
          roomTitle: '너만오면 고',
          gameRound: 20,
          gameStatus: 'WAITING',
          participantCount: 1
        },
        {
          gameRoomId: 2,
          roomTitle: '트로트 마스터전',
          gameRound: 15,
          gameStatus: 'WAITING',
          participantCount: 1
        },
        {
          gameRoomId: 3,
          roomTitle: '추억의 노래방',
          gameRound: 10,
          gameStatus: 'WAITING',
          participantCount: 2
        },
        {
          gameRoomId: 4,
          roomTitle: '신나는 뮤직게임',
          gameRound: 25,
          gameStatus: 'WAITING',
          participantCount: 1
        },
        {
          gameRoomId: 5,
          roomTitle: '클래식 명곡',
          gameRound: 12,
          gameStatus: 'WAITING',
          participantCount: 1
        }
      ],
      
      // 선택된 방
      selectedRoom: null,
      
      // 방 만들기 모달
      showCreateRoomModal: false,
      newRoom: {
        roomTitle: '',
        gameRound: ''
      }
    }
  },
  
  mounted() {
    this.loadUserInfo()
    this.loadRoomList()
  },
  
  methods: {
    // 사용자 정보 로드
    async loadUserInfo() {
      try {
        // API 호출 (실제 구현 시)
        // const response = await api.get('/api/v1/main/me')
        // this.userInfo = response.data
        
        // 더미 데이터 사용
        this.userInfo = {
          nickname: '게임러버',
          personalPoint: 1250
        }
      } catch (error) {
        console.error('사용자 정보 로드 실패:', error)
      }
    },
    
    // 방 목록 로드
    async loadRoomList() {
      try {
        // API 호출 (실제 구현 시)
        // const response = await api.get('/api/v1/game-rooms')
        // this.roomList = response.data
        
        console.log('방 목록 로드 완료')
      } catch (error) {
        console.error('방 목록 로드 실패:', error)
      }
    },
    
    // 방 선택
    selectRoom(room) {
      this.selectedRoom = room
      console.log('방 선택:', room)
    },
    
    // 방 참가
    joinRoom() {
      if (!this.selectedRoom) {
        alert('참가할 방을 선택해주세요.')
        return
      }
      
      console.log('방 참가:', this.selectedRoom)
      
      // 방 참가 API 호출 (실제 구현 시)
      // this.joinRoomAPI(this.selectedRoom.gameRoomId)
      
      // 방 내부로 이동 (실제 구현 시)
      // this.$router.push(`/game/${this.selectedRoom.gameRoomId}`)
      
      alert(`${this.selectedRoom.roomTitle} 방에 참가합니다!`)
    },
    
    // 방 만들기 모달 열기
    createRoom() {
      this.showCreateRoomModal = true
      this.newRoom = {
        roomTitle: '',
        gameRound: ''
      }
    },
    
    // 방 만들기 모달 닫기
    closeCreateRoomModal() {
      this.showCreateRoomModal = false
    },
    
    // 방 생성 제출
    submitCreateRoom() {
      if (!this.newRoom.roomTitle.trim()) {
        alert('방 이름을 입력해주세요.')
        return
      }
      
      if (!this.newRoom.gameRound) {
        alert('라운드를 선택해주세요.')
        return
      }
      
      console.log('방 생성:', this.newRoom)
      
      // 방 생성 API 호출 (실제 구현 시)
      // const response = await api.post('/api/v1/game-rooms', this.newRoom)
      
      // 성공 메시지
      alert(`${this.newRoom.roomTitle} 방이 생성되었습니다!`)
      
      // 모달 닫기
      this.closeCreateRoomModal()
      
      // 방 내부로 이동 (실제 구현 시)
      // this.$router.push(`/game/${response.data.gameRoomId}`)
    },
    
    // 게임 종료
    exitGame() {
      console.log('게임 종료')
      // 메인 페이지로 이동
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.game-lobby {
  display: flex;
  height: 100vh;
  width: 100vw;
  background: white;
  font-family: 'NanumSquareRound', sans-serif;
  overflow: hidden;
}

/* 왼쪽 섹션 */
.left-section {
  flex: 1;
  padding: 30px;
  display: flex;
  flex-direction: column;
  color: #333;
  overflow: hidden;
}

.header {
  margin-bottom: 30px;
}

.title-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title h1 {
  font-size: 2.5rem;
  font-weight: bold;
  margin: 0 0 10px 0;
  text-align: left;
}

.title p {
  font-size: 1.1rem;
  opacity: 0.8;
  margin: 0;
  text-align: left;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-danger:hover {
  background: #c82333;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.btn-success {
  background: #007bff;
  color: white;
}

.btn-success:hover {
  background: #0056b3;
}

.icon {
  font-size: 16px;
}

/* 방 목록 */
.room-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding-right: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin: 0 10px;
}

.room-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #333;
  border: 1px solid #e9ecef;
}

.room-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.room-card.selected {
  border: 3px solid #007bff;
  background: white;
}

.room-title {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 10px;
  color: #2c3e50;
}

.room-participants {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.9rem;
  color: #6c757d;
}

.status-text {
  margin-top: 20px;
  font-size: 0.9rem;
  opacity: 0.6;
}

/* 오른쪽 섹션 */
.right-section {
  flex: 1;
  padding: 30px;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  gap: 30px;
  overflow: hidden;
}

.right-section.empty {
  justify-content: center;
  align-items: center;
}

.empty-state {
  text-align: center;
  color: #333;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 1.5rem;
  margin-bottom: 10px;
}

.empty-state p {
  opacity: 0.8;
}

/* 사용자 정보 */
.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #333;
}

.avatar {
  font-size: 3rem;
  background: #007bff;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.user-details {
  flex: 1;
}

.username {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 5px;
}

.points {
  font-size: 0.9rem;
  opacity: 0.8;
}

/* 방 상세 정보 */
.room-details-card {
  background: white;
  border-radius: 12px;
  padding: 0;
  color: #333;
  border: 2px solid #e9ecef;
  overflow: hidden;
}

.room-header {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  padding: 20px;
  margin-bottom: 0;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.music-icon {
  font-size: 2rem;
}

.round-info {
  flex: 1;
}

.round-label {
  font-size: 0.9rem;
  color: #6c757d;
  margin-bottom: 5px;
}

.round-number {
  font-size: 1.3rem;
  font-weight: bold;
  color: #007bff;
}

.room-details-card .room-title {
  font-size: 1.4rem;
  font-weight: bold;
  color: #007bff;
  text-align: center;
  margin: 0;
  padding: 25px;
}

/* 참가하기 버튼 */
.join-btn {
  width: 100%;
  padding: 50px 15px;
  font-size: 1.1rem;
  border-radius: 0;
  background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
  color: white;
  text-align: center;
  border: none;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .game-lobby {
    flex-direction: column;
  }
  
  .right-section {
    flex: none;
    height: auto;
  }
  
  .header {
    flex-direction: column;
    gap: 20px;
  }
  
  .action-buttons {
    justify-content: center;
  }
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 30px;
  width: 400px;
  max-width: 90vw;
  position: relative;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.close-btn {
  position: absolute;
  top: 15px;
  right: 20px;
  background: #f8f9fa;
  border: none;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  font-size: 20px;
  color: #6c757d;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: #e9ecef;
  color: #495057;
}

.modal-title {
  text-align: center;
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 30px;
  margin-top: 10px;
}

.input-group {
  margin-bottom: 20px;
  position: relative;
}

.form-input, .form-select {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 14px;
  font-family: 'NanumSquareRound', sans-serif;
  background: white;
}

.form-input:focus, .form-select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.char-count {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #6c757d;
  background: white;
  padding: 2px 6px;
  border-radius: 4px;
}

.create-room-btn {
  width: 100%;
  padding: 15px;
  font-size: 1.1rem;
  border-radius: 8px;
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  border: none;
  margin-top: 10px;
}
</style>
