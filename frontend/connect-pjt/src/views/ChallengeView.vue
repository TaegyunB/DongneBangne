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
          <!-- 완료/미완료 상태 표시만 (클릭 불가) -->
          <div 
            class="challenge-complete-btn"
            :class="{ 'completed': isCompleted(challenge) }"
          >
            {{ getButtonText(challenge) }}
          </div>
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
  delete: { show: false, showFinal: false, selectedChallenge: null, selectedIndex: null }
})

// 계산된 속성
const shouldShowCreateButton = computed(() => {
  const customChallengeCount = challenges.value.filter((challenge, index) => 
    index >= 2 && challenge.challengeType === 'CUSTOM' && !challenge.isEmpty
  ).length
  return customChallengeCount < 2
})

// 모달 상태 단축 접근
const showModal = computed(() => modals.value.detail.show)
const selectedChallenge = computed(() => modals.value.detail.selectedChallenge)
const selectedChallengeId = computed(() => modals.value.detail.selectedChallengeId)
const showEditModal = computed(() => modals.value.edit.show)
const showEditSuccessModal = computed(() => modals.value.edit.showSuccess)
const editForm = computed(() => modals.value.edit.form)
const showDeleteModal = computed(() => modals.value.delete.show)
const showFinalDeleteModal = computed(() => modals.value.delete.showFinal)

// 핵심 기능 함수들 
const isCompleted = (challenge) => {
  if (challenge.id) {
    // API에서 받은 도전과제 (서비스 제공 또는 커스텀)
    return challenge.isSuccess === true
  } else {
    // 빈 칸
    return false
  }
}

const getButtonText = (challenge) => {
  if (isCompleted(challenge)) {
    return '완료'
  } else {
    return '미완료'
  }
}

const getChallengeImage = (challenge) => {
  if (challenge.id && challenge.challengeImage) {
    // API에서 받은 도전과제의 이미지
    return challenge.challengeImage
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
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('도전과제 목록 응답:', response.data)

    
    const data = response.data
    
    // 현재 월 업데이트
    if (data.month) {
      currentMonth.value = data.month
    }
    
    // 서비스 제공 도전과제
    const serviceChallenges = data.serviceChallenges || []
    
    // 커스텀 도전과제
    const customChallenges = data.customChallenges || []

    //디버깅
    console.log(serviceChallenges[0])
    console.log(serviceChallenges[1])
    
    // 4개의 슬롯에 배치
    challenges.value = [
      // 첫 번째, 두 번째는 서비스 제공 도전과제
      serviceChallenges[0] || { challengeTitle: '준비 중입니다.', description: '', isEmpty: true },
      serviceChallenges[1] || { challengeTitle: '준비 중입니다.', description: '', isEmpty: true },
      
      // 세 번째, 네 번째는 커스텀 도전과제
      customChallenges[0] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true, index: 3 },
      customChallenges[1] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true, index: 4 }
    ]
    
    updateCompletedCount()
  } catch (error) {
    console.error('도전과제 목록 불러오기 실패:', error)
    // 에러 시 기본값 설정
    challenges.value = [
      { challengeTitle: '준비 중입니다.', description: '', isEmpty: true },
      { challengeTitle: '준비 중입니다.', description: '', isEmpty: true },
      { title: '도전과제를 생성해주세요', description: '', isEmpty: true, index: 3 },
      { title: '도전과제를 생성해주세요', description: '', isEmpty: true, index: 4 }
    ]
  }
}

// 도전과제 상세 정보 가져오기
const fetchChallengeDetail = async (challengeId) => {
  try {
    const response = await axios.get(`/api/v1/challenges/${challengeId}`, {
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('도전과제 상세 응답:', response.data)
    challengeDetails.value[challengeId] = response.data
    return response.data
  } catch (error) {
    console.error('도전과제 상세 불러오기 실패:', error)
    return null
  }
}

// 모달 함수
const openModal = async (challenge, index) => {
  if (challenge.isEmpty) return
  
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
    modals.value.detail = { 
      show: true, 
      selectedChallenge: challenge, 
      selectedChallengeId: challenge.id 
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
    
    if (challenge.challengeType === 'SERVICE') {
      alert('서비스 제공 도전과제는 수정할 수 없습니다.')
      return
    } 
    else if (challenge.challengeType === 'CUSTOM' && challenge.id) {
      const challengeId = challenge.id

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

      // 성공 시 로컬 상태 업데이트
      challenges.value[editingIndex] = {
        ...challenges.value[editingIndex],
        challengeTitle: response.data.challengeTitle,
        challengePlace: response.data.challengePlace,
        description: response.data.description
      }
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
      if (challenge.challengeType === 'SERVICE') {
        alert('서비스 제공 도전과제는 삭제할 수 없습니다.')
        return
      } 
      else if (challenge.challengeType === 'CUSTOM' && challenge.id) {
        const response = await axios.delete(`/api/v1/admin/challenges/${challenge.id}`, {
          withCredentials: true,
          headers: {
            'Content-Type': 'application/json'
          }
        })
        console.log(response.data.message)
      }
      
      // 삭제 후 목록 다시 불러오기
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
  const challengeId = selectedChallenge.value.id || selectedChallengeId.value
  router.push(`/admin/challenges/${challengeId}/complete`)
}

// 라이프사이클 훅
onMounted(async () => {
  if (!userStore.userRole) {
    await userStore.fetchUserRole()
  }
  
  console.log('ChallengeView userRole:', userRole.value)
  console.log('ChallengeView currentMonth:', currentMonth.value)
  
  loadMessages()
  await fetchChallenges()
})

watch(percent, updateMessage)
watch(() => router.currentRoute.value, async () => {
  await fetchChallenges()
  updateCompletedCount()
}, { immediate: true })
</script>

<style>
        /* 기본 스타일 */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background-color: #f8f9fa;
            color: #333;
            line-height: 1.6;
        }

        /* 메인 컬러 변수 */
        :root {
            --primary-orange: #FF6B35;
            --secondary-orange: #FFE5DE;
            --primary-blue: #4A90E2;
            --secondary-blue: #E8F4FD;
            --neutral-gray: #f5f5f5;
            --dark-gray: #666;
            --text-black: #333;
            --border-light: #e0e0e0;
        }

        /* 헤더 */
        .header {
            text-align: center;
            margin: 30px auto;
            font-size: 32px;
            font-weight: 700;
            color: var(--text-black);
        }

        /* 진행률 섹션 */
        .progress-container {
            max-width: 800px;
            width: 90%;
            margin: 30px auto;
            display: flex;
            align-items: center;
            gap: 20px;
            background: white;
            padding: 25px;
            border-radius: 16px;
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
        }

        .progress-container h3 {
            font-size: 20px;
            font-weight: 600;
            color: var(--text-black);
            min-width: 80px;
        }

        .progress-bar {
            flex: 1;
            height: 12px;
            border-radius: 8px;
            background: var(--neutral-gray);
            overflow: hidden;
        }

        .inner-bar {
            height: 100%;
            border-radius: 8px;
            background: linear-gradient(90deg, var(--primary-orange), var(--primary-blue));
            transition: width 0.3s ease;
        }

        /* 메시지 박스 */
        .message-box {
            max-width: 800px;
            width: 90%;
            margin: 20px auto;
            color: var(--primary-blue);
            font-weight: 600;
            text-align: center;
            padding: 20px;
            background: var(--secondary-blue);
            border-radius: 16px;
            font-size: 18px;
            border: 2px solid rgba(74, 144, 226, 0.1);
        }

        /* 도전과제 컨테이너 */
        .challenge-container {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            max-width: 1200px;
            width: 90%;
            margin: 30px auto;
            gap: 24px;
        }

        .single-challenge {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
            cursor: pointer;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            min-height: 420px;
            transition: all 0.2s ease;
            border: 2px solid transparent;
        }

        .single-challenge:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
            border-color: var(--primary-orange);
        }

        .challenge-image {
            width: 100%;
            height: 200px;
            overflow: hidden;
            background: var(--neutral-gray);
        }

        .challenge-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            object-position: center;
        }

        .challenge-content {
            position: relative;
            padding: 24px;
            display: flex;
            flex-direction: column;
            flex: 1;
            background: white;
        }

        .text-content {
            flex: 1;
        }

        .title-with-buttons {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 12px;
        }

        .challenge-content h2 {
            margin: 0;
            font-size: 20px;
            font-weight: 700;
            flex: 1;
            color: var(--text-black);
            line-height: 1.3;
        }

        .action-buttons {
            display: flex;
            gap: 8px;
            margin-left: 12px;
        }

        .edit-btn, .delete-btn {
            padding: 6px 12px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            color: white;
            transition: all 0.2s ease;
        }

        .edit-btn {
            background-color: var(--primary-blue);
        }

        .edit-btn:hover {
            background-color: #357abd;
        }

        .delete-btn {
            background-color: #e74c3c;
        }

        .delete-btn:hover {
            background-color: #c0392b;
        }

        .challenge-content p {
            margin: 8px 0 20px 0;
            font-size: 16px;
            font-weight: 400;
            line-height: 1.5;
            color: var(--dark-gray);
        }

        /* 완료 버튼 */
        .challenge-complete-btn {
            position: absolute;
            bottom: 24px;
            left: 50%;
            transform: translateX(-50%);
            font-weight: 600;
            color: white;
            width: 100px;
            height: 36px;
            border: none;
            font-size: 16px;
            border-radius: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: var(--dark-gray);
            transition: all 0.2s ease;
        }

        .challenge-complete-btn.completed {
            background-color: var(--primary-blue);
        }

        /* 생성 버튼 */
        .create-challenge {
            display: flex;
            justify-content: center;
            align-items: center;
            width: 100%;
            margin: 30px 0;
        }

        .challenge-btn {
            background: linear-gradient(135deg, var(--primary-orange), var(--primary-blue));
            color: white;
            width: 280px;
            height: 56px;
            border: none;
            cursor: pointer;
            font-size: 18px;
            font-weight: 600;
            border-radius: 16px;
            transition: all 0.2s ease;
            box-shadow: 0 4px 16px rgba(255, 107, 53, 0.3);
        }

        .challenge-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(255, 107, 53, 0.4);
        }

        /* 모달 스타일 */
        .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            background: rgba(0, 0, 0, 0.5);
            z-index: 1000;
            display: flex;
            align-items: center;
            justify-content: center;
            backdrop-filter: blur(4px);
        }

        .modal-content {
            background: white;
            border-radius: 20px;
            padding: 32px;
            width: 90%;
            max-width: 480px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
            text-align: center;
            z-index: 1001;
            position: relative;
        }

        .modal-close-btn {
            position: absolute;
            top: 16px;
            right: 16px;
            width: 32px;
            height: 32px;
            border: none;
            background: var(--neutral-gray);
            font-size: 20px;
            font-weight: 700;
            color: var(--dark-gray);
            cursor: pointer;
            border-radius: 50%;
            transition: all 0.2s ease;
        }

        .modal-close-btn:hover {
            background-color: var(--border-light);
            color: var(--text-black);
        }

        .modal-content h2 {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 16px;
            color: var(--text-black);
        }

        .modal-description {
            font-size: 16px;
            line-height: 1.6;
            margin-bottom: 20px;
            color: var(--dark-gray);
        }

        .modal-place {
            font-size: 16px;
            color: var(--dark-gray);
            margin-bottom: 24px;
            padding: 12px;
            background: var(--neutral-gray);
            border-radius: 12px;
        }

        .modal-button {
            background-color: var(--primary-orange);
            color: white;
            padding: 14px 28px;
            font-size: 16px;
            font-weight: 600;
            border-radius: 12px;
            border: none;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .modal-button:hover {
            background-color: #e55a2b;
            transform: translateY(-1px);
        }

        .completed-message {
            background-color: var(--secondary-blue);
            color: var(--primary-blue);
            padding: 16px 24px;
            border-radius: 12px;
            font-size: 16px;
            font-weight: 600;
            border: 2px solid rgba(74, 144, 226, 0.2);
        }

        /* 수정 모달 */
        .edit-modal {
            max-width: 520px;
            text-align: left;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: var(--text-black);
            font-size: 16px;
        }

        .form-input, .form-textarea {
            width: 100%;
            padding: 14px;
            border: 2px solid var(--border-light);
            border-radius: 12px;
            font-size: 16px;
            transition: border-color 0.3s ease;
            font-family: inherit;
        }

        .form-input:focus, .form-textarea:focus {
            outline: none;
            border-color: var(--primary-orange);
        }

        .form-textarea {
            resize: vertical;
            min-height: 100px;
        }

        /* 모달 버튼들 */
        .modal-buttons {
            display: flex;
            gap: 12px;
            justify-content: center;
            margin-top: 24px;
        }

        .btn-cancel, .btn-save, .delete-confirm-btn, .delete-success-btn {
            padding: 14px 24px;
            border: none;
            border-radius: 12px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .btn-cancel {
            background-color: var(--neutral-gray);
            color: var(--dark-gray);
        }

        .btn-cancel:hover {
            background-color: var(--border-light);
        }

        .btn-save, .delete-confirm-btn, .delete-success-btn {
            background-color: var(--primary-orange);
            color: white;
        }

        .btn-save:hover:not(:disabled), .delete-confirm-btn:hover, .delete-success-btn:hover {
            background-color: #e55a2b;
            transform: translateY(-1px);
        }

        .btn-save:disabled {
            background-color: #ccc;
            cursor: not-allowed;
            transform: none;
        }

        .delete-modal {
            max-width: 400px;
        }

        .delete-confirm-btn {
            width: 140px;
        }

        .delete-success-btn {
            width: 180px;
        }

        /* 반응형 디자인 */
        @media (max-width: 768px) {
            .header {
                font-size: 28px;
                margin: 20px auto;
            }

            .progress-container {
                flex-direction: column;
                gap: 16px;
                text-align: center;
            }

            .progress-container h3 {
                min-width: auto;
            }

            .challenge-container {
                grid-template-columns: 1fr;
                gap: 16px;
            }

            .title-with-buttons {
                flex-direction: column;
                align-items: flex-start;
            }

            .action-buttons {
                margin-left: 0;
                margin-top: 8px;
            }

            .modal-buttons {
                flex-direction: column;
                gap: 12px;
            }

            .btn-cancel, .btn-save {
                width: 100%;
            }

            .challenge-btn {
                width: 100%;
                max-width: 320px;
            }
        }

        /* 접근성 개선 */
        @media (prefers-reduced-motion: reduce) {
            *, *::before, *::after {
                animation-duration: 0.01ms !important;
                animation-iteration-count: 1 !important;
                transition-duration: 0.01ms !important;
            }
        }

        /* 고대비 모드 지원 */
        @media (prefers-contrast: high) {
            .single-challenge {
                border: 2px solid var(--text-black);
            }
            
            .modal-content {
                border: 2px solid var(--text-black);
            }
        }
    </style>