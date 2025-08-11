<!-- AINewsPDFGenerator.vue -->
<template>
  <div class="pdf-generator">
    <!-- PDF 미리보기 및 생성 버튼 -->
    <div v-if="!pdfGenerated" class="pdf-preview-section">
      <button @click="generateAndUploadPDF" :disabled="generating" class="generate-pdf-btn">
        {{ generating ? 'PDF 생성 중...' : 'PDF 생성하기' }}
      </button>
    </div>

    <!-- PDF 템플릿 (숨김) -->
    <div ref="pdfTemplate" class="pdf-template" style="position: absolute; left: -9999px; top: -9999px;">
      <div class="newspaper-container">
        <!-- 헤더 -->
        <div class="newspaper-header">
          <h1 class="newspaper-title">{{ newsData.newsTitle }}</h1>
          <div class="newspaper-date">{{ formatDate(newsData.year, newsData.month) }}</div>
          <div class="center-name">{{ newsData.centerName }}</div>
        </div>

        <!-- 도전과제별 기사 -->
        <div class="articles-container">
          <div 
            v-for="(challenge, index) in completedChallenges" 
            :key="challenge.id"
            class="article-section"
          >
            <div class="article-header">
              <h2 class="article-title">{{ challenge.challengeTitle }}</h2>
              <div class="article-meta">
                <span class="article-date">{{ formatChallengeDate(challenge.completedAt) }}</span>
                <span class="article-location">{{ challenge.challengePlace }}</span>
              </div>
            </div>

            <!-- 기사 본문 -->
            <div class="article-content">
              <div class="article-text">
                {{ challenge.aiDescription || challenge.description }}
              </div>
              
              <!-- 이미지 -->
              <div v-if="challenge.challengeImage" class="article-image">
                <img :src="challenge.challengeImage" :alt="challenge.challengeTitle" />
                <div class="image-caption">{{ challenge.challengeTitle }} 활동 모습</div>
              </div>
            </div>

            <!-- 구분선 -->
            <div v-if="index < completedChallenges.length - 1" class="article-divider"></div>
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

// PDF 생성 및 업로드
const generateAndUploadPDF = async () => {
  if (completedChallenges.value.length === 0) {
    alert('완료된 도전과제가 없어 PDF를 생성할 수 없습니다.')
    return
  }

  generating.value = true

  try {
    // 1. HTML을 Canvas로 변환
    const element = pdfTemplate.value
    element.style.position = 'static'
    element.style.left = 'auto'
    element.style.top = 'auto'

    const canvas = await html2canvas(element, {
      scale: 2,
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#ffffff',
      width: 794, // A4 width in pixels at 96 DPI
      height: 1123 // A4 height in pixels at 96 DPI
    })

    // 다시 숨김
    element.style.position = 'absolute'
    element.style.left = '-9999px'
    element.style.top = '-9999px'

    // 2. Canvas를 PDF로 변환
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    
    const imgWidth = 210 // A4 width in mm
    const pageHeight = 295 // A4 height in mm
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    let heightLeft = imgHeight

    let position = 0

    // 첫 페이지
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight

    // 여러 페이지가 필요한 경우
    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }

    // 3. PDF를 Blob으로 변환
    const pdfBlob = pdf.output('blob')

    // 4. FormData로 백엔드에 업로드
    const formData = new FormData()
    formData.append('pdfFile', pdfBlob, `${props.newsData.centerName}_${props.newsData.year}_${props.newsData.month}.pdf`)
    formData.append('newsId', props.newsData.id)

    const response = await api.post('/api/v1/admin/ai-news/upload-pdf', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    console.log('PDF 업로드 성공:', response.data)
    pdfGenerated.value = true
    emit('pdf-generated', response.data)

    alert('AI 신문 PDF가 성공적으로 생성되었습니다!')

  } catch (error) {
    console.error('PDF 생성 실패:', error)
    alert('PDF 생성에 실패했습니다.')
  } finally {
    generating.value = false
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

/* PDF 템플릿 스타일 */
.newspaper-container {
  width: 794px;
  min-height: 1123px;
  background: white;
  padding: 40px;
  font-family: 'Noto Sans KR', serif;
  color: #333;
  box-shadow: none;
}

.newspaper-header {
  text-align: center;
  border-bottom: 3px solid #333;
  padding-bottom: 20px;
  margin-bottom: 30px;
}

.newspaper-title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #1a202c;
}

.newspaper-date {
  font-size: 16px;
  color: #666;
  margin-bottom: 5px;
}

.center-name {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.articles-container {
  margin-bottom: 40px;
}

.article-section {
  margin-bottom: 30px;
}

.article-header {
  margin-bottom: 15px;
}

.article-title {
  font-size: 22px;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 8px;
}

.article-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.article-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.article-text {
  flex: 1;
  font-size: 14px;
  line-height: 1.6;
  color: #444;
}

.article-image {
  flex: 0 0 200px;
  text-align: center;
}

.article-image img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.image-caption {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
  font-style: italic;
}

.article-divider {
  height: 1px;
  background-color: #e2e8f0;
  margin: 25px 0;
}

.newspaper-footer {
  border-top: 2px solid #e2e8f0;
  padding-top: 20px;
  text-align: center;
  margin-top: 40px;
}

.footer-text {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.footer-date {
  font-size: 12px;
  color: #999;
}
</style>