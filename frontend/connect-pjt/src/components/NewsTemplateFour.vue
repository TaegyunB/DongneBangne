<template>
  <div>
    <!-- PDF로 저장할 영역 -->
    <div class="a4-container" ref="pdfTarget">
      <!-- 헤더 영역 -->
      <div class="header">
        <div class="title-wrapper">
            <h1>{{ seniorCenterName }} {{ month }}월 AI 신문</h1>
        </div>
        <span class="date">2025.{{ month }}.31</span>
      </div>
       
      <!-- 랭킹 정보 -->
      <div class="ranking">
        <h3>이번달 랭킹 {{ ranking }}위</h3>
      </div>
       
      <!-- 첫 번째 기사 (텍스트 왼쪽, 이미지 오른쪽) -->
      <div class="article-section main-article">
        <div class="text-wrapper main-text">
            <h2>{{ headline1 }}</h2>
            <p class="content">{{ content1 }}</p>
        </div>
        <div class="image-wrapper main-image">
            <img :src="imageUrl1" alt="도전 이미지1" />
            <p class="caption">도전 체험을 하는 모습</p>
        </div>
      </div>
      
      <hr>
      
      <!-- 하단 세 개 기사 나란히 배치 -->
      <div class="bottom-articles">
        <!-- 두 번째 기사 -->
        <div class="article-column">
          <div class="image-wrapper small-image">
            <img :src="imageUrl2" alt="도전 이미지2" />
            <p class="caption">도전 체험을 하는 모습</p>
          </div>
          <h2>{{ headline2 }}</h2>
          <p class="content">{{ content2 }}</p>
        </div>
        
        <!-- 세로 구분선 -->
        <div class="vertical-divider"></div>
        
        <!-- 세 번째 기사 -->
        <div class="article-column">
          <div class="image-wrapper small-image">
            <img :src="imageUrl3" alt="도전 이미지3" />
            <p class="caption">도전 체험을 하는 모습</p>
          </div>
          <h2>{{ headline3 }}</h2>
          <p class="content">{{ content3 }}</p>
        </div>
        
        <!-- 세로 구분선 -->
        <div class="vertical-divider"></div>
        
        <!-- 네 번째 기사 -->
        <div class="article-column">
          <div class="image-wrapper small-image">
            <img :src="imageUrl4" alt="도전 이미지4" />
            <p class="caption">도전 체험을 하는 모습</p>
          </div>
          <h2>{{ headline4 }}</h2>
          <p class="content">{{ content4 }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, ref, defineExpose } from 'vue'

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
  imageUrl3: String,
  // 기사4
  headline4: String,
  content4: String,
  imageUrl4: String
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

.header {
  position: relative;
  border-top: 1px solid black;
  border-bottom: 1px solid black;
  padding: 15px 0;
  margin-bottom: 5mm;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-wrapper {
  text-align: center;
  flex: 1;
}

.title-wrapper h1 {
  font-size: 22px;
  margin: 0;
  font-weight: bold;
}

.date {
  position: absolute;
  right: 0;
  top: 15px;
  font-size: 13px;
  color: #555;
}

.ranking {
  text-align: center;
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 6mm;
}

/* 메인 기사 스타일 */
.main-article {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6mm;
  gap: 8mm;
}

.main-text {
  width: 55%;
}

.main-image {
  width: 40%;
  text-align: center;
}

.main-image img {
  width: 100%;
  height: auto;
  max-height: 50mm;
  object-fit: cover;
  border: 1px solid #ccc;
}

hr {
  border: none;
  border-top: 1px solid #ccc;
  margin: 6mm 0;
}

/* 하단 세 개 기사 스타일 */
.bottom-articles {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 4mm;
  margin-top: 6mm;
}

.article-column {
  width: 30%;
  text-align: center;
}

.article-column h2 {
  font-size: 14px;
  font-weight: bold;
  margin: 3mm 0 2mm 0;
  line-height: 1.2;
}

.article-column .content {
  font-size: 14px;
  line-height: 1.3;
  word-wrap: break-word;
  text-align: left;
}

/* 세로 구분선 */
.vertical-divider {
  width: 1px;
  background-color: #ccc;
  align-self: stretch;
  margin: 0 1mm;
}

.small-image {
  text-align: center;
  margin-bottom: 2mm;
}

.small-image img {
  width: 100%;
  height: auto;
  max-height: 35mm;
  object-fit: cover;
  border: 1px solid #ccc;
}

.caption {
  font-size: 9px;
  color: #777;
  margin-top: 2px;
}

/* 공통 텍스트 스타일 */
.text-wrapper h2 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 6mm;
  line-height: 1.3;
}

.content {
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
}
</style>