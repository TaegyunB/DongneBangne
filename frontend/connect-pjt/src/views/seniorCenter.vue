<template>
  <div class="find-senior-center">
    <main class="main-content">
      <h1 class="headline">경로당 찾기</h1>
      <div class="search-box">
        <select v-model="selectedType" class="type-select">
          <option value="name">이름</option>
          <option value="address">주소</option>
        </select>
        <input
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="싸피 경로당"
          @keyup.enter="onSearch"
        />
        <button class="search-btn" @click="onSearch">Search</button>
      </div>

      <!-- 검색 결과 리스트 -->
      <table v-if="searchResults.length" class="result-table">
        <thead>
          <tr>
            <th>이름</th>
            <th>주소</th>
            <th>지도</th>
            <th>확인</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="center in searchResults" :key="center.id">
            <td>{{ center.name }}</td>
            <td>{{ center.address }}</td>
            <td>
              <button class="map-btn" @click="openKakaoMap(center.address)">지도</button>
            </td>
            <td>
              <button class="confirm-btn" @click="openConfirm(center)">확인</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else-if="searched" class="no-result">검색 결과가 없습니다.</div>

      <!-- 확인 모달 -->
      <div v-if="showModal" class="modal-overlay">
        <div class="modal-content">
          <h3>정말로 맞습니까?</h3>
          <p>
            <b>{{ modalCenter.name }}</b><br />
            {{ modalCenter.address }}
          </p>
          <div class="modal-actions">
            <button class="confirm-btn" @click="confirmCenter">네</button>
            <button class="cancel-btn" @click="closeModal">아니오</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/api/axios' // axios 인스턴스 import만!
import { useRouter } from 'vue-router'

const selectedType = ref('name')
const keyword = ref('')
const searchResults = ref([])
const searched = ref(false)
const router = useRouter()

const showModal = ref(false)
const modalCenter = ref({ name: '', address: '' })

// DB에서 실제 검색
async function fetchCenters() {
  try {
    const res = await api.get('/api/v1/senior-centers', {
      params: { type: selectedType.value, keyword: keyword.value.trim() }
    })
    console.log('백엔드 응답 데이터:', res.data, res.status)
    console.log('파라미터:', selectedType.value, keyword.value.trim())
    searchResults.value = res.data
    searched.value = true
  } catch (e) {
    searchResults.value = []
    searched.value = true
  }
}

function onSearch() {
  if (!keyword.value.trim()) {
    searchResults.value = []
    searched.value = true
    return
  }
  fetchCenters()
}

// 지도 버튼 클릭시 카카오맵 새 창 열기
function openKakaoMap(address) {
  const url = 'https://map.kakao.com/?q=' + encodeURIComponent(address)
  window.open(url, '_blank', 'width=700,height=600')
}

// 확인 버튼 클릭시 모달 오픈
function openConfirm(center) {
  modalCenter.value = center
  showModal.value = true
}

// // 모달 내 확인/취소 버튼 동작
// function confirmCenter() {
//   showModal.value = false
//   router.push('/senior-center/profile')
// }
// function closeModal() {
//   showModal.value = false
// }

async function confirmCenter() {
  try {
    // 실제 DB에 저장 요청
    await api.post('/api/v1/users/senior-center', {
      seniorCenterId: modalCenter.value.id // ← id가 맞는지 반드시 확인!
    })
    showModal.value = false
    // 성공 시 라우터 이동
    router.push('/senior-center/profile')
  } catch (err) {
    alert('경로당 선택에 실패했습니다.')
    showModal.value = false
  }
}

</script>

<style scoped>
.find-senior-center {
  min-height: 100vh;
  background: #fff;
}
.main-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 120px;
}
.headline {
  font-size: 42px;
  font-weight: 600;
  margin-bottom: 50px;
  letter-spacing: -1px;
}
.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border: 2px solid #bbb;
  border-radius: 8px;
  padding: 18px 24px;
  gap: 14px;
  min-width: 470px;
  box-sizing: border-box;
}
.type-select {
  font-size: 16px;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  min-width: 70px;
}
.search-input {
  font-size: 16px;
  padding: 8px 12px;
  border: none;
  outline: none;
  width: 200px;
  background: transparent;
}
.search-btn {
  font-size: 16px;
  padding: 8px 24px;
  background: #12795a;
  color: #fff;
  border: none;
  border-radius: 5px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.search-btn:hover {
  background: #0f6148;
}

/* 검색 결과 테이블 */
.result-table {
  width: 700px;
  margin-top: 32px;
  border-collapse: collapse;
  background: #fafbfc;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(30,40,60,0.06);
}
.result-table th, .result-table td {
  padding: 14px 10px;
  border-bottom: 1px solid #eaeaea;
  text-align: center;
}
.result-table th {
  background: #f4f4f5;
  font-size: 1.06rem;
}
.result-table tr:last-child td {
  border-bottom: none;
}
.map-btn, .confirm-btn, .cancel-btn {
  font-size: 14px;
  padding: 6px 18px;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  background: #fafbfc;
  color: #12795a;
  transition: background 0.15s;
}
.map-btn:hover {
  background: #eafaf3;
}
.confirm-btn {
  background: #12795a;
  color: #fff;
  margin-right: 10px;
}
.confirm-btn:hover {
  background: #0f6148;
}
.cancel-btn {
  background: #bbb;
  color: #fff;
}
.cancel-btn:hover {
  background: #888;
}
.no-result {
  margin-top: 32px;
  color: #8d8d8d;
  font-size: 1.1rem;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  z-index: 2000;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(50,60,70,0.22);
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-content {
  background: #fff;
  padding: 38px 38px 28px 38px;
  border-radius: 16px;
  text-align: center;
  min-width: 320px;
  box-shadow: 0 2px 16px rgba(20,25,40,0.14);
}
.modal-actions {
  margin-top: 26px;
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
