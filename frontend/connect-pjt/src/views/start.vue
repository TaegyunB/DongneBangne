<template>
  <div class="welcome-page">
    <div class="welcome-container">
      <transition name="fade">
        <h1 v-if="showText" class="welcome-text">동네방네에 오신 것을 환영합니다.</h1>
      </transition>
      <p class="subtext">즐겁게 이용해 주세요.</p>

      <p class="guide-text">잠시 후 로그인 페이지로 이동합니다...</p>

      <!-- 로딩 스피너 -->
      <div class="loading-wrap" role="status" aria-live="polite" aria-label="로딩 중">
        <div class="spinner" aria-hidden="true"></div>
        <span class="sr-only">로딩 중...</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()
const showText = ref(false)

onMounted(async () => {
  // 카카오가 루트('/')로 code를 보낸 경우 → /login으로 code를 전달하며 이동
  const urlCode = new URLSearchParams(window.location.search).get('code')
  if (urlCode) {
    router.replace({ path: '/login', query: { code: urlCode } })
    return
  }

  document.body.classList.add('hide-toolbar')

  setTimeout(() => {
    showText.value = true
  }, 300)

  try {
    const res = await api.get('/api/v1/users/senior-center')
    document.body.classList.remove('hide-toolbar')

    if (res.data?.hasCenter) {
      router.push('/mainpage')
    } else {
      router.push('/senior-center')
    }
  } catch (err) {
    setTimeout(() => {
      document.body.classList.remove('hide-toolbar')
      router.push('/login')
    }, 2500)
  }
})
</script>

<style scoped>
/* ===== 온보딩 페이지와 톤/팔레트 일치 ===== */
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

.welcome-page{
  --accent:#f5b301;
  --bg-top:#F7F1E6;
  --bg-bottom:#EBE2D2;
  --bg-spot:rgba(255,255,255,.55);
  --panel-bg:rgba(255,255,255,.78);
  --text-strong:#222;
  --text-muted:#555;

  width:100%;
  height:100vh;
  display:flex; align-items:center; justify-content:center;
  background:
    radial-gradient(1200px 720px at 14% 10%, var(--bg-spot) 0%, transparent 62%),
    radial-gradient(980px 620px at 88% 86%, var(--bg-spot) 0%, transparent 68%),
    linear-gradient(180deg, var(--bg-top) 0%, var(--bg-bottom) 100%);
  font-family:'KoddiUDOnGothic','Apple SD Gothic Neo','Noto Sans KR',system-ui,sans-serif;
  overflow:hidden;
}

.welcome-container{
  background: var(--panel-bg);
  backdrop-filter: saturate(110%) blur(1.2px);
  border: 1px solid rgba(0,0,0,.06);
  border-radius: 18px;
  padding: clamp(24px, 6vw, 56px);
  width: min(760px, 92vw);
  text-align:center;
  box-shadow: 0 10px 30px rgba(0,0,0,.08);
}

.welcome-text{
  font-size: clamp(26px, 5.2vw, 42px);
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--text-strong);
  line-height: 1.25;
  margin: 0;
}

.subtext{
  margin-top: 10px;
  font-size: clamp(18px, 2.6vw, 22px);
  color: var(--text-strong);
  line-height: 1.8;
  font-weight: 700;
}

.guide-text{
  margin-top: 12px;
  font-size: clamp(16px, 2.2vw, 18px);
  color: var(--text-muted);
  line-height: 1.8;
}

/* 로딩 스피너 */
.loading-wrap{
  margin-top: 18px;
  display:flex; align-items:center; justify-content:center; gap: 10px;
}
.spinner{
  width: 44px; height: 44px;
  border-radius: 50%;
  border: 4px solid #e5e7eb;
  border-top-color: var(--accent);
  animation: spin 0.9s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg) } }

/* 스크린리더 전용 텍스트 */
.sr-only{
  position:absolute !important; width:1px; height:1px;
  padding:0; margin:-1px; overflow:hidden; clip:rect(0,0,0,0); border:0;
}

/* 페이드 효과 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.8s ease }
.fade-enter-from, .fade-leave-to { opacity: 0 }

@media (max-width: 480px){
  .welcome-container { padding: 22px }
}
</style>

<style>
/* 글로벌: toolbar 숨김(기존 로직 유지) */
body.hide-toolbar .toolbar {
  transform: translateY(-200%);
  transition: transform 0.4s ease;
}
</style>
