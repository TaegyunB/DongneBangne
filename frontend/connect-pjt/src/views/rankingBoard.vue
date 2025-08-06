<template>
  <div class="container">
    <h1 class="title">경로당 순위</h1>

    <table class="ranking-table">
      <thead>
        <tr>
          <th>순위</th>
          <th class="text-left">경로당 이름</th>
          <th>트로트 맞추기 포인트</th>
          <th>도전 현황</th>
          <th class="text-blue">월간 포인트</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="centers.length === 0">
          <td colspan="5" class="empty">데이터가 없습니다.</td>
        </tr>
        <tr v-for="(center, index) in paginatedCenters" :key="center.id">
          <td>{{ index + 1 + (currentPage - 1) * pageSize }}</td>
          <td class="text-left">
            <div class="center-name">
              <img src="@/assets/logo.png" class="logo" />
              <span>{{ center.name }}</span>
              <span class="arrow">»</span>
            </div>
          </td>
          <td>{{ center.trotPoint }}</td>
          <td>
            <div class="status-box">
              <span
                v-for="(status, idx) in center.challengeStatuses"
                :key="idx"
                :class="status === 'success' ? 'status success' : 'status fail'"
              >
                {{ status === 'success' ? '✓' : '✕' }}
              </span>
            </div>
          </td>
          <td class="text-blue bold">{{ center.monthlyPoint.toLocaleString() }}</td>
        </tr>
      </tbody>
    </table>

    <<!-- Pagination -->
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
import { ref, computed, onMounted } from 'vue'

const centers = ref([])
const currentPage = ref(1)
const pageSize = 10
const totalPages = ref(1)

const paginatedCenters = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return centers.value.slice(start, end)
})

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}

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

onMounted(() => {
  const dummy = []
  for (let i = 1; i <= 35; i++) {
    dummy.push({
      id: i,
      name: `SSAFY 경로당`,
      trotPoint: 100,
      monthlyPoint: 2000,
      challengeStatuses: [
        i % 2 === 0 ? 'success' : 'fail',
        i % 3 === 0 ? 'success' : 'fail',
        i % 4 === 0 ? 'success' : 'fail',
        i % 5 === 0 ? 'success' : 'fail'
      ]
    })
  }
  centers.value = dummy
  totalPages.value = Math.ceil(dummy.length / pageSize)
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

</style>
