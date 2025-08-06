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

      <!-- 첫 번째 기사 (이미지 왼쪽, 텍스트 오른쪽) -->
      <div class="article-section reverse">
        <div class="image-wrapper">
            <img :src="imageUrl1" alt="도전 이미지1" />
            <p class="caption">도전 체험을 하는 모습</p>
        </div>
        <div class="text-wrapper">
            <h2>{{ headline1 }}</h2>
            <p class="content">{{ content1 }}</p>
        </div>
      </div>
      <hr>
      <!-- 두 번째 기사 (텍스트 왼쪽, 이미지 오른쪽) -->
      <div class="article-section">
        <div class="text-wrapper">
            <h2>{{ headline2 }}</h2>
            <p class="content">{{ content2 }}</p>
        </div>
        <div class="image-wrapper">
            <img :src="imageUrl2" alt="도전 이미지2" />
            <p class="caption">도전 체험을 하는 모습</p>
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
  // 기사1
  headline1: String,
  content1: String,
  imageUrl1: String,
  // 기사2
  headline2: String,
  content2: String,
  imageUrl2: String
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
  top: 0;
  font-size: 13px;
  color: #555;
}


.ranking {
  text-align: center;
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 10mm;
}

.article-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12mm;
  margin-top: 12mm;
  gap: 10mm;
}

.article-section.reverse {
  flex-direction: row-reverse;
}

.image-wrapper {
  width: 45%;
  text-align: center;
}

.image-wrapper img {
  width: 100%;
  height: auto;
  max-height: 80mm;
  object-fit: cover;
  border: 1px solid #ccc;
}

.caption {
  font-size: 11px;
  color: #777;
  margin-top: 4px;
}

.text-wrapper {
  width: 50%;
}

.text-wrapper h2 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 6mm;
}

.content {
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
}
</style>
