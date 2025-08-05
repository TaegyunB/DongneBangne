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

    <!-- 공지 리스트 -->
    <div class="notice-list">
      <div
        class="notice-item"
        v-for="(item, index) in filteredNotices"
        :key="index"
        @click="goToDetail(index)"
      >
        <div class="notice-left">
          <span class="badge">{{ item.category }}</span>
          <div class="notice-text">
            <div class="notice-title">{{ item.title }}</div>
            <div class="notice-meta">작성자: {{ item.author }}</div>
          </div>
        </div>
        <div class="notice-right">
          <div class="notice-date">{{ item.date }}</div>
          <div class="notice-likes">👍 {{ item.likes }}</div>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const categories = ['전체', '인기', '잡담', '나눔', '취미', '정보']
const selectedCategory = ref('전체')
const searchQuery = ref('')

// 더미 데이터
const notices = ref([
  {
    id: 1,
    title: '경로당 요가 프로그램 후기',
    date: '2025-08-01',
    category: '취미',
    author: '홍길동',
    likes: 12,
  },
  {
    id: 2,
    title: '송파구 무료 검진 정보 공유',
    date: '2025-07-30',
    category: '정보',
    author: '김철수',
    likes: 8,
  },
  {
    id: 3,
    title: '반찬 나눔 행사 열려요',
    date: '2025-08-02',
    category: '나눔',
    author: '이영희',
    likes: 17,
  },
  {
    id: 4,
    title: '웃긴 이야기 하나!',
    date: '2025-08-03',
    category: '잡담',
    author: '박명수',
    likes: 5,
  },
  {
    id: 5,
    title: '이번 주 인기 게시글입니다',
    date: '2025-08-04',
    category: '인기',
    author: 'admin',
    likes: 33,
  },
])

// 필터링된 게시글
const filteredNotices = computed(() => {
  return notices.value.filter((n) => {
    const matchCategory = selectedCategory.value === '전체' || n.category === selectedCategory.value
    const matchSearch = n.title.includes(searchQuery.value)
    return matchCategory && matchSearch
  })
})

// 상세 페이지 이동
const goToDetail = (id) => {
  router.push(`/boards/${id}`)
}
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