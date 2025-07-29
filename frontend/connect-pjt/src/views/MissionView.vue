<template>
  <div>
    <h1 class="header">다양한 도전과제를 수행해보세요</h1>
  </div>
     
  <div class="progress-container">
    <h3>진행률</h3>
    <div class="progress-bar">
      <div class="inner-bar" :style="{width: percent + '%'}"></div>
    </div>
    <h3>{{percent}}%</h3>
  </div>
   
  <div class="message-box">
    <p>{{ currentMessage }}</p>
  </div>
   
  <div class="challenge-container">
    <div
        class="single-challenge"
        v-for="(challenge, index) in challenges"
        :key="index"
        @click="openModal(challenge)" 
        >
      <div class="challenge-content">
        <div class="text-content">
          <h2>{{ challenge.title }}</h2>
          <p>{{ challenge.description }}</p>
        </div>
        <button class="mission-complete-btn">미완료</button>
      </div>
    </div>
  </div>

  <!-- 상세정보 모달  -->
   <!-- 모달 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
        <h2>{{ selectedChallenge.title }}</h2>
        <p class="modal-description">{{ selectedChallenge.description }}</p>
        <div class="modal-place">
        <span class="icon">📍</span>
        장소: {{ selectedChallenge.place || '장소 정보 없음' }}
        </div>
        <button class="modal-button" @click="closeModal">도전 인증하기</button>
    </div>
    </div>


  <div class="create-mission">
    <button class="mission-btn" @click="moveToCreate()">미션 생성하기</button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, defineProps } from 'vue'
import { useRouter } from 'vue-router'
const router=useRouter()

const moveToCreate = () => {
  router.push({ name: 'missionCreate' }) 
}


const props = defineProps({
  month: {
    type: Number,
    default: new Date().getMonth() + 1 
  }
})

const count = ref(0)
const percent = computed(() => Math.round((count.value / 4) * 100))
const progressMessages = ref([])
const currentMessage = ref('')
const monthlyMissions = ref({})
const challenges = ref([])

// 진행률 메시지 JSON에서 데이터 불러오기
const loadMessages = async () => {
  try {
    const response = await fetch('/progress_sentence.json')
    progressMessages.value = await response.json()
    updateMessage()
  } catch (error) {
    currentMessage.value = 'JSON 파일을 불러올 수 없습니다.'
  }
}

// 메시지 랜덤으로 업데이트
const updateMessage = () => {
  const messages = progressMessages.value.filter(item => item.percent === `${percent.value}%`)
  if (messages.length > 0) {
    currentMessage.value = messages[Math.floor(Math.random() * messages.length)].message
  }
}

// 월별 미션을 JSON에서 데이터 불러오기
const loadMonthlyMissions = async () => {
  try {
    const response = await fetch('/public/monthly_mission.json')
    monthlyMissions.value = await response.json()
    updateChallenges()
  } catch (error) {
    console.error('월별 미션 JSON 파일을 불러올 수 없습니다:', error)
    challenges.value = Array(4).fill({ title: '미션을 불러올 수 없습니다', description: '' })
  }
}

// 제공 미션을 2개 선택
const updateChallenges = () => {
  const monthMissions = monthlyMissions.value[props.month.toString()]

  if (monthMissions?.length > 0) {
    // 랜덤으로 2개 뽑기 (랜덤 시드 사용)
    const getSeededIndex = (seed) => Math.floor((Math.sin(seed) * 10000 - Math.floor(Math.sin(seed) * 10000)) * monthMissions.length)
    
    const index1 = getSeededIndex(props.month * 31 + 17)
    const index2 = getSeededIndex(props.month * 37 + 23) === index1 ? (index1 + 1) % monthMissions.length : getSeededIndex(props.month * 37 + 23)
    
    challenges.value = [
      monthMissions[index1], 
      monthMissions[index2],
      { title: '미션을 생성해주세요', description: '' },
      { title: '미션을 생성해주세요', description: '' }
    ]
  } else {
    challenges.value = [
      { title: '준비 중입니다.', description: '' },
      { title: '준비 중입니다.', description: '' },
      { title: '', description: '' },
      { title: '', description: '' }
    ]
  }
}
//상세정보 모달 관련
const showModal = ref(false)
const selectedChallenge = ref({ title: '', description: '', place: '' })

const openModal = (challenge) => {
  selectedChallenge.value = challenge
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

// percent가 변경되면 자동으로 메시지 업데이트
watch(percent, updateMessage)

// month가 변경되면 챌린지 업데이트
watch(() => props.month, updateChallenges)

// 컴포넌트가 마운트될 때 JSON 파일들 자동 로드
onMounted(() => {
  loadMessages()
  loadMonthlyMissions()
})
</script>

<style scoped>
.header { 
  text-align: center;
  margin: 45px auto 20px;
}

.progress-container {
  display: flex; 
  align-items: center; 
  max-width: 800px; 
  width: 90%;
  margin: 20px auto; 
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
  margin: 10px auto;
  max-width: 800px; 
  width: 90%; 
  padding: 5px; 
  background: #EBF3FC; 
  border-radius: 30px;
  font-size: 20px; 
}

.challenge-container {
  display: flex; 
  justify-content: space-between; 
  align-items: stretch;
  max-width: 1200px; 
  width: 90%; 
  margin: 20px auto; 
  gap: 20px;
}

.single-challenge {
  flex: 1;
  color: black;
  font-weight: bold;
  height: 240px; /*  박스 높이 줄임 */
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  position: relative;
  padding-bottom: 50px; /* 버튼을 위한 여유 공간 확보 */
}

.challenge-content { 
  position: relative; /* 버튼 절대 위치 기준 설정 */
  padding: 20px;
  padding-bottom: 60px; /* 버튼 공간 확보 */
  display: flex;
  flex-direction: column;
  height: 100%;
}

.text-content {
  flex: 1;
}

.challenge-content h2 { 
  margin: 13px 5px 13px 5px; 
  font-size: 22px; 
}

.challenge-content p { 
  margin: 13px 5px 13px 5px; 
  font-size: 20px; 
  font-weight: normal; 
  line-height: 1.4; 
}

.single-challenge:nth-child(1) { background: #FFBF8F; }
.single-challenge:nth-child(1):hover { background: #FFD4B3; }
.single-challenge:nth-child(2) { background: #97B9FF; }
.single-challenge:nth-child(2):hover { background: #B3D1FF; }
.single-challenge:nth-child(3) { background: #ABBAF9; }
.single-challenge:nth-child(3):hover { background: #C4D0FB; }
.single-challenge:nth-child(4) { background: #F1C399; }
.single-challenge:nth-child(4):hover { background: #F5D6B8; }

.create-mission { 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  width: 100%; 
}

.mission-btn {
  margin: 10px; 
  background-color: #3074FF; 
  font-weight: bold; 
  color: white;
  width: 320px; 
  height: 65px; 
  border: none; 
  outline: none; 
  cursor: pointer;
  font-size: 16px; 
  border-radius: 15px; 
}

.mission-btn:hover { 
  background-color: #a2b7e3; 
}


.mission-complete-btn {
  position: absolute;
  bottom: 50px; /*  버튼을 위로 올림 */
  left: 50%;
  transform: translateX(-50%);
  background-color: #FF8120;
  font-weight: bold;
  color: white;
  width: 120px;
  height: 40px;
  border: none;
  outline: none;
  cursor: pointer;
  font-size: 16px;
  border-radius: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.4);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 30px 40px;
  width: 90%;
  max-width: 480px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  text-align: center;
}

.modal-content h2 {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 15px;
}

.modal-description {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.modal-place {
  font-size: 15px;
  color: #444;
  margin-bottom: 25px;
}

.modal-button {
  background-color: #8C5EFF;
  color: white;
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  transition: background 0.3s;
}

.modal-button:hover {
  background-color: #6e49d8;
}

</style>