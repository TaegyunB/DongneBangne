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
        <div class="col-action">보기</div>
      </div>
      
      <div class="table-body">
        <div 
          v-for="news in sortedNewsList" 
          :key="news.id"
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
            <button 
              v-if="news.pdfUrl"
              @click="viewNews(news.id)"
              class="action-btn view-btn"
            >
              보기
            </button>
            <button 
              v-else
              @click="publishNews(news)"
              :disabled="publishingIds.includes(news.id) || !canPublishNews(news)"
              class="action-btn publish-btn"
              :title="getPublishButtonTooltip(news)"
            >
              {{ getPublishButtonText(news) }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 데이터가 없는 경우 -->
    <div v-if="!loading && newsList.length === 0" class="no-data">
      <p>아직 생성된 신문이 없습니다.</p>
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
        :class="['page-btn', { active: currentPage === totalPages }]"
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
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const loading = ref(false)
const newsList = ref([])
const seniorCenterName = ref('AI 신문')
const publishingIds = ref([])
const currentPage = ref(1)
const itemsPerPage = 10

// 신문 목록 조회
const fetchNews = async () => {
  loading.value = true
  try {
    console.log('API 요청 시작: /api/v1/ai-news')
    
    // 명시적으로 HTTP URL 사용
    const response = await axios.get('/api/v1/ai-news', {
      withCredentials: true,
      timeout: 10000
    })
    
    newsList.value = response.data
    console.log('신문 목록 로드 완료:', response.data)
    
  } catch (error) {
    console.error('상세 에러 정보:', error)
    // 기존 에러 처리 코드...
  } finally {
    loading.value = false
  }
}

// 신문 리스트 정렬 및 페이지네이션 적용
const sortedNewsList = computed(() => {
  const sorted = [...newsList.value].sort((a, b) => {
    if (a.year !== b.year) return b.year - a.year
    return b.month - a.month
  })
  
  // 중복 제거 (id 기준)
  const uniqueNews = sorted.filter((news, index, self) => 
    index === self.findIndex(n => n.id === news.id)
  )
  
  // 페이지네이션 적용
  const startIndex = (currentPage.value - 1) * itemsPerPage
  const endIndex = startIndex + itemsPerPage
  return uniqueNews.slice(startIndex, endIndex)
})

// 전체 데이터에서 중복 제거 후 총 페이지 계산
const totalItems = computed(() => {
  const sorted = [...newsList.value].sort((a, b) => {
    if (a.year !== b.year) return b.year - a.year
    return b.month - a.month
  })
  
  const uniqueNews = sorted.filter((news, index, self) => 
    index === self.findIndex(n => n.id === news.id)
  )
  
  return uniqueNews.length
})

// 페이지네이션 계산
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage))
const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// 신문 설명 생성 (challengeName과 challengeTitle 둘 다 지원)
const getNewsDescription = (news) => {
  if (news.challenges && news.challenges.length > 0) {
    const challengeNames = news.challenges.map(c => 
      c.challengeName || c.challengeTitle || '활동'
    ).join(', ')
    return `${challengeNames} 활동을 하며 하하호호 추억을 나누...`
  }
  return '다 같이 근처 계곡으로 놀러가..'
}

// 해당 월의 마지막 날 구하기
const getLastDay = (year, month) => {
  return new Date(year, month, 0).getDate()
}

// 신문 발간 가능 여부 확인
const canPublishNews = (news) => {
  // 이미 발간된 경우 false
  if (news.pdfUrl) return false
  
  // 도전과제가 없으면 발간 불가
  if (!news.challenges || news.challenges.length === 0) return false
  
  // 도전과제가 있으면 발간 가능 (isSuccess는 항상 true인 데이터만 온다고 가정)
  return true
}

// 발간하기 버튼 텍스트
const getPublishButtonText = (news) => {
  if (publishingIds.value.includes(news.id)) {
    return 'AI 신문 발간중...'
  }
  
  if (!canPublishNews(news)) {
    return '도전과제 없음'
  }
  
  return 'AI 신문 발간하기'
}

// 발간하기 버튼 툴팁
const getPublishButtonTooltip = (news) => {
  if (!canPublishNews(news)) {
    return '이번 달에 수행된 도전과제가 없어 신문을 발간할 수 없습니다.'
  }
  return ''
}

// 신문 발간하기 (현재 달의 신문 생성)
const publishNews = async (news) => {
  // 발간 가능 여부 재확인
  if (!canPublishNews(news)) {
    alert('이 신문을 발간할 수 없습니다. 도전과제가 필요합니다.')
    return
  }

  publishingIds.value.push(news.id)
  
  try {
    console.log(`${news.year}년 ${news.month}월 AI 신문 발간 시작`)
    
    // 현재는 전체 센터의 최신 신문을 생성하는 API를 호출
    // 향후 특정 월의 신문 발간 API가 필요할 수 있음
    const response = await axios.post('/api/v1/admin/ai-news/create', {
      year: news.year,
      month: news.month
    }, {
      withCredentials: true
    })
    
    // 성공시 뉴스 목록 새로고침
    await fetchNews()
    
    console.log('AI 신문 발간 완료:', response.data)
    alert(`${news.year}년 ${news.month}월 AI 신문이 성공적으로 발간되었습니다!`)
    
  } catch (error) {
    console.error('AI 신문 발간 실패:', error)
    
    let errorMessage = 'AI 신문 발간에 실패했습니다.'
    
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message || '서버 오류'
      
      if (status === 400) {
        errorMessage = `발간 조건이 맞지 않습니다: ${message}`
      } else if (status === 403) {
        errorMessage = 'AI 신문을 발간할 권한이 없습니다.'
      } else if (status === 409) {
        errorMessage = '이미 발간된 신문이거나 중복된 요청입니다.'
      } else {
        errorMessage = `AI 신문 발간에 실패했습니다: ${message}`
      }
    } else if (error.request) {
      errorMessage = 'AI 신문 발간에 실패했습니다: 서버와 연결할 수 없습니다.'
    }
    
    alert(errorMessage)
  } finally {
    // 발간 중 상태 제거
    publishingIds.value = publishingIds.value.filter(id => id !== news.id)
  }
}

// 신문 보기
const viewNews = (newsId) => {
  router.push(`/news/${newsId}`)
}

// 페이지 변경
const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

onMounted(() => {
  fetchNews()
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

.publish-btn {
  background-color: #10b981;
  color: white;
}

.publish-btn:hover:not(:disabled) {
  background-color: #059669;
  transform: translateY(-1px);
}

.publish-btn:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
  transform: none;
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