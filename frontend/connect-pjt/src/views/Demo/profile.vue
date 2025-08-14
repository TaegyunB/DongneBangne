<template>
  <div class="profile-page">
    <h1 class="title">내 프로필</h1>

    <div class="card">
      <!-- 상단: 아바타 + 닉네임 -->
      <div class="top-row">
        <img
          v-if="imageOk && me.profileImage"
          :src="me.profileImage"
          alt="프로필 이미지"
          class="avatar"
          @error="imageOk = false"
        />
        <div v-else class="avatar-fallback">{{ initials }}</div>

        <div class="name-block">
          <div v-if="!editing" class="nickname-row">
            <span class="nickname">{{ me.nickname || '닉네임 없음' }}</span>
            <button class="btn" @click="startEdit" :disabled="loading">닉네임 수정</button>
          </div>

          <div v-else class="edit-row">
            <input
              v-model.trim="formNickname"
              class="input"
              :disabled="submitting"
              placeholder="닉네임 입력 (2~12자)"
              @keyup.enter="save"
            />
            <button class="btn primary" @click="save" :disabled="submitting || !canSave">
              저장
            </button>
            <button class="btn" @click="cancel" :disabled="submitting">취소</button>
          </div>

          <p v-if="nickError" class="error">{{ nickError }}</p>
        </div>
      </div>

      <!-- 기본 정보 -->
      <div class="info-grid">
        <div class="info"><span class="label">역할</span><span class="value">{{ me.userRole }}</span></div>
        <div class="info"><span class="label">개인 포인트</span><span class="value">{{ n(me.personalPoint) }}</span></div>
        <div class="info"><span class="label">총 포인트</span><span class="value">{{ n(me.totalPoint) }}</span></div>
      </div>

      <!-- 경로당 정보 -->
      <div v-if="me.seniorCenter" class="center-card">
        <h2 class="subtitle">내 경로당</h2>
        <div class="info"><span class="label">이름</span><span class="value">{{ me.seniorCenter.centerName }}</span></div>
        <div class="info"><span class="label">주소</span><span class="value">{{ me.seniorCenter.address }}</span></div>

        <div class="points">
          <div><span class="label">트로트</span><span class="value">{{ n(me.trotPoint) }}</span></div>
          <div><span class="label">도전</span><span class="value">{{ n(me.challengePoint) }}</span></div>
        </div>
      </div>

      <div v-if="loading" class="hint">불러오는 중...</div>
      <p v-if="loadError" class="error mt">{{ loadError }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import api from '@/api/axios'

const ME_ENDPOINT = '/api/v1/main/me'
const NICKNAME_ENDPOINT = '/api/v1/main/me/nickname' // 백엔드 경로가 다르면 이것만 바꾸면 됨

const me = reactive({})
const loading = ref(true)
const loadError = ref('')
const imageOk = ref(true)

const editing = ref(false)
const formNickname = ref('')
const nickError = ref('')
const submitting = ref(false)

const initials = computed(() => {
  const t = me.nickname || ''
  return t.slice(0, 2) || '유저'
})

const n = v => (v ?? 0).toLocaleString()

const validateNickname = nick => {
  if (!nick) {
    nickError.value = '닉네임을 입력하세요'
    return false
  }
  if (nick.length < 2 || nick.length > 12) {
    nickError.value = '2~12자만 가능해요'
    return false
  }
  const ok = /^[\p{L}\p{N}_\- ]+$/u.test(nick)
  if (!ok) {
    nickError.value = '특수문자 불가 (공백, _ , - 만 허용)'
    return false
  }
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
    console.error('GET /main/me 실패', e)
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
</script>

<style scoped>
.profile-page { max-width: 880px; margin: 0 auto; padding: 24px }
.title { font-size: 28px; font-weight: 800; margin-bottom: 12px }

.card { background: #fff; border: 1px solid #e6e6e6; border-radius: 16px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,.04) }

.top-row { display: flex; align-items: center; gap: 16px }
.avatar { width: 96px; height: 96px; border-radius: 50%; object-fit: cover; border: 2px solid #f0f0f0 }
.avatar-fallback { width: 96px; height: 96px; border-radius: 50%; background:#f5f5f5; display:flex; align-items:center; justify-content:center; font-size: 22px; font-weight: 700 }

.name-block { display: flex; flex-direction: column; gap: 6px }
.nickname-row, .edit-row { display:flex; align-items:center; gap: 8px; flex-wrap: wrap }
.nickname { font-size: 22px; font-weight: 700 }

.input { font-size: 18px; padding: 10px 12px; border: 1px solid #d5d5d5; border-radius: 10px; min-width: 240px }
.input:focus { outline: 3px solid #f5b30155 }

.btn { font-size: 16px; padding: 10px 14px; border: 1px solid #cfcfcf; background: #fafafa; border-radius: 12px; cursor: pointer }
.btn:hover { background: #f4f4f4 }
.btn.primary { border-color: #f5b301; background: #f5b301; color: #111 }
.btn.primary:disabled { opacity: .6; cursor: not-allowed }

.info-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin: 18px 0 }
.info { display:flex; gap: 8px; font-size: 18px }
.label { min-width: 110px; color:#666 }
.value { font-weight: 700 }

.center-card { border-top: 1px dashed #eee; padding-top: 14px; margin-top: 6px }
.subtitle { font-size: 22px; margin-bottom: 8px }
.points { display:grid; grid-template-columns: repeat(2, 1fr); gap: 10px; margin-top: 8px }

.hint { margin-top: 12px; color:#666; font-size: 16px }
.error { color: #b42318; margin-top: 6px; font-size: 14px }
.mt { margin-top: 12px }

@media (max-width: 640px) {
  .info-grid { grid-template-columns: 1fr }
  .points { grid-template-columns: 1fr }
}
</style>

import Profile from '@/views/profile.vue'

export default createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ...기존 라우트들
    {
      path: '/profile',
      name: 'profile',
      component: Profile,
      meta: { hideToolbar: false }
    }
  ]
})
