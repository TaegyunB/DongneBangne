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
            @error="onImgError"
          />
          <div v-else class="avatar-fallback" aria-hidden="true">{{ initials }}</div>

          <div class="name-block">
            <div v-if="!editing" class="nickname-row">
              <span class="nickname">{{ me.nickname || '닉네임 없음' }}</span>
              <button class="btn" @click="startEdit" :disabled="loading">닉네임 수정</button>
            </div>

            <div v-else class="edit-row" role="group" aria-label="닉네임 편집">
              <input
                v-model.trim="formNickname"
                class="input"
                :disabled="submitting"
                placeholder="닉네임 입력 (2~12자)"
                @keyup.enter="save"
                :aria-invalid="!!nickError"
                :aria-errormessage="nickError ? 'nick-error' : undefined"
              />
              <button class="btn primary" @click="save" :disabled="submitting || !canSave">저장</button>
              <button class="btn" @click="cancel" :disabled="submitting">취소</button>
            </div>

            <p v-if="nickError" id="nick-error" class="error">{{ nickError }}</p>
          </div>
        </div>

        <div class="info-grid">
          <div class="info">
            <span class="label">역할</span>
            <span class="value">{{ me.userRole || '-' }}</span>
          </div>
          <div class="info">
            <span class="label">개인 포인트</span>
            <span class="value">{{ n(me.personalPoint) }}</span>
          </div>
          <div class="info">
            <span class="label">센터 총 포인트</span>
            <span class="value">{{ n(me.seniorCenter?.totalPoint) }}</span>
          </div>
        </div>

        <div v-if="loading" class="hint">불러오는 중...</div>
        <p v-if="loadError" class="error mt">{{ loadError }}</p>
      </section>

      <!-- === 내 경로당 === -->
      <section class="card">
        <h2 class="card-title">내 경로당</h2>

        <div v-if="me.seniorCenter" class="center-block">
          <div class="info">
            <span class="label">이름</span>
            <span class="value">{{ me.seniorCenter.centerName }}</span>
          </div>
          <div class="info">
            <span class="label">주소</span>
            <span class="value">{{ me.seniorCenter.address }}</span>
          </div>

          <div class="points">
            <div class="point-box">
              <span class="point-label">트로트</span>
              <span class="point-value">{{ n(me.seniorCenter.trotPoint) }}</span>
            </div>
            <div class="point-box">
              <span class="point-label">도전</span>
              <span class="point-value">{{ n(me.seniorCenter.challengePoint) }}</span>
            </div>
            <div class="point-box">
              <span class="point-label">총 포인트</span>
              <span class="point-value">{{ n(me.seniorCenter.totalPoint) }}</span>
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

// ⚠️ baseURL에 경로가 포함되어 있어도 안전하게 합쳐지도록 상대경로 사용
// 예) baseURL = https://api.example.com/api/v1  =>  GET {base}/main/me
const ME_ENDPOINT = 'main/me'
const NICKNAME_ENDPOINT = 'main/me/nickname'

const me = reactive({})
const loading = ref(true)
const loadError = ref('')
const imageOk = ref(true)

const editing = ref(false)
const formNickname = ref('')
const nickError = ref('')
const submitting = ref(false)

const blobUrl = ref('')

const initials = computed(() => {
  const t = me.nickname || ''
  return t.slice(0, 2) || '유저'
})

const n = v => (v ?? 0).toLocaleString()

// http → https 업그레이드 + 상대경로를 baseURL 기준 절대화(안전한 결합)
const normalizeImageUrl = url => {
  if (!url) return ''
  let u = url.trim()
  if (u.startsWith('//')) u = 'https:' + u
  if (u.startsWith('http://')) u = u.replace(/^http:\/\//, 'https://')
  if (/^https?:\/\//.test(u)) return u
  // 상대 경로를 baseURL(상대일 수도 있음)과 안전하게 결합
  const base = api.defaults?.baseURL || '/'
  const baseAbs = new URL(base, window.location.origin) // 상대 baseURL 지원
  // u가 'files/...'든 '/files/...'든 동일하게 처리
  const rel = u.startsWith('/') ? u : `/${u}`
  return new URL(rel, baseAbs).toString()
}

// 동일 오리진 판별: baseURL이 상대여도 안전하게 처리
const isSameOriginAsApi = url => {
  try {
    const finalUrl = new URL(normalizeImageUrl(url))
    const base = api.defaults?.baseURL || '/'
    const apiBaseAbs = new URL(base, window.location.origin)
    return finalUrl.origin === apiBaseAbs.origin
  } catch {
    return false
  }
}

const loadProfileImage = async url => {
  blobUrl.value = ''
  if (!url) return
  // 외부 공개 이미지면 blob 불필요
  if (!isSameOriginAsApi(url)) {
    imageOk.value = true
    return
  }
  try {
    const finalUrl = normalizeImageUrl(url)
    const { data } = await api.get(finalUrl, {
      responseType: 'blob',
      withCredentials: true
    })
    blobUrl.value = URL.createObjectURL(data)
    imageOk.value = true
  } catch (e) {
    console.error('프로필 이미지 로드 실패', e)
    imageOk.value = false
  }
}

const onImgError = () => { imageOk.value = false }

const validateNickname = nick => {
  if (!nick) { nickError.value = '닉네임을 입력하세요'; return false }
  if (nick.length < 2 || nick.length > 12) { nickError.value = '2~12자만 가능해요'; return false }
  const ok = /^[\p{L}\p{N}_\- ]+$/u.test(nick)
  if (!ok) { nickError.value = '특수문자 불가 (공백, _ , - 만 허용)'; return false }
  nickError.value = ''
  return true
}

const canSave = computed(() => validateNickname(formNickname.value) && formNickname.value !== me.nickname)

const fetchMe = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await api.get(ME_ENDPOINT, { withCredentials: true })
    Object.assign(me, data || {})
  } catch (e) {
    console.error('GET main/me 실패', e)
    loadError.value = '내 정보를 불러오지 못했어요'
  } finally {
    loading.value = false
  }
}

const startEdit = () => {
  editing.value = true
  formNickname.value = me.nickname || ''
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
    await api.patch(NICKNAME_ENDPOINT, { nickname: formNickname.value }, { withCredentials: true })
    me.nickname = formNickname.value
    editing.value = false
  } catch (e) {
    console.error('PATCH 닉네임 실패', e)
    nickError.value = '저장에 실패했어요'
  } finally {
    submitting.value = false
  }
}

onMounted(fetchMe)

// me.profileImage 변경 시 처리
watch(() => me.profileImage, loadProfileImage, { immediate: true })

onUnmounted(() => {
  if (blobUrl.value) URL.revokeObjectURL(blobUrl.value)
})
</script>

<style scoped>
:root { color-scheme: light }
.profile-page { max-width: 1080px; margin: 0 auto; padding: 24px }
.page-title { font-size: 30px; font-weight: 800; letter-spacing: -0.02em; margin-bottom: 16px }

.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px }
@media (max-width: 880px) { .grid-2 { grid-template-columns: 1fr } }

.card {
  background: #fff; border: 1px solid #e6e6e6; border-radius: 16px;
  padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,.04)
}
.card-title { font-size: 22px; font-weight: 800; margin-bottom: 12px }

/* 내 정보 */
.top-row { display: flex; align-items: center; gap: 16px }
.avatar { width: 96px; height: 96px; border-radius: 50%; object-fit: cover; border: 2px solid #f0f0f0 }
.avatar-fallback {
  width: 96px; height: 96px; border-radius: 50%;
  background:#f5f5f5; display:flex; align-items:center; justify-content:center;
  font-size: 22px; font-weight: 700
}
.name-block { display: flex; flex-direction: column; gap: 6px; min-width: 0 }
.nickname-row, .edit-row { display:flex; align-items:center; gap: 8px; flex-wrap: wrap }
.nickname { font-size: 22px; font-weight: 800 }

.input { font-size: 18px; padding: 10px 12px; border: 1px solid #d5d5d5; border-radius: 10px; min-width: 260px }
.input:focus { outline: 3px solid #f5b30155 }

.btn {
  font-size: 16px; padding: 10px 14px;
  border: 1px solid #cfcfcf; background: #fafafa; border-radius: 12px; cursor: pointer
}
.btn:hover { background: #f4f4f4 }
.btn[disabled] { opacity:.6; cursor:not-allowed }
.btn.primary { border-color: #3074FF; background: #3074FF; color:#fff }

/* 정보 그리드 */
.info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 18px }
@media (max-width: 640px) { .info-grid { grid-template-columns: 1fr } }
.info { display:flex; gap: 8px; font-size: 18px }
.label { min-width: 110px; color:#555 }
.value { font-weight: 800 }
.info-grid .info:first-child .label { min-width: 72px }

/* 모바일에선 어차피 1열이니 더 자연스럽게 */
@media (max-width: 640px) {
  .info-grid .info:first-child .label { min-width: 64px }
}
/* 내 경로당 */
.center-block { display: flex; flex-direction: column; gap: 8px }
.points { display:grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 10px }
@media (max-width: 640px) { .points { grid-template-columns: 1fr } }
.point-box { border: 1px dashed #e9e9e9; border-radius: 12px; padding: 12px; display:flex; justify-content: space-between; align-items: center }
.point-label { color:#666; font-size: 16px }
.point-value { font-size: 20px; font-weight: 800 }
.empty { color:#666; font-size:16px; padding: 12px; background:#fafafa; border-radius:12px }

.hint { margin-top: 12px; color:#666; font-size: 16px }
.error { color: #b42318; margin-top: 6px; font-size: 14px }
.mt { margin-top: 12px }
</style>
