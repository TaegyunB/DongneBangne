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
      lensSize: 400, // 2배로 확대
      zoomLevel: 2,
      excludedRoutes: ['/games', '/webrtc']
    }
  },
  computed: {
    shouldShowMagnifier() {
      const currentPath = this.$route?.path || '';
      return !this.excludedRoutes.some(route => 
        currentPath.startsWith(route)
      );
    },
    
    lensStyle() {
      return {
        left: `${this.mouseX - this.lensSize/2}px`,
        top: `${this.mouseY - this.lensSize/2}px`,
        width: `${this.lensSize}px`,
        height: `${this.lensSize}px`,
      }
    },
    
    contentStyle() {
      // CSS transform으로 실시간 확대
      // 마우스 위치가 렌즈 중앙에 오도록 계산
      const offsetX = -this.mouseX * this.zoomLevel + this.lensSize/2;
      const offsetY = -this.mouseY * this.zoomLevel + this.lensSize/2;
      
      return {
        transform: `scale(${this.zoomLevel}) translate(${offsetX/this.zoomLevel}px, ${offsetY/this.zoomLevel}px)`,
        transformOrigin: '0 0',
      }
    }
  },
  
  methods: {
    toggleMagnifier() {
      this.isActive = !this.isActive;
      
      if (this.isActive) {
        this.startMagnifier();
        try {
          localStorage.setItem('magnifierActive', 'true');
        } catch (e) {
          console.warn('LocalStorage not available:', e);
        }
      } else {
        this.stopMagnifier();
        try {
          localStorage.removeItem('magnifierActive');
        } catch (e) {
          console.warn('LocalStorage not available:', e);
        }
      }
    },
    
    startMagnifier() {
      document.addEventListener('mousemove', this.handleMouseMove);
      document.body.style.cursor = 'none';
      
      // 돋보기용 body 복사본 생성
      this.createMagnifierContent();
    },
    
    stopMagnifier() {
      document.removeEventListener('mousemove', this.handleMouseMove);
      document.body.style.cursor = 'auto';
    },
    
    handleMouseMove(event) {
      // 실시간으로 마우스 위치 업데이트 (throttling 없음)
      this.mouseX = event.clientX;
      this.mouseY = event.clientY;
    },
    
    createMagnifierContent() {
      this.$nextTick(() => {
        if (!this.$refs.magnifierLens) return;
        
        const content = this.$refs.magnifierLens.querySelector('.magnifier-content');
        if (!content) return;
        
        // 현재 페이지의 HTML을 그대로 복사
        const htmlClone = document.documentElement.cloneNode(true);
        
        // 돋보기 관련 요소들 제거
        const magnifierElements = htmlClone.querySelectorAll('.magnifier-toggle, .magnifier-lens');
        magnifierElements.forEach(el => el.remove());
        
        // 모든 스크립트 제거 (이벤트 중복 방지)
        const scripts = htmlClone.querySelectorAll('script');
        scripts.forEach(script => script.remove());
        
        // 링크와 버튼의 기본 동작 방지
        const interactiveElements = htmlClone.querySelectorAll('a, button, input, select, textarea');
        interactiveElements.forEach(el => {
          el.style.pointerEvents = 'none';
        });
        
        // 내용 교체
        content.innerHTML = htmlClone.outerHTML;
      });
    },
    
    initializeMagnifier() {
      try {
        const savedState = localStorage.getItem('magnifierActive');
        if (savedState === 'true') {
          this.isActive = true;
          this.$nextTick(() => {
            this.startMagnifier();
          });
        }
      } catch (e) {
        console.warn('LocalStorage not available:', e);
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
    '$route'() {
      if (!this.shouldShowMagnifier && this.isActive) {
        this.isActive = false;
        this.stopMagnifier();
      } else if (this.shouldShowMagnifier && this.isActive) {
        this.$nextTick(() => {
          this.startMagnifier();
        });
      }
    },
    
    // 돋보기가 활성화되면 내용 업데이트
    isActive(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          this.createMagnifierContent();
        });
      }
    }
  }
}
</script>

<style scoped>
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

.magnifier-lens {
  position: fixed;
  z-index: 10000;
  border: 3px solid #333;
  border-radius: 50%;
  overflow: hidden;
  pointer-events: none;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
  background: #fff;
}

.magnifier-content {
  width: 100vw;
  height: 100vh;
  position: absolute;
  overflow: hidden;
  pointer-events: none;
}

/* 돋보기 내부 요소들의 상호작용 차단 */
.magnifier-content * {
  pointer-events: none !important;
  user-select: none !important;
}

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

.magnifier-toggle:focus {
  outline: 3px solid #FFD700;
  outline-offset: 3px;
}
</style>