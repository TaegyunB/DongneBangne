<template>
  <div class="edit-container">
    <h2 class="page-title">게시글 수정</h2>

    <div class="form-group">
      <label>카테고리</label>
      <select v-model="form.category">
        <option disabled value="">카테고리를 선택하세요</option>
        <option v-for="category in categories" :key="category" :value="category">
          {{ category }}
        </option>
      </select>
    </div>

    <div class="form-group">
      <label>제목</label>
      <input type="text" v-model="form.title" placeholder="제목을 입력하세요" />
    </div>

    <div class="form-group">
      <label>내용</label>
      <textarea v-model="form.content" placeholder="내용을 입력하세요" />
    </div>

    <button class="submit-button" @click="updatePost">수정 완료</button>
  </div>
</template>


<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// import axios from 'axios' // 실제 연동 시 주석 해제

const route = useRoute()
const router = useRouter()

const categories = ['잡담', '나눔', '취미', '정보', '인기']

const form = ref({
  category: '',
  title: '',
  content: '',
})

// 1. 게시글 데이터 불러오기
onMounted(async () => {
  const id = route.params.id

  // 실제 요청 예시
  /*
  try {
    const res = await axios.get(`/api/v1/boards/${id}`)
    form.value = {
      category: res.data.category,
      title: res.data.title,
      content: res.data.content,
    }
  } catch (err) {
    console.error(err)
    alert('글을 불러오는 데 실패했습니다.')
    router.push('/boards')
  }
  */

  // 더미 데이터 로드
  form.value = {
    category: '정보',
    title: '게시글 수정 테스트',
    content: '기존 내용을 수정합니다.',
  }
})

// 2. 수정 요청
const updatePost = async () => {
  const id = route.params.id

  if (!form.value.category || !form.value.title || !form.value.content) {
    alert('모든 항목을 입력해주세요.')
    return
  }

  // 실제 요청 예시
  /*
  try {
    await axios.put(`/api/v1/boards/${id}`, form.value)
    alert('수정 완료되었습니다.')
    router.push(`/boards/${id}`)
  } catch (err) {
    console.error(err)
    alert('수정에 실패했습니다.')
  }
  */

  // 더미 처리
  alert('수정 완료되었습니다.')
  router.push(`/boards/${id}`)
}
</script>

<style scoped>
.edit-container {
  max-width: 700px;
  margin: 40px auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 12px;
  background-color: #fff;
  font-family: 'Noto Sans KR', sans-serif;
}

.page-title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 6px;
  font-weight: bold;
}

input,
select,
textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}

textarea {
  min-height: 180px;
  resize: vertical;
}

.submit-button {
  background-color: #10b981;
  color: white;
  font-size: 15px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>