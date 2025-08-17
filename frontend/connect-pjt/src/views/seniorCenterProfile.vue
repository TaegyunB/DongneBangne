<template>
  <div class="profile-wrap">
    <h1 class="headline">프로필 정보 작성</h1>

    <div class="profile-card">
      <!-- 프로필 사진 영역 -->
      <div class="profile-pic-area">
        <div class="pic-and-btns">
          <div class="profile-img-preview" aria-label="프로필 사진 미리보기">
            <img v-if="safePreviewUrl" :src="safePreviewUrl" alt="프로필 사진" class="profile-img" />
            <div v-else class="default-profile" aria-hidden="true"></div>
          </div>

          <div class="pic-btns">
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
              <li>권장 크기: <b>400×400px</b></li>
              <li>파일 용량: <b>2MB 이하</b></li>
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
          <button
            type="button"
            class="dup-btn"
            :disabled="!nicknameValid || checkingDup"
            @click="checkNickname"
          >
            {{ checkingDup ? '확인 중…' : '중복 확인' }}
          </button>
        </div>

        <div id="nickname-help" class="nickname-help">
          공백 없이 한글/영문/숫자 2~12자
          <br /><small class="muted">저장 시 “소속경로당 닉네임” 형식으로 표시됩니다.</small>
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
          :disabled="!nicknameAvailable || saving"
          @click="completeProfile"
        >
          {{ saving ? '저장 중…' : '저장하기' }}
        </button>
      </div>
    </div>

    <!-- 안내 모달 -->
    <div v-if="notice.open" class="notice-overlay" @keydown.escape="closeNotice">
      <div class="notice-modal" role="dialog" aria-modal="true" aria-labelledby="noticeTitle">
        <h3 id="noticeTitle" class="notice-title">{{ notice.title }}</h3>
        <p class="notice-text">{{ notice.message }}</p>
        <button class="notice-btn" @click="closeNotice">확인</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()

const isBlobUrl = url => typeof url === 'string' && url.startsWith('blob:')
const ensureHttps = url => {
  if (!url) return null
  if (isBlobUrl(url)) return url
  return url.replace(/^http:\/\//, 'https://')
}

const nickname = ref('')
const nicknameAvailable = ref(false)
const nicknameMessage = ref('')
const nicknameTouched = ref(false)
const checkingDup = ref(false)
const saving = ref(false)

const previewUrl = ref(null)
const selectedFile = ref(null)
const safePreviewUrl = computed(() => ensureHttps(previewUrl.value))

const myCenterName = ref('') // “소속경로당 닉네임”용 접두어

// 안내 모달
const notice = ref({ open: false, title: '안내', message: '' })
const showNotice = (message, title = '안내') => { notice.value = { open: true, title, message } }
const closeNotice = () => { notice.value.open = false }

// 진입 시 사용자/센터 정보
onMounted(async () => {
  try {
    const res = await api.get('/api/v1/users/profile', { withCredentials: true })
    nickname.value = res.data?.nickname || ''
    previewUrl.value = ensureHttps(res.data?.profileImage || null)
  } catch (e) {}

  try {
    const me = await api.get('/api/v1/me', { withCredentials: true })
    myCenterName.value =
      me.data?.seniorCenterName ??
      me.data?.centerName ??
      me.data?.myCenter?.centerName ??
      me.data?.mySeniorCenter?.name ??
      ''
  } catch (e) {
    myCenterName.value = ''
  }
})

// 입력 규칙: 공백 없이 한/영/숫자 2~12자 (최종 저장 문자열에는 공백 포함됨)
const nicknamePattern = /^[가-힣A-Za-z0-9]{2,12}$/
const nicknameValid = computed(() => nicknamePattern.test(nickname.value.trim()))

function onNicknameInput() {
  nicknameMessage.value = ''
  nicknameAvailable.value = false
  nicknameTouched.value = true
}

// 최종 표시용 닉네임: “센터명 닉네임”(센터명이 없으면 닉네임만)
const buildFinalNickname = (base, center) => {
  const b = base.trim()
  if (!center) return b
  return `${center} ${b}`
}

// 닉네임 중복 검사 (최종 문자열 기준)
async function checkNickname() {
  nicknameTouched.value = true
  if (!nicknameValid.value) {
    showNotice('닉네임 형식을 확인해주세요. (공백 없이 한글/영문/숫자 2~12자)', '닉네임 안내')
    nicknameAvailable.value = false
    nicknameMessage.value = '닉네임 형식을 확인해주세요.'
    return
  }
  try {
    checkingDup.value = true
    const nameForCheck = buildFinalNickname(nickname.value, myCenterName.value)

    // 실제 API가 있으면 아래 사용
    // const { data } = await api.get('/api/v1/users/nickname-check', { params: { nickname: nameForCheck }, withCredentials: true })
    // const exists = !!data?.exists

    // 데모 금칙어 로직
    const exists = /관리자|운영자/.test(nameForCheck)

    nicknameAvailable.value = !exists
    nicknameMessage.value = exists ? '이미 사용 중인 닉네임입니다.' : `사용 가능: ${nameForCheck}`
    if (exists) showNotice('이미 사용 중인 닉네임입니다.', '중복 확인')
  } catch (e) {
    nicknameAvailable.value = false
    nicknameMessage.value = '확인 중 오류가 발생했습니다.'
    showNotice('닉네임 중복 확인 중 오류가 발생했습니다.', '오류')
  } finally {
    checkingDup.value = false
  }
}

// 사진 첨부
function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return

  if (file.size > 2 * 1024 * 1024) {
    showNotice('파일 용량은 2MB 이하만 가능합니다.', '사진 안내')
    return
  }

  const tempUrl = URL.createObjectURL(file)
  const img = new Image()
  img.onload = () => {
    if (img.width < 400 || img.height < 400) {
      showNotice('이미지는 400×400픽셀 이상이어야 합니다.', '사진 안내')
      URL.revokeObjectURL(tempUrl)
      return
    }
    if (previewUrl.value && previewUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(previewUrl.value)
    }
    previewUrl.value = tempUrl
    selectedFile.value = file
  }
  img.onerror = () => {
    showNotice('이미지 파일을 불러올 수 없습니다.', '사진 안내')
    URL.revokeObjectURL(tempUrl)
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

// 저장 (최종 문자열 = “소속경로당 닉네임”)
// 백엔드가 이미지 URL을 재수급하므로 닉네임 중심으로 저장하고,
// 공개 URL(비-blob)만 있으면 선택적으로 함께 전달
async function completeProfile() {
  if (!nicknameAvailable.value) {
    showNotice('닉네임 중복 확인 후 저장해주세요.', '저장 안내')
    return
  }
  try {
    saving.value = true

    const finalNickname = buildFinalNickname(nickname.value, myCenterName.value)
    const payload = { nickname: finalNickname }

    if (previewUrl.value && !isBlobUrl(previewUrl.value)) {
      payload.profileImage = ensureHttps(previewUrl.value)
    }

    await api.put('/api/v1/users/profile', payload, { withCredentials: true })

    showNotice('저장되었습니다.', '완료')
    const unwatch = watch(() => notice.value.open, v => { if (!v) router.push('/mainpage') })
  } catch (e) {
    showNotice('저장에 실패했습니다. 잠시 후 다시 시도해주세요.', '오류')
  } finally {
    saving.value = false
  }
}

onBeforeUnmount(() => {
  if (previewUrl.value && previewUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(previewUrl.value)
  }
})
</script>

<style scoped>
/* 브랜드/모달 컬러 */
.profile-wrap {
  --brand:#3074FF; --brand-hover:#2866E6; --brand-active:#2258CC;
  --notice:#4B5563; --notice-hover:#374151; --notice-active:#1F2937;
}

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

/* 포커스 */
:focus-visible { outline: 3px solid var(--brand); outline-offset: 3px; border-radius: 8px }

/* 제목 */
.headline { font-size: 44px; font-weight: 800; margin: 48px 0 28px; letter-spacing: -0.5px; width: 960px }

/* 카드 */
.profile-card {
  background: #fff; border: 2px solid #dcdcdc; border-radius: 14px;
  padding: 32px 36px; display: flex; gap: 48px; width: 960px; min-height: 340px;
  margin-bottom: 56px; box-sizing: border-box; box-shadow: 0 2px 12px rgba(0,0,0,.06);
}

/* 왼쪽 */
.profile-pic-area { display: flex; gap: 20px; align-items: flex-start; min-width: 320px; flex: 1.3 }
.pic-and-btns { display: flex; flex-direction: column; align-items: center; gap: 16px }

.profile-img-preview {
  width: 128px; height: 128px; border-radius: 50%; background: #f3f4f6;
  display: flex; align-items: center; justify-content: center; overflow: hidden; border: 2px solid #e5e7eb;
}
.profile-img { width: 100%; height: 100%; object-fit: cover }

.default-profile {
  width: 72px; height: 72px; border-radius: 50%;
  background: #e2e4ea;
  background-image: url('data:image/svg+xml;utf8,<svg fill="gray" height="64" width="64" viewBox="0 0 50 50" xmlns="http://www.w3.org/2000/svg"><circle cx="25" cy="20" r="12"/><ellipse cx="25" cy="41" rx="15" ry="9"/></svg>');
  background-repeat: no-repeat; background-position: center; background-size: 72px 72px;
}

.pic-btns { display: flex; flex-direction: column; gap: 10px }

/* 파란 버튼 공통 */
.upload-btn, .dup-btn, .submit-btn { background: var(--brand); color: #fff; border: none; border-radius: 10px; cursor: pointer; transition: background .15s, transform .05s, opacity .15s }
.upload-btn:hover, .dup-btn:hover, .submit-btn:hover { background: var(--brand-hover) }
.upload-btn:active, .dup-btn:active, .submit-btn:active { background: var(--brand-active); transform: translateY(1px) }

.upload-btn { font-size: 18px; min-height: 44px; padding: 8px 16px; font-weight: 800; text-align: center }
.del-btn {
  font-size: 18px; min-height: 44px; background: #fff5f5; border: 2px solid #ffdcdc;
  color: #d33b3b; padding: 8px 16px; border-radius: 10px; cursor: pointer; font-weight: 800;
}

/* 안내 */
.profile-info { padding-top: 6px; min-width: 200px }
.guide-title { font-size: 18px; font-weight: 800; margin-bottom: 6px }
.pic-restrictions { font-size: 16px; line-height: 1.7; color: #333 }
.pic-restrictions ul { padding-left: 18px; margin: 4px 0 0 }

/* 오른쪽 */
.profile-input-area { display: flex; flex-direction: column; gap: 12px; flex: 2; min-width: 360px }
.user-detail-title { font-size: 20px; font-weight: 800; color: #111; margin-bottom: 6px }
.nickname-label { font-size: 18px; margin-bottom: 4px; color: #2a2d33; font-weight: 700 }
.nickname-input-row { display: flex; gap: 10px }

.nickname-input {
  flex: 1; font-size: 20px; padding: 14px 16px; border: 2px solid #c6cbd1; border-radius: 10px; background: #fbfdff; outline: none; transition: border .15s
}
.nickname-input:focus { border: 2px solid var(--brand) }

.dup-btn { padding: 0 18px; min-width: 120px; min-height: 48px; font-size: 18px; font-weight: 800 }
.dup-btn:disabled { opacity: .45; cursor: not-allowed }

.nickname-help { margin-top: 6px; font-size: 16px; color: #444 }
.nickname-help .muted { color:#6b7280 }
.nickname-msg { margin-top: 10px; font-size: 18px; font-weight: 800 }
.nickname-msg.valid { color: #15803d }
.nickname-msg.invalid { color: #b91c1c }

.submit-btn { margin-top: 22px; font-size: 20px; padding: 14px 0; min-height: 52px; font-weight: 900; width: 100%; border-radius: 12px }
.submit-btn:disabled { opacity: .45; cursor: not-allowed }

/* 안내 모달 */
.notice-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.38); display: flex; align-items: center; justify-content: center; z-index: 2000; padding: 16px }
.notice-modal { background: #fff; width: min(420px, 92vw); border-radius: 14px; box-shadow: 0 6px 24px rgba(0,0,0,.18); padding: 22px 20px; text-align: center }
.notice-title { font-size: 22px; font-weight: 800; margin-bottom: 8px }
.notice-text { font-size: 16px; color: #222; margin-bottom: 14px }
.notice-btn { background: var(--notice); color:#fff; border:none; border-radius:10px; padding:10px 16px; min-height:44px; min-width:120px; cursor:pointer; transition: background .15s, transform .05s }
.notice-btn:hover { background: var(--notice-hover) }
.notice-btn:active { background: var(--notice-active); transform: translateY(1px) }

/* 반응형 */
@media (max-width: 960px) {
  .headline { width: 100%; padding: 0 16px }
  .profile-card { width: 100%; border-radius: 0; padding: 24px 16px; gap: 24px }
  .profile-pic-area { min-width: 0; flex-direction: column }
  .nickname-input-row { flex-direction: column }
  .dup-btn { width: 100% }
}
</style>
