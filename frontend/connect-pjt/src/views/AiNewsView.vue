<template>
  <div class="container">
    <div class="header">
      <h1>{{ seniorCenterName }}의 AI 신문을 살펴보세요!</h1>
      <h3>AI가 {{ seniorCenterName }}의 랭킹, 미션 수행도, 미션 인증 사진, 미션 인증 글을 정리해주었어요</h3>
    </div>

    <!-- 신문 테이블 -->
    <div class="news-table">
      <div class="table-header">
        <div class="col-month">발간 월</div>
        <div class="col-date">발간일자</div>
        <div class="col-action">액션</div>
      </div>
      
      <div class="table-body">
        <div 
          v-for="news in displayNewsList" 
          :key="news.id || `${news.year}-${news.month}`"
          class="table-row"
        >
          <div class="col-month">
            <div class="month-label">
              {{ news.month }}월
            </div>
            <div class="news-description">
              {{ getNewsDescription(news) }}
            </div>
          </div>
          
          <div class="col-date">
            {{ news.year }}.{{ String(news.month).padStart(2, '0') }}.{{ getLastDay(news.year, news.month) }}
          </div>
          
          <div class="col-action">
            <!-- PDF가 생성된 경우: 보기 버튼 -->
            <button 
              v-if="news.isGenerated && news.pdfUrl"
              @click="viewNewsPdf(news.id)"
              class="action-btn view-btn"
            >
              PDF 보기
            </button>
            
            <!-- AI 기사는 생성되었지만 PDF가 없는 경우: PDF 생성 버튼 -->
            <button 
              v-else-if="news.isGenerated && !news.pdfUrl && hasCompletedChallenges(news)"
              @click="showPdfGenerator(news)"
              class="action-btn generate-pdf-btn"
            >
              PDF 생성하기
            </button>
            
            <!-- AI 기사가 아직 생성되지 않은 경우: AI 신문 생성 버튼 -->
            <button 
              v-else-if="!news.isGenerated"
              @click="createNews(news)"
              :disabled="creatingNewsIds.includes(getNewsKey(news)) || !canCreateNews(news)"
              class="action-btn create-btn"
              :title="getCreateButtonTooltip(news)"
            >
              {{ getCreateButtonText(news) }}
            </button>
            
            <!-- 완료된 도전과제가 없는 경우 -->
            <span 
              v-else 
              class="no-content-label"
              title="이번 달은 완료된 도전과제가 없어 신문이 생성되지 않았습니다."
            >
              내용 없음
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- PDF 생성 모달 -->
    <div v-if="showPdfModal" class="modal-overlay" @click.self="closePdfModal">
      <div class="modal-content pdf-modal">
        <button class="modal-close-btn" @click="closePdfModal">×</button>
        <h2>AI 신문 PDF 생성</h2>
        <p>{{ selectedNews?.year }}년 {{ selectedNews?.month }}월 AI 신문을 PDF로 생성합니다.</p>
        
        <!-- PDF 생성 컴포넌트 -->
        <AINewsPDFGenerator 
          v-if="selectedNews"
          :newsData="selectedNews"
          @pdf-generated="handlePdfGenerated"
        />
      </div>
    </div>

    <!-- 데이터가 없는 경우 -->
    <div v-if="!loading && displayNewsList.length === 0" class="no-data">
      <p>아직 생성된 신문이 없습니다.</p>
      <p>도전과제를 완료하고 AI 신문을 생성해보세요!</p>
    </div>

    <!-- 페이지네이션 -->
    <div v-if="totalPages > 1" class="pagination">
      <button 
        @click="changePage(currentPage - 1)"
        :disabled="currentPage === 1"
        class="page-btn"
      >
        ‹
      </button>
      
      <button
        v-for="page in visiblePages"
        :key="page"
        @click="changePage(page)"
        :class="['page-btn', { active: currentPage === page }]"
      >
        {{ page }}
      </button>
      
      <span v-if="totalPages > 5" class="page-dots">...</span>
      
      <button
        v-if="totalPages > 5"
        @click="changePage(totalPages)"
        :class="['page-btn', { active: currentPage === page }]"
      >
        {{ totalPages }}
      </button>
      
      <button 
        @click="changePage(currentPage + 1)"
        :disabled="currentPage === totalPages"
        class="page-btn"
      >
        ›
      </button>
    </div>

    <!-- 로딩 상태 -->
    <div class="loading" v-if="loading">
      신문을 불러오고 있습니다...
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/api/axios'
import AINewsPDFGenerator from '@/components/AINewsPDFGenerator.vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const newsList = ref([])
const seniorCenterName = ref('경로당')
const creatingNewsIds = ref([])
const currentPage = ref(1)
const itemsPerPage = 10

// PDF 생성 모달 관련
const showPdfModal = ref(false)
const selectedNews = ref(null)

// 신문 목록 조회
const fetchNews = async () => {
  loading.value = true
  try {
    console.log('API 요청 시작: /api/v1/ai-news')
    
    const response = await api.get('/api/v1/ai-news')
    
    newsList.value = response.data || []
    console.log('신문 목록 로드 완료:', response.data)
    
    // 센터명 업데이트 (첫 번째 신문에서 가져오기)
    if (response.data && response.data.length > 0 && response.data[0].centerName) {
      seniorCenterName.value = response.data[0].centerName
    }
    
  } catch (error) {
    console.error('신문 목록 로드 실패:', error)
    newsList.value = [] // 에러 시 빈 배열로 초기화
    
    let errorMessage = '신문 목록을 불러오는데 실패했습니다.'
    
    if (error.response) {
      const status = error.response.status
      if (status === 403) {
        errorMessage = '신문을 볼 권한이 없습니다.'
      } else if (status === 404) {
        errorMessage = '신문 데이터를 찾을 수 없습니다.'
      }
    } else if (error.request) {
      errorMessage = '서버와 연결할 수 없습니다.'
    }
    
    console.error(errorMessage)
  } finally {
    loading.value = false
  }
}

// 현재 월부터 최근 6개월 생성
const generateRecentMonths = () => {
  const months = []
  const now = new Date()
  
  for (let i = 0; i < 6; i++) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push({
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      isGenerated: false
    })
  }
  
  return months
}

// 표시할 신문 목록 (생성된 신문 + 생성 가능한 월)
const displayNewsList = computed(() => {
  // 최근 6개월 월 목록
  const recentMonths = generateRecentMonths()
  
  // 기존 신문과 매칭하여 통합 리스트 생성
  const combinedList = recentMonths.map(monthData => {
    const existingNews = newsList.value.find(news => 
      news.year === monthData.year && news.month === monthData.month
    )
    
    if (existingNews) {
      return {
        ...existingNews,
        isGenerated: existingNews.isGenerated || true, // API에서 온 데이터는 생성된 것으로 간주
        centerName: existingNews.centerName || seniorCenterName.value
      }
    } else {
      return {
        ...monthData,
        id: null,
        pdfUrl: null,
        challenges: [],
        newsTitle: `${monthData.year}년 ${monthData.month}월 ${seniorCenterName.value} 소식`,
        centerName: seniorCenterName.value,
        isGenerated: false
      }
    }
  })
  
  // 년-월 내림차순 정렬
  const sorted = combinedList.sort((a, b) => {
    if (a.year !== b.year) return b.year - a.year
    return b.month - a.month
  })
  
  // 페이지네이션 적용
  const startIndex = (currentPage.value - 1) * itemsPerPage
  const endIndex = startIndex + itemsPerPage
  return sorted.slice(startIndex, endIndex)
})

// 페이지네이션 계산
const totalPages = computed(() => Math.ceil(6 / itemsPerPage)) // 최근 6개월 기준
const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// 신문 키 생성 (id가 없는 경우 year-month 조합)
const getNewsKey = (news) => {
  return news.id || `${news.year}-${news.month}`
}

// 신문 설명 생성
const getNewsDescription = (news) => {
  if (news.isGenerated && news.challenges && news.challenges.length > 0) {
    const challengeNames = news.challenges.map(c => 
      c.challengeName || c.challengeTitle || '활동'
    ).join(', ')
    return `${challengeNames} 활동을 하며 추억을 나누...`
  } else if (news.isGenerated && (!news.challenges || news.challenges.length === 0)) {
    return '이번 달은 도전과제가 없었습니다.'
  } else if (!news.isGenerated) {
    return '이번 달 도전과제로 AI 신문을 만들어보세요!'
  }
  return '도전과제가 없는 달입니다.'
}

// 해당 월의 마지막 날 구하기
const getLastDay = (year, month) => {
  return new Date(year, month, 0).getDate()
}

// 신문 생성 가능 여부 확인
const canCreateNews = (news) => {
  // 이미 생성된 경우 false
  if (news.isGenerated) return false
  
  // 현재 월보다 미래 월인 경우 false
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth() + 1
  
  if (news.year > currentYear || (news.year === currentYear && news.month > currentMonth)) {
    return false
  }
  
  return true
}

// 생성하기 버튼 텍스트
const getCreateButtonText = (news) => {
  if (creatingNewsIds.value.includes(getNewsKey(news))) {
    return 'AI 신문 생성중...'
  }
  
  if (!canCreateNews(news)) {
    return '생성 불가'
  }
  
  return 'AI 신문 생성하기'
}

// 생성하기 버튼 툴팁
const getCreateButtonTooltip = (news) => {
  if (!canCreateNews(news)) {
    const now = new Date()
    const currentYear = now.getFullYear()
    const currentMonth = now.getMonth() + 1
    
    if (news.year > currentYear || (news.year === currentYear && news.month > currentMonth)) {
      return '미래 월의 신문은 생성할 수 없습니다.'
    }
    return '이미 생성된 신문입니다.'
  }
  return `${news.year}년 ${news.month}월에 완료한 도전과제로 AI 신문을 생성합니다.`
}

// 완료된 도전과제가 있는지 확인
const hasCompletedChallenges = (news) => {
  return news.challenges && news.challenges.some(challenge => 
    challenge.isSuccess && challenge.aiDescription
  )
}

// PDF 생성 모달 열기
const showPdfGenerator = async (news) => {
  try {
    // 신문 상세 정보 가져오기 (도전과제 포함)
    const response = await api.get(`/api/v1/ai-news/${news.id}`)
    selectedNews.value = response.data
    showPdfModal.value = true
  } catch (error) {
    console.error('신문 상세 정보 로드 실패:', error)
    alert('신문 정보를 불러오는데 실패했습니다.')
  }
}

// PDF 생성 모달 닫기
const closePdfModal = () => {
  showPdfModal.value = false
  selectedNews.value = null
}

// PDF 생성 완료 처리
const handlePdfGenerated = (result) => {
  console.log('PDF 생성 완료:', result)
  closePdfModal()
  fetchNews() // 신문 목록 새로고침
}

// PDF 보기
const viewNewsPdf = async (newsId) => {
  try {
    const response = await api.get(`/api/v1/admin/ai-news/${newsId}/pdf`)
    const pdfUrl = response.data.pdfUrl
    
    // 새 탭에서 PDF 열기
    window.open(pdfUrl, '_blank')
    
  } catch (error) {
    console.error('PDF 조회 실패:', error)
    alert('PDF를 불러오는데 실패했습니다.')
  }
}

// AI 신문 생성하기
const createNews = async (news) => {
  if (!canCreateNews(news)) {
    alert('이 신문을 생성할 수 없습니다.') 
    return
  }

  const newsKey = getNewsKey(news)
  creatingNewsIds.value.push(newsKey)
  
  try {
    console.log(`${news.year}년 ${news.month}월 AI 신문 생성 시작`)
    
    const response = await api.post('/api/v1/admin/ai-news/create', {
      year: news.year,
      month: news.month
    })
    
    console.log('AI 신문 생성 완료:', response.data)
    
    // 성공시 신문 목록 새로고침
    await fetchNews()
    
    alert(`${news.year}년 ${news.month}월 AI 신문이 성공적으로 생성되었습니다!`)
    
    // 생성된 신문 보기 (생성된 신문의 ID가 있는 경우)
    if (response.data && response.data.id) {
      router.push(`/news/${response.data.id}`)
    }
    
  } catch (error) {
    console.error('AI 신문 생성 실패:', error)
    
    let errorMessage = 'AI 신문 생성에 실패했습니다.'
    
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message || '서버 오류'
      
      if (status === 400) {
        errorMessage = `신문 생성 조건이 맞지 않습니다: ${message}`
      } else if (status === 403) {
        errorMessage = 'AI 신문을 생성할 권한이 없습니다.'
      } else if (status === 409) {
        errorMessage = '이미 생성된 신문이거나 중복된 요청입니다.'
      } else {
        errorMessage = `AI 신문 생성에 실패했습니다: ${message}`
      }
    } else if (error.request) {
      errorMessage = 'AI 신문 생성에 실패했습니다: 서버와 연결할 수 없습니다.'
    }
    
    alert(errorMessage)
  } finally {
    // 생성 중 상태 제거
    creatingNewsIds.value = creatingNewsIds.value.filter(id => id !== newsKey)
  }
}

// 페이지 변경
const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

onMounted(async () => {
  await fetchNews()
  
  // URL 쿼리에서 PDF 생성 요청 확인
  const generatePdfId = route.query.generatePdf
  
  if (generatePdfId) {
    const news = newsList.value.find(n => n.id == generatePdfId)
    if (news && news.isGenerated && !news.pdfUrl) {
      showPdfGenerator(news)
    }
  }
})
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 20px;
  font-family: 'Noto Sans KR', sans-serif;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h1 {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #333;
}

.header h3 {
  font-size: 16px;
  font-weight: 400;
  color: #666;
  line-height: 1.5;
}

.news-table {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.table-header {
  display: grid;
  grid-template-columns: 3fr 1.5fr 1fr;
  background-color: #f9fafb;
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}

.table-header > div {
  padding: 20px 16px;
  text-align: center;
  border-right: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
}

.table-header > div:last-child {
  border-right: none;
}

.table-row {
  display: grid;
  grid-template-columns: 3fr 1.5fr 1fr;
  border-bottom: 1px solid #f3f4f6;
  transition: background-color 0.15s;
  min-height: 80px;
}

.table-row:hover {
  background-color: #f8fafc;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row > div {
  padding: 20px 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #f3f4f6;
}

.table-row > div:last-child {
  border-right: none;
}

.col-month {
  flex-direction: column !important;
  align-items: flex-start !important;
  gap: 8px;
  justify-content: center !important;
}

.month-label {
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.news-description {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.4;
  max-width: 100%;
}

.col-date {
  font-size: 14px;
  color: #4b5563;
  font-weight: 500;
}

.col-action {
  justify-content: center !important;
}

.action-btn {
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 80px;
  white-space: nowrap;
}

.view-btn {
  background-color: #8b5cf6;
  color: white;
}

.view-btn:hover {
  background-color: #7c3aed;
  transform: translateY(-1px);
}

.generate-pdf-btn {
  background-color: #8b5cf6;
  color: white;
}

.generate-pdf-btn:hover:not(:disabled) {
  background-color: #7c3aed;
  transform: translateY(-1px);
}

.create-btn {
  background-color: #10b981;
  color: white;
}

.create-btn:hover:not(:disabled) {
  background-color: #059669;
  transform: translateY(-1px);
}

.create-btn:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
  transform: none;
}

.no-content-label {
  font-size: 12px;
  color: #9ca3af;
  background-color: #f3f4f6;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  cursor: help;
}

.no-data {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
  font-size: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 30px;
}

.page-btn {
  border: 1px solid #e5e7eb;
  background-color: white;
  color: #6b7280;
  padding: 10px 14px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  min-width: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover:not(:disabled) {
  background-color: #f3f4f6;
  border-color: #d1d5db;
}

.page-btn.active {
  background-color: #1f2937;
  color: white;
  border-color: #1f2937;
}

.page-btn:disabled {
  background-color: #f9fafb;
  color: #d1d5db;
  cursor: not-allowed;
}

.page-dots {
  color: #9ca3af;
  padding: 0 8px;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
  font-size: 16px;
}

.pdf-modal {
  max-width: 600px;
  min-height: 400px;
}

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

.modal-content {
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

@media (max-width: 768px) {
  .container {
    padding: 0 16px;
  }
  
  .table-header,
  .table-row {
    grid-template-columns: 2fr 1fr 1fr;
  }
  
  .news-description {
    display: none;
  }
  
  .action-btn {
    padding: 8px 12px;
    font-size: 12px;
    min-width: 60px;
  }
}
</style>