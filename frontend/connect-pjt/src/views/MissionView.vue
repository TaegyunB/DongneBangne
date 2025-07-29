# 박스랑 hover까지 완료 
<template>
  <div class="header">
    <h1>다양한 도전과제를 수행해보세요</h1>
  </div>
  
  <div class="progress-container">
    <span>진행률</span>
    <div class="progress-bar">
      <div class="inner-bar" :style="{width: percent + '%'}"></div>
    </div>
    <span>{{percent}}%</span>
  </div>

  <div class="message-box">
    <p>{{ currentMessage }}</p>
  </div>

  <div class="challenge-container">
    <div class="single-challenge">
        <h3>{{title}}</h3>
        <p>{{ description }}</p>
    </div>
    <div class="single-challenge">
        <h3>{{title}}</h3>
        <p>{{ description }}</p>
    </div>
    <div class="single-challenge">
        <h3>{{title}}</h3>
        <p>{{ description }}</p>
    </div>
    <div class="single-challenge">
        <h3>{{title}}</h3>
        <p>{{ description }}</p>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

const count = ref(0)
const percent = computed(() => Math.round((count.value / 4) * 100))
const progressMessages = ref([]) // 해당 퍼센트의 모든 메시지
const currentMessage = ref('') //해당 퍼센트의 한 메시지

//JSON에서 데이터 불러오기 
const loadMessages = async () => { //비동기 처리 
  try {
    const response = await fetch('/progress_sentence.json')
    progressMessages.value = await response.json()
    updateMessage()
  } catch (error) {
    currentMessage.value = 'JSON 파일을 불러올 수 없습니다.'
  }
}

//메시지 랜덤으로 업데이트
const updateMessage = () => {
  const messages = progressMessages.value.filter(item => item.percent === `${percent.value}%`)
  if (messages.length > 0) {
    currentMessage.value = messages[Math.floor(Math.random() * messages.length)].message
  }
}

//percent가 변경되면 자동으로 메시지 업데이트 
watch(percent, updateMessage)

//컴포넌트가 마운트될 때 JSON 파일 자동 로드 
onMounted(loadMessages)
</script>

<style scoped>
.header { 
    text-align: center; 
}

.progress-container {
  display: flex;
  align-items: center;
  max-width: 800px;
  width: 90%;
  margin: 50px auto 50px;
  gap: 15px;
}

.progress-bar {
  flex: 1;
  height: 15px;
  border-radius: 10px;
  background: #E6E6E6;
}

.inner-bar {
  height: 100%;
  border-radius: 10px;
  background: #107C10;
}

.message-box {
  color: #115EA3;
  font-weight: bold;
  text-align: center;
  margin: 30px auto;
  max-width: 800px;
  width: 90%;
  padding: 10px;
  background: #EBF3FC;
  border-radius: 30px;
}

.challenge-container{
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 900px;
    width: 100%;
    margin: 20px auto;
    gap: 20px;
}
.single-challenge {
    flex: 1;
    color: black;
    font-weight: bold;
    height: 150px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.single-challenge:nth-child(1) {
    background: #FFBF8F;
}

.single-challenge:nth-child(1):hover {
    background: #FFD4B3;
}

.single-challenge:nth-child(2) {
    background: #97B9FF;
}

.single-challenge:nth-child(2):hover {
    background: #B3D1FF;
}

.single-challenge:nth-child(3) {
    background: #ABBAF9;
}

.single-challenge:nth-child(3):hover {
    background: #C4D0FB;
}

.single-challenge:nth-child(4) {
    background: #F1C399;
}

.single-challenge:nth-child(4):hover {
    background: #F5D6B8;
}
</style>