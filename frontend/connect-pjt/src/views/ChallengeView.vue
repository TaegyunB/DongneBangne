<template>
  <div>
    <h1 class="header">{{ currentMonthTitle }}월 도전과제를 수행해보세요</h1>
     
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
            :src="getChallengeImage(challenge)" 
            :alt="challenge.challengeTitle || challenge.title"
            class="challenge-img"
          />
        </div>
        
        <!-- 텍스트 영역 -->
        <div class="challenge-content">
          <div class="text-content">
            <div class="title-with-buttons">
              <h2>{{ challenge.challengeTitle || challenge.title }}</h2>
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
            :class="{ 'completed': isCompleted(challenge), 'uploaded': isUploaded(challenge) && !isCompleted(challenge) }"
            @click.stop="userRole === 'ADMIN' ? toggleChallengeStatus(challenge) : null"
            :disabled="userRole !== 'ADMIN'"
          >
            {{ getButtonText(challenge) }}
          </button>
        </div>
      </div>
    </div>

    <!-- 상세정보 모달 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <button class="modal-close-btn" @click="closeModal">×</button>
        <h2>{{ selectedChallenge.challengeTitle || selectedChallenge.title }}</h2>
        <p class="modal-description">{{ selectedChallenge.description }}</p>
        <div class="modal-place">
          <span class="icon">📍</span>
          장소: {{ selectedChallenge.challengePlace || selectedChallenge.place || '장소 정보 없음' }}
        </div>
        
        <button 
          v-if="userRole === 'ADMIN' && !selectedChallenge.isEmpty && !isCompleted(selectedChallenge)" 
          class="modal-button" 
          @click="moveToFinish"
        >
          도전 인증하기
        </button>
        
        <div 
          v-if="!selectedChallenge.isEmpty && isCompleted(selectedChallenge)"
          class="completed-message"
        >
          완료된 도전입니다
        </div>

        <!-- 업로드된 상태 표시 -->
        <div 
          v-if="!selectedChallenge.isEmpty && isUploaded(selectedChallenge) && !isCompleted(selectedChallenge)"
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
import { ref, computed, onMounted, watch } from 'vue' 
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user' 
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()

// props 대신 store 사용
const userRole = computed(() => userStore.userRole || 'MEMBER')
const currentMonth = ref(new Date().getMonth() + 1)
const currentMonthTitle = computed(() => currentMonth.value)

// 반응형 데이터
const count = ref(0)
const percent = computed(() => Math.round((count.value / 4) * 100))
const progressMessages = ref([])
const currentMessage = ref('')
const challenges = ref([])
const challengeDetails = ref({}) // 도전 상세 정보 캐시

// 모달 상태 
const modals = ref({
  detail: { show: false, selectedChallenge: { challengeTitle: '', description: '', challengePlace: '' }, selectedChallengeId: null },
  edit: { show: false, form: { title: '', description: '', place: '' }, editingIndex: null, showSuccess: false },
  delete: { show: false, showFinal: false, selectedChallenge: null, selectedIndex: null },
  status: { show: false, message: '' }
})

// 계산된 속성
const shouldShowCreateButton = computed(() => 
  JSON.parse(localStorage.getItem('adminChallenges') || '[]').length < 2
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
const isCompleted = (challenge) => {
  if (challenge.id) {
    // API에서 받은 도전과제 (우리가 제공하는 도전)
    return challenge.isSuccess === true
  } else if (challenge.challengeId) {
    // ADMIN이 생성한 도전과제
    const data = localStorage.getItem(`admin_challenge_${challenge.challengeId}`)
    return data ? JSON.parse(data).is_success === true : false
  } else {
    // 빈 칸
    return false
  }
}

const isUploaded = (challenge) => {
  if (challenge.id) {
    // API에서 받은 도전과제 (우리가 제공하는 도전)
    return challenge.imageDescription !== null && challenge.imageDescription !== undefined
  } else if (challenge.challengeId) {
    // ADMIN이 생성한 도전과제
    const data = localStorage.getItem(`admin_challenge_${challenge.challengeId}`)
    return data ? JSON.parse(data).is_uploaded === true : false
  } else {
    // 빈 칸
    return false
  }
}

const getButtonText = (challenge) => {
  if (isCompleted(challenge)) {
    return '완료'
  } else if (isUploaded(challenge)) {
    return '대기'
  } else {
    return '미완료'
  }
}

const getChallengeImage = (challenge) => {
  if (challenge.id && challenge.challengeImage) {
    // API에서 받은 도전과제 (우리가 제공하는 도전)
    return challenge.challengeImage
  } else if (challenge.challengeId) {
    // ADMIN이 생성한 도전과제
    const data = localStorage.getItem(`admin_challenge_${challenge.challengeId}`)
    return data && JSON.parse(data).image ? JSON.parse(data).image : '/src/assets/default_image.png'
  }
  return '/src/assets/default_image.png'
}

const updateCompletedCount = () => {
  count.value = challenges.value.filter(challenge => isCompleted(challenge)).length
}

const updateMessage = () => {
  const messages = progressMessages.value.filter(item => item.percent === `${percent.value}%`)
  if (messages.length > 0) {
    currentMessage.value = messages[Math.floor(Math.random() * messages.length)].message
  }
}

// API에서 도전과제 목록 가져오기
const fetchChallenges = async () => {
  try {
    const response = await axios.get('/api/v1/challenges', {
      withCredentials: true,  // 쿠키 포함하여 요청
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('도전과제 목록 응답:', response.data)
    
    const apiChallenges = response.data || []
    const customChallenges = JSON.parse(localStorage.getItem('adminChallenges') || '[]')
    
    // 첫 번째, 두 번째는 API에서 받은 도전과제 (우리가 제공하는 도전)
    // 세 번째, 네 번째는 ADMIN이 생성한 도전과제
    challenges.value = [
      apiChallenges[0] || { challengeTitle: '준비 중입니다.', description: '', isEmpty: true },
      apiChallenges[1] || { challengeTitle: '준비 중입니다.', description: '', isEmpty: true },
      customChallenges[0] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true, index: 3 },
      customChallenges[1] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true, index: 4 }
    ]
    
    // 월 정보 업데이트
    if (apiChallenges.length > 0 && apiChallenges[0].month) {
      currentMonth.value = apiChallenges[0].month
    }
    
    updateCompletedCount()
  } catch (error) {
    console.error('도전과제 목록 불러오기 실패:', error)
    // 실패 시 기존 로직으로 fallback
    const customChallenges = JSON.parse(localStorage.getItem('adminChallenges') || '[]')
    challenges.value = [
      { challengeTitle: '준비 중입니다.', description: '', isEmpty: true },
      { challengeTitle: '준비 중입니다.', description: '', isEmpty: true },
      customChallenges[0] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true, index: 3 },
      customChallenges[1] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true, index: 4 }
    ]
  }
}

// 도전과제 상세 정보 가져오기
const fetchChallengeDetail = async (challengeId) => {
  try {
    const response = await axios.get(`/api/v1/challenges/${challengeId}`)
    console.log('도전과제 상세 응답:', response.data)
    challengeDetails.value[challengeId] = response.data
    return response.data
  } catch (error) {
    console.error('도전과제 상세 불러오기 실패:', error)
    return null
  }
}

// 도전과제 상태 토글 함수
const toggleChallengeStatus = async (challenge) => {
  if (userRole.value !== 'ADMIN') return

  // API 도전과제인 경우 (우리가 제공하는 도전)
  if (challenge.id) {
    const challengeId = challenge.id
    const currentlyCompleted = isCompleted(challenge)
    const currentlyUploaded = isUploaded(challenge)

    // 업로드되지 않은 상태에서는 토글 불가
    if (!currentlyUploaded && !currentlyCompleted) {
      alert('먼저 도전 인증을 업로드해주세요.')
      return
    }

    try {
      if (currentlyCompleted) {
        // 완료 → 미완료 (cancel API)
        const response = await axios.put(`/api/v1/admin/challenges/${challengeId}/cancel`,{},{
          withCredentials: true,
          headers: {
            'Content-Type': 'application/json'
          }
        })
        console.log('Cancel API 응답:', response.data)
        
        // 로컬 상태 업데이트
        challenge.isSuccess = false
        
        modals.value.status = { 
          show: true, 
          message: `도전이 취소되었습니다.<br>${response.data.subtractedPoint}점이 차감되었습니다.` 
        }
      } else {
        // 미완료(업로드됨) → 완료 (complete API)
        const response = await axios.post(`/api/v1/admin/challenges/${challengeId}/complete`, {}, {
          withCredentials: true,
          headers: {
            'Content-Type': 'application/json'
          }
        })
        console.log('Complete API 응답:', response.data)
        
        // 로컬 상태 업데이트
        challenge.isSuccess = true
        
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
  // ADMIN이 생성한 도전과제인 경우
  else if (challenge.challengeId) {
    const challengeId = challenge.challengeId
    const currentlyCompleted = isCompleted(challenge)
    const currentlyUploaded = isUploaded(challenge)

    if (!currentlyUploaded && !currentlyCompleted) {
      alert('먼저 도전 인증을 업로드해주세요.')
      return
    }

    try {
      if (currentlyCompleted) {
        // Cancel API - withCredentials 추가!
        const response = await axios.put(`/api/v1/admin/challenges/${challengeId}/cancel`, {}, {
          withCredentials: true,
          headers: {
            'Content-Type': 'application/json'
          }
        })
        console.log('Cancel API 응답:', response.data)
        
        const data = localStorage.getItem(`admin_challenge_${challengeId}`)
        if (data) {
          const challengeData = JSON.parse(data)
          challengeData.is_success = false
          localStorage.setItem(`admin_challenge_${challengeId}`, JSON.stringify(challengeData))
        }
        
        modals.value.status = { 
          show: true, 
          message: `도전이 취소되었습니다.<br>${response.data.subtractedPoint}점이 차감되었습니다.` 
        }
      } else {
        const response = await axios.post(`/api/v1/admin/challenges/${challengeId}/complete`,{},{
          withCredentials: true,
          headers: {
            'Content-Type': 'application/json'
          }
        })
        console.log('Complete API 응답:', response.data)
        
        const data = localStorage.getItem(`admin_challenge_${challengeId}`)
        if (data) {
          const challengeData = JSON.parse(data)
          challengeData.is_success = true
          challengeData.completedAt = new Date().toISOString()
          challengeData.earnedPoints = response.data.earnedPoint
          localStorage.setItem(`admin_challenge_${challengeId}`, JSON.stringify(challengeData))
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
}

const closeStatusModal = () => {
  modals.value.status.show = false
}

// 모달 함수
const openModal = async (challenge, index) => {
  if (challenge.isEmpty) return
  
  // API 도전과제인 경우 (우리가 제공하는 도전) 상세 정보 가져오기
  if (challenge.id) {
    const detailChallenge = await fetchChallengeDetail(challenge.id)
    if (detailChallenge) {
      modals.value.detail = { 
        show: true, 
        selectedChallenge: detailChallenge, 
        selectedChallengeId: challenge.id 
      }
    }
  } else {
    // ADMIN이 생성한 도전과제
    modals.value.detail = { 
      show: true, 
      selectedChallenge: challenge, 
      selectedChallengeId: challenge.challengeId 
    }
  }
}

const closeModal = () => {
  modals.value.detail.show = false
}

const editChallenge = (index) => {
  const challenge = challenges.value[index]
  modals.value.edit = {
    show: true,
    form: { 
      title: challenge.challengeTitle || challenge.title, 
      description: challenge.description, 
      place: challenge.challengePlace || challenge.place || '' 
    },
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

const saveEditChallenge = async () => {
  const { form, editingIndex } = modals.value.edit
  if (!form.title.trim() || !form.description.trim()) {
    alert('제목과 설명을 모두 입력해주세요.')
    return
  }

  try {
    const challenge = challenges.value[editingIndex]
    
    // API 도전과제인 경우 (우리가 제공하는 도전) - 수정 불가
    if (challenge.id) {
      alert('시스템 제공 도전과제는 수정할 수 없습니다.')
      return
    } 
    // ADMIN이 생성한 도전과제인 경우
    else if (challenge.challengeId) {
      const challengeId = challenge.challengeId

      const response = await axios.put(`/api/v1/admin/challenges/${challengeId}`, {
        challengeTitle: form.title.trim(),
        challengePlace: form.place.trim(),
        description: form.description.trim()
      }, {
        withCredentials: true,
        headers: {
          'Content-Type': 'application/json'
        }
      })

      // 로컬 상태 업데이트
      challenges.value[editingIndex] = {
        ...challenges.value[editingIndex],
        challengeTitle: response.data.challengeTitle,
        challengePlace: response.data.challengePlace,
        description: response.data.description
      }

      // localStorage 업데이트
      const adminChallenges = JSON.parse(localStorage.getItem('adminChallenges') || '[]')
      const adminIndex = editingIndex - 2
      
      adminChallenges[adminIndex] = {
        ...adminChallenges[adminIndex],
        challengeTitle: response.data.challengeTitle,
        challengePlace: response.data.challengePlace,
        description: response.data.description
      }
      
      localStorage.setItem('adminChallenges', JSON.stringify(adminChallenges))
    }

    modals.value.edit.show = false
    modals.value.edit.showSuccess = true

  } catch (error) {
    console.error('도전과제 수정 실패:', error)
    
    if (error.response?.status === 401 || error.response?.status === 403) {
      alert('로그인이 필요합니다. 다시 로그인해주세요.')
    } else {
      alert('도전과제 수정에 실패했습니다.')
    }
    return
  }
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

const confirmDelete = async () => {
  const selectedIndex = modals.value.delete.selectedIndex
  if (selectedIndex !== null) {
    const challenge = challenges.value[selectedIndex]
    
    try {
      // API 도전과제인 경우 (우리가 제공하는 도전) - 삭제 불가
      if (challenge.id) {
        alert('시스템 제공 도전과제는 삭제할 수 없습니다.')
        return
      } 
      // ADMIN이 생성한 도전과제인 경우
      else if (challenge.challengeId) {
        const response = await axios.delete(`/api/v1/admin/challenges/${challenge.challengeId}`, {
          withCredentials: true,
          headers: {
            'Content-Type': 'application/json'
          }
        })
        console.log(response.data.message)

        // localStorage에서도 제거
        const adminChallenges = JSON.parse(localStorage.getItem('adminChallenges') || '[]')
        const adminIndex = selectedIndex - 2
        adminChallenges.splice(adminIndex, 1)
        localStorage.setItem('adminChallenges', JSON.stringify(adminChallenges))
      }
      
      // 목록에서 제거하고 다시 불러오기
      await fetchChallenges()

    } catch (error) {
      console.error('도전과제 삭제 실패:', error)
      
      if (error.response?.status === 401 || error.response?.status === 403) {
        alert('로그인이 필요합니다. 다시 로그인해주세요.')
      } else {
        alert('도전과제 삭제에 실패했습니다.')
      }
      return
    }
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

// 네비게이션 
const moveToCreate = () => router.push({ name: 'challengeCreate' })

const moveToFinish = () => {
  const challengeId = selectedChallenge.value.id || selectedChallenge.value.challengeId || selectedChallengeId.value
  router.push(`/admin/challenges/${challengeId}/complete`)
}

// 라이프사이클 훅
onMounted(async () => {
  // userRole이 없으면 가져오기
  if (!userStore.userRole) {
    await userStore.fetchUserRole()
  }
  
  console.log('ChallengeView userRole:', userRole.value)
  console.log('ChallengeView currentMonth:', currentMonth.value)
  
  loadMessages()
  await fetchChallenges() // API에서 도전과제 목록 가져오기
})

watch(percent, updateMessage)
watch(() => router.currentRoute.value, async () => {
  await fetchChallenges()
  updateCompletedCount()
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