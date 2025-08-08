<template>
  <div class="detail-container">
    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading">
      신문을 불러오고 있습니다...
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="error" class="error">
      {{ error }}
      <button @click="$router.push('/news')" class="back-btn">목록으로 돌아가기</button>
    </div>

    <!-- PDF가 있는 경우 PDF 표시 -->
    <div v-else-if="newsData && newsData.pdfUrl" class="pdf-container">
      <div class="pdf-header">
        <h2>{{ newsData.newsTitle }}</h2>
        <div class="pdf-actions">
          <a :href="newsData.pdfUrl" target="_blank" class="pdf-btn download-btn">
            📄 PDF 다운로드
          </a>
          <button @click="$router.push('/news')" class="pdf-btn back-btn">
            목록으로 돌아가기
          </button>
        </div>
      </div>
      
      <!-- PDF 뷰어 -->
      <!-- <div class="pdf-viewer">
        <iframe 
          :src="newsData.pdfUrl" 
          width="100%" 
          height="800px"
          frameborder="0"
        >
          <p>PDF를 표시할 수 없습니다. <a :href="newsData.pdfUrl" target="_blank">여기를 클릭하여 PDF를 다운로드하세요.</a></p>
        </iframe>
      </div> -->
      <div class="pdf-viewer">
        <iframe 
          v-if="pdfLoadSuccess"
          :src="newsData.pdfUrl" 
          frameborder="0">
        </iframe>
        
        <!-- iframe 외부에 fallback 메시지 -->
        <div v-else>
          <p>PDF를 표시할 수 없습니다. 
            <a :href="newsData.pdfUrl" target="_blank">
              여기를 클릭하여 PDF를 다운로드하세요.
            </a>
          </p>
        </div>
      </div>
    </div>

    <!-- PDF가 없는 경우 신문 템플릿 렌더링 -->
    <div v-else-if="newsData">
      <!-- 발간되지 않은 경우 발간하기 버튼 (관리자만) -->
      <div v-if="!newsData.pdfUrl && userStore.isAdmin" class="generate-section">
        <div class="generate-info">
          <h3>{{ newsData.newsTitle }}</h3>
          <p>이 신문이 아직 발간되지 않았습니다. AI 신문을 발간하시겠습니까?</p>
        </div>
        <button 
          @click="generateAiNews" 
          :disabled="generatingNews"
          class="generate-btn"
        >
          {{ generatingNews ? '🤖 AI 신문 발간중...' : '🤖 AI 신문 발간하기' }}
        </button>
        <button @click="$router.push('/news')" class="back-btn">목록으로 돌아가기</button>
      </div>

      <!-- 발간되지 않았고 멤버인 경우 -->
      <div v-else-if="!newsData.pdfUrl && userStore.isMember" class="no-access">
        <h3>{{ newsData.newsTitle }}</h3>
        <p>이 신문이 아직 발간되지 않았습니다.</p>
        <button @click="$router.push('/news')" class="back-btn">목록으로 돌아가기</button>
      </div>

      <!-- Template 1: 도전과제 1개 -->
      <NewsTemplateOne 
        v-else-if="newsData.challenges.length === 1"
        ref="pdfComponent"
        v-bind="templateOneData"
      />

      <!-- Template 2: 도전과제 2개 -->
      <NewsTemplateTwo 
        v-else-if="newsData.challenges.length === 2"
        ref="pdfComponent"
        v-bind="templateTwoData"
      />

      <!-- Template 3: 도전과제 3개 -->
      <NewsTemplateThree 
        v-else-if="newsData.challenges.length === 3"
        ref="pdfComponent"
        v-bind="templateThreeData"
      />

      <!-- Template 4: 도전과제 4개 -->
      <NewsTemplateFour 
        v-else-if="newsData.challenges.length === 4"
        ref="pdfComponent"
        v-bind="templateFourData"
      />

      <!-- 도전과제가 0개이거나 4개 초과인 경우 -->
      <div v-else class="no-template">
        <p>이번달에 도전과제를 수행하지 않았군요. 다음달엔 도전해보세요!</p>
        <p>성공한 도전과제: {{ newsData.challenges.length }}개</p>
      </div>

      <!-- PDF 관련 버튼들 (관리자만) -->
      <div v-if="newsData.challenges.length >= 1 && newsData.challenges.length <= 4 && userStore.isAdmin" class="save-button">
        <button @click="saveAsPDF" :disabled="savingPdf">
          {{ savingPdf ? '📄 PDF 생성중...' : '📄 PDF로 저장하기' }}
        </button>
        <button @click="generateAndUploadPDF" :disabled="uploadingPdf" class="upload-btn">
          {{ uploadingPdf ? '📤 PDF 업로드중...' : '📤 PDF 업로드하기' }}
        </button>
        <button @click="$router.push('/news')" class="back-btn">목록으로 돌아가기</button>
      </div>

      <!-- 멤버인 경우 목록 버튼만 -->
      <div v-else-if="newsData.challenges.length >= 1 && newsData.challenges.length <= 4 && userStore.isMember" class="member-actions">
        <button @click="$router.push('/news')" class="back-btn">목록으로 돌아가기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import html2pdf from 'html2pdf.js'
import { useUserStore } from '@/stores/user'

// 컴포넌트 임포트
import NewsTemplateOne from '@/components/NewsTemplateOne.vue'
import NewsTemplateTwo from '@/components/NewsTemplateTwo.vue'
import NewsTemplateThree from '@/components/NewsTemplateThree.vue'
import NewsTemplateFour from '@/components/NewsTemplateFour.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const error = ref('')
const newsData = ref(null)
const pdfComponent = ref(null)
const savingPdf = ref(false)
const uploadingPdf = ref(false)
const generatingNews = ref(false)

// 신문 데이터 가져오기
const fetchNewsData = async (aiNewsId) => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await axios.get(`/api/v1/news/${aiNewsId}`)
    newsData.value = response.data
    console.log('신문 데이터 로드 완료:', response.data)
    
  } catch (err) {
    console.error('신문 데이터 로드 실패:', err)
    error.value = '신문을 불러오는데 실패했습니다. 다시 시도해주세요.'
  } finally {
    loading.value = false
  }
}

// AI 신문 발간하기
const generateAiNews = async () => {
  generatingNews.value = true
  
  try {
    const response = await axios.post('/api/v1/admin/ai-news/create')
    console.log('AI 신문 발간 완료:', response.data)
    
    // 성공시 newsData 업데이트
    if (response.data) {
      newsData.value = { ...newsData.value, ...response.data }
    }
    
    alert('AI 신문이 성공적으로 발간되었습니다!')
    
  } catch (error) {
    console.error('AI 신문 발간 실패:', error)
    
    if (error.response) {
      alert(`AI 신문 발간에 실패했습니다: ${error.response.data.message || '서버 오류'}`)
    } else if (error.request) {
      alert('AI 신문 발간에 실패했습니다: 서버와 연결할 수 없습니다.')
    } else {
      alert('AI 신문 발간에 실패했습니다.')
    }
  } finally {
    generatingNews.value = false
  }
}

// Template One 데이터 (도전과제 1개)
const templateOneData = computed(() => {
  if (!newsData.value || newsData.value.challenges.length !== 1) return {}
  
  const challenge = newsData.value.challenges[0]
  return {
    seniorCenterName: newsData.value.seniorCenterName,
    month: String(newsData.value.month).padStart(2, '0'),
    ranking: 1, // 실제로는 랭킹 정보가 있어야 함
    headline: challenge.challengeTitle,
    content: challenge.aiDescription,
    imageUrl: challenge.challengeImage
  }
})

// Template Two 데이터 (도전과제 2개)
const templateTwoData = computed(() => {
  if (!newsData.value || newsData.value.challenges.length !== 2) return {}
  
  const challenges = newsData.value.challenges.slice(0, 2)
  return {
    seniorCenterName: newsData.value.seniorCenterName,
    month: String(newsData.value.month).padStart(2, '0'),
    ranking: 1,
    headline1: challenges[0].challengeTitle,
    content1: challenges[0].aiDescription,
    imageUrl1: challenges[0].challengeImage,
    headline2: challenges[1].challengeTitle,
    content2: challenges[1].aiDescription,
    imageUrl2: challenges[1].challengeImage
  }
})

// Template Three 데이터 (도전과제 3개)
const templateThreeData = computed(() => {
  if (!newsData.value || newsData.value.challenges.length !== 3) return {}
  
  const challenges = newsData.value.challenges.slice(0, 3)
  return {
    seniorCenterName: newsData.value.seniorCenterName,
    month: String(newsData.value.month).padStart(2, '0'),
    ranking: 1,
    headline1: challenges[0].challengeTitle,
    content1: challenges[0].aiDescription,
    imageUrl1: challenges[0].challengeImage,
    headline2: challenges[1].challengeTitle,
    content2: challenges[1].aiDescription,
    imageUrl2: challenges[1].challengeImage,
    headline3: challenges[2].challengeTitle,
    content3: challenges[2].aiDescription,
    imageUrl3: challenges[2].challengeImage
  }
})

// Template Four 데이터 (도전과제 4개)
const templateFourData = computed(() => {
  if (!newsData.value || newsData.value.challenges.length !== 4) return {}
  
  const challenges = newsData.value.challenges.slice(0, 4)
  return {
    seniorCenterName: newsData.value.seniorCenterName,
    month: String(newsData.value.month).padStart(2, '0'),
    ranking: 1,
    headline1: challenges[0].challengeTitle,
    content1: challenges[0].aiDescription,
    imageUrl1: challenges[0].challengeImage,
    headline2: challenges[1].challengeTitle,
    content2: challenges[1].aiDescription,
    imageUrl2: challenges[1].challengeImage,
    headline3: challenges[2].challengeTitle,
    content3: challenges[2].aiDescription,
    imageUrl3: challenges[2].challengeImage,
    headline4: challenges[3].challengeTitle,
    content4: challenges[3].aiDescription,
    imageUrl4: challenges[3].challengeImage
  }
})

// PDF 생성 및 백엔드 업로드 함수
const generateAndUploadPDF = async () => {
  if (!pdfComponent.value) {
    alert("PDF 컴포넌트가 준비되지 않았습니다!")
    return
  }

  uploadingPdf.value = true
  
  try {
    await nextTick()
    const element = pdfComponent.value.pdfTarget

    if (!element) {
      alert("PDF 타겟이 아직 준비되지 않았어요!")
      return
    }

    // PDF 생성 옵션
    const opt = {
      margin: [5, 5, 5, 5],
      filename: `${newsData.value.seniorCenterName}_${newsData.value.year}_${String(newsData.value.month).padStart(2, '0')}_신문.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: {
        scale: 1.5,
        useCORS: true,
        allowTaint: true,
        scrollX: 0,
        scrollY: 0
      },
      jsPDF: {
        unit: 'mm',
        format: 'a4',
        orientation: 'portrait',
        compress: true
      }
    }

    // PDF 생성 (Blob으로)
    const pdfBlob = await html2pdf().set(opt).from(element).outputPdf('blob')
    
    // FormData 생성
    const formData = new FormData()
    const filename = `${newsData.value.seniorCenterName}_${newsData.value.year}_${String(newsData.value.month).padStart(2, '0')}_신문.pdf`
    
    formData.append('file', pdfBlob, filename)
    formData.append('newsId', newsData.value.id)
    formData.append('seniorCenterName', newsData.value.seniorCenterName)
    formData.append('year', newsData.value.year)
    formData.append('month', newsData.value.month)

    // 백엔드로 PDF 업로드
    const uploadResponse = await axios.post('/api/v1/admin/ai-news/upload-pdf', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    console.log('PDF 업로드 성공:', uploadResponse.data)
    
    // 업로드 성공 시 newsData 업데이트 (pdfUrl 반영)
    if (uploadResponse.data.pdfUrl) {
      newsData.value.pdfUrl = uploadResponse.data.pdfUrl
      alert('PDF가 성공적으로 업로드되었습니다!')
    }
    
  } catch (error) {
    console.error('PDF 업로드 실패:', error)
    
    if (error.response) {
      alert(`PDF 업로드에 실패했습니다: ${error.response.data.message || '서버 오류'}`)
    } else if (error.request) {
      alert('PDF 업로드에 실패했습니다: 서버와 연결할 수 없습니다.')
    } else {
      alert('PDF 생성에 실패했습니다.')
    }
  } finally {
    uploadingPdf.value = false
  }
}

// PDF 저장 기능
const saveAsPDF = async () => {
  if (!pdfComponent.value) {
    alert("PDF 컴포넌트가 준비되지 않았습니다!")
    return
  }

  savingPdf.value = true
  
  try {
    await nextTick()
    const element = pdfComponent.value.pdfTarget

    if (!element) {
      alert("PDF 타겟이 아직 준비되지 않았어요!")
      return
    }

    const opt = {
      margin: [5, 5, 5, 5],
      filename: `${newsData.value.seniorCenterName}_${newsData.value.year}_${String(newsData.value.month).padStart(2, '0')}_신문.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: {
        scale: 1.5,
        useCORS: true,
        allowTaint: true,
        scrollX: 0,
        scrollY: 0
      },
      jsPDF: {
        unit: 'mm',
        format: 'a4',
        orientation: 'portrait',
        compress: true
      }
    }

    // PDF 생성하고 Blob으로 변환
    const pdfBlob = await html2pdf().set(opt).from(element).outputPdf('blob')
    
    // Blob을 URL로 변환
    const pdfUrl = URL.createObjectURL(pdfBlob)
    
    // 백엔드에 PDF URL 저장 요청
    const response = await axios.post(`/api/v1/admin/ai-news/${newsData.value.id}/save-pdf`, {
      newsId: newsData.value.id,
      pdfUrl: pdfUrl
    })
    
    console.log('PDF 저장 성공:', response.data)
    
    // 성공시 newsData 업데이트
    if (response.data.pdfUrl) {
      newsData.value.pdfUrl = response.data.pdfUrl
    }
    
    alert('PDF가 성공적으로 저장되었습니다!')
    
    // 로컬 다운로드도 실행
    await html2pdf().set(opt).from(element).save()
    
    // 사용이 끝난 URL 해제
    URL.revokeObjectURL(pdfUrl)
    
  } catch (error) {
    console.error('PDF 저장 실패:', error)
    
    if (error.response) {
      alert(`PDF 저장에 실패했습니다: ${error.response.data.message || '서버 오류'}`)
    } else if (error.request) {
      alert('PDF 저장에 실패했습니다: 서버와 연결할 수 없습니다.')
    } else {
      alert('PDF 생성에 실패했습니다.')
    }
  } finally {
    savingPdf.value = false
  }
}

onMounted(async () => {
  // 사용자 권한 확인
  await userStore.fetchUserRole()
  
  const aiNewsId = route.params.id
  if (aiNewsId) {
    fetchNewsData(aiNewsId)
  } else {
    error.value = '잘못된 신문 ID입니다.'
  }
})
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  padding: 20px;
  background-color: #f5f5f5;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  font-size: 18px;
  color: #666;
}

.error {
  text-align: center;
  padding: 60px 20px;
  color: #e74c3c;
  font-size: 16px;
}

.no-template {
  text-align: center;
  padding: 60px 20px;
  color: #666;
  font-size: 16px;
}

.pdf-container {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.pdf-header {
  padding: 20px 30px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.pdf-header h2 {
  margin: 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.pdf-actions {
  display: flex;
  gap: 10px;
}

.pdf-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.download-btn {
  background-color: #3498db;
  color: white;
}

.download-btn:hover {
  background-color: #2980b9;
}

.pdf-viewer {
  padding: 0;
}

.pdf-viewer iframe {
  border: none;
  display: block;
}

.save-button {
  text-align: center;
  margin: 40px 0;
  padding: 20px;
  gap: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.save-button button {
  padding: 12px 24px;
  font-size: 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.save-button button:first-child {
  background-color: #3498db;
  color: white;
}

.save-button button:first-child:hover:not(:disabled) {
  background-color: #2980b9;
}

.save-button button:first-child:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

.back-btn {
  background-color: #95a5a6 !important;
  color: white !important;
}

.back-btn:hover {
  background-color: #7f8c8d !important;
}

.upload-btn {
  background-color: #27ae60 !important;
  color: white !important;
}

.upload-btn:hover:not(:disabled) {
  background-color: #229954 !important;
}

.upload-btn:disabled {
  background-color: #bdc3c7 !important;
  cursor: not-allowed;
}

.generate-section {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin: 20px auto;
  max-width: 600px;
}

.generate-info h3 {
  color: #333;
  margin-bottom: 15px;
  font-size: 20px;
}

.generate-info p {
  color: #666;
  margin-bottom: 30px;
  line-height: 1.6;
}

.generate-btn {
  background-color: #10b981 !important;
  color: white !important;
  padding: 15px 30px !important;
  font-size: 16px !important;
  border: none !important;
  border-radius: 8px !important;
  cursor: pointer !important;
  margin-right: 15px !important;
  transition: all 0.2s !important;
}

.generate-btn:hover:not(:disabled) {
  background-color: #059669 !important;
  transform: translateY(-1px);
}

.generate-btn:disabled {
  background-color: #9ca3af !important;
  cursor: not-allowed !important;
  transform: none !important;
}

.no-access {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin: 20px auto;
  max-width: 600px;
}

.no-access h3 {
  color: #333;
  margin-bottom: 15px;
  font-size: 20px;
}

.no-access p {
  color: #666;
  margin-bottom: 30px;
  line-height: 1.6;
}

.member-actions {
  text-align: center;
  margin: 40px 0;
  padding: 20px;
}
</style>