<template>
  <div class="main-landing">
    <div class="section-title">이야기하고, 도전하고, 함께하는 경로당 서비스</div>
    
    <!-- Admin,member UI 공통 -->
    <div class="main-card-grid-one">
      <div class="main-card one" @click="goTo('/challenges')">
        <div class="card-title">도전 과제</div>
        <div class="card-desc">함께라서 더 의미있는 도전<br />매달 다양한 도전을 해보세요!</div>
        <img src="@/assets/mainpage_2/challenge.png" alt="도전 아이콘" class="card-icon" />
      </div>
      <div class="main-card two" @click="goTo('/games')">
        <div class="card-title">게임</div>
        <div class="card-desc">다른 경로당과 다양한 게임을<br />즐겨보세요!</div>
        <img src="@/assets/mainpage_2/game.png" alt="게임 아이콘" class="card-icon" />
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
      <div class="main-card five" @click="goTo('/news')">
        <div class="card-title">AI 신문</div>
        <div class="card-desc">매달 우리만의 특별한 소식지</div>
        <img src="@/assets/mainpage_2/news.png" alt="신문 아이콘" class="card-icon" />
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
.main-landing {
  width: 100%;
  min-height: 100vh;
  background: #fff;
  padding: 60px 0;
}

.section-title {
  margin-bottom: 60px;
  font-size: 30px;
  font-weight: 600;
  text-align: center;
  color: #1e1e1e;
}

.main-card-grid-one {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 34px;
  width: 1400px;
  max-width: 1300px;
  margin: 0 auto;
  margin-bottom: 50px;
}

.main-card-grid-two {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 34px;
  width: 1400px;
  max-width: 1300px;
  margin: 0 auto;
  margin-bottom: 50px;
}

.main-card {
  position: relative;
  height: 280px;
  background: #fff;
  border-radius: 17px;
  box-shadow: 0 4px 16px rgba(44,66,90,0.3);
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  padding: 23px 20px 14px 25px;
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
  font-weight: 700;
  margin-bottom: 12px;
  color: #222;
}

.card-desc {
  font-size: 25px;
  color: #232426;
  margin-bottom: 6px;
}

.card-icon {
  position: absolute;
  right: 24px;
  bottom: 19px;
  width: 120px;
  height: 120px;
  object-fit: contain;
  opacity: 0.98;
}

.main-card.one {
  background: #FFBF8F;
}

.main-card.two {
  background: #97B9FF;
}

.main-card.three {
  background: #ABBAF9;
}

.main-card.four {
  background: #F1C399;
}

.main-card.five {
  background: #7A91F7;
}
</style>