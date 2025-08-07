<template>
  <div class="detail-container">
    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading">
      AI 신문을 생성하고 있습니다...
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="error" class="error">
      {{ error }}
      <button @click="$router.push('/news')" class="back-btn">목록으로 돌아가기</button>
    </div>

    <!-- 신문 템플릿 렌더링 -->
    <div v-else-if="newsData">
      <!-- Template 1: 도전과제 1개 -->
      <NewsTemplateOne 
        v-if="newsData.successChallengeCount === 1"
        ref="pdfComponent"
        v-bind="templateOneData"
      />

      <!-- Template 2: 도전과제 2개 -->
      <NewsTemplateTwo 
        v-else-if="newsData.successChallengeCount === 2"
        ref="pdfComponent"
        v-bind="templateTwoData"
      />

      <!-- Template 3: 도전과제 3개 -->
      <NewsTemplateThree 
        v-else-if="newsData.successChallengeCount === 3"
        ref="pdfComponent"
        v-bind="templateThreeData"
      />

      <!-- Template 4: 도전과제 4개 -->
      <NewsTemplateFour 
        v-else-if="newsData.successChallengeCount === 4"
        ref="pdfComponent"
        v-bind="templateFourData"
      />

      <!-- 도전과제가 0개이거나 4개 초과인 경우 -->
      <div v-else class="no-template">
        <p>이번달에 도전과제를 수행하지 않았군요. 다음달엔 도전해보세요!</p>
        <p>성공한 도전과제: {{ newsData.successChallengeCount }}개</p>
      </div>

      <!-- PDF 저장 버튼 -->
      <div v-if="newsData.successChallengeCount >= 1 && newsData.successChallengeCount <= 4" class="save-button">
        <button @click="saveAsPDF" :disabled="savingPdf">
          {{ savingPdf ? '📄 PDF 생성중...' : '📄 PDF로 저장하기' }}
        </button>
        <button @click="$router.push('/news')" class="back-btn">목록으로 돌아가기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import html2pdf from 'html2pdf.js'

// 컴포넌트 임포트
import NewsTemplateOne from '@/components/NewsTemplateOne.vue'
import NewsTemplateTwo from '@/components/NewsTemplateTwo.vue'
import NewsTemplateThree from '@/components/NewsTemplateThree.vue'
import NewsTemplateFour from '@/components/NewsTemplateFour.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref('')
const newsData = ref(null)
const pdfComponent = ref(null)
const savingPdf = ref(false)

// 백엔드에서 AI 신문 생성 요청
const generateAiNews = async (newsId) => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await axios.post('/api/v1/admin/ai-news', {
      newsId: newsId
    })
    
    newsData.value = response.data
    console.log('AI 신문 생성 완료:', response.data)
    
  } catch (err) {
    console.error('AI 신문 생성 실패:', err)
    error.value = 'AI 신문 생성에 실패했습니다. 다시 시도해주세요.'
    
    // 개발용 더미 데이터
    newsData.value = {
      id: 6,
      newsTitle: "강일리버파크2단지, 어르신들과 함께한 따뜻한 여름의 추억!",
      newsContent: null,
      year: 2025,
      month: 8,
      pdfUrl: null,
      seniorCenterName: "강일리버파크2단지",
      challenges: [
        {
          id: 1,
          challengeTitle: "백숙먹기",
          challengePlace: "경로당",
          description: "다같이 백숙먹기",
          year: 2025,
          month: 8,
          point: 300,
          challengeImage: "https://picsum.photos/300?random=1",
          imageDescription: "무더운 날씨에도 우리 다같이 산책했어요. 산책하면서 꽃도 보고 사람 구경도 했답니다.",
          aiDescription: "우리 경로당 어르신들이 함께 백숙을 즐겼습니다! 따뜻한 백숙을 나누며 소통하고, 무더운 날씨 속에서도 산책하며 꽃과 사람들을 구경했습니다. 이런 활동은 서로의 정을 나누고 건강에도 좋습니다. 앞으로도 즐거운 시간을 많이 만들어가요!",
          isSuccess: true,
          seniorCenterName: "강일리버파크2단지"
        },
        {
          id: 2,
          challengeTitle: "산책하기",
          challengePlace: "경로당",
          description: "다같이 산책하기",
          year: 2025,
          month: 8,
          point: 300,
          challengeImage: "https://picsum.photos/300?random=2",
          imageDescription: "몸보신하기 위해 우리 다같이 백숙먹었어요. 다같이 재료 사러 마트에도 가고 즐거운 시간을 보냈답니다.",
          aiDescription: "경로당 어르신들, 함께하는 산책과 백숙 파티! 어르신들이 경로당에서 함께 산책을 즐겼습니다. 건강을 위해 백숙도 함께 나누며, 마트에서 재료를 사는 즐거운 시간도 보냈습니다. 서로의 소중한 순간을 나누며, 따뜻한 정을 느끼는 값진 활동이었습니다.",
          isSuccess: true,
          seniorCenterName: "강일리버파크2단지"
        },
        {
          id: 3,
          challengeTitle: "책 읽기",
          challengePlace: "경로당",
          description: "더운데 다같이 모여서 책 읽기",
          year: 2025,
          month: 8,
          point: 300,
          challengeImage: "https://picsum.photos/300?random=3",
          imageDescription: "날씨도 더운데 우리 다같이 경로당에서 책을 읽었습니다. 책을 다 읽은 후에는 다같이 모여서 책 내용을 공유하였습니다.",
          aiDescription: "여름의 더위 속, 경로당 어르신들이 모여 책을 읽었습니다. 함께한 독서 시간 후, 서로의 이야기를 나누며 따뜻한 소통의 시간을 가졌습니다. 책 읽기는 지혜를 나누고, 정을 나누는 소중한 활동입니다.",
          isSuccess: true,
          seniorCenterName: "강일리버파크2단지"
        }
      ],
      successChallengeCount: 3,
      createdAt: "2025-08-06T09:26:39.1127011"
    }
    error.value = '' // 더미 데이터 사용시 에러 클리어
    
  } finally {
    loading.value = false
  }
}

// Template One 데이터 (도전과제 1개)
const templateOneData = computed(() => {
  if (!newsData.value || newsData.value.successChallengeCount !== 1) return {}
  
  const challenge = newsData.value.challenges[0]
  return {
    seniorCenterName: newsData.value.seniorCenterName,
    month: String(newsData.value.month).padStart(2, '0'),
    ranking: 1, // 실제로는 랭킹 정보가 있어야 함
    headline: challenge.challengeTitle,
    content: challenge.aiDescription,
    imageUrl: challenge.challengeImage
  }
})

// Template Two 데이터 (도전과제 2개)
const templateTwoData = computed(() => {
  if (!newsData.value || newsData.value.successChallengeCount !== 2) return {}
  
  const challenges = newsData.value.challenges.slice(0, 2)
  return {
    seniorCenterName: newsData.value.seniorCenterName,
    month: String(newsData.value.month).padStart(2, '0'),
    ranking: 1,
    headline1: challenges[0].challengeTitle,
    content1: challenges[0].aiDescription,
    imageUrl1: challenges[0].challengeImage,
    headline2: challenges[1].challengeTitle,
    content2: challenges[1].aiDescription,
    imageUrl2: challenges[1].challengeImage
  }
})

// Template Three 데이터 (도전과제 3개)
const templateThreeData = computed(() => {
  if (!newsData.value || newsData.value.successChallengeCount !== 3) return {}
  
  const challenges = newsData.value.challenges.slice(0, 3)
  return {
    seniorCenterName: newsData.value.seniorCenterName,
    month: String(newsData.value.month).padStart(2, '0'),
    ranking: 1,
    headline1: challenges[0].challengeTitle,
    content1: challenges[0].aiDescription,
    imageUrl1: challenges[0].challengeImage,
    headline2: challenges[1].challengeTitle,
    content2: challenges[1].aiDescription,
    imageUrl2: challenges[1].challengeImage,
    headline3: challenges[2].challengeTitle,
    content3: challenges[2].aiDescription,
    imageUrl3: challenges[2].challengeImage
  }
})

// Template Four 데이터 (도전과제 4개)
const templateFourData = computed(() => {
  if (!newsData.value || newsData.value.successChallengeCount !== 4) return {}
  
  const challenges = newsData.value.challenges.slice(0, 4)
  return {
    seniorCenterName: newsData.value.seniorCenterName,
    month: String(newsData.value.month).padStart(2, '0'),
    ranking: 1,
    headline1: challenges[0].challengeTitle,
    content1: challenges[0].aiDescription,
    imageUrl1: challenges[0].challengeImage,
    headline2: challenges[1].challengeTitle,
    content2: challenges[1].aiDescription,
    imageUrl2: challenges[1].challengeImage,
    headline3: challenges[2].challengeTitle,
    content3: challenges[2].aiDescription,
    imageUrl3: challenges[2].challengeImage,
    headline4: challenges[3].challengeTitle,
    content4: challenges[3].aiDescription,
    imageUrl4: challenges[3].challengeImage
  }
})

// PDF 저장 기능
const saveAsPDF = async () => {
  if (!pdfComponent.value) {
    alert("PDF 컴포넌트가 준비되지 않았습니다!")
    return
  }

  savingPdf.value = true
  
  try {
    await nextTick()
    const element = pdfComponent.value.pdfTarget

    if (!element) {
      alert("PDF 타겟이 아직 준비되지 않았어요!")
      return
    }

    const opt = {
      margin: [5, 5, 5, 5],
      filename: `${newsData.value.seniorCenterName}_${newsData.value.year}_${String(newsData.value.month).padStart(2, '0')}_신문.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: {
        scale: 1.5,
        useCORS: true,
        allowTaint: true,
        scrollX: 0,
        scrollY: 0
      },
      jsPDF: {
        unit: 'mm',
        format: 'a4',
        orientation: 'portrait',
        compress: true
      }
    }

    await html2pdf().set(opt).from(element).save()
    
  } catch (error) {
    console.error('PDF 저장 실패:', error)
    alert('PDF 저장에 실패했습니다.')
  } finally {
    savingPdf.value = false
  }
}

onMounted(() => {
  const newsId = route.params.id
  if (newsId) {
    generateAiNews(parseInt(newsId))
  } else {
    error.value = '잘못된 신문 ID입니다.'
  }
})
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  padding: 20px;
  background-color: #f5f5f5;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  font-size: 18px;
  color: #666;
}

.error {
  text-align: center;
  padding: 60px 20px;
  color: #e74c3c;
  font-size: 16px;
}

.no-template {
  text-align: center;
  padding: 60px 20px;
  color: #666;
  font-size: 16px;
}

.save-button {
  text-align: center;
  margin: 40px 0;
  padding: 20px;
  gap: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.save-button button {
  padding: 12px 24px;
  font-size: 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.save-button button:first-child {
  background-color: #3498db;
  color: white;
}

.save-button button:first-child:hover:not(:disabled) {
  background-color: #2980b9;
}

.save-button button:first-child:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

.back-btn {
  background-color: #95a5a6 !important;
  color: white !important;
}

.back-btn:hover {
  background-color: #7f8c8d !important;
}
</style>