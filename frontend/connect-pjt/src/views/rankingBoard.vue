<template>
  <div class="px-10 py-8">
    <h1 class="text-3xl font-bold mb-6">경로당 순위</h1>

    <table class="w-full border text-center text-sm">
      <thead class="bg-gray-50">
        <tr>
          <th class="py-3 border">순위</th>
          <th class="py-3 border">경로당 이름</th>
          <th class="py-3 border">트로트 맞추기 포인트</th>
          <th class="py-3 border">도전 현황</th>
          <th class="py-3 border text-blue-600">월간 포인트</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="centers.length === 0">
          <td colspan="5" class="py-4 text-gray-400">데이터가 없습니다.</td>
        </tr>
        <tr v-for="(center, index) in centers" :key="center.id" class="hover:bg-gray-50">
          <td class="py-2 border">{{ index + 1 + (currentPage - 1) * 10 }}</td>
          <td class="py-2 border text-left">
            <div class="flex items-center gap-2 pl-2">
              <img src="@/assets/logo.png" class="w-6 h-6 rounded-full" />
              <span>{{ center.name }}</span>
              <span class="text-gray-400 ml-auto">&#xbb;</span>
            </div>
          </td>
          <td class="py-2 border">{{ center.trotPoint }}</td>
          <td class="py-2 border text-lg">
            <span v-for="(status, idx) in center.challengeStatuses" :key="idx" class="mx-1">
              <span v-if="status === 'success'" class="text-green-600">✔️</span>
              <span v-else-if="status === 'fail'" class="text-red-600">❌</span>
              <span v-else class="text-gray-500">⬇️</span>
            </span>
          </td>
          <td class="py-2 border text-blue-600 font-semibold">{{ center.monthlyPoint.toLocaleString() }}</td>
        </tr>
      </tbody>
    </table>

    <!-- Pagination -->
    <div class="flex justify-center items-center mt-6 gap-2">
      <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1" class="text-gray-600 px-2">Previous</button>
      <button v-for="page in totalPages" :key="page" @click="goToPage(page)"
        :class="['px-3 py-1', currentPage === page ? 'bg-blue-100 text-blue-600 font-bold rounded' : '']">
        {{ page }}
      </button>
      <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages" class="text-gray-600 px-2">Next</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const centers = ref([])
const currentPage = ref(1)
const totalPages = ref(1)

const fetchRanking = async () => {
  try {
    const res = await axios.get(`/api/v1/rankings?page=${currentPage.value}`)
    console.log('API 응답:', res.data)
    const data = res.data

    centers.value = (data.content || []).map(item => ({
      id: item.seniorCenterId,
      name: item.seniorCenterName,
      trotPoint: item.trotPoint,
      monthlyPoint: item.totalPoint,
      challengeStatuses: item.challengeStatuses || []
    }))
    totalPages.value = data.totalPages || 1
  } catch (err) {
    console.error('랭킹 조회 실패:', err)
  }
}

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchRanking()
}

onMounted(fetchRanking)
</script>

<style scoped>
th, td {
  min-width: 100px;
}
</style>