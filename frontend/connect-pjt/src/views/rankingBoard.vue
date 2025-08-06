<template>
  <div class="container">
    <h1 class="title">경로당 순위</h1>

    
    <!-- 검색창 추가 -->
    <div class="search-bar">
      <input
        type="text"
        v-model="searchQuery"
        placeholder="경로당 이름을 입력하세요"
        class="search-input"
      />
    </div>

    <table class="ranking-table">
      <thead>
        <tr>
          <th>순위</th>
          <th class="text-left">경로당 이름</th>
          <th>도전 현황</th>
          <th>트로트 맞추기 포인트</th>
          <th>도전 포인트</th>
          <th class="text-blue">월간 포인트</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="centers.length === 0">
          <td colspan="6" class="empty">데이터가 없습니다.</td>
        </tr>
        <tr v-for="(center, index) in paginatedCenters" :key="center.id">
          <td>{{ index + 1 + (currentPage - 1) * pageSize }}</td>
          <td class="text-left">
            <div class="center-name">
              <img src="@/assets/logo.png" class="logo" />
              <span>{{ center.name }}</span>
            </div>
          </td>

          <!-- 도전 현황 -->
          <td>
            <div class="status-box with-arrow">
              <span
                v-for="(status, idx) in center.challengeStatuses"
                :key="idx"
                :class="status === 'success' ? 'status success' : 'status fail'"
              >
                {{ status === 'success' ? '✓' : '✕' }}
              </span>
              <span class="arrow">»</span>
              <span class="describe-text">다음 버튼을 클릭 시 해당 경로당의 도전 현황을 열람 가능하세요!</span>
            </div>
          </td>

          <!-- 트로트 맞추기 포인트 -->
          <td>{{ center.trotPoint.toLocaleString() }}</td>

          <!-- 도전 포인트 -->
          <td>{{ center.missionPoint.toLocaleString() }}</td>

          <!-- 월간 포인트 -->
          <td class="text-blue bold">{{ center.monthlyPoint.toLocaleString() }}</td>
        </tr>
      </tbody>


    </table>

    <!-- Pagination -->
    <div class="pagination">
      <button @click="goToPage(1)" :disabled="currentPage === 1">«</button>
      <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1">‹</button>

      <button
        v-for="page in visiblePages"
        :key="page"
        @click="goToPage(page)"
        :class="['page-button', { active: currentPage === page }]"
      >
        {{ page }}
      </button>

      <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages">›</button>
      <button @click="goToPage(totalPages)" :disabled="currentPage === totalPages">»</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

const centers = ref([])
const currentPage = ref(1)
const pageSize = 10
const totalPages = ref(1)
const searchQuery = ref('')

// ✅ 검색된 센터만 반환
const filteredCenters = computed(() => {
  if (!searchQuery.value.trim()) return centers.value
  return centers.value.filter((center) =>
    center.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// ✅ 페이지네이션 적용된 센터 목록
const paginatedCenters = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredCenters.value.slice(start, end)
})

// ✅ 페이지 버튼 목록
const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = start + maxVisible - 1

  if (end > totalPages.value) {
    end = totalPages.value
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// ✅ 페이지 이동
const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}

// ✅ 페이지 수 계산 (필터링 결과 기준)
watch(filteredCenters, (filtered) => {
  totalPages.value = Math.ceil(filtered.length / pageSize)
  currentPage.value = 1
})

// ✅ JSON 불러오기 및 정제
onMounted(async () => {
  try {
    const response = await fetch('/dummy_centers.json')
    if (!response.ok) throw new Error('JSON 파일을 불러올 수 없습니다.')

    const data = await response.json()

    const normalized = data.map((center) => ({
      ...center,
      challengeStatuses: center.challengeStatuses.map((s) =>
        s === 'yes' ? 'success' : 'fail'
      )
    }))

    normalized.sort((a, b) => b.monthlyPoint - a.monthlyPoint)

    centers.value = normalized
  } catch (error) {
    console.error('데이터 로드 실패:', error)
  }
})
</script>


<style scoped>
.container {
  padding: 32px;
}
.title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 24px;
}
.ranking-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  text-align: center;
}
.ranking-table th,
.ranking-table td {
  border: 1px solid #ddd;
  padding: 10px;
  min-width: 100px;
}
.ranking-table th.text-left,
.ranking-table td.text-left {
  text-align: left;
}
.ranking-table th.text-blue,
.ranking-table td.text-blue {
  color: #007bff;
}
.bold {
  font-weight: 600;
}
.empty {
  padding: 20px;
  color: #999;
  text-align: center;
}
.center-name {
  display: flex;
  align-items: center;
  gap: 8px;
}
.logo {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}
.arrow {
  margin-left: auto;
  color: #ccc;
}
.status-box {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  justify-items: center;
}

/* .status-box.with-arrow {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* .status-box.with-arrow .arrow {
  color: #aaa;
  font-size: 14px;
  margin-left: 4px;
} */ 

.status {
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 50%;
  font-size: 13px;
  font-weight: bold;
  color: white;
}
.success {
  background-color: #28a745;
}
.fail {
  background-color: #dc3545;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 6px;
}
.page-button {
  padding: 6px 10px;
  border: 1px solid #ccc;
  background-color: white;
  cursor: pointer;
}
.page-button.active {
  background-color: #007bff;
  color: white;
  font-weight: bold;
  border-color: #007bff;
}
.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 6px;
  align-items: center;
}
.page-button,
.pagination button {
  padding: 6px 10px;
  border: 1px solid #ccc;
  background-color: white;
  cursor: pointer;
}
.page-button.active {
  background-color: #007bff;
  color: white;
  font-weight: bold;
  border-color: #007bff;
}
.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ✅ 검색창 스타일 */
.search-bar {
  margin-bottom: 16px;
  text-align: right;
}
.search-input {
  padding: 6px 10px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
</style>
