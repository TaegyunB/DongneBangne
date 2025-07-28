<template>     
    <div class="header">         
        <h1>다양한 도전과제를 수행해보세요</h1>         
        <p>{{percent}}%</p>     
    </div>     
    <div class="progress-area">
        <div class="progress-container">
            <span class="progress-label">진행률</span>
            <div class="progressBar">             
                <div id="bar" class="innerbar" :style="{width: percent + '%'}"></div>         
            </div>
            <span class="progress-percent">{{percent}}%</span>
        </div>     
    </div>

    <!-- 미션 코멘트 부분 -->
    <div class="mission-comment">
        <p v-if="currentMessage">{{ currentMessage }}</p>
        <p v-else>메시지를 불러오는 중...</p>
    </div>
    
    <!-- 테스트용 버튼 -->
    <div class="test-buttons">
        <button @click="count = 0">0/4</button>
        <button @click="count = 1">1/4</button>
        <button @click="count = 2">2/4</button>
        <button @click="count = 3">3/4</button>
        <button @click="count = 4">4/4</button>
    </div>
</template>

<script setup> 
import { ref, computed, onMounted, watch } from 'vue' 

// 미션 수행 정도
const count = ref(0) 
const percent = computed(() => {     
    return Math.round((count.value / 4) * 100)
})

// JSON 데이터를 저장할 변수
const progressMessages = ref([])
const currentMessage = ref('')

// JSON 파일에서 데이터 불러오기
const loadProgressMessages = async () => {
    try {
        const response = await fetch('/progress_sentence.json')
        const data = await response.json()
        progressMessages.value = data
        updateMessage() // 초기 메시지 설정
    } catch (error) {
        console.error('JSON 파일 로드 실패:', error)
        currentMessage.value = 'JSON 파일을 불러올 수 없습니다.'
    }
}

// 현재 퍼센트에 맞는 메시지 업데이트
const updateMessage = () => {
    if (progressMessages.value.length === 0) return
    
    const currentPercent = `${percent.value}%`
    
    const matchingMessages = progressMessages.value.filter(
        item => item.percent === currentPercent
    )
    
    // 랜덤하게 선택
    const randomIndex = Math.floor(Math.random() * matchingMessages.length)
    currentMessage.value = matchingMessages[randomIndex].message
}

// percent가 변경될 때마다 자동으로 메시지 업데이트
watch(percent, () => {
    if (progressMessages.value.length > 0) {
        updateMessage()
    }
})

// 컴포넌트가 마운트될 때 JSON 파일 로드
onMounted(() => {
    loadProgressMessages()
})
</script>

<style scoped> 
.header{     
    text-align: center; 
} 

.progress-container {
    display: flex;
    align-items: center;
    max-width: 800px;
    width: 90%;
    margin: 10px auto;
    margin-top: 100px;
    gap: 15px;
}

.progress-label {
    font-weight: bold;
    color: #333;
    white-space: nowrap;
}

.progress-percent {
    font-weight: bold;
    color: #333;
    min-width: 40px;
    text-align: right;
}

.progressBar{   
    flex: 1;
    height: 15px;   
    border-radius: 10px;   
    background: linear-gradient(#E6E6E6, #E6E6E6); 
}

.innerbar{   
    height: 100%;   
    border-radius: 10px;   
    background: linear-gradient(#107C10, #107C10);
}

.mission-comment {
    color: #115EA3;
    font-weight: bold;
    text-align: center;
    margin: 30px auto;
    max-width: 800px;
    width: 90%;
    padding: 20px;
    background-color: #EBF3FC;
    border-radius: 30px;
}

.mission-comment p {
    margin: 0;
    font-size: 16px;
    color: inherit; 
    line-height: 1.5;
}

/* 테스트용 버튼 스타일 (실제 사용시에는 제거) */
.test-buttons {
    text-align: center;
    margin-top: 20px;
}

.test-buttons button {
    margin: 0 5px;
    padding: 5px 10px;
    background: #107C10;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

.test-buttons button:hover {
    background: #0d5c0d;
}
</style>