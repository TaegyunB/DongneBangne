<template>
  <div class="detail-container">
    <!-- 로딩/에러 -->
    <div v-if="loading" class="state">불러오는 중...</div>
    <div v-else-if="error" class="state error">게시글을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.</div>

    <template v-else>
      <div class="header">
        <div class="title-row">
          <span :class="badgeClass(displayCategory(board.category))">
            {{ displayCategory(board.category) }}
          </span>
          <h2 class="title">{{ board.title || '(제목 없음)' }}</h2>
        </div>

        <div class="meta">
          <span>작성자: {{ board.nickname }}</span>
          <span>경로당: {{ board.seniorCenterName }}</span>
          <span>{{ formatCreatedAt(board.createdAt) }}</span>
        </div>
      </div>

      <!-- 이미지 표시 개선 -->
      <div v-if="board.boardImage" class="image">
        <img 
          :src="getBoardImage(board)" 
          alt="게시글 이미지" 
          crossorigin="anonymous"
          @error="onImageError($event, board)"
          @load="onImageLoad($event, board)"
        />
      </div>

      <div class="content">{{ board.content }}</div>

      <div class="footer">
        <!-- 좋아요 -->
        <button class="like-button" :disabled="likeBusy" @click="toggleLike">
          <span class="thumb" :class="{ on: liked }">👍</span>
          <span class="count">{{ likeCount }}</span>
          <span class="label">{{ liked ? '좋아요 취소' : '좋아요' }}</span>
        </button>

        <div class="spacer"></div>

        <!-- 작성자만 수정/삭제 노출 -->
        <template v-if="isOwner">
          <RouterLink
            :to="{ name:'communityEdit', params:{ boardId: board.boardId }, query:{ category: listQueryCategory } }"
            class="edit-button"
          >수정</RouterLink>
          <button class="delete-button" :disabled="deleting" @click="openConfirm">삭제</button>
        </template>

        <button class="back-button" @click="goBack">목록으로</button>
      </div>
    </template>

    <!-- 삭제 확인 모달 -->
    <div v-if="showConfirm" class="modal-backdrop" @click.self="closeConfirm">
      <div class="modal" role="dialog" aria-modal="true" aria-labelledby="confirmTitle">
        <h3 id="confirmTitle" class="modal-title">삭제하시겠어요?</h3>
        <p class="modal-desc">이 작업은 되돌릴 수 없습니다.</p>
        <div class="modal-actions">
          <button class="modal-cancel" @click="closeConfirm">취소</button>
          <button class="modal-danger" :disabled="deleting" @click="confirmDelete">
            {{ deleting ? '삭제 중...' : '삭제' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'
import { getAccessToken } from '@/utils/token'
import defaultImage from '@/assets/default_image.png'

const route = useRoute()
const router = useRouter()
const boardId = computed(() => route.params.boardId)

const loading = ref(true)
const error = ref(false)

const board = ref({
  boardId: null,
  userId: null,
  nickname: '',
  seniorCenterName: '',
  title: '',
  content: '',
  category: '',
  boardImage: null,
  likeCount: 0,
  createdAt: ''
})

const likeBusy = ref(false)
const liked = ref(false)
const likeCount = ref(0)

const deleting = ref(false)
const showConfirm = ref(false)

const me = ref(null)

// 이미지 처리 함수들 추가
const getBoardImage = (boardData) => {
  console.log('=== 게시글 이미지 디버깅 ===')
  console.log('Board 객체:', boardData)
  console.log('Board Image URL:', boardData.boardImage)
  
  if (!boardData.boardImage) {
    console.log('📷 게시글 이미지 URL 없음 - 기본 이미지 사용')
    return defaultImage
  }
  
  if (boardData.boardImage.includes('amazonaws.com') || 
      boardData.boardImage.includes('s3')) {
    console.log('✅ 게시글 S3 URL 감지:', boardData.boardImage)
  }
  
  return boardData.boardImage
}

// 이미지 에러 핸들링
const onImageError = (event, boardData) => {
  console.error('❌ 게시글 이미지 로드 실패:', {
    src: event.target.src,
    boardId: boardData.boardId,
    boardImage: boardData.boardImage,
    error: event,
    errorType: event.target.src.includes('s3') ? 'S3 CORS/권한 문제' : '기타 오류'
  })
  
  if (event.target.src.includes('s3') || event.target.src.includes('amazonaws')) {
    console.warn('🔒 게시글 S3 이미지 로드 실패 - CORS 또는 권한 문제일 가능성')
  }
  
  // 기본 이미지로 대체
  event.target.src = defaultImage
}

const onImageLoad = (event, boardData) => {
  console.log('✅ 게시글 이미지 로드 성공:', {
    src: event.target.src,
    boardId: boardData.boardId
  })
}

// 토큰이 있으면 헤더에 추가(없으면 쿠키로만 요청)
const headersWithToken = () => {
  const token = getAccessToken?.()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

// 선택: 내 정보
const fetchMe = async () => {
  try {
    const { data } = await api.get('/api/v1/users/me', { headers: headersWithToken() })
    me.value = data
  } catch { /* 미제공/비로그인 시 무시 */ }
}

// 카테고리
const apiToKo = { ALL:'전체', POPULAR:'인기', CHAT:'잡담', SHARE:'나눔', INFO:'정보', HOBBY:'취미' }
const koToQuery = { 전체:'all', 인기:'popular', 잡담:'chat', 나눔:'share', 정보:'info', 취미:'hobby' }
const displayCategory = code => apiToKo[String(code||'').toUpperCase()] || code
const listQueryCategory = computed(() => koToQuery[displayCategory(board.value.category)] || 'all')

const badgeClass = (ko) => {
  const base = 'badge'
  const map = {
    '전체':'badge badge--all',
    '인기':'badge badge--popular',
    '잡담':'badge badge--chat',
    '나눔':'badge badge--share',
    '정보':'badge badge--info',
    '취미':'badge badge--hobby'
  }
  return map[ko] || base
}

// 시간 포맷
const formatCreatedAt = (s, { thresholdHours=24 } = {}) => {
  if (!s) return ''
  const d = new Date(s)
  if (Number.isNaN(d.getTime())) return String(s).replace('T',' ').slice(0,16)
  let diff = Math.floor((Date.now()-d.getTime())/1000)
  if (diff < 0) diff = 0
  const min = Math.floor(diff/60), hr = Math.floor(min/60)
  if (hr < thresholdHours) {
    if (diff < 60) return '방금 전'
    if (min < 60) return `${min}분 전`
    return `${hr}시간 전`
  }
  const y=d.getFullYear(), m=String(d.getMonth()+1).padStart(2,'0'), day=String(d.getDate()).padStart(2,'0')
  const hh=String(d.getHours()).padStart(2,'0'), mm=String(d.getMinutes()).padStart(2,'0')
  return `${y}-${m}-${day} ${hh}:${mm}`
}

const normalize = (raw) => ({
  ...raw,
  category: (raw?.category || '').toString().toUpperCase(),
  likeCount: Number(raw?.likeCount ?? 0),
})

// 작성자 판별(가능한 정보로만)
const isOwner = computed(() => {
  const b = board.value
  if ('isOwner' in b) return !!b.isOwner
  if ('mine' in b) return !!b.mine
  if (b.userId && me.value?.userId) return b.userId === me.value.userId
  if (b.nickname && me.value?.nickname) return b.nickname === me.value.nickname
  return false
})

const fetchDetail = async () => {
  loading.value = true
  error.value = false
  try {
    const { data } = await api.get(`/api/v1/boards/${boardId.value}`, {
      headers: headersWithToken()
    })
    const row = normalize(data || {})
    board.value = row
    likeCount.value = row.likeCount
    liked.value = Boolean(row?.liked ?? row?.isLiked ?? false)
  } catch (e) {
    const status = e?.response?.status
    if (status === 401) {
      alert('로그인이 필요합니다.')
      router.push({ name: 'onboarding' })
      return
    }
    console.error('상세 조회 실패:', e)
    error.value = true
  } finally {
    loading.value = false
  }
}

const toggleLike = async () => {
  if (likeBusy.value) return
  likeBusy.value = true

  // 낙관적 업데이트
  const prevLiked = liked.value
  const prevCount = likeCount.value
  liked.value = !prevLiked
  likeCount.value = prevLiked ? Math.max(0, prevCount - 1) : prevCount + 1

  try {
    // 백엔드가 POST 토글 방식이므로 항상 POST만 사용
    const { data } = await api.post(`/api/v1/boards/${boardId.value}/like`, null, { 
      headers: headersWithToken() 
    })
    
    // 백엔드 응답으로 실제 상태 동기화
    if (data) {
      liked.value = data.isLiked ?? data.liked ?? !prevLiked
      likeCount.value = data.likeCount ?? data.like_count ?? likeCount.value
    }
    
  } catch (e) {
    // 실패 시 롤백
    liked.value = prevLiked
    likeCount.value = prevCount
    
    const status = e?.response?.status
    if (status === 401) {
      alert('로그인이 필요합니다.')
      router.push({ name: 'onboarding' })
    } else {
      console.error('좋아요 처리 실패:', e)
      console.error('에러 상세:', {
        status,
        data: e?.response?.data,
        config: e?.config
      })
      alert('좋아요 처리에 실패했습니다. 잠시 후 다시 시도해 주세요.')
    }
  } finally {
    likeBusy.value = false
  }
}

const openConfirm = () => {
  showConfirm.value = true
  document.addEventListener('keydown', onEscClose)
}
const closeConfirm = () => {
  showConfirm.value = false
  document.removeEventListener('keydown', onEscClose)
}
const onEscClose = (e) => {
  if (e.key === 'Escape') closeConfirm()
}

const confirmDelete = async () => {
  if (deleting.value) return
  deleting.value = true
  try {
    await api.delete(`/api/v1/boards/${boardId.value}`, { headers: headersWithToken() })
    alert('삭제되었습니다.')
    closeConfirm()
    goBack()
  } catch (e) {
    const status = e?.response?.status
    if (status === 401) {
      alert('로그인이 필요합니다.')
      router.push({ name: 'onboarding' })
      return
    }
    if (status === 403) {
      alert('삭제 권한이 없습니다.')
      closeConfirm()
      return
    }
    console.error('삭제 실패:', e)
    alert('삭제에 실패했습니다.')
  } finally {
    deleting.value = false
  }
}

const goBack = () => {
  const qcat = typeof route.query.category === 'string' ? route.query.category : listQueryCategory.value
  router.push({ name:'boards', query:{ category: qcat } })
}

onMounted(async () => {
  await Promise.all([fetchMe(), fetchDetail()])
})
watch(boardId, fetchDetail)
onBeforeUnmount(() => document.removeEventListener('keydown', onEscClose))
</script>

<style scoped>
.detail-container{max-width:900px;margin:40px auto;padding:20px;font-family:'Noto Sans KR',sans-serif;border:1px solid #e5e7eb;border-radius:12px;background:#fff}
.state{text-align:center;color:#6b7280;padding:40px 0}.state.error{color:#b91c1c}
.header{margin-bottom:16px}.title-row{display:flex;align-items:center;gap:10px}
.title{font-size:24px;font-weight:800;margin:0;color:#0f172a}
.meta{font-size:13px;color:#6b7280;display:flex;gap:12px;flex-wrap:wrap;margin-top:8px}
.image{margin:12px 0 16px}.image img{width:100%;max-height:520px;object-fit:cover;border-radius:10px;border:1px solid #e5e7eb}
.content{font-size:16px;line-height:1.7;color:#111827;white-space:pre-wrap;border:1px solid #e5e7eb;border-radius:10px;padding:14px}
.footer{display:flex;align-items:center;margin-top:14px;gap:10px}.spacer{flex:1}
.like-button{display:inline-flex;align-items:center;gap:8px;background:#eff6ff;color:#1d4ed8;border:none;border-radius:9999px;padding:8px 12px;cursor:pointer;min-height:36px;font-weight:700}
.like-button:disabled{opacity:.6;cursor:not-allowed}.thumb{font-size:16px}.thumb.on{filter:drop-shadow(0 0 2px rgba(29,78,216,.6))}.count{min-width:16px;text-align:right}.label{font-weight:600}
.back-button,.edit-button,.delete-button{font-size:13px;padding:8px 12px;border-radius:8px;border:1px solid #d1d5db;background:#fff;cursor:pointer}
.back-button:hover,.edit-button:hover,.delete-button:hover{background:#f3f4f6}
.badge{display:inline-flex;align-items:center;padding:4px 10px;border-radius:9999px;font-size:12px;font-weight:700}
.badge--all{background:#f3f4f6;color:#1f2937}.badge--popular{background:#fee2e2;color:#b91c1c}.badge--chat{background:#dbeafe;color:#1e40af}
.badge--share{background:#dcfce7;color:#14532d}.badge--info{background:#ede9fe;color:#6d28d9}.badge--hobby{background:#ffedd5;color:#9a3412}

/* 모달 */
.modal-backdrop{position:fixed;inset:0;background:rgba(15,23,42,.45);display:flex;align-items:center;justify-content:center;padding:16px;z-index:50}
.modal{width:100%;max-width:420px;background:#fff;border-radius:12px;border:1px solid #e5e7eb;box-shadow:0 10px 30px rgba(2,6,23,.2);padding:18px}
.modal-title{font-size:18px;font-weight:800;color:#0f172a;margin:0 0 6px}
.modal-desc{font-size:14px;color:#475569;margin:0 0 14px}
.modal-actions{display:flex;justify-content:flex-end;gap:8px}
.modal-cancel{border:1px solid #d1d5db;background:#fff;border-radius:8px;padding:8px 12px;cursor:pointer}
.modal-danger{border:none;background:#ef4444;color:#fff;border-radius:8px;padding:8px 12px;cursor:pointer}
.modal-cancel:hover{background:#f3f4f6}
.modal-danger:hover{background:#dc2626}
</style>