<template>
  <div>
    <!-- 이 부분만 PDF로 저장 -->
    <div class="a4-container" ref="pdfTarget">
      <!-- 내용은 그대로 -->
      <div class="header">
        <div class="title-wrapper">
            <h1>{{ seniorCenterName }} {{ month }}월 AI 신문</h1>
        </div>
        <span class="date">2025.{{ month }}.31</span>
      </div>
      <div class="ranking">
        <h3>이번달 랭킹 {{ ranking }}위</h3>
      </div>
      <div class="image-wrapper">
        <img :src="imageUrl" alt="경로당 활동 이미지" />
        <p class="caption">도전 체험을 하는 모습</p>
      </div>
      <div class="headline-section">
        <h2>AI 헤드라인 :AI 헤드라인 :AI 헤드라인 :{{ headline }}</h2>
        <p class="content">{{ content }}</p>
      </div>
    </div>

  </div>
</template>

<script setup>
import html2pdf from 'html2pdf.js'
import { defineProps, ref, defineExpose  } from 'vue'

const props = defineProps({
  seniorCenterName: String,
  month: String,
  ranking: Number,
  headline: String,
  content: String,
  imageUrl: String
})

const pdfTarget = ref(null)
defineExpose({ pdfTarget })  // View에서 ref 접근 가능하게
</script>

<style scoped>
.a4-container {
  width: 190mm; /* 크기를 좀 더 키움 */
  height: 270mm; /* 고정 높이로 설정 */
  max-width: 190mm;
  margin: 0 auto;
  border: 2px solid black; /* 테두리 두께 줄임 */
  padding: 12mm; /* 패딩 줄임 */
  box-sizing: border-box;
  font-family: 'Noto Sans KR', sans-serif;
  background-color: white;
  color: #000;
  page-break-inside: avoid;
  page-break-after: avoid;
  page-break-before: avoid;
  overflow: hidden;
  position: relative;
}

.header {
  position: relative;
  border-top: 1px solid black;
  border-bottom: 1px solid black;
  padding-bottom: 8px;
  margin-bottom: 10mm;
  margin-top: 10mm;
  height: 30px; /* 높이 고정 필요 시 조정 */
}

.title-wrapper {
  text-align: center;
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
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
  margin: 10mm 0; /* 마진 줄임 */
  font-weight: bold;
  font-size: 16px; /* 폰트 크기 줄임 */
}

.ranking h3 {
  margin: 0;
}

.image-wrapper {
  text-align: center;
  margin: 10mm 0; /* 마진 줄임 */
}

.image-wrapper img {
  width: 70mm; /* 이미지 크기 줄임 */
  height: 70mm;
  object-fit: cover;
  border: 1px solid #ccc;
}

.caption {
  font-size: 11px;
  color: #777;
  margin-top: 3px;
}

.headline-section {
  margin-top: 15mm; /* 마진 줄임 */
}

.headline-section h2 {
  font-size: 20px; /* 폰트 크기 줄임 */
  margin-bottom: 8mm;
}
.headline-section p {
  font-size: 16px; /* 폰트 크기 줄임 */
  margin-bottom: 8mm;
}

.content {
  font-size: 14px; /* 폰트 크기 줄임 */
  line-height: 1.5; /* 라인 높이 줄임 */
  margin-bottom: 6mm;
  word-wrap: break-word;
  overflow-wrap: break-word;
}


</style>