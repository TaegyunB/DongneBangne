<!-- AINewsPDFGenerator.vue (최종 최적화 버전) -->
<template>
  <div class="pdf-generator">
    <!-- PDF 미리보기 및 생성 버튼 -->
    <div v-if="!pdfGenerated" class="pdf-preview-section">
      <button @click="generateAndUploadPDF" :disabled="generating" class="generate-pdf-btn">
        {{ generating ? 'PDF 생성 중...' : 'PDF 생성하기' }}
      </button>
      
      <!-- 진행 상태 표시 -->
      <div v-if="generating" class="progress-info">
        <p>{{ progressMessage }}</p>
        <div v-if="imageProgress.total > 0" class="image-progress">
          이미지 처리: {{ imageProgress.loaded }}/{{ imageProgress.total }}
        </div>
      </div>
    </div>
<!--  -->
    <!-- PDF 템플릿 (숨김) -->
    <div ref="pdfTemplate" class="pdf-template" style="position: absolute; left: -9999px; top: -9999px;">
      <div class="newspaper-container">
        <!-- 헤더 -->
        <div class="newspaper-header">
          <h1 class="newspaper-title">{{ newsData.newsTitle }}</h1>
          <div class="newspaper-date">{{ newsData.centerName }} - {{ formatDate(newsData.year, newsData.month) }}</div>
        </div>
<!-- . -->
        <!-- 도전과제별 기사 (상하 2행 레이아웃) -->
        <div class="articles-container">
          <!-- 상단 행 (첫 번째, 두 번째 미션) -->
          <div class="articles-row">
            <div 
              v-for="(challenge, index) in completedChallenges.slice(0, 2)" 
              :key="challenge.id"
              class="article-item"
            >
              <div class="article-header">
                <h2 class="article-title">{{ challenge.challengeTitle }}</h2>
                <div class="article-meta">
                  <span class="article-location-date">{{ challenge.challengePlace }} - {{ formatChallengeDate(challenge.completedAt) }}</span>
                </div>
              </div>

              <div class="article-content">
                <div v-if="challenge.challengeImage" class="article-image">
                  <img 
                    :src="challenge.base64Image || challenge.challengeImage" 
                    :alt="challenge.challengeTitle"
                    crossorigin="anonymous"
                  />
                </div>
                
                <div class="article-text">
                  {{ challenge.aiDescription || challenge.description }}
                </div>
              </div>
            </div>
          </div>

          <!-- 하단 행 (세 번째, 네 번째 미션) -->
          <div v-if="completedChallenges.length > 2" class="articles-row">
            <div 
              v-for="(challenge, index) in completedChallenges.slice(2, 4)" 
              :key="challenge.id"
              class="article-item"
            >
              <div class="article-header">
                <h2 class="article-title">{{ challenge.challengeTitle }}</h2>
                <div class="article-meta">
                  <span class="article-location-date">{{ challenge.challengePlace }} - {{ formatChallengeDate(challenge.completedAt) }}</span>
                </div>
              </div>

              <div class="article-content">
                <div v-if="challenge.challengeImage" class="article-image">
                  <img 
                    :src="challenge.base64Image || challenge.challengeImage" 
                    :alt="challenge.challengeTitle"
                    crossorigin="anonymous"
                  />
                </div>
                
                <div class="article-text">
                  {{ challenge.aiDescription || challenge.description }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 푸터 -->
        <div class="newspaper-footer">
          <div class="footer-text">{{ newsData.centerName }}에서 발행하는 AI 신문</div>
          <div class="footer-date">발행일: {{ formatDate(newsData.year, newsData.month) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import api from '@/api/axios'

const props = defineProps({
  newsData: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['pdf-generated'])

const pdfTemplate = ref(null)
const generating = ref(false)
const pdfGenerated = ref(false)
const progressMessage = ref('')
const imageProgress = ref({ loaded: 0, total: 0 })

// 완료된 도전과제만 필터링
const completedChallenges = computed(() => {
  return props.newsData.challenges?.filter(challenge => 
    challenge.isSuccess && challenge.aiDescription
  ) || []
})

// 날짜 포맷팅
const formatDate = (year, month) => {
  return `${year}년 ${month}월`
}

const formatChallengeDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`
}

// 이미지를 Base64로 변환하는 함수
const convertImageToBase64 = (url) => {
  return new Promise((resolve, reject) => {
    // CORS 문제를 해결하기 위해 프록시를 통해 이미지 가져오기
    const proxyUrl = `/api/v1/images/proxy?url=${encodeURIComponent(url)}`
    
    const img = new Image()
    img.crossOrigin = 'anonymous'
    
    img.onload = () => {
      try {
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        
        canvas.width = img.naturalWidth
        canvas.height = img.naturalHeight
        
        ctx.drawImage(img, 0, 0)
        
        const base64 = canvas.toDataURL('image/jpeg', 0.8)
        resolve(base64)
      } catch (error) {
        console.error('Base64 변환 실패:', error)
        reject(error)
      }
    }
    
    img.onerror = (error) => {
      console.error('이미지 로드 실패:', error)
      // S3 이미지 로드 실패 시 원본 URL 그대로 사용
      resolve(url)
    }
    
    // 프록시 URL이 있으면 프록시로, 없으면 원본 URL로
    img.src = proxyUrl
  })
}

// 백엔드 프록시 없이 직접 처리하는 대안 함수
const convertImageToBase64Direct = (url) => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    
    // CORS 허용 시도
    img.crossOrigin = 'anonymous'
    
    img.onload = () => {
      try {
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        
        canvas.width = img.naturalWidth
        canvas.height = img.naturalHeight
        
        ctx.drawImage(img, 0, 0)
        
        const base64 = canvas.toDataURL('image/jpeg', 0.8)
        resolve(base64)
      } catch (error) {
        console.error('Base64 변환 실패:', error)
        // Canvas 오염 등의 문제로 변환 실패 시 원본 URL 반환
        resolve(url)
      }
    }
    
    img.onerror = (error) => {
      console.error('이미지 로드 실패:', error)
      // 로드 실패 시 원본 URL 반환 (PDF에서는 표시되지 않을 수 있음)
      resolve(url)
    }
    
    img.src = url
  })
}

// 모든 이미지를 Base64로 미리 변환
const convertAllImagesToBase64 = async () => {
  const challenges = completedChallenges.value.filter(c => c.challengeImage)
  
  if (challenges.length === 0) return
  
  progressMessage.value = '이미지를 처리하고 있습니다...'
  imageProgress.value = { loaded: 0, total: challenges.length }
  
  for (let i = 0; i < challenges.length; i++) {
    const challenge = challenges[i]
    try {
      console.log(`이미지 변환 시작: ${challenge.challengeImage}`)
      
      // 프록시 사용 시도, 실패하면 직접 시도
      let base64Image
      try {
        base64Image = await convertImageToBase64(challenge.challengeImage)
      } catch (error) {
        console.warn('프록시를 통한 변환 실패, 직접 시도:', error)
        base64Image = await convertImageToBase64Direct(challenge.challengeImage)
      }
      
      challenge.base64Image = base64Image
      console.log(`이미지 변환 완료: ${challenge.id}`)
      
    } catch (error) {
      console.error(`이미지 변환 실패 (${challenge.id}):`, error)
      // 변환 실패 시 원본 URL 유지
    }
    
    imageProgress.value.loaded = i + 1
  }
}

// PDF 생성 및 업로드
const generateAndUploadPDF = async () => {
  if (completedChallenges.value.length === 0) {
    alert('완료된 도전과제가 없어 PDF를 생성할 수 없습니다.')
    return
  }

  generating.value = true

  try {
    // 1. 모든 이미지를 Base64로 변환
    await convertAllImagesToBase64()
    
    // 짧은 대기 시간 (이미지 렌더링 완료 대기)
    progressMessage.value = 'PDF를 생성하고 있습니다...'
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 2. HTML을 Canvas로 변환
    const element = pdfTemplate.value
    element.style.position = 'static'
    element.style.left = 'auto'
    element.style.top = 'auto'

    const canvas = await html2canvas(element, {
      scale: 1.5,
      useCORS: true,
      allowTaint: false,
      backgroundColor: '#ffffff',
      width: 794,
      height: 1123,
      logging: false,
      imageTimeout: 15000,
      onclone: (clonedDoc) => {
        console.log('Document cloned for canvas conversion')
      }
    })

    // 다시 숨김
    element.style.position = 'absolute'
    element.style.left = '-9999px'
    element.style.top = '-9999px'

    // 3. Canvas를 PDF로 변환
    progressMessage.value = 'PDF 파일을 생성하고 있습니다...'
    
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    
    const imgWidth = 210
    const pageHeight = 297
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    
    console.log(`Canvas 크기: ${canvas.width} x ${canvas.height}`)
    console.log(`PDF 이미지 크기: ${imgWidth} x ${imgHeight}mm`)
    console.log(`A4 페이지 높이: ${pageHeight}mm`)
    
    // 이미지가 한 페이지에 들어가는지 확인
    if (imgHeight <= pageHeight) {
      // 한 페이지에 모두 들어감
      pdf.addImage(imgData, 'PNG', 0, 0, imgWidth, imgHeight)
      console.log('한 페이지에 모든 내용이 들어감')
    } else {
      // 여러 페이지로 분할
      let remainingHeight = imgHeight
      let pageNumber = 1

      while (remainingHeight > 0) {
        if (pageNumber > 1) {
          pdf.addPage()
        }

        const heightForThisPage = Math.min(pageHeight, remainingHeight)
        const sourceY = (pageNumber - 1) * pageHeight * (canvas.height / imgHeight)
        const sourceHeight = heightForThisPage * (canvas.height / imgHeight)

        // Canvas의 일부를 잘라서 새 캔버스에 그리기
        const tempCanvas = document.createElement('canvas')
        const tempCtx = tempCanvas.getContext('2d')
        tempCanvas.width = canvas.width
        tempCanvas.height = sourceHeight

        tempCtx.drawImage(canvas, 0, sourceY, canvas.width, sourceHeight, 0, 0, canvas.width, sourceHeight)
        const tempImgData = tempCanvas.toDataURL('image/png')

        pdf.addImage(tempImgData, 'PNG', 0, 0, imgWidth, heightForThisPage)

        remainingHeight -= heightForThisPage
        pageNumber++
        
        console.log(`페이지 ${pageNumber - 1} 추가 완료, 남은 높이: ${remainingHeight}mm`)
      }
    }

    // 4. PDF를 Blob으로 변환
    const pdfBlob = pdf.output('blob')

    // 파일 크기 체크
    const fileSizeInMB = pdfBlob.size / (1024 * 1024)
    console.log(`생성된 PDF 크기: ${fileSizeInMB.toFixed(2)}MB`)
    
    if (fileSizeInMB > 10) {
      console.warn('PDF 크기가 큽니다. 압축을 고려해주세요.')
    }

    // 5. FormData로 백엔드에 업로드
    progressMessage.value = 'PDF를 업로드하고 있습니다...'
    
    const formData = new FormData()
    formData.append('pdfFile', pdfBlob, `${props.newsData.centerName}_${props.newsData.year}_${props.newsData.month}.pdf`)
    formData.append('newsId', props.newsData.id)

    const response = await api.post('/api/v1/admin/ai-news/upload-pdf', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 60000 // 60초 타임아웃
    })

    console.log('PDF 업로드 성공:', response.data)
    pdfGenerated.value = true
    emit('pdf-generated', response.data)

    alert('AI 신문 PDF가 성공적으로 생성되었습니다!')

  } catch (error) {
    console.error('PDF 생성 실패:', error)
    
    let errorMessage = 'PDF 생성에 실패했습니다.'
    
    if (error.response?.status === 413) {
      errorMessage = 'PDF 파일 크기가 너무 큽니다. 이미지 품질을 낮춰주세요.'
    } else if (error.message?.includes('canvas')) {
      errorMessage = 'PDF 변환 중 오류가 발생했습니다. 이미지 로딩을 확인해주세요.'
    }
    
    alert(errorMessage)
  } finally {
    generating.value = false
    progressMessage.value = ''
    imageProgress.value = { loaded: 0, total: 0 }
  }
}
</script>

<style scoped>
.pdf-generator {
  margin: 20px 0;
}

.generate-pdf-btn {
  background-color: #10b981;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.generate-pdf-btn:hover:not(:disabled) {
  background-color: #059669;
}

.generate-pdf-btn:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.progress-info {
  margin-top: 10px;
  padding: 10px;
  background-color: #f3f4f6;
  border-radius: 6px;
  font-size: 14px;
  color: #374151;
}

.image-progress {
  margin-top: 5px;
  font-size: 12px;
  color: #6b7280;
}

/* PDF 템플릿 스타일 (최종 최적화 버전) */
.newspaper-container {
  width: 794px;
  height: 1123px;
  background: white;
  padding: 20px;
  font-family: 'Noto Sans KR', serif;
  color: #333;
  box-shadow: none;
  display: flex;
  flex-direction: column;
}

.newspaper-header {
  text-align: center;
  border-top: 1px solid #333;
  border-bottom: 1px solid #333;
  padding-bottom: 10px;
  margin-bottom: 15px;
  flex-shrink: 0;
}

.newspaper-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
  color: #1a202c;
}

.newspaper-date {
  font-size: 16px;
  color: #666;
  margin-bottom: 2px;
}

.articles-container {
  flex: 1;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.articles-row {
  display: flex;
  gap: 15px;
  flex: 1;
}

.article-item {
  flex: 1;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.article-header {
  margin-bottom: 10px;
  flex-shrink: 0;
}

.article-title {
  font-size: 20px;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 5px;
  line-height: 1.2;
}

.article-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 16px;
  color: #666;
}

.article-location-date {
  font-size: 16px;
  color: #666;
}

.article-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
}

.article-image {
  flex-shrink: 0;
  text-align: center;
  height: 160px;
  overflow: hidden;
}

.article-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
}

.article-text {
  flex: 1;
  font-size: 16px;
  line-height: 1.4;
  color: #444;
  text-align: justify;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 12;
  -webkit-box-orient: vertical;
}

.newspaper-footer {
  border-top: 1px solid #e2e8f0;
  padding-top: 8px;
  text-align: center;
  flex-shrink: 0;
}

.footer-text {
  font-size: 10px;
  color: #666;
  margin-bottom: 2px;
}

.footer-date {
  font-size: 8px;
  color: #999;
}
</style>