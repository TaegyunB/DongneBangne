<template>
  <!-- 툴바 -->
  <nav v-if="!hideToolbar" class="toolbar">
    <div class="toolbar-container">
      <img @click="router.push('/mainpage')" src="@/assets/logo.png" alt="로고" class="logo">
             
      <div v-if="ui.showMenu" class="nav-menu">
        <router-link to="/admin/game" class="nav-item">게임</router-link>
        <!-- userRole을 전달해야 하기 때문에 따로 처리  -->
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
  <!--<main class="">
    <RouterView />
  </main>-->

  <main :style="{ marginTop: hideToolbar ? '0' : '60px', paddingTop: hideToolbar ? '0' : '20px' }">
  <RouterView />
</main>
</template>

<script setup>
import { RouterView, useRoute, useRouter } from 'vue-router'
import { useUiStore } from '@/stores/useUiStore'
import { ref, onMounted, computed } from 'vue'

const ui = useUiStore()
const route = useRoute()
const router = useRouter();
const userRole = ref('')

// 툴바 비제공
const hideToolbar = computed(() => route.matched.some(r => r.meta?.hideToolbar))

//userRole back에서 받아오기 
const fetchUserInfo = async () => {
  try {
    const response = await fetch('/api/v1/main/me', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })
    
    if (response.ok) {
      const data = await response.json()
      userRole.value = data.userRole
      console.log('Toolbar - User role:', userRole.value)
    } else {
      console.error('Failed to fetch user info')
      userRole.value = 'MEMBER'
    }
  } catch (error) {
    console.error('Error fetching user info:', error)
    userRole.value = 'MEMBER'
  }
}

// 네비게이션 함수 (userRole을 함께 전달)
const navigateTo = (path) => {
  if (path === '/challenges') {
    router.push({ 
      path: path, 
      query: { userRole: userRole.value } 
    })
  } else {
    router.push(path)
  }
}

// 로고 클릭 시 홈으로 이동
// const goToHome = () => {
//   router.push('/');
// };

// 컴포넌트 마운트 시 사용자 정보 가져오기
onMounted(() => {
  fetchUserInfo()
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