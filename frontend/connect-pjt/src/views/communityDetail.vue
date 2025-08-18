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
          <span>작성자: {{ boardAuthor }}</span>
          <span>경로당: {{ board.seniorCenterName }}</span>
          <span>{{ formatCreatedAt(board.createdAt) }}</span>
        </div>
      </div>

      <!-- 이미지 표시 -->
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

      <!-- ===== 댓글 섹션 ===== -->
      <div class="comments">
        <h3 class="comments-title">
          댓글 <span class="count">{{ commentCount }}</span>
        </h3>

        <!-- 작성 -->
        <div v-if="me" class="comment-editor">
          <textarea
            v-model.trim="newComment"
            :disabled="commentBusy"
            placeholder="댓글을 입력하세요"
            aria-label="댓글 입력"
          ></textarea>
          <button class="comment-submit" :disabled="commentBusy || !newComment" @click="createComment">
            {{ commentBusy ? '등록 중...' : '댓글 등록' }}
          </button>
        </div>

        <!-- 목록 -->
        <div class="comment-list">
          <div v-for="c in comments" :key="c.commentId" class="comment-item">
            <div class="comment-meta">
              <span class="author">{{ c.nickname }}</span>
              <span class="date">{{ formatCreatedAt(c.createdAt) }}</span>
            </div>

            <!-- 편집 모드 -->
            <div v-if="editTargetId === c.commentId" class="comment-editing">
              <textarea v-model.trim="editContent" :disabled="commentBusy" aria-label="댓글 수정"></textarea>
              <div class="edit-actions">
                <button class="btn" :disabled="commentBusy" @click="cancelEdit">취소</button>
                <button class="btn primary" :disabled="commentBusy || !editContent" @click="saveEdit(c.commentId)">저장</button>
              </div>
            </div>

            <!-- 보기 모드 -->
            <div v-else class="comment-content">{{ c.content }}</div>

            <!-- 내 댓글만 조작 (편집 중에는 숨김) -->
            <div class="comment-actions" v-if="isMyComment(c) && editTargetId !== c.commentId">
              <button class="btn" @click="startEdit(c)">수정</button>
              <button class="btn danger" :disabled="commentBusy" @click="openCommentConfirm(c.commentId)">삭제</button>
            </div>
          </div>
        </div>
      </div>
      <!-- ===== /댓글 섹션 ===== -->
    </template>

    <!-- 게시글 삭제 확인 모달 -->
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

    <!-- 댓글 삭제 확인 모달 -->
    <div v-if="showCmtConfirm" class="modal-backdrop" @click.self="closeCmtConfirm">
      <div class="modal" role="dialog" aria-modal="true" aria-labelledby="cmtConfirmTitle">
        <h3 id="cmtConfirmTitle" class="modal-title">댓글을 삭제할까요?</h3>
        <p class="modal-desc">삭제 후에는 복구할 수 없습니다.</p>
        <div class="modal-actions">
          <button class="modal-cancel" @click="closeCmtConfirm">취소</button>
          <button class="modal-danger" :disabled="commentBusy" @click="confirmRemoveComment">
            {{ commentBusy ? '삭제 중...' : '삭제' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 안내 모달 -->
    <div v-if="notice.open" class="modal-backdrop" @click.self="closeNotice">
      <div class="modal" role="dialog" aria-modal="true" aria-labelledby="noticeTitle">
        <h3 id="noticeTitle" class="modal-title">{{ notice.title }}</h3>
        <p class="modal-desc">{{ notice.message }}</p>
        <div class="modal-actions">
          <button class="modal-cancel" @click="closeNotice">확인</button>
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

/* 안내 모달 */
const notice = ref({ open:false, title:'안내', message:'', onClose:null })
const showNotice = (msg, title='안내', onClose=null) => { notice.value = { open:true, title, message:msg, onClose } }
const closeNotice = () => { const cb = notice.value.onClose; notice.value.open = false; notice.value.onClose = null; if (cb) cb() }

/* 댓글 삭제 모달 상태 */
const showCmtConfirm = ref(false)
const targetCommentId = ref(null)
const openCommentConfirm = (commentId) => { targetCommentId.value = commentId; showCmtConfirm.value = true }
const closeCmtConfirm = () => { targetCommentId.value = null; showCmtConfirm.value = false }

/* ========= 이미지 ========= */
const getBoardImage = (boardData) => boardData.boardImage || defaultImage
const onImageError = (event, boardData) => { console.error('이미지 로드 실패:', { src: event.target.src, boardId: boardData.boardId }); event.target.src = defaultImage }
const onImageLoad = () => { /* no-op */ }

/* ========= 헤더 ========= */
const headersWithToken = () => {
  const token = getAccessToken?.()
  return {
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    'X-Requested-With': 'XMLHttpRequest',
    'Accept': 'application/json'
  }
}

/* ========= 로그인 리다이렉트 탐지(서버가 302로 보낼 때) ========= */
const isLoginRedirect = (err) => {
  const url = err?.request?.responseURL || ''
  return url.includes('/login') || url.includes('/oauth2/authorization')
}

/* ========= 제약오류(FK) 탐지 ========= */
const isConstraintError = (e) => {
  const s = e?.response?.status
  const msg = (e?.response?.data?.message || e?.message || '').toLowerCase()
  return s === 409 || /constraint|foreign key|integrity/.test(msg)
}

/* ========= 내 정보 ========= */
const fetchMe = async () => {
  try {
    const { data } = await api.get('/api/v1/main/me', { headers: headersWithToken(), withCredentials: true })
    me.value = data
  } catch {}
}

/* ========= 카테고리 ========= */
const apiToKo = { ALL:'전체', POPULAR:'인기', CHAT:'잡담', SHARE:'나눔', INFO:'정보', HOBBY:'취미' }
const koToQuery = { 전체:'all', 인기:'popular', 잡담:'chat', 나눔:'share', 정보:'info', 취미:'hobby' }
const displayCategory = code => apiToKo[String(code||'').toUpperCase()] || code
const listQueryCategory = computed(() => koToQuery[displayCategory(board.value.category)] || 'all')

const badgeClass = (ko) => {
  const map = {
    '전체':'badge badge--all','인기':'badge badge--popular','잡담':'badge badge--chat',
    '나눔':'badge badge--share','정보':'badge badge--info','취미':'badge badge--hobby'
  }
  return map[ko] || 'badge'
}

/* ========= 시간 포맷 ========= */
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

const normalize = (raw) => ({ ...raw, category: (raw?.category || '').toString().toUpperCase(), likeCount: Number(raw?.likeCount ?? 0) })

/* ========= 소유자 판별 ========= */
const isOwner = computed(() => {
  const b = board.value
  if ('isOwner' in b) return !!b.isOwner
  if ('mine' in b) return !!b.mine
  if (b.userId && me.value?.userId) return b.userId === me.value.userId
  if (b.nickname && me.value?.nickname) return b.nickname === me.value.nickname
  return false
})

/* ========= 상세 ========= */
const fetchDetail = async () => {
  loading.value = true
  error.value = false
  try {
    const { data } = await api.get(`/api/v1/boards/${boardId.value}`, { headers: headersWithToken(), withCredentials: true })
    const row = normalize(data || {})
    board.value = row
    likeCount.value = row.likeCount
    liked.value = Boolean(row?.liked ?? row?.isLiked ?? false)
  } catch (e) {
    // 로그인 문구는 노출하지 않음(필요시 라우터 가드/인터셉터에서 처리)
    if (isLoginRedirect(e) || e?.response?.status === 401) return
    console.error('상세 조회 실패:', e)
    error.value = true
  } finally {
    loading.value = false
  }
}

/* ========= 댓글 ========= */
const comments = ref([])
const commentCount = ref(0)
const newComment = ref('')
const commentBusy = ref(false)
const editTargetId = ref(null)
const editContent = ref('')

const isMyComment = (c) => {
  if (c.userId && me.value?.userId) return c.userId === me.value.userId
  if (c.nickname && me.value?.nickname) return c.nickname === me.value.nickname
  return false
}
const normalizeComment = (raw) => {
  const uid = raw?.userId ?? raw?.user?.id ?? raw?.writerId ?? null
  const nick =
    raw?.nickname ?? raw?.authorNickname ?? raw?.writerNickname ?? raw?.userNickname ?? raw?.user?.nickname ??
    (uid && me.value?.userId && uid === me.value.userId ? me.value?.nickname : null)
  return {
    commentId: raw?.commentId ?? raw?.id,
    userId: uid,
    nickname: nick || '익명',
    content: raw?.content ?? '',
    createdAt: raw?.createdAt ?? raw?.created_at ?? new Date().toISOString(),
  }
}

const boardAuthor = computed(() => {
  const b = board.value
  const candidate = b?.nickname ?? b?.userNickname ?? b?.authorNickname ?? b?.user?.nickname ?? (isOwner.value ? me.value?.nickname : null)
  return candidate || '작성자'
})

const fetchCommentCount = async () => {
  try {
    const { data } = await api.get(`/api/v1/boards/${boardId.value}/comments/count`, { headers: headersWithToken(), withCredentials: true })
    commentCount.value = typeof data === 'number' ? data : Number(data?.count ?? 0)
  } catch { commentCount.value = comments.value.length }
}
const fetchComments = async () => {
  try {
    const { data } = await api.get(`/api/v1/boards/${boardId.value}/comments`, { headers: headersWithToken(), withCredentials: true })
    const rows = Array.isArray(data) ? data : []
    comments.value = rows.map(normalizeComment)
    if (!commentCount.value) commentCount.value = comments.value.length
  } catch (e) { console.error('댓글 목록 불러오기 실패:', e); comments.value = [] }
}
const createComment = async () => {
  if (commentBusy.value || !newComment.value) return
  commentBusy.value = true
  try {
    const body = { content: newComment.value }
    const { data } = await api.post(`/api/v1/boards/${boardId.value}/comments`, body, { headers: headersWithToken(), withCredentials: true })
    const created = normalizeComment({
      ...data,
      nickname: data?.nickname ?? me.value?.nickname,
      userId: data?.userId ?? me.value?.userId,
      content: data?.content ?? newComment.value,
      createdAt: data?.createdAt ?? new Date().toISOString()
    })
    comments.value.unshift(created)
    newComment.value = ''
    commentCount.value += 1
  } catch (e) {
    console.error(e); showNotice('댓글 등록에 실패했습니다.', '오류')
  } finally { commentBusy.value = false }
}
const startEdit = (c) => { editTargetId.value = c.commentId; editContent.value = c.content }
const cancelEdit = () => { editTargetId.value = null; editContent.value = '' }
const saveEdit = async (commentId) => {
  if (commentBusy.value || !editContent.value) return
  commentBusy.value = true
  try {
    const body = { content: editContent.value }
    await api.put(`/api/v1/boards/${boardId.value}/comments/${commentId}`, body, { headers: headersWithToken(), withCredentials: true })
    const idx = comments.value.findIndex(c => c.commentId === commentId)
    if (idx !== -1) comments.value[idx] = { ...comments.value[idx], content: editContent.value }
    cancelEdit()
  } catch (e) {
    if (e?.response?.status === 403) showNotice('수정 권한이 없습니다.', '안내')
    else { console.error(e); showNotice('댓글 수정에 실패했습니다.', '오류') }
  } finally { commentBusy.value = false }
}
const confirmRemoveComment = async () => {
  if (commentBusy.value || !targetCommentId.value) return
  commentBusy.value = true
  try {
    await api.delete(`/api/v1/boards/${boardId.value}/comments/${targetCommentId.value}`, { headers: headersWithToken(), withCredentials: true })
    comments.value = comments.value.filter(c => c.commentId !== targetCommentId.value)
    commentCount.value = Math.max(0, commentCount.value - 1)
    closeCmtConfirm()
  } catch (e) {
    if (e?.response?.status === 403) showNotice('삭제 권한이 없습니다.', '안내')
    else { console.error(e); showNotice('댓글 삭제에 실패했습니다.', '오류') }
  } finally { commentBusy.value = false }
}

/* ========= 좋아요 ========= */
const toggleLike = async () => {
  if (likeBusy.value) return
  likeBusy.value = true

  const prevLiked = liked.value
  const prevCount = likeCount.value
  liked.value = !prevLiked
  likeCount.value = prevLiked ? Math.max(0, prevCount - 1) : prevCount + 1

  try {
    const { data } = await api.post(`/api/v1/boards/${boardId.value}/like`, null, { headers: headersWithToken(), withCredentials: true })
    if (data) {
      liked.value = data.isLiked ?? data.liked ?? !prevLiked
      likeCount.value = data.likeCount ?? likeCount.value
    }
  } catch (e) {
    liked.value = prevLiked
    likeCount.value = prevCount
    console.error('좋아요 처리 실패:', e)
  } finally { likeBusy.value = false }
}

/* ========= 게시글 삭제 ========= */
const openConfirm = () => { showConfirm.value = true; document.addEventListener('keydown', onEscClose) }
const closeConfirm = () => { showConfirm.value = false; document.removeEventListener('keydown', onEscClose) }
const onEscClose = (e) => { if (e.key === 'Escape') closeConfirm() }

/* 댓글 전부 삭제(폴백) */
const deleteAllComments = async () => {
  if (!comments.value.length) await fetchComments()
  for (const c of [...comments.value]) {
    await api.delete(`/api/v1/boards/${boardId.value}/comments/${c.commentId}`, {
      headers: headersWithToken(),
      withCredentials: true
    })
  }
  comments.value = []
  commentCount.value = 0
}

const confirmDelete = async () => {
  if (deleting.value) return
  deleting.value = true
  try {
    // 1차: 곧바로 삭제 시도
    await api.delete(`/api/v1/boards/${boardId.value}`, {
      headers: headersWithToken(),
      withCredentials: true
    })
    closeConfirm()
    goBack()
  } catch (e) {
    // 서버가 로그인으로 리다이렉트했거나 401인 경우: 문구 없이 이동/무시
    if (isLoginRedirect(e) || e?.response?.status === 401) {
      closeConfirm()
      // 필요 시 라우팅 가드/인터셉터에서 처리. 여기서는 메시지 없이 종료.
      return
    }
    // FK 제약: 댓글 먼저 일괄 삭제 후 재시도
    if (isConstraintError(e)) {
      try {
        await deleteAllComments()
        await api.delete(`/api/v1/boards/${boardId.value}`, {
          headers: headersWithToken(),
          withCredentials: true
        })
        closeConfirm()
        goBack()
        return
      } catch (e2) {
        console.error('댓글 삭제 후 재시도 실패:', e2)
        showNotice('댓글이 남아 있어 삭제가 막혔습니다. 잠시 후 다시 시도해 주세요.', '오류')
        return
      }
    }
    // 권한 없음
    if (e?.response?.status === 403) {
      closeConfirm()
      showNotice('삭제 권한이 없습니다.', '안내')
      return
    }
    // 기타
    console.error('삭제 실패:', e)
    showNotice('삭제에 실패했습니다. 잠시 후 다시 시도해 주세요.', '오류')
  } finally {
    deleting.value = false
  }
}

/* ========= 기타 ========= */
const goBack = () => {
  const qcat = typeof route.query.category === 'string' ? route.query.category : listQueryCategory.value
  router.push({ name:'boards', query:{ category: qcat } })
}

onMounted(async () => {
  await Promise.all([fetchMe(), fetchDetail()])
  await Promise.all([fetchComments(), fetchCommentCount()])
})
watch(boardId, async () => {
  await Promise.all([fetchDetail(), fetchComments(), fetchCommentCount()])
})
onBeforeUnmount(() => document.removeEventListener('keydown', onEscClose))
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

/* 기존 .detail-container의 font-family만 아래처럼 변경 */
.detail-container{
  max-width:900px;margin:40px auto;padding:20px;
  font-family: 'KoddiUDOnGothic', -apple-system, BlinkMacSystemFont,
               'Segoe UI', Roboto, 'Noto Sans KR', 'Apple SD Gothic Neo',
               'Malgun Gothic', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  border:1px solid #e5e7eb;border-radius:12px;background:#fff
}

.state{text-align:center;color:#6b7280;padding:40px 0}.state.error{color:#b91c1c}
.header{margin-bottom:16px}.title-row{display:flex;align-items:center;gap:10px}
.title{font-size:24px;font-weight:800;margin:0;color:#0f172a}
.meta{font-size:13px;color:#6b7280;display:flex;gap:12px;flex-wrap:wrap;margin-top:8px}
.image{margin:12px 0 16px}.image img{width:100%;max-height:520px;object-fit:cover;border-radius:10px;border:1px solid #e5e7eb}
.content{font-size:16px;line-height:1.7;color:#111827;white-space:pre-wrap;border:1px solid #e5e7eb;border-radius:10px;padding:14px}

/* 댓글 */
.comments{margin-top:18px;border:1px solid #e5e7eb;border-radius:10px;padding:14px;background:#fafafa}
.comments-title{font-size:18px;font-weight:800;margin:0 0 10px;color:#0f172a}
.comments-title .count{color:#2563eb}
.comment-editor{display:flex;gap:8px;align-items:flex-start;margin-bottom:12px}
.comment-editor textarea{flex:1;min-height:80px;border:1px solid #d1d5db;border-radius:8px;padding:10px;font-size:15px}
.comment-submit{background:#2563eb;color:#fff;border:none;border-radius:8px;padding:10px 14px;cursor:pointer}
.comment-submit:disabled{opacity:.6;cursor:not-allowed}
.comment-list{display:grid;gap:10px}
.comment-item{background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:10px}
.comment-meta{font-size:13px;color:#6b7280;display:flex;gap:8px;margin-bottom:6px}
.comment-content{font-size:16px;line-height:1.6;color:#111827;white-space:pre-wrap}
.comment-actions{display:flex;gap:6px;margin-top:8px}
.btn{border:1px solid #d1d5db;background:#fff;border-radius:6px;padding:6px 10px;cursor:pointer;font-size:13px}
.btn.primary{background:#2563eb;border-color:#2563eb;color:#fff}
.btn.danger{background:#ef4444;border-color:#ef4444;color:#fff}
.comment-editing textarea{width:100%;min-height:80px;border:1px solid #d1d5db;border-radius:8px;padding:10px;font-size:15px}
.edit-actions{display:flex;gap:6px;margin-top:8px}
.comment-login-hint{font-size:14px;color:#6b7280;margin-bottom:10px}

/* 하단 */
.footer{display:flex;align-items:center;margin-top:14px;gap:10px}.spacer{flex:1}
.like-button{display:inline-flex;align-items:center;gap:8px;background:#eff6ff;color:#1d4ed8;border:none;border-radius:9999px;padding:8px 12px;cursor:pointer;min-height:36px;font-weight:700}
.like-button:disabled{opacity:.6;cursor:not-allowed}.thumb{font-size:16px}.thumb.on{filter:drop-shadow(0 0 2px rgba(29,78,216,.6))}.count{min-width:16px;text-align:right}.label{font-weight:600}
.back-button,.edit-button,.delete-button{font-size:13px;padding:8px 12px;border-radius:8px;border:1px solid #d1d5db;background:#fff;cursor:pointer}
.back-button:hover,.edit-button:hover,.delete-button:hover{background:#f3f4f6}
.badge{display:inline-flex;align-items:center;padding:4px 10px;border-radius:9999px;font-size:12px;font-weight:700}
.badge--all{background:#f3f4f6;color:#1f2937}.badge--popular{background:#fee2e2;color:#b91c1c}.badge--chat{background:#dbeafe;color:#1e40af}
.badge--share{background:#dcfce7;color:#14532d}.badge--info{background:#ede9fe;color:#6d28d9}.badge--hobby{background:#ffedd5;color:#9a3412}

/* 모달 (공통) */
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
