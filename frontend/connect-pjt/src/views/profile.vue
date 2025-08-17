<template>
  <div class="profile-page">
    <h1 class="page-title">내 프로필</h1>

    <div class="grid-2">
      <!-- === 내 정보 === -->
      <section class="card">
        <h2 class="card-title">내 정보</h2>

        <div class="top-row">
          <img
            v-if="imageOk && (blobUrl || me.profileImage)"
            :src="blobUrl || normalizeImageUrl(me.profileImage)"
            alt="프로필 이미지"
            class="avatar"
            crossorigin="anonymous"
            @error="onImgError"
          />
          <div v-else class="avatar-fallback" aria-hidden="true">{{ initials }}</div>

          <div class="name-block">
            <!-- 이름 + 역할 + (우측) 수정버튼 -->
            <div class="name-line">
              <div class="name-and-role">
                <span class="nickname">{{ me.nickname || '닉네임 없음' }}</span>
                <!-- ▼ 역할 칩: 매핑된 한글 표시 -->
                <span class="role-chip" :title="roleLabel">{{ roleLabel }}</span>
              </div>
              <button class="btn" @click="startEdit" :disabled="loading">닉네임 수정</button>
            </div>

            <!-- 편집 모드 -->
            <div v-if="editing" class="edit-row" role="group" aria-label="닉네임 편집">
              <input
                v-model.trim="formNickname"
                class="input"
                :disabled="submitting"
                placeholder="닉네임 입력 (2~12자)"
                @keyup.enter="save"
                :aria-invalid="!!nickError"
                :aria-errormessage="nickError ? 'nick-error' : undefined"
              />
              <div class="edit-actions">
                <button class="btn primary" @click="save" :disabled="submitting || !canSave">저장</button>
                <button class="btn" @click="cancel" :disabled="submitting">취소</button>
              </div>
              <p v-if="nickError" id="nick-error" class="error">{{ nickError }}</p>
            </div>

            <!-- 포인트 요약 -->
            <div class="stats">
              <div class="stat">
                <span class="stat-label">개인 점수</span>
                <span class="stat-value nowrap" :title="n(me.personalPoint)">{{ n(me.personalPoint) }}</span>
              </div>
              <div class="stat">
                <span class="stat-label">센터 점수</span>
                <span class="stat-value nowrap" :title="n(me.seniorCenter?.totalPoint)">{{ n(me.seniorCenter?.totalPoint) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="loading" class="hint">불러오는 중...</div>
        <p v-if="loadError" class="error mt">{{ loadError }}</p>
      </section>

      <!-- === 내 경로당 === (이전 레이아웃 유지) -->
      <section class="card">
        <h2 class="card-title">내 경로당</h2>

        <div v-if="me.seniorCenter" class="center-block">
          <div class="info">
            <span class="label">이름</span>
            <span class="value no-wrap" :title="me.seniorCenter.centerName">{{ me.seniorCenter.centerName }}</span>
          </div>
          <div class="info">
            <span class="label">주소</span>
            <span class="value" :title="me.seniorCenter.address">{{ me.seniorCenter.address }}</span>
          </div>

          <div class="points">
            <div class="point-box">
              <span class="point-label">트로트</span>
              <span class="point-value no-wrap" :title="n(me.seniorCenter.trotPoint)">
                {{ n(me.seniorCenter.trotPoint) }}
              </span>
            </div>
            <div class="point-box">
              <span class="point-label">도전</span>
              <span class="point-value no-wrap" :title="n(me.seniorCenter.challengePoint)">
                {{ n(me.seniorCenter.challengePoint) }}
              </span>
            </div>
            <div class="point-box">
              <span class="point-label">총 점수</span>
              <span class="point-value no-wrap" :title="n(me.seniorCenter.totalPoint)">
                {{ n(me.seniorCenter.totalPoint) }}
              </span>
            </div>
          </div>
        </div>

        <div v-else class="empty">아직 소속 경로당 정보가 없어요</div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import api from '@/api/axios'

/* 조회/수정: 동일 리소스(닉네임만 수정) */
const ME_ENDPOINT = '/api/v1/main/me'
const NICKNAME_ENDPOINT = '/api/v1/main/me'

const me = reactive({})
const loading = ref(true)
const loadError = ref('')
const imageOk = ref(true)

const editing = ref(false)
const formNickname = ref('')
const nickError = ref('')
const submitting = ref(false)

const blobUrl = ref('')

const initials = computed(() => (me.nickname || '').slice(0, 2) || '유저')
const n = v => (v ?? 0).toLocaleString()

/* 역할 한글 매핑 */
const roleLabel = computed(() => {
  const raw = me.userRole ?? ''
  const key = String(raw).toUpperCase()
  const map = { ADMIN: '대표', MEMBER: '맴버' }
  return map[key] ?? (raw || '-')
})

/* 센터명(접두어로 사용) */
const centerName = computed(() =>
  me.seniorCenter?.centerName || me.seniorCenterName || ''
)

/* 닉네임 조립/제거 */
const buildFinalNickname = (base, center) => {
  const b = (base || '').trim()
  return center ? `${center} ${b}` : b
}
const stripCenterPrefix = (full, center) => {
  const f = (full || '').trim()
  const c = (center || '').trim()
  return c && f.startsWith(c + ' ') ? f.slice(c.length + 1) : f
}

/* 이미지 URL 정규화 */
const normalizeImageUrl = (url) => {
  if (!url) return ''
  let u = String(url).trim()
  if (u.startsWith('data:') || u.startsWith('blob:')) return u
  if (u.startsWith('//')) u = 'https:' + u
  if (u.startsWith('http://')) u = u.replace(/^http:\/\//, 'https://')
  if (/^https?:\/\//.test(u)) return u
  const base = api.defaults?.baseURL || '/'
  const baseAbs = new URL(base, window.location.origin)
  return new URL(u, baseAbs).toString()
}
const isSameOriginAsApi = (url) => {
  try {
    const finalUrl = new URL(normalizeImageUrl(url))
    const base = api.defaults?.baseURL || '/'
    const apiBaseAbs = new URL(base, window.location.origin)
    return finalUrl.origin === apiBaseAbs.origin
  } catch { return false }
}

/* 동일 오리진이면 인증 붙여서 blob 변환 */
const loadProfileImage = async (url) => {
  if (blobUrl.value) { URL.revokeObjectURL(blobUrl.value); blobUrl.value = '' }
  if (!url) return
  if (!isSameOriginAsApi(url)) { imageOk.value = true; return }
  try {
    const finalUrl = normalizeImageUrl(url)
    const { data } = await api.get(finalUrl, { responseType: 'blob', withCredentials: true })
    blobUrl.value = URL.createObjectURL(data)
    imageOk.value = true
  } catch (e) {
    console.error('프로필 이미지 로드 실패', e)
    imageOk.value = false
  }
}
const onImgError = () => { imageOk.value = false }

/* 닉네임 유효성 & 저장 가능 여부(최종 문자열 기준 비교) */
const validateNickname = (nick) => {
  if (!nick) { nickError.value = '닉네임을 입력하세요'; return false }
  if (nick.length < 2 || nick.length > 12) { nickError.value = '2~12자만 가능해요'; return false }
  const ok = /^[\p{L}\p{N}_\- ]+$/u.test(nick)
  if (!ok) { nickError.value = '특수문자 불가 (공백, _ , - 만 허용)'; return false }
  nickError.value = ''
  return true
}
const canSave = computed(() => {
  if (!validateNickname(formNickname.value)) return false
  const finalNickname = buildFinalNickname(formNickname.value, centerName.value)
  return finalNickname !== (me.nickname || '')
})

/* 내 정보 불러오기 */
const fetchMe = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await api.get(ME_ENDPOINT, { withCredentials: true })
    const normalized = { ...data }
    normalized.profileImage = data?.profileImage ?? data?.profile_image ?? data?.imageUrl ?? ''
    Object.assign(me, normalized)
  } catch (e) {
    console.error('GET main/me 실패', e)
    loadError.value = '내 정보를 불러오지 못했어요'
  } finally {
    loading.value = false
  }
}

/* 편집/저장 */
const startEdit = () => {
  editing.value = true
  formNickname.value = stripCenterPrefix(me.nickname || '', centerName.value)
  nickError.value = ''
}
const cancel = () => {
  editing.value = false
  formNickname.value = ''
  nickError.value = ''
}
const save = async () => {
  if (!canSave.value || submitting.value) return
  submitting.value = true
  try {
    const finalNickname = buildFinalNickname(formNickname.value, centerName.value)
    await api.put(NICKNAME_ENDPOINT, { nickname: finalNickname }, { withCredentials: true })
    me.nickname = finalNickname
    editing.value = false
    nickError.value = ''
  } catch (e) {
    console.error('PUT 닉네임 실패', e)
    nickError.value = e?.response?.data?.message || '저장에 실패했어요'
  } finally {
    submitting.value = false
  }
}

onMounted(fetchMe)
watch(() => me.profileImage, loadProfileImage, { immediate: true })
onUnmounted(() => { if (blobUrl.value) URL.revokeObjectURL(blobUrl.value) })
</script>

<style scoped>
/* ===== 폰트 등록 (scoped여도 @font-face는 전역으로 처리됩니다) ===== */
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

/* 🔹 이 한 줄로 페이지 전체에 적용 */
.profile-page{
  font-family: 'KoddiUDOnGothic', -apple-system, BlinkMacSystemFont,
               'Segoe UI', Roboto, 'Noto Sans KR', 'Apple SD Gothic Neo',
               'Malgun Gothic', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
/* 스타일은 이전 메시지와 동일 — 필요 부분만 표시 */
:root { color-scheme: light }
.profile-page { max-width: 1080px; margin: 0 auto; padding: 24px }
.page-title { font-size: 30px; font-weight: 800; letter-spacing: -0.02em; margin-bottom: 16px }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px }
@media (max-width: 880px) { .grid-2 { grid-template-columns: 1fr } }
.card { background: #fff; border: 1px solid #e6e6e6; border-radius: 16px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,.04) }
.card-title { font-size: 22px; font-weight: 800; margin-bottom: 12px }

.top-row { display: flex; align-items: center; gap: 16px; min-width: 0 }
.avatar { width: 96px; height: 96px; border-radius: 50%; object-fit: cover; border: 2px solid #f0f0f0; flex: 0 0 auto }
.avatar-fallback { width: 96px; height: 96px; border-radius: 50%; background:#f5f5f5; display:flex; align-items:center; justify-content:center; font-size: 22px; font-weight: 700 }
.name-block { display: flex; flex-direction: column; gap: 10px; min-width: 0; flex: 1 }

.name-line { display:flex; align-items:center; justify-content:space-between; gap: 12px; flex-wrap: wrap }
.name-and-role { display:flex; align-items:center; gap: 10px; min-width: 0 }
.nickname { font-size: 24px; font-weight: 900; white-space: nowrap }
.role-chip { font-size: 12px; font-weight: 800; padding: 4px 8px; border-radius: 999px; background:#eef2ff; color:#3730a3; white-space: nowrap; border:1px solid #e5e7eb }

.edit-row { display:flex; flex-direction: column; gap: 8px }
.input { font-size: 18px; padding: 10px 12px; border: 1px solid #d5d5d5; border-radius: 10px; min-width: 260px; max-width: 440px }
.input:focus { outline: 3px solid #f5b30155 }
.edit-actions { display:flex; gap:8px }
.btn { font-size: 16px; padding: 10px 14px; border: 1px solid #cfcfcf; background: #fafafa; border-radius: 12px; cursor: pointer }
.btn:hover { background: #f4f4f4 }
.btn[disabled] { opacity:.6; cursor:not-allowed }
.btn.primary { border-color: #3074FF; background: #3074FF; color:#fff }

.stats { display: grid; grid-template-columns: repeat(2, minmax(180px, 1fr)); gap: 12px }
@media (max-width: 520px) { .stats { grid-template-columns: 1fr } }
.stat { border: 1px dashed #e9e9e9; border-radius: 12px; padding: 12px 14px; display:flex; align-items: baseline; justify-content: space-between; min-width: 0 }
.stat-label { color:#666; font-size: 16px; white-space: nowrap }
.stat-value { font-size: 22px; font-weight: 900; min-width: 0; font-variant-numeric: tabular-nums; letter-spacing: -0.01em }
.nowrap { white-space: nowrap; overflow: hidden; text-overflow: ellipsis }

/* 내 경로당/포인트 영역 스타일은 기존과 동일 */
.center-block { display: flex; flex-direction: column; gap: 8px }
.info { display:flex; gap: 8px; font-size: 18px; min-width: 0 }
.label { min-width: 72px; color:#555; white-space: nowrap }
.value { font-weight: 800; min-width: 0 }
.no-wrap { white-space: nowrap; overflow: hidden; text-overflow: ellipsis }
.points { display:grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-top: 10px }
@media (max-width: 640px) { .points { grid-template-columns: 1fr } }
.point-box { border: 1px dashed #e9e9e9; border-radius: 12px; padding: 12px; display:flex; justify-content: space-between; align-items: center; min-width: 0 }
.point-label { color:#666; font-size: 16px; white-space: nowrap }
.point-value { font-size: 20px; font-weight: 800; min-width: 0 }
.point-value.no-wrap { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-variant-numeric: tabular-nums; letter-spacing: -0.01em }

.hint { margin-top: 12px; color:#666; font-size: 16px }
.error { color: #b42318; margin-top: 6px; font-size: 14px }
.mt { margin-top: 12px }
</style>
