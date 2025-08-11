<template>
  <!-- 툴바 -->
  <nav class="toolbar">
    <div class="toolbar-container">
      <img src="@/assets/logo.png" alt="로고" class="logo">
      
      <div v-if="ui.showMenu" class="nav-menu">
        <router-link to="/admin/game" class="nav-item">게임</router-link>
        <!-- userRole을 store에서 가져와서 전달 -->
        <a href="#" @click.prevent="navigateTo('/challenges')" class="nav-item">도전과제</a>
        <a href="#" class="nav-item">게시판</a>
        <a href="#" class="nav-item">순위</a>
        <a href="#" class="nav-item">AI 신문</a>
      </div>
      
      <div v-if="!ui.showProfile" class="welcome-message" v-html="ui.welcomeText"></div>
      <img v-else src="@/assets/profile.png" alt="프로필" class="profile">
    </div>
  </nav>
    
  <!-- 이 아래 부분에 이제 본문 위치 -->
   <!-- <main :style="{ marginTop: hideToolbar ? '0' : '60px', paddingTop: hideToolbar ? '0' : '20px' }"></main> -->
  <main class="">
    <RouterView />
  </main>
</template>

<script setup>
import { RouterView, useRouter } from 'vue-router'
import { useUiStore } from '@/stores/useUiStore'
import { useUserStore } from '@/stores/user.js'
import { onMounted } from 'vue'

const ui = useUiStore()
const user = useUserStore()
const router = useRouter()

// 네비게이션 함수 (userRole을 store에서 가져와서 전달)
const navigateTo = (path) => {
  if (path === '/challenges') {
    router.push({
      path: path,
      query: { userRole: user.userRole }
    })
  } else {
    router.push(path)
  }
}

// ✅ 수정된 마운트 로직
onMounted(async () => {
  // 공개 페이지들 정의
  const publicRoutes = ['/', '/login', '/onboarding']
  const currentPath = window.location.pathname
  
  console.log('App 시작, 현재 경로:', currentPath)
  
  // ✅ 공개 페이지가 아닌 경우에만 인증 확인
  if (!publicRoutes.includes(currentPath)) {
    try {
      console.log('보호된 페이지 - 인증 확인 중...')
      await user.fetchUserRole()
      console.log('인증 확인 완료')
    } catch (error) {
      console.log('인증 실패, 로그인 페이지로 이동')
      // CORS 에러나 401 에러 시 로그인 페이지로 리다이렉트
      window.location.href = '/login'
    }
  } else {
    console.log('공개 페이지 - 인증 확인 스킵')
  }
})
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
  position: fixed; 
  top: 0;
  left: 0;
  right: 0; 
  width: 100vw;
  z-index: 999; 
  background-color: white;
}

.toolbar-container {
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
  font-weight: 700; 
  font-size: 20px;
  padding: 8px 16px;
  border-radius: 6px;
} 

.nav-item:hover {
  background: white;
  color: #007bff;
}

.profile {
  height: 40px;
  width: 40px;
  cursor: pointer;
}

main {
  margin-top: 60px; 
  padding-top: 20px; 
}
</style>