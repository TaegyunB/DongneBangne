<template>
  <div class="start-container">
    <!-- motion-div + img 패턴 -->
    <motion-div
      :initial="{ opacity: 0, scale: 0.7 }"
      :enter="{ opacity: 1, scale: 1 }"
      transition="ease"
      :delay="0.2"
      class="logo-img"
    >
      <img :src="logo" alt="동네방네 로고" style="width: 100%;" />
    </motion-div>

    <!-- motion-h2 텍스트 애니메이션 -->
    <motion-h2
      :initial="{ opacity: 0, y: 24 }"
      :enter="{ opacity: 1, y: 0 }"
      transition="ease"
      :delay="0.8"
      class="welcome-msg"
    >
      동네방네에 오신 것을 환영합니다!
    </motion-h2>
  </div>
</template>

<script setup>
import { useMotion } from '@vueuse/motion'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import logo from '@/assets/logo.png'
import { useUiStore } from '@/stores/useUiStore'

useMotion()

const router = useRouter()
const ui = useUiStore()

onMounted(() => {
  // 툴바 항목 강제 비활성화
  ui.showMenu = false
  ui.showProfile = false
  ui.welcomeText = ''

  setTimeout(() => {
    router.push('/login')
  }, 2500)
})
</script>

<style scoped>
.start-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #fffaf0;
}

.logo-img {
  width: 180px;
  margin-bottom: 32px;
}

.welcome-msg {
  font-size: 2rem;
  font-weight: 700;
  color: #31414b;
  letter-spacing: 0.01em;
}
</style>
