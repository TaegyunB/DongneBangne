<template>
    <div class="container">
        <!-- 헤더 -->
        <div class="header">
            <h1>도전을 성공적으로 수행하셨나요?</h1>
            <h2>도전을 인증해주세요</h2>
            <h2>인증을 완료해야 순위에 반영이 됩니다.</h2>
        </div>
        
        <!-- 메인 콘텐츠 -->
        <div class="content">
            <!-- 텍스트 입력 -->
            <div class="section">
                <h3>도전 상세</h3>
                <textarea 
                    v-model="form.description"
                    placeholder="도전을 어떻게 수행하셨나요? &#10;도전을 수행하면서 느꼈던 감정 등을 자유롭게 작성해주세요"
                    class="textarea"
                />
            </div>
            
            <!-- 이미지 업로드 (필수) -->
            <div class="section">
                <h3>이미지 업로드 <span class="required">*필수</span></h3>
                <div class="upload-area" @click="triggerFileInput">
                    <div v-if="!form.image" class="upload-placeholder">
                        <div class="upload-icon">📁</div>
                        <button type="button" class="upload-btn">파일 선택</button>
                        <p>도전 인증을 위한 이미지를<br>업로드해주세요</p>
                    </div>
                    <div v-else class="preview">
                        <img :src="previewUrl" alt="preview" />
                        <button @click.stop="removeImage" class="remove-btn">×</button>
                    </div>
                </div>
                <input ref="fileInput" type="file" accept="image/*" @change="handleFileUpload" hidden>
            </div>
        </div>
        
        <!-- 버튼 -->
        <div class="buttons">
            <button @click="cancel" class="btn-cancel">취소</button>
            <button @click="submit" class="btn-submit" :disabled="!isValid || loading">
                {{ loading ? '업로드 중...' : '저장' }}
            </button>
        </div>
        
        <!-- 확인 모달 -->
        <div v-if="showConfirmModal" class="modal" @click="closeConfirmModal">
            <div class="modal-content" @click.stop>
                <h2>도전 인증 내용을 확인해주세요</h2>
                <div class="confirm-content">
                    <div class="form-group">
                        <label>도전 상세:</label>
                        <p class="content-text">{{ form.description }}</p>
                    </div>
                    <div class="form-group">
                        <label>업로드 이미지:</label>
                        <div class="confirm-image">
                            <img :src="previewUrl" alt="확인 이미지" />
                        </div>
                    </div>
                </div>
                <div class="modal-buttons">
                    <button @click="closeConfirmModal" class="btn-modal-cancel">수정하기</button>
                    <button @click="confirmSubmit" class="btn-modal-confirm" :disabled="confirming">
                        {{ confirming ? '제출 중...' : '확인' }}
                    </button>
                </div>
            </div>
        </div>
        
        <!-- 성공 모달 -->
        <div v-if="showSuccessModal" class="modal" @click="closeSuccessModal">
            <div class="modal-content" @click.stop>
                <h2>도전 인증이 <br> 완료되었습니다!</h2>
                <p>도전이 성공적으로 인증되어 완료 상태로 변경되었습니다.</p>
                <div class="modal-buttons">
                    <button @click="goToChallenge" class="btn-modal">도전 페이지로</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const form = ref({ description: '', image: null })
const previewUrl = ref('')
const fileInput = ref(null)
const showConfirmModal = ref(false)
const showSuccessModal = ref(false)
const loading = ref(false)
const confirming = ref(false)

// 이미지 업로드도 필수로 변경
const isValid = computed(() => form.value.description.trim() && form.value.image)

const challengeId = ref(null)
const challengeType = ref('system') // 'system' 또는 'admin'

onMounted(() => {
  challengeId.value = route.params.challengeId
  
  // challengeId로 도전 타입 구분
  // 우리가 제공하는 도전인지 ADMIN이 생성한 도전인지 확인
  const adminChallenges = JSON.parse(localStorage.getItem('adminChallenges') || '[]')
  const isAdminChallenge = adminChallenges.some(challenge => 
    challenge.challengeId && challenge.challengeId.toString() === challengeId.value
  )
  
  challengeType.value = isAdminChallenge ? 'admin' : 'system'
  console.log('도전 타입:', challengeType.value, 'challengeId:', challengeId.value)
})

const triggerFileInput = () => fileInput.value?.click()

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    form.value.image = file
    const reader = new FileReader()
    reader.onload = (e) => previewUrl.value = e.target.result
    reader.readAsDataURL(file)
  }
}

const removeImage = () => {
  form.value.image = null
  previewUrl.value = ''
  if (fileInput.value) fileInput.value.value = ''
}

const cancel = () => router.go(-1)

const submit = () => {
  if (!form.value.description.trim()) {
    alert('도전 상세 내용을 입력해주세요.')
    return
  }
  
  if (!form.value.image) {
    alert('도전 인증을 위한 이미지를 업로드해주세요.')
    return
  }
  
  showConfirmModal.value = true
}

const closeConfirmModal = () => {
  showConfirmModal.value = false
}

const token = localStorage.getItem('accessToken');

const confirmSubmit = async () => {
  confirming.value = true

  try {
    // ==========  파일 유효성 검사 추가  ==========
    console.log('=== 파일 유효성 검사 ===')
    
    if (!form.value.image) {
      alert('이미지를 선택해주세요.')
      return
    }
    
    // 파일 크기 검사 (10MB = 10 * 1024 * 1024 bytes)
    const maxSize = 10 * 1024 * 1024
    if (form.value.image.size > maxSize) {
      alert(`파일 크기가 너무 큽니다. 최대 ${Math.round(maxSize / 1024 / 1024)}MB까지 업로드 가능합니다.`)
      return
    }
    
    // 파일 타입 검사
    if (!form.value.image.type.startsWith('image/')) {
      alert('이미지 파일만 업로드 가능합니다.')
      return
    }
    
    console.log('파일 정보:')
    console.log('- 이름:', form.value.image.name)
    console.log('- 크기:', `${(form.value.image.size / 1024 / 1024).toFixed(2)}MB`)
    console.log('- 타입:', form.value.image.type)
    console.log('- 설명 길이:', form.value.description.length)
    
    // ==========  FormData 생성  ==========
    const formData = new FormData()
    formData.append('imageFile', form.value.image)
    formData.append('imageDescription', form.value.description)
    
    // FormData 내용 확인
    console.log('FormData entries:')
    for (let [key, value] of formData.entries()) {
      if (value instanceof File) {
        console.log(`${key}: File(${value.name}, ${value.size} bytes)`)
      } else {
        console.log(`${key}: ${value}`)
      }
    }

    console.log('🚀 missionFinishUpdate API 호출 시작...')
    
    // ==========  더 짧은 타임아웃으로 테스트  ==========
    const response = await axios.post(
      `/api/v1/admin/challenges/${challengeId.value}/missionFinishUpdate`, 
      formData,
      {
        withCredentials: true,
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        timeout: 60000, // 60초로 증가
        maxContentLength: Infinity,
        maxBodyLength: Infinity,
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          console.log(`업로드 진행률: ${percentCompleted}%`)
          
          // 진행률이 100%가 되면 서버 처리 중임을 표시
          if (percentCompleted === 100) {
            console.log('⏳ 파일 업로드 완료, 서버 처리 중...')
          }
        }
      }
    )

    console.log('✅ missionFinishUpdate 성공:', response.data)

    // 두 번째 API 호출
    console.log('🚀 complete API 호출 시작...')
    
    const completeResponse = await axios.post(
      `/api/v1/admin/challenges/${challengeId.value}/complete`,
      {},
      {
        withCredentials: true,
        headers: {
          'Content-Type': 'application/json'
        },
        timeout: 30000
      }
    )

    console.log('✅ complete API 성공:', completeResponse.data)

    // 성공 처리
    const completedChallenge = {
      challengeId: parseInt(challengeId.value),
      description: form.value.description,
      image: previewUrl.value,
      completedAt: new Date().toISOString(),
      is_success: true,
      is_uploaded: true,
      serverData: response.data,
      completeData: completeResponse.data
    }

    if (challengeType.value === 'admin') {
      localStorage.setItem(`admin_challenge_${challengeId.value}`, JSON.stringify(completedChallenge))
    } else {
      localStorage.setItem(`challenge_${challengeId.value}`, JSON.stringify(completedChallenge))
    }
    
    showConfirmModal.value = false
    showSuccessModal.value = true

  } catch (error) {
    console.error('❌ API 호출 에러:', error)
    
    if (error.code === 'ERR_NETWORK') {
      console.log('🔍 Network Error 상세 분석:')
      console.log('- readyState:', error.request?.readyState)
      console.log('- status:', error.request?.status)
      console.log('- responseText:', error.request?.responseText)
      
      // 백엔드 로그 확인 요청
      alert(`네트워크 오류가 발생했습니다. 

가능한 원인:
1. 서버에서 파일 처리 중 오류 발생
2. S3 업로드 실패  
3. 데이터베이스 오류

백엔드 서버 로그를 확인해주세요.`)
      
    } else if (error.code === 'ECONNABORTED') {
      alert('요청 시간이 초과되었습니다. 파일 크기를 줄이거나 나중에 다시 시도해주세요.')
    } else if (error.response) {
      const status = error.response.status
      const errorMessage = error.response.data?.message || `서버 오류 (${status})`
      alert(`서버 오류: ${errorMessage}`)
    } else {
      alert('알 수 없는 오류가 발생했습니다.')
    }
    
  } finally {
    confirming.value = false
  }
}

// ==========  간단한 테스트 함수  ==========
const testSmallFile = async () => {
  try {
    // 매우 작은 더미 파일로 테스트
    const dummyFile = new File(['test'], 'test.txt', { type: 'text/plain' })
    const testFormData = new FormData()
    testFormData.append('imageFile', dummyFile)
    testFormData.append('imageDescription', 'test description')
    
    console.log('🧪 작은 파일로 테스트...')
    
    const response = await axios.post(
      `/api/v1/admin/challenges/${challengeId.value}/missionFinishUpdate`,
      testFormData,
      {
        withCredentials: true,
        timeout: 10000
      }
    )
    
    console.log('✅ 작은 파일 테스트 성공:', response.data)
    
  } catch (error) {
    console.error('❌ 작은 파일 테스트 실패:', error)
  }
}

const closeSuccessModal = () => showSuccessModal.value = false

const goToChallenge = () => {
  showSuccessModal.value = false
  router.push('/challenges')
}
</script>

<style scoped>
.container { 
    max-width: 1200px; 
    margin: 0 auto; 
    padding: 20px; 
}
.header { 
    text-align: center; 
    margin-bottom: 40px; 
}
.header h1 { margin: 30px 10px 10px; font-size: 32px; font-weight: bold; }
.header h2 { margin: 5px 0; font-weight: normal; color: #666; }

.content { display: flex; gap: 40px; margin-bottom: 40px; }
.section { flex: 1; }
.section h3 { font-size: 20px; font-weight: bold; margin-bottom: 15px; }

.required {
    color: #FF4444;
    font-size: 16px;
    font-weight: normal;
    margin-left: 8px;
}

.textarea {
    width: 100%; height: 200px; padding: 15px; border: 2px solid #e0e0e0;
    border-radius: 8px; resize: vertical; font-family: inherit;font-size: 18px;
}
.textarea:focus { outline: none; border-color: #3074FF; }

.upload-area {
    width: 100%; height: 200px; border: 2px dashed #e0e0e0; border-radius: 8px;
    display: flex; align-items: center; justify-content: center; cursor: pointer;
    position: relative; transition: border-color 0.3s;
}
.upload-area:hover { border-color: #6c9dff; }

.upload-placeholder { text-align: center; }
.upload-icon { font-size: 48px; margin-bottom: 10px; }
.upload-btn {
    background: #3074FF; color: white; border: none; padding: 8px 16px;
    border-radius: 4px; cursor: pointer; margin-bottom: 8px;font-size: 18px;font-weight: bold;
}
.upload-placeholder p { color: #999; font-size: 18px; margin: 0; line-height: 1.4; }

.preview { width: 100%; height: 100%; position: relative; }
.preview img { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
.remove-btn {
    position: absolute; top: 8px; right: 8px; width: 24px; height: 24px;
    border-radius: 50%; background: rgba(0,0,0,0.6); color: white; border: none;
    cursor: pointer; display: flex; align-items: center; justify-content: center;
}

.buttons { display: flex; justify-content: center; gap: 20px; }
.btn-cancel, .btn-submit {
    padding: 12px 30px; border: none; border-radius: 6px; font-size: 20px;
    cursor: pointer; transition: background-color 0.3s;font-weight: bold;
}
.btn-cancel { background: #f5f5f5; color: #666; }
.btn-cancel:hover { background: #e0e0e0; }
.btn-submit { background: #3074FF; color: white; }
.btn-submit:hover:not(:disabled) { background: #6c9dff; }
.btn-submit:disabled { background: #ccc; cursor: not-allowed; }

.modal {
    position: fixed; top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(0,0,0,0.5); display: flex; align-items: center;
    justify-content: center; z-index: 1000;
}
.modal-content {
    background: white; padding: 40px; border-radius: 12px; text-align: center;
    max-width: 600px; width: 90%; box-shadow: 0 8px 32px rgba(0,0,0,0.3);
}
.modal-content h2 { font-size: 28px; margin-bottom: 20px; }
.modal-content p { color: #666; margin-bottom: 30px; font-size: 18px; }

.confirm-content { text-align: left; margin: 30px 0; }
.form-group { margin-bottom: 20px; }
.form-group label { 
    display: block; margin-bottom: 8px; font-weight: bold; 
    color: #333; font-size: 18px; 
}
.content-text { 
    background: #f8f9fa; padding: 15px; border-radius: 8px; 
    font-size: 16px; line-height: 1.5; border: 1px solid #e9ecef; 
}
.confirm-image { 
    width: 200px; height: 150px; border-radius: 8px; 
    overflow: hidden; border: 1px solid #e9ecef; 
}
.confirm-image img { width: 100%; height: 100%; object-fit: cover; }

.modal-buttons { display: flex; gap: 15px; justify-content: center; }
.btn-modal, .btn-modal-cancel, .btn-modal-confirm {
    padding: 12px 24px; border: none; border-radius: 6px; 
    cursor: pointer; transition: background-color 0.3s;font-size: 18px;font-weight: bold;
}
.btn-modal, .btn-modal-confirm { background: #3074FF; color: white; }
.btn-modal:hover, .btn-modal-confirm:hover:not(:disabled) { background: #6c9dff; }
.btn-modal-cancel { background: #f5f5f5; color: #666; }
.btn-modal-cancel:hover { background: #e0e0e0; }
.btn-modal-confirm:disabled { background: #ccc; cursor: not-allowed; }

@media (max-width: 768px) {
    .content { flex-direction: column; gap: 20px; }
    .buttons { flex-direction: column; align-items: center; }
    .btn-cancel, .btn-submit { width: 200px; }
    .modal-buttons { flex-direction: column; gap: 10px; }
    .btn-modal, .btn-modal-cancel, .btn-modal-confirm { width: 100%; }
}
</style>