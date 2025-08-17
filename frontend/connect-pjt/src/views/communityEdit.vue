<template>
  <div class="write-container">
    <h2 class="page-title">게시글 수정</h2>

    <div v-if="loading" class="hint">불러오는 중...</div>

    <div v-else>
      <div class="form-group">
        <label for="category">카테고리</label>
        <select id="category" v-model="form.category" :disabled="submitting">
          <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
        </select>
      </div>

      <div class="form-group">
        <label for="title">제목</label>
        <input id="title" type="text" v-model.trim="form.title" :disabled="submitting" />
      </div>

      <div class="form-group">
        <label for="content">내용</label>
        <textarea id="content" v-model.trim="form.content" :disabled="submitting"></textarea>
      </div>

      <!-- (선택) 이미지 변경은 보류: 미리보기만 표시 -->
      <div class="form-group" v-if="form.boardImage">
        <label>현재 이미지</label>
        <div class="preview">
          <img :src="form.boardImage" alt="현재 이미지" />
        </div>
      </div>

      <div class="button-row">
        <button class="submit-button" :disabled="submitting" @click="submitEdit">
          {{ submitting ? '수정 중...' : '수정 완료' }}
        </button>
        <button class="cancel-button" :disabled="submitting" @click="goBack">취소</button>
      </div>
    </div>

    <!-- 안내 모달 -->
    <div v-if="notice.open" class="modal-backdrop" @click.self="closeNotice" @keydown.esc="closeNotice">
      <div class="modal" role="dialog" aria-modal="true" aria-labelledby="noticeTitle" aria-describedby="noticeDesc" tabindex="-1">
        <h3 id="noticeTitle" class="modal-title">{{ notice.title }}</h3>
        <p id="noticeDesc" class="modal-desc">{{ notice.message }}</p>
        <div class="modal-actions">
          <button class="modal-primary" @click="closeNotice" autofocus>확인</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'
import { getAccessToken } from '@/utils/token'

const route = useRoute()
const router = useRouter()
const boardId = computed(() => route.params.boardId)

const categories = ['잡담','나눔','정보','취미']
const uiToApiLower = { 잡담:'chat', 나눔:'share', 정보:'info', 취미:'hobby' }
const apiToUi = { CHAT:'잡담', SHARE:'나눔', INFO:'정보', HOBBY:'취미' }

const loading = ref(true)
const submitting = ref(false)

const form = ref({
  category: '잡담',
  title: '',
  content: '',
  boardImage: null,   // 표시용(수정 요청엔 파일만 보냄)
})

// 상세 원본 + 소유자 판별용
const detail = ref(null)
const me = ref(null)

/* ===== 안내 모달 ===== */
const notice = ref({ open: false, title: '안내', message: '', onClose: null })
const showNotice = (message, title = '안내', onClose = null) => {
  notice.value = { open: true, title, message, onClose }
}
const closeNotice = () => {
  const cb = notice.value.onClose
  notice.value.open = false
  notice.value.onClose = null
  if (typeof cb === 'function') cb()
}

/* ===== 인증/헤더 유틸 ===== */
const headersWithToken = () => {
  const token = getAccessToken?.()
  return token ? { Authorization: `Bearer ${token}` } : {}
}
const isOauthRedirectError = (err) => {
  const url = err?.request?.responseURL || ''
  return url.includes('kauth.kakao.com') || url.includes('/oauth2/authorization')
}

/* ===== URL 보정(상대경로 → 절대경로) ===== */
const normalizeUrl = (url) => {
  if (!url) return ''
  let u = String(url).trim()
  if (u.startsWith('data:') || u.startsWith('blob:')) return u
  if (u.startsWith('//')) u = 'https:' + u
  if (u.startsWith('http://')) u = u.replace(/^http:\/\//, 'https://')
  if (/^https?:\/\//.test(u)) return u
  const base = api.defaults?.baseURL || '/'
  return new URL(u.replace(/^\//,''), new URL(base, window.location.origin)).toString()
}

/* ===== 오너 판별 ===== */
const isOwner = computed(() => {
  const b = detail.value
  if (!b) return false
  if ('isOwner' in b) return !!b.isOwner
  if ('mine' in b) return !!b.mine
  if (b.userId && me.value?.userId) return b.userId === me.value.userId
  if (b.nickname && me.value?.nickname) return b.nickname === me.value.nickname
  return false
})
const canDecideOwnership = computed(() => {
  const b = detail.value
  if (!b) return false
  return ('isOwner' in b) || ('mine' in b) || (b.userId && me.value?.userId) || (b.nickname && me.value?.nickname)
})

/* ===== API ===== */
const fetchMe = async () => {
  try {
    const { data } = await api.get('/api/v1/main/me', {
      headers: headersWithToken(),
      withCredentials: true,           // ✅ 쿠키 동반
    })
    me.value = data
  } catch {
    me.value = null
  }
}

const fetchDetail = async () => {
  const { data } = await api.get(`/api/v1/boards/${boardId.value}`, {
    headers: headersWithToken(),
    withCredentials: true,             // ✅ 쿠키 동반
  })
  detail.value = data
  form.value = {
    category: apiToUi[String(data?.category || '').toUpperCase()] || '잡담',
    title: data?.title || '',
    content: data?.content || '',
    boardImage: normalizeUrl(data?.boardImage || ''),   // ✅ 절대 URL로 보정
  }
}

/* ===== lifecycle ===== */
onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([fetchMe(), fetchDetail()])
    if (canDecideOwnership.value && !isOwner.value) {
      showNotice('수정 권한이 없습니다.', '권한 없음', () => {
        router.replace({ name: 'communityDetail', params: { boardId: boardId.value }, query: route.query })
      })
      return
    }
  } catch (e) {
    if (isOauthRedirectError(e) || (!e.response && e.code === 'ERR_NETWORK')) {
      showNotice('로그인이 필요합니다.', '안내', () => router.push({ name: 'onboarding' }))
      return
    }
    const s = e?.response?.status
    if (s === 401) {
      showNotice('로그인이 필요합니다.', '안내', () => router.push({ name: 'onboarding' }))
      return
    }
    if (s === 404) {
      showNotice('게시글을 찾을 수 없습니다.', '안내', () => router.replace({ name: 'boards', query: route.query }))
      return
    }
    console.error('상세 불러오기 실패:', e)
    showNotice('게시글을 불러오지 못했습니다.', '오류', () => {
      router.replace({ name: 'communityDetail', params: { boardId: boardId.value }, query: route.query })
    })
  } finally {
    loading.value = false
  }
})

/* ===== submit (multipart/form-data) ===== */
const submitEdit = async () => {
  if (submitting.value) return

  if (!form.value.title?.trim() || !form.value.content?.trim()) {
    showNotice('제목과 내용을 입력해 주세요.', '입력 안내')
    return
  }

  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('title', form.value.title.trim())
    fd.append('content', form.value.content.trim())
    fd.append('boardCategory', uiToApiLower[form.value.category] || 'chat')
    // 이미지 변경 UI가 없으므로 imageFile은 미첨부
    // 나중에 변경 기능 넣으면: fd.append('imageFile', file) 형태로 추가

    await api.put(`/api/v1/boards/${boardId.value}`, fd, {
      headers: { ...headersWithToken() }, // ❌ Content-Type 강제 금지(경계값 필요) → axios가 자동 설정
      withCredentials: true,
    })

    showNotice('수정이 완료되었습니다.', '완료', () => {
      router.push({ name:'communityDetail', params:{ boardId: boardId.value }, query: route.query })
    })
  } catch (e) {
    // 카카오로 리다이렉트되면 로그인 필요로 처리
    if (isOauthRedirectError(e) || (!e.response && e.code === 'ERR_NETWORK')) {
      showNotice('로그인이 필요합니다.', '안내', () => router.push({ name: 'onboarding' }))
      submitting.value = false
      return
    }
    const s = e?.response?.status
    if (s === 401) {
      showNotice('로그인이 필요합니다.', '안내', () => router.push({ name: 'onboarding' }))
    } else if (s === 403) {
      showNotice('수정 권한이 없습니다.', '권한 없음', () => {
        router.push({ name: 'communityDetail', params: { boardId: boardId.value }, query: route.query })
      })
    } else if (s === 400) {
      showNotice('입력값을 다시 확인해 주세요.', '요청 오류')
    } else if (s === 413) {
      showNotice('본문 또는 첨부 크기가 너무 큽니다.', '요청 오류')
    } else {
      console.error('수정 실패:', e)
      showNotice('수정에 실패했습니다. 잠시 후 다시 시도해 주세요.', '오류')
    }
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push({ name:'communityDetail', params:{ boardId: boardId.value }, query: route.query })
}
</script>

<style scoped>
/* ===== 폰트 등록 ===== */
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

.write-container{
  max-width:700px;margin:40px auto;padding:20px;border:1px solid #ddd;border-radius:12px;background:#fff;
  font-family:'KoddiUDOnGothic', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
               'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;
}

.page-title{font-size:26px;font-weight:800;margin-bottom:24px;color:#0f172a}
.hint{color:#6b7280}
.form-group{margin-bottom:20px}
label{display:block;margin-bottom:8px;font-weight:700;color:#111827}
input,select,textarea{width:100%;padding:12px 14px;border:1px solid #ccc;border-radius:10px;font-size:16px;box-sizing:border-box;outline:none;transition:box-shadow .2s,border-color .2s}
input:focus,select:focus,textarea:focus{border-color:#60a5fa;box-shadow:0 0 0 4px rgba(96,165,250,.15)}
textarea{min-height:200px;resize:vertical;line-height:1.6}
.button-row{display:flex;gap:10px;justify-content:flex-end;margin-top:8px}
.submit-button,.cancel-button{cursor:pointer;border:none;border-radius:10px;padding:12px 18px;font-size:16px;min-height:44px;transition:background .2s,transform .1s,box-shadow .2s}
.submit-button{background:#2563eb;color:#fff;box-shadow:0 4px 14px rgba(37,99,235,.25)}
.submit-button:hover{background:#1e40af;transform:translateY(-1px)}
.submit-button:disabled{background:#9db6f7;cursor:not-allowed;transform:none;box-shadow:none}
.cancel-button{background:#f3f4f6;color:#111827}
.cancel-button:hover{background:#e5e7eb;transform:translateY(-1px)}
.preview img{width:140px;height:140px;object-fit:cover;border-radius:10px;border:1px solid #e5e7eb}

/* ===== 안내 모달 ===== */
.modal-backdrop{
  position:fixed; inset:0; background:rgba(15,23,42,.45);
  display:flex; align-items:center; justify-content:center; padding:16px; z-index:1000
}
.modal{
  width:100%; max-width:420px; background:#fff; border-radius:12px;
  border:1px solid #e5e7eb; box-shadow:0 10px 30px rgba(2,6,23,.2); padding:18px
}
.modal-title{ font-size:18px; font-weight:800; color:#0f172a; margin:0 0 6px }
.modal-desc{ font-size:14px; color:#475569; margin:0 0 14px }
.modal-actions{ display:flex; justify-content:flex-end; gap:8px }
.modal-primary{
  border:none; background:#2563eb; color:#fff; border-radius:8px; padding:8px 12px; cursor:pointer
}
.modal-primary:hover{ background:#1e40af }
</style>
