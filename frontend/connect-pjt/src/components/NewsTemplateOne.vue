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
      
      <!-- 중앙 이미지 -->
      <div class="center-image">
        <img :src="imageUrl" alt="경로당 활동 이미지" />
        <p class="caption">도전 체험을 하는 모습</p>
      </div>
      
      <!-- 메인 기사 내용 -->
      <div class="main-content">
        <h2>{{ headline }}</h2>
        <p class="content">{{ content }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import NewsHeader from '@/components/NewsHeader.vue'

const props = defineProps({
  seniorCenterName: String,
  month: String,
  ranking: Number,
  headline: String,
  content: String,
  imageUrl: String
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

.center-image {
  text-align: center;
  margin: 10mm 0;
}

.center-image img {
  width: 70mm;
  height: 70mm;
  object-fit: cover;
  border: 1px solid #ccc;
}

.caption {
  font-size: 11px;
  color: #777;
  margin-top: 3px;
}

.main-content {
  margin-top: 15mm;
}

.main-content h2 {
  font-size: 20px;
  margin-bottom: 8mm;
  font-weight: bold;
}

.content {
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 6mm;
  word-wrap: break-word;
  overflow-wrap: break-word;
}
</style>