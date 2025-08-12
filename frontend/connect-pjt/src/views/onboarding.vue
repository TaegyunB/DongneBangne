<template>
  <div class="onboarding-root">
    <Swiper
      class="onboarding-swiper"
      :modules="modules"
      :slides-per-view="1"
      :space-between="0"
      :pagination="{ clickable: true }"
      :navigation="true"
      :mousewheel="true"
      :keyboard="{ enabled: true }"
      :loop="false"
      :a11y="{ enabled: true }"
    >
      <SwiperSlide v-for="(section, index) in sections" :key="index">
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
        </div>

        <!-- 섹션 2~6 -->
        <div v-else-if="section.type === 'onboarding'" class="onboarding-content">
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
        <div v-else-if="section.type === 'final'" class="onboarding-content only-image">
          <img src="@/assets/onboarding/onboarding7.png" alt="온보딩 마지막" class="final-image" />
        </div>
      </SwiperSlide>
    </Swiper>

    <!-- 모든 섹션에서 항상 보이는 전역 로그인 CTA -->
    <div class="login-cta">
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
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'

// Swiper
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Pagination, Navigation, Mousewheel, Keyboard, A11y } from 'swiper/modules'
import 'swiper/css'
import 'swiper/css/pagination'
import 'swiper/css/navigation'

import onboarding2 from '@/assets/onboarding/onboarding2.png'
import onboarding3 from '@/assets/onboarding/onboarding3.png'
import onboarding4 from '@/assets/onboarding/onboarding4.png'
import onboarding5 from '@/assets/onboarding/onboarding5.png'
import onboarding6 from '@/assets/onboarding/onboarding6.png'

const modules = [Pagination, Navigation, Mousewheel, Keyboard, A11y]
const route = useRoute()
const router = useRouter()

onMounted(async () => {
  // /login에 도착했을 때 code가 쿼리나 URL에 있으면 처리
  const code =
    (typeof route.query.code === 'string' && route.query.code) ||
    new URLSearchParams(window.location.search).get('code')

  if (code) {
    try {
      // 백엔드가 이미 세션/쿠키/헤더 세팅했다고 가정하고 유저 상태 체크
      const res = await api.get('/api/v1/users/senior-center')
      if (res.data?.hasCenter) {
        router.replace('/mainpage')
      } else {
        router.replace('/senior-center')
      }
    } catch (err) {
      console.error('로그인 후 사용자 정보 확인 실패:', err)
      router.replace('/login')
    }
    return
  }
  // code 없으면 온보딩에서 로그인 버튼 노출만
})

const onboardingSections = [
  {
    image: onboarding2,
    title: '“<span class="highlight">같이</span> 하면 더 즐거워요!”',
    subtitle: '소소한 일상도 함께 하면<br />웃음이 배가 됩니다.'
  },
  {
    image: onboarding3,
    title: '“매일 <span class="highlight">새로운 도전</span>이 기다려요”',
    subtitle: '게임과 미션으로<br />경로당 생활을 더 활기차게!'
  },
  {
    image: onboarding4,
    title: '“<span class="highlight">AI 신문</span>으로 소식을 전해요”',
    subtitle: '우리 동네 이야기부터<br />전국 소식까지 한눈에!'
  },
  {
    image: onboarding5,
    title: '“<span class="highlight">실시간</span> 소통으로 연결돼요”',
    subtitle: '화상으로 함께 노래하고<br />이야기 나눠요.'
  },
  {
    image: onboarding6,
    title: '“<span class="highlight">랭킹</span>으로 재미를 더해요”',
    subtitle: '경로당 활동 순위도 확인하고<br />성취감을 느껴보세요.'
  }
].map((item, idx) => ({
  ...item,
  type: 'onboarding',
  index: idx
}))

const sections = [
  { type: 'intro' },
  ...onboardingSections,
  { type: 'final' }
]

const handleKakaoLogin = () => {
  window.location.href = `${import.meta.env.VITE_API_BASE_URL}/login/oauth2/authorization/kakao`
}
</script>

<style scoped>
* { margin: 0; padding: 0; box-sizing: border-box; }

/* 루트 컨테이너 */
.onboarding-root {
  position: relative;
}

/* Swiper 전체를 한 화면으로 */
.onboarding-swiper {
  width: 100vw;
  height: 100vh;
}

/* 원래 레이아웃 유지 */
.onboarding-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
  max-width: 1200px;
  width: 100%;
  height: 100%;
  margin: 0 auto;
  position: relative;
  justify-content: center;
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

/* Swiper 네비/도트 살짝 커스텀 */
:deep(.swiper-button-next),
:deep(.swiper-button-prev) {
  color: #222;
}
:deep(.swiper-pagination-bullet) {
  opacity: 0.5;
  background: #bbb;
}
:deep(.swiper-pagination-bullet-active) {
  opacity: 1;
  background: #12795a;
}

.login-cta {
  position: fixed;
  left: 50%;
  bottom: 60px;
  transform: translateX(-50%);
  text-align: center;
  z-index: 1001; /* 스와이퍼 화살표(기본 z=10)보다 위 */
  pointer-events: auto;
}

.kakao-login-img {
  width: 240px;
  cursor: pointer;
  transition: opacity 0.2s ease;
  margin-bottom: 12px;
}
.kakao-login-img:hover { opacity: 0.85; }
</style>
