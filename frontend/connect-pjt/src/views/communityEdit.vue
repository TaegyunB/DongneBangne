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
const uiToApiLower = { 잡담:'CHAT', 나눔:'SHARE', 정보:'INFO', 취미:'HOBBY' }
const apiToUi = { CHAT:'잡담', SHARE:'나눔', INFO:'정보', HOBBY:'취미' }

const loading = ref(true)
const submitting = ref(false)

const form = ref({
  category: '잡담',
  title: '',
  content: '',
  boardImage: null,
})

// 상세 원본 + 소유자 판별용
const detail = ref(null)
const me = ref(null)

// 토큰 헤더
const headersWithToken = () => {
  const token = getAccessToken?.()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

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

const fetchMe = async () => {
  try {
    const { data } = await api.get('/api/v1/main/me', { headers: headersWithToken() })
    me.value = data
  } catch {}
}

const fetchDetail = async () => {
  const { data } = await api.get(`/api/v1/boards/${boardId.value}`, {
    headers: headersWithToken()
  })
  detail.value = data
  form.value = {
    category: apiToUi[String(data?.category || '').toUpperCase()] || '잡담',
    title: data?.title || '',
    content: data?.content || '',
    boardImage: data?.boardImage || null,
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([fetchMe(), fetchDetail()])
    if (canDecideOwnership.value && !isOwner.value) {
      alert('수정 권한이 없습니다.')
      router.replace({ name: 'communityDetail', params: { boardId: boardId.value }, query: route.query })
      return
    }
  } catch (e) {
    const s = e?.response?.status
    if (s === 401) {
      alert('로그인이 필요합니다.')
      router.push({ name: 'onboarding' })
      return
    }
    if (s === 404) {
      alert('게시글을 찾을 수 없습니다.')
      router.replace({ name: 'boards', query: route.query })
      return
    }
    console.error('상세 불러오기 실패:', e)
    alert('게시글을 불러오지 못했습니다.')
    router.replace({ name: 'communityDetail', params: { boardId: boardId.value }, query: route.query })
  } finally {
    loading.value = false
  }
})

const submitEdit = async () => {
  console.log('[edit] baseURL:', api.defaults.baseURL)
  console.log('[edit] has token?', !!getAccessToken?.())
  console.log('[edit] form.category ->', form.value.category)
  if (submitting.value) return

  if (!form.value.title?.trim() || !form.value.content?.trim()) {
    alert('제목과 내용을 입력해 주세요.')
    return
  }

  submitting.value = true
  try {
    const body = {
      title: form.value.title.trim(),
      content: form.value.content.trim(),
      // boardCategory: uiToApiLower[form.value.category] || 'CHAT' // 필요시 .toUpperCase()
      // 이미지 변경 없음: 이미지 필드 전송하지 않음
      boardCategory: { '잡담':'CHAT','나눔':'SHARE','정보':'INFO','취미':'HOBBY' }[form.value.category] || 'CHAT'
    }

    await api.put(`/api/v1/boards/${boardId.value}`, body, {
      headers: {
        'Content-Type': 'application/json',
        ...headersWithToken()
      },
      withCredentials: true
    })

    // 디버깅용 (원인 찾고나면 지워도 됨)
    const t = getAccessToken?.()
    if (!t) console.warn('액세스 토큰 없음: 로그인 만료로 401 가능')

    alert('수정이 완료되었습니다.')
    router.push({ name:'communityDetail', params:{ boardId: boardId.value }, query: route.query })
  } catch (e) {
    const s = e?.response?.status
    if (s === 401) {
      alert('로그인이 필요합니다.')
      router.push({ name: 'onboarding' })
      return
    }
    if (s === 403) {
      alert('수정 권한이 없습니다.')
      router.push({ name: 'communityDetail', params: { boardId: boardId.value }, query: route.query })
      return
    }
    console.error('수정 실패:', e)
    alert('수정에 실패했습니다. 잠시 후 다시 시도해 주세요.')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push({ name:'communityDetail', params:{ boardId: boardId.value }, query: route.query })
}
</script>

<style scoped>
.write-container{max-width:700px;margin:40px auto;padding:20px;border:1px solid #ddd;border-radius:12px;background:#fff;font-family:'Noto Sans KR',sans-serif}
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
</style>