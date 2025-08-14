<template>
  <div class="profile-wrap">
    <h1 class="headline">프로필 정보 작성</h1>

    <div class="profile-card">
      <!-- 프로필 사진 영역 -->
      <div class="profile-pic-area">
        <div class="pic-and-btns">
          <div class="profile-img-preview" aria-label="프로필 사진 미리보기">
            <img
              v-if="previewUrl"
              :src="previewUrl"
              alt="프로필 사진"
              class="profile-img"
            />
            <div v-else class="default-profile" aria-hidden="true"></div>
          </div>

          <div class="pic-btns">
            <!-- 필요 시: 이미지 수정 불가로 만들려면 disabled 처리 -->
            <label class="upload-btn">
              사진 첨부
              <input type="file" accept="image/*" @change="onFileChange" hidden />
            </label>
            <button type="button" class="del-btn" @click="removeFile">지우기</button>
          </div>
        </div>

        <div class="profile-info">
          <div class="pic-restrictions" id="photo-guide">
            <p class="guide-title">사진 안내</p>
            <ul>
              <li>권장 크기: 400 × 400픽셀 이상</li>
              <li>파일 용량: 2MB 이하</li>
              <li>선택 사항입니다(나중에 변경 가능)</li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 닉네임 입력 -->
      <div class="profile-input-area">
        <p class="user-detail-title">내 정보</p>

        <label for="nickname" class="nickname-label">닉네임</label>
        <div class="nickname-input-row">
          <input
            id="nickname"
            v-model="nickname"
            class="nickname-input"
            placeholder="닉네임을 입력하세요 (2~12자)"
            :aria-invalid="!nicknameValid && nicknameTouched"
            aria-describedby="nickname-help nickname-msg"
            @input="onNicknameInput"
            @blur="nicknameTouched = true"
            @keyup.enter="checkNickname"
          />
          <button type="button" class="dup-btn" @click="checkNickname">중복 검사</button>
        </div>

        <div id="nickname-help" class="nickname-help">
          공백 없이 한글/영문/숫자 2~12자
        </div>

        <div
          id="nickname-msg"
          class="nickname-msg"
          :class="{ valid: nicknameAvailable, invalid: nicknameTouched && !nicknameAvailable }"
          v-if="nicknameMessage"
          aria-live="polite"
          role="status"
        >
          {{ nicknameMessage }}
        </div>

        <button
          type="button"
          class="submit-btn"
          :disabled="!nicknameAvailable"
          @click="completeProfile"
        >
          저장하기
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const nickname = ref('')
const nicknameAvailable = ref(false)
const nicknameMessage = ref('')
const nicknameTouched = ref(false)
const previewUrl = ref(null)   // 프로필 이미지 URL(기본/업로드)
const selectedFile = ref(null) // 선택한 파일(옵션)

const router = useRouter()

// 페이지 진입 시 기본값 조회
onMounted(async () => {
  try {
    const res = await api.get('/api/v1/users/profile')
    nickname.value = res.data.nickname || ''
    previewUrl.value = res.data.profileImage || null
  } catch (e) {
    // 필요 시 토큰 만료 처리
  }
})

// 닉네임 입력 유효성(프론트 가드)
const nicknameValid = computed(() => {
  const v = nickname.value.trim()
  if (!v) return false
  if (v.length < 2 || v.length > 12) return false
  return true
})

function onNicknameInput() {
  nicknameMessage.value = ''
  nicknameAvailable.value = false
}

// 닉네임 중복 검사
async function checkNickname() {
  nicknameTouched.value = true
  if (!nicknameValid.value) {
    nicknameMessage.value = '닉네임 형식을 확인해주세요.'
    nicknameAvailable.value = false
    return
  }
  try {
    // 실제 API가 있다면 아래 주석 해제
    // const res = await api.get('/api/v1/users/nickname-check', { params: { nickname: nickname.value } })
    // nicknameAvailable.value = !res.data.exists
    // nicknameMessage.value = res.data.exists ? '이미 사용 중인 닉네임입니다.' : '사용 가능한 닉네임입니다.'

    // 데모 로직(교체 예정)
    if (nickname.value.includes('관리자') || nickname.value.includes('운영자')) {
      nicknameAvailable.value = false
      nicknameMessage.value = '이미 사용 중인 닉네임입니다.'
    } else {
      nicknameAvailable.value = true
      nicknameMessage.value = '사용 가능한 닉네임입니다.'
    }
  } catch (e) {
    nicknameAvailable.value = false
    nicknameMessage.value = '확인 중 오류가 발생했습니다.'
  }
}

// 사진 첨부
function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return

  if (file.size > 2 * 1024 * 1024) {
    alert('파일 용량은 2MB 이하만 가능합니다.')
    return
  }

  const tempUrl = URL.createObjectURL(file)
  const img = new Image()
  img.onload = () => {
    if (img.width < 400 || img.height < 400) {
      alert('이미지는 400×400픽셀 이상이어야 합니다.')
      URL.revokeObjectURL(tempUrl)
      return
    }
    // 이전 blob 정리
    if (previewUrl.value && previewUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(previewUrl.value)
    }
    previewUrl.value = tempUrl
    selectedFile.value = file
  }
  img.src = tempUrl
}

function removeFile() {
  if (previewUrl.value && previewUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = null
  selectedFile.value = null
}

// 저장(닉네임 + 프로필 이미지 URL)
async function completeProfile() {
  try {
    let profileImageUrl = previewUrl.value

    // 실제 업로드 API가 있다면 사용
    // if (selectedFile.value) {
    //   const formData = new FormData()
    //   formData.append('file', selectedFile.value)
    //   const uploadRes = await api.post('/api/v1/files/profile', formData)
    //   profileImageUrl = uploadRes.data.url
    // }

    await api.put('/api/v1/users/profile', {
      nickname: nickname.value.trim(),
      profileImage: profileImageUrl
    })

    alert('저장되었습니다.')
    router.push('/mainpage')
  } catch (e) {
    alert('저장에 실패했습니다.')
  }
}
</script>

<style scoped>
:root { --accent:#12795a }

/* 전체 */
.profile-wrap {
  width: 100%;
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #111;
}

/* 포커스 가시성(키보드 접근성) */
:focus-visible {
  outline: 3px solid var(--accent);
  outline-offset: 3px;
  border-radius: 8px;
}

/* 제목 */
.headline {
  font-size: 44px;            /* 큰 제목 */
  font-weight: 800;
  margin: 48px 0 28px 0;
  letter-spacing: -0.5px;
  width: 960px;
}

/* 카드 */
.profile-card {
  background: #fff;
  border: 2px solid #dcdcdc;  /* 더 굵은 테두리로 대비 */
  border-radius: 14px;
  padding: 32px 36px;
  display: flex;
  gap: 48px;
  width: 960px;
  min-height: 340px;
  margin-bottom: 56px;
  box-sizing: border-box;
  box-shadow: 0 2px 12px rgba(0,0,0,.06);
}

/* 왼쪽: 프로필 사진 */
.profile-pic-area {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  min-width: 320px;
  flex: 1.3;
}

.pic-and-btns {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.profile-img-preview {
  width: 128px;               /* 더 큰 이미지 */
  height: 128px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 2px solid #e5e7eb;
}

.profile-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-profile {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #e2e4ea;
  background-image: url('data:image/svg+xml;utf8,<svg fill="gray" height="64" width="64" viewBox="0 0 50 50" xmlns="http://www.w3.org/2000/svg"><circle cx="25" cy="20" r="12"/><ellipse cx="25" cy="41" rx="15" ry="9"/></svg>');
  background-repeat: no-repeat;
  background-position: center;
  background-size: 72px 72px;
}

.pic-btns {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.upload-btn, .del-btn {
  font-size: 18px;            /* 버튼 글자 확대 */
  min-height: 44px;           /* 터치 영역 확대 */
  background: #effaf5;
  border: 2px solid #cdeee1;
  color: var(--accent);
  padding: 8px 16px;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 800;
  text-align: center;
}
.del-btn {
  background: #fff5f5;
  border: 2px solid #ffdcdc;
  color: #d33b3b;
}

.profile-info { padding-top: 6px; min-width: 200px }
.guide-title { font-size: 18px; font-weight: 800; margin-bottom: 6px }
.pic-restrictions { font-size: 16px; line-height: 1.7; color: #333 }
.pic-restrictions ul { padding-left: 18px; margin: 4px 0 0 }

/* 오른쪽: 닉네임 */
.profile-input-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 2;
  min-width: 360px;
}

.user-detail-title {
  font-size: 20px;
  font-weight: 800;
  color: #111;
  margin-bottom: 6px;
}

.nickname-label {
  font-size: 18px;
  margin-bottom: 4px;
  color: #2a2d33;
  font-weight: 700;
}

.nickname-input-row {
  display: flex;
  gap: 10px;
}

.nickname-input {
  flex: 1;
  font-size: 20px;            /* 큰 입력 글자 */
  padding: 14px 16px;
  border: 2px solid #c6cbd1;
  border-radius: 10px;
  background: #fbfdff;
  outline: none;
  transition: border .15s;
}
.nickname-input:focus {
  border: 2px solid var(--accent);
}

.dup-btn {
  font-size: 18px;
  background: var(--accent);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 0 18px;
  min-width: 120px;
  min-height: 48px;           /* 충분한 터치 영역 */
  font-weight: 800;
  cursor: pointer;
  transition: background .15s;
}
.dup-btn:hover { background: #0f6148 }

.nickname-help {
  margin-top: 6px;
  font-size: 16px;
  color: #444;
}

.nickname-msg {
  margin-top: 10px;
  font-size: 18px;
  font-weight: 800;
}
.nickname-msg.valid { color: #15803d }     /* 초록 */
.nickname-msg.invalid { color: #b91c1c }   /* 빨강(고대비) */

.submit-btn {
  margin-top: 22px;
  font-size: 20px;
  background: var(--accent);
  color: #fff;
  border: none;
  border-radius: 12px;
  padding: 14px 0;
  min-height: 52px;            /* 큰 버튼 */
  font-weight: 900;
  cursor: pointer;
  transition: background .15s;
  width: 100%;
}
.submit-btn:hover { background: #0f6148 }
.submit-btn:disabled {
  background: #b7c9c2;
  color: #fff;
  cursor: not-allowed;
}

/* 반응형 */
@media (max-width: 960px) {
  .headline { width: 100%; padding: 0 16px }
  .profile-card { width: 100%; border-radius: 0; padding: 24px 16px; gap: 24px }
  .profile-pic-area { min-width: 0; flex-direction: column }
  .nickname-input-row { flex-direction: column }
  .dup-btn { width: 100% }
}
</style>