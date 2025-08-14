<!-- AI 신문 생성 가이드 팝업 (항상 표시) -->
        <div 
          class="ai-news-guide-popup"
        >
          <div class="popup-content">
            <span v-if="!isAINewsButtonEnabled">도전인증을을 해주세요</span>
            <span v-else>완료된 도전과제로 신문을 생성합니다</span>
            <div class="popup-arrow"></div>
          </div>
        </div><template>
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
     
    <div class="message-and-ai-container">
      <div class="message-box">
        <p>{{ currentMessage }}</p>
      </div>

      <!-- AI 신문 생성 버튼을 메시지 박스 우측에 배치 -->
      <div v-if="userRole === 'ADMIN'" class="ai-news-section">
        <button 
          @click="goToAINews" 
          class="btn-ai-news" 
          :disabled="creatingAINews || !isAINewsButtonEnabled"
          :title="getAINewsButtonTooltip"
        >
          {{ creatingAINews ? ' AI 신문 생성 중...' : 'AI 신문 생성하기' }}
        </button>
        
        <!-- AI 신문 생성 가이드 팝업 (항상 표시) -->
        <div 
          class="ai-news-guide-popup"
        >
          <div class="popup-content">
            <span v-if="!isAINewsButtonEnabled">도전인증을을 해주세요</span>
            <span v-else>완료된 도전과제로 신문을 생성합니다</span>
            <div class="popup-arrow"></div>
          </div>
        </div>
      </div>
    </div>
     
    <!-- 도전과제 목록 -->
    <div class="challenge-container">
      <div
        v-for="(challenge, index) in challenges"
        :key="index"
        class="single-challenge"
        :class="`challenge-${index + 1}`"
      >
        <!-- 이미지 영역 -->
        <div class="challenge-image">
          <!-- 인증되지 않은 도전: 회색 배경만 -->
          <div 
            v-if="!isCompleted(challenge)" 
            class="challenge-placeholder"
          >
            <!-- 텍스트 제거 -->
          </div>
          <!-- 인증된 도전 또는 빈 도전: 이미지 표시 -->
          <img 
            v-else
            :src="getChallengeImage(challenge)" 
            :alt="challenge.challengeTitle || challenge.title"
            class="challenge-img"
            crossorigin="anonymous"
            @error="onImageError($event, challenge)"
            @load="onImageLoad($event, challenge)"
          />
        </div>
        
        <!-- 텍스트 영역 -->
        <div class="challenge-content">
          <div class="text-content">
            <div class="title-with-buttons">
              <h2>{{ getDisplayTitle(challenge, index) }}</h2>
              <!-- 3,4번째 칸에서 미션이 없을 때만 생성 버튼 표시 -->
              <div v-if="shouldShowCreateButton(challenge, index)" class="action-buttons">
                <button class="create-btn" @click.stop="moveToCreate()">생성</button>
              </div>
            </div>
          </div>
          <!-- 상태별 버튼 -->
          <div 
            v-if="!challenge.isEmpty"
            class="challenge-complete-btn"
            :class="getButtonClass(challenge)"
            @click="handleButtonClick(challenge, index)"
          >
            {{ getButtonText(challenge) }}
          </div>
          <!-- 생성 전 버튼 (MEMBER, 빈 도전) -->
          <div 
            v-else-if="userRole !== 'ADMIN' && challenge.isEmpty"
            class="challenge-complete-btn btn-not-created"
            @click="showNotCreatedModal()"
          >
            도전 생성 전
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
        
        <!-- 인증 전 상태일 때 대형 인증 버튼 (ADMIN만) -->
        <div v-if="userRole === 'ADMIN' && !selectedChallenge.isEmpty && !isCompleted(selectedChallenge)">
          <button class="large-verify-btn" @click="moveToFinish">
            도전 인증하기
          </button>
        </div>
        
        <!-- 모달 내 이미지 표시 (완료된 도전만) -->
        <div v-else-if="selectedChallenge.challengeImage && isCompleted(selectedChallenge)" class="modal-image">
          <img 
            :src="getChallengeImage(selectedChallenge)" 
            :alt="selectedChallenge.challengeTitle"
            crossorigin="anonymous"
            @error="onImageError($event, selectedChallenge)"
            @load="onImageLoad($event, selectedChallenge)"
          />
        </div>
        
        <!-- 모달 내 버튼들 -->
        <div class="modal-action-buttons">
          <!-- 도전 인증 버튼 (ADMIN이고 완료되지 않은 도전) -->
          <button 
            v-if="userRole === 'ADMIN' && !selectedChallenge.isEmpty && !isCompleted(selectedChallenge)" 
            class="modal-button modal-complete-btn" 
            @click="moveToFinish"
          >
            도전 인증하기
          </button>
          
          <!-- 완료 메시지 (완료된 도전) -->
          <div 
            v-if="!selectedChallenge.isEmpty && isCompleted(selectedChallenge)"
            class="completed-message"
          >
            완료된 도전입니다
          </div>
          
          <!-- 수정/삭제 버튼 (ADMIN이고 커스텀 도전과제인 경우) -->
          <div v-if="shouldShowEditDeleteButtons(selectedChallenge)" class="modal-edit-delete-buttons">
            <button 
              v-if="shouldShowEditButton(selectedChallenge)" 
              class="modal-edit-btn" 
              @click="editChallenge(getSelectedChallengeIndex())"
            >
              수정
            </button>
            <button 
              v-if="shouldShowDeleteButton(selectedChallenge)" 
              class="modal-delete-btn" 
              @click="showDeleteConfirm(getSelectedChallengeIndex())"
            >
              삭제
            </button>
          </div>
          
          <!-- 삭제 버튼 (ADMIN이고 완료된 도전) -->
          <div v-if="userRole === 'ADMIN' && !selectedChallenge.isEmpty && isCompleted(selectedChallenge) && selectedChallenge.challengeType === 'CUSTOM'" class="modal-edit-delete-buttons">
            <button 
              class="modal-delete-btn" 
              @click="showDeleteConfirm(getSelectedChallengeIndex())"
            >
              삭제
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 도전 생성 전 알림 모달 -->
    <div v-if="showNotCreatedAlertModal" class="modal-overlay" @click.self="closeNotCreatedAlertModal">
      <div class="modal-content">
        <button class="modal-close-btn" @click="closeNotCreatedAlertModal">×</button>
        <h2>알림</h2>
        <p class="modal-description">아직 도전이 생성되지 않았습니다</p>
        <div class="modal-action-buttons">
          <button class="modal-button" @click="closeNotCreatedAlertModal">확인</button>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue' 
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user' 
import api from '@/api/axios' // 기존 API 클라이언트 import

import defaultImage from '@/assets/default_image.png'

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
const creatingAINews = ref(false) // AI 신문 생성 중 상태

// 모달 상태 - 새로운 알림 모달 추가
const modals = ref({
  detail: { show: false, selectedChallenge: { challengeTitle: '', description: '', challengePlace: '' }, selectedChallengeId: null },
  edit: { show: false, form: { title: '', description: '', place: '' }, editingIndex: null, showSuccess: false },
  delete: { show: false, showFinal: false, selectedChallenge: null, selectedIndex: null },
  notCreatedAlert: { show: false } // 새로운 모달 추가
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
const showNotCreatedAlertModal = computed(() => modals.value.notCreatedAlert.show) // 새로운 모달

// 새로운 유틸리티 함수들
const getDisplayTitle = (challenge, index) => {
  if (!challenge.isEmpty) {
    return challenge.challengeTitle || challenge.title
  }
  
  // 빈 도전인 경우
  if (index >= 2) { // 3, 4번째 칸 (커스텀 도전)
    return userRole.value === 'ADMIN' 
      ? '다 같이 재밌는 도전을 수행해보는 것은 어때요?' 
      : '관리자가 재밌는 도전 주제를 고민하고 있어요'
  } else { // 1, 2번째 칸 (서비스 제공 도전)
    return '준비 중입니다.'
  }
}

// 생성 버튼 표시 조건 (3,4번째 칸에서 미션이 없을 때만)
const shouldShowCreateButton = (challenge, index) => {
  return userRole.value === 'ADMIN' && index >= 2 && challenge.isEmpty
}

// 수정 버튼 표시 조건 (완료 전에만)
const shouldShowEditButton = (challenge) => {
  return userRole.value === 'ADMIN' && 
         !challenge.isEmpty && 
         challenge.challengeType === 'CUSTOM' &&
         !isCompleted(challenge)
}

// 삭제 버튼 표시 조건 (완료 전후 모두)
const shouldShowDeleteButton = (challenge) => {
  return userRole.value === 'ADMIN' && 
         !challenge.isEmpty && 
         challenge.challengeType === 'CUSTOM'
}

// 수정/삭제 버튼 영역 표시 조건 (둘 중 하나라도 보여야 할 때)
const shouldShowEditDeleteButtons = (challenge) => {
  return shouldShowEditButton(challenge) || shouldShowDeleteButton(challenge)
}

// 선택된 도전과제의 인덱스 찾기
const getSelectedChallengeIndex = () => {
  const selectedId = selectedChallengeId.value
  return challenges.value.findIndex(challenge => challenge.id === selectedId)
}

// AI 신문 버튼 활성화 조건
const isAINewsButtonEnabled = computed(() => {
  // 완료된 도전이 1개 이상 있을 때만 활성화
  return count.value > 0
})

// AI 신문 버튼 툴팁 메시지
const getAINewsButtonTooltip = computed(() => {
  if (count.value === 0) {
    return '미션을 하나라도 인증해야 활성화됩니다'
  }
  return `완료된 ${count.value}개의 도전과제로 AI 신문을 생성합니다`
})

// AI 신문 설명 텍스트
const getAINewsDescription = computed(() => {
  if (count.value === 0) {
    return '미션을 한 개라도 인증해야 AI 신문을 생성할 수 있습니다.'
  }
  return '완료된 도전과제들을 바탕으로 특별한 신문을 생성할 수 있습니다.'
})

// 기존 핵심 기능 함수들 
const isCompleted = (challenge) => {
  if (challenge.id) {
    // API에서 받은 도전과제 (서비스 제공 또는 커스텀)
    return challenge.isSuccess === true
  } else {
    // 빈 칸
    return false
  }
}

// 새로운 버튼 텍스트 함수
const getButtonText = (challenge) => {
  if (isCompleted(challenge)) {
    return '완료'
  }
  
  if (userRole.value === 'ADMIN') {
    return '도전 인증'
  } else {
    return '도전 인증 전'
  }
}

// 새로운 버튼 클래스 함수
const getButtonClass = (challenge) => {
  if (isCompleted(challenge)) {
    return 'btn-completed'
  }
  
  if (userRole.value === 'ADMIN') {
    return 'btn-verify'
  } else {
    return 'btn-not-verified'
  }
}

// 새로운 버튼 클릭 핸들러
const handleButtonClick = (challenge, index) => {
  modals.value.detail = { 
    show: true, 
    selectedChallenge: challenge, 
    selectedChallengeId: challenge.id 
  }
}

// 새로운 알림 모달 함수들
const showNotCreatedModal = () => {
  modals.value.notCreatedAlert.show = true
}

const closeNotCreatedAlertModal = () => {
  modals.value.notCreatedAlert.show = false
}

// 이미지 처리 함수 (S3 디버깅 포함)
const getChallengeImage = (challenge) => {
  console.log('=== 이미지 디버깅 ===')
  console.log('Challenge 전체 객체:', challenge)
  console.log('Challenge ID:', challenge.id)
  console.log('Challenge Image URL:', challenge.challengeImage)
  console.log('Challenge isEmpty:', challenge.isEmpty)
  console.log('URL 타입:', typeof challenge.challengeImage)
  
  // 빈 도전인 경우 기본 이미지
  if (challenge.isEmpty) {
    console.log('📷 빈 도전 - 기본 이미지 사용')
    return defaultImage
  }
  
  if (challenge.id && challenge.challengeImage) {
    // S3 URL 확인
    if (challenge.challengeImage.includes('amazonaws.com') || 
        challenge.challengeImage.includes('s3')) {
      console.log('✅ S3 URL 감지:', challenge.challengeImage)
    } else {
      console.log('⚠️ S3 URL이 아닐 수 있음:', challenge.challengeImage)
    }
    return challenge.challengeImage
  }
  
  console.log('📷 ID 또는 이미지 URL 없음 - 기본 이미지 사용')
  return defaultImage
}

// 이미지 에러 핸들링
const onImageError = (event, challenge) => {
  console.error('❌ 이미지 로드 실패:', {
    src: event.target.src,
    challengeId: challenge.id,
    challengeImage: challenge.challengeImage,
    error: event,
    errorType: event.target.src.includes('s3') ? 'S3 CORS/권한 문제' : '기타 오류'
  })
  
  // S3 이미지 에러인 경우 특별 처리
  if (event.target.src.includes('s3') || event.target.src.includes('amazonaws')) {
    console.warn('🔒 S3 이미지 로드 실패 - CORS 또는 권한 문제일 가능성')
  }
  
  // 기본 이미지로 대체
  event.target.src = defaultImage
}

const onImageLoad = (event, challenge) => {
  console.log('✅ 이미지 로드 성공:', {
    src: event.target.src,
    challengeId: challenge.id
  })
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
    // 기존 api 인스턴스 사용
    const response = await api.get('/api/v1/challenges')
    console.log('도전과제 목록 응답:', response.data)

    const data = response.data
    console.log('data:', data)

    console.log('=== 각 도전과제별 이미지 확인 ===')
    challenges.value.forEach((challenge, index) => {
      console.log(`Challenge ${index + 1}:`, {
        id: challenge.id,
        title: challenge.challengeTitle,
        hasImage: !!challenge.challengeImage,
        imageUrl: challenge.challengeImage,
        imageType: typeof challenge.challengeImage
      })
    })
    
    // 현재 월 업데이트
    if (data.month) {
      currentMonth.value = data.month
    }
    
    // 서비스 제공 도전과제
    const serviceChallenges = data.serviceChallenges || []
    
    // 커스텀 도전과제
    const customChallenges = data.customChallenges || []

    // 디버깅 - 이미지 URL 확인
    console.log('=== 전체 API 응답 확인 ===')
    console.log('Full API response:', JSON.stringify(data, null, 2))
    
    console.log('=== 서비스 도전과제 이미지 확인 ===')
    serviceChallenges.forEach((challenge, index) => {
      console.log(`Service Challenge ${index + 1}:`, {
        id: challenge.id,
        title: challenge.challengeTitle,
        image: challenge.challengeImage,
        fullObject: challenge
      })
    })
    
    console.log('=== 커스텀 도전과제 이미지 확인 ===')
    customChallenges.forEach((challenge, index) => {
      console.log(`Custom Challenge ${index + 1}:`, {
        id: challenge.id,
        title: challenge.challengeTitle,
        image: challenge.challengeImage,
        fullObject: challenge
      })
    })
    
    // 4개의 슬롯에 배치
    challenges.value = [
      // 첫 번째, 두 번째는 서비스 제공 도전과제
      serviceChallenges[0] || { challengeTitle: '준비 중입니다.', description: '', isEmpty: true },
      serviceChallenges[1] || { challengeTitle: '준비 중입니다.', description: '', isEmpty: true },
      
      // 세 번째, 네 번째는 커스텀 도전과제
      customChallenges[0] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true, index: 3 },
      customChallenges[1] || { title: '도전과제를 생성해주세요', description: '', isEmpty: true, index: 4 }
    ]
    
    console.log('=== 최종 challenges 배열 확인 ===')
    challenges.value.forEach((challenge, index) => {
      console.log(`Final Challenge ${index + 1}:`, {
        id: challenge.id,
        title: challenge.challengeTitle || challenge.title,
        image: challenge.challengeImage,
        isEmpty: challenge.isEmpty,
        fullObject: challenge
      })
    })
    
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
    // 기존 api 인스턴스 사용
    const response = await api.get(`/api/v1/challenges/${challengeId}`)
    console.log('도전과제 상세 응답:', response.data)
    challengeDetails.value[challengeId] = response.data
    return response.data
  } catch (error) {
    console.error('도전과제 상세 불러오기 실패:', error)
    return null
  }
}

// 모달 함수
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
  // 상세 모달 닫기
  closeModal()
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

      // 기존 api 인스턴스 사용
      const response = await api.put(`/api/v1/admin/challenges/${challengeId}`, {
        challengeTitle: form.title.trim(),
        challengePlace: form.place.trim(),
        description: form.description.trim()
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
  // 상세 모달 닫기
  closeModal()
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
        // 기존 api 인스턴스 사용
        const response = await api.delete(`/api/v1/admin/challenges/${challenge.id}`)
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

const goToAINews = async () => {
  // 완료된 도전이 없으면 실행하지 않음
  if (!isAINewsButtonEnabled.value) {
    alert('미션을 하나라도 인증해야 AI 신문을 생성할 수 있습니다.')
    return
  }
  
  // AI 신문 생성 API 호출
  creatingAINews.value = true
  
  try {
    console.log('AI 신문 생성 시작...')
    
    const response = await api.post('/api/v1/admin/ai-news/create', {
      year: new Date().getFullYear(),
      month: new Date().getMonth() + 1
    })
    
    console.log('AI 신문 생성 완료:', response.data)
    
    // 생성된 신문의 ID가 있다면 해당 상세 페이지로, 없다면 목록으로
    if (response.data && response.data.id) {
      router.push(`/news`)
    } else {
      router.push('/news')
    }
    
    alert('AI 신문이 성공적으로 생성되었습니다!')
    
  } catch (error) {
    console.error('AI 신문 생성 실패:', error)
    
    let errorMessage = 'AI 신문 생성에 실패했습니다.'
    
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message || '서버 오류'
      
      if (status === 400) {
        errorMessage = `신문 생성 조건이 맞지 않습니다: ${message}`
      } else if (status === 403) {
        errorMessage = 'AI 신문을 생성할 권한이 없습니다.'
      } else if (status === 409) {
        errorMessage = '이미 생성된 신문이 있습니다.'
      } else {
        errorMessage = `AI 신문 생성에 실패했습니다: ${message}`
      }
    } else if (error.request) {
      errorMessage = 'AI 신문 생성에 실패했습니다: 서버와 연결할 수 없습니다.'
    }
    
    alert(errorMessage)
    
    // 에러가 발생해도 신문 목록 페이지로 이동 (선택사항)
    router.push('/news')
  } finally {
    creatingAINews.value = false
  }
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
</script>

<style>
 /* 글꼴 정의 */
    @font-face {
      font-family: 'KoddiUD';
      src: url('@/assets/fonts/KoddiUDOnGothic-Regular.ttf') format('truetype');
      font-weight: 400;
      font-style: normal;
    }

    @font-face {
      font-family: 'KoddiUD';
      src: url('@/assets/fonts/KoddiUDOnGothic-Bold.ttf') format('truetype');
      font-weight: 700;
      font-style: normal;
    }

    @font-face {
      font-family: 'KoddiUD';
      src: url('@/assets/fonts/KoddiUDOnGothic-ExtraBold.ttf') format('truetype');
      font-weight: 800;
      font-style: normal;
    }

/* 기본 스타일 */
    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
        font-family: 'KoddiUD', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
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
        --primary-green: #28a745;
        --neutral-gray: #f5f5f5;
        --dark-gray: #666;
        --text-black: #333;
        --border-light: #e0e0e0;
        --pastel-yellow: #FFF9C4;
        --sky-blue: #87CEEB;
        --hover-blue: linear-gradient(135deg, #4A90E2, #87CEEB);
    }

    /* 헤더 */
    .header {
        text-align: center;
        margin: 20px auto;
        font-size: 32px;
        font-weight: 700;
        color: var(--text-black);
        font-family: 'KoddiUD', sans-serif;
    }

    /* 진행률 섹션 */
    .progress-container {
        max-width: 800px;
        width: 90%;
        margin: 15px auto;
        display: flex;
        align-items: center;
        gap: 20px;
        background: white;
        padding: 25px;
        border-radius: 16px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
        position: relative;
    }

    .progress-container h3 {
        font-size: 20px;
        font-weight: 500;
        color: var(--text-black);
        min-width: 80px;
        font-family: 'KoddiUD', sans-serif;
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
        background: linear-gradient(90deg, var(--pastel-yellow), var(--sky-blue));
        transition: width 0.3s ease;
    }

    /* 메시지 박스와 AI 신문 버튼 컨테이너 */
    .message-and-ai-container {
        max-width: 800px;
        width: 90%;
        margin: 15px auto;
        display: flex;
        align-items: stretch; /* 높이를 동일하게 맞춤 */
        gap: 20px;
    }

    /* 메시지 박스 */
    .message-box {
        flex: 1;
        color: rgb(0, 0, 0);
        font-weight: 600;
        text-align: center;
        padding: 15px;
        background: rgba(248, 239, 104, 0.225);
        border-radius: 16px;
        font-size: 18px;
        border: 3px solid rgb(255, 225, 0);
        box-shadow: 0 4px 16px rgba(255, 207, 17, 0.15);
        font-family: 'KoddiUD', sans-serif;
        margin: 0;
        display: flex;
        align-items: center; /* 텍스트를 세로 중앙 정렬 */
        justify-content: center;
    }

    .message-box p {
        margin: 0;
    }

    /* AI 신문 생성 섹션 - 메시지 박스 우측 */
    .ai-news-section {
        flex-shrink: 0;
        position: relative;
        display: flex;
        align-items: stretch; /* 버튼 높이를 메시지 박스와 맞춤 */
    }

    .btn-ai-news {
        background: rgba(255, 204,0);;
        color: rgb(0, 0, 0);
        border: none;
        padding: 0 20px; /* 세로 패딩 제거하고 가로 패딩만 */
        border-radius: 12px;
        font-size: 18px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s ease;
        white-space: nowrap;
        box-shadow: 0 4px 16px rgba(255, 207, 17, 0.15);
        font-family: 'KoddiUD', sans-serif;
        height: 100%; /* 컨테이너 높이에 맞춤 */
        display: flex;
        align-items: center;
        justify-content: center;
    }

        .btn-ai-news:hover:not(:disabled) {
        background: rgb(255, 157, 0);
        transform: translateY(-1px);
        box-shadow: 0 6px 16px rgba(255, 107, 53, 0.4);
    }

    .btn-ai-news:disabled {
        background: #9ca3af;
        cursor: not-allowed;
        transform: none;
        box-shadow: 0 2px 8px rgba(156, 163, 175, 0.3);
        opacity: 0.6;
    }

    .btn-ai-news:disabled:hover {
        background: #9ca3af;
        transform: none;
        box-shadow: 0 2px 8px rgba(156, 163, 175, 0.3);
    }

    /* AI 신문 가이드 팝업 */
    .ai-news-guide-popup {
        position: absolute;
        right: -270px; /* 버튼 오른쪽에 표시 */
        top: 50%;
        transform: translateY(-50%);
        z-index: 1000;
        animation: fadeInRight 0.3s ease-out;
    }

    .ai-news-guide-popup .popup-content {
        background: rgba(248, 239, 104, 0.225);
        color: black;
        padding: 12px 16px;
        border-radius: 8px;
        font-size: 14px;
        white-space: nowrap;
        position: relative;
        border: 1px solid rgb(255, 225, 0);
        box-shadow: 0 4px 16px rgba(255, 207, 17, 0.15);
        font-family: 'KoddiUD', sans-serif;
    }

    .ai-news-guide-popup .popup-arrow {
        position: absolute;
        left: -9px;
        top: 50%;
        transform: translateY(-50%);
        width: 0;
        height: 0;
        border-right: 8px solid rgba(248, 239, 104, 0.225);
        border-top: 6px solid transparent;
        border-bottom: 6px solid transparent;
    }
    
    .ai-news-guide-popup .popup-arrow::before {
        content: '';
        position: absolute;
        left: 1px;
        top: -6px;
        width: 0;
        height: 0;
        border-right: 8px solid rgb(255, 225, 0);
        border-top: 6px solid transparent;
        border-bottom: 6px solid transparent;
    }

    @keyframes fadeInLeft {
        0% {
            opacity: 0;
            transform: translateY(-50%) translateX(-10px);
        }
        100% {
            opacity: 1;
            transform: translateY(-50%) translateX(0);
        }
    }

    /* 도전과제 컨테이너 */
    .challenge-container {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
        max-width: 1200px;
        width: 90%;
        margin: 20px auto;
        gap: 20px;
    }

    .single-challenge {
        background: white;
        border-radius: 16px;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
        cursor: default; /* 카드 클릭 비활성화 */
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
        border: 2px solid;
        border-image: var(--hover-blue) 1;
    }

    .challenge-image {
        width: 100%;
        height: 200px;
        overflow: hidden;
        background: var(--neutral-gray);
        position: relative;
    }

    .challenge-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        object-position: center;
    }

    /* 인증 전 플레이스홀더 텍스트 스타일 - 텍스트 제거 */
    .challenge-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgb(220, 220, 220);
        color: var(--text-black);
        font-family: 'KoddiUD', sans-serif;
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
        font-family: 'KoddiUD', sans-serif;
    }

    .action-buttons {
        display: flex;
        gap: 8px;
        margin-left: 12px;
    }

    .create-btn {
        padding: 6px 12px;
        border: none;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        color: white;
        transition: all 0.2s ease;
        font-family: 'KoddiUD', sans-serif;
        background-color: var(--primary-green);
    }

    .create-btn:hover {
        background-color: #218838;
    }

    .challenge-content p {
        margin: 8px 0 20px 0;
        font-size: 16px;
        font-weight: 400;
        line-height: 1.5;
        color: var(--dark-gray);
        font-family: 'KoddiUD', sans-serif;
    }

    /* 완료 버튼 - 새로운 버튼 클래스들 */
    .challenge-complete-btn {
        position: absolute;
        bottom: 24px;
        left: 50%;
        transform: translateX(-50%);
        font-weight: 600;
        color: white;
        width: 120px;
        height: 36px;
        border: none;
        font-size: 16px;
        border-radius: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s ease;
        font-family: 'KoddiUD', sans-serif;
        cursor: pointer;
    }

    /* 도전 인증 버튼 (ADMIN, 인증 전) */
    .btn-verify {
        background-color: #FFA500;
        color: black;
    }

    .btn-verify:hover {
        background-color: #FF8C00;
        transform: translateX(-50%) translateY(-2px);
    }

    /* 완료 버튼 */
    .btn-completed {
        background-color: rgb(30, 58, 138); /* 진한 파란색 */
    }

    .btn-completed:hover {
        background-color: rgb(23, 37, 84); /* 더 진한 파란색 */
        transform: translateX(-50%) translateY(-2px);
    }

    /* 도전 생성 전 버튼 (MEMBER) */
    .btn-not-created {
        background-color: #6c757d;
    }

    .btn-not-created:hover {
        background-color: #5a6268;
        transform: translateX(-50%) translateY(-2px);
    }

    /* 도전 인증 전 버튼 (MEMBER) */
    .btn-not-verified {
        background-color: #17a2b8;
    }

    .btn-not-verified:hover {
        background-color: #138496;
        transform: translateX(-50%) translateY(-2px);
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
        top: 15px;
        right: 15px;
        background: none;
        border: none;
        font-size: 24px;
        cursor: pointer;
        width: 30px;
        height: 30px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        transition: background-color 0.2s ease;
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
        font-family: 'KoddiUD', sans-serif;
    }

    .modal-description {
        font-size: 16px;
        line-height: 1.6;
        margin-bottom: 20px;
        color: var(--dark-gray);
        font-family: 'KoddiUD', sans-serif;
    }

    .modal-place {
        font-size: 16px;
        color: var(--dark-gray);
        margin-bottom: 24px;
        padding: 12px;
        background: var(--neutral-gray);
        border-radius: 12px;
        font-family: 'KoddiUD', sans-serif;
    }

    .modal-image {
        margin: 20px 0;
        border-radius: 12px;
        overflow: hidden;
        max-width: 100%;
        max-height: 300px;
    }

    .modal-image img {
        width: 100%;
        height: auto;
        object-fit: cover;
    }

    /* 대형 도전 인증하기 버튼 */
    .large-verify-btn {
        background-color: var(--primary-blue);
        color: white;
        padding: 40px 60px;
        font-size: 24px;
        font-weight: 700;
        border-radius: 16px;
        border: none;
        cursor: pointer;
        transition: all 0.2s ease;
        font-family: 'KoddiUD', sans-serif;
        width: 100%;
        margin: 20px 0;
    }

    .large-verify-btn:hover {
        background-color: #2b6ce5;
        transform: translateY(-2px);
    }

    /* 모달 내 액션 버튼들 컨테이너 */
    .modal-action-buttons {
        margin-top: 24px;
        display: flex;
        flex-direction: column;
        gap: 16px;
        align-items: center;
    }

    .modal-button {
        background-color: var(--primary-blue);
        color: white;
        padding: 14px 28px;
        font-size: 16px;
        font-weight: 600;
        border-radius: 12px;
        border: none;
        cursor: pointer;
        transition: all 0.2s ease;
        font-family: 'KoddiUD', sans-serif;
    }

    .modal-button:hover {
        background-color: #2b6ce5;
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
        font-family: 'KoddiUD', sans-serif;
    }

    /* 모달 내 수정/삭제 버튼들 */
    .modal-edit-delete-buttons {
        display: flex;
        gap: 12px;
        justify-content: center;
    }

    .modal-edit-btn, .modal-delete-btn, .modal-edit-btn-small {
        padding: 12px 20px;
        border: none;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        color: white;
        transition: all 0.2s ease;
        font-family: 'KoddiUD', sans-serif;
    }

    .modal-edit-btn, .modal-edit-btn-small {
        background-color: var(--primary-blue);
    }

    .modal-edit-btn:hover, .modal-edit-btn-small:hover {
        background-color: #357abd;
    }

    .modal-delete-btn {
        background-color: #e74c3c;
    }

    .modal-delete-btn:hover {
        background-color: #c0392b;
    }

    /* 수정 모달 */
    .edit-modal {
        max-width: 520px;
        text-align: left;
    }

    .edit-modal h1 {
        font-family: 'KoddiUD', sans-serif;
        text-align: center;
        margin-bottom: 24px;
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
        font-family: 'KoddiUD', sans-serif;
    }

    .form-input, .form-textarea {
        width: 100%;
        padding: 14px;
        border: 2px solid var(--border-light);
        border-radius: 12px;
        font-size: 16px;
        transition: border-color 0.3s ease;
        font-family: 'KoddiUD', sans-serif;
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
        font-family: 'KoddiUD', sans-serif;
    }

    .btn-cancel {
        background-color: var(--neutral-gray);
        color: var(--dark-gray);
    }

    .btn-cancel:hover {
        background-color: var(--border-light);
    }

    .btn-save, .delete-confirm-btn, .delete-success-btn {
        background-color: var(--primary-blue);
        color: white;
    }

    .btn-save:hover:not(:disabled), .delete-confirm-btn:hover, .delete-success-btn:hover {
        background-color: #2ba7e5;
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

    .delete-modal h2 {
        font-family: 'KoddiUD', sans-serif;
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

        .message-and-ai-container {
            flex-direction: column;
            gap: 15px;
            align-items: stretch;
        }

        .ai-news-section {
            width: 100%;
        }

        .btn-ai-news {
            width: 100%;
            padding: 14px 20px;
            font-size: 16px;
            height: auto; /* 모바일에서는 자동 높이 */
        }

        .ai-news-guide-popup {
            position: static;
            transform: none;
            margin-top: 10px;
            animation: fadeInUp 0.3s ease-out;
        }

        .ai-news-guide-popup .popup-arrow {
            display: none; /* 모바일에서는 화살표 숨김 */
        }

        @keyframes fadeInUp {
            0% {
                opacity: 0;
                transform: translateY(10px);
            }
            100% {
                opacity: 1;
                transform: translateY(0);
            }
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

        .modal-edit-delete-buttons {
            flex-direction: column;
            gap: 12px;
        }

        .modal-edit-btn, .modal-delete-btn, .modal-edit-btn-small {
            width: 100%;
        }

        .large-verify-btn {
            padding: 30px 40px;
            font-size: 20px;
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
        
        .ai-news-section .btn-ai-news {
            border: 2px solid var(--text-black);
        }
        
        .challenge-placeholder {
            border: 2px solid var(--text-black);
        }

        .ai-news-guide-popup .popup-content {
            border: 2px solid var(--text-black);
        }
    }
</style>