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
      <div class="pdf-viewer">
        <iframe 
          :src="newsData.pdfUrl" 
          width="100%" 
          height="800px"
          frameborder="0"
        >
          <p>PDF를 표시할 수 없습니다. <a :href="newsData.pdfUrl" target="_blank">여기를 클릭하여 PDF를 다운로드하세요.</a></p>
        </iframe>
      </div>
    </div>

    <!-- PDF가 없는 경우 신문 템플릿 렌더링 -->
    <div v-else-if="newsData">
      <!-- 도전과제 개수에 따른 템플릿 선택 -->
      <div v-if="challengeCount >= 1 && challengeCount <= 4">
        <!-- 디버깅 정보 표시 (개발 환경에서만 표시하려면 v-if="isDev" 추가) -->
        <div class="debug-info" style="background: #f0f0f0; padding: 10px; margin: 10px; border-radius: 5px;">
          <p><strong>디버깅 정보:</strong></p>
          <p>도전과제 개수: {{ newsData.challenges?.length || 0 }}</p>
          <p>뉴스 제목: {{ newsData.newsTitle }}</p>
          <p>센터명: {{ newsData.seniorCenterName }}</p>
          <p>Year/Month: {{ newsData.year }}/{{ newsData.month }}</p>
        </div>

        <!-- Template 1: 도전과제 1개 -->
        <NewsTemplateOne 
          v-if="challengeCount === 1"
          ref="pdfComponent"
          v-bind="templateOneData"
        />

        <!-- Template 2: 도전과제 2개 -->
        <NewsTemplateTwo 
          v-else-if="challengeCount === 2"
          ref="pdfComponent"
          v-bind="templateTwoData"
        />

        <!-- Template 3: 도전과제 3개 -->
        <NewsTemplateThree 
          v-else-if="challengeCount === 3"
          ref="pdfComponent"
          v-bind="templateThreeData"
        />

        <!-- Template 4: 도전과제 4개 -->
        <NewsTemplateFour 
          v-else-if="challengeCount === 4"
          ref="pdfComponent"
          v-bind="templateFourData"
        />

        <!-- PDF 관련 버튼들 (관리자만) -->
        <div v-if="userStore.isAdmin" class="save-button">
          <button @click="saveAsPDF" :disabled="savingPdf">
            {{ savingPdf ? '📄 PDF 생성중...' : '📄 PDF로 저장하기' }}
          </button>
          <button @click="generateAndUploadPDF" :disabled="uploadingPdf" class="upload-btn">
            {{ uploadingPdf ? '📤 PDF 업로드중...' : '📤 PDF 업로드하기' }}
          </button>
          <button @click="$router.push('/news')" class="back-btn">목록으로 돌아가기</button>
        </div>

        <!-- 멤버인 경우 목록 버튼만 -->
        <div v-else-if="userStore.isMember" class="member-actions">
          <button @click="$router.push('/news')" class="back-btn">목록으로 돌아가기</button>
        </div>
      </div>

      <!-- 도전과제가 0개이거나 4개 초과인 경우 -->
      <div v-else class="no-template">
        <h3>{{ newsData.newsTitle }}</h3>
        <p>이번달에 도전과제가 {{ challengeCount }}개입니다.</p>
        
        <div v-if="challengeCount === 0" class="no-challenges">
          <p>도전과제가 없어 신문을 생성할 수 없습니다.</p>
          <p>다음달에는 도전과제를 완료해보세요! 💪</p>
        </div>
        
        <div v-else-if="challengeCount > 4" class="too-many-challenges">
          <p>너무 많은 도전과제를 수행하셨네요! 🎉</p>
          <p>현재는 최대 4개까지만 신문에 표시되며, 처음 4개의 도전과제가 선택됩니다.</p>
          
          <!-- 선택된 도전과제와 제외된 도전과제 표시 -->
          <div class="challenge-selection">
            <div class="selected-challenges">
              <h4>신문에 포함될 도전과제 (처음 4개):</h4>
              <ul>
                <li v-for="challenge in getSuccessfulChallenges().slice(0, 4)" :key="challenge.id">
                  {{ challenge.challengeTitle }}
                </li>
              </ul>
            </div>
            
            <div v-if="getSuccessfulChallenges().length > 4" class="excluded-challenges">
              <h4>신문에서 제외되는 도전과제:</h4>
              <ul>
                <li v-for="challenge in getSuccessfulChallenges().slice(4)" :key="challenge.id">
                  {{ challenge.challengeTitle }}
                </li>
              </ul>
            </div>
          </div>
        </div>
        
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

// 성공한 도전과제 가져오기 (모든 도전과제가 성공한 것으로 가정)
const getSuccessfulChallenges = () => {
  if (!newsData.value || !newsData.value.challenges) {
    return []
  }
  
  // isSuccess가 false인 데이터는 오지 않으므로 전체 도전과제 반환
  return newsData.value.challenges
}

// 도전과제 개수 계산
const challengeCount = computed(() => {
  return getSuccessfulChallenges().length
})

// 신문 데이터 가져오기
const fetchNewsData = async (aiNewsId) => {
  loading.value = true
  error.value = ''
  
  try {
    console.log('신문 데이터 요청 중:', aiNewsId)
    const response = await axios.get(`/api/v1/ai-news/${aiNewsId}`, {
      withCredentials: true
    })
    
    newsData.value = response.data
    console.log('신문 데이터 로드 완료:', response.data)
    console.log('도전과제 배열:', response.data.challenges)
    console.log('도전과제 개수:', response.data.challenges ? response.data.challenges.length : 0)
    
    // 데이터 검증
    if (!response.data.challenges) {
      console.warn('도전과제 데이터가 없습니다. 빈 배열로 설정합니다.')
      newsData.value.challenges = []
    }
    
  } catch (err) {
    console.error('신문 데이터 로드 실패:', err)
    
    if (err.response?.status === 404) {
      error.value = '해당 신문을 찾을 수 없습니다.'
    } else if (err.response?.status === 403) {
      error.value = '이 신문을 볼 권한이 없습니다.'
    } else {
      error.value = '신문을 불러오는데 실패했습니다. 다시 시도해주세요.'
    }
  } finally {
    loading.value = false
  }
}

// Template One 데이터 (도전과제 1개)
const templateOneData = computed(() => {
  if (!newsData.value || challengeCount.value !== 1) return {}
  
  const successfulChallenges = getSuccessfulChallenges()
  const challenge = successfulChallenges[0]
  
  if (!challenge) return {}
  
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
  if (!newsData.value || challengeCount.value !== 2) return {}
  
  const successfulChallenges = getSuccessfulChallenges().slice(0, 2)
  
  if (successfulChallenges.length < 2) return {}
  
  return {
    seniorCenterName: newsData.value.seniorCenterName,
    month: String(newsData.value.month).padStart(2, '0'),
    ranking: 1,
    headline1: successfulChallenges[0].challengeTitle,
    content1: successfulChallenges[0].aiDescription,
    imageUrl1: successfulChallenges[0].challengeImage,
    headline2: successfulChallenges[1].challengeTitle,
    content2: successfulChallenges[1].aiDescription,
    imageUrl2: successfulChallenges[1].challengeImage
  }
})

// Template Three 데이터 (도전과제 3개)
const templateThreeData = computed(() => {
  if (!newsData.value || challengeCount.value !== 3) return {}
  
  const successfulChallenges = getSuccessfulChallenges().slice(0, 3)
  
  if (successfulChallenges.length < 3) return {}
  
  return {
    seniorCenterName: newsData.value.seniorCenterName,
    month: String(newsData.value.month).padStart(2, '0'),
    ranking: 1,
    headline1: successfulChallenges[0].challengeTitle,
    content1: successfulChallenges[0].aiDescription,
    imageUrl1: successfulChallenges[0].challengeImage,
    headline2: successfulChallenges[1].challengeTitle,
    content2: successfulChallenges[1].aiDescription,
    imageUrl2: successfulChallenges[1].challengeImage,
    headline3: successfulChallenges[2].challengeTitle,
    content3: successfulChallenges[2].aiDescription,
    imageUrl3: successfulChallenges[2].challengeImage
  }
})

// Template Four 데이터 (도전과제 4개)
const templateFourData = computed(() => {
  if (!newsData.value || challengeCount.value !== 4) return {}
  
  const successfulChallenges = getSuccessfulChallenges().slice(0, 4)
  
  if (successfulChallenges.length < 4) return {}
  
  return {
    seniorCenterName: newsData.value.seniorCenterName,
    month: String(newsData.value.month).padStart(2, '0'),
    ranking: 1,
    headline1: successfulChallenges[0].challengeTitle,
    content1: successfulChallenges[0].aiDescription,
    imageUrl1: successfulChallenges[0].challengeImage,
    headline2: successfulChallenges[1].challengeTitle,
    content2: successfulChallenges[1].aiDescription,
    imageUrl2: successfulChallenges[1].challengeImage,
    headline3: successfulChallenges[2].challengeTitle,
    content3: successfulChallenges[2].aiDescription,
    imageUrl3: successfulChallenges[2].challengeImage,
    headline4: successfulChallenges[3].challengeTitle,
    content4: successfulChallenges[3].aiDescription,
    imageUrl4: successfulChallenges[3].challengeImage
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
      withCredentials: true,
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
    
    let errorMessage = 'PDF 업로드에 실패했습니다.'
    
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message || '서버 오류'
      
      if (status === 413) {
        errorMessage = 'PDF 파일이 너무 큽니다. 다시 시도해주세요.'
      } else if (status === 415) {
        errorMessage = '지원하지 않는 파일 형식입니다.'
      } else {
        errorMessage = `PDF 업로드에 실패했습니다: ${message}`
      }
    } else if (error.request) {
      errorMessage = 'PDF 업로드에 실패했습니다: 서버와 연결할 수 없습니다.'
    } else {
      errorMessage = 'PDF 생성에 실패했습니다.'
    }
    
    alert(errorMessage)
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

    // PDF 생성하고 다운로드
    await html2pdf().set(opt).from(element).save()
    
    alert('PDF가 성공적으로 다운로드되었습니다!')
    
  } catch (error) {
    console.error('PDF 저장 실패:', error)
    
    let errorMessage = 'PDF 생성에 실패했습니다.'
    
    if (error.name === 'QuotaExceededError') {
      errorMessage = '브라우저 저장 공간이 부족합니다. 캐시를 정리하고 다시 시도해주세요.'
    } else if (error.message?.includes('Canvas')) {
      errorMessage = 'PDF 생성 중 화면 캡처에 실패했습니다. 이미지 로딩을 확인해주세요.'
    }
    
    alert(errorMessage)
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
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin: 20px auto;
  max-width: 700px;
}

.no-template h3 {
  color: #333;
  margin-bottom: 20px;
  font-size: 24px;
}

.no-challenges, .too-many-challenges {
  margin: 30px 0;
  text-align: left;
}

.challenge-selection {
  margin-top: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  text-align: left;
}

.challenge-selection h4 {
  color: #495057;
  margin-bottom: 15px;
  font-size: 16px;
}

.challenge-selection ul {
  list-style: none;
  padding: 0;
}

.selected-challenges ul li, .excluded-challenges ul li {
  padding: 5px 0;
  color: #495057;
}

.excluded-challenges {
  opacity: 0.7;
  margin-top: 15px;
}

.debug-info {
  font-family: monospace;
  font-size: 12px;
  color: #333;
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
  flex-wrap: wrap;
}

.save-button button {
  padding: 12px 24px;
  font-size: 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  white-space: nowrap;
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

.member-actions {
  text-align: center;
  margin: 40px 0;
  padding: 20px;
}

@media (max-width: 768px) {
  .detail-container {
    padding: 10px;
  }
  
  .pdf-header {
    padding: 15px 20px;
    flex-direction: column;
    align-items: stretch;
  }
  
  .pdf-actions {
    justify-content: center;
  }
  
  .save-button {
    flex-direction: column;
    gap: 10px;
  }
  
  .save-button button {
    width: 100%;
    max-width: 300px;
  }
  
  .no-template {
    margin: 10px;
    padding: 30px 20px;
  }
  
  .challenge-selection {
    padding: 15px;
  }
}
</style>