<template>
  <div class="container">
    <!-- 제목 -->
    <h1 class="title">경로당 순위표</h1>

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
          <th class="text-center">경로당 이름</th>
          <th class="text-center narrow">도전 현황</th>
          <th class="text-center">트로트 포인트</th>
          <th class="text-center">도전 포인트</th>
          <th class="text-blue text-center">월간 포인트</th>
        </tr>
      </thead>

      <tbody>
        <!-- 내 경로당: 표 안 첫 줄 + sticky -->
        <tr v-if="myCenter" class="my-center-row" :class="medalClass(myCenterRank)">
          <!-- 순위: Top3 메달(숫자), 그 외 숫자 배지 -->
          <td class="rank-cell">
            <div class="rank-wrap">
              <span v-if="Number(myCenterRank) === 1" class="medal-icon gold"><span class="medal-num">1</span></span>
              <span v-else-if="Number(myCenterRank) === 2" class="medal-icon silver"><span class="medal-num">2</span></span>
              <span v-else-if="Number(myCenterRank) === 3" class="medal-icon bronze"><span class="medal-num">3</span></span>
              <span v-else class="rank-badge">{{ myCenterRank }}</span>
            </div>
          </td>

          <td class="text-center">
            <div class="center-name">
              <img src="@/assets/logo.png" class="logo" />
              <span class="ellipsis">{{ myCenter.centerName }}</span>
              <span class="chip-mycenter">내 경로당</span>
            </div>
          </td>

          <!-- 도전 현황: 4칸 고정(부족분 '?') -->
          <td class="text-center">
            <div class="status-box">
              <span
                v-for="(s, i) in myCenter.challengeStatusesPadded"
                :key="'my-s-'+i"
                :class="['status', s === 'success' ? 'success' : s === 'fail' ? 'fail' : 'unknown']"
              >
                {{ s === 'success' ? '✓' : s === 'fail' ? '✕' : '?' }}
              </span>
            </div>
            <button class="challenge-info-btn" @click="openModal(myCenter.id)">
              <span class="lens" aria-hidden="true">🔍</span>
              <span class="describe-text">도전 미션 현황 보기</span>
            </button>
          </td>

          <td class="text-center">{{ myCenter.trotPoint ?? 0 }}</td>
          <td class="text-center">{{ myCenter.missionPoint ?? 0 }}</td>
          <td class="text-blue text-center">{{ myCenter.monthlyPoint ?? 0 }}</td>
        </tr>

        <!-- 비어있음 표시(내 경로당만 있을 땐 표시하지 않음) -->
        <tr v-if="paginatedCenters.length === 0 && !myCenter">
          <td colspan="6" class="empty">데이터가 없습니다.</td>
        </tr>

        <!-- 목록 행들(내 경로당은 filteredCenters에서 제외됨) -->
        <tr
          v-for="(center, index) in paginatedCenters"
          :key="center.id"
          :class="medalClass(center.ranking ?? (index + 1 + (currentPage - 1) * pageSize))"
        >
          <td class="rank-cell">
            <div class="rank-wrap">
              <span v-if="Number(center.ranking ?? (index + 1 + (currentPage - 1) * pageSize)) === 1" class="medal-icon gold"><span class="medal-num">1</span></span>
              <span v-else-if="Number(center.ranking ?? (index + 1 + (currentPage - 1) * pageSize)) === 2" class="medal-icon silver"><span class="medal-num">2</span></span>
              <span v-else-if="Number(center.ranking ?? (index + 1 + (currentPage - 1) * pageSize)) === 3" class="medal-icon bronze"><span class="medal-num">3</span></span>
              <span v-else class="rank-badge">
                {{ center.ranking ?? (index + 1 + (currentPage - 1) * pageSize) }}
              </span>
            </div>
          </td>

          <td class="text-center">
            <div class="center-name">
              <img src="@/assets/logo.png" class="logo" />
              <span class="ellipsis">{{ center.centerName }}</span>
            </div>
          </td>

          <td class="text-center">
            <div class="status-box">
              <span
                v-for="(status, idx) in center.challengeStatusesPadded"
                :key="'st-'+center.id+'-'+idx"
                :class="[
                  'status',
                  status === 'success' ? 'success' : status === 'fail' ? 'fail' : 'unknown'
                ]"
              >
                {{ status === 'success' ? '✓' : status === 'fail' ? '✕' : '?' }}
              </span>
            </div>
            <button class="challenge-info-btn" @click="openModal(center.id)">
              <span class="lens" aria-hidden="true">🔍</span>
              <span class="describe-text">도전 미션 현황 보기</span>
            </button>
          </td>

          <td class="text-center">{{ center.trotPoint ?? 0 }}</td>
          <td class="text-center">{{ center.missionPoint ?? 0 }}</td>
          <td class="text-blue text-center">{{ center.monthlyPoint ?? 0 }}</td>
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

    <!-- 도전 모달 -->
    <div class="modal-overlay" v-if="selectedCenter" @click.self="closeModal">
      <div class="modal-content challenge-modal">
        <h2>{{ selectedCenter.seniorCenterName }} 도전</h2>

        <div class="challenge-grid">
          <div v-for="(m, idx) in modalChallenges" :key="'slot-' + (idx+1)" class="challenge-card">
            <template v-if="m">
              <div class="image-placeholder">
                <img 
                  v-if="m.challengeImage" 
                  :src="getChallengeImage(m)" 
                  class="challenge-img" 
                  alt="미션 이미지"
                  crossorigin="anonymous"
                  @error="onImageError($event, m)"
                  @load="onImageLoad($event, m)"
                />
                <span v-else style="color:#999">🖼️</span>
              </div>
              <div class="card-text">
                <h3 class="card-title">
                  <span class="card-title-text">{{ m.challengeTitle }}</span>
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

        <button class="close-btn" @click="closeModal">닫기</button>
      </div>
    </div>

    <!-- 상세 모달 -->
    <div class="modal-overlay" v-if="showDetailModal" @click.self="closeDetailModal">
      <div class="modal-content detail-modal">
        <h2>도전 상세</h2>

        <div v-if="selectedChallenge" class="detail-grid">
          <div class="detail-image">
            <div class="image-box" v-if="selectedChallenge.image">
              <img
                :src="getChallengeImageFromSelected(selectedChallenge)"
                class="detail-img"
                alt="상세 이미지"
                crossorigin="anonymous"
                @error="onImageErrorSelected($event, selectedChallenge)"
                @load="onImageLoadSelected($event, selectedChallenge)"
              />
            </div>
            <div class="image-box empty" v-else>🖼️</div>
          </div>

          <div class="detail-info">
            <h3 class="detail-title">
              <span class="detail-title-text">{{ selectedChallenge.title }}</span>
              <span :class="selectedChallenge.isSuccess ? 'check-icon' : 'fail-icon'">
                {{ selectedChallenge.isSuccess ? '✅' : '❌' }}
              </span>
            </h3>

            <p class="detail-desc">{{ selectedChallenge.description }}</p>

            <div class="detail-meta">
              <div class="meta-row">📍 {{ selectedChallenge.place }}</div>
              <div class="meta-row">💎 {{ selectedChallenge.point }}점</div>
              <div class="meta-row" v-if="selectedChallenge.year && selectedChallenge.month">
                🗓 {{ selectedChallenge.year }}년 {{ selectedChallenge.month }}월
              </div>
            </div>

            <div class="detail-actions">
              <button class="close-btn" @click="closeDetailModal">닫기</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '@/api/axios'
import defaultImage from '@/assets/default_image.png'

const centers = ref([])
const currentPage = ref(1)
const pageSize = 10
const totalPages = ref(1)
const searchQuery = ref('')

const selectedCenter = ref(null)
const showDetailModal = ref(false)
const selectedChallenge = ref(null)

/* 내 경로당 ID (스토어/백엔드/로컬 우선) */
const myCenterId = ref(null)

/* 내 경로당 객체 & 순위 */
const myCenter = computed(() =>
  centers.value.find(c => Number(c.id) === Number(myCenterId.value)) || null
)

const myCenterRank = computed(() => {
  if (!myCenter.value) return null
  const r = myCenter.value.ranking
  if (r != null) return Number(r)
  const idx = centers.value.findIndex(c => Number(c.id) === Number(myCenter.value.id))
  return idx >= 0 ? idx + 1 : null
})

/* 로컬에서 소속 센터 ID 로드(임시) */
const fetchMyCenterId = async () => {
  try {
    const saved = localStorage.getItem('mySeniorCenterId')
    if (saved) myCenterId.value = Number(saved)
  } catch (e) {}
}

/* 목록 호출: /api/v1/rankings */
const fetchRankings = async () => {
  try {
    const { data } = await api.get('/api/v1/rankings')
    const normalized = (data ?? []).map(item => {
      const name = (item.seniorCenterName || '').replace(/\uFEFF/g, '')
      const challenges = Array.isArray(item.challenges) ? item.challenges : []

      const rawStatuses = challenges.slice(0, 4).map(c =>
        (c?.isSuccess === true || c?.isSuccess === 'True' || c?.is_success === true)
          ? 'success' : 'fail'
      )
      while (rawStatuses.length < 4) rawStatuses.push('unknown')

      return {
        id: item.seniorCenterId,
        centerName: name,
        trotPoint: item.trotPoint ?? 0,
        missionPoint: item.challengePoint ?? 0,
        monthlyPoint: item.totalPoint ?? 0,
        ranking: item.ranking ?? null,
        challenges,
        challengeStatusesPadded: rawStatuses
      }
    })

    normalized.sort((a, b) => (a.ranking ?? 1e9) - (b.ranking ?? 1e9))
    centers.value = normalized
    totalPages.value = Math.max(1, Math.ceil(filteredCenters.value.length / pageSize))
    currentPage.value = 1
  } catch (err) {
    console.error('랭킹 로딩 실패:', err)
    centers.value = []
    totalPages.value = 1
    currentPage.value = 1
  }
}

/* 검색 + 내 경로당 중복 제거 */
const filteredCenters = computed(() => {
  const base = myCenter.value
    ? centers.value.filter(c => Number(c.id) !== Number(myCenterId.value))
    : centers.value

  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return base

  return base.filter(center =>
    (center.centerName || '').toLowerCase().includes(q)
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

/* 모달 도전 데이터 정규화 */
const normalizeChallenges = (challenges) => {
  const list = Array.isArray(challenges) ? challenges : []
  return list.map(c => ({
    id: c.id ?? c.challengeId ?? c.challegeId ?? c.challenge_id ?? c.slot ?? null,
    challengeImage: c.challengeImage ?? c.image ?? null,
    challengeTitle: c.challengeTitle ?? c.title ?? '',
    description: c.description ?? '',
    challengePlace: c.challengePlace ?? c.place ?? '',
    point: c.point ?? 0,
    isSuccess: c.isSuccess ?? c.is_success ?? false
  })).filter(c => c.id != null)
}

/* 모달 열기: 센터별 도전 조회 */
const openModal = async (centerId) => {
  try {
    const res = await api.get(`/api/v1/rankings/senior-center/${centerId}/challenges`)
    const data = res.data
    const centerInList = centers.value.find(c => Number(c.id) === Number(centerId))
    selectedCenter.value = {
      seniorCenterId: data.seniorCenterId ?? centerId,
      seniorCenterName: data.seniorCenterName ?? centerInList?.centerName ?? '',
      challenges: normalizeChallenges(data.challenges)
    }
  } catch (err) {
    console.error('도전 미션 로딩 실패:', err)
  }
}

const modalChallenges = computed(() => {
  const list = selectedCenter.value?.challenges ?? []
  const out = [...list.slice(0, 4)]
  while (out.length < 4) out.push(null)
  return out
})

const closeModal = () => { selectedCenter.value = null }

watch(filteredCenters, (filtered) => {
  totalPages.value = Math.max(1, Math.ceil(filtered.length / pageSize))
  currentPage.value = 1
})

onMounted(async () => {
  await fetchMyCenterId()
  await fetchRankings()
})

/* 상세 모달 */
const truncateText = (text, maxLength = 30) => {
  if (!text) return ''
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}

const openDetailModal = async (challengePk) => {
  const centerId = selectedCenter.value?.seniorCenterId
  if (!centerId || !challengePk) return
  try {
    const { data } = await api.get(`/api/v1/rankings/senior-center/${centerId}/challenges/${challengePk}`)
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

/* 행 하이라이트/메달용 클래스 */
const medalClass = (rank) => {
  const r = Number(rank)
  if (r === 1) return 'rank-1'
  if (r === 2) return 'rank-2'
  if (r === 3) return 'rank-3'
  return ''
}

/* 이미지 헬퍼 */
const getChallengeImage = (challenge) => challenge.challengeImage || defaultImage
const getChallengeImageFromSelected = (challenge) => challenge.image || defaultImage
const onImageError = (e) => { e.target.src = defaultImage }
const onImageLoad = () => {}
const onImageErrorSelected = (e) => { e.target.src = defaultImage }
const onImageLoadSelected = () => {}
</script>

<style scoped>
:root { --toolbar-height: 64px; --sticky-offset: var(--toolbar-height) } /* 헤더 높이에 맞게 조정 */

/* 레이아웃/타이포 */
.container { padding: 40px; max-width: 1400px; margin: 0 auto }
.title { font-size: 32px; line-height: 1.3; font-weight: 800; margin-bottom: 28px; text-align: center }
.search-bar { margin-bottom: 20px; display: flex; justify-content: flex-end }
.search-input { width: min(100%, 360px); min-width: 260px; box-sizing: border-box; padding: 12px 16px; font-size: 16px; border: 1px solid #ccc; border-radius: 10px }
.search-input::placeholder { font-size: 15px; color: #9aa3af }

/* 테이블 */
.ranking-table { width: 100%; border-collapse: separate; border-spacing: 0; border-radius: 10px; overflow: hidden; box-shadow: 0 0 6px rgba(0,0,0,0.05); font-size: 16px }
.ranking-table thead { background-color: #f9fafb }
.ranking-table th, .ranking-table td { padding: 14px 12px; text-align: center; border-bottom: 0; font-variant-numeric: tabular-nums }
.ranking-table tr:hover { background-color: #f8f9fa }
.ranking-table th.narrow, .ranking-table td:nth-child(3) { width: 240px }
.ranking-table td.text-blue { color: #007bff; font-weight: 700 }

/* ✅ 모든 목록 행은 tr 하나로만 하단선을 그림 → 셀 경계 불일치 해결 */
.ranking-table tbody tr { position: relative }
.ranking-table tbody tr::after {
  content: "";
  position: absolute;
  left: 0; right: 0; bottom: 0;
  height: 1px;
  background: #eee;
}
/* 마지막 행은 바닥선 숨기고 싶으면 주석 해제 */
.ranking-table tbody tr:last-child::after { display: none }

/* ✅ 내 경로당: 행 단위 sticky + 파란 테두리(겹침/끊김 방지) */
.my-center-row{
  position: sticky;
  top: var(--sticky-offset);
  z-index: 30;
  background: #f0f7ff;          /* 파란 박스 배경 */
  outline: 2px solid #3b82f6;   /* 행 전체 테두리 */
  outline-offset: -2px;
}
/* 내 경로당은 별도의 바닥선 필요 없음 */
.ranking-table tbody tr.my-center-row::after { display: none }

/* 경로당 이름 중앙 정렬 */
.center-name { display: flex; align-items: center; justify-content: center; gap: 10px }
.logo { width: 32px; height: 32px; border-radius: 50%; object-fit: cover }
.ellipsis { max-width: 280px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.chip-mycenter { margin-left: 8px; padding: 2px 10px; font-size: 12px; line-height: 18px; border-radius: 999px; border: 1px solid #93c5fd; background: #dbeafe; color: #1d4ed8; font-weight: 800 }

/* 순위 셀: 메달/배지 */
.rank-cell { display: inline-flex; align-items: center; justify-content: center; gap: 8px; font-weight: 700 }

.medal-icon{
  width:28px;height:28px;border-radius:50%;
  display:inline-flex;align-items:center;justify-content:center;
  position:relative;
  box-shadow:inset 0 0 0 2px rgba(0,0,0,.08),0 1px 2px rgba(0,0,0,.08);
}
.medal-icon::after{
  content:'';position:absolute;inset:2px 6px auto auto;width:8px;height:8px;border-radius:50%;
  background:rgba(255,255,255,.75);filter:blur(.5px);
}
.gold   { background: radial-gradient(circle at 30% 30%, #fff3b0, #f5b301 60%, #d89200) }
.silver { background: radial-gradient(circle at 30% 30%, #ffffff, #bfc7cf 60%, #9aa3ae) }
.bronze { background: radial-gradient(circle at 30% 30%, #ffe2c4, #cd7f32 60%, #9a5a21) }

.medal-num{
  position:absolute;inset:0;display:flex;align-items:center;justify-content:center;
  font-size:14px;font-weight:900;color:#1f2937;text-shadow:0 1px 0 rgba(255,255,255,.4);
}

.rank-badge { min-width:28px;height:28px;border-radius:999px;padding:0 8px;display:inline-flex;align-items:center;justify-content:center;background:#eef2f7;color:#333;font-weight:800 }

/* 상위 3위 행 하이라이트(왼쪽 띠 포함) */
tr.rank-1 { background:#fff7d6 }
tr.rank-2 { background:#f2f4f7 }
tr.rank-3 { background:#ffe9d6 }
tr.rank-1 td:first-child { border-left:6px solid #f5b301 }
tr.rank-2 td:first-child { border-left:6px solid #9aa3ae }
tr.rank-3 td:first-child { border-left:6px solid #cd7f32 }
.ranking-table tbody tr.rank-1:hover { background:#fff3c2 }
.ranking-table tbody tr.rank-2:hover { background:#eceff3 }
.ranking-table tbody tr.rank-3:hover { background:#ffe1c8 }

/* 상태/뱃지: 4칸 고정, 간격 살짝 넓힘 */
.status-box { display:grid; grid-template-columns:repeat(4,28px); gap:10px; justify-content:center; align-items:center }
.status { width:28px; height:28px; line-height:28px; text-align:center; border-radius:50%; font-size:16px; font-weight:800; color:#fff }
.success { background-color:#28a745 } .fail{ background-color:#dc3545 } .unknown{ background-color:#9aa3ae }

/* 액션 버튼: 🔍 돋보기 */
.challenge-info-btn { display:inline-flex; align-items:center; gap:6px; cursor:pointer; margin-top:8px; color:#333; font-size:14px; font-weight:700; background:transparent; border:none; padding:4px 6px; border-radius:8px }
.challenge-info-btn:hover { background:#f1f5f9 }
.challenge-info-btn .lens { font-size:16px }

/* 페이지네이션 */
.pagination { margin-top:24px; display:flex; justify-content:center; gap:8px; align-items:center }
.page-button, .pagination button { padding:8px 12px; border:1px solid #ccc; background-color:#fff; cursor:pointer; font-size:16px; border-radius:8px }
.page-button.active { background-color:#007bff; color:#fff; font-weight:800; border-color:#007bff }
.pagination button:disabled { opacity:.4; cursor:not-allowed }

.empty { padding:24px; color:#999; text-align:center }

/* 모달 공통 */
.modal-overlay { position:fixed; inset:0; background-color:rgba(0,0,0,.4); display:flex; justify-content:center; align-items:center; z-index:999 }
.modal-content { background:#fff; padding:20px 24px; border-radius:10px; box-shadow:0 2px 10px rgba(0,0,0,.1); max-width:1000px; width:100%; text-align:center }
.modal-overlay .modal-content { max-height:80vh; overflow-y:auto }
.close-btn { margin-top:16px; background:#007bff; color:#fff; border:none; padding:10px 16px; border-radius:6px; cursor:pointer }

/* 도전 카드 */
.challenge-grid { display:grid; grid-template-columns:repeat(auto-fit, minmax(220px, 1fr)); gap:16px; margin:20px 0; align-items:stretch }
.challenge-card { background:#f8f9fa; border-radius:12px; padding:12px; box-shadow:0 2px 6px rgba(0,0,0,.1); display:grid; grid-template-rows:auto 1fr; text-align:left; position:relative }
.image-placeholder { width:100%; aspect-ratio:1/1; background:#dfe3e6; border-radius:8px; margin-bottom:10px; display:flex; align-items:center; justify-content:center }
.challenge-img { width:100%; height:100%; object-fit:cover; border-radius:8px }
.card-title { font-size:18px; font-weight:800; display:flex; justify-content:space-between; align-items:center; line-height:1.35; min-height:calc(1.35em * 2) }
.card-title-text { flex:1; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden }
.card-description { font-size:14px; margin:6px 0; color:#444; line-height:1.5; display:-webkit-box; -webkit-line-clamp:1; -webkit-box-orient:vertical; overflow:hidden }
.card-subtext, .card-point { font-size:13px; color:#666; margin-top:4px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis }
.card-text { display:flex; flex-direction:column; min-height:100% }
.more-info { margin-top:auto; align-self:flex-end; background:transparent; border:none; cursor:pointer; color:#007bff; font-weight:700; padding:0 }
.more-info:hover { text-decoration:underline }

/* 상세 모달: 좌 이미지 / 우 텍스트 */
.detail-modal{ max-width:1120px }
.detail-grid{ display:grid; grid-template-columns:minmax(420px,1fr) 1fr; gap:24px; align-items:start; text-align:left }
.detail-image .image-box{ width:100%; aspect-ratio:4/3; background:#dfe3e6; border-radius:12px; overflow:hidden; display:flex; align-items:center; justify-content:center }
.detail-img{ width:100%; height:100%; object-fit:cover }
.detail-info{ display:flex; flex-direction:column; min-height:100% }
.detail-actions{ margin-top:auto; display:flex; justify-content:flex-end; gap:8px }

@media (max-width:900px){
  .detail-grid{ grid-template-columns:1fr }
  .detail-actions{ justify-content:flex-start }
}

/* td는 table-cell, 내부 래퍼로 정렬 */
td.rank-cell { text-align:center; vertical-align:middle; font-weight:700; padding:14px 12px }
.rank-wrap { display:inline-flex; align-items:center; justify-content:center; gap:8px; width:100% }

/* (선택) 1열 고정 폭 */
.ranking-table th:first-child,
.ranking-table td:first-child { width: 90px }
</style>