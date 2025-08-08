<template>
  <div>
    <!-- PDF로 저장할 영역 -->
    <div class="a4-container" ref="pdfTarget">
      <!-- 공통 헤더 컴포넌트 -->
      <NewsHeader 
        :seniorCenterName="seniorCenterName" 
        :month="month" 
        :ranking="ranking" 
      />
      
      <!-- 메인 기사 (텍스트 왼쪽, 이미지 오른쪽) -->
      <div class="main-article">
        <div class="main-text">
          <h2>{{ headline1 }}</h2>
          <p class="content">{{ content1 }}</p>
        </div>
        <div class="main-image">
          <img :src="imageUrl1" alt="도전 이미지" />
          <p class="caption">도전 체험을 하는 모습</p>
        </div>
      </div>
      
      <hr>
      
      <!-- 하단 두 기사 나란히 배치 -->
      <div class="bottom-articles">
        <div class="bottom-article">
          <h3>{{ headline2 }}</h3>
          <div class="bottom-image">
            <img :src="imageUrl2" alt="도전 이미지" />
            <p class="caption">도전 체험을 하는 모습</p>
          </div>
          <p class="bottom-content">{{ content2 }}</p>
        </div>
        
        <!-- 세로 구분선 -->
        <div class="vertical-divider"></div>
        
        <div class="bottom-article">
          <h3>{{ headline3 }}</h3>
          <div class="bottom-image">
            <img :src="imageUrl3" alt="도전 이미지" />
            <p class="caption">도전 체험을 하는 모습</p>
          </div>
          <p class="bottom-content">{{ content3 }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, ref, defineExpose } from 'vue'
import NewsHeader from '@/components/NewsHeader.vue'

const props = defineProps({
  seniorCenterName: String,
  month: String,
  ranking: Number,
  // 기사1 (메인)
  headline1: String,
  content1: String,
  imageUrl1: String,
  // 기사2
  headline2: String,
  content2: String,
  imageUrl2: String,
  // 기사3
  headline3: String,
  content3: String,
  imageUrl3: String
})

const pdfTarget = ref(null)
defineExpose({ pdfTarget })
</script>

<style scoped>
.a4-container {
  width: 190mm;
  height: 270mm;
  margin: 0 auto;
  border: 2px solid black;
  padding: 12mm;
  box-sizing: border-box;
  font-family: 'Noto Sans KR', sans-serif;
  background-color: white;
  color: #000;
  overflow: hidden;
  position: relative;
}

/* 메인 기사 스타일 */
.main-article {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8mm;
  margin-bottom: 8mm;
}

.main-text {
  width: 55%;
}

.main-text h2 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 6mm;
  line-height: 1.3;
  color: #000;
}

.content {
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 4mm;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.main-image {
  width: 40%;
  text-align: center;
}

.main-image img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border: 1px solid #ccc;
}

hr {
  border: none;
  border-top: 1px solid #ccc;
  margin: 6mm 0;
}

/* 하단 기사들 */
.bottom-articles {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 6mm;
  margin-top: 6mm;
}

.bottom-article {
  width: 45%;
  margin-bottom: 0;
}

.bottom-article h3 {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 4mm;
  line-height: 1.2;
  color: #000;
}

.bottom-content {
  font-size: 12px;
  line-height: 1.3;
  margin-bottom: 4mm;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.bottom-image {
  text-align: center;
  margin-top: 3mm;
}

.bottom-image img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border: 1px solid #ccc;
}

/* 세로 구분선 */
.vertical-divider {
  width: 1px;
  background-color: #ccc;
  align-self: stretch;
  margin: 0 2mm;
}

.caption {
  font-size: 10px;
  color: #777;
  margin-top: 3px;
}
</style>