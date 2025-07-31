<template>
  <div class="profile-wrap">
    <h1 class="headline">프로필 정보 작성</h1>
    <div class="profile-card">
      <!-- 프로필 사진 + 제한사항 -->
      <div class="profile-pic-area">
        <div class="pic-and-btns">
          <div class="profile-img-preview">
            <img
              v-if="previewUrl"
              :src="previewUrl"
              alt="profile"
              class="profile-img"
            />
            <div v-else class="default-profile"></div>
          </div>
          <div class="pic-btns">
            <label class="upload-btn">
              사진 첨부
              <input type="file" accept="image/*" @change="onFileChange" hidden />
            </label>
            <button class="del-btn" @click="removeFile">지우기</button>
          </div>
        </div>
        <div class="profile-info">
          <div class="pic-restrictions">
            <p>프로필 사진 제한:</p>
            <ul>
              <li>1. Min. 400 x 400px</li>
              <li>2. Max. 2MB</li>
              <li>3. Your face or company logo</li>
            </ul>
          </div>
        </div>
      </div>
      <!-- 프로필 입력 영역 -->
      <div class="profile-input-area">
        <p class="user-detail-title">User Details</p>
        <label class="nickname-label">닉네임</label>
        <div class="nickname-input-row">
          <input
            v-model="nickname"
            class="nickname-input"
            placeholder="닉네임을 입력하세요"
            @input="nicknameMessage = ''"
            @keyup.enter="checkNickname"
          />
          <button class="dup-btn" @click="checkNickname">중복 검사</button>
        </div>
        <div
          class="nickname-msg"
          :class="{ valid: nicknameMessage==='사용가능한 닉네임입니다.' }"
          v-if="nicknameMessage"
        >
          {{ nicknameMessage }}
        </div>
        <button class="submit-btn" :disabled="!nicknameAvailable" @click="completeProfile">
          회원가입 완료
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const nickname = ref('')
const nicknameAvailable = ref(false)
const nicknameMessage = ref('')
const router = useRouter()

const previewUrl = ref(null)
const selectedFile = ref(null)

// 사진 첨부
function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    alert('파일 용량은 2MB 이하만 가능합니다.')
    return
  }
  const img = new Image()
  img.onload = () => {
    if (img.width < 400 || img.height < 400) {
      alert('이미지 크기는 400x400 이상이어야 합니다.')
      return
    }
    previewUrl.value = URL.createObjectURL(file)
    selectedFile.value = file
  }
  img.src = URL.createObjectURL(file)
}

// 사진 지우기
function removeFile() {
  previewUrl.value = null
  selectedFile.value = null
}

// 닉네임 중복검사(임시: "싸피"가 포함되어 있으면 불가, 아니면 가능)
function checkNickname() {
  if (!nickname.value.trim()) {
    nicknameMessage.value = '닉네임을 입력해주세요.'
    nicknameAvailable.value = false
    return
  }
  // 임시 중복검사 로직(백 연결 전용)
  if (nickname.value.includes('싸피')) {
    nicknameMessage.value = '이미 사용 중인 닉네임입니다.'
    nicknameAvailable.value = false
  } else {
    nicknameMessage.value = '사용가능한 닉네임입니다.'
    nicknameAvailable.value = true
  }
}

// 회원가입 완료
function completeProfile() {
  alert('회원가입이 완료되었습니다!')
  // 이후 페이지 이동 or API 연동
  router.push('/login')
}
</script>

<style scoped>
.profile-wrap {
  width: 100%;
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.headline {
  font-size: 2.3rem;
  font-weight: 700;
  margin: 60px 0 36px 0;
  letter-spacing: -0.5px;
  text-align: left;
  width: 900px;
}
.profile-card {
  background: #fff;
  border: 1.2px solid #e9e9e9;
  border-radius: 10px;
  padding: 36px 42px 34px 42px;
  display: flex;
  flex-direction: row;
  gap: 60px;
  width: 900px;
  min-height: 320px;
  margin-bottom: 50px;
  box-sizing: border-box;
}
.profile-pic-area {
  display: flex;
  flex-direction: row;
  gap: 20px;
  align-items: flex-start;
  min-width: 260px;
  flex: 1.3;
}
.pic-and-btns {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
}
.profile-img-preview {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  overflow: hidden;
}
.profile-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}
.default-profile {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #e2e4ea;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  color: #b4b4b4;
  background-image: url('data:image/svg+xml;utf8,<svg fill="gray" height="50" width="50" viewBox="0 0 50 50" xmlns="http://www.w3.org/2000/svg"><circle cx="25" cy="20" r="12"/><ellipse cx="25" cy="41" rx="15" ry="9"/></svg>');
  background-repeat: no-repeat;
  background-position: center;
  background-size: 60px 60px;
}
.pic-btns {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.upload-btn, .del-btn {
  font-size: 13px;
  background: #f6f9ff;
  border: 1px solid #b4c2e6;
  color: #2558af;
  padding: 7px 13px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 1px;
  font-weight: 500;
}
.del-btn {
  color: #f55555;
  border: 1px solid #ffdddd;
  background: #fff6f6;
}
.profile-info {
  padding-top: 7px;
  min-width: 180px;
}
.pic-restrictions {
  font-size: 13px;
  color: #333;
  line-height: 1.7;
  letter-spacing: 0px;
}
.pic-restrictions ul {
  padding-left: 15px;
  margin: 4px 0 0 0;
}
.profile-input-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 2;
  min-width: 320px;
  margin-left: 25px;
}
.user-detail-title {
  font-size: 1.09rem;
  font-weight: 600;
  color: #111;
  margin-bottom: 9px;
}
.nickname-label {
  font-size: 1rem;
  margin-bottom: 4px;
  color: #2a2d33;
  font-weight: 500;
}
.nickname-input-row {
  display: flex;
  flex-direction: row;
  gap: 8px;
}
.nickname-input {
  flex: 1;
  font-size: 1rem;
  padding: 11px 15px;
  border: 1px solid #c6cbd1;
  border-radius: 5px 0 0 5px;
  background: #f7fafd;
  outline: none;
  transition: border 0.15s;
}
.nickname-input:focus {
  border: 1.6px solid #254fc7;
}
.dup-btn {
  font-size: 1rem;
  background: #2b71e8;
  color: #fff;
  border: none;
  border-radius: 0 5px 5px 0;
  padding: 11px 17px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.13s;
}
.dup-btn:hover {
  background: #175cc5;
}
.nickname-msg {
  margin-top: 8px;
  font-size: 1rem;
  color: #16b64a;
}
.nickname-msg:not(.valid) {
  color: #f55555;
}
.submit-btn {
  margin-top: 22px;
  font-size: 1.08rem;
  background: #2b71e8;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 12px 0;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.13s;
  width: 100%;
}
.submit-btn:disabled {
  background: #c8d6ee;
  color: #fff;
  cursor: not-allowed;
}
</style>