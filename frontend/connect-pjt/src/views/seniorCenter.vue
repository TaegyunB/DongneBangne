<template>
  <div class="find-senior-center">
    <main class="main-content">
      <h1 class="headline" id="page-title">내 소속 경로당 찾기</h1>

      <OnboardingGuide v-model="showOnboarding" @confirm="handleOnboardingConfirm" />

      <!-- 검색 폼: 엔터로도 검색, 스크린리더 레이블 제공 -->
      <form class="search-box" @submit.prevent="onSearch" :aria-busy="isLoading">
        <label for="type" class="sr-only">검색 종류</label>
        <select id="type" v-model="selectedType" class="type-select" aria-label="검색 종류 선택">
          <option value="name">이름</option>
          <option value="address">주소</option>
        </select>

        <label for="keyword" class="sr-only">검색어</label>
        <input
          id="keyword"
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="예) 싸피 경로당 또는 서초구 역삼동"
          autocomplete="off"
          inputmode="search"
        />

        <button
          type="submit"
          class="search-btn"
          :disabled="isSearchDisabled"
          aria-label="검색"
        >
          {{ isLoading ? '검색 중...' : '검색' }}
        </button>
      </form>

      <table v-if="searchResults.length" class="result-table" aria-describedby="page-title">
        <caption class="sr-only">경로당 검색 결과</caption>
        <thead>
          <tr>
            <th scope="col">이름</th>
            <th scope="col">주소</th>
            <th scope="col">선택</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="center in searchResults" :key="center.id || center.name">
            <td class="cell-name">{{ center.name }}</td>
            <td class="cell-addr">
              <a
                v-if="center.address"
                :href="kakaoMapUrl(center.address)"
                target="_blank"
                rel="noopener"
                class="addr-link"
                :aria-label="center.address + ' 카카오맵으로 열기'"
              >
                {{ center.address }}
              </a>
              <span v-else>-</span>
            </td>
            <td class="cell-check">
              <label class="row-check">
                <span class="sr-only">이 경로당 선택</span>
                <input
                  type="checkbox"
                  :checked="selectedId === center.id"
                  @change="onRowCheck(center, $event)"
                  aria-label="이 경로당 선택"
                />
              </label>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-else-if="searched" class="no-result" aria-live="polite">
        검색 결과가 없습니다.
      </div>

      <!-- 모달 -->
      <div v-if="showModal" class="modal-overlay">
        <div
          class="modal-content"
          role="dialog"
          aria-modal="true"
          aria-labelledby="confirmTitle"
        >
          <h3 id="confirmTitle" class="modal-title">선택한 경로당이 맞으신가요?</h3>
          <p class="modal-text">
            <b>{{ modalCenter.name }}</b><br />
            {{ modalCenter.address }}
          </p>
          <div class="modal-actions">
            <label class="agree">
              <input type="checkbox" v-model="isConfirmed" />
              <span>위 경로당이 본인 소속임에 동의합니다</span>
            </label>
          </div>
          <div class="modal-buttons">
            <button
              class="confirm-btn lg"
              @click="confirmCenter"
              :disabled="!isConfirmed"
            >
              확인
            </button>
            <button class="cancel-btn lg" @click="closeModal">아니오</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/api/axios'
import { useRouter } from 'vue-router'
import { useOnboardingStore } from '@/stores/useOnboardingStore'
import OnboardingGuide from '@/components/OnboardingGuide.vue'

const selectedType = ref('name')
const keyword = ref('')
const searchResults = ref([])
const searched = ref(false)
const isLoading = ref(false)

const router = useRouter()
const store = useOnboardingStore()

const showModal = ref(false)
const modalCenter = ref({ id: null, name: '', address: '' })
const showOnboarding = ref(false)
const isConfirmed = ref(false)
const selectedId = ref(null)

const isSearchDisabled = computed(() => !keyword.value.trim() || isLoading.value)

const normalizeCenter = c => ({
  id: c.id ?? c.seniorCenterId ?? c.senior_center_id ?? null,
  name: c.name ?? c.seniorCenterName ?? c.centerName ?? '',
  address: c.address ?? c.addr ?? ''
})

const kakaoMapUrl = addr => 'https://map.kakao.com/?q=' + encodeURIComponent(addr)

async function fetchCenters() {
  isLoading.value = true
  try {
    const res = await api.get('/api/v1/senior-centers', {
      params: { type: selectedType.value, keyword: keyword.value.trim() }
    })
    const list = Array.isArray(res.data) ? res.data.map(normalizeCenter) : []
    searchResults.value = list
  } catch (e) {
    console.error('경로당 검색 실패:', e)
    searchResults.value = []
  } finally {
    searched.value = true
    isLoading.value = false
  }
}

function onSearch() {
  if (isSearchDisabled.value) {
    searched.value = true
    searchResults.value = []
    return
  }
  fetchCenters()
}

function onRowCheck(center, evt) {
  if (evt.target.checked) {
    selectedId.value = center.id
    openConfirm(center)
  } else {
    selectedId.value = null
  }
}

function openConfirm(center) {
  modalCenter.value = center
  isConfirmed.value = false
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  // 취소 시 체크 해제
  selectedId.value = null
}

async function confirmCenter() {
  try {
    await api.post('/api/v1/users/senior-center', {
      seniorCenterId: modalCenter.value.id
    })
    showModal.value = false
    router.push('/senior-center/profile')
  } catch (err) {
    console.error('경로당 선택 실패:', err)
    alert('경로당 선택에 실패했습니다.')
    showModal.value = false
    selectedId.value = null
  }
}

onMounted(() => {
  if (!store.seen) {
    showOnboarding.value = true
  }
})

function handleOnboardingConfirm(payload) {
  if (payload.confirm && payload.dontShowAgain) {
    store.markSeen()
  }
}
</script>

<style scoped>
/* ========= 접근성/시니어 친화 기본값 ========= */
.find-senior-center {
  min-height: 100vh;
  background: #ffffff;
  color: #111;
  line-height: 1.55;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

:focus-visible {
  outline: 3px solid #12795a;
  outline-offset: 3px;
  border-radius: 6px;
}

.sr-only {
  position: absolute !important;
  width: 1px; height: 1px;
  padding: 0; margin: -1px;
  overflow: hidden; clip: rect(0,0,0,0);
  white-space: nowrap; border: 0;
}

/* ========= 레이아웃 ========= */
.main-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 16px 96px;
}

.headline {
  font-size: 48px;
  font-weight: 800;
  letter-spacing: -0.5px;
  margin: 12px 0 24px;
}

/* ========= 검색 영역 ========= */
.search-box {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border: 2px solid #1e1e1e;
  border-radius: 12px;
  padding: 14px 16px;
  max-width: 720px;
  box-sizing: border-box;
}

.type-select {
  font-size: 20px;
  padding: 12px 12px;
  border: 2px solid #b7b7b7;
  border-radius: 8px;
  min-width: 120px;
}

.search-input {
  font-size: 22px;
  padding: 14px 16px;
  border: none;
  outline: none;
  width: 100%;
  background: transparent;
}

.search-btn {
  font-size: 20px;
  font-weight: 800;
  min-height: 52px;
  min-width: 120px;
  padding: 10px 18px;
  background: #12795a;
  color: #fff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: transform .05s ease, background .15s ease;
}
.search-btn:hover { background: #0f6148 }
.search-btn:active { transform: translateY(1px) }
.search-btn:disabled { opacity: .6; cursor: not-allowed }

/* ========= 결과 테이블 ========= */
.result-table {
  width: 100%;
  max-width: 980px;
  margin-top: 28px;
  border-collapse: separate;
  border-spacing: 0;
  background: #fff;
  border: 1.5px solid #e3e5e8;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 6px 18px rgba(0,0,0,.06);
  font-size: 18px;
}

.result-table th,
.result-table td {
  padding: 18px 14px;
  border-bottom: 1px solid #ececec;
  text-align: center;
}

.result-table thead th {
  background: #f4f6f8;
  font-size: 20px;
  font-weight: 800;
}

.result-table tbody tr:nth-child(even) {
  background: #fafafa;
}

.result-table tr:last-child td {
  border-bottom: none;
}

.cell-name, .cell-addr { text-align: left }

.addr-link {
  text-decoration: underline;
  text-underline-offset: 3px;
}
.addr-link:focus-visible {
  outline: 3px solid #12795a;
  outline-offset: 2px;
  border-radius: 4px;
}

.cell-check { text-align: center }

.row-check input {
  width: 26px;
  height: 26px;
  cursor: pointer;
}

/* 버튼 공통 (남겨두되 현재 테이블에선 미사용) */
.confirm-btn, .cancel-btn {
  font-size: 18px;
  font-weight: 700;
  min-height: 44px;
  padding: 8px 18px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background .15s ease, transform .05s ease;
}
.confirm-btn { background: #12795a; color: #fff }
.confirm-btn:hover { background: #0f6148 }
.cancel-btn { background: #a6a6a6; color: #fff }
.cancel-btn:hover { background: #8d8d8d }
.confirm-btn.lg, .cancel-btn.lg { min-width: 140px; min-height: 52px; font-size: 20px }

.no-result {
  margin-top: 24px;
  color: #333;
  font-size: 20px;
}

/* ========= 모달 ========= */
.modal-overlay {
  position: fixed;
  z-index: 2000;
  inset: 0;
  background: rgba(0,0,0,.38);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.modal-content {
  background: #fff;
  padding: 36px 28px 28px;
  border-radius: 16px;
  min-width: 360px;
  max-width: 92vw;
  box-shadow: 0 6px 24px rgba(0,0,0,.18);
  text-align: center;
}

.modal-title {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 10px;
}

.modal-text { font-size: 20px; margin: 0 0 16px }

.modal-actions {
  display: flex;
  justify-content: center;
  margin: 10px 0 18px;
}
.agree {
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.agree input { width: 22px; height: 22px }

/* ========= 반응형 ========= */
@media (max-width: 820px) {
  .headline { font-size: 38px }
  .search-box { min-width: 100%; padding: 16px }
  .type-select { font-size: 18px }
  .search-input { font-size: 20px }
  .result-table { font-size: 18px }
  .result-table thead th { font-size: 20px }
}
</style>
