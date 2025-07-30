<template>
  <div class="welcome-container">
    <h1 class="welcome-text" :class="{ show: showText }">환영합니다!</h1>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const showText = ref(false)

onMounted(() => {
  // 애니메이션 효과를 위해 약간의 지연 후 show
  setTimeout(() => {
    showText.value = true
  }, 300)

  // 일정 시간 후 자동으로 /login 페이지로 이동
  setTimeout(() => {
    router.push('/login')
  }, 2500)
})
</script>

<style scoped>
.welcome-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #fffaf0;
}

.welcome-text {
  font-size: 48px;
  font-weight: bold;
  opacity: 0;
  transform: scale(0.8);
  transition: all 1s ease;
  color: #333;
}

.welcome-text.show {
  opacity: 1;
  transform: scale(1);
}
</style>