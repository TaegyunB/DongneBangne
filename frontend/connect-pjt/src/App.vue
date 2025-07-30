<template>
  <!-- 툴바 -->
  <nav class="toolbar">
    <div class="toolbar-container">
      <img src="@/assets/logo.png" alt="로고" class="logo">
             
      <div v-if="ui.showMenu" class="nav-menu">
        <a href="#" class="nav-item">게임</a>
        <router-link to="/challenges" class="nav-item">도전과제</router-link>
        <a href="#" class="nav-item">게시판</a>
        <a href="#" class="nav-item">순위</a>
        <a href="#" class="nav-item">AI 신문</a>
      </div>
             
      <div v-if="!ui.showProfile" class="welcome-message" v-html="ui.welcomeText"></div>
      <img v-else src="@/assets/profile.png" alt="프로필" class="profile">
    </div>
  </nav>
     
  <!-- 이 아래 부분에 이제 본문 위치 -->
  <main class="">
    <RouterView />
  </main>
</template>

<script setup>
import { RouterView, useRouter  } from 'vue-router';
import { useUiStore } from '@/stores/useUiStore'

const ui = useUiStore()

const router = useRouter();

// 로고 클릭 시 홈으로 이동
// const goToHome = () => {
//   router.push('/');
// };

</script>

<style>
/* 전역 스타일로 변경 - 이걸 해야, 툴바가 상단에 딱 붙고, 좌우 여백 안생김 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
}

body {
  margin: 0;
  padding: 0;
}
</style>

<style scoped>
.toolbar {
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: fixed; /* sticky -> fixed로 변경 */
  top: 0;
  left: 0; /* 좌측에 완전히 붙이기 */
  right: 0; /* 우측에 완전히 붙이기 */
  width: 100vw; /* 전체 화면 너비 */
  z-index: 999; /* 모달보다 낮은 z-index */
  background-color: white;
}

.toolbar-container {
  /* max-width: 1200px; */
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  width: 100%;
}

.logo {
  height: 60px;
  /* 클릭 시 메인 페이지로 */
  cursor: pointer;
}

.nav-menu {
  display: flex;
  gap: 30px;
  flex: 1;
  justify-content: center;
}

.nav-item {
  text-decoration: none;
  color: #333;
  font-weight: 700; /* 볼드 */
  font-size: 20px;
  padding: 8px 16px;
  border-radius: 6px;
} /* font-size 수정(nav-bar) */

/* 마우스 위에 가져다 댈 때 */
.nav-item:hover {
  background: white;
  color: #007bff;
}

.profile {
  height: 40px;
  width: 40px;
  /* 클릭 시 프로필 페이지로 */
  cursor: pointer;
}

/* 메인 컨텐츠가 툴바에 가려지지 않도록 상단 여백 추가 */
main {
  margin-top: 60px; /* 툴바 높이만큼 여백 */
}
</style>