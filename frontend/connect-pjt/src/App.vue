<template>
  <!-- 툴바 -->
  <nav class="toolbar">
    <div class="toolbar-container">
      <img src="@/assets/logo.png" alt="로고" class="logo">
      
      <div class="nav-menu">
        <a href="#" class="nav-item">게임</a>
        <a href="#" class="nav-item">도전과제</a>
        <a href="#" class="nav-item">게시판</a>
        <a href="#" class="nav-item">순위</a>
        <a href="#" class="nav-item">AI 신문</a>
      </div>
      
      <img src="@/assets/profile.png" alt="프로필" class="profile">
    </div>
  </nav>
  
  <!-- 이 아래 부분에 이제 본문 위치 -->
  <main class="">
    <RouterView />
  </main>
</template>

<script setup>
</script>

<style scoped>
.toolbar {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.toolbar-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
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
  /* 볼드 */
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 6px;
  /* transition: all 0.2s; */
}

/* 마우스 위에 가져다 댈 때 */
.nav-item:hover {
  background: #f0f0f0;
  color: #007bff;
}

.profile {
  height: 40px;
  width: 40px;
  /* 클릭 시 프로필 페이지로 */
  cursor: pointer;
}

</style>