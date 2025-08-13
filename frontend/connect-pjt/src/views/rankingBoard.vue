<template>
  <div class="container">
    <!-- 제목(재량 #3 반영: 더 명확한 표현) -->
    <h1 class="title">경로당 순위표</h1>

    <!-- 내 경로당: 상단 고정 버전 -->
    <div
      v-if="myCenter"
      class="pinned-row pinned-top"
      :class="medalClass(myCenterRank)"
    >
      <div class="col-rank rank-cell">
        <span v-if="myCenterRank === 1" class="medal medal-gold" aria-label="1위">🥇</span>
        <span v-else-if="myCenterRank === 2" class="medal medal-silver" aria-label="2위">🥈</span>
        <span v-else-if="myCenterRank === 3" class="medal medal-bronze" aria-label="3위">🥉</span>
        <span class="rank-number">{{ myCenterRank }}</span>
      </div>
      <div class="col-name">
        <div class="center-name">
          <img src="@/assets/logo.png" class="logo" />
          <span class="ellipsis">{{ myCenter.centerName }}</span>
        </div>
      </div>
      <div class="col-status">
        <div class="status-box">
          <span
            v-for="(s, i) in (myCenter.challengeStatuses || [])"
            :key="i"
            :class="s === 'success' ? 'status success' : 'status fail'"
          >
            {{ s === 'success' ? '✓' : '✕' }}
          </span>
        </div>
        <div class="challenge-info-btn" @click="openModal(myCenter.id)">
          <span class="arrow">»</span><span class="describe-text">도전 미션 현황 보기</span>
        </div>
      </div>
      <div class="col-num">{{ myCenter.trotPoint ?? 0 }}</div>
      <div class="col-num">{{ myCenter.missionPoint ?? 0 }}</div>
      <div class="col-num text-blue">{{ myCenter.monthlyPoint ?? 0 }}</div>
    </div>

    <!-- 대체안: 하단 고정 버전 (원하면 이 블록 주석 해제해서 사용)
    <div v-if="myCenter" class="pinned-row pinned-bottom" :class="medalClass(myCenterRank)">
      <div class="col-rank rank-cell">
        <span v-if="myCenterRank === 1" class="medal medal-gold" aria-label="1위">🥇</span>
        <span v-else-if="myCenterRank === 2" class="medal medal-silver" aria-label="2위">🥈</span>
        <span v-else-if="myCenterRank === 3" class="medal medal-bronze" aria-label="3위">🥉</span>
        <span class="rank-number">{{ myCenterRank }}</span>
      </div>
      <div class="col-name">
        <div class="center-name">
          <img src="@/assets/logo.png" class="logo" />
          <span class="ellipsis">{{ myCenter.centerName }}</span>
        </div>
      </div>
      <div class="col-status">
        <div class="status-box">
          <span
            v-for="(s, i) in (myCenter.challengeStatuses || [])"
            :key="i"
            :class="s === 'success' ? 'status success' : 'status fail'"
          >
            {{ s === 'success' ? '✓' : '✕' }}
          </span>
        </div>
        <div class="challenge-info-btn" @click="openModal(myCenter.id)">
          <span class="arrow">»</span><span class="describe-text">도전 미션 현황 보기</span>
        </div>
      </div>
      <div class="col-num">{{ myCenter.trotPoint ?? 0 }}</div>
      <div class="col-num">{{ myCenter.missionPoint ?? 0 }}</div>
      <div class="col-num text-blue">{{ myCenter.monthlyPoint ?? 0 }}</div>
    </div>
    -->

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
          <!-- 재량 #4: 왼쪽으로 기운 느낌 → 중앙 정렬 -->
          <th class="text-center">경로당 이름</th>
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

        <!-- 행에 순위 기반 하이라이트 클래스 적용 -->
        <tr
          v-for="(center, index) in paginatedCenters"
          :key="center.id"
          :class="medalClass(center.ranking ?? (index + 1 + (currentPage - 1) * pageSize))"
        >
          <!-- 순위 칸: 메달 + 숫자 -->
          <td class="rank-cell">
            <span v-if="(center.ranking ?? (index + 1 + (currentPage - 1) * pageSize)) === 1" class="medal medal-gold" aria-label="1위">🥇</span>
            <span v-else-if="(center.ranking ?? (index + 1 + (currentPage - 1) * pageSize)) === 2" class="medal medal-silver" aria-label="2위">🥈</span>
            <span v-else-if="(center.ranking ?? (index + 1 + (currentPage - 1) * pageSize)) === 3" class="medal medal-bronze" aria-label="3위">🥉</span>
            <span class="rank-number">{{ center.ranking ?? (index + 1 + (currentPage - 1) * pageSize) }}</span>
          </td>

          <td class="text-center">
            <div class="center-name">
              <img src="@/assets/logo.png" class="logo" />
              <span class="ellipsis">{{ center.centerName }}</span>
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

    <!-- 상세 모달 (좌 이미지 / 우 정보) -->
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
              <!-- <button class="report-btn" @click="reportChallenge">신고하기</button> -->
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

/* 내 경로당 ID (프로필/스토어/로컬 등에서 주입) */
const myCenterId = ref(null)

/* 상단 고정에 쓸 내 경로당 객체 & 순위 */
const myCenter = computed(() => centers.value.find(c => c.id === myCenterId.value) || null)
const myCenterRank = computed(() => {
  if (!myCenter.value) return '-'
  // 서버에서 주는 ranking 우선, 없으면 정렬 위치 보정
  return myCenter.value.ranking ?? (centers.value.findIndex(c => c.id === myCenter.value.id) + 1)
})

/* 예시: 로컬 저장소에서 소속 경로당 ID 로드 (원하는 방식으로 대체) */
const fetchMyCenterId = async () => {
  try {
    const saved = localStorage.getItem('mySeniorCenterId')
    if (saved) myCenterId.value = Number(saved)
  } catch (e) {}
}

/* 목록 호출: /api/v1/rankings */
const fetchRankings = async () => {
  const { data } = await api.get('/api/v1/rankings')
  const normalized = (data ?? []).map(item => {
    const name = (item.seniorCenterName || '').replace(/\uFEFF/g, '')
    const challenges = Array.isArray(item.challenges) ? item.challenges : []
    return {
      id: item.seniorCenterId,
      centerName: name,
      trotPoint: item.trotPoint ?? 0,
      missionPoint: item.challengePoint ?? 0,
      monthlyPoint: item.totalPoint ?? 0,
      ranking: item.ranking ?? null,
      challenges,
      challengeStatuses: challenges.slice(0, 4).map(c =>
        (c?.isSuccess === true || c?.isSuccess === 'True' || c?.is_success === true)
          ? 'success' : 'fail'
      )
    }
  })
  // 서버 제공 ranking 기준 정렬 (null은 맨 뒤)
  normalized.sort((a, b) => (a.ranking ?? 1e9) - (b.ranking ?? 1e9))
  centers.value = normalized
  totalPages.value = Math.ceil(centers.value.length / pageSize)
}

/* 검색/페이지네이션 */
const filteredCenters = computed(() => {
  if (!searchQuery.value.trim()) return centers.value
  return centers.value.filter(center =>
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

/* 모달용 도전 데이터 정규화 (오타 challegeId 대응) */
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

/* 모달 열기: 센터별 도전 조회 (기존 엔드포인트 유지) */
const openModal = async (centerId) => {
  try {
    const res = await api.get(`/api/v1/rankings/senior-center/${centerId}/challenges`)
    const data = res.data
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

const modalChallenges = computed(() => {
  const list = selectedCenter.value?.challenges ?? []
  const out = [...list.slice(0, 4)]
  while (out.length < 4) out.push(null)
  return out
})

const closeModal = () => { selectedCenter.value = null }

watch(filteredCenters, (filtered) => {
  totalPages.value = Math.ceil(filtered.length / pageSize)
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

/* 행 하이라이트/메달용 클래스 매핑 */
const medalClass = (rank) => {
  if (rank === 1) return 'rank-1'
  if (rank === 2) return 'rank-2'
  if (rank === 3) return 'rank-3'
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
/* ==== 1) UI 확대 ==== */
.container { padding: 40px; max-width: 1400px; margin: 0 auto; }
.title { font-size: 28px; font-weight: 800; margin-bottom: 28px; }
.search-bar { margin-bottom: 20px; text-align: right; }
.search-input { padding: 10px 14px; font-size: 16px; border: 1px solid #ccc; border-radius: 8px; }

/* 테이블 */
.ranking-table { width: 100%; border-collapse: separate; border-spacing: 0; border-radius: 10px; overflow: hidden; box-shadow: 0 0 6px rgba(0,0,0,0.05); font-size: 16px; }
.ranking-table thead { background-color: #f9fafb; }
.ranking-table th, .ranking-table td { padding: 16px; text-align: center; border-bottom: 1px solid #eee; font-variant-numeric: tabular-nums; }
.ranking-table tr:hover { background-color: #f8f9fa; }
.ranking-table td.text-blue { color: #007bff; font-weight: 700; }

/* 4) 경로당 이름 중앙 정렬 */
.center-name { display: flex; align-items: center; justify-content: center; gap: 10px; }
.logo { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; }
.ellipsis { max-width: 280px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* 상태/뱃지 */
.status-box { display: grid; grid-template-columns: repeat(4, 1fr); gap: 4px; justify-items: center; }
.status { width: 24px; height: 24px; line-height: 24px; text-align: center; border-radius: 50%; font-size: 13px; font-weight: 700; color: #fff; }
.success { background-color: #28a745; } .fail { background-color: #dc3545; }

/* 페이지네이션 */
.pagination { margin-top: 24px; display: flex; justify-content: center; gap: 8px; align-items: center; }
.page-button, .pagination button { padding: 8px 12px; border: 1px solid #ccc; background-color: #fff; cursor: pointer; font-size: 16px; border-radius: 8px; }
.page-button.active { background-color: #007bff; color: #fff; font-weight: 800; border-color: #007bff; }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }

.empty { padding: 24px; color: #999; text-align: center; }

/* 모달 */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 999; }
.modal-content { background: #fff; padding: 20px 24px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 1000px; width: 100%; text-align: center; }
.modal-overlay .modal-content { max-height: 80vh; overflow-y: auto; }
.close-btn { margin-top: 16px; background: #007bff; color: #fff; border: none; padding: 10px 16px; border-radius: 6px; cursor: pointer; }

/* 도전 카드 */
.challenge-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin: 20px 0; align-items: stretch; }
.challenge-card { background: #f8f9fa; border-radius: 12px; padding: 12px; box-shadow: 0 2px 6px rgba(0,0,0,0.1); display: grid; grid-template-rows: auto 1fr; text-align: left; position: relative; }
.image-placeholder { width: 100%; aspect-ratio: 1/1; background: #dfe3e6; border-radius: 8px; margin-bottom: 10px; display: flex; align-items: center; justify-content: center; }
.challenge-img { width: 100%; height: 100%; object-fit: cover; border-radius: 8px; }
.card-title { font-size: 18px; font-weight: 800; display: flex; justify-content: space-between; align-items: center; line-height: 1.35; min-height: calc(1.35em * 2); }
.card-title-text { flex: 1; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-description { font-size: 14px; margin: 6px 0; color: #444; line-height: 1.5; min-height: calc(1.5em * 2); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-subtext, .card-point { font-size: 13px; color: #666; margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.more-info { margin-top: auto; align-self: flex-end; background: transparent; border: none; cursor: pointer; color: #007bff; font-weight: 700; padding: 0; }
.more-info:hover { text-decoration: underline; }

/* 상세 모달: 좌(이미지)/우(텍스트) */
.detail-modal { max-width: 1120px; }
.modal-overlay + .modal-overlay { z-index: 1001; }
.detail-grid { display: grid; grid-template-columns: minmax(420px,1fr) 1fr; gap: 24px; align-items: start; text-align: left; }
.detail-image .image-box { width: 100%; aspect-ratio: 4/3; background: #dfe3e6; border-radius: 12px; overflow: hidden; display: flex; align-items: center; justify-content: center; }
.detail-image .image-box.empty { color: #888; font-size: 24px; }
.detail-img { width: 100%; height: 100%; object-fit: cover; }
.detail-info { display: flex; flex-direction: column; min-height: 100%; }
.detail-title { display: flex; align-items: flex-start; gap: 8px; font-size: 22px; line-height: 1.4; margin: 0 0 8px 0; }
.detail-title-text { flex: 1; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.detail-desc { white-space: pre-line; line-height: 1.7; font-size: 16px; color: #333; margin: 8px 0 12px 0; max-height: calc(1.7em * 6); overflow: auto; }
.detail-meta { display: grid; gap: 8px; margin: 4px 0 16px 0; }
.meta-row { font-size: 15px; color: #444; }
.detail-actions { margin-top: auto; display: flex; justify-content: flex-end; gap: 8px; }

/* 2) 내 경로당 고정 바 */
.pinned-row {
  display: grid;
  grid-template-columns: 80px 1.4fr 1.2fr 1fr 1fr 1fr; /* 표와 유사한 비율 */
  gap: 8px;
  align-items: center;
  padding: 12px 16px;
  margin: 8px 0 16px 0;
  border: 1px solid #e9eef7;
  border-left: 6px solid #4c89ff;
  background: #f5f9ff;
  border-radius: 10px;
}
.pinned-top { position: sticky; top: 64px; z-index: 5; }   /* 상단 고정 */
.pinned-bottom { position: sticky; bottom: 0; z-index: 5; }/* 하단 고정(대체안) */
.pinned-row .col-rank, .pinned-row .col-num { text-align: center; font-weight: 700; }
.pinned-row .center-name { justify-content: center; }

/* === 메달 & 상위 3위 하이라이트 === */
.rank-cell { display: inline-flex; align-items: center; justify-content: center; gap: 6px; font-weight: 700; }
.medal { font-size: 18px; line-height: 1; }
.rank-number { min-width: 2ch; display: inline-block; }

/* 상위 3위 행 하이라이트 */
tr.rank-1 { background: #fff7d6; }            /* 금: 연한 골드 */
tr.rank-2 { background: #f2f4f7; }            /* 은: 연한 실버 */
tr.rank-3 { background: #ffe9d6; }            /* 동: 연한 브론즈 */
tr.rank-1 td:first-child { border-left: 6px solid #f5b301; }
tr.rank-2 td:first-child { border-left: 6px solid #9aa3ae; }
tr.rank-3 td:first-child { border-left: 6px solid #cd7f32; }

/* 호버 색상 유지 */
.ranking-table tbody tr.rank-1:hover { background: #fff3c2; }
.ranking-table tbody tr.rank-2:hover { background: #eceff3; }
.ranking-table tbody tr.rank-3:hover { background: #ffe1c8; }

/* 상단 고정 바도 포인트 컬러만 맞춤 */
.pinned-row.rank-1 { border-left-color: #f5b301; }
.pinned-row.rank-2 { border-left-color: #9aa3ae; }
.pinned-row.rank-3 { border-left-color: #cd7f32; }

@media (max-width: 900px) {
  .detail-modal { max-width: 92vw; }
  .detail-grid { grid-template-columns: 1fr; }
  .detail-actions { justify-content: flex-start; }
}

.challenge-info-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  margin-top: 6px;
  color: #555;
  font-size: 12px;
  transition: color 0.2s ease;
}
.challenge-info-btn:hover { color: #007bff; }
.challenge-info-btn .arrow { font-size: 14px; color: inherit; }
</style>
