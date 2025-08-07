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
            
            <!-- 이미지 업로드 (선택사항) -->
            <div class="section">
                <h3>이미지 업로드 (선택사항)</h3>
                <div class="upload-area" @click="triggerFileInput">
                    <div v-if="!form.image" class="upload-placeholder">
                        <div class="upload-icon">📁</div>
                        <button type="button" class="upload-btn">파일 선택</button>
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
        
        <!-- 모달 -->
        <div v-if="showModal" class="modal" @click="closeModal">
            <div class="modal-content" @click.stop>
                <h2>도전 인증 정보가 <br> 업로드되었습니다.</h2>
                <p>관리자의 승인을 기다려주세요.</p>
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
const showModal = ref(false)
const loading = ref(false)

const isValid = computed(() => form.value.description.trim())

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

const submit = async () => {
  if (!isValid.value) {
    alert('도전 상세 내용을 입력해주세요.')
    return
  }

  loading.value = true

  try {
    const formData = new FormData()
    formData.append('imageDescription', form.value.description)
    if (form.value.image) {
      formData.append('imageFile', form.value.image)
    }

    console.log('missionFinishUpdate API 호출:', {
      challengeId: challengeId.value,
      description: form.value.description,
      image: form.value.image,
      challengeType: challengeType.value
    })

    const response = await axios.post(
      `/api/v1/admin/challenges/${challengeId.value}/missionFinishUpdate`, 
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    )

    console.log('서버 응답:', response.data)

    const uploadedChallenge = {
      challengeId: parseInt(challengeId.value),
      description: form.value.description,
      image: form.value.image ? previewUrl.value : null,
      uploadedAt: new Date().toISOString(),
      is_success: false,
      is_uploaded: true,
      serverData: response.data
    }

    // 도전 타입에 따라 다른 localStorage 키 사용
    if (challengeType.value === 'admin') {
      // ADMIN이 생성한 도전과제
      localStorage.setItem(`admin_challenge_${challengeId.value}`, JSON.stringify(uploadedChallenge))
    } else {
      // 우리가 제공하는 도전과제 (기존 방식 유지)
      localStorage.setItem(`challenge_${challengeId.value}`, JSON.stringify(uploadedChallenge))
    }
    
    showModal.value = true
  } catch (error) {
    console.error('업로드 오류:', error)
    alert('도전 인증 업로드 중 오류가 발생했습니다.')
  } finally {
    loading.value = false
  }
}

const closeModal = () => showModal.value = false

const goToChallenge = () => {
  showModal.value = false
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
    max-width: 500px; width: 90%; box-shadow: 0 8px 32px rgba(0,0,0,0.3);
}
.modal-content h2 { font-size: 32px; margin-bottom: 10px; }
.modal-content p { color: #666; margin-bottom: 30px; font-size: 20px; }
.modal-buttons { display: flex; gap: 15px; justify-content: center; }
.btn-modal {
    padding: 12px 24px; border: none; border-radius: 6px; background: #3074FF;
    color: white; cursor: pointer; transition: background-color 0.3s;font-size: 20px;font-weight: bold;
}
.btn-modal:hover { background: #6c9dff; }

@media (max-width: 768px) {
    .content { flex-direction: column; gap: 20px; }
    .buttons { flex-direction: column; align-items: center; }
    .btn-cancel, .btn-submit { width: 200px; }
    .modal-buttons { flex-direction: column; gap: 10px; }
    .btn-modal { width: 100%; }
}
</style>