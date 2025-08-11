<template>
  <div class="onboarding-container" ref="wrap">
    <section
      v-for="(section, index) in sections"
      :key="index"
      class="onboarding-wrapper"
    >
      <!-- 섹션 1 -->
      <div
        v-if="section.type === 'intro'"
        class="onboarding-content intro-section"
      >
        <div class="text-box">
          <h1 class="title">“<span class="bold">동네방네,</span> <span class="highlight">경로당을 연결하다</span>”</h1>
          <p class="subtitle">“전국 경로당이 하나로 연결되어<br />함께 웃고 소통하는 세상을 만듭니다.”</p>
        </div>
        <div class="image-box">
          <img src="@/assets/onboarding/onboarding1.png" alt="온보딩1" />
        </div>

        <!-- 로그인 버튼: 이미지 버튼 -->
        <div class="intro-login-box">
          <img
            src="@/assets/onboarding/kakao_login_button.png"
            alt="카카오 로그인"
            class="kakao-login-img"
            @click="handleKakaoLogin"
          />
          <p class="login-note">
            *처음 이용하신다면 <span class="highlight">로그인 후 경로당을 등록</span>하시면 가입이 완료됩니다.<br />
            (“로그인 후 등록된 경로당 선택”으로 이동됩니다.)
          </p>
        </div>
      </div>

      <!-- 섹션 2~6 -->
      <div
        v-else-if="section.type === 'onboarding'"
        class="onboarding-content"
      >
        <template v-if="section.index % 2 === 0">
          <div class="image-box">
            <img :src="section.image" :alt="`온보딩 이미지 ${section.index}`" />
          </div>
          <div class="text-box">
            <h1 class="title" v-html="section.title" />
            <p class="subtitle" v-html="section.subtitle" />
          </div>
        </template>
        <template v-else>
          <div class="text-box">
            <h1 class="title" v-html="section.title" />
            <p class="subtitle" v-html="section.subtitle" />
          </div>
          <div class="image-box">
            <img :src="section.image" :alt="`온보딩 이미지 ${section.index}`" />
          </div>
        </template>
      </div>

      <!-- 섹션 7 -->
      <div
        v-else-if="section.type === 'final'"
        class="onboarding-content only-image"
      >
        <img src="@/assets/onboarding/onboarding7.png" alt="온보딩 마지막" class="final-image" />
      </div>
    </section>

    <!-- 좌/우 이동 버튼 (오른쪽으로 넘기기 / 처음으로) -->
    <button class="scroll-down-btn" @click="handleScroll">
      {{ isLastSection ? '‹' : '›' }}
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUiStore } from '@/stores/useUiStore'
import api from '@/api/axios'

import onboarding2 from '@/assets/onboarding/onboarding2.png'
import onboarding3 from '@/assets/onboarding/onboarding3.png'
import onboarding4 from '@/assets/onboarding/onboarding4.png'
import onboarding5 from '@/assets/onboarding/onboarding5.png'
import onboarding6 from '@/assets/onboarding/onboarding6.png'

const ui = useUiStore()
const wrap = ref(null)
const currentIndex = ref(0)

onMounted(async () => {
  ui.showLogo = false
  ui.showMenu = false
  ui.showProfile = false
  ui.welcomeText = '지금 동네방네를 <span class="start-word">시작</span>해보세요!'

  // 가로 스크롤 위치 → 인덱스 동기화
  wrap.value?.addEventListener('scroll', onScroll)
  // 마우스 휠을 가로 스크롤로 매핑
  wrap.value?.addEventListener('wheel', wheelToHorizontal, { passive: false })

  // SNS 로그인 콜백 처리
  const code = new URLSearchParams(window.location.search).get('code')
  if (code) {
    try {
      const res = await api.get('/api/v1/users/senior-center')
      if (res.data?.hasCenter) window.location.href = '/mainpage'
      else window.location.href = '/senior-center'
    } catch (err) {
      console.error('로그인 후 사용자 정보 확인 실패:', err)
      alert('로그인 후 정보를 불러오는 데 실패했습니다.')
    }
  }
})

onUnmounted(() => {
  ui.showLogo = true
  ui.showMenu = true
  ui.showProfile = true
  ui.welcomeText = ''

  wrap.value?.removeEventListener('scroll', onScroll)
  wrap.value?.removeEventListener('wheel', wheelToHorizontal)
})

const sections = [
  { type: 'intro' },
  ...[
    { image: onboarding2, title: '“<span class="highlight">같이</span> 하면 더 즐거워요!”', subtitle: '소소한 일상도 함께 하면<br />웃음이 배가 됩니다.' },
    { image: onboarding3, title: '“매일 <span class="highlight">새로운 도전</span>이 기다려요”', subtitle: '게임과 미션으로<br />경로당 생활을 더 활기차게!' },
    { image: onboarding4, title: '“<span class="highlight">AI 신문</span>으로 소식을 전해요”', subtitle: '우리 동네 이야기부터<br />전국 소식까지 한눈에!' },
    { image: onboarding5, title: '“<span class="highlight">실시간</span> 소통으로 연결돼요”', subtitle: '화상으로 함께 노래하고<br />이야기 나눠요.' },
    { image: onboarding6, title: '“<span class="highlight">랭킹</span>으로 재미를 더해요”', subtitle: '경로당 활동 순위도 확인하고<br />성취감을 느껴보세요.' },
  ].map((item, idx) => ({ ...item, type: 'onboarding', index: idx })),
  { type: 'final' }
]

const isLastSection = computed(() => currentIndex.value === sections.length - 1)

const handleScroll = () => {
  currentIndex.value = isLastSection.value ? 0 : currentIndex.value + 1
  const x = currentIndex.value * window.innerWidth
  wrap.value?.scrollTo({ left: x, behavior: 'smooth' })
}

const onScroll = () => {
  if (!wrap.value) return
  const i = Math.round((wrap.value.scrollLeft || 0) / window.innerWidth)
  currentIndex.value = Math.min(Math.max(i, 0), sections.length - 1)
}

const wheelToHorizontal = (e) => {
  if (!wrap.value) return
  e.preventDefault()
  wrap.value.scrollLeft += e.deltaY
}

const handleKakaoLogin = () => {
  const url = `${import.meta.env.VITE_API_BASE_URL}/login/oauth2/authorization/kakao`
  window.location.href = url
  console.log('카카오 로그인 URL:', url)
}
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; }

/* === 핵심: 가로 스크롤 + 스냅 === */
.onboarding-container {
  height: 100vh;
  display: flex;                 /* 가로로 섹션 나열 */
  overflow-x: auto;              /* 가로 스크롤 */
  overflow-y: hidden;            /* 세로 스크롤 숨김 */
  scroll-snap-type: x mandatory; /* 스냅 활성화 */
  scroll-behavior: smooth;
}

.onboarding-wrapper {
  flex: 0 0 100vw;               /* 한 슬라이드 = 한 화면 폭 */
  height: 100vh;
  display: flex;
  justify-content: center;
  padding: 0 60px;
  scroll-snap-align: start;      /* 스냅 정렬 */
}

/* 원래 레이아웃 스타일 유지 */
.onboarding-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
  max-width: 1200px;
  width: 100%;
  position: relative;
}
@media (min-width: 1024px) {
  .onboarding-content {
    flex-direction: row;
    justify-content: center;
    align-items: stretch;
    text-align: left;
    gap: 80px;
  }
}

.intro-section .text-box {
  text-align: left;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  flex: 1;
}
.image-box { flex: 1; display: flex; justify-content: center; align-items: center; }

.intro-login-box {
  position: absolute;
  bottom: 60px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
}

.kakao-login-img { width: 240px; cursor: pointer; transition: opacity 0.2s ease; margin-bottom: 12px; }
.kakao-login-img:hover { opacity: 0.85; }

.login-note { font-size: 14px; color: #888; line-height: 1.8; margin: 0; text-align: center; max-width: 460px; margin-inline: auto; }
.login-note .highlight { color: #d97706; font-weight: 600; }

.text-box { max-width: 500px; }
.image-box img { width: 600px; height: auto; object-fit: contain; }

.final-image { width: 500px; height: auto; margin: 0 auto; }
.only-image { flex-direction: column; justify-content: center; align-items: center; }

.title { font-size: 32px; font-weight: bold; }
.subtitle { font-size: 20px; color: #444; line-height: 1.6; }
.highlight { color: #fbbf24; font-weight: bold; }
.start-word { color: #3b82f6; font-weight: bold; font-size: 32px; }

/* 오른쪽/처음으로 버튼 */
.scroll-down-btn {
  position: fixed;
  bottom: 20px;
  left: 20px;
  font-size: 48px;
  background: none;
  border: none;
  cursor: pointer;
  opacity: 0.6;
  transition: 0.3s;
  z-index: 999;
}
.scroll-down-btn:hover { opacity: 1; }

/* (선택) 전체 페이지 세로 스크롤 제거 */
:global(html), :global(body) {
  height: 100%;
  overflow: hidden;
}
</style>
