<template>
  <div class="main-landing">
    <!-- 상단 소개 -->
    <section class="top-section">
      <div class="top-bg-img">
        <img src="@/assets/mainpage/background.png" alt="메인 배경" class="main-bg-image" />
      </div>
      <div class="top-text">
        <h1>동네방네에서 만나는<br />새로운 하루</h1>
        <br>
        <p class="desc">
          우리 경로당 친구들과 도전 미션을 수행하고,<br />
          다른 경로당 사람들과 재미있는 게임도 하며,<br />
          매달 우리만의 특별한 신문도 만들어보세요.<br />
          <br></br>
        </p>
        <p class="bold-desc"> " 혼자가 아닌 <strong>함께라서</strong> 더욱 즐거운 일상이 시작됩니다." </p>
        
      </div>
    </section>
    <br>
    <div class="section-title">이야기하고, 도전하고, 함께하는 경로당 서비스</div>
    <br>
    
    <!-- Admin,member UI 통합 -->
    <div class="main-card-grid-one">
      <div class="main-card one" @click="goToChallenges">
        <div class="card-title">도전 과제</div>
        <div class="card-desc">함께라서 더 의미있는 도전<br />매달 다양한 도전을 해보세요!</div>
        <img src="@/assets/mainpage/assignment.png" alt="도전 아이콘" class="card-icon" />
      </div>
      <div class="main-card two" @click="goTo('/admin/games')">
        <div class="card-title">게임</div>
        <div class="card-desc">다른 경로당과 다양한 게임을<br />즐겨보세요!</div>
        <img src="@/assets/mainpage/joystick.png" alt="게임 아이콘" class="card-icon" />
      </div>
    </div>
    <div class="main-card-grid-two">
      <div class="main-card three" @click="goTo('/boards')">
        <div class="card-title">게시판</div>
        <div class="card-desc">이웃 경로당들과 소통하는 공간</div>
        <img src="@/assets/mainpage/community.png" alt="게시판 아이콘" class="card-icon" />
      </div>
      <div class="main-card four" @click="goTo('/ranking')">
        <div class="card-title">순위</div>
        <div class="card-desc">우리 경로당은 몇 등일까요?</div>
        <img src="@/assets/mainpage/ranking.png" alt="순위 아이콘" class="card-icon" />
      </div>
      <div class="main-card five" @click="goTo('/news')">
        <div class="card-title">AI 신문</div>
        <div class="card-desc">매달 우리만의 특별한 소식지</div>
        <img src="@/assets/mainpage/newspaper.png" alt="신문 아이콘" class="card-icon" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const userRole = ref('')

// 도전으로 넘겨줄 role 백으로부터 받아오기. API주소 수정 필요. 
const fetchUserRole = async () => {
  try {
    const response = await axios.get('/api/user/role', {
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (response.status === 200) {
      userRole.value = response.data.userrole // API 응답에서 userrole 추출
      console.log('사용자 역할:', userRole.value)
    } else {
      console.error('Failed to fetch user role')
      userRole.value = 'ADMIN' // 기본값 설정
    }
  } catch (error) {
    console.error('Error fetching user role:', error)
    userRole.value = 'ADMIN' // 에러 시 기본값 설정
  }
}

// 도전 과제 페이지로 이동하면서 userRole을 props로 전달
const goToChallenges = () => {
  router.push({
    name: 'challenge', // 또는 실제 라우트 이름
    params: { userRole: userRole.value }
  })
}

// 일반 카드 클릭 시 이동 함수
function goTo(url) {
  router.push(url)
}

// 컴포넌트 마운트 시 사용자 역할 정보 가져오기
onMounted(() => {
  fetchUserRole()
})
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
  overflow: hidden;
}
.top-bg-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}
.main-bg-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  user-select: none;
}
.top-text {
  position: relative;
  z-index: 2;
  padding: 80px 0 45px 0;
  text-align: center;
}
.top-text h1 {
  font-size: 54px;
  font-weight: 700;
  color: #222;
  margin-bottom: 18px;
  line-height: 1.23;
}
.desc {
  color: #3a3a3a;
  font-size: 25px;
  line-height: 1.8;
  font-weight: 400;
}
.bold-desc{
  color: #3a3a3a;
  font-size: 28px;
  line-height: 1.8;
  font-weight: 600;
}
.bold-desc strong{
  color: #ff8c21;
  font-size: 32;
  line-height: 1.8;
}
.section-title {
  margin-top: 38px;
  margin-bottom: 35px;
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
.main-card-grid-three {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 34px;
  width: 1400px;
  max-width: 1300px;
  margin: 0 auto;
  margin-bottom: 50px;
}
.main-card-grid-four {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
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
  width: 90px;
  height: 90px;
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
.magnifier-btn {
  position: fixed;
  right: 38px;
  bottom: 34px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 3px 12px rgba(70,80,100,0.4);
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