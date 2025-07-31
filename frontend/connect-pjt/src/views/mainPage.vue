<template>
  <div class="main-landing">
    <!-- 상단 소개 -->
    <section class="top-section">
      <div class="top-bg-img">
        <img src="@/assets/mainpage/background.png" alt="메인 배경" class="main-bg-image" />
      </div>
      <div class="top-text">
        <h1>동네방네에서 만나는<br />새로운 하루</h1>
        <p class="desc">
          우리 경로당 친구들과 도전 미션을 수행하고,<br />
          다른 경로당 사람들과 재미있는 게임도 하며,<br />
          매달 우리만의 특별한 신문도 만들어보세요.<br />
          혼자가 아닌 함께라서 더욱 즐거운 일상이 시작됩니다.
        </p>
      </div>
    </section>

    <div class="section-title">이야기하고, 도전하고, 함께하는 경로당 서비스</div>
    <!-- 메인 카드  -->
    <div class="main-card-grid">
      <div class="main-card orange" @click="goTo('/challenges')">
        <div class="card-title">도전 과제</div>
        <div class="card-desc">함께라서 더 의미있는 도전<br />매달 다양한 도전을 해보세요!</div>
        <img src="@/assets/mainpage/assignment.png" alt="도전 아이콘" class="card-icon" />
      </div>
      <div class="main-card blue" @click="goTo('/admin/games')">
        <div class="card-title">게임</div>
        <div class="card-desc">다른 경로당과 다양한 게임을<br />즐겨보세요!</div>
        <img src="@/assets/mainpage/joystick.png" alt="게임 아이콘" class="card-icon" />
      </div>
      <div class="main-card purple" @click="goTo('/boards')">
        <div class="card-title">게시판</div>
        <div class="card-desc">이웃 경로당들과 소통하는 공간</div>
        <img src="@/assets/mainpage/community.png" alt="게시판 아이콘" class="card-icon" />
      </div>
      <div class="main-card orange" @click="goTo('/ranking')">
        <div class="card-title">순위</div>
        <div class="card-desc">우리 경로당은 몇 등일까요?</div>
        <img src="@/assets/mainpage/ranking.png" alt="순위 아이콘" class="card-icon" />
      </div>
      <div class="main-card blue" @click="goTo('/news')">
        <div class="card-title">AI 신문</div>
        <div class="card-desc">매달 우리만의 특별한 소식지</div>
        <img src="@/assets/mainpage/newspaper.png" alt="신문 아이콘" class="card-icon" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

// 카드 클릭 시 이동 함수
function goTo(url) {
  router.push(url)
}
</script>

<style scoped>
.main-landing {
  width: 100%;
  min-height: 100vh;
  background: #fff;
  padding-bottom: 60px;
}
.top-section {
  position: relative;
  margin-top: 34px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.top-bg-img {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  position: absolute;
  left: 50%;
  top: -60px;
  transform: translateX(-50%);
  z-index: 1;
  opacity: 0.17;
  pointer-events: none;
}
.main-bg-image {
  width: 100%;
  max-width: 900px;
  min-width: 480px;
  height: auto;
  object-fit: contain;
  user-select: none;
}
.top-text {
  position: relative;
  z-index: 2;
  padding: 80px 0 45px 0;
  text-align: center;
}
.top-text h1 {
  font-size: 2.6rem;
  font-weight: 800;
  color: #222;
  margin-bottom: 18px;
  line-height: 1.23;
}
.desc {
  color: #3a3a3a;
  font-size: 1.19rem;
  line-height: 1.8;
  font-weight: 400;
}
.section-title {
  margin-top: 38px;
  margin-bottom: 23px;
  font-size: 1.22rem;
  font-weight: 600;
  text-align: center;
  color: #1e1e1e;
}
.main-card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 34px;
  width: 900px;
  max-width: 97vw;
  margin: 0 auto;
  margin-bottom: 50px;
}
.main-card {
  position: relative;
  height: 178px;
  background: #fff;
  border-radius: 17px;
  box-shadow: 0 4px 16px rgba(44,66,90,0.11);
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
  box-shadow: 0 8px 22px rgba(44,66,90,0.17);
  background: #e7ecff;
}
.card-title {
  font-size: 1.16rem;
  font-weight: 700;
  margin-bottom: 12px;
  color: #222;
}
.card-desc {
  font-size: 1rem;
  color: #232426;
  margin-bottom: 6px;
}
.card-icon {
  position: absolute;
  right: 24px;
  bottom: 19px;
  width: 44px;
  height: 44px;
  object-fit: contain;
  opacity: 0.98;
}
.main-card.orange {
  background: #ffe5c6;
}
.main-card.blue {
  background: #bddcff;
}
.main-card.purple {
  background: #dadcff;
}
.magnifier-btn {
  position: fixed;
  right: 38px;
  bottom: 34px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 3px 12px rgba(70,80,100,0.13);
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  outline: none;
  cursor: pointer;
  z-index: 50;
}
.magnifier-btn img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}
</style>
