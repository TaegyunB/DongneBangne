<template>
  <div class="write-container">
    <h2 class="page-title">게시글 작성</h2>

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

    <button class="submit-button" @click="submitPost">등록하기</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
// import axios from 'axios' // 실제 API 연동 시 주석 해제

const router = useRouter()

const categories = ['잡담', '나눔', '취미', '정보', '인기'] // 인기 -> 인기는 좋아요 10 이상이면 조회되도록 필터링
const form = ref({
  category: '',
  title: '',
  content: '',
})

const submitPost = async () => {
  if (!form.value.category || !form.value.title || !form.value.content) {
    alert('모든 항목을 입력해주세요.')
    return
  }

  // API 요청 (현재는 더미 처리)
  /*
  try {
    await axios.post('/api/v1/boards', form.value)
    alert('글이 등록되었습니다.')
    router.push('/boards')
  } catch (err) {
    console.error(err)
    alert('등록 실패')
  }
  */

  // 더미 처리용
  alert('글이 등록되었습니다.')
  router.push('/boards')
}

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
  background-color: #284cea;
  color: white;
  font-size: 15px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>