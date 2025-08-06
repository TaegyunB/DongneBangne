<template>
  <div>
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
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  seniorCenterName: String,
  month: String,
  ranking: Number
})
</script>

<style scoped>
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
  margin-bottom: 8mm;
}
</style>