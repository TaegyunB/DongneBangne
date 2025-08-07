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
      <NewsArticle
        :headline="headline1"
        :content="content1"
        :imageUrl="imageUrl1"
        layout="horizontal"
        imagePosition="right"
        size="medium"
      />
      
      <hr>
      
      <!-- 하단 세 개 기사 나란히 배치 -->
      <div class="bottom-articles">
        <NewsArticle
          :headline="headline2"
          :content="content2"
          :imageUrl="imageUrl2"
          layout="vertical"
          imagePosition="top"
          size="small"
          class="bottom-article"
        />
        
        <!-- 세로 구분선 -->
        <div class="vertical-divider"></div>
        
        <NewsArticle
          :headline="headline3"
          :content="content3"
          :imageUrl="imageUrl3"
          layout="vertical"
          imagePosition="top"
          size="small"
          class="bottom-article"
        />
        
        <!-- 세로 구분선 -->
        <div class="vertical-divider"></div>
        
        <NewsArticle
          :headline="headline4"
          :content="content4"
          :imageUrl="imageUrl4"
          layout="vertical"
          imagePosition="top"
          size="small"
          class="bottom-article"
        />
      </div>
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

hr {
  border: none;
  border-top: 1px solid #ccc;
  margin: 6mm 0;
}

.bottom-articles {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 4mm;
  margin-top: 6mm;
}

.bottom-article {
  width: 30%;
  margin-bottom: 0;
}

/* 세로 구분선 */
.vertical-divider {
  width: 1px;
  background-color: #ccc;
  align-self: stretch;
  margin: 0 1mm;
}
</style>