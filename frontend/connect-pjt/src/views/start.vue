<template>
  <div class="welcome-page">
    <div class="welcome-container">
      <transition name="fade">
        <h1 v-if="showText" class="welcome-text">🌸 환영합니다! 🌸</h1>
      </transition>
      <p class="subtext">잠시 후 로그인 페이지로 이동합니다...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUiStore } from '@/stores/useUiStore'
import api from '@/api/axios'

const router = useRouter()
const showText = ref(false)
const ui = useUiStore()

onMounted(async () => {
  document.body.classList.add('hide-toolbar')
  ui.showMenu = false
  ui.showProfile = false
  ui.showLogo = false

  setTimeout(() => {
    showText.value = true
  }, 300)

  // ✅ 로그인 된 상태면 바로 분기!
  try {
    const res = await api.get('/api/v1/users/senior-center')
    if (res.data?.hasCenter) {
      router.push('/mainpage')
      return
    } else {
      router.push('/senior-center')
      return
    }
  } catch (err) {
    // 인증 안 된 경우만 애니메이션 후 로그인 페이지로 이동
    setTimeout(() => {
      document.body.classList.remove('hide-toolbar')
      ui.showMenu = true
      ui.showProfile = true
      ui.showLogo = true
      router.push('/login')
    }, 2500)
  }
})
</script>

<style scoped>
.welcome-page {
  width: 100%;
  height: 100vh;
  background: linear-gradient(to bottom right, #fffaf0, #ffe4b5);
  font-family: 'NanumSquare', sans-serif;
  overflow: hidden;
  position: relative;
}

.welcome-container {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1000;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.welcome-text {
  font-size: 48px;
  font-weight: bold;
  color: #333;
  text-align: center;
}

.subtext {
  margin-top: 20px;
  font-size: 20px;
  color: #777;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.8s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>

<style>
/* 💡 글로벌 스타일: body에 클래스가 있으면 toolbar를 숨긴다 */
body.hide-toolbar .toolbar {
  transform: translateY(-200%);
  transition: transform 0.4s ease;
}
</style>