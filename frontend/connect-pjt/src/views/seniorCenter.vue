<template>
  <div class="find-senior-center">
    <main class="main-content">
      <h1 class="headline" id="page-title">내 소속 경로당 찾기</h1>

      <OnboardingGuide v-model="showOnboarding" @confirm="handleOnboardingConfirm" />

      <!-- 검색 폼 -->
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

      <!-- 주소 안내 문구 -->
      <p
        v-if="searched"
        class="addr-hint"
        aria-live="polite"
      >
        검색 결과의 <b>주소를 클릭하면</b> 새 창에서 <b>지도</b>로 연결돼요. 좌표가 있는 항목은 지도만 열려 위치가 바로 표시됩니다.
      </p>

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
                :href="kakaoMapUrl(center)"
                target="_blank"
                rel="noopener"
                class="addr-link"
                :aria-label="(center.name || center.address) + ' 위치 지도로 열기'"
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

      <!-- (1) 확인 모달 -->
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
              <span>위 경로당이 본인 소속임을 확인했습니다.</span>
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

      <!-- (2) 가입 완료 안내 모달 -->
      <div v-if="showDoneModal" class="modal-overlay">
        <div
          class="modal-content"
          role="dialog"
          aria-modal="true"
          aria-labelledby="doneTitle"
        >
          <h3 id="doneTitle" class="modal-title">회원가입이 완료되었어요!</h3>
          <p class="modal-text">
            지금 바로 메인으로 가면 <br />
            현재 <b>카카오 닉네임</b> 앞에 <b>‘{{ chosenCenterName }}’</b>이 자동으로 붙어 저장돼요.<br />
            <span v-if="previewNick" style="display:inline-block;margin-top:6px;color:#444">
              예: <b>{{ previewNick }}</b>
            </span>
            <br />언제든지 프로필에서 변경할 수 있어요.
          </p>
          <div class="modal-buttons">
            <button class="confirm-btn lg" @click="goEditProfile">
              회원 정보 수정하기
            </button>
            <button class="cancel-btn lg" @click="goMain">
              나중에 할래요
            </button>
          </div>
        </div>
      </div>
      <!-- // 모달 끝 -->
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
const showDoneModal = ref(false)
const modalCenter = ref({ id: null, name: '', address: '' })
const showOnboarding = ref(false)
const isConfirmed = ref(false)
const selectedId = ref(null)

/* ▼ 추가 상태: 선택한 센터명, 미리보기 닉네임 */
const chosenCenterName = ref('')
const previewNick = ref('')

/* === 닉네임 접두어 유틸 === */
const buildFinalNickname = (base, center) => {
  const b = String(base || '').trim()
  const c = String(center || '').trim()
  return c ? `${c} ${b}` : b
}
const stripCenterPrefix = (full, center) => {
  const f = String(full || '').trim()
  const c = String(center || '').trim()
  return c && f.startsWith(c + ' ') ? f.slice(c.length + 1) : f
}

const isSearchDisabled = computed(() => !keyword.value.trim() || isLoading.value)

// 데이터 정규화: 좌표도 같이 수용
const normalizeCenter = c => ({
  id: c.id ?? c.seniorCenterId ?? c.senior_center_id ?? null,
  name: c.name ?? c.seniorCenterName ?? c.centerName ?? '',
  address: c.address ?? c.addr ?? '',
  lat: c.lat ?? c.latitude ?? c.y ?? null,
  lng: c.lng ?? c.longitude ?? c.x ?? null
})

// 카카오맵 링크 생성 (좌표 있으면 지도+마커, 없으면 검색)
const kakaoMapUrl = center => {
  const n = center?.name || center?.address || '위치'
  if (center?.lat && center?.lng) {
    return `https://map.kakao.com/link/map/${encodeURIComponent(n)},${center.lat},${center.lng}`
  }
  return `https://map.kakao.com/link/search/${encodeURIComponent(center?.address || '')}`
}

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
  selectedId.value = null
}

async function confirmCenter() {
  try {
    await api.post('/api/v1/users/senior-center', {
      seniorCenterId: modalCenter.value.id
    })
    // 선택한 센터명 보관
    chosenCenterName.value = modalCenter.value.name || ''
    // 카카오 닉네임 가져와 미리보기 생성
    try {
      const { data: me } = await api.get('/api/v1/main/me', { withCredentials: true })
      const current = me?.nickname || ''
      const pure = stripCenterPrefix(current, chosenCenterName.value)
      previewNick.value = buildFinalNickname(pure, chosenCenterName.value)
    } catch (e) {
      // 조회 실패 시에도 모달은 열어둠(미리보기만 생략)
      previewNick.value = ''
    }
    showModal.value = false
    showDoneModal.value = true
  } catch (err) {
    console.error('경로당 선택 실패:', err)
    alert('경로당 선택에 실패했습니다.')
    showModal.value = false
    selectedId.value = null
  }
}

function goEditProfile() {
  router.push('/senior-center/profile')
}

async function goMain() {
  // 바로 메인: 닉네임을 "센터명 + (카카오 닉네임의 순수본)"으로 저장 후 이동
  try {
    // me 재조회(최신 정보)
    const { data: me } = await api.get('/api/v1/main/me', { withCredentials: true })
    const center = chosenCenterName.value || me?.seniorCenter?.centerName || modalCenter.value.name || ''
    const current = me?.nickname || ''
    const pure = stripCenterPrefix(current, center)
    const finalNickname = buildFinalNickname(pure, center)
    if (center && finalNickname !== current) {
      await api.put('/api/v1/users/profile', { nickname: finalNickname }, { withCredentials: true })
    }
  } catch (e) {
    console.error('메인 이동 전 닉네임 자동 저장 실패(무시 후 진행):', e)
  } finally {
    router.push('/mainpage')
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
/* ===== 폰트 등록(페이지 단위) ===== */
@font-face {
  font-family: 'KoddiUDOnGothic';
  src: url('@/assets/fonts/KoddiUDOnGothic-Regular.ttf') format('truetype');
  font-weight: 400; font-style: normal; font-display: swap;
}
@font-face {
  font-family: 'KoddiUDOnGothic';
  src: url('@/assets/fonts/KoddiUDOnGothic-Bold.ttf') format('truetype');
  font-weight: 700; font-style: normal; font-display: swap;
}
@font-face {
  font-family: 'KoddiUDOnGothic';
  src: url('@/assets/fonts/KoddiUDOnGothic-ExtraBold.ttf') format('truetype');
  font-weight: 800; font-style: normal; font-display: swap;
}

/* ========= 접근성/시니어 친화 기본값 ========= */
.find-senior-center {
  --brand: #3074FF;
  --brand-hover: #2966E6;
  --brand-active: #2359CC;

  min-height: 100vh;
  background: #ffffff;
  color: #111;
  line-height: 1.55;

  font-family: 'KoddiUDOnGothic', -apple-system, BlinkMacSystemFont,
               'Segoe UI', Roboto, 'Noto Sans KR', 'Apple SD Gothic Neo',
               'Malgun Gothic', system-ui, sans-serif;

  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

:focus-visible {
  outline: 3px solid var(--brand);
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

/* ========= 검색 영역 (사이즈 통일) ========= */
.search-box {
  display: flex;
  align-items: stretch;
  gap: 14px;
  background: #fff;
  border: 2px solid #1e1e1e;
  border-radius: 12px;
  padding: 10px 12px;
  max-width: 720px;
  width: 100%;
  box-sizing: border-box;
}

.type-select,
.search-input,
.search-btn {
  height: 56px;
  font-size: 22px;
  line-height: 56px;
}

.type-select {
  padding: 0 12px;
  border: 2px solid #b7b7b7;
  border-radius: 8px;
  min-width: 140px;
}

.search-input {
  padding: 0 16px;
  border: none;
  outline: none;
  width: 100%;
  background: transparent;
}
.search-input::placeholder {
  font-size: 22px;
  color: #9aa1a8;
}

.search-btn {
  font-weight: 800;
  min-width: 140px;
  padding: 0 18px;
  background: var(--brand);
  color: #fff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: transform .05s ease, background .15s ease;
}
.search-btn:hover { background: var(--brand-hover) }
.search-btn:active { transform: translateY(1px); background: var(--brand-active) }
.search-btn:disabled { opacity: .6; cursor: not-allowed }

/* 주소 안내 문구 */
.addr-hint {
  margin-top: 12px;
  font-size: 14px;
  color: #666;
}

/* ========= 결과 테이블 ========= */
.result-table {
  width: 100%;
  max-width: 980px;
  margin-top: 20px;
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
  outline: 3px solid var(--brand);
  outline-offset: 2px;
  border-radius: 4px;
}

.cell-check { text-align: center }

.row-check input {
  width: 26px;
  height: 26px;
  cursor: pointer;
}

/* 버튼 공통 */
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
.confirm-btn { background: var(--brand); color: #fff }
.confirm-btn:hover { background: var(--brand-hover) }
.confirm-btn:active { background: var(--brand-active) }
.cancel-btn { background: #a6a6a6; color: #fff }
.cancel-btn:hover { background: #8d8d8d }

/* 모달 버튼 크기 */
.confirm-btn.lg, .cancel-btn.lg {
  min-width: 140px; min-height: 52px; font-size: 20px;
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

/* 가로 폭 축소: 최대 420px로 슬림 */
.modal-content {
  background: #fff;
  padding: 28px 22px;
  border-radius: 16px;
  width: min(420px, 92vw);
  box-shadow: 0 6px 24px rgba(0,0,0,.18);
  text-align: center;
}

.modal-title { font-size: 26px; font-weight: 800; margin-bottom: 10px }
.modal-text  { font-size: 18px; margin: 0 0 16px }

.modal-actions {
  display: flex; justify-content: center; margin: 10px 0 18px;
}
.agree {
  font-size: 18px; display: flex; align-items: center; gap: 10px;
}
.agree input { width: 22px; height: 22px }

/* ========= 반응형 ========= */
@media (max-width: 820px) {
  .headline { font-size: 38px }
  .search-box { min-width: 100%; padding: 10px 12px }
  .type-select,
  .search-input,
  .search-btn { height: 52px; font-size: 20px; line-height: 52px }
  .search-input::placeholder { font-size: 20px }
  .result-table { font-size: 18px }
  .result-table thead th { font-size: 20px }
}
</style>
