<template>
  <div class="container">
    <h1 class="title">경로당 순위</h1>

    <!-- 검색창 -->
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
          <th class="text-center">순위</th>
          <th class="text-left">경로당 이름</th>
          <th class="text-center">도전 현황</th>
          <th class="text-center">트로트 맞추기 포인트</th>
          <th class="text-center">도전 포인트</th>
          <th class="text-blue text-center">월간 포인트</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="paginatedCenters.length === 0">
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
              <div class="challenge-info-btn" @click="openModal(center)">
                <span class="arrow">»</span>
                <span class="describe-text">도전 미션 현황 보기</span>
              </div>
            </div>
          </td>

          <td class="text-center">{{ center.trotPoint.toLocaleString() }}</td>
          <td class="text-center">{{ center.missionPoint.toLocaleString() }}</td>
          <td class="text-blue text-center">{{ center.monthlyPoint.toLocaleString() }}</td>
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

    <!-- 모달 -->
    <div class="modal-overlay" v-if="selectedCenter" @click.self="closeModal">
      <div class="modal-content">
        <h3>{{ selectedCenter.name }}</h3>
        <p>이곳에 도전 미션 요약 정보가 들어갈 예정입니다.</p>
        <button class="close-btn" @click="closeModal">닫기</button>
      </div>
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

// 검색된 센터만 반환
const filteredCenters = computed(() => {
  if (!searchQuery.value.trim()) return centers.value
  return centers.value.filter((center) =>
    center.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 페이지네이션 적용된 센터 목록
const paginatedCenters = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredCenters.value.slice(start, end)
})

// 페이지 버튼 목록
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

const selectedCenter = ref(null)

const openModal = (center) => {
  selectedCenter.value = center
}

const closeModal = () => {
  selectedCenter.value = null
}

// 페이지 이동
const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}

// 페이지 수 계산 (필터링 결과 기준)
watch(filteredCenters, (filtered) => {
  totalPages.value = Math.ceil(filtered.length / pageSize)
  currentPage.value = 1
})

// JSON 불러오기 및 정제
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
  max-width: 1400px;
  margin: 0 auto;
}
.title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 24px;
}
.search-bar {
  margin-bottom: 16px;
  text-align: right;
}
.search-input {
  padding: 8px 12px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

/* 테이블 스타일 */
.ranking-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.05);
  font-size: 14px;
}
.ranking-table thead {
  background-color: #f9fafb;
}
.ranking-table th {
  padding: 14px;
  font-weight: 600;
  text-align: center;
  border-bottom: 1px solid #eee;
}
/* 테이블 내 여백을 균일하게 유지 */
.ranking-table td {
  padding: 12px 16px;
}
.ranking-table tr:hover {
  background-color: #f8f9fa;
}

.ranking-table th.text-center,
.ranking-table td.text-center {
  text-align: center;
  font-variant-numeric: tabular-nums;
}
/* 기존 스타일 수정 */
.ranking-table td.text-blue {
  color: #007bff;
  font-weight: normal;  /* bold 제거 */
}
.ranking-table td:last-child {
  border-right: none;
}

.center-name {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}
.status-box {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  justify-items: center;
}
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
  margin-top: 24px;
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
  font-size: 14px;
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
.empty {
  padding: 20px;
  color: #999;
  text-align: center;
}

.text-right {
  text-align: right;
}
.text-blue {
  color: #007bff;
}
.bold {
  font-weight: bold;
}
.clickable {
  cursor: pointer;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}
.modal-content {
  background: #fff;
  padding: 24px 32px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  max-width: 400px;
  width: 100%;
  text-align: center;
}
.close-btn {
  margin-top: 16px;
  background: #007bff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}
.challenge-info-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  grid-column: span 4;
  cursor: pointer;
  margin-top: 6px;
  color: #555;
  font-size: 12px;
  transition: color 0.2s ease;
}
.challenge-info-btn:hover {
  color: #007bff;
}
.challenge-info-btn .arrow {
  font-size: 14px;
  color: inherit;
}
/* 스타일 일관성 위해 모든 숫자 열에 공통 적용 */
.ranking-table td.text-right {
  font-variant-numeric: tabular-nums; /* 폰트에서 숫자 폭 균등 */
  padding-right: 14px;
}
/* 월간 포인트 강조는 색상만 */
.ranking-table td.text-blue {
  color: #007bff;
  /* font-weight: bold; ← 주석 처리 가능 */
}
</style>
