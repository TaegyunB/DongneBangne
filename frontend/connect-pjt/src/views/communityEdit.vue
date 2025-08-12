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

      <!-- (선택) 이미지 변경은 나중에: 미리보기만 -->
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

const route = useRoute()
const router = useRouter()
const boardId = computed(() => route.params.boardId)

const categories = ['잡담','나눔','정보','취미']
const uiToApiLower = { 잡담:'chat', 나눔:'share', 정보:'info', 취미:'hobby' }
const apiToUi = { CHAT:'잡담', SHARE:'나눔', INFO:'정보', HOBBY:'취미' }

const loading = ref(true)
const submitting = ref(false)

const form = ref({
  category: '잡담',   // UI 라벨
  title: '',
  content: '',
  boardImage: null,  // 현재 이미지 URL (표시용)
})

const fetchDetail = async () => {
  loading.value = true
  try {
    const { data } = await api.get(`/api/v1/boards/${boardId.value}`)
    form.value = {
      category: apiToUi[String(data?.category || '').toUpperCase()] || '잡담',
      title: data?.title || '',
      content: data?.content || '',
      boardImage: data?.boardImage || null,
    }
  } catch (e) {
    console.error('상세 불러오기 실패:', e)
    alert('게시글을 불러오지 못했습니다.')
    goBack()
  } finally {
    loading.value = false
  }
}

const submitEdit = async () => {
  if (submitting.value) return
  if (!form.value.title || !form.value.content) {
    alert('제목과 내용을 입력해 주세요.')
    return
  }

  submitting.value = true
  try {
    // 이미지 변경은 보류: 기존 이미지를 그대로 넘기거나(null 허용이면 생략)
    const body = {
      title: form.value.title,
      content: form.value.content,
      boardCategory: uiToApiLower[form.value.category] || 'chat',
      imageFile: form.value.boardImage || null, // 변경 기능 합칠 때 교체
    }
    await api.put(`/api/v1/boards/${boardId.value}`, body)

    alert('수정이 완료되었습니다.')
    router.push({ name:'communityDetail', params:{ boardId: boardId.value }, query: route.query })
  } catch (e) {
    console.error('수정 실패:', e)
    alert('수정에 실패했습니다.')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push({ name:'communityDetail', params:{ boardId: boardId.value }, query: route.query })
}

onMounted(fetchDetail)
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
