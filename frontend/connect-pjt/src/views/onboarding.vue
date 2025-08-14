<template>
  <div class="onboarding-root">
    <Swiper
      class="onboarding-swiper"
      :modules="modules"
      :autoplay="{ delay: 4000, disableOnInteraction: false, pauseOnMouseEnter: true }"
      :slides-per-view="1"
      :space-between="0"
      :pagination="{ clickable: true }"
      :navigation="true"
      :mousewheel="true"
      :keyboard="{ enabled: true }"
      :loop="true"
      :a11y="{ enabled: true }"
      @swiper="onSwiper"
      @slideChange="onSlideChange"
    >
      <SwiperSlide v-for="(s, i) in sections" :key="i">
        <!-- 정확히 50/50 분할 -->
        <section class="split-panels" :class="[s.kind]">
          <!-- LEFT: TEXT ONLY -->
          <div class="panel panel-text">
            <div class="panel-inner text-inner">
              <h1 class="title" v-html="s.title" />
              <p class="subtitle" v-html="s.subtitle" />
            </div>
          </div>

          <!-- RIGHT: IMAGE ONLY (배경효과 포함) -->
          <div class="panel panel-visual">
            <div class="panel-inner visual-inner">
              <img :src="s.image" :alt="s.alt" class="illust" />
            </div>
          </div>
        </section>
      </SwiperSlide>
    </Swiper>

    <!-- 고정 CTA (항상 맨 위 레이어) -->
    <div class="login-cta">
      <img
        src="@/assets/onboarding/kakao_login_button.png"
        alt="카카오 로그인"
        class="kakao-login-img"
        @click="handleKakaoLogin"
      />
      <p class="login-note" v-if="showLoginNote">
        *처음 이용하신다면 <span class="highlight">로그인 후 경로당을 등록</span>하시면 가입이 완료됩니다.<br />
        (“로그인 후 등록된 경로당 선택”으로 이동됩니다.)
      </p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'

import { Swiper, SwiperSlide } from 'swiper/vue'
import { Autoplay, Pagination, Navigation, Mousewheel, Keyboard, A11y } from 'swiper/modules'
import 'swiper/css'
import 'swiper/css/pagination'
import 'swiper/css/navigation'

import onboarding1 from '@/assets/onboarding/onboarding1.png'
import onboarding2 from '@/assets/onboarding/onboarding2.png'
import onboarding3 from '@/assets/onboarding/onboarding3.png'
import onboarding4 from '@/assets/onboarding/onboarding4.png'
import onboarding5 from '@/assets/onboarding/onboarding5.png'
import onboarding6 from '@/assets/onboarding/onboarding6.png'
import onboarding7 from '@/assets/onboarding/onboarding7.png'

const modules = [Autoplay, Pagination, Navigation, Mousewheel, Keyboard, A11y]
const route = useRoute()
const router = useRouter()
const showLoginNote = ref(true)

function resolveABBucket () {
  const hasVisited = localStorage.getItem('onboarding_visited')
  if (hasVisited) {
    localStorage.setItem('onboarding_ab', 'note_off')
    showLoginNote.value = false
  } else {
    localStorage.setItem('onboarding_ab', 'note_on')
    localStorage.setItem('onboarding_visited', 'true')
    showLoginNote.value = true
  }
}

const swiperRef = ref(null)
function onSwiper (s) { swiperRef.value = s }
function onSlideChange () {}

onMounted(async () => {
  const code =
    (typeof route.query.code === 'string' && route.query.code) ||
    new URLSearchParams(window.location.search).get('code')

  if (code) {
    try {
      const res = await api.get('/api/v1/users/senior-center')
      if (res.data?.hasCenter) router.replace('/mainpage')
      else router.replace('/senior-center')
    } catch (e) {
      console.error(e)
      router.replace('/login')
    }
    return
  }
  resolveABBucket()
})

/* 모든 슬라이드를 50/50 고정. hero(1,7)는 타이포만 살짝 키움 */
const sections = [
  {
    kind: 'hero',
    image: onboarding1,
    alt: '인트로',
    title: '“<span class="bold">동네방네,</span><br /><span class="highlight">경로당을 연결하다</span>”',
    subtitle: '“전국 경로당이 하나로 연결되어<br />함께 웃고 소통하는 세상을 만듭니다.”',
  },
  { image: onboarding2, alt: '같이 더 즐거워요', title: '“같이 하면 더 즐거워요!”', subtitle: '소소한 일상도 함께 하면 웃음이 배가 됩니다.', },
  { image: onboarding3, alt: '도전', title: '“매일 새로운 도전이 기다려요”', subtitle: '게임과 미션으로 경로당 생활을 더 활기차게!', },
  { image: onboarding4, alt: 'AI 신문', title: '“AI 신문으로 소식을 전해요”', subtitle: '우리 동네 이야기부터 전국 소식까지 한눈에!', },
  { image: onboarding5, alt: '실시간 소통', title: '“실시간 소통으로 연결돼요”', subtitle: '화상으로 함께 노래하고 이야기 나눠요.', },
  { image: onboarding6, alt: '랭킹', title: '“랭킹으로 재미를 더해요”', subtitle: '경로당 활동 순위를 확인하고 성취감을 느껴보세요.', },
  {
    kind: 'hero',
    image: onboarding7,
    alt: '마무리',
    title: '“<span class="bold">동네방네,</span> <span class="highlight">새로운 하루</span>가 시작돼요”',
    subtitle: '“혼자가 아닌 함께라서 더 즐거운 일상.”',
    reverse: false
  }
]

const handleKakaoLogin = () => {
  window.location.href = `${import.meta.env.VITE_API_BASE_URL}/login/oauth2/authorization/kakao`
}
</script>

<style scoped>
*{margin:0;padding:0;box-sizing:border-box}
.onboarding-root{ --accent:#f5b301 }

/* 전체 배경(밝은 베이지) */
.onboarding-swiper{
  width:100vw; height:100vh;
  background: linear-gradient(180deg,#FAF4E7 0%,#EFE4D3 100%);
  position: relative;
  z-index: 1; /* 고정 UI보다 낮게 */
}

/* ===== 50/50 정확히 반반 ===== */
.split-panels{
  display:grid;
  grid-template-columns: 50% 50%;
  width:100%; height:100%;
  position: relative;
}

/* 좌우 뒤집기 옵션 */
.split-panels.reverse{ grid-auto-flow:column; direction:rtl }
.split-panels.reverse .panel{ direction:ltr }

/* 중앙 가이드 라인(아주 옅게) */
.split-panels::before{
  content:"";
  position:absolute; left:50%; top:0; bottom:0; width:1px;
  transform:translateX(-0.5px);
  background: rgba(0,0,0,.06);
}

/* 각 패널 공통 */
.panel{
  position:relative; overflow:hidden;
  display:flex; align-items:center; justify-content:center;
  padding: clamp(24px, 4vw, 64px);
  width:100%; height:100%;
  z-index: 1; /* 고정 UI보다 낮음 */
}
.panel-text{ background:#ffffff80 } /* 살짝 투명하게 */
.panel-visual{
  /* clay 톤 그라디언트 */
  background: linear-gradient(180deg,#E7D5B9 0%, #D8C3A3 100%);
}

/* 비는 영역을 자연스럽게 메우는 보조 효과 */
.panel-visual::before{
  content:"";
  position:absolute; inset:-15% -10% -10% -15%;
  background:
    radial-gradient(60% 60% at 80% 20%, rgba(255,255,255,.35) 0%, rgba(255,255,255,0) 60%),
    radial-gradient(50% 50% at 10% 85%, rgba(255,255,255,.22) 0%, rgba(255,255,255,0) 60%);
  pointer-events:none;
  z-index:0;
}
.panel-visual::after{
  content:"";
  position:absolute; inset:0;
  background: linear-gradient(90deg, rgba(0,0,0,.03), transparent 25% 75%, rgba(0,0,0,.03));
  pointer-events:none;
  z-index:0;
}

/* 텍스트 영역 */
.text-inner{
  width: min(640px, 80%);
  margin-inline:auto;
  display:flex; flex-direction:column; justify-content:center;
}
.title{
  font-weight:800; letter-spacing:-.02em; line-height:1.2;
  font-size: clamp(32px, 5vw, 64px);
  color:#2d2d2d;
}
.subtitle{
  margin-top:14px; color:#444; line-height:1.7;
  font-size: clamp(18px, 2.1vw, 22px);
}
.hero .title{ font-size: clamp(36px, 6vw, 72px) }
.hero .subtitle{ font-size: clamp(18px, 2.3vw, 24px) }
.highlight{ color: var(--accent); font-weight:700 }
.bold{ font-weight:800 }

/* 이미지 영역 */
.visual-inner{
  width:100%; height:100%;
  display:flex; align-items:center; justify-content:center;
  z-index:1;
}
.illust{
  max-width: 95%;
  max-height: 90vh;             /* 패널 최대한 채우기 */
  height:auto; object-fit:contain;
  filter: drop-shadow(0 10px 20px rgba(0,0,0,.12));
  image-rendering:auto;
}

/* 모바일: 위/아래 스택(텍스트 → 이미지) */
@media (max-width: 900px){
  .split-panels{ grid-template-columns:1fr; grid-template-rows: 1fr 1fr }
  .split-panels::before{ display:none }
  .panel{ padding: 20px }
  .text-inner{ width:min(560px,92%); text-align:center }
}

/* 고정 CTA (항상 맨 위) */
.login-cta{
  position:fixed; left:50%; bottom:max(20px, env(safe-area-inset-bottom));
  transform:translateX(-50%); text-align:center;
  z-index: 50; /* 스와이퍼/이미지보다 높게 */
}
.kakao-login-img{ width:240px; cursor:pointer; transition:opacity .2s; margin-bottom:12px }
.kakao-login-img:hover{ opacity:.85 }
.login-note{ font-size:14px; color:#888; line-height:1.8; max-width:460px; margin:0 auto }

/* Swiper UI */
:deep(.swiper-button-next), :deep(.swiper-button-prev){ color:#222; z-index: 40 }
:deep(.swiper-pagination-bullet){ width:12px; height:12px; opacity:.5; background:#bbb }
:deep(.swiper-pagination-bullet-active){ opacity:1; background:var(--accent) }
:deep(.swiper-pagination){ top:20px; bottom:auto; left:50%; transform:translateX(-50%); z-index:40 }
</style>
