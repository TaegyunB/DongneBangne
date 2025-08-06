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
      
      <!-- 첫 번째 기사 (이미지 왼쪽) -->
      <NewsArticle
        :headline="headline1"
        :content="content1"
        :imageUrl="imageUrl1"
        layout="horizontal"
        imagePosition="left"
        size="large"
      />
      
      <hr>
      
      <!-- 두 번째 기사 (이미지 오른쪽) -->
      <NewsArticle
        :headline="headline2"
        :content="content2"
        :imageUrl="imageUrl2"
        layout="horizontal"
        imagePosition="right"
        size="large"
      />
    </div>
  </div>
</template>

<script setup>
import { defineProps, ref, defineExpose } from 'vue'
import NewsHeader from '@/components/NewsHeader.vue'
import NewsArticle from '@/components/NewsArticle.vue'

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

hr {
  border: none;
  border-top: 1px solid #ccc;
  margin: 8mm 0;
}
</style>