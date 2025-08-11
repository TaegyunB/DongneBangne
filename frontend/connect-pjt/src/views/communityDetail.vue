<template>
  <div class="detail-container">
    <div class="header">
      <h2 class="title">{{ post.title }}</h2>
      <div class="meta">
        <span>작성자: {{ post.author }}</span>
        <span>{{ post.date }}</span>
      </div>
    </div>

    <div class="content">
      {{ post.content }}
    </div>

    <div class="footer">
      <div class="likes">👍 따봉 {{ post.likes }}</div>

      <!-- (옵션) 로그인 유저 == 작성자일 때만 표시 -->
      <div class="actions">
        <RouterLink :to="`/boards/${post.id}/edit`" class="edit-button">수정</RouterLink>
        <button class="delete-button" @click="handleDelete">삭제</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 더미 데이터 (나중에 API 연동)
const post = ref({
  id: route.params.id,
  title: '경로당 프로그램 후기',
  content: '오늘 요가 프로그램이 정말 좋았어요! 다음에도 꼭 참석하고 싶네요.',
  author: '홍길동',
  date: '2025-08-05',
  likes: 15,
})

const handleDelete = () => {
  if (confirm('정말 삭제하시겠습니까?')) {
    // TODO: 삭제 API 호출 예정
    alert('삭제되었습니다')
    router.push('/boards')
  }
}
</script>

<style scoped>
.detail-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;
  font-family: 'Noto Sans KR', sans-serif;
  border: 1px solid #ddd;
  border-radius: 10px;
  background-color: #fff;
}

.header {
  margin-bottom: 20px;
}

.title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.meta {
  font-size: 13px;
  color: #777;
  display: flex;
  gap: 10px;
}

.content {
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 24px;
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.likes {
  font-size: 14px;
  color: #284cea;
}

.actions {
  display: flex;
  gap: 10px;
}

.edit-button,
.delete-button {
  font-size: 13px;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.edit-button {
  background-color: #10b981;
  color: white;
  text-decoration: none;
}

.delete-button {
  background-color: #ef4444;
  color: white;
}
</style>