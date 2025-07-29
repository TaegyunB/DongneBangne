<template>
  <div class="container">
    <div class="header">
      <h1>경로당 맞춤형 도전을 생성하시겠어요?</h1>
      <h3>몇가지만 작성해주세요</h3>
    </div>

    <div class="form-group">
      <label>도전 제목</label>
      <input
        v-model="title"
        type="text"
        placeholder="간단하게 작성해주세요."
        class="styled-input"
      />

      <label>장소</label>
      <input
        v-model="place"
        type="text"
        placeholder="미션은 어디서 수행하나요?"
        class="styled-input"
      />

      <label>상세 정보</label>
      <textarea
        v-model="description"
        placeholder="간단하게 작성해주세요."
        class="styled-textarea"
      />

      <button class="submit-button" @click="handleSubmit">
        ✔ 저장
      </button>
    </div>

    <!-- 생성 완료 모달 -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content">
        <h2 class="modal-title">00경로당의 도전</h2>
        <p class="modal-subtext">도전이 성공적으로 지정되었습니다.</p>
        <button class="modal-button" @click="goToList">도전 목록으로</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const title = ref('')
const place = ref('')
const description = ref('')
const showModal = ref(false)
const router = useRouter()

const handleSubmit = () => {
  console.log('제목:', title.value)
  console.log('장소:', place.value)
  console.log('설명:', description.value)

  // 저장 완료 후 모달 표시
  showModal.value = true
}

const goToList = () => {
  showModal.value = false
  router.push('/challenges')  
}
</script>

<style scoped>
.container {
  max-width: 500px;
  margin: 0 auto;
  padding: 40px 20px;
}

.header {
  text-align: center;
}

.header h1 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 10px;
}

.header h3 {
  font-size: 20px;
  font-weight: 500;
  color: #555;
  margin-bottom: 30px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group label {
  font-size: 18px;
  font-weight: 600;
  text-align: left;
}

.styled-input,
.styled-textarea {
  width: 100%;
  padding: 12px 15px;
  font-size: 14px;
  border: 1.5px solid #ccc;
  border-radius: 10px;
  outline: none;
}

.styled-input:focus,
.styled-textarea:focus {
  border-color: #a88beb;
}

.styled-input::placeholder,
.styled-textarea::placeholder {
  color: #aaa;
  font-size: 14px;
}

.styled-textarea {
  min-height: 120px;
  resize: vertical;
}

.submit-button {
  margin-top: 20px;
  background-color: #a88beb;
  color: white;
  padding: 12px 0;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.submit-button:hover {
  background-color: #946fe5;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: white;
  padding: 60px 50px;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  max-width: 400px;
}

.modal-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 12px;
}

.modal-subtext {
  font-size: 16px;
  color: #333;
  margin-bottom: 24px;
}

.modal-button {
  background-color: #a88beb;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
}

.modal-button:hover {
  background-color: #946fe5;
}
</style>
