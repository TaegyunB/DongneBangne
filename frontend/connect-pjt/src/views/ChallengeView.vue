<template>
  <div>
    <h1 class="header">다양한 도전과제를 수행해보세요</h1>
     
    <!-- 진행률 -->
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
     
    <!-- 도전과제 목록 -->
    <div class="challenge-container">
      <div
        v-for="(challenge, index) in challenges"
        :key="index"
        class="single-challenge"
        :class="`challenge-${index + 1}`"
        @click="openModal(challenge, index)" 
      >
        <!-- 이미지 영역 -->
        <div class="challenge-image">
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
              <!-- userRole이 admin일 때만 수정/삭제 버튼 표시 -->
              <div v-if="userRole === 'ADMIN' && index >= 2 && !challenge.isEmpty" class="action-buttons">
                <button class="edit-btn" @click.stop="editChallenge(index)">수정</button>
                <button class="delete-btn" @click.stop="showDeleteConfirm(index)">삭제</button>
              </div>
            </div>
            <p>{{ challenge.description }}</p>
          </div>
          <!-- ADMIN만 완료/미완료 버튼을 클릭할 수 있도록 수정 -->
          <button 
            class="challenge-complete-btn"
            :class="{ 'completed': isCompleted(index), 'uploaded': isUploaded(index) && !isCompleted(index) }"
            @click.stop="userRole === 'ADMIN' ? toggleChallengeStatus(index) : null"
            :disabled="userRole !== 'ADMIN'"
          >
            {{ getButtonText(index) }}
          </button>
        </div>
      </div>
    </div>

    <!-- 상세정보 모달 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <button class="modal-close-btn" @click="closeModal">×</button>
        <h2>{{ selectedChallenge.title }}</h2>
        <p class="modal-description">{{ selectedChallenge.description }}</p>
        <div class="modal-place">
          <span class="icon">📍</span>
          장소: {{ selectedChallenge.place || '장소 정보 없음' }}
        </div>
        
        <button 
          v-if="userRole === 'ADMIN' && !selectedChallenge.isEmpty && !isCompleted(selectedChallengeId - 1)" 
          class="modal-button" 
          @click="moveToFinish"
        >
          도전 인증하기
        </button>
        
        <div 
          v-if="!selectedChallenge.isEmpty && isCompleted(selectedChallengeId - 1)"
          class="completed-message"
        >
          완료된 도전입니다
        </div>

        <!-- 업로드된 상태 표시 -->
        <div 
          v-if="!selectedChallenge.isEmpty && isUploaded(selectedChallengeId - 1) && !isCompleted(selectedChallengeId - 1)"
          class="uploaded-message"
        >
          관리자 승인 대기 중입니다
        </div>
      </div>
    </div>

    <!-- 수정 모달 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content edit-modal">
        <button class="modal-close-btn" @click="closeEditModal">×</button>
        <h1>도전과제 수정</h1>
        
        <div class="edit-form">
          <div class="form-group">
            <label>제목</label>
            <input v-model="editForm.title" type="text" class="form-input" placeholder="도전과제 제목을 입력하세요" />
          </div>

          <div class="form-group">
            <label>장소</label>
            <input v-model="editForm.place" type="text" class="form-input" placeholder="장소를 입력하세요 (선택사항)" />
          </div>
          
          <div class="form-group">
            <label>설명</label>
            <textarea v-model="editForm.description" class="form-textarea" placeholder="도전과제 설명을 입력하세요" rows="4"></textarea>
          </div>
        
        </div>
        
        <div class="modal-buttons">
          <button class="btn-cancel" @click="closeEditModal">취소</button>
          <button class="btn-save" @click="saveEditChallenge" :disabled="!editForm.title.trim() || !editForm.description.trim()">저장</button>
        </div>
      </div>
    </div>

    <!-- 수정 확인 모달 -->
    <div v-if="showEditSuccessModal" class="modal-overlay" @click.self="closeEditSuccessModal">
      <div class="modal-content delete-modal">
        <h2>도전이 성공적으로 <br> 수정되었습니다</h2>
        <div class="modal-buttons">
          <button class="delete-success-btn" @click="confirmEdit">도전 페이지로</button>
        </div>
      </div>
    </div>

    <!-- 삭제 확인 모달 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="modal-content delete-modal">
        <h2>도전을 <br> 삭제하시겠어요?</h2>
        <div class="modal-buttons">
          <button class="delete-confirm-btn" @click="showFinalDeleteConfirm">도전 삭제</button>
        </div>
      </div>
    </div>

    <!-- 최종 삭제 확인 모달 -->
    <div v-if="showFinalDeleteModal" class="modal-overlay" @click.self="closeFinalDeleteModal">
      <div class="modal-content delete-modal">
        <h2>도전이 성공적으로 <br> 삭제되었습니다</h2>
        <div class="modal-buttons">
          <button class="delete-success-btn" @click="confirmDelete">도전 페이지로</button>
        </div>
      </div>
    </div>

    <!-- 상태 변경 성공 모달 -->
    <div v-if="showStatusModal" class="modal-overlay" @click.self="closeStatusModal">
      <div class="modal-content delete-modal">
        <h2>{{ statusModalMessage }}</h2>
        <div class="modal-buttons">
          <button class="delete-success-btn" @click="closeStatusModal">확인</button>
        </div>
      </div>
    </div>

    <!-- 생성 버튼 - userRole이 admin일 때만 표시 -->
    <div class="create-challenge" v-if="userRole === 'ADMIN' && shouldShowCreateButton">
      <button class="challenge-btn" @click="moveToCreate()">도전과제 생성하기</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, defineProps } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const props = defineProps({
  month: { type: Number, default: new Date().getMonth() + 1 },
  userRole: { type: String, default: 'ADMIN' } // MainPage에서 받는 userRole prop
})

// 반응형 데이터
const count = ref(0)
const percent = computed(() => Math.round((count.value / 4) * 100))
const progressMessages = ref([])
const currentMessage = ref('')
const monthlyChallenges = ref({})
const challenges = ref([])

// 모달 상태
const modals = ref({
  detail: { show: false, selectedChallenge: { title: '', description: '', place: '' }, selectedChallengeId: null },
  edit: { show: false, form: { title: '', description: '', place: '' }, editingIndex: null, showSuccess: false },
  delete: { show: false, showFinal: false, selectedChallenge: null, selectedIndex: null },
  status: { show: false, message: '' }
})

// 계산된 속성
const shouldShowCreateButton = computed(() => 
  JSON.parse(localStorage.getItem('customChallenges') || '[]').length < 2
)

// 모달 상태 단축 접근
const showModal = computed(() => modals.value.detail.show)
const selectedChallenge = computed(() => modals.value.detail.selectedChallenge)
const selectedChallengeId = computed(() => modals.value.detail.selectedChallengeId)
const showEditModal = computed(() => modals.value.edit.show)
const showEditSuccessModal = computed(() => modals.value.edit.showSuccess)
const editForm = computed(() => modals.value.edit.form)
const showDeleteModal = computed(() => modals.value.delete.show)
const showFinalDeleteModal = computed(() => modals.value.delete.showFinal)
const selectedDeleteChallenge = computed(() => modals.value.delete.selectedChallenge)
const showStatusModal = computed(() => modals.value.status.show)
const statusModalMessage = computed(() => modals.value.status.message)

// 핵심 기능 함수들
const isCompleted = (index) => {
  const data = localStorage.getItem(`challenge_${index + 1}`)
  return data ? JSON.parse(data).is_success === true : false
}

const isUploaded = (index) => {
  const data = localStorage.getItem(`challenge_${index + 1}`)
  return data ? JSON.parse(data).is_uploaded === true : false
}

const getButtonText = (index) => {
  if (isCompleted(index)) {
    return '완료'
  } else if (isUploaded(index)) {
    return '대기'
  } else {
    return '미완료'
  }
}

const getChallengeImage = (index) => {
  const data = localStorage.getItem(`challenge_${index + 1}`)
  return data && JSON.parse(data).image ? JSON.parse(data).image : '/src/assets/default_image.png'
}

const updateCompletedCount = () => {
  count.value = Array.from({length: 4}, (_, i) => isCompleted(i)).filter(Boolean).length
}

const updateMessage = () => {
  const messages = progressMessages.value.filter(item => item.percent === `${percent.value}%`)
  if (messages.length > 0) {
    currentMessage.value = messages[Math.floor(Math.random() * messages.length)].message
  }
}

// 도전과제 상태 토글 함수 (ADMIN 전용)
const toggleChallengeStatus = async (index) => {
  if (props.userRole !== 'ADMIN') return

  const challengeId = index + 1
  const currentlyCompleted = isCompleted(index)
  const currentlyUploaded = isUploaded(index)

  // 업로드되지 않은 상태에서는 토글 불가
  if (!currentlyUploaded && !currentlyCompleted) {
    alert('먼저 도전 인증을 업로드해주세요.')
    return
  }

  try {
    if (currentlyCompleted) {
      // 완료 → 미완료 (cancel API)
      const response = await axios.put(`/api/v1/admin/challenges/${challengeId}/cancel`)
      console.log('Cancel API 응답:', response.data)
      
      // 로컬 상태 업데이트
      const data = localStorage.getItem(`challenge_${challengeId}`)
      if (data) {
        const challengeData = JSON.parse(data)
        challengeData.is_success = false
        localStorage.setItem(`challenge_${challengeId}`, JSON.stringify(challengeData))
      }
      
      modals.value.status = { 
        show: true, 
        message: `도전이 취소되었습니다.<br>${response.data.subtractedPoint}점이 차감되었습니다.` 
      }
    } else {
      // 미완료(업로드됨) → 완료 (complete API)
      const response = await axios.post(`/api/v1/admin/challenges/${challengeId}/complete`)
      console.log('Complete API 응답:', response.data)
      
      // 로컬 상태 업데이트
      const data = localStorage.getItem(`challenge_${challengeId}`)
      if (data) {
        const challengeData = JSON.parse(data)
        challengeData.is_success = true
        challengeData.completedAt = new Date().toISOString()
        challengeData.earnedPoints = response.data.earnedPoint
        localStorage.setItem(`challenge_${challengeId}`, JSON.stringify(challengeData))
      }
      
      modals.value.status = { 
        show: true, 
        message: `도전이 완료되었습니다!<br>${response.data.earnedPoint}점이 부여되었습니다.` 
      }
    }
    
    updateCompletedCount()
    
  } catch (error) {
    console.error('상태 변경 오류:', error)
    alert('도전 상태 변경 중 오류가 발생했습니다.')
  }
}

const closeStatusModal = () => {
  modals.value.status.show = false
}

// 백 연결시 주석 해제: 백엔드에서 도전과제 데이터 가져오기
/*
const fetchChallengesFromBackend = async () => {
  try {
    const response = await axios.get('/api/challenges')
    const backendChallenages = response.data
    
    // 백엔드 데이터를 기존 구조에 맞게 변환
    challenges.value = backendChallenages.map(challenge => ({
      challengeId: challenge.challengeId,
      title: challenge.challeneTitle, // 백엔드 오타 그대로 맞춰서
      description: challenge.description,
      place: challenge.challengePlace,
      role: challenge.role,
      isEmpty: false
    }))
    
  } catch (error) {
    console.error('백엔드에서 도전과제를 불러오는 데 실패했습니다:', error)
    // 에러 시 기존 로직으로 폴백
    loadMonthlyChallenges()
  }
}
*/

const updateChallenges = () => {
  const monthChallenges = monthlyChallenges.value[props.month.toString()]
  const customChallenges = JSON.parse(localStorage.getItem('customChallenges') || '[]')

  if (monthChallenges?.length > 0) {
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

// 모달 함수들
const openModal = (challenge, index) => {
  if (challenge.isEmpty) return
  modals.value.detail = { show: true, selectedChallenge: challenge, selectedChallengeId: index + 1 }
}

const closeModal = () => {
  modals.value.detail.show = false
}

const editChallenge = (index) => {
  const challenge = challenges.value[index]
  modals.value.edit = {
    show: true,
    form: { title: challenge.title, description: challenge.description, place: challenge.place || '' },
    editingIndex: index,
    showSuccess: false
  }
}

const closeEditModal = () => {
  modals.value.edit = { show: false, form: { title: '', description: '', place: '' }, editingIndex: null, showSuccess: false }
}

const closeEditSuccessModal = () => {
  modals.value.edit.showSuccess = false
}

// 백 연결시 주석 해제: 백엔드로 수정 요청 보내기
const saveEditChallenge = async () => {
  const { form, editingIndex } = modals.value.edit
  if (!form.title.trim() || !form.description.trim()) {
    alert('제목과 설명을 모두 입력해주세요.')
    return
  }

  /* 백 연결시 주석 해제
  try {
    const challenge = challenges.value[editingIndex]
    const response = await axios.put(`/api/challenges/${challenge.challengeId}`, {
      title: form.title.trim(),
      description: form.description.trim(),
      place: form.place.trim()
    })
    
    // 성공 시 로컬 데이터 업데이트
    challenges.value[editingIndex] = {
      ...challenges.value[editingIndex],
      title: form.title.trim(),
      description: form.description.trim(),
      place: form.place.trim()
    }
    
  } catch (error) {
    console.error('도전과제 수정 실패:', error)
    alert('도전과제 수정에 실패했습니다.')
    return
  }
  */

  // 기존 로컬스토리지 로직 (백 연결 전까지 유지)
  const customIndex = editingIndex - 2
  const customChallenges = JSON.parse(localStorage.getItem('customChallenges') || '[]')
  
  customChallenges[customIndex] = {
    ...customChallenges[customIndex],
    title: form.title.trim(),
    description: form.description.trim(),
    place: form.place.trim()
  }
  
  localStorage.setItem('customChallenges', JSON.stringify(customChallenges))
  updateChallenges()
  
  // 수정 모달 닫고 성공 모달 열기
  modals.value.edit.show = false
  modals.value.edit.showSuccess = true
}

const confirmEdit = () => {
  closeEditSuccessModal()
  closeEditModal()
}

const showDeleteConfirm = (index) => {
  modals.value.delete = { show: true, showFinal: false, selectedChallenge: challenges.value[index], selectedIndex: index }
}

const closeDeleteModal = () => {
  modals.value.delete = { show: false, showFinal: false, selectedChallenge: null, selectedIndex: null }
}

const showFinalDeleteConfirm = () => {
  modals.value.delete.show = false
  modals.value.delete.showFinal = true
}

const closeFinalDeleteModal = () => {
  modals.value.delete.showFinal = false
}

// 백 연결시 주석 해제: 백엔드로 삭제 요청 보내기
const confirmDelete = async () => {
  const selectedIndex = modals.value.delete.selectedIndex
  if (selectedIndex !== null) {
    /* 백 연결시 주석 해제
    try {
      const challenge = challenges.value[selectedIndex]
      await axios.delete(`/api/challenges/${challenge.challengeId}`)
      
      // 성공 시 로컬에서도 제거
      challenges.value.splice(selectedIndex, 1)
      
    } catch (error) {
      console.error('도전과제 삭제 실패:', error)
      alert('도전과제 삭제에 실패했습니다.')
      return
    }
    */

    // 기존 로컬스토리지 로직 (백 연결 전까지 유지)
    const customIndex = selectedIndex - 2
    const customChallenges = JSON.parse(localStorage.getItem('customChallenges') || '[]')
    customChallenges.splice(customIndex, 1)
    localStorage.setItem('customChallenges', JSON.stringify(customChallenges))
    localStorage.removeItem(`challenge_${selectedIndex + 1}`)
    updateChallenges()
    updateCompletedCount()
  }
  closeDeleteModal()
}

// 데이터 로딩 함수들
const loadMessages = async () => {
  try {
    const response = await fetch('/progress_sentence.json')
    progressMessages.value = await response.json()
    updateMessage()
  } catch (error) {
    currentMessage.value = 'JSON 파일을 불러올 수 없습니다.'
  }
}

const loadMonthlyChallenges = async () => {
  try {
    const response = await fetch('/public/monthly_challenges.json')
    monthlyChallenges.value = await response.json()
    updateChallenges()
  } catch (error) {
    console.error('도전 데이터를 불러오는 데 실패했습니다:', error)
    challenges.value = Array(4).fill({ title: '도전과제를 불러올 수 없습니다', description: '' })
  }
}

// 네비게이션
const moveToCreate = () => router.push({ name: 'challengeCreate' })
const moveToFinish = () => router.push(`/admin/challenges/${selectedChallengeId.value}/complete`)

// 라이프사이클 훅
onMounted(() => {
  loadMessages()
  // 백 연결시 주석 해제: 백엔드에서 데이터 가져오기
  // fetchChallengesFromBackend()
  loadMonthlyChallenges() // 백 연결시 이 줄은 제거하거나 폴백용으로만 사용
  updateCompletedCount()
})

// 감시자
watch(percent, updateMessage)
watch(() => props.month, () => {
  // 백 연결시 주석 해제
  // fetchChallengesFromBackend()
  updateChallenges() // 백 연결시 이 줄은 제거
})
watch(() => router.currentRoute.value, () => {
  updateCompletedCount()
  // 백 연결시 주석 해제
  // fetchChallengesFromBackend()
  updateChallenges() // 백 연결시 이 줄은 제거
}, { immediate: true })
</script>

<style scoped>
/* 레이아웃 */
.header { text-align: center; margin: 20px auto; }

.progress-container, .message-box {
  max-width: 800px; width: 90%; margin: 20px auto;
}

.progress-container {
  display: flex; align-items: center; gap: 15px;
}

.progress-bar { 
  flex: 1; height: 15px; border-radius: 10px; background: #E6E6E6; 
}

.inner-bar { 
  height: 100%; border-radius: 10px; background: #107C10; 
}

.message-box {
  color: #115EA3; font-weight: bold; text-align: center; 
  padding: 5px; background: #EBF3FC; border-radius: 30px; font-size: 20px; 
}

/* 도전과제 컨테이너 */
.challenge-container {
  display: flex; justify-content: space-between; align-items: stretch;
  max-width: 1200px; width: 90%; margin: 20px auto; gap: 20px;
}

.single-challenge {
  flex: 1; color: black; font-weight: bold; border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.4); cursor: pointer;
  position: relative; overflow: hidden; display: flex; flex-direction: column; height: 480px;
}

.challenge-image { width: 100%; aspect-ratio: 1; overflow: hidden; }
.challenge-img { width: 100%; height: 100%; object-fit: cover; object-position: center; }

.challenge-content { 
  position: relative; padding: 15px; display: flex; flex-direction: column; flex: 1;
}

.text-content { flex: 1; }
.text-content p {font-size: 18px !important;}
.title-with-buttons {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px;
}

.challenge-content h2 { margin: 0; font-size: 23px; flex: 1; }

.action-buttons { display: flex; gap: 5px; margin-left: 10px; }

.edit-btn, .delete-btn {
  padding: 4px 8px; border: none; border-radius: 4px; font-size: 16px; 
  font-weight: bold; cursor: pointer; color: white;
}

.edit-btn { background-color: #28a745; }
.edit-btn:hover { background-color: #218838; }
.delete-btn { background-color: #dc3545; }
.delete-btn:hover { background-color: #c82333; }

.challenge-content p { 
  margin: 5px 0 15px 0; font-size: 16px; font-weight: normal; line-height: 1.4; 
}

/* 도전과제 배경색 */
.challenge-1 .challenge-content { background: #FFBF8F; }
.challenge-1:hover .challenge-content { background: #FFD4B3; }
.challenge-2 .challenge-content { background: #97B9FF; }
.challenge-2:hover .challenge-content { background: #B3D1FF; }
.challenge-3 .challenge-content { background: #ABBAF9; }
.challenge-3:hover .challenge-content { background: #C4D0FB; }
.challenge-4 .challenge-content { background: #F1C399; }
.challenge-4:hover .challenge-content { background: #F5D6B8; }

.challenge-complete-btn {
  position: absolute; bottom: 20px; left: 50%; transform: translateX(-50%);
  font-weight: bold; color: white; width: 120px; height: 35px; border: none;
  cursor: pointer; font-size: 20px; border-radius: 15px; 
  box-shadow: 0 2px 4px rgba(0,0,0,0.1); background-color: #FF8120;
}

.challenge-complete-btn.completed { background-color: #3074FF; }
.challenge-complete-btn.uploaded { background-color: #FFA500; }
.challenge-complete-btn:disabled { 
  cursor: not-allowed; 
  opacity: 0.7;
}

/* 생성 버튼 */
.create-challenge { display: flex; justify-content: center; align-items: center; width: 100%; }

.challenge-btn {
  margin: 10px; background-color: #3074FF; font-weight: bold; color: white;
  width: 320px; height: 65px; border: none; cursor: pointer;
  font-size: 23px; border-radius: 15px; 
}

.challenge-btn:hover { background-color: #a2b7e3; }

/* 모달 공통 스타일 */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.4); z-index: 1000;
  display: flex; align-items: center; justify-content: center;
}

.modal-content {
  background: white; border-radius: 16px; padding: 30px 40px;
  width: 90%; max-width: 480px; box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  text-align: center; z-index: 1001; position: relative;
}

.modal-close-btn {
  position: absolute; top: 15px; right: 15px; width: 30px; height: 30px;
  border: none; background: none; font-size: 24px; font-weight: bold;
  color: #666; cursor: pointer; border-radius: 50%;
}

.modal-close-btn:hover { background-color: #f0f0f0; color: #333; }

.modal-content h2 { font-size: 28px; font-weight: bold; margin-bottom: 15px; }

.modal-description { font-size: 20px; line-height: 1.6; margin-bottom: 20px; }

.modal-place { font-size: 20px; color: #444; margin-bottom: 25px; }

.modal-button {
  background-color: #3074FF; color: white; padding: 12px 24px;
  font-size: 20px; border-radius: 10px; border: none; cursor: pointer;font-weight: bold;
}

.modal-button:hover { background-color: #6c9dff; }

.completed-message {
  background-color: #e8f5e8; color: #2d5a2d; padding: 12px 24px;
  border-radius: 10px; font-size: 20px; font-weight: 600;
}

.uploaded-message {
  background-color: #fff3cd; color: #856404; padding: 12px 24px;
  border-radius: 10px; font-size: 20px; font-weight: 600;
}

/* 수정 모달 */
.edit-modal { max-width: 500px; text-align: left; }

.edit-form { margin-bottom: 25px; }

.form-group { margin-bottom: 20px; margin-top: 20px;}

.form-group label { display: block; margin-bottom: 8px; font-weight: bold; color: #333; font-size: 20px;}

.form-input, .form-textarea {
  width: 100%; padding: 12px; border: 2px solid #e0e0e0; border-radius: 8px;
  font-size: 18px; transition: border-color 0.3s;
}

.form-input:focus, .form-textarea:focus { outline: none; border-color: #3074FF; }

.form-textarea { resize: vertical; min-height: 80px; font-family: inherit; }

/* 모달 버튼들 */
.modal-buttons { display: flex; gap: 15px; justify-content: center; margin-top: 25px; }

.btn-cancel, .btn-save, .delete-confirm-btn, .delete-success-btn {
  padding: 12px 24px; border: none; border-radius: 8px;
  font-size: 20px; font-weight: bold; cursor: pointer;
}

.btn-cancel { background-color: #f5f5f5; color: #666; }
.btn-cancel:hover { background-color: #e0e0e0; }

.btn-save, .delete-confirm-btn, .delete-success-btn { 
  background-color: #3074FF; color: white; 
}

.btn-save:hover:not(:disabled), .delete-confirm-btn:hover, .delete-success-btn:hover { 
  background-color: #6c9dff; 
}

.btn-save:disabled { background-color: #ccc; cursor: not-allowed; }

.delete-modal { max-width: 400px; }
.delete-confirm-btn { width: 150px; }
.delete-success-btn { width: 200px; }

/* 반응형 */
@media (max-width: 768px) {
  .challenge-container { flex-direction: column; align-items: center; }
  .single-challenge { width: 100%; max-width: 400px; }
  .title-with-buttons { flex-direction: column; align-items: flex-start; }
  .action-buttons { margin-left: 0; margin-top: 5px; }
  .modal-buttons { flex-direction: column; gap: 10px; }
  .btn-cancel, .btn-save { width: 100%; }
}
</style>