<template>
  <div>
    <h1 class="header">다양한 도전과제를 수행해보세요</h1>
  </div>
     
  <div class="progress-container">
    <h3>진행률</h3>
    <div class="progress-bar">
      <div class="inner-bar" :style="{width: percent + '%'}"></div>
    </div>
    <h3>{{percent}}%</h3>
  </div>
   
  <div class="message-box">
    <p>{{ currentMessage }}</p>
  </div>
   
  <div class="challenge-container">
    <!-- 모달 여는 함수에 인덱스 번호도 주기(url 변수) -->
    <div
        class="single-challenge"
        v-for="(challenge, index) in challenges"
        :key="index"
        @click="openModal(challenge, index)" 
        >
      <!-- 이미지 영역 -->
      <div class="challenge-image">
        <!-- 이미지가 없으면 default로, 있으면 그걸로 -->
        <img 
          :src="getChallengeImage(index)" 
          :alt="challenge.title"
          class="challenge-img"
        />
      </div>
      
      <!-- 텍스트 영역 -->
      <div class="challenge-content">
        <div class="text-content">
          <div class="title-with-buttons">
            <h2>{{ challenge.title }}</h2>
            <!-- 커스텀 도전과제(인덱스 2, 3)에만 수정/삭제 버튼 표시 -->
            <div v-if="index >= 2 && !challenge.isEmpty" class="action-buttons">
              <button 
                class="edit-btn" 
                @click.stop="editChallenge(index)"
                title="수정"
              >
                수정
              </button>
              <button 
                class="delete-btn" 
                @click.stop="showDeleteConfirm(index)"
                title="삭제"
              >
                삭제
              </button>
            </div>
          </div>
          <p>{{ challenge.description }}</p>
        </div>
        <button 
          class="challenge-complete-btn"
          :class="{ 'completed': isCompleted(index) }"
        >
        <!-- 도전과제가 수행 완료 되면, 완료로 설정 -->
          {{ isCompleted(index) ? '완료' : '미완료' }}
        </button>
      </div>
    </div>
  </div>

  <!-- 상세정보 모달  -->
  <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <!-- x 버튼을 누르면 모달 닫힘 -->
        <button class="modal-close-btn" @click="closeModal">×</button>
        <h2>{{ selectedChallenge.title }}</h2>
        <p class="modal-description">{{ selectedChallenge.description }}</p>
        <div class="modal-place">
        <span class="icon">📍</span>
        장소: {{ selectedChallenge.place || '장소 정보 없음' }}
        </div>
        <!-- 도전 인증 페이지로 넘어가기 (빈 도전과제는 버튼 숨김) -->
        <button 
          v-if="!selectedChallenge.isEmpty && !isCompleted(selectedChallengeId - 1)" 
          class="modal-button" 
          @click="moveToFinish"
        >
          도전 인증하기
        </button>
        
        <!-- 완료된 도전과제 표시 -->
        <div 
          v-if="!selectedChallenge.isEmpty && isCompleted(selectedChallengeId - 1)"
          class="completed-message"
        >
          완료된 도전입니다
        </div>
    </div>
  </div>

  <!-- 수정 모달 -->
  <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
    <div class="modal-content edit-modal">
      <button class="modal-close-btn" @click="closeEditModal">×</button>
      <h2>도전과제 수정</h2>
      
      <div class="edit-form">
        <div class="form-group">
          <label>제목</label>
          <input 
            v-model="editForm.title" 
            type="text" 
            class="form-input"
            placeholder="도전과제 제목을 입력하세요"
          />
        </div>
        
        <div class="form-group">
          <label>설명</label>
          <textarea 
            v-model="editForm.description" 
            class="form-textarea"
            placeholder="도전과제 설명을 입력하세요"
            rows="4"
          ></textarea>
        </div>
        
        <div class="form-group">
          <label>장소</label>
          <input 
            v-model="editForm.place" 
            type="text" 
            class="form-input"
            placeholder="장소를 입력하세요 (선택사항)"
          />
        </div>
      </div>
      
      <div class="edit-modal-buttons">
        <button class="btn-cancel" @click="closeEditModal">취소</button>
        <button 
          class="btn-save" 
          @click="saveEditChallenge"
          :disabled="!editForm.title.trim() || !editForm.description.trim()"
        >
          저장
        </button>
      </div>
    </div>
  </div>

  <!-- 삭제 확인 모달 (1단계) -->
  <div v-if="showDeleteModal" class="modal-overlay" @click.self="closeDeleteModal">
    <div class="modal-content delete-modal">
      <h2>"{{ selectedDeleteChallenge?.title }}"를 삭제하시겠어요?</h2>
      <div class="delete-modal-buttons">
        <button class="delete-confirm-btn" @click="showFinalDeleteConfirm">도전 삭제</button>
      </div>
    </div>
  </div>

  <!-- 최종 삭제 확인 모달 (2단계) -->
  <div v-if="showFinalDeleteModal" class="modal-overlay" @click.self="closeFinalDeleteModal">
    <div class="modal-content delete-modal">
      <h2>미션이 성공적으로 삭제되었습니다</h2>
      <div class="delete-modal-buttons">
        <button class="delete-success-btn" @click="confirmDelete">도전 확인하러 가기</button>
      </div>
    </div>
  </div>

  <div class="create-challenge" v-if="shouldShowCreateButton">
    <button class="challenge-btn" @click="moveToCreate()">도전과제 생성하기</button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, defineProps } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router=useRouter()

const moveToCreate = () => {
  router.push({ name: 'challengeCreate' }) 
}

const props = defineProps({
  month: {
    type: Number,
    default: new Date().getMonth() + 1 
  }
})

const count = ref(0)
const percent = computed(() => Math.round((count.value / 4) * 100))
const progressMessages = ref([])
const currentMessage = ref('')
const monthlyChallenges = ref({})
const challenges = ref([])

// 삭제 관련 상태
const showDeleteModal = ref(false)
const showFinalDeleteModal = ref(false)
const selectedDeleteChallenge = ref(null)
const selectedDeleteIndex = ref(null)

// 수정 관련 상태
const showEditModal = ref(false)
const editForm = ref({
  title: '',
  description: '',
  place: ''
})
const editingChallengeIndex = ref(null)

// 일단 프런트 테스트용으로 localstorage와 연결---------------------------------------------------
// 도전과제 생성 버튼 표시 여부(2개 이하로 생성되면 버튼 표시)
const shouldShowCreateButton = computed(() => {
  const customChallenges = JSON.parse(localStorage.getItem('customChallenges') || '[]')
  return customChallenges.length < 2
})

// 완료된 도전 확인
const isCompleted = (index) => {
  const challengeData = localStorage.getItem(`challenge_${index + 1}`)
  if (challengeData) {
    const parsedData = JSON.parse(challengeData)
    return parsedData.is_success === true
  }
  return false
}

// 도전 이미지 가져오기
const getChallengeImage = (index) => {
  const challengeData = localStorage.getItem(`challenge_${index + 1}`)
  
  if (challengeData) {
    const parsedData = JSON.parse(challengeData)
    if (parsedData.image) {
      return parsedData.image
    }
  }
  
  // 기본 이미지 반환
  return '/src/assets/default_image.png'
}

// 완료된 도전 수 계산
const updateCompletedCount = () => {
  let completedCount = 0
  for (let i = 1; i <= 4; i++) {
    const challengeData = localStorage.getItem(`challenge_${i}`)
    if (challengeData) {
      const parsedData = JSON.parse(challengeData)
      if (parsedData.is_success === true) {
        completedCount++
      }
    }
  }
  count.value = completedCount
}

// 진행률 메시지 JSON에서 데이터 불러오기
const loadMessages = async () => {
  try {
    const response = await fetch('/progress_sentence.json')
    progressMessages.value = await response.json()
    updateMessage()
  } catch (error) {
    currentMessage.value = 'JSON 파일을 불러올 수 없습니다.'
  }
}

// 메시지 랜덤으로 업데이트
const updateMessage = () => {
  const messages = progressMessages.value.filter(item => item.percent === `${percent.value}%`)
  if (messages.length > 0) {
    currentMessage.value = messages[Math.floor(Math.random() * messages.length)].message
  }
}

// 월별 도전과제를 백엔드에서 불러오는 방식 (axios)
const loadMonthlyChallenges = async () => {
  try {
    // 백엔드 연동 시 주석 해제
    /*
    const response = await axios.get('http://localhost:8080/challenges/')
    const challengeList = response.data

    // challengeTitle → title 로 변환
    const mappedList = mapChallengeFields(challengeList)

    // 월별 데이터가 아니면 전체 그대로 할당
    monthlyChallenges.value[props.month.toString()] = mappedList
    */

    // 현재는 JSON 파일에서 불러오기
    const response = await fetch('/public/monthly_challenges.json')
    monthlyChallenges.value = await response.json()

    updateChallenges()
  } catch (error) {
    console.error('도전 데이터를 불러오는 데 실패했습니다:', error)
    challenges.value = Array(4).fill({ title: '도전과제를 불러올 수 없습니다', description: '' })
  }
}

// 제공 도전과제를 2개 선택하고 커스텀 도전과제 추가
const updateChallenges = () => {
  const monthChallenges = monthlyChallenges.value[props.month.toString()]
  const customChallenges = JSON.parse(localStorage.getItem('customChallenges') || '[]')

  if (monthChallenges?.length > 0) {
    // 랜덤으로 2개 뽑기 (랜덤 시드 사용)
    const getSeededIndex = (seed) => Math.floor((Math.sin(seed) * 10000 - Math.floor(Math.sin(seed) * 10000)) * monthChallenges.length)
    
    const index1 = getSeededIndex(props.month * 31 + 17)
    const index2 = getSeededIndex(props.month * 37 + 23) === index1 ? (index1 + 1) % monthChallenges.length : getSeededIndex(props.month * 37 + 23)
    
    challenges.value = [
      monthChallenges[index1], 
      monthChallenges[index2],
      customChallenges[0] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true },
      customChallenges[1] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true }
    ]
  } else {
    challenges.value = [
      { title: '준비 중입니다.', description: '', isEmpty: true },
      { title: '준비 중입니다.', description: '', isEmpty: true },
      customChallenges[0] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true },
      customChallenges[1] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true }
    ]
  }
}

//상세정보 모달 관련
const showModal = ref(false)
const selectedChallenge = ref({ title: '', description: '', place: '' })
const selectedChallengeId = ref(null)

// 인덱스 값을 인자로 받음 
const openModal = (challenge, index) => {
  // 빈 도전과제인 경우 모달을 열지 않음
  if (challenge.isEmpty) {
    return
  }
  
  selectedChallenge.value = challenge
  selectedChallengeId.value = index + 1 // 1부터 시작하는 ID
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const moveToFinish = () => {
  router.push(`/admin/challenges/${selectedChallengeId.value}/complete`)
}

// 수정 기능
const editChallenge = (index) => {
  const challenge = challenges.value[index]
  editForm.value = {
    title: challenge.title,
    description: challenge.description,
    place: challenge.place || ''
  }
  editingChallengeIndex.value = index
  showEditModal.value = true
}

// 수정 모달 닫기
const closeEditModal = () => {
  showEditModal.value = false
  editForm.value = { title: '', description: '', place: '' }
  editingChallengeIndex.value = null
}

// 수정 저장
const saveEditChallenge = () => {
  if (!editForm.value.title.trim() || !editForm.value.description.trim()) {
    alert('제목과 설명을 모두 입력해주세요.')
    return
  }

  const customIndex = editingChallengeIndex.value - 2 // 0 또는 1
  const customChallenges = JSON.parse(localStorage.getItem('customChallenges') || '[]')
  
  // 수정된 데이터로 업데이트
  customChallenges[customIndex] = {
    ...customChallenges[customIndex],
    title: editForm.value.title.trim(),
    description: editForm.value.description.trim(),
    place: editForm.value.place.trim()
  }
  
  localStorage.setItem('customChallenges', JSON.stringify(customChallenges))
  
  // 챌린지 목록 업데이트
  updateChallenges()
  closeEditModal()
}

// 삭제 확인 모달 표시
const showDeleteConfirm = (index) => {
  selectedDeleteChallenge.value = challenges.value[index]
  selectedDeleteIndex.value = index
  showDeleteModal.value = true
}

// 삭제 확인 모달 닫기
const closeDeleteModal = () => {
  showDeleteModal.value = false
  selectedDeleteChallenge.value = null
  selectedDeleteIndex.value = null
}

// 최종 삭제 확인 모달 표시
const showFinalDeleteConfirm = () => {
  showDeleteModal.value = false
  showFinalDeleteModal.value = true
}

// 최종 삭제 확인 모달 닫기
const closeFinalDeleteModal = () => {
  showFinalDeleteModal.value = false
}

// 실제 삭제 실행
const confirmDelete = () => {
  if (selectedDeleteIndex.value !== null) {
    const customIndex = selectedDeleteIndex.value - 2 // 0 또는 1
    
    // localStorage에서 커스텀 도전과제 삭제
    const customChallenges = JSON.parse(localStorage.getItem('customChallenges') || '[]')
    customChallenges.splice(customIndex, 1)
    localStorage.setItem('customChallenges', JSON.stringify(customChallenges))
    
    // 해당 도전과제의 완료 데이터도 삭제
    localStorage.removeItem(`challenge_${selectedDeleteIndex.value + 1}`)
    
    // 챌린지 목록 업데이트
    updateChallenges()
    updateCompletedCount()
  }
  
  // 모달 닫기
  showFinalDeleteModal.value = false
  selectedDeleteChallenge.value = null
  selectedDeleteIndex.value = null
}

// 필드 이름 매핑용 유틸 함수
const mapChallengeFields = (rawList) => {
  return rawList.map(item => ({
    challengeId: item.challengeId,
    title: item.challengeTitle,
    place: item.place,
    description: item.description
  }))
}

// percent가 변경되면 자동으로 메시지 업데이트
watch(percent, updateMessage)

// month가 변경되면 챌린지 업데이트
watch(() => props.month, updateChallenges)

// 컴포넌트가 마운트될 때 JSON 파일들 자동 로드 및 완료 상태 업데이트
onMounted(() => {
  loadMessages()
  loadMonthlyChallenges()
  updateCompletedCount()
})

// 페이지가 활성화될 때마다 완료 상태 업데이트
watch(() => router.currentRoute.value, () => {
  updateCompletedCount()
  updateChallenges() // 커스텀 도전과제도 업데이트
}, { immediate: true })
</script>

<style scoped>
.header { 
  text-align: center;
  margin: 20px auto 20px;
}

.progress-container {
  display: flex; 
  align-items: center; 
  max-width: 800px; 
  width: 90%;
  margin: 20px auto; 
  gap: 15px;
}

.progress-bar { 
  flex: 1; 
  height: 15px; 
  border-radius: 10px; 
  background: #E6E6E6; 
}

.inner-bar { 
  height: 100%; 
  border-radius: 10px; 
  background: #107C10; 
}

.message-box {
  color: #115EA3; 
  font-weight: bold; 
  text-align: center; 
  margin: 10px auto;
  max-width: 800px; 
  width: 90%; 
  padding: 5px; 
  background: #EBF3FC; 
  border-radius: 30px;
  font-size: 20px; 
}

.challenge-container {
  display: flex; 
  justify-content: space-between; 
  align-items: stretch;
  max-width: 1200px; 
  width: 90%; 
  margin: 20px auto; 
  gap: 20px;
}

.single-challenge {
  flex: 1;
  color: black;
  font-weight: bold;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 480px; /* 높이 더 증가하여 정사각형 이미지와 콘텐츠 영역 확보 */
}

.challenge-image {
  width: 100%;
  aspect-ratio: 1; /* 정사각형 비율 */
  overflow: hidden;
  position: relative;
}

.challenge-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

.challenge-content { 
  position: relative;
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.text-content {
  flex: 1;
}

.title-with-buttons {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.challenge-content h2 { 
  margin: 0; 
  font-size: 20px; 
  flex: 1;
}

.action-buttons {
  display: flex;
  gap: 5px;
  margin-left: 10px;
}

.edit-btn, .delete-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  cursor: pointer;
  color: white;
}

.edit-btn {
  background-color: #28a745;
}

.edit-btn:hover {
  background-color: #218838;
}

.delete-btn {
  background-color: #dc3545;
}

.delete-btn:hover {
  background-color: #c82333;
}

.challenge-content p { 
  margin: 5px 0 15px 0; 
  font-size: 16px; 
  font-weight: normal; 
  line-height: 1.4; 
}

.single-challenge:nth-child(1) .challenge-content { background: #FFBF8F; }
.single-challenge:nth-child(1):hover .challenge-content { background: #FFD4B3; }
.single-challenge:nth-child(2) .challenge-content { background: #97B9FF; }
.single-challenge:nth-child(2):hover .challenge-content { background: #B3D1FF; }
.single-challenge:nth-child(3) .challenge-content { background: #ABBAF9; }
.single-challenge:nth-child(3):hover .challenge-content { background: #C4D0FB; }
.single-challenge:nth-child(4) .challenge-content { background: #F1C399; }
.single-challenge:nth-child(4):hover .challenge-content { background: #F5D6B8; }

.create-challenge { 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  width: 100%; 
}

.challenge-btn {
  margin: 10px; 
  background-color: #3074FF; 
  font-weight: bold; 
  color: white;
  width: 320px; 
  height: 65px; 
  border: none; 
  outline: none; 
  cursor: pointer;
  font-size: 16px; 
  border-radius: 15px; 
}

.challenge-btn:hover { 
  background-color: #a2b7e3; 
}

.challenge-complete-btn {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  font-weight: bold;
  color: white;
  width: 120px;
  height: 40px;
  border: none;
  outline: none;
  cursor: pointer;
  font-size: 16px;
  border-radius: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.challenge-complete-btn {
  background-color: #FF8120;
}

.challenge-complete-btn.completed {
  background-color: #3074FF;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.4);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 30px 40px;
  width: 90%;
  max-width: 480px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  text-align: center;
  z-index: 1001;
  position: relative;
}

.delete-modal {
  max-width: 400px;
}

.edit-modal {
  max-width: 500px;
  text-align: left;
}

.modal-close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 30px;
  height: 30px;
  border: none;
  background: none;
  font-size: 24px;
  font-weight: bold;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.modal-close-btn:hover {
  background-color: #f0f0f0;
  color: #333;
}

.modal-content h2 {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 15px;
}

.modal-description {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.modal-place {
  font-size: 15px;
  color: #444;
  margin-bottom: 25px;
}

.modal-button {
  background-color: #8C5EFF;
  color: white;
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
}

.modal-button:hover {
  background-color: #6e49d8;
}

.completed-message {
  background-color: #e8f5e8;
  color: #2d5a2d;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
}

.edit-form {
  margin-bottom: 25px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #8C5EFF;
}

.form-textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
  transition: border-color 0.3s;
}

.form-textarea:focus {
  outline: none;
  border-color: #8C5EFF;
}

.edit-modal-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.btn-cancel, .btn-save {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-cancel {
  background-color: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background-color: #e0e0e0;
}

.btn-save {
  background-color: #8C5EFF;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background-color: #6e49d8;
}

.btn-save:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.delete-modal-buttons {
  margin-top: 25px;
}

.delete-confirm-btn {
  background-color: #8C5EFF;
  color: white;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  width: 150px;
}

.delete-confirm-btn:hover {
  background-color: #6e49d8;
}

.delete-success-btn {
  background-color: #8C5EFF;
  color: white;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  width: 200px;
}

.delete-success-btn:hover {
  background-color: #6e49d8;
}

@media (max-width: 768px) {
  .challenge-container {
    flex-direction: column;
    align-items: center;
  }
  
  .single-challenge {
    width: 100%;
    max-width: 400px;
  }
  
  .title-with-buttons {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .action-buttons {
    margin-left: 0;
    margin-top: 5px;
  }
  
  .edit-modal-buttons {
    flex-direction: column;
    gap: 10px;
  }
  
  .btn-cancel, .btn-save {
    width: 100%;
  }
}
</style>