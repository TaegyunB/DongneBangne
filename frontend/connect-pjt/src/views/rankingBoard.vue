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

    <!-- 테이블 -->
    <table class="ranking-table">
      <thead>
        <tr>
          <th class="text-center">순위</th>
          <th class="text-left">경로당 이름</th>
          <th class="text-center">도전 현황</th>
          <th class="text-center">트로트 포인트</th>
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
              <span>{{ center.centerName }}</span>
            </div>
          </td>
          <td>
            <div class="status-box with-arrow">
              <span
                v-for="(status, idx) in center.challengeStatuses"
                :key="idx"
                :class="status === 'success' ? 'status success' : 'status fail'"
              >
                {{ status === 'success' ? '✓' : '✕' }}
              </span>
              <div class="challenge-info-btn" @click="openModal(center.id)">
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

    <!-- 페이지네이션 -->
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
      <div class="modal-content challenge-modal">
        <h2>{{ selectedCenter.seniorCenterName }} 도전</h2>

        <div class="challenge-grid">
          <div v-for="(m, idx) in modalChallenges" :key="'slot-' + (idx+1)" class="challenge-card">
            <template v-if="m">
              <div class="image-placeholder">
                <img v-if="m.challengeImage" :src="m.challengeImage" class="challenge-img" alt="미션 이미지" />
              </div>
              <div class="card-text">
                <h3 class="card-title">
                  {{ m.challengeTitle }}
                  <span :class="m.isSuccess ? 'check-icon' : 'fail-icon'">
                    {{ m.isSuccess ? '✅' : '❌' }}
                  </span>
                </h3>
                <p class="card-description">{{ truncateText(m.description) }}</p>
                <p class="card-subtext">📍 {{ m.challengePlace }}</p>
                <p class="card-point">💎 {{ m.point }}점</p>
                <button class="more-info" @click="openDetailModal(m.id)">더보기 →</button>
              </div>
            </template>
            <template v-else>
              <div class="image-placeholder"><span style="color:#999">🕳</span></div>
              <div class="card-text">
                <h3 class="card-title">미션이 등록되지 않았습니다</h3>
              </div>
            </template>
          </div>
        </div>
    <div class="modal-overlay" v-if="showDetailModal" @click.self="closeDetailModal">
      <div class="modal-content">
        <h2>도전 상세</h2>
        <div v-if="selectedChallenge">
          <div class="detail-body">
            <div class="image-placeholder" v-if="selectedChallenge.image">
              <img :src="selectedChallenge.image" class="challenge-img" alt="상세 이미지" />
            </div>
            <h3 class="card-title" style="margin-top:12px">
              {{ selectedChallenge.title }}
              <span :class="selectedChallenge.isSuccess ? 'check-icon' : 'fail-icon'">
                {{ selectedChallenge.isSuccess ? '✅' : '❌' }}
              </span>
            </h3>
            <p class="card-description" style="white-space:pre-line">{{ selectedChallenge.description }}</p>
            <p class="card-subtext">📍 {{ selectedChallenge.place }}</p>
            <p class="card-point">💎 {{ selectedChallenge.point }}점</p>
          </div>

          <div class="detail-actions">
            <button class="report-btn" @click="reportChallenge">신고하기</button>
            <button class="close-btn" @click="closeDetailModal">닫기</button>
          </div>
        </div>
      </div>
    </div>

        <button class="close-btn" @click="closeModal">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '@/api/axios'

const centers = ref([])
const currentPage = ref(1)
const pageSize = 10
const totalPages = ref(1)
const searchQuery = ref('')
const selectedCenter = ref(null)
const showDetailModal = ref(false)
const selectedChallenge = ref(null)

const sortChallenges = list =>
  [...(list || [])].sort((a, b) => {
    if ((a.year ?? 0) !== (b.year ?? 0)) return (a.year ?? 0) - (b.year ?? 0)
    if ((a.month ?? 0) !== (b.month ?? 0)) return (a.month ?? 0) - (b.month ?? 0)
    return (a.id ?? 0) - (b.id ?? 0)
  })

const filteredCenters = computed(() => {
  if (!searchQuery.value.trim()) return centers.value
  return centers.value.filter((center) =>
    center.centerName.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const paginatedCenters = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredCenters.value.slice(start, end)
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = start + maxVisible - 1
  if (end > totalPages.value) {
    end = totalPages.value
    start = Math.max(1, end - maxVisible + 1)
  }
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}

// const normalizeChallenges = (challenges) => {
//   const result = []
//   for (let id = 1; id <= 4; id++) {
//     const found = challenges.find(c => c.id === id)
//     result.push(found || null)
//   }
//   return result
// }

const normalizeChallenges = (challenges) => {
  const list = Array.isArray(challenges) ? challenges : []
  return sortChallenges(list)
    .map(c => ({
      id: c.id ?? c.challengeId ?? c.challenge_id ?? c.slot ?? null,
      challengeImage: c.challengeImage ?? c.image ?? null,
      challengeTitle: c.challengeTitle ?? c.title ?? '',
      description: c.description ?? '',
      challengePlace: c.challengePlace ?? c.place ?? '',
      point: c.point ?? 0,
      isSuccess: c.isSuccess ?? c.is_success ?? false
    }))
    .filter(c => c.id != null) // null 제거
}

const openModal = async (centerId) => {
  try {
    const res = await api.get(`/api/v1/rankings/senior-center/${centerId}/challenges`)
    const data = res.data
    // selectedCenter.value = {
    //   ...data,
    //   challenges: normalizeChallenges(data.challenges)
    // }

    const centerInList = centers.value.find(c => c.id === centerId)
    selectedCenter.value = {
      seniorCenterId: data.seniorCenterId ?? centerId,
      seniorCenterName: data.seniorCenterName ?? centerInList?.centerName ?? '',
      challenges: normalizeChallenges(data.challenges)
    }

  } catch (err) {
    console.error('도전 미션 로딩 실패:', err)
  }
}

// 모달에서 표시할 4칸: 데이터 0~4개 + 나머지는 null(미등록)
const modalChallenges = computed(() => {
  const list = selectedCenter.value?.challenges ?? []
  const out = [...list.slice(0, 4)]
  while (out.length < 4) out.push(null)
  return out
})

const closeModal = () => {
  selectedCenter.value = null
}

watch(filteredCenters, (filtered) => {
  totalPages.value = Math.ceil(filtered.length / pageSize)
  currentPage.value = 1
})

onMounted(async () => {
  try {
    const response = await api.get('/api/v1/rankings')
    const data = response.data
    const normalized = data.map((center) => ({
      id: center.seniorCenterId,
      centerName: center.seniorCenterName,
      trotPoint: center.trotPoint,
      missionPoint: center.challengePoint,
      monthlyPoint: center.totalPoint,
      challenges: center.challenges,
      // challengeStatuses: center.challenges.slice(0, 4).map((c) =>
      //   c.isSuccess ? 'success' : 'fail'
      // )
      challengeStatuses: (center.challenges ?? []).slice(0, 4).map(c =>
        (c.isSuccess ?? c.is_success) ? 'success' : 'fail'
      )
    }))
    normalized.sort((a, b) => b.monthlyPoint - a.monthlyPoint)
    centers.value = normalized
  } catch (error) {
    console.error('API 호출 실패:', error)
  }
})

// const getChallengeById = (id) => {
//   return selectedCenter.value?.challenges?.find(c => c.id === id)
// }

// const getChallengeById = (id) => {
//   const arr = selectedCenter.value?.challenges || []
//   return arr.find(c => c && c.id === id) || null
// }

const truncateText = (text, maxLength = 30) => {
  if (!text) return ''
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}

// const openDetailModal = async (challengeId) => {
//   const centerId = selectedCenter.value?.seniorCenterId
//   if (!centerId) return

//   try {
//     const res = await api.get(`/api/v1/rankings/senior-center/${centerId}/challenges/${challengeId}`)
//     const challenge = res.data
//     // 모달 띄우는 로직 구현 위치
//     console.log('✅ 상세 미션:', challenge)
//     // 예: 상세 모달 상태로 따로 띄우거나, selectedChallenge.value = challenge;
//   } catch (err) {
//     console.error('상세 미션 불러오기 실패:', err)
//   }
// }

const openDetailModal = async (challengePk) => {
  const centerId = selectedCenter.value?.seniorCenterId
  if (!centerId || !challengePk) return
  try {
    const { data } = await api.get(`/api/v1/rankings/senior-center/${centerId}/challenges/${challengePk}`)
    // 필요한 필드만 통일
    selectedChallenge.value = {
      id: data.id ?? data.challengeId,
      title: data.challengeTitle ?? data.title ?? '',
      description: data.description ?? '',
      place: data.challengePlace ?? data.place ?? '',
      point: data.point ?? 0,
      image: data.challengeImage ?? data.image ?? null,
      isSuccess: data.isSuccess ?? data.is_success ?? false,
      year: data.year, month: data.month
    }
    showDetailModal.value = true
  } catch (err) {
    console.error('상세 미션 불러오기 실패:', err)
  }
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedChallenge.value = null
}

// 신고하기(페이로드 확정 필요)
const reportChallenge = async () => {
  if (!selectedCenter.value?.seniorCenterId || !selectedChallenge.value?.id) return
  if (!confirm('해당 인증을 신고하시겠습니까?')) return
  try {
    // ⚠️ 백엔드 요구 스펙 확인 필요: body에 {challengeId, reason} 등?
    await api.post(`/api/v1/rankings/${selectedCenter.value.seniorCenterId}`, {
      challengeId: selectedChallenge.value.id
    })
    alert('신고가 접수되었습니다.')
    closeDetailModal()
  } catch (e) {
    console.error('신고 실패:', e)
    alert('신고에 실패했습니다.')
  }
}
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
.ranking-table th,
.ranking-table td {
  padding: 14px;
  text-align: center;
  border-bottom: 1px solid #eee;
  font-variant-numeric: tabular-nums;
}
.ranking-table tr:hover {
  background-color: #f8f9fa;
}
.ranking-table td.text-blue {
  color: #007bff;
  font-weight: normal;
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
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  max-width: 1000px;
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

.challenge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); /* 반응형 대응 */
  gap: 16px;
  margin: 20px 0;
}

.challenge-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: left;
  position: relative;
}
.image-placeholder {
  width: 100%;
  aspect-ratio: 1 / 1;
  background: #dfe3e6;
  border-radius: 8px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.challenge-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
  border-radius: 8px;
}
.card-title {
  font-size: 18px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-description {
  font-size: 14px;
  margin: 6px 0;
  color: #444;
}
.card-subtext, .card-point {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
}
.check-icon {
  color: green;
}
.fail-icon {
  color: red;
}
.no-mission-text {
  text-align: center;
  color: #999;
  font-size: 16px;
  margin: 30px 0;
}

/* ── 카드 내부 텍스트 영역을 컬럼으로, '더보기'를 우하단으로 ───────────────── */
.card-text {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.more-info {
  margin-top: auto;          /* 내용 밑으로 밀기 */
  align-self: flex-end;      /* 우측 정렬 */
  background: transparent;
  border: none;
  cursor: pointer;
  color: #007bff;
  font-weight: 600;
  padding: 0;
}
.more-info:hover { text-decoration: underline; }

/* ── 상세 모달 액션 버튼 우하단 정렬 ───────────────────────────────────────── */
.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
.report-btn {
  background: #dc3545;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}
.report-btn:hover { filter: brightness(0.95); }

/* ── 모달 스크롤/사이즈 보강 ──────────────────────────────────────────────── */
.modal-overlay .modal-content {
  max-height: 80vh;     /* 긴 내용이면 내부 스크롤 */
  overflow-y: auto;
}

/* 상세 모달이(두 번째 모달) 첫 모달 위로 올라오도록 z-index 살짝 높임
   (현재 상세 모달이 첫 모달 안쪽에 렌더되어도 확실히 위에 보이게) */
.challenge-modal .modal-overlay {
  z-index: 1001;
}

/* 선택: 상세 모달 폭을 조금 좁게 (원하면 유지하세요) */
.challenge-modal .modal-overlay .modal-content {
  max-width: 720px;
}

/* ── 카드/그리드 자잘한 간격 다듬기(선택) ───────────────────────────────── */
.challenge-grid { gap: 18px; }
.card-title { margin-bottom: 4px; }
.card-point { font-weight: 600; }
</style>
