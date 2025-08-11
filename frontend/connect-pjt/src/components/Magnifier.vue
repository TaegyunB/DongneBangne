<template>
  <div>
    <!-- 돋보기 토글 버튼 -->
    <div 
      v-if="shouldShowMagnifier" 
      class="magnifier-toggle"
      @click="toggleMagnifier"
      :class="{ active: isActive }"
    >
      <svg 
        width="32" 
        height="32" 
        viewBox="0 0 24 24" 
        fill="none" 
        xmlns="http://www.w3.org/2000/svg"
      >
        <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
        <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2"/>
        <line v-if="!isActive" x1="11" y1="8" x2="11" y2="14" stroke="currentColor" stroke-width="2"/>
        <line v-if="!isActive" x1="8" y1="11" x2="14" y2="11" stroke="currentColor" stroke-width="2"/>
      </svg>
    </div>

    <!-- 돋보기 렌즈 -->
    <div 
      v-if="isActive && shouldShowMagnifier"
      ref="magnifierLens"
      class="magnifier-lens"
      :style="lensStyle"
    >
      <div 
        class="magnifier-content"
        :style="contentStyle"
      ></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MouseMagnifier',
  data() {
    return {
      isActive: false,
      mouseX: 0,
      mouseY: 0,
      lensSize: 300, // 돋보기 크기 (2배 확대)
      zoomLevel: 2,  // 확대 배율 (200%)
      excludedRoutes: ['/games', '/webrtc'] // 게임 관련 라우트 목록
    }
  },
  computed: {
    shouldShowMagnifier() {
      // 현재 라우트가 게임 페이지가 아닌 경우에만 표시
      const currentPath = this.$route?.path || '';
      return !this.excludedRoutes.some(route => 
        currentPath.startsWith(route)
      );
    },
    
    lensStyle() {
      return {
        left: `${this.mouseX}px`,
        top: `${this.mouseY}px`,
        width: `${this.lensSize}px`,
        height: `${this.lensSize}px`,
      }
    },
    
    contentStyle() {
      // 마우스 위치를 중심으로 정확한 확대 계산
      const centerX = this.lensSize / 2;
      const centerY = this.lensSize / 2;
      
      // 마우스 위치가 확대된 후에도 렌즈 중심에 오도록 계산
      const translateX = centerX - (this.mouseX * this.zoomLevel);
      const translateY = centerY - (this.mouseY * this.zoomLevel);
      
      return {
        transform: `scale(${this.zoomLevel}) translate(${translateX / this.zoomLevel}px, ${translateY / this.zoomLevel}px)`,
        transformOrigin: '0 0',
      }
    }
  },
  
  methods: {
    toggleMagnifier() {
      this.isActive = !this.isActive;
      
      if (this.isActive) {
        this.startMagnifier();
        // 상태 저장
        localStorage.setItem('magnifierActive', 'true');
      } else {
        this.stopMagnifier();
        localStorage.removeItem('magnifierActive');
      }
    },
    
    startMagnifier() {
      document.addEventListener('mousemove', this.handleMouseMove);
      document.body.style.cursor = 'none'; // 기본 커서 숨김
      this.updateMagnifierContent();
    },
    
    stopMagnifier() {
      document.removeEventListener('mousemove', this.handleMouseMove);
      document.body.style.cursor = 'auto'; // 커서 복원
    },
    
    handleMouseMove(event) {
      // 마우스 위치를 정확히 가져오기 (뷰포트 기준)
      this.mouseX = event.clientX;
      this.mouseY = event.clientY;
      this.updateMagnifierContent();
    },
    
    updateMagnifierContent() {
      this.$nextTick(() => {
        if (!this.$refs.magnifierLens) return;
        
        const lens = this.$refs.magnifierLens;
        const content = lens.querySelector('.magnifier-content');
        
        if (content) {
          // 현재 페이지 전체를 캡처하여 복사
          const html = document.documentElement.cloneNode(true);
          
          // 돋보기 관련 요소들 제거
          const magnifierElements = html.querySelectorAll('.magnifier-toggle, .magnifier-lens');
          magnifierElements.forEach(el => el.remove());
          
          // 기존 스타일 유지
          const existingStyles = Array.from(document.head.querySelectorAll('style, link[rel="stylesheet"]'));
          existingStyles.forEach(style => {
            if (!html.querySelector(`[href="${style.href}"]`) && !html.querySelector(`style[data-vite-dev-id="${style.dataset.viteDevId}"]`)) {
              html.head.appendChild(style.cloneNode(true));
            }
          });
          
          content.innerHTML = '';
          content.appendChild(html);
        }
      });
    },
    
    initializeMagnifier() {
      // 페이지 로드 시 이전 상태 복원
      const savedState = localStorage.getItem('magnifierActive');
      if (savedState === 'true') {
        this.isActive = true;
        this.$nextTick(() => {
          this.startMagnifier();
        });
      }
    }
  },
  
  mounted() {
    this.initializeMagnifier();
  },
  
  beforeUnmount() {
    this.stopMagnifier();
  },
  
  watch: {
    // 라우트 변경 시 돋보기 상태 관리
    '$route'() {
      if (!this.shouldShowMagnifier && this.isActive) {
        this.isActive = false;
        this.stopMagnifier();
      } else if (this.shouldShowMagnifier && this.isActive) {
        this.$nextTick(() => {
          this.startMagnifier();
        });
      }
    }
  }
}
</script>

<style scoped>
/* 돋보기 토글 버튼 */
.magnifier-toggle {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
  width: 60px;
  height: 60px;
  background: #4CAF50;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  color: white;
}

.magnifier-toggle:hover {
  background: #45a049;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
}

.magnifier-toggle.active {
  background: #FF6B6B;
  animation: pulse 2s infinite;
}

.magnifier-toggle.active:hover {
  background: #FF5252;
}

/* 돋보기 렌즈 */
.magnifier-lens {
  position: fixed;
  z-index: 10000;
  border: 3px solid #333;
  border-radius: 50%;
  overflow: hidden;
  pointer-events: none;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
  background: white;
  transform: translate(-50%, -50%);
}

.magnifier-content {
  width: 100vw;
  height: 100vh;
  position: absolute;
  top: 0;
  left: 0;
  overflow: visible;
}

/* 펄스 애니메이션 */
@keyframes pulse {
  0% {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 0 0 rgba(76, 175, 80, 0.7);
  }
  70% {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 0 10px rgba(76, 175, 80, 0);
  }
  100% {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 0 0 rgba(76, 175, 80, 0);
  }
}

/* 모바일 대응 */
@media (max-width: 768px) {
  .magnifier-toggle {
    width: 50px;
    height: 50px;
    bottom: 15px;
    right: 15px;
  }
  
  .magnifier-toggle svg {
    width: 24px;
    height: 24px;
  }
}

/* 접근성을 위한 키보드 포커스 */
.magnifier-toggle:focus {
  outline: 3px solid #FFD700;
  outline-offset: 3px;
}
</style>