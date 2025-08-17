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

    <!-- 이미지 업로드 -->
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()
const route = useRoute()

// 글쓰기 선택 가능한 카테고리 (인기는 제외)
const categories = ['잡담', '나눔', '취미', '정보']
// UI ↔ API(백엔드 요청은 소문자 코드)
const uiToApiLower = { 잡담: 'chat', 나눔: 'share', 취미: 'hobby', 정보: 'info' }

// 폼 상태
const form = ref({ category: '', title: '', content: '' })
const imageFile = ref(null)
const previewUrl = ref('')
const submitting = ref(false)
const errors = ref({ category: '', title: '', content: '', image: '' })

// 안내 모달 상태
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

// URL 쿼리로 넘어온 category 프리셋 적용
onMounted(() => {
  const map = { all: '잡담', popular: '잡담', chat: '잡담', share: '나눔', hobby: '취미', info: '정보' }
  const q = typeof route.query.category === 'string' ? route.query.category : ''
  if (q && map[q]) form.value.category = map[q]
})

// presign 도우미(선택적으로 사용)
const resolvePresignEndpoint = (raw) => {
  const base = (api?.defaults?.baseURL || '').replace(/\/$/, '')
  if (!raw) return `${base}/v1/uploads/presign`
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/')) return `${base}${raw}`
  return `${base}/${raw.replace(/^\//, '')}`
}
const uploadToS3 = async (presign, file) => {
  if (presign?.fields && presign?.uploadUrl) {
    const fd = new FormData()
    Object.entries(presign.fields).forEach(([k, v]) => fd.append(k, String(v)))
    fd.append('file', file)
    const res = await fetch(presign.uploadUrl, { method: 'POST', body: fd })
    if (!res.ok) throw new Error(`S3 POST 업로드 실패: ${res.status}`)
    return presign.fileUrl || (presign.uploadUrl.replace(/\/$/, '') + '/' + presign.fields.key)
  }
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
const uploadImageIfNeeded = async () => {
  if (!imageFile.value) return null
  const raw = import.meta.env.VITE_IMAGE_PRESIGN_URL || import.meta.env.VITE_IMAGE_PRESIGN_PATH
  const presignEndpoint = resolvePresignEndpoint(String(raw || ''))
  const filename = `${crypto.randomUUID?.() || Date.now()}_${imageFile.value.name}`
  const { data: presign } = await api.post(
    presignEndpoint,
    { filename, contentType: imageFile.value.type }
  )
  const finalUrl = await uploadToS3(presign, imageFile.value)
  return finalUrl
}

// 업로드 입력 처리
const onFileChange = (e) => {
  const file = e.target.files?.[0]
  if (!file) { 
    clearImage()
    return 
  }
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
  const fileInput = document.getElementById('image')
  if (fileInput) fileInput.value = ''
}

const validate = () => {
  errors.value = { category: '', title: '', content: '', image: '' }
  let ok = true
  if (!form.value.category) { errors.value.category = '카테고리를 선택해 주세요.'; ok = false }
  if (!form.value.title || form.value.title.length < 2) { errors.value.title = '제목을 2자 이상 입력해 주세요.'; ok = false }
  if (!form.value.content || form.value.content.length < 2) { errors.value.content = '내용을 2자 이상 입력해 주세요.'; ok = false }
  return ok
}

// FormData 방식 등록
const submitPost = async () => {
  if (submitting.value) return
  if (!validate()) return

  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('title', form.value.title)
    formData.append('content', form.value.content)
    formData.append('boardCategory', uiToApiLower[form.value.category] || 'chat') // 필요시 .toUpperCase()

    if (imageFile.value) {
      formData.append('imageFile', imageFile.value)
      // presign을 쓰려면 대신:
      // const url = await uploadImageIfNeeded()
      // formData.delete('imageFile')
      // formData.append('boardImageUrl', url)
    }

    const response = await api.post('/api/v1/boards', formData)
    console.log('등록 성공:', response.data)

    // ✅ 성공 안내 모달 → 확인 누르면 목록으로 이동
    const categoryParam = uiToApiLower[form.value.category] || 'chat'
    showNotice('글이 등록되었습니다.', '완료', () => {
      router.push({ name: 'boards', query: { category: categoryParam } })
    })
  } catch (err) {
    console.error('등록 실패:', err)
    if (err?.response?.status === 401) {
      alert('세션이 만료되었어요. 다시 로그인해 주세요.')
      router.push({ name: 'onboarding' })
    } else if (err?.response?.status === 400) {
      alert('입력한 정보를 다시 확인해 주세요.')
    } else if (err?.response?.status === 413) {
      alert('파일 크기가 너무 큽니다. 5MB 이하의 이미지를 선택해 주세요.')
    } else {
      alert('등록에 실패했습니다. 잠시 후 다시 시도해 주세요.')
    }
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  router.back()
}

onBeforeUnmount(() => {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
})
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

.write-container {
  max-width: 700px;
  margin: 40px auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 12px;
  background-color: #fff;

  /* ▼ 폰트 적용 */
  font-family: 'KoddiUDOnGothic', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
               'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased; -moz-osx-font-smoothing: grayscale;

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

.submit-button:focus-visible,
.cancel-button:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible {
  outline: 3px solid #ffbf47;
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .submit-button:hover, .cancel-button:hover { transform: none; }
}

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
