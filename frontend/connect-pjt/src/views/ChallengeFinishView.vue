<template>
    <div class="container">
        <!-- 헤더 -->
        <div class="header">
            <h1>도전을 성공적으로 수행하셨나요?</h1>
            <h3>도전을 인증해주세요</h3>
            <h3>인증을 완료해야 순위에 반영이 됩니다.</h3>
        </div>
        
        <!-- 메인 콘텐츠 -->
        <div class="content">
            <!-- 텍스트 입력 -->
            <div class="section">
                <h4>도전 상세</h4>
                <textarea 
                    v-model="form.description"
                    placeholder="도전을 어떻게 수행하셨나요? &#10;도전을 수행하면서 느꼈던 감정 등 자유롭게 작성해주세요"
                    class="textarea"
                />
            </div>
            
            <!-- 이미지 업로드 (선택사항) -->
            <div class="section">
                <h4>이미지 업로드 (선택사항)</h4>
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
            <button @click="submit" class="btn-submit" :disabled="!isValid">저장</button>
        </div>
        
        <!-- 모달 -->
        <div v-if="showModal" class="modal" @click="closeModal">
            <div class="modal-content" @click.stop>
                <h2>도전 인증이 완료되었습니다.</h2>
                <p>포인트 00점이 부여되었습니다.</p>
                <div class="modal-buttons">
                    <button @click="goToChallenge" class="btn-modal">도전 페이지로</button>
                    <button @click="goToRanking" class="btn-modal">순위 페이지로</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const form = ref({ description: '', image: null })
const previewUrl = ref('')
const fileInput = ref(null)
const showModal = ref(false)

const isValid = computed(() => form.value.description.trim())

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

  const challengeId = router.currentRoute.value.params.challengeId

  // FormData 구성
  const formData = new FormData()
  formData.append('challengeId', challengeId)
  formData.append('imageDescription', form.value.description)
  if (form.value.image) {
    formData.append('challengeImage', form.value.image)
  }

  console.log('axios로 보낼 FormData:', {
    challengeId,
    description: form.value.description,
    image: form.value.image
  })

  try {
    // 실제 API 전송 (백 연결 후 사용)
    /*
    const response = await axios.post('http://localhost:8080/api/challenges/complete', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    console.log('서버 응답:', response.data)
    */

    // 임시 로컬 저장
    const completedChallenge = {
      challengeId: parseInt(challengeId),
      description: form.value.description,
      image: form.value.image ? previewUrl.value : null,
      completedAt: new Date().toISOString(),
      is_success: true
    }

    localStorage.setItem(`challenge_${challengeId}`, JSON.stringify(completedChallenge))
    showModal.value = true
  } catch (error) {
    console.error('axios 오류:', error)
    alert('도전 인증 중 오류가 발생했습니다.')
  }
}

const closeModal = () => showModal.value = false
const goToChallenge = () => {
  showModal.value = false
  router.push('/challenges')
}
const goToRanking = () => {
  showModal.value = false
  console.log('순위 페이지로 이동 (미구현)')
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
.header h1 { margin: 30px 10px 10px; font-size: 28px; font-weight: bold; }
.header h3 { margin: 5px 0; font-weight: normal; color: #666; }

.content { display: flex; gap: 40px; margin-bottom: 40px; }
.section { flex: 1; }
.section h4 { font-size: 18px; font-weight: bold; margin-bottom: 15px; }

.textarea {
    width: 100%; height: 200px; padding: 15px; border: 2px solid #e0e0e0;
    border-radius: 8px; resize: vertical; font-family: inherit;
}
.textarea:focus { outline: none; border-color: #4CAF50; }

.upload-area {
    width: 100%; height: 200px; border: 2px dashed #e0e0e0; border-radius: 8px;
    display: flex; align-items: center; justify-content: center; cursor: pointer;
    position: relative; transition: border-color 0.3s;
}
.upload-area:hover { border-color: #4CAF50; }

.upload-placeholder { text-align: center; }
.upload-icon { font-size: 48px; margin-bottom: 10px; }
.upload-btn {
    background: #4CAF50; color: white; border: none; padding: 8px 16px;
    border-radius: 4px; cursor: pointer; margin-bottom: 8px;
}
.upload-placeholder p { color: #999; font-size: 14px; margin: 0; line-height: 1.4; }

.preview { width: 100%; height: 100%; position: relative; }
.preview img { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
.remove-btn {
    position: absolute; top: 8px; right: 8px; width: 24px; height: 24px;
    border-radius: 50%; background: rgba(0,0,0,0.6); color: white; border: none;
    cursor: pointer; display: flex; align-items: center; justify-content: center;
}

.buttons { display: flex; justify-content: center; gap: 20px; }
.btn-cancel, .btn-submit {
    padding: 12px 30px; border: none; border-radius: 6px; font-size: 16px;
    cursor: pointer; transition: background-color 0.3s;
}
.btn-cancel { background: #f5f5f5; color: #666; }
.btn-cancel:hover { background: #e0e0e0; }
.btn-submit { background: #6c5ce7; color: white; }
.btn-submit:hover:not(:disabled) { background: #5a4bd4; }
.btn-submit:disabled { background: #ccc; cursor: not-allowed; }

.modal {
    position: fixed; top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(0,0,0,0.5); display: flex; align-items: center;
    justify-content: center; z-index: 1000;
}
.modal-content {
    background: white; padding: 40px; border-radius: 12px; text-align: center;
    max-width: 400px; width: 90%; box-shadow: 0 8px 32px rgba(0,0,0,0.3);
}
.modal-content h2 { font-size: 20px; margin-bottom: 10px; }
.modal-content p { color: #666; margin-bottom: 30px; }
.modal-buttons { display: flex; gap: 15px; justify-content: center; }
.btn-modal {
    padding: 12px 24px; border: none; border-radius: 6px; background: #6c5ce7;
    color: white; cursor: pointer; transition: background-color 0.3s;
}
.btn-modal:hover { background: #5a4bd4; }

@media (max-width: 768px) {
    .content { flex-direction: column; gap: 20px; }
    .buttons { flex-direction: column; align-items: center; }
    .btn-cancel, .btn-submit { width: 200px; }
    .modal-buttons { flex-direction: column; gap: 10px; }
    .btn-modal { width: 100%; }
}
</style>