<template>
    <div class="container">
        <!-- 헤더 -->
        <div class="header">
            <h1>{{ isEditMode ? '도전 인증을 수정해주세요' : '도전을 성공적으로 수행하셨나요?' }}</h1>
            <h2>{{ isEditMode ? '기존 내용을 수정할 수 있습니다' : '도전을 인증해주세요' }}</h2>
            <h2 v-if="!isEditMode">인증을 완료해야 순위에 반영이 됩니다.</h2>
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
                <h3>이미지 업로드 <span class="required">*</span></h3>
                <div class="upload-area" @click="triggerFileInput">
                    <div v-if="!form.image && !previewUrl" class="upload-placeholder">
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
            <button @click="submit" class="btn-submit" :disabled="!isValid">
                {{ isEditMode ? '수정 완료' : '저장' }}
            </button>
        </div>
        
        <!-- 모달 -->
        <div v-if="showModal" class="modal" @click="closeModal">
            <div class="modal-content" @click.stop>
                <h2>{{ isEditMode ? '도전 인증이 수정되었습니다.' : '도전 인증이 완료되었습니다.' }}</h2>
                <p v-if="!isEditMode">포인트 {{ awardedPoints }}점이 부여되었습니다.</p>
                <p v-else>변경사항이 저장되었습니다.</p>
                <div class="modal-buttons">
                    <button @click="goToChallenge" class="btn-modal">도전 페이지로</button>
                    <button v-if="!isEditMode" @click="goToRanking" class="btn-modal">순위 페이지로</button>
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
const awardedPoints = ref(0)

// 수정 모드 확인
const isEditMode = computed(() => route.query.edit === 'true')

const isValid = computed(() => {
  const hasDescription = form.value.description.trim()
  const hasImage = form.value.image || previewUrl.value
  return hasDescription && hasImage
})

// URL 파라미터에서 challengeId 가져오기
const challengeId = ref(null)

onMounted(() => {
  challengeId.value = route.params.challengeId
  
  // 수정 모드일 때 기존 데이터 로드
  if (isEditMode.value) {
    loadExistingData()
  }
})

// 기존 데이터 로드 함수
const loadExistingData = () => {
  const existingData = localStorage.getItem(`challenge_${challengeId.value}`)
  if (existingData) {
    const data = JSON.parse(existingData)
    form.value.description = data.description || ''
    if (data.image) {
      previewUrl.value = data.image
      // 기존 이미지가 있음을 표시 (실제 File 객체가 아닌 URL이므로)
      form.value.image = 'existing'
    }
  }
}

// 도전 타입별 포인트 계산 함수
const calculatePoints = (challengeId) => {
  const id = parseInt(challengeId)
  
  // 제공하는 도전은 500점, 자체 생성 도전은 300점 
  if (id === 1 || id === 2) {
    return 500
  } else if (id === 3 || id === 4) {
    return 300
  }
}

const triggerFileInput = () => fileInput.value?.click()

//이미지 업로드 POST------------------------
const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (file) {
    form.value.image = file
    const reader = new FileReader()
    reader.onload = (e) => previewUrl.value = e.target.result
    reader.readAsDataURL(file)

    // === 백엔드 연동 시 이미지 업로드 POST 요청 ===
    /*
    const formData = new FormData()
    formData.append('challengeImage', file)

    try {
      const response = await axios.post(`/api/v1/admin/challenges/${challengeId.value}/image`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      console.log('이미지 업로드 응답:', response.data)
    } catch (error) {
      console.error('이미지 업로드 실패:', error)
      alert('이미지 업로드 중 오류가 발생했습니다.')
    }
    */
  }
}
//---------------------------------

//이미지 제거(DELETE)----------------------------
const removeImage = async () => {
  form.value.image = null
  previewUrl.value = ''
  if (fileInput.value) fileInput.value.value = ''

  // === 백엔드 연동 시 이미지 삭제 DELETE 요청 ===
  /*
  try {
    const response = await axios.delete(`/api/v1/admin/challenges/${challengeId.value}/image`)
    console.log('이미지 삭제 응답:', response.data)
  } catch (error) {
    console.error('이미지 삭제 실패:', error)
    alert('이미지 삭제 중 오류가 발생했습니다.')
  }
  */
}
//----------------------------------------------


const cancel = () => router.go(-1)

const submit = async () => {
  if (!form.value.description.trim()) {
    alert('도전 상세 내용을 입력해주세요.')
    return
  }
  
  if (!form.value.image && !previewUrl.value) {
    alert('이미지를 업로드해주세요.')
    return
  }

  // 수정 모드가 아닐 때만 포인트 계산
  if (!isEditMode.value) {
    const points = calculatePoints(challengeId.value)
    awardedPoints.value = points
  }

  // FormData 구성 (백엔드 연결 시 사용)
  const formData = new FormData()
  formData.append('challengeId', challengeId.value)
  formData.append('imageDescription', form.value.description)
  if (form.value.image && typeof form.value.image !== 'string') {
    // 새로운 이미지 파일이 있을 때만 추가
    formData.append('challengeImage', form.value.image)
  }

  console.log('axios로 보낼 FormData:', {
    challengeId: challengeId.value,
    description: form.value.description,
    image: form.value.image,
    isEditMode: isEditMode.value,
    points: isEditMode.value ? undefined : awardedPoints.value
  })

//도전 완료 처리 시(PUT)------------------------------
  try {
  // === 백엔드 연동 시 도전 완료 처리 ===
  /*
  if (!isEditMode.value) {
    const response = await axios.put(`/api/v1/admin/challenges/${challengeId.value}/complete`, {
      imageDescription: form.value.description,
      isSuccess: true
    })

    console.log('도전 완료 응답:', response.data)
    awardedPoints.value = response.data.earnedPoint
  }
  */

  // 임시 로컬 저장
  const existingData = isEditMode.value 
    ? JSON.parse(localStorage.getItem(`challenge_${challengeId.value}`) || '{}')
    : {}
//----------------------------------------------------
    
    const challengeData = {
      ...existingData,
      challengeId: parseInt(challengeId.value),
      description: form.value.description,
      completedAt: existingData.completedAt || new Date().toISOString(),
      is_success: true,
      // 수정 모드일 때는 기존 포인트 유지, 새로 생성할 때만 포인트 부여
      points: existingData.points || (isEditMode.value ? 0 : awardedPoints.value)
    }

    // 이미지 처리
    if (form.value.image) {
      if (typeof form.value.image === 'string') {
        // 기존 이미지 유지
        challengeData.image = existingData.image
      } else {
        // 새로운 이미지
        challengeData.image = previewUrl.value
      }
    } else {
      // 이미지 제거
      challengeData.image = null
    }

    localStorage.setItem(`challenge_${challengeId.value}`, JSON.stringify(challengeData))
    showModal.value = true
  } catch (error) {
    console.error('axios 오류:', error)
    alert(isEditMode.value ? '도전 인증 수정 중 오류가 발생했습니다.' : '도전 인증 중 오류가 발생했습니다.')
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
.header h1 { margin: 30px 10px 10px; font-size: 32px; font-weight: bold; }
.header h2 { margin: 5px 0; font-weight: normal; color: #666; }

.content { display: flex; gap: 40px; margin-bottom: 40px; }
.section { flex: 1; }
.section h3 { font-size: 20px; font-weight: bold; margin-bottom: 15px; }

.required { color: #dc3545; font-weight: bold; }

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