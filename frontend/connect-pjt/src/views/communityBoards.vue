<template>
  <div class="container">
    <h1 class="title">
      게시판
      <RouterLink to="/boards/write" class="write-button">글쓰기</RouterLink>
    </h1>

    <!-- 카테고리 필터 -->
    <div class="category-buttons">
      <button
        v-for="category in categories"
        :key="category"
        @click="selectedCategory = category"
        :class="['category-button', { active: selectedCategory === category }]"
      >
        {{ category }}
      </button>
    </div>

    <!-- 검색창 -->
    <div class="search-box">
      <input
        type="text"
        v-model="searchQuery"
        placeholder="게시글 검색"
        class="search-input"
      />
    </div>

    <!-- 게시글 리스트 -->
    <div class="notice-list">
      <div
        class="notice-item"
        v-for="(item, index) in filteredNotices"
        :key="index"
        @click="goToDetail(item.boardId)"
      >
        <div class="notice-left">
          <span class="badge">{{ item.category }}</span>
          <div class="notice-text">
            <div class="notice-title">{{ item.content }}</div>
            <div class="notice-meta">작성자: {{ item.nickname }}</div>
          </div>
        </div>
        <div class="notice-right">
          <div class="notice-date">{{ item.createdAt }}</div>
          <div class="notice-likes">👍 {{ item.likeCount }}</div>
        </div>
      </div>
    </div>

    <!-- 검색 결과 없음 메시지 -->
    <div v-if="searched && filteredNotices.length === 0" class="no-result">검색 결과가 없습니다.</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()

// 카테고리 옵션 (여기에 실제 API 카테고리를 추가)
const categories = ['전체', 'CHAT', 'SHARE', 'INFO', 'HOBBY']  
const selectedCategory = ref('전체')  // 기본값은 '전체'
const searchQuery = ref('')
const searched = ref(false)  // 검색 여부 표시

// 게시글 데이터 (API 호출을 통해 가져옴)
const notices = ref([])

// 게시글 데이터 가져오기
const fetchNotices = async () => {
  try {
    const res = await api.get('/api/v1/boards', {
      params: { category: selectedCategory.value } // 카테고리 필터링
    })
    notices.value = res.data
    searched.value = true
  } catch (error) {
    console.error('게시글 로드 실패:', error)
    searched.value = true
    notices.value = []
  }
}

// API에서 받아온 데이터 필터링 (카테고리와 검색어 기반)
const filteredNotices = computed(() => {
  return notices.value.filter((n) => {
    const matchCategory = selectedCategory.value === '전체' || n.category === selectedCategory.value
    const matchSearch = n.content.includes(searchQuery.value)  // 제목이나 내용에서 검색어 찾기
    return matchCategory && matchSearch
  })
})

// 게시글 상세 페이지로 이동
const goToDetail = (boardId) => {
  router.push(`/boards/${boardId}`)
}

// 컴포넌트 마운트 시 API 호출
onMounted(() => {
  fetchNotices()
})
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
  font-family: 'Noto Sans KR', sans-serif;
}

.title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.write-button {
  font-size: 14px;
  background-color: #284cea;
  color: white;
  padding: 6px 14px;
  border-radius: 8px;
  text-decoration: none;
}

.category-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.category-button {
  padding: 6px 14px;
  border: 1px solid #ccc;
  border-radius: 9999px;
  font-size: 14px;
  background-color: #fff;
  cursor: pointer;
  color: #555;
  transition: background-color 0.2s, color 0.2s;
}

.category-button.active {
  background-color: #000;
  color: #fff;
  border-color: #000;
}

.search-box {
  margin-bottom: 16px;
}

.search-input {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 8px;
}

.notice-list {
  border: 1px solid #ddd;
  border-radius: 10px;
  overflow: hidden;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 14px 16px;
  font-size: 14px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-item:hover {
  background-color: #f9f9f9;
  cursor: pointer;
}

.notice-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.badge {
  background-color: #10b981;
  color: white;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 9999px;
  white-space: nowrap;
}

.notice-text {
  display: flex;
  flex-direction: column;
}

.notice-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.notice-meta {
  font-size: 12px;
  color: #777;
}

.notice-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.notice-date {
  color: #888;
  font-size: 13px;
}

.notice-likes {
  font-size: 13px;
  color: #284cea;
}
</style>
