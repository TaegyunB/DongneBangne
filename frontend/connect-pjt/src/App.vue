<template>
  <!-- 툴바: 지정한 경로에서는 숨김 -->
  <nav v-if="!hideToolbar" class="toolbar">
    <div class="toolbar-container">
      <img src="@/assets/logo.png" alt="로고" class="logo" @click="router.push('/')" />

      <div v-if="ui.showMenu" class="nav-menu">
        <router-link to="/admin/game" class="nav-item">게임</router-link>
        <a href="#" @click.prevent="navigateTo('/challenges')" class="nav-item">도전과제</a>
        <a href="#" class="nav-item">게시판</a>
        <a href="#" class="nav-item">순위</a>
        <a href="#" class="nav-item">AI 신문</a>
      </div>

      <!-- 우측: 환영문구 또는 프로필 드롭다운 -->
      <div v-if="!ui.showProfile" class="welcome-message" v-html="ui.welcomeText"></div>

      <div v-else class="profile-wrap">
        <img
          ref="profileBtn"
          src="@/assets/profile.png"
          alt="프로필"
          class="profile"
          @click.stop="toggleProfileMenu"
        />
        <div v-if="showProfileMenu" class="profile-menu">
          <button class="menu-item" @click="goProfile">프로필로 가기</button>
          <button class="menu-item danger" @click="logout">로그아웃</button>
        </div>
      </div>
    </div>
  </nav>

  <!-- 본문 -->
  <main class="">
    <RouterView />
  </main>
</template>

<script setup>
import { RouterView, useRouter, useRoute } from 'vue-router'
import { onMounted, onBeforeUnmount, ref, computed } from 'vue'
import { useUiStore } from '@/stores/useUiStore'
import { useUserStore } from '@/stores/user.js'
import api from '@/api/axios'

const ui = useUiStore()
const user = useUserStore()
const router = useRouter()
const route = useRoute()

/** 툴바 숨김 경로 */
const HIDDEN_ROUTES = new Set(['/', '/login', '/games', '/mainpage'])
const hideToolbar = computed(() => HIDDEN_ROUTES.has(route.path))

/** 프로필 드롭다운 */
const showProfileMenu = ref(false)
const profileBtn = ref(null)

const toggleProfileMenu = () => {
  showProfileMenu.value = !showProfileMenu.value
}
const onDocClick = (e) => {
  const menu = document.querySelector('.profile-menu')
  if (
    showProfileMenu.value &&
    !profileBtn.value?.contains(e.target) &&
    !menu?.contains(e.target)
  ) {
    showProfileMenu.value = false
  }
}
onMounted(() => document.addEventListener('click', onDocClick, { capture: true }))
onBeforeUnmount(() => document.removeEventListener('click', onDocClick, { capture: true }))

const goProfile = () => {
  showProfileMenu.value = false
  router.push('/senior-center/profile')
}

const logout = () => {
  showProfileMenu.value = false
  // 헤더 토큰 제거(이 프로젝트 규칙)
  delete api.defaults.headers.common['Authorization']
  // 혹시 저장돼 있으면 같이 정리
  localStorage.removeItem('access_token')
  sessionStorage.removeItem('access_token')
  // 필요한 스토어 상태 초기화
  if (user?.$reset) user.$reset()
  router.push('/login')
}

/** 네비게이션 (userRole 전달 필요 시) */
const navigateTo = (path) => {
  if (path === '/challenges') {
    router.push({ path, query: { userRole: user.userRole } })
  } else {
    router.push(path)
  }
}

/** 공개/보호 라우트별 인증 체크 */
onMounted(async () => {
  const publicRoutes = ['/', '/login', '/onboarding']
  const currentPath = window.location.pathname
  if (!publicRoutes.includes(currentPath)) {
    try {
      await user.fetchUserRole()
    } catch (error) {
      window.location.href = '/login'
    }
  }
})
</script>

<style>
/* 전역 리셋 */
* { margin: 0; padding: 0; box-sizing: border-box; }
html, body, #app { width: 100%; height: 100%; }
</style>

<style scoped>
/* ───────── Toolbar (고정 해제: static) ───────── */
.toolbar {
  background: #fff;
  position: sticky; /* 여기만 변경 */
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 4px rgba(0,0,0,.06);
}
.toolbar-container {
  margin: 0 auto;
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo { height: 60px; cursor: pointer; }

/* 가운데 메뉴 */
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
.nav-item:hover { color: #007bff; }

/* 우측 프로필 */
.profile-wrap { position: relative; }
.profile { height: 40px; width: 40px; cursor: pointer; }

/* 드롭다운 */
.profile-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  min-width: 160px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
  padding: 6px;
  z-index: 1000;
}
.menu-item {
  width: 100%;
  text-align: left;
  padding: 10px 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 8px;
  font-size: 14px;
}
.menu-item:hover { background: #f3f4f6; }
.menu-item.danger { color: #dc2626; }
.menu-item.danger:hover { background: #fee2e2; }

/* 본문은 이제 top 마진 불필요 (툴바 고정 해제) */
main { margin-top: 0; padding-top: 0; }
</style>