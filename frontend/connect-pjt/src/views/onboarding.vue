<template>
  <div class="onboarding-container">
    <!-- 섹션 1 -->
    <section class="onboarding-wrapper" ref="sectionRefs[0]">
      <div class="onboarding-content">
        <div class="text-box">
          <h1 class="title">“동네방네, <span class="highlight">경로당을 연결하다</span>”</h1>
          <p class="subtitle">전국 경로당이 하나로 연결되어<br />함께 웃고 소통하는 세상을 만듭니다.</p>
        </div>
        <div class="image-box">
          <img src="@/assets/onboarding/onboarding1.png" alt="온보딩1" />
        </div>
      </div>
    </section>

    <!-- 로그인 버튼 -->
    <div class="login-section" ref="sectionRefs[1]">
      <div class="kakao-login">
        <span class="bubble-icon">💬</span>
        <span class="login-text">카카오 로그인</span>
      </div>
      <p class="login-note">
        *처음 이용하신다면 <span class="highlight">로그인 후 경로당을 등록</span>하시면 가입이 완료됩니다.<br />
        (“로그인 후 등록된 경로당 선택”으로 이동됩니다.)
      </p>
    </div>

    <!-- 섹션 2~7 -->
    <section
      v-for="(item, index) in onboardingSections"
      :key="index"
      class="onboarding-wrapper"
      :ref="el => sectionRefs[index + 2] = el"
    >
      <div class="onboarding-content">
        <div v-if="index % 2 === 0" class="text-box">
          <h1 class="title" v-html="item.title" />
          <p class="subtitle" v-html="item.subtitle" />
        </div>
        <div class="image-box">
          <img :src="item.image" :alt="`온보딩 이미지 ${index + 2}`" />
        </div>
        <div v-if="index % 2 === 1" class="text-box">
          <h1 class="title" v-html="item.title" />
          <p class="subtitle" v-html="item.subtitle" />
        </div>
      </div>
    </section>

    <!-- 아래 화살표 -->
    <button class="scroll-down-btn" @click="scrollToNextSection">
      ⌄
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUiStore } from '@/stores/useUiStore'

import onboarding2 from '@/assets/onboarding/onboarding2.png'
import onboarding3 from '@/assets/onboarding/onboarding3.png'
import onboarding4 from '@/assets/onboarding/onboarding4.png'
import onboarding5 from '@/assets/onboarding/onboarding5.png'
import onboarding6 from '@/assets/onboarding/onboarding6.png'
import onboarding7 from '@/assets/onboarding/onboarding7.png'

const ui = useUiStore()
onMounted(() => {
  ui.showLogo = false
  ui.showMenu = false
  ui.showProfile = false
  ui.welcomeText = '지금 동네방네를 <span class="start-word">시작</span>해보세요!'
})
onUnmounted(() => {
  ui.showLogo = true
  ui.showMenu = true
  ui.showProfile = true
  ui.welcomeText = ''
})

// 스크롤 기능
const sectionRefs = ref([])
let currentIndex = 0
const scrollToNextSection = () => {
  currentIndex = (currentIndex + 1) % sectionRefs.value.length
  sectionRefs.value[currentIndex]?.scrollIntoView({ behavior: 'smooth' })
}

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
  },
  {
    image: onboarding7,
    title: '“당신을 위한 <span class="highlight">따뜻한 연결</span>”',
    subtitle: '혼자가 아닌, 함께하는<br />경로당을 만들어가요.'
  }
]
</script>

<style scoped>
.onboarding-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-top: 0;
  position: relative;
}

.onboarding-wrapper {
  display: flex;
  justify-content: center;
  padding: 0 40px;
  margin: 40px 0;
}

.onboarding-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
  max-width: 1100px;
  width: 100%;
  text-align: center;
}

@media (min-width: 1024px) {
  .onboarding-content {
    flex-direction: row;
    justify-content: center;
    align-items: center;
    text-align: center;
    gap: 80px;
  }
}

.text-box {
  max-width: 500px;
}

.image-box img {
  width: 600px;
  height: auto;
  object-fit: contain;
}

.title {
  font-size: 32px;
  font-weight: bold;
}

.subtitle {
  font-size: 20px;
  color: #444;
  line-height: 1.6;
}

.highlight {
  color: #fbbf24;
  font-weight: bold;
}

.login-section {
  text-align: center;
  margin-top: 30px;
  margin-bottom: 60px;
}

.kakao-login {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: #fee500;
  padding: 14px 30px;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #3c1e1e;
  gap: 10px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  margin-bottom: 20px;
}

.kakao-login:hover {
  background-color: #fddc00;
}

.bubble-icon {
  font-size: 20px;
}

.login-note {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.login-note .highlight {
  color: #d97706;
  font-weight: 600;
}

.start-word {
  color: #3b82f6;
  font-weight: bold;
}

.scroll-down-btn {
  position: fixed;
  bottom: 20px;
  left: 20px;
  font-size: 60px;
  background: none;
  border: none;
  cursor: pointer;
  opacity: 0.6;
  transition: 0.3s;
  z-index: 999;
}
.scroll-down-btn:hover {
  opacity: 1;
}
</style>
