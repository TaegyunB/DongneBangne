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
        <div class="col-challenges">수행한 도전</div>
        <div class="col-date header-date">발간일자</div>
        <div class="col-action">PDF</div>
      </div>
      
      <div class="table-body">
        <div 
          v-for="news in displayNewsList" 
          :key="news.id"
          class="table-row"
        >
          <div class="col-month">
            <div class="month-label">
              {{ news.month }}월
            </div>
          </div>
          
          <div class="col-challenges">
            <div class="challenges-list" v-if="getChallengeNames(news).length > 0">
              <ul>
                <li v-for="challengeName in getChallengeNames(news)" :key="challengeName">
                  {{ challengeName }}
                </li>
              </ul>
            </div>
            <div v-else class="no-challenges">
              완료된 도전과제가 없습니다
            </div>
          </div>
          
          <div class="col-date">
            {{ formatPublishDate(news) }}
          </div>
          
          <div class="col-action">
            <!-- PDF가 생성된 경우: 보기 버튼 -->
            <button 
              v-if="news.pdfUrl"
              @click="viewNewsPdf(news.id)"
              class="action-btn view-btn"
            >
              PDF 보기
            </button>
            
            <!-- AI 기사는 생성되었지만 PDF가 없는 경우: PDF 생성 버튼 -->
            <button 
              v-else-if="!news.pdfUrl && hasCompletedChallenges(news)"
              @click="showPdfGenerator(news)"
              class="action-btn generate-pdf-btn"
            >
              PDF 생성하기
            </button>
            
            <!-- 완료된 도전과제가 없는 경우 -->
            <span 
              v-else 
              class="no-content-label"
              title="이번 달은 완료된 도전과제가 없어 PDF를 생성할 수 없습니다."
            >
              PDF 생성 불가
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

    <!-- 알림 모달 -->
    <div v-if="showAlertModal" class="modal-overlay" @click.self="closeAlertModal">
      <div class="modal-content">
        <button class="modal-close-btn" @click="closeAlertModal">×</button>
        <h2>{{ alertTitle }}</h2>
        <p class="modal-description">{{ alertMessage }}</p>
        <div class="modal-action-buttons">
          <button class="modal-button" @click="closeAlertModal">확인</button>
        </div>
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
const currentPage = ref(1)
const itemsPerPage = 10

// PDF 생성 모달 관련
const showPdfModal = ref(false)
const selectedNews = ref(null)

// 알림 모달 관련
const showAlertModal = ref(false)
const alertTitle = ref('')
const alertMessage = ref('')

// 알림 모달 함수
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
        errorMessage = '권한이 없습니다.'
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

// 표시할 신문 목록 (실제로 생성된 신문만)
const displayNewsList = computed(() => {
  // API에서 받은 신문 목록을 년-월 내림차순으로 정렬
  const sortedNews = [...newsList.value].sort((a, b) => {
    if (a.year !== b.year) return b.year - a.year
    return b.month - a.month
  })
  
  // 페이지네이션 적용
  const startIndex = (currentPage.value - 1) * itemsPerPage
  const endIndex = startIndex + itemsPerPage
  return sortedNews.slice(startIndex, endIndex)
})

// 페이지네이션 계산 (실제 신문 개수 기준)
const totalPages = computed(() => Math.ceil(newsList.value.length / itemsPerPage))

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// 도전과제 이름 목록 가져오기 (bullet point 용)
const getChallengeNames = (news) => {
  if (news.challenges && news.challenges.length > 0) {
    return news.challenges
      .filter(c => (c.challengeName || c.challengeTitle) && c.isSuccess)
      .map(c => c.challengeName || c.challengeTitle)
      .slice(0, 6) // 최대 6개까지만 표시
  }
  return []
}

// 발간일자 포맷팅 (현재 날짜 기준)
const formatPublishDate = (news) => {
  // 신문이 생성된 날짜가 있으면 그 날짜를 사용, 없으면 현재 날짜 사용
  let publishDate
  
  if (news.createdAt) {
    // API에서 생성일시를 제공하는 경우
    publishDate = new Date(news.createdAt)
  } else {
    // 생성일시가 없으면 현재 날짜 사용
    publishDate = new Date()
  }
  
  const year = publishDate.getFullYear()
  const month = String(publishDate.getMonth() + 1).padStart(2, '0')
  const day = String(publishDate.getDate()).padStart(2, '0')
  
  return `${year}.${month}.${day}`
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
    showAlert('오류', '신문 정보를 불러오는데 실패했습니다.')
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
    
    let errorMessage = 'PDF를 불러오는데 실패했습니다.'
    
    if (error.response?.status === 404) {
      errorMessage = 'PDF 파일을 찾을 수 없습니다. PDF를 먼저 생성해주세요.'
    } else if (error.response?.status === 403) {
      errorMessage = 'PDF를 볼 권한이 없습니다.'
    }
    
    showAlert('오류', errorMessage)
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
    if (news && !news.pdfUrl) {
      showPdfGenerator(news)
    }
  }
})
</script>

<style scoped>
.container {
  max-width: 1300px; /* 기존 1200px에서 1300px로 증가 */
  margin: 40px auto;
  padding: 0 20px;
  font-family: 'Noto Sans KR', sans-serif;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h1 {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #333;
}

.header h3 {
  font-size: 20px;
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
  background-color: white;
  width: 100%; /* 테이블 전체 너비 사용 */
}

.table-header {
  display: grid;
  grid-template-columns: 200px 1fr 200px 200px; /* 고정 너비로 세로줄 일정하게 */
  background-color: white;
  font-weight: 600;
  font-size: 20px; /* 헤더 폰트 크기 20px */
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
  grid-template-columns: 200px 1fr 200px 200px; /* 헤더와 동일한 고정 너비 */
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
  font-size: 18px; /* 기본 폰트 크기 18px */
}

.table-row > div:last-child {
  border-right: none;
}

.col-month {
  flex-direction: column !important;
  align-items: center !important;
  gap: 8px;
  justify-content: center !important;
}

.month-label {
  font-weight: bold; /* 월 표시 볼드 */
  color: #1f2937;
  font-size: 19px;
}

.col-challenges {
  flex-direction: column !important;
  align-items: flex-start !important;
  justify-content: center !important;
  text-align: left !important;
}

.challenges-list {
  width: 100%;
}

.challenges-list ul {
  margin: 0;
  padding-left: 16px;
  list-style-type: disc;
}

.challenges-list li {
  font-size: 18px;
  color: #4b5563;
  line-height: 1.5;
  margin-bottom: 4px;
  font-weight: normal; /* 리스트 항목은 볼드 아님 */
}

.challenges-list li:last-child {
  margin-bottom: 0;
}

.no-challenges {
  font-size: 18px;
  color: #9ca3af;
  font-style: italic;
  font-weight: normal;
}

.col-date {
  font-size: 18px;
  color: #4b5563;
  font-weight: bold; /* 날짜는 볼드 아님 */
}

.col-action {
  justify-content: center !important;
}

.action-btn {
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: bold; /* 버튼 텍스트만 볼드 */
  cursor: pointer;
  transition: all 0.2s;
  min-width: 80px;
  white-space: nowrap;
}

.view-btn {
  background-color: #4A90E2;
  color: white;
}

.view-btn:hover {
  background-color: #1e7ae3;
  transform: translateY(-1px);
}

.generate-pdf-btn {
  background-color: #fed800;
  color: white;
}

.generate-pdf-btn:hover:not(:disabled) {
  background-color: #feb200;
  transform: translateY(-1px);
}

.no-content-label {
  font-size: 14px;
  color: #9ca3af;
  background-color: #f3f4f6;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  cursor: help;
  font-weight: normal;
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

/* 모달 스타일 - 2번째 코드와 동일하게 */
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

.modal-content h2 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #333;
  font-family: 'Noto Sans KR', sans-serif;
}

.modal-description {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
  color: #666;
  font-family: 'Noto Sans KR', sans-serif;
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
  font-family: 'Noto Sans KR', sans-serif;
}

.modal-button:hover {
  background-color: #2b6ce5;
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .container {
    padding: 0 16px;
    max-width: 100%;
  }
  
  .table-header,
  .table-row {
    grid-template-columns: 80px 1fr 100px 100px; /* 모바일에서도 고정 너비 */
  }
  
  .table-header {
    font-size: 16px; /* 모바일에서 헤더 폰트 크기 조정 */
  }
  
  .table-row > div {
    font-size: 14px; /* 모바일에서 내용 폰트 크기 조정 */
  }
  
  .challenges-list li {
    font-size: 14px;
  }
  
  .action-btn {
    padding: 8px 12px;
    font-size: 12px;
    min-width: 60px;
  }
}
</style>