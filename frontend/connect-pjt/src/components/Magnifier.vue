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

    <!-- 안내 팝업 -->
    <div 
      v-if="showPopup && isActive"
      class="help-popup"
      :style="popupStyle"
    >
      <div class="popup-content">
        <span>오른쪽 클릭하면 해제됩니다</span>
        <div class="popup-arrow"></div>
      </div>
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
      <!-- 돋보기 중앙 십자선 (마우스 위치 표시) -->
      <div class="magnifier-crosshair">
        <div class="crosshair-horizontal"></div>
        <div class="crosshair-vertical"></div>
      </div>
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
      excludedRoutes: ['/games', '/webrtc'],
      showPopup: false,
      popupTimer: null,
      domObserver: null,
      updateTimer: null
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
      // 현재 스크롤 위치 고려
      const scrollX = window.scrollX || document.documentElement.scrollLeft || 0;
      const scrollY = window.scrollY || document.documentElement.scrollTop || 0;
      
      // CSS transform으로 실시간 확대 + 스크롤 위치 반영
      // 마우스 위치가 렌즈 중앙에 오도록 계산
      const offsetX = -(this.mouseX + scrollX) * this.zoomLevel + this.lensSize/2;
      const offsetY = -(this.mouseY + scrollY) * this.zoomLevel + this.lensSize/2;
      
      return {
        transform: `scale(${this.zoomLevel}) translate(${offsetX/this.zoomLevel}px, ${offsetY/this.zoomLevel}px)`,
        transformOrigin: '0 0',
      }
    },

    popupStyle() {
      // 돋보기 버튼 근처에 팝업 표시
      return {
        right: '90px', // 버튼 왼쪽에 표시
        bottom: '80px'
      }
    }
  },
  
  methods: {
    toggleMagnifier() {
      this.isActive = !this.isActive;
      
      if (this.isActive) {
        this.startMagnifier();
        this.showHelpPopup();
        // localStorage 저장 제거 - 페이지별로 새로 시작
      } else {
        this.stopMagnifier();
        this.hideHelpPopup();
        // localStorage 제거도 불필요
      }
    },

    showHelpPopup() {
      this.showPopup = true;
      // 돋보기가 활성화되어 있는 동안 계속 표시
    },

    hideHelpPopup() {
      this.showPopup = false;
      if (this.popupTimer) {
        clearTimeout(this.popupTimer);
        this.popupTimer = null;
      }
    },
    
    startMagnifier() {
      document.addEventListener('mousemove', this.handleMouseMove);
      document.addEventListener('contextmenu', this.handleRightClick);
      document.addEventListener('scroll', this.handleScroll, { passive: true });
      // 커서를 완전히 숨기고 중요도를 높임
      document.body.style.setProperty('cursor', 'none', 'important');
      document.documentElement.style.setProperty('cursor', 'none', 'important');
      
      // 돋보기용 body 복사본 생성
      this.createMagnifierContent();
      
      // DOM 변화 감지 시작 (모달 대응)
      this.setupDOMObserver();
    },
    
    stopMagnifier() {
      document.removeEventListener('mousemove', this.handleMouseMove);
      document.removeEventListener('contextmenu', this.handleRightClick);
      document.removeEventListener('scroll', this.handleScroll);
      // 커서 복원
      document.body.style.removeProperty('cursor');
      document.documentElement.style.removeProperty('cursor');
      
      // DOM 감지 중단
      if (this.domObserver) {
        this.domObserver.disconnect();
        this.domObserver = null;
      }
      
      // 타이머 정리
      if (this.updateTimer) {
        clearTimeout(this.updateTimer);
        this.updateTimer = null;
      }
    },

    handleRightClick(event) {
      // 오른쪽 클릭으로 돋보기 해제
      event.preventDefault();
      event.stopPropagation();
      
      if (this.isActive) {
        this.isActive = false;
        this.stopMagnifier();
        this.hideHelpPopup();
        // localStorage 제거 불필요
      }
    },
    
    handleMouseMove(event) {
      // 실시간으로 마우스 위치 업데이트
      this.mouseX = event.clientX;
      this.mouseY = event.clientY;
    },

    handleScroll() {
      // 스크롤할 때 돋보기 내용 실시간 업데이트
      // 이미 contentStyle에서 스크롤 위치를 반영하므로 
      // 별도 처리는 필요 없지만, 필요시 여기서 추가 로직 가능
    },
    
    createMagnifierContent() {
      this.$nextTick(() => {
        if (!this.$refs.magnifierLens) return;
        
        const content = this.$refs.magnifierLens.querySelector('.magnifier-content');
        if (!content) return;
        
        // 현재 페이지의 HTML을 그대로 복사
        const htmlClone = document.documentElement.cloneNode(true);
        
        // 돋보기 관련 요소들 제거
        const magnifierElements = htmlClone.querySelectorAll('.magnifier-toggle, .magnifier-lens, .help-popup');
        magnifierElements.forEach(el => el.remove());
        
        // 모든 스크립트 제거 (이벤트 중복 방지)
        const scripts = htmlClone.querySelectorAll('script');
        scripts.forEach(script => script.remove());
        
        // 링크와 버튼의 기본 동작 방지
        const interactiveElements = htmlClone.querySelectorAll('a, button, input, select, textarea');
        interactiveElements.forEach(el => {
          el.style.pointerEvents = 'none';
        });
        
        // 페이지의 전체 크기 계산 (스크롤 영역 포함)
        const documentHeight = Math.max(
          document.body.scrollHeight,
          document.body.offsetHeight,
          document.documentElement.clientHeight,
          document.documentElement.scrollHeight,
          document.documentElement.offsetHeight
        );
        
        const documentWidth = Math.max(
          document.body.scrollWidth,
          document.body.offsetWidth,
          document.documentElement.clientWidth,
          document.documentElement.scrollWidth,
          document.documentElement.offsetWidth
        );
        
        // 복사된 콘텐츠의 크기를 전체 문서 크기로 설정
        const clonedHtml = htmlClone;
        const clonedBody = htmlClone.querySelector('body');
        
        if (clonedHtml) {
          clonedHtml.style.width = documentWidth + 'px';
          clonedHtml.style.height = documentHeight + 'px';
          clonedHtml.style.overflow = 'visible';
        }
        
        if (clonedBody) {
          clonedBody.style.width = documentWidth + 'px';
          clonedBody.style.height = documentHeight + 'px';
          clonedBody.style.overflow = 'visible';
          clonedBody.style.position = 'relative';
        }
        
        // 내용 교체
        content.innerHTML = clonedHtml.outerHTML;
        
        // 돋보기 콘텐츠 컨테이너의 크기도 조정
        content.style.width = documentWidth + 'px';
        content.style.height = documentHeight + 'px';
      });
    },

    // 모달이 열릴 때 돋보기 내용 업데이트
    updateMagnifierForModal() {
      if (this.isActive) {
        // 잠깐 기다린 후 모달이 완전히 렌더링되면 업데이트
        setTimeout(() => {
          this.createMagnifierContent();
        }, 100);
      }
    },

    // DOM 변화 감지해서 돋보기 내용 업데이트 (모달 대응)
    setupDOMObserver() {
      if (this.domObserver) {
        this.domObserver.disconnect();
      }

      this.domObserver = new MutationObserver((mutations) => {
        let shouldUpdate = false;
        
        mutations.forEach((mutation) => {
          // 모달 관련 클래스나 요소가 추가/제거될 때만 업데이트
          if (mutation.type === 'childList') {
            const addedNodes = Array.from(mutation.addedNodes);
            const removedNodes = Array.from(mutation.removedNodes);
            
            // 모달, 팝업, 오버레이 등이 감지되면 업데이트
            const modalKeywords = ['modal', 'popup', 'overlay', 'dialog', 'dropdown'];
            
            const hasModalChange = [...addedNodes, ...removedNodes].some(node => {
              if (node.nodeType === Node.ELEMENT_NODE) {
                const className = node.className || '';
                const id = node.id || '';
                return modalKeywords.some(keyword => 
                  className.toLowerCase().includes(keyword) || 
                  id.toLowerCase().includes(keyword)
                );
              }
              return false;
            });
            
            if (hasModalChange) {
              shouldUpdate = true;
            }
          }
        });
        
        if (shouldUpdate && this.isActive) {
          // 디바운싱으로 너무 자주 업데이트되는 것 방지
          clearTimeout(this.updateTimer);
          this.updateTimer = setTimeout(() => {
            this.createMagnifierContent();
          }, 150);
        }
      });

      // body의 변화를 감지
      this.domObserver.observe(document.body, {
        childList: true,
        subtree: true,
        attributes: false
      });
    },
    
    initializeMagnifier() {
      // 페이지 로드 시에는 돋보기를 자동으로 켜지 않음 (노인 사용성 개선)
      // 사용자가 명시적으로 각 페이지에서 활성화해야 함
    }
  },
  
  mounted() {
    this.initializeMagnifier();
  },
  
  beforeUnmount() {
    this.stopMagnifier();
    this.hideHelpPopup();
    
    // DOM 옵저버 정리
    if (this.domObserver) {
      this.domObserver.disconnect();
    }
    
    // 타이머 정리
    if (this.updateTimer) {
      clearTimeout(this.updateTimer);
    }
  },
  
  watch: {
    '$route'() {
      // 페이지 전환 시 돋보기 자동 해제 (노인 사용성 개선)
      if (this.isActive) {
        this.isActive = false;
        this.stopMagnifier();
        this.hideHelpPopup();
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
  position: absolute;
  overflow: hidden;
  pointer-events: none;
  /* 크기는 JavaScript에서 동적으로 설정 */
  min-width: 100vw;
  min-height: 100vh;
}

/* 돋보기 내부 요소들의 상호작용 차단 */
.magnifier-content * {
  pointer-events: none !important;
  user-select: none !important;
  cursor: none !important;
}

/* 십자선 (마우스 위치 표시) */
.magnifier-crosshair {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 10001;
}

.crosshair-horizontal,
.crosshair-vertical {
  position: absolute;
  background: rgba(255, 0, 0, 0.7);
}

.crosshair-horizontal {
  width: 20px;
  height: 1px;
  top: 0;
  left: -10px;
}

.crosshair-vertical {
  width: 1px;
  height: 20px;
  left: 0;
  top: -10px;
}

/* 도움말 팝업 */
.help-popup {
  position: fixed;
  z-index: 10001;
  animation: fadeInUp 0.3s ease-out;
}

.popup-content {
  background: rgba(0, 0, 0, 0.85);
  color: white;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  white-space: nowrap;
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.popup-arrow {
  position: absolute;
  right: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 0;
  border-left: 8px solid rgba(0, 0, 0, 0.85);
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
}

@keyframes fadeInUp {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 0 0 rgba(255, 107, 107, 0.7);
  }
  70% {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 0 10px rgba(255, 107, 107, 0);
  }
  100% {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 0 0 rgba(255, 107, 107, 0);
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
  
  .help-popup {
    right: 70px;
    bottom: 65px;
  }
  
  .popup-content {
    font-size: 12px;
    padding: 10px 14px;
  }
}

.magnifier-toggle:focus {
  outline: 3px solid #FFD700;
  outline-offset: 3px;
}

/* 전역 커서 숨김 (돋보기 활성화 시) */
.magnifier-active * {
  cursor: none !important;
}
</style>