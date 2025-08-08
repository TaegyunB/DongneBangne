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
              :disabled="publishingIds.includes(news.id)"
              class="action-btn publish-btn"
            >
              {{ publishingIds.includes(news.id) ? '발간 중...' : '발간하기' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination">
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
const seniorCenterName = ref('싸피 경로당')
const publishingIds = ref([])
const currentPage = ref(1)
const itemsPerPage = 10

// 더미 데이터 로드
const fetchNews = async () => {
  loading.value = true
  try {
    // 더미 데이터
    const dummyData = [
      {
        id: 7,
        newsTitle: "강일리버파크2단지, 무더위를 이기는 어르신들의 활기찬 7월 이야기",
        newsContent: null,
        year: 2025,
        month: 7,
        pdfUrl: "https://s13p11a708.s3.us-east-2.amazonaws.com/ai-news/강일리버파크2단지_2025_7_1.pdf",
        seniorCenterName: "강일리버파크2단지",
        challenges: [],
        createdAt: "2025-07-31T14:20:00"
      },
      {
        id: 6,
        newsTitle: "강일리버파크2단지, 초여름을 맞이한 어르신들의 활동 현황",
        newsContent: null,
        year: 2025,
        month: 6,
        pdfUrl: "https://s13p11a708.s3.us-east-2.amazonaws.com/ai-news/강일리버파크2단지_2025_6_1.pdf",
        seniorCenterName: "강일리버파크2단지",
        challenges: [],
        createdAt: "2025-06-30T13:30:45"
      },
      {
        id: 5,
        newsTitle: "강일리버파크2단지, 봄의 마지막을 장식한 어르신들의 5월 이야기",
        newsContent: null,
        year: 2025,
        month: 5,
        pdfUrl: "https://s13p11a708.s3.us-east-2.amazonaws.com/ai-news/강일리버파크2단지_2025_5_1.pdf",
        seniorCenterName: "강일리버파크2단지",
        challenges: [],
        createdAt: "2025-05-31T17:10:22"
      },
      {
        id: 4,
        newsTitle: "강일리버파크2단지, 어르신들과 함께한 행복한 여름의 추억",
        newsContent: null,
        year: 2025,
        month: 4,
        pdfUrl: null, // 발간 안됨
        seniorCenterName: "강일리버파크2단지",
        challenges: [
          {id: 1, challengeTitle: "백숙먹기", challengePlace: "경로당", description: "다같이 백숙먹기"},
          {id: 2, challengeTitle: "산책하기", challengePlace: "경로당", description: "다같이 산책하기"},
          {id: 3, challengeTitle: "책 읽기", challengePlace: "경로당", description: "더운데 다같이 모여서 책 읽기"}
        ],
        createdAt: "2025-04-30T16:28:02"
      },
      {
        id: 3,
        newsTitle: "강일리버파크2단지, 봄의 새싹과 함께한 3월 이야기",
        newsContent: null,
        year: 2025,
        month: 3,
        pdfUrl: null, // 발간 안됨
        seniorCenterName: "강일리버파크2단지",
        challenges: [],
        createdAt: "2025-03-31T16:22:33"
      },
      {
        id: 2,
        newsTitle: "강일리버파크2단지, 무더위를 이기는 어르신들의 활기찬 7월 이야기",
        newsContent: null,
        year: 2025,
        month: 7,
        pdfUrl: "https://s13p11a708.s3.us-east-2.amazonaws.com/ai-news/강일리버파크2단지_2025_7_1.pdf",
        seniorCenterName: "강일리버파크2단지",
        challenges: [],
        createdAt: "2025-07-31T14:20:00"
      },
      {
        id: 1,
        newsTitle: "강일리버파크2단지, 초여름을 맞이한 어르신들의 활동 현황",
        newsContent: null,
        year: 2025,
        month: 6,
        pdfUrl: "https://s13p11a708.s3.us-east-2.amazonaws.com/ai-news/강일리버파크2단지_2025_6_1.pdf",
        seniorCenterName: "강일리버파크2단지",
        challenges: [],
        createdAt: "2025-06-30T13:30:45"
      },
      {
        id: 8,
        newsTitle: "강일리버파크2단지, 봄의 마지막을 장식한 어르신들의 5월 이야기",
        newsContent: null,
        year: 2025,
        month: 5,
        pdfUrl: "https://s13p11a708.s3.us-east-2.amazonaws.com/ai-news/강일리버파크2단지_2025_5_1.pdf",
        seniorCenterName: "강일리버파크2단지",
        challenges: [],
        createdAt: "2025-05-31T17:10:22"
      },
      {
        id: 9,
        newsTitle: "강일리버파크2단지, 어르신들과 함께한 행복한 여름의 추억",
        newsContent: null,
        year: 2025,
        month: 4,
        pdfUrl: null, // 발간 안됨
        seniorCenterName: "강일리버파크2단지",
        challenges: [
          {id: 1, challengeTitle: "백숙먹기", challengePlace: "경로당", description: "다같이 백숙먹기"},
          {id: 2, challengeTitle: "산책하기", challengePlace: "경로당", description: "다같이 산책하기"},
          {id: 3, challengeTitle: "책 읽기", challengePlace: "경로당", description: "더운데 다같이 모여서 책 읽기"}
        ],
        createdAt: "2025-04-30T16:28:02"
      },
      {
        id: 10,
        newsTitle: "강일리버파크2단지, 봄의 새싹과 함께한 3월 이야기",
        newsContent: null,
        year: 2025,
        month: 3,
        pdfUrl: null, // 발간 안됨
        seniorCenterName: "강일리버파크2단지",
        challenges: [],
        createdAt: "2025-03-31T16:22:33"
      },
      {
        id: 11,
        newsTitle: "강일리버파크2단지, 봄의 새싹과 함께한 3월 이야기",
        newsContent: null,
        year: 2025,
        month: 3,
        pdfUrl: null, // 발간 안됨
        seniorCenterName: "강일리버파크2단지",
        challenges: [],
        createdAt: "2025-03-31T16:22:33"
      }
    ]
    
    newsList.value = dummyData
    seniorCenterName.value = dummyData[0].seniorCenterName
    
  } catch (error) {
    console.error('신문 목록을 불러오는데 실패했습니다:', error)
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

// 신문 설명 생성
const getNewsDescription = (news) => {
  if (news.challenges && news.challenges.length > 0) {
    return `${news.challenges.map(c => c.challengeTitle).join(', ')} 수업을 하며 하하호호 추억을 나누...`
  }
  return '다 같이 근처 계곡으로 놀러가..'
}

// 해당 월의 마지막 날 구하기
const getLastDay = (year, month) => {
  return new Date(year, month, 0).getDate()
}

// 신문 발간하기
const publishNews = async (news) => {
  publishingIds.value.push(news.id)
  
  try {
    // API 호출
    const response = await axios.post('/api/v1/admin/ai-news', {
      newsId: news.id,
      year: news.year,
      month: news.month
    })
    
    // 성공시 pdfUrl 업데이트
    const newsIndex = newsList.value.findIndex(n => n.id === news.id)
    if (newsIndex !== -1) {
      newsList.value[newsIndex] = {
        ...newsList.value[newsIndex],
        ...response.data
      }
    }
    
    console.log('신문 발간 완료:', response.data)
    
  } catch (error) {
    console.error('신문 발간에 실패했습니다:', error)
    
    // 에러 발생시 임시로 성공 처리 (개발용)
    setTimeout(() => {
      const newsIndex = newsList.value.findIndex(n => n.id === news.id)
      if (newsIndex !== -1) {
        newsList.value[newsIndex].pdfUrl = `https://s13p11a708.s3.us-east-2.amazonaws.com/ai-news/강일리버파크2단지_${news.year}_${news.month}.pdf`
      }
    }, 2000)
    
  } finally {
    setTimeout(() => {
      publishingIds.value = publishingIds.value.filter(id => id !== news.id)
    }, 2000)
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