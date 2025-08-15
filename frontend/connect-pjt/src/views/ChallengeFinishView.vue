<template>
    <div class="container">
        <!-- 헤더 -->
        <div class="header">
            <h1>도전을 성공적으로 수행하셨나요?</h1>
            <h2>도전을 인증해주세요</h2>
        </div>
        
        <!-- 메인 콘텐츠 -->
        <div class="content">
            <!-- 텍스트 입력 -->
            <div class="section">
                <h3>도전 상세</h3>
                <textarea 
                    v-model="form.description"
                    placeholder="도전을 수행하면서 느꼈던 감정 등을 자유롭게 작성해주세요"
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
                        <p>도전 인증을 위한 이미지를<br>올려주세요</p>
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
                <div class="warning-message">
                    <p>⚠️ 도전을 인증하면 수정이 불가합니다</p>
                </div>
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
                    <button @click="closeConfirmModal" class="btn-modal-cancel">취소</button>
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
        
        <!-- 알림 모달 -->
        <div v-if="showAlertModal" class="modal-overlay" @click.self="closeAlertModal">
          <div class="alert-modal" @click.stop>
            <button class="modal-close-btn" @click="closeAlertModal">×</button>
            <h2>{{ alertTitle }}</h2>
            <p class="modal-description">{{ alertMessage }}</p>
            <div class="modal-action-buttons">
              <button class="modal-button" @click="closeAlertModal">확인</button>
            </div>
          </div>
</div>
    </div>
    
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/api/axios' // 기존 API 클라이언트 import

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

onMounted(() => {
  challengeId.value = route.params.challengeId
  console.log('challengeId:', challengeId.value)
})

const triggerFileInput = () => fileInput.value?.click()

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 파일 크기 검사 (10MB 제한)
    const maxSize = 10 * 1024 * 1024
    if (file.size > maxSize) {
      showAlert(`파일 크기가 너무 큽니다. 최대 ${Math.round(maxSize / 1024 / 1024)}MB까지 업로드 가능합니다.`)
      return
    }
    
    // 파일 타입 검사
    if (!file.type.startsWith('image/')) {
      showAlert('이미지 파일만 업로드 가능합니다.')
      return
    }
    
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
    showAlert('도전 상세 내용을 입력해주세요.')
    return
  }
  
  if (!form.value.image) {
    showAlert('도전 인증을 위한 이미지를 업로드해주세요.')
    return
  }
  
  showConfirmModal.value = true
}

const closeConfirmModal = () => {
  showConfirmModal.value = false
}

const confirmSubmit = async () => {
  confirming.value = true

  try {
    console.log('=== 도전 인증 시작 ===')
    console.log('challengeId:', challengeId.value)
    console.log('파일 정보:', {
      name: form.value.image.name,
      size: `${(form.value.image.size / 1024 / 1024).toFixed(2)}MB`,
      type: form.value.image.type
    })
    
    // FormData 생성
    const formData = new FormData()
    formData.append('imageFile', form.value.image)
    formData.append('imageDescription', form.value.description)
    
    console.log('🚀 missionFinishUpdate API 호출 시작...')
    
    // 첫 번째 API: 이미지 업로드 및 설명 저장
    const response = await api.post(
      `/api/v1/admin/challenges/${challengeId.value}/missionFinishUpdate`, 
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        timeout: 60000, // 60초
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          console.log(`업로드 진행률: ${percentCompleted}%`)
          
          if (percentCompleted === 100) {
            console.log('⏳ 파일 업로드 완료, 서버 처리 중...')
          }
        }
      }
    )

    console.log('✅ missionFinishUpdate 성공:', response.data)

    // 두 번째 API: 도전 완료 처리
    console.log('🚀 complete API 호출 시작...')
    
    const completeResponse = await api.post(
      `/api/v1/admin/challenges/${challengeId.value}/complete`,
      {}
    )

    console.log('✅ complete API 성공:', completeResponse.data)

    // 성공 처리
    showConfirmModal.value = false
    showSuccessModal.value = true

  } catch (error) {
    console.error('❌ API 호출 에러:', error)
    
    let errorMessage = '알 수 없는 오류가 발생했습니다.'
    
    if (error.code === 'ERR_NETWORK') {
      errorMessage = `네트워크 오류가 발생했습니다.

가능한 원인:
1. 서버에서 파일 처리 중 오류 발생
2. 파일 업로드 실패
3. 서버 연결 문제

잠시 후 다시 시도해주세요.`
      
    } else if (error.code === 'ECONNABORTED') {
      errorMessage = '요청 시간이 초과되었습니다. 파일 크기를 줄이거나 나중에 다시 시도해주세요.'
    } else if (error.response) {
      const status = error.response.status
      const serverMessage = error.response.data?.message || `서버 오류 (${status})`
      
      if (status === 401 || status === 403) {
        errorMessage = '로그인이 필요합니다. 다시 로그인해주세요.'
      } else if (status === 413) {
        errorMessage = '파일 크기가 너무 큽니다. 더 작은 파일을 선택해주세요.'
      } else {
        errorMessage = `서버 오류: ${serverMessage}`
      }
    }
    
    showAlert(errorMessage)
    
  } finally {
    confirming.value = false
  }
}

// 알림 모달 관련
const showAlertModal = ref(false)
const alertTitle = ref('')
const alertMessage = ref('')

const showAlert = (title, message) => {
  alertTitle.value = title
  alertMessage.value = message
  showAlertModal.value = true
}

const closeAlertModal = () => {
  showAlertModal.value = false
  alertTitle.value = ''
  alertMessage.value = ''
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
.header h1 { margin: 30px 10px 10px; font-size: 40px; font-weight: bold; }
.header h2 { margin: 5px 0; font-size: 35px;font-weight: normal; color: #666; }

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

.warning-message {
    background: #fff3cd;
    border: 1px solid #ffeaa7;
    border-radius: 8px;
    padding: 12px 16px;
    margin: 20px 0;
    text-align: center;
}

.warning-message p {
    margin: 0;
    color: #856404;
    font-size: 16px;
    font-weight: bold;
}

/* 일반 알림 모달 스타일 */
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

.alert-modal {
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
  background-color: #f3f4f6;
}

.alert-modal h2 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #333;
}

.modal-description {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
  color: #666;
}

.modal-action-buttons {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.modal-button {
  background-color: #4A90E2;
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
  background-color: #2b6ce5;
  transform: translateY(-1px);
}

@media (max-width: 768px) {
    .content { flex-direction: column; gap: 20px; }
    .buttons { flex-direction: column; align-items: center; }
    .btn-cancel, .btn-submit { width: 200px; }
    .modal-buttons { flex-direction: column; gap: 10px; }
    .btn-modal, .btn-modal-cancel, .btn-modal-confirm { width: 100%; }
}
</style>