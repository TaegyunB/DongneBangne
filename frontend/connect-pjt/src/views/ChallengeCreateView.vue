<template>
  <div class="container">
    <div class="header">
      <h1>경로당 맞춤형 도전을 생성하시겠어요?</h1>
      <h2>몇 가지만 작성해주세요</h2>
    </div>
    
    <div class="form-group">
      <label>도전 제목</label>
      <input
        v-model="title"
        type="text"
        placeholder="간단하게 작성해주세요."
        class="styled-input"
      />
      
      <label>장소</label>
      <input
        v-model="place"
        type="text"
        placeholder="도전은 어디서 수행하나요?"
        class="styled-input"
      />
      
      <label>상세 정보</label>
      <textarea
        v-model="description"
        placeholder="도전에 대해 상세하게 설명해주세요."
        class="styled-textarea"
      />
      <!-- isValid를 통과해야 버튼이 활성화되게 -->
      <button class="submit-button" @click="handleSubmit" :disabled="!isValid || loading">
        {{ loading ? '저장 중...' : '✔ 저장' }}
      </button>
    </div>
    
    <!-- 생성 완료 모달 -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content">
        <h2 class="modal-title">경로당 도전</h2>
        <p class="modal-subtext">도전이 <br> 성공적으로 저장되었습니다.</p>
        <button class="modal-button" @click="goToList">도전 목록으로</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const title = ref('')
const place = ref('')
const description = ref('')
const showModal = ref(false)
const loading = ref(false)
const router = useRouter()

const isValid = computed(() => {
  return title.value.trim() && place.value.trim() && description.value.trim()
})

const handleSubmit = async () => {
  if (!isValid.value) {
    alert('모든 항목을 입력해주세요.')
    return
  }

  loading.value = true

  const challengeData = {
    challengeTitle: title.value.trim(),
    challengePlace: place.value.trim(),
    description: description.value.trim()
  }

  console.log('API로 보낼 데이터:', challengeData)

  try {
    const response = await axios.post('http://localhost:8080/api/v1/admin/challenges/create', challengeData, {
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json'
      }
    })
    console.log('서버 응답:', response.data)

    // localStorage에 ADMIN 도전과제로 저장 (challengeId 포함)
    const adminChallenges = JSON.parse(localStorage.getItem('adminChallenges') || '[]')
    const newChallenge = {
      challengeId: response.data.id,
      challengeTitle: response.data.challengeTitle,
      challengePlace: response.data.challengePlace,
      description: response.data.description,
      isCustom: true,
      createdAt: new Date().toISOString()
    }

    adminChallenges.push(newChallenge)
    localStorage.setItem('adminChallenges', JSON.stringify(adminChallenges))
    
    showModal.value = true
  } catch (error) {
    console.error('도전과제 생성 실패:', error)
    
    if (error.response?.status === 401 || error.response?.status === 403) {
      alert('로그인이 필요합니다. 다시 로그인해주세요.')
    } else {
      alert('도전 생성 중 오류가 발생했습니다.')
    }
  } finally {
    loading.value = false
  }
}

const goToList = () => {
  showModal.value = false
  router.push('/challenges') 
}
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
}

.header {
  text-align: center;
}

.header h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 10px;
}

.header h3 {
  font-size: 20px;
  font-weight: 500;
  color: #555;
  margin-bottom: 30px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group label {
  font-size: 20px;
  font-weight: 600;
  text-align: left;
}
.form-group input::placeholder {
  font-size: 18px;
}

.form-group textarea::placeholder {
  font-size: 18px;
}

.styled-input, .styled-textarea {
  width: 100%;
  padding: 12px 15px;
  font-size: 18px;
  border: 1.5px solid #ccc;
  border-radius: 10px;
  outline: none;
}

.styled-input:focus, .styled-textarea:focus {
  border-color: #3074FF;
}

.styled-input::placeholder, .styled-textarea::placeholder {
  color: #aaa;
  font-size: 14px;
}

.styled-textarea {
  min-height: 120px;
  resize: vertical;
}

.submit-button {
  margin-top: 20px;
  background-color: #3074FF;
  color: white;
  padding: 12px 0;
  border: none;
  border-radius: 10px;
  font-size: 20px;
  font-weight: 600;
  cursor: pointer;
}

.submit-button:hover:not(:disabled) {
  background-color: #6c9dff;
}

/* 다 입력하지 않은 경우 접근하지 못하게 */
.submit-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: white;
  padding: 60px 50px;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  max-width: 400px;
  z-index: 1001;
}

.modal-title {
  font-size: 30px;
  font-weight: bold;
  margin-bottom: 12px;
}

.modal-subtext {
  font-size: 24px;
  color: #333;
  margin-bottom: 24px;
}

.modal-button {
  background-color: #3074FF;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  font-size: 20px;
  cursor: pointer;
}

.modal-button:hover {
  background-color: #6c9dff;
}
</style>