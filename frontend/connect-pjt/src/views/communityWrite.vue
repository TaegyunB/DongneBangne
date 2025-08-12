<template>
  <div class="write-container">
    <h2 class="page-title">게시글 작성</h2>

    <div class="form-group">
      <label for="category">카테고리</label>
      <select id="category" v-model="form.category" :disabled="submitting">
        <option disabled value="">카테고리를 선택하세요</option>
        <option v-for="category in categories" :key="category" :value="category">
          {{ category }}
        </option>
      </select>
      <p v-if="errors.category" class="error">{{ errors.category }}</p>
    </div>

    <div class="form-group">
      <label for="title">제목</label>
      <input
        id="title"
        type="text"
        v-model.trim="form.title"
        :disabled="submitting"
        placeholder="제목을 입력하세요"
      />
      <p v-if="errors.title" class="error">{{ errors.title }}</p>
    </div>

    <div class="form-group">
      <label for="content">내용</label>
      <textarea
        id="content"
        v-model.trim="form.content"
        :disabled="submitting"
        placeholder="내용을 입력하세요"
      ></textarea>
      <p v-if="errors.content" class="error">{{ errors.content }}</p>
    </div>

    <!-- 이미지 업로드 (지금은 미리보기만, 전송 X) -->
    <div class="form-group">
      <label for="image">이미지 (선택)</label>
      <input
        id="image"
        type="file"
        accept="image/*"
        :disabled="submitting"
        @change="onFileChange"
      />
      <div v-if="previewUrl" class="preview">
        <img :src="previewUrl" alt="미리보기" />
        <button class="preview-remove" type="button" @click="clearImage" :disabled="submitting">
          이미지 제거
        </button>
      </div>
      <p v-if="errors.image" class="error">{{ errors.image }}</p>
    </div>

    <div class="button-row">
      <button class="submit-button" :disabled="submitting" @click="submitPost">
        {{ submitting ? '등록 중...' : '등록하기' }}
      </button>
      <button class="cancel-button" :disabled="submitting" @click="goBack">취소</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/api/axios' // ✅ 전역 axios 인스턴스만 사용

const router = useRouter()
const route = useRoute()

// 글쓰기 선택 가능한 카테고리 (인기는 제외)
const categories = ['잡담', '나눔', '취미', '정보']
// UI ↔ API(백엔드 요청은 소문자 코드)
const uiToApiLower = { 잡담: 'chat', 나눔: 'share', 취미: 'hobby', 정보: 'info' }

// 폼 상태
const form = ref({ category: '', title: '', content: '' })
const imageFile = ref(null)   // 지금은 서버로 전송하지 않음(미리보기만)
const previewUrl = ref('')
const submitting = ref(false)
const errors = ref({ category: '', title: '', content: '', image: '' })

// URL 쿼리로 넘어온 category 프리셋 적용
onMounted(() => {
  const map = { all: '잡담', popular: '잡담', chat: '잡담', share: '나눔', hobby: '취미', info: '정보' }
  const q = typeof route.query.category === 'string' ? route.query.category : ''
  if (q && map[q]) form.value.category = map[q]
})

// [ADD] presign URL/path를 baseURL와 안전하게 합쳐주는 도우미
const resolvePresignEndpoint = (raw) => {
  const base = (api?.defaults?.baseURL || '').replace(/\/$/, '')
  if (!raw) return `${base}/v1/uploads/presign`   // 디폴트 경로
  if (/^https?:\/\//i.test(raw)) return raw       // 풀 URL
  if (raw.startsWith('/')) return `${base}${raw}` // 경로만 온 경우
  return `${base}/${raw.replace(/^\//, '')}`
}

// [ADD] S3로 실제 업로드 수행 (POST policy or PUT presign 둘 다 지원)
const uploadToS3 = async (presign, file) => {
  // POST 정책 방식
  if (presign?.fields && presign?.uploadUrl) {
    const fd = new FormData()
    Object.entries(presign.fields).forEach(([k, v]) => fd.append(k, String(v)))
    fd.append('file', file) // 필드명은 'file' 고정
    const res = await fetch(presign.uploadUrl, { method: 'POST', body: fd })
    if (!res.ok) throw new Error(`S3 POST 업로드 실패: ${res.status}`)
    return presign.fileUrl || (presign.uploadUrl.replace(/\/$/, '') + '/' + presign.fields.key)
  }

  // PUT 방식
  if (presign?.uploadUrl) {
    const res = await fetch(presign.uploadUrl, {
      method: 'PUT',
      headers: { 'Content-Type': file.type },
      body: file,
    })
    if (!res.ok) throw new Error(`S3 PUT 업로드 실패: ${res.status}`)
    return presign.fileUrl || presign.uploadUrl.split('?')[0]
  }

  throw new Error('presign 응답에 uploadUrl이 없음')
}

// [ADD] 필요하면 presign → S3 업로드 → 최종 URL 반환
const uploadImageIfNeeded = async () => {
  if (!imageFile.value) return null

  // presign 엔드포인트 결정
  const raw = import.meta.env.VITE_IMAGE_PRESIGN_URL || import.meta.env.VITE_IMAGE_PRESIGN_PATH
  const presignEndpoint = resolvePresignEndpoint(String(raw || ''))

  const token = getAccessToken()

  // 1) presign 요청 (백 규격 예: { uploadUrl, fileUrl, method?, fields? })
  const filename = `${crypto.randomUUID?.() || Date.now()}_${imageFile.value.name}`
  const { data: presign } = await api.post(
    presignEndpoint,
    { filename, contentType: imageFile.value.type },
    { headers: { Authorization: `Bearer ${token}` } }
  )

  // 2) S3 업로드
  const finalUrl = await uploadToS3(presign, imageFile.value)
  return finalUrl
}

// 이미지 미리보기만 처리
const onFileChange = (e) => {
  const file = e.target.files?.[0]
  if (!file) { clearImage(); return }
  if (!file.type.startsWith('image/')) {
    errors.value.image = '이미지 파일만 업로드 가능합니다.'
    e.target.value = ''
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    errors.value.image = '파일 크기는 5MB 이하여야 합니다.'
    e.target.value = ''
    return
  }
  errors.value.image = ''
  imageFile.value = file
  previewUrl.value = URL.createObjectURL(file)
}
const clearImage = () => {
  imageFile.value = null
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
}

const validate = () => {
  errors.value = { category: '', title: '', content: '', image: '' }
  let ok = true
  if (!form.value.category) { errors.value.category = '카테고리를 선택해 주세요.'; ok = false }
  if (!form.value.title || form.value.title.length < 2) { errors.value.title = '제목을 2자 이상 입력해 주세요.'; ok = false }
  if (!form.value.content || form.value.content.length < 2) { errors.value.content = '내용을 2자 이상 입력해 주세요.'; ok = false }
  return ok
}

// [UPDATE] submitPost: 이미지 URL 먼저 얻고 body에 넣기
const submitPost = async () => {
  if (submitting.value) return
  if (!validate()) return

  submitting.value = true
  try {

    // const token = getAccessToken()
    // if (!token) {
    //   alert('로그인이 필요합니다. 다시 로그인해 주세요.')
    //   // 로그인 페이지 라우팅(사용 중인 라우트 이름으로 수정 가능)
    //   router.push({ name: 'onboarding' })
    //   return
    // }
    // 이미지 있으면 업로드 → URL 획득
    const imageUrl = await uploadImageIfNeeded()   // ← 추가

    const body = {
      title: form.value.title,
      content: form.value.content,
      boardCategory: uiToApiLower[form.value.category] || 'chat', // 소문자
      imageFile: imageUrl ?? null, // 백 스펙: URL 넘김
    }
    // 토큰이 있으면 헤더에 붙이고, 없으면 withCredentials 쿠키로 진행
    const headers = {}
    try {
      const { getAccessToken } = await import('@/utils/token')
      const t = getAccessToken?.()
      if (t) headers.Authorization = `Bearer ${t}`
    } catch (e) { /* 토큰 유틸 없거나 에러면 무시하고 쿠키로 진행 */ }
    
    await api.post('/api/v1/boards', body, {
    headers
    })

    alert('글이 등록되었습니다.')
    router.push({ name: 'boards', query: { category: body.boardCategory } })
  } catch (err) {
    console.error('등록 실패:', err)
    if (err?.response?.status === 401) {
      alert('세션이 만료되었어요. 다시 로그인해 주세요.')
      router.push({ name: 'onboarding' })
    } else {
    alert('등록에 실패했습니다. 잠시 후 다시 시도해 주세요.')
    }
  } finally {
    submitting.value = false
  }
}

const goBack = () => router.back()
</script>

<style scoped>
.write-container {
  max-width: 700px;
  margin: 40px auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 12px;
  background-color: #fff;
  font-family: 'Noto Sans KR', sans-serif;
  font-size: 18px;
}
.page-title {
  font-size: 26px;
  font-weight: 800;
  margin-bottom: 24px;
  color: #0f172a;
}
.form-group { margin-bottom: 20px; }
label {
  display: block;
  margin-bottom: 8px;
  font-weight: 700;
  color: #111827;
}
input, select, textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #ccc;
  border-radius: 10px;
  font-size: 16px;
  box-sizing: border-box;
  outline: none;
  transition: box-shadow .2s, border-color .2s;
}
input:focus, select:focus, textarea:focus {
  border-color: #60a5fa;
  box-shadow: 0 0 0 4px rgba(96,165,250,.15);
}
textarea {
  min-height: 200px;
  resize: vertical;
  line-height: 1.6;
}
.error {
  color: #d32f2f;
  font-size: 14px;
  margin-top: 6px;
}
.button-row {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 8px;
}
.submit-button, .cancel-button {
  cursor: pointer;
  border: none;
  border-radius: 10px;
  padding: 12px 18px;
  font-size: 16px;
  min-height: 44px;
  transition: background .2s, transform .1s, box-shadow .2s;
}
.submit-button {
  background-color: #2563eb;
  color: #fff;
  box-shadow: 0 4px 14px rgba(37,99,235,.25);
}
.submit-button:hover { background-color: #1e40af; transform: translateY(-1px); }
.submit-button:disabled {
  background-color: #9db6f7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
.cancel-button { background-color: #f3f4f6; color: #111827; }
.cancel-button:hover { background-color: #e5e7eb; transform: translateY(-1px); }

/* 이미지 미리보기 */
.preview {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.preview img {
  width: 140px;
  height: 140px;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
}
.preview-remove {
  background: #f43f5e;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}
.preview-remove:hover { background: #e11d48; }

/* 포커스 링 명확히 */
.submit-button:focus-visible,
.cancel-button:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible {
  outline: 3px solid #ffbf47;
  outline-offset: 2px;
}

/* 모션 최소화 */
@media (prefers-reduced-motion: reduce) {
  .submit-button:hover, .cancel-button:hover { transform: none; }
}
</style>