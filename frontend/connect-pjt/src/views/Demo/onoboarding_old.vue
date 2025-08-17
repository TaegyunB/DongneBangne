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
              <!-- 브랜드(선택) -->
              <div v-if="s.brand" class="brand">
                <span class="brand-name">“동네방네”</span>
              </div>

              <!-- 헤드라인 -->
              <h1 class="headline"
                  :class="{ shout: s.shout }">
                <span v-html="s.headline" />
              </h1>

              <!-- 본문 -->
              <p class="lead" v-html="s.body" />
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

/* 모든 슬라이드를 50/50 고정.
   brand: 좌측 상단 "동네방네" 노출 여부
   shout: 느낌표/강조 헤드라인(ExtraBold + 더 큼)
*/
const sections = [
  // 1. 인트로
  {
    kind: 'hero',
    brand: true,
    image: onboarding1,
    alt: '인트로',
    headline: '경로당을 연결하다',
    body: '전국 경로당이 하나로 연결되어 함께 웃고 소통하는 세상을 만듭니다.'
  },
  // 2. 도전
  {
    image: onboarding2,
    alt: '도전',
    shout: true,
    headline: '매달 새로운 도전을!',
    body: '매달 주어지는 2개의 미션과 경로당에서 설정하는 2개의 미션으로 더욱 활기찬 한 달을 보내요!'
  },
  // 3. 트로트 게임
  {
    image: onboarding3,
    alt: '트로트 게임',
    shout: true,
    headline: '먼저 트로트 제목 맞추기!',
    body: '다른 경로당과 실시간 화상 통화로 소통하면서 게임을 해보아요.'
  },
  // 4. AI 신문
  {
    image: onboarding4,
    alt: 'AI 신문',
    shout: true,
    headline: '한 달의 마무리를 AI신문과 함께!',
    body: '매달 수행한 도전들을 바탕으로 한 달 동안 경로당에서의 활동을 정리해서 보아요.'
  },
  // 5. 랭킹
  {
    image: onboarding5,
    alt: '랭킹',
    shout: true,
    headline: '다른 경로당의 순위와 활동을!',
    body: '경로당 활동 순위를 확인하고 다른 경로당의 활동을 구경하며 동네방네를 즐겨보아요.'
  },
  // 6. 게시판
  {
    image: onboarding6,
    alt: '게시판',
    shout: true,
    headline: '타 경로당 사람들과 소통을!',
    body: '일상을 공유하며 동네방네 사람들과 다양한 이야기를 나누어보아요.'
  },
  // 7. 마무리
  {
    kind: 'hero',
    brand: true,
    image: onboarding7,
    alt: '마무리',
    headline: '새로운 하루의 시작이 됩니다.',
    body: '혼자가 아닌 함께라서 더 즐거운 일상을 통해서 더 나은 내일을 꿈꿔보아요.'
  }
]

const handleKakaoLogin = () => {
  window.location.href = `${import.meta.env.VITE_API_BASE_URL}/login/oauth2/authorization/kakao`
}
</script>

<style scoped>
/* ===== 폰트 등록 ===== */
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
*{margin:0;padding:0;box-sizing:border-box}
.onboarding-root{ --accent:#f5b301; font-family:'KoddiUDOnGothic','Apple SD Gothic Neo','Noto Sans KR',system-ui,sans-serif }

/* 전체 배경(밝은 베이지) */
.onboarding-swiper{
  width:100vw; height:100vh;
  background: linear-gradient(180deg,#FAF4E7 0%,#EFE4D3 100%);
  position: relative; z-index: 1;
}

/* ===== 50/50 정확히 반반 ===== */
.split-panels{
  display:grid; grid-template-columns: 50% 50%;
  width:100%; height:100%; position: relative;
}

/* 중앙 가이드 라인(아주 옅게) */
.split-panels::before{
  content:""; position:absolute; left:50%; top:0; bottom:0; width:1px;
  transform:translateX(-0.5px); background: rgba(0,0,0,.06);
}

/* 각 패널 공통 */
.panel{
  position:relative; overflow:hidden;
  display:flex; align-items:center; justify-content:center;
  padding: clamp(24px, 4vw, 64px);
  width:100%; height:100%; z-index: 1;
}
.panel-text{ background:#ffffff80 }
.panel-visual{
  background: linear-gradient(180deg,#E7D5B9 0%, #D8C3A3 100%);
  overflow: visible;
}

/* 비는 영역을 자연스럽게 메우는 보조 효과 */
.panel-visual::before{
  content:""; position:absolute; inset:-15% -10% -10% -15%;
  background:
    radial-gradient(60% 60% at 80% 20%, rgba(255,255,255,.35) 0%, rgba(255,255,255,0) 60%),
    radial-gradient(50% 50% at 10% 85%, rgba(255,255,255,.22) 0%, rgba(255,255,255,0) 60%);
  pointer-events:none; z-index:0;
}
.panel-visual::after{
  content:""; position:absolute; inset:0;
  background: linear-gradient(90deg, rgba(0,0,0,.03), transparent 25% 75%, rgba(0,0,0,.03));
  pointer-events:none; z-index:0;
}

/* ===== 텍스트 영역(계층) ===== */
.text-inner{
  width:min(58ch, 82%);  /* 줄길이 가드레일: 잘림/삐죽 방지 */
  margin-inline:auto;
  display:flex; flex-direction:column; gap: clamp(10px,1.6vw,16px);
  justify-content:center;
  word-break: keep-all;     /* 한국어 단어 중간 끊김 방지 */
  text-wrap: balance;       /* 지원 브라우저에서 줄 균형 */
}

/* 브랜드(가장 큼) */
.brand .brand-name{
  display:inline-block;
  font-weight:800;          /* ExtraBold */
  font-size: clamp(40px, 7.2vw, 80px);
  letter-spacing:-0.02em;
  line-height:1.08;
  color:#111;
}

/* 헤드라인 */
.headline{
  font-weight:700;          /* 기본 Bold */
  font-size: clamp(28px, 4.6vw, 50px);
  line-height:1.18;
  letter-spacing:-0.01em;
  color:#222;
}
.headline.shout{
  font-weight:800;          /* 강조는 ExtraBold */
  font-size: clamp(32px, 5.2vw, 64px);
}

/* 본문 */
.lead{
  margin-top:2px;
  color:#444;
  line-height:1.7;
  font-weight:400;
  font-size: clamp(16px, 1.9vw, 20px);
}

/* 강조 색상 */
.highlight{ color: var(--accent); font-weight:700 }

/* 이미지 영역 */
.visual-inner{ width:100%; height:100%; display:flex; align-items:center; justify-content:center; z-index:1 }
.illust{
  max-width: none; width: 110%;
  max-height: 94vh; height:auto; object-fit:contain;
  filter: drop-shadow(0 10px 20px rgba(0,0,0,.12));
}

/* 모바일: 위/아래 스택(텍스트 → 이미지) */
@media (max-width: 900px){
  .split-panels{ grid-template-columns:1fr; grid-template-rows: 1fr 1fr }
  .split-panels::before{ display:none }
  .panel{ padding: 20px }
  .text-inner{ width:min(60ch,92%); text-align:center }
  .brand .brand-name{ font-size: clamp(34px, 9vw, 64px) }
  .headline{ font-size: clamp(24px, 6vw, 40px) }
  .headline.shout{ font-size: clamp(26px, 6.8vw, 44px) }
}

/* 고정 CTA (항상 맨 위) */
.login-cta{
  position:fixed; left:50%; bottom:max(20px, env(safe-area-inset-bottom));
  transform:translateX(-50%); text-align:center; z-index: 50;
}
.kakao-login-img{ width:240px; cursor:pointer; transition:opacity .2s; margin-bottom:12px }
.kakao-login-img:hover{ opacity:.85 }
.login-note{ font-size:14px; color:#888; line-height:1.8; max-width:460px; margin:0 auto }

/* Swiper UI */
:deep(.swiper-button-next), :deep(.swiper-button-prev){ color:#222; z-index: 40 }
:deep(.swiper-pagination-bullet){ width:12px; height:12px; opacity:.5; background:#bbb }
:deep(.swiper-pagination-bullet-active){ opacity:1; background:var(--accent) }
:deep(.swiper-pagination){ top:20px; bottom:auto; left:50%; transform:translateX(-50%); z-index:40 }

/* 변수: 톤만 바꾸고 싶을 때 여기 숫자만 조절하세요 */
.onboarding-root{
  --accent:#f5b301;
  /* 화면 전체 배경 */
  --bg-top:#F7F1E6;         /* 조금 더 크리미한 상단 */
  --bg-bottom:#EBE2D2;      /* 부드럽게 내려오는 하단 */
  --bg-spot:rgba(255,255,255,.55); /* 은은한 하이라이트 */

  /* 오른쪽 비주얼 패널 배경 */
  --panel-vis-top:#E7D6BE;  /* 기존보다 노랑끼 살짝 낮춤 */
  --panel-vis-btm:#D8C7AE;

  /* 왼쪽 텍스트 패널 배경(유리 느낌 살짝) */
  --panel-text-bg:rgba(255,255,255,.78);
}

/* ===== 화면 전체 배경 ===== */
.onboarding-swiper{
  width:100vw; height:100vh;
  /* 라디얼 하이라이트 + 그라데이션을 레이어로 쌓음 */
  background:
    radial-gradient(1200px 720px at 14% 10%, var(--bg-spot) 0%, transparent 62%),
    radial-gradient(980px 620px at 88% 86%, var(--bg-spot) 0%, transparent 68%),
    linear-gradient(180deg, var(--bg-top) 0%, var(--bg-bottom) 100%);
  position: relative; z-index: 1;
}

/* ===== 반반 레이아웃 공통 ===== */
.split-panels{
  display:grid; grid-template-columns: 50% 50%;
  width:100%; height:100%; position: relative;
}
/* 중앙 가이드 라인 살짝 더 옅게 */
.split-panels::before{
  content:""; position:absolute; left:50%; top:0; bottom:0; width:1px;
  transform:translateX(-0.5px); background: rgba(0,0,0,.04);
}

/* 패널 공통 */
.panel{
  position:relative; overflow:hidden;
  display:flex; align-items:center; justify-content:center;
  padding: clamp(24px, 4vw, 64px);
  width:100%; height:100%; z-index: 1;
}
/* 왼쪽 텍스트 패널: 살짝 투명 + 아주 미세한 블러로 자연스럽게 */
.panel-text{
  background: var(--panel-text-bg);
  backdrop-filter: saturate(110%) blur(1.2px);
}

/* 오른쪽 비주얼 패널: 클레이 톤을 부드럽게 */
.panel-visual{
  background:
    radial-gradient(58% 60% at 80% 18%, rgba(255,245,220,.28) 0%, rgba(255,245,220,0) 62%),
    linear-gradient(180deg, var(--panel-vis-top) 0%, var(--panel-vis-btm) 100%);
  overflow: visible;
}
/* 기존 보조 하이라이트/비네트 톤을 더 자연스럽게 */
.panel-visual::before{
  content:""; position:absolute; inset:-12% -8% -8% -12%;
  background:
    radial-gradient(60% 60% at 78% 22%, rgba(255,255,255,.28) 0%, rgba(255,255,255,0) 60%),
    radial-gradient(50% 50% at 12% 86%, rgba(255,255,255,.18) 0%, rgba(255,255,255,0) 60%);
  pointer-events:none; z-index:0;
}
.panel-visual::after{
  content:""; position:absolute; inset:0;
  /* 아주 옅은 비네트로 가장자리 결 정리 */
  background: radial-gradient(120% 100% at 50% 50%, transparent 70%, rgba(0,0,0,.03) 100%);
  pointer-events:none; z-index:0;
}
</style>