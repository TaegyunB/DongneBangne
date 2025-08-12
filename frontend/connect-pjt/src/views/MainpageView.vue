<template>
  <div class="main-landing">
    <!-- 제목과 로고를 함께 배치하는 헤더 섹션 -->
    <div class="header-section">
      <div class="section-title">이야기하고, 도전하고, 함께하는 동네방네</div>
    </div>
    
    <!-- Admin,member UI 공통 -->
    <div class="main-card-grid-one">
      <div class="main-card one" @click="goTo('/challenges')">
        <div class="card-title_2">도전 과제</div>
        <div class="card-desc">매달 다양한 도전을 <br />수행해보세요!</div>
        <img src="@/assets/mainpage_2/challenge.png" alt="도전 아이콘" class="card-icon_2" />
      </div>
      <div class="main-card two" @click="goTo('/games')">
        <div class="card-title_2">트로트 게임</div>
        <div class="card-desc">다른 경로당과<br />게임을 즐겨보세요!</div>
        <img src="@/assets/mainpage_2/game.png" alt="게임 아이콘" class="card-icon_2" />
      </div>
      <div class="main-card five" @click="goTo('/news')">
        <div class="card-title_2">AI 신문</div>
        <div class="card-desc">매달 우리만의 <br />특별한 소식지</div>
        <img src="@/assets/mainpage_2/news.png" alt="신문 아이콘" class="card-icon_2" />
      </div>
    </div>
    <div class="main-card-grid-two">
      <div class="main-card three" @click="goTo('/boards')">
        <div class="card-title">게시판</div>
        <div class="card-desc">이웃 경로당들과 소통하는 공간</div>
        <img src="@/assets/mainpage_2/chat.png" alt="게시판 아이콘" class="card-icon" />
      </div>
      <div class="main-card four" @click="goTo('/rankings')">
        <div class="card-title">순위</div>
        <div class="card-desc">우리 경로당은 몇 등일까요?</div>
        <img src="@/assets/mainpage_2/ranking.png" alt="순위 아이콘" class="card-icon" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'

const router = useRouter()
const userStore = useUserStore()

console.log("UserStore 정보:", userStore)

// userRole을 백에서 가져오기
const fetchUserInfo = async () => {
  await userStore.fetchUserRole()
}

// userRole을 prop으로 전달하지 않고 그냥 이동
function goTo(url) {
  router.push(url)
}

// 컴포넌트 마운트 시 사용자 정보 가져오기
onMounted(() => {
  fetchUserInfo()
})

// userRole을 외부에서 사용할 수 있도록 export (필요시)
defineExpose({
  userRole: userStore.userRole
})
</script>

<style scoped>
/* 글꼴 정의 */
@font-face {
  font-family: 'KoddiUD';
  src: url('@/assets/fonts/KoddiUDOnGothic-Regular.ttf') format('truetype');
  font-weight: 400;
  font-style: normal;
}

@font-face {
  font-family: 'KoddiUD';
  src: url('@/assets/fonts/KoddiUDOnGothic-Bold.ttf') format('truetype');
  font-weight: 700;
  font-style: normal;
}

@font-face {
  font-family: 'KoddiUD';
  src: url('@/assets/fonts/KoddiUDOnGothic-ExtraBold.ttf') format('truetype');
  font-weight: 800;
  font-style: normal;
}
/* 회색 버전 */
.main-landing {
  width: 100%;
  min-height: 100vh;
  background: #f8f9fadc;
  padding: 40px 0;
}

/* .main-landing {
  width: 100%;
  min-height: 100vh;
  background-image: url('@/assets/background/back3.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  padding: 60px 0;
} */

/* 헤더 섹션 - 제목과 로고를 함께 배치 */
.header-section {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40px;
  gap: 40px;
}

.section-title {
  font-size: 40px;
  font-weight: 750;
  text-align: center;
  color: #1e1e1e;
  font-family: 'KoddiUD', sans-serif; /* KoddiUD 적용 */
}

.logo-icon {
  width: 100px;
  height: 100px;
  object-fit: contain;
}

.main-card-grid-one {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 34px;
  width: 1400px;
  max-width: 1300px;
  margin: 0 auto;
  margin-bottom: 30px;
}

.main-card-grid-two {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 34px;
  width: 1400px;
  max-width: 1300px;
  margin: 0 auto;
  margin-bottom: 30px;
}

.main-card {
  position: relative;
  height: 240px;
  background: #fff;
  border-radius: 17px;
  box-shadow: 0 4px 16px rgba(44,66,90,0.3);
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  padding: 32px 20px 14px 40px;
  min-width: 0;
  overflow: hidden;
  transition: transform 0.11s;
  cursor: pointer;
  user-select: none;
}

.main-card:hover {
  transform: translateY(-6px) scale(1.035);
  box-shadow: 0 8px 22px rgba(44,66,90,0.4);
  background: #e7ecff;
}

.card-title {
  font-size: 32px;
  font-weight: 750;
  margin-bottom: 30px;
  color: #222;
  font-family: 'KoddiUD', sans-serif; /* KoddiUD 적용 */
}
.card-title_2 {
  font-size: 32px;
  font-weight: 750;
  margin-bottom: 20px;
  color: #222;
  font-family: 'KoddiUD', sans-serif; /* KoddiUD 적용 */
}

.card-desc {
  font-size: 25px;
  color: #232426;
  margin-bottom: 6px;
  font-family: 'KoddiUD', sans-serif; /* KoddiUD 적용 */
}

.card-icon {
  position: absolute;
  right: 30px;
  bottom: 20px;
  width: 180px;
  height: 180px;
  object-fit: contain;
}

.card-icon_2 {
  position: absolute;
  right: 24px;
  bottom: 10px;
  width: 110px;
  height: 120px;
  object-fit: contain;
}

/* 그라데이션 배경 */
.main-card.one {
  background: linear-gradient(135deg,#f5efba 0%, #ffdc72 100%);
}

.main-card.two {
  background: linear-gradient(135deg,#f5efba 0%, #ffdc72 100%);
}

.main-card.three {
  background: linear-gradient(135deg,#d1daff 0%, #8ba3ff 100%);
}

.main-card.four {
  background: linear-gradient(135deg,#d1daff 0%, #8FA3F3 100%);
}

.main-card.five {
  background: linear-gradient(135deg,#f7efd0 0%, #FFE082 100%);
}
 /* ---------------------- */
 /* .main-card.one {
  background: linear-gradient(135deg, #FFD7B9 0%, #FFBC8E 100%);
}

.main-card.two {
  background: linear-gradient(135deg,#ABBAF9 0%, #8FA3F3 100%);
}

.main-card.three {
  background: linear-gradient(135deg,#ABBAF9 0%, #8FA3F3 100%);
}

.main-card.four {
  background: linear-gradient(135deg, #FFD7B9 0%, #FFBC8E 100%);
}

.main-card.five {
  background: linear-gradient(135deg,#ABBAF9 0%, #8FA3F3 100%);
} */

</style>