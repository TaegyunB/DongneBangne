<template>
  <div class="page">
    <div class="container">
      <!-- 제목 -->
      <h1 class="title">게시판</h1>

      <!-- 카테고리 + 글쓰기 한 줄 -->
      <div class="category-row">
        <div class="category-buttons">
          <button
            v-for="category in categories"
            :key="category"
            @click="onClickCategory(category)"
            :class="['category-button', { active: selectedCategory === category }]"
          >
            {{ category }}
          </button>
        </div>

        <!-- 글쓰기: 항상 all로 이동 -->
        <RouterLink
          :to="{ name: 'communityWrite', query: { category: 'all' } }"
          class="write-button"
          aria-label="새 글쓰기"
        >
          <svg class="icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 20h9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z" stroke="currentColor" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          글쓰기
        </RouterLink>
      </div>

      <!-- 검색창 -->
      <div class="search-box">
        <svg class="search-icon" viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2" fill="none"></circle>
          <path d="M21 21l-3.6-3.6" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path>
        </svg>
        <input
          type="text"
          v-model="searchQuery"
          placeholder="게시글 검색..."
          class="search-input"
          aria-label="게시글 검색"
        />
      </div>

      <!-- 게시글 리스트 -->
      <div class="list">
        <div v-if="filteredNotices.length === 0 && searched" class="empty">
          <div class="empty-title">검색 결과가 없습니다</div>
          <div class="empty-desc">다른 키워드로 검색해 보세요</div>
        </div>

        <div
          v-for="(item, index) in filteredNotices"
          :key="index"
          @click="goToDetail(item.boardId)"
          class="card"
          role="button"
          tabindex="0"
          @keyup.enter="goToDetail(item.boardId)"
          :aria-label="`게시글 열기: ${item.title || item.content}`"
        >
          <div class="card-body">
            <div class="card-main">
              <div class="card-header">
                <span :class="badgeClass(displayCategory(item.category))">
                  {{ displayCategory(item.category) }}
                </span>
                <span v-if="item.boardImage" class="image-indicator" title="이미지 첨부됨">
                  📎사진 첨부
                </span>
              </div>

              <div class="card-title" :title="item.title || item.content">
                {{ item.title || item.content }}
              </div>

              <div class="card-meta">
                <span class="meta">
                  <span class="meta-icon">👤</span>{{ item.nickname }}
                </span>
                <span class="meta">
                  <span class="meta-icon">🗓️</span>{{ formatCreatedAt(item.createdAt) }}
                </span>
                <span class="meta" v-if="item.commentCount != null">
                  <span class="meta-icon">💬</span>{{ item.commentCount }}
                </span>
              </div>
            </div>

            <div class="likes" aria-label="좋아요 수">
              <span class="meta-icon">👍</span>
              <span class="likes-count">{{ item.likeCount }}</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()
const route = useRoute()

// 화면 표시는 한글
const categories = ['전체', '인기', '잡담', '나눔', '정보', '취미']
const selectedCategory = ref('전체')
const searchQuery = ref('')
const searched = ref(false)
const notices = ref([])

// 표시↔요청 매핑
const categoryToApi = { 전체: 'all', 인기: 'popular', 잡담: 'chat', 나눔: 'share', 정보: 'info', 취미: 'hobby' }
const apiToKorean = { ALL: '전체', POPULAR: '인기', CHAT: '잡담', SHARE: '나눔', INFO: '정보', HOBBY: '취미' }
const queryToKo = { all: '전체', popular: '인기', chat: '잡담', share: '나눔', info: '정보', hobby: '취미' }

const displayCategory = code => apiToKorean[code] || code
const getMappedCategory = category => categoryToApi[category] || 'all'

// 응답 → 프론트 표준 형태로 정규화
const normalizeBoard = b => ({
  ...b,
  likeCount: Number(b?.likeCount ?? b?.likes ?? b?.likeCnt ?? b?.like ?? 0),
  category: (b?.category || '').toString().toUpperCase(),
  boardImage: b?.boardImage || b?.image || null,
  commentCount: b?.commentCount != null ? Number(b.commentCount) : null
})

// 목록 조회
const fetchNotices = async () => {
  try {
    const res = await api.get('/api/v1/boards', {
      params: { category: getMappedCategory(selectedCategory.value) }
    })
    const rows = Array.isArray(res.data) ? res.data : []
    notices.value = rows.map(normalizeBoard)
    searched.value = true

    // 목록에 commentCount가 없으면 일부만 하이드레이트
    if (rows.some(r => r.commentCount == null)) {
      await hydrateCommentCounts()
    }
  } catch (error) {
    console.error('게시글 로드 실패:', error)
    searched.value = true
    notices.value = []
  }
}

// 댓글 수 하이드레이트 (최대 20개만)
const MAX_COUNT_FETCH = 20
const hydrateCommentCounts = async () => {
  const targets = notices.value.slice(0, MAX_COUNT_FETCH)
  const jobs = targets.map(n =>
    api.get(`/api/v1/boards/${n.boardId}/comments/count`)
      .then(r => ({
        id: n.boardId,
        count: typeof r.data === 'number' ? r.data : Number(r.data?.count ?? 0)
      }))
      .catch(() => null)
  )
  const results = await Promise.all(jobs)
  const mapCount = Object.fromEntries(results.filter(Boolean).map(x => [x.id, x.count]))
  notices.value = notices.value.map(n => ({ ...n, commentCount: mapCount[n.boardId] ?? n.commentCount ?? 0 }))
}

const onClickCategory = category => {
  if (selectedCategory.value === category) return
  selectedCategory.value = category
  router.replace({ name: 'boards', query: { category: getMappedCategory(category) } })
}

const filteredNotices = computed(() => {
  const q = searchQuery.value.trim()
  if (!q) return notices.value
  return notices.value.filter(n =>
    n.title?.includes(q) || n.content?.includes(q) || n.nickname?.includes(q)
  )
})

// 24시간 이내면 상대시간, 아니면 날짜
const formatCreatedAt = (s, { thresholdHours = 24 } = {}) => {
  if (!s) return ''
  const d = new Date(s)
  if (Number.isNaN(d.getTime())) return String(s).replace('T', ' ').slice(0, 16)
  let diffSec = Math.floor((Date.now() - d.getTime()) / 1000)
  if (diffSec < 0) diffSec = 0
  const diffMin = Math.floor(diffSec / 60)
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < thresholdHours) {
    if (diffSec < 60) return '방금 전'
    if (diffMin < 60) return `${diffMin}분 전`
    return `${diffHour}시간 전`
  }
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}`
}

const badgeClass = (ko) => {
  const base = 'badge'
  const map = {
    '전체': 'badge badge--all',
    '인기': 'badge badge--popular',
    '잡담': 'badge badge--chat',
    '나눔': 'badge badge--share',
    '정보': 'badge badge--info',
    '취미': 'badge badge--hobby'
  }
  return map[ko] || base
}

watch(selectedCategory, fetchNotices)

// 초기값 + 첫 로드
onMounted(() => {
  const qcat = route.query.category
  if (typeof qcat === 'string' && queryToKo[qcat]) selectedCategory.value = queryToKo[qcat]
  fetchNotices()
})

// 뒤로가기 등 쿼리 변경 동기화
watch(() => route.query.category, (q) => {
  if (typeof q === 'string' && queryToKo[q]) {
    if (selectedCategory.value !== queryToKo[q]) selectedCategory.value = queryToKo[q]
  }
})

const goToDetail = boardId => {
  router.push(`/boards/${boardId}`)
}
</script>

<style scoped>
/* ===== 노인 친화 기본값(크게/선명/터치 넓게) ===== */
.page {
  background: #f7f9fc;
  min-height: 100vh;
}
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 16px 48px;
  font-family: 'Noto Sans KR', system-ui, -apple-system, Segoe UI, Roboto, sans-serif;
  font-size: 18px;
}

.title {
  font-size: 30px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 16px;
}

.category-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.category-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
}
.category-button {
  border: 1px solid #9aa3af;
  background: #fff;
  color: #374151;
  padding: 10px 16px;
  min-height: 44px;
  border-radius: 9999px;
  font-size: 16px;
  cursor: pointer;
  transition: background .2s, color .2s, border-color .2s, box-shadow .2s;
}
.category-button:hover {
  background: #f0f7ff;
  border-color: #93c5fd;
  color: #1d4ed8;
}
.category-button.active {
  background: #0b57d0;
  color: #fff;
  border-color: #0b57d0;
  box-shadow: 0 2px 8px rgba(11, 87, 208, .25);
}

.write-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  min-height: 44px;
  font-size: 16px;
  color: #fff;
  background: #2563eb;
  border-radius: 10px;
  text-decoration: none;
  box-shadow: 0 4px 14px rgba(37, 99, 235, .25);
  transition: transform .15s, box-shadow .2s, background .2s;
  white-space: nowrap;
}
.write-button:hover {
  background: #1e40af;
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(30, 64, 175, .35);
}
.write-button .icon { width: 16px; height: 16px; }

.category-button:focus-visible,
.write-button:focus-visible,
.search-input:focus-visible,
.card:focus-visible {
  outline: 3px solid #ffbf47;
  outline-offset: 2px;
}

.search-box { position: relative; margin: 16px 0 24px; }
.search-icon {
  position: absolute; left: 12px; top: 50%;
  width: 18px; height: 18px; color: #6b7280;
  transform: translateY(-50%);
}
.search-input {
  width: 100%;
  padding: 14px 14px 14px 40px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  font-size: 16px;
  background: #fff;
  transition: border-color .2s, box-shadow .2s;
  outline: none;
}
.search-input:focus {
  border-color: #60a5fa;
  box-shadow: 0 0 0 4px rgba(96, 165, 250, .15);
}

.list { display: grid; gap: 12px; }
.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(2, 6, 23, .04);
  transition: box-shadow .2s, border-color .2s, transform .08s;
  cursor: pointer;
}
.card:hover {
  border-color: #bfdbfe;
  box-shadow: 0 6px 18px rgba(2, 6, 23, .08);
  transform: translateY(-1px);
}
.card-body {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16px 18px;
  gap: 16px;
}
.card-main { flex: 1; min-width: 0; }

.card-header { 
  margin-bottom: 8px; 
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  margin: 6px 0 10px;
  line-height: 1.6;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.card-meta {
  display: flex; flex-wrap: wrap; gap: 14px;
  font-size: 16px;
  color: #4b5563;
}
.meta { display: inline-flex; align-items: center; gap: 6px; }
.meta-icon { display: inline-block; width: 18px; text-align: center; }

.likes {
  display: inline-flex; align-items: center; gap: 6px;
  background: #eff6ff; color: #1d4ed8;
  padding: 8px 12px; border-radius: 9999px;
  font-weight: 700; min-height: 32px; white-space: nowrap;
}
.likes-count { min-width: 18px; text-align: right; }

.badge {
  display: inline-flex; align-items: center;
  padding: 6px 12px; border-radius: 9999px;
  font-size: 14px; font-weight: 700;
}
.badge--all     { background: #f3f4f6; color: #1f2937; }
.badge--popular { background: #fee2e2; color: #b91c1c; }
.badge--chat    { background: #dbeafe; color: #1e40af; }
.badge--share   { background: #dcfce7; color: #14532d; }
.badge--info    { background: #ede9fe; color: #6d28d9; }
.badge--hobby   { background: #ffedd5; color: #9a3412; }

.image-indicator {
  font-size: 11px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 3px 6px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  font-weight: 500;
  white-space: nowrap;
}

.empty {
  background: #fff; border: 1px solid #e5e7eb;
  border-radius: 12px; text-align: center; padding: 40px 16px;
}
.empty-title { color: #9ca3af; font-size: 18px; margin-bottom: 6px }
.empty-desc  { color: #6b7280; font-size: 16px }

@media (prefers-reduced-motion: reduce) {
  .card:hover, 
  .write-button:hover { transform: none; }
}
@media (max-width: 640px) {
  .card-body { padding: 14px; }
  .likes { padding: 6px 12px; }
}
</style>
