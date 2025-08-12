<template>
  <div v-if="open" class="onb-overlay">
    <div class="onb-modal">
      <h2 class="onb-title">처음 방문하셨네요!</h2>
      <p class="onb-desc">경로당을 검색 후, 지도 확인 → 선택하여 프로필을 설정하세요.</p>

      <ol class="onb-list">
        <li>검색 유형(이름/주소) 선택</li>
        <li>키워드 입력 후 Enter 또는 Search</li>
        <li>지도에서 위치 확인</li>
        <li>확인 누르면 내 경로당으로 설정</li>
      </ol>

      <div class="onb-actions">
        <button class="onb-btn onb-cancel" @click="close(false)">닫기</button>
        <button class="onb-btn onb-primary" @click="close(true)">알겠어요</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, toRefs } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'confirm'])

const { modelValue } = toRefs(props)
const open = ref(modelValue.value)
const dontShowAgain = ref(true)

watch(modelValue, v => open.value = v)

function close(confirm) {
  emit('confirm', { confirm, dontShowAgain: dontShowAgain.value })
  emit('update:modelValue', false)
}
</script>

<style scoped>
.onb-overlay {
  position: fixed;
  inset: 0;
  background: rgba(40,50,60,0.32);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
}
.onb-modal {
  width: min(560px, 92vw);
  background: #fff;
  border-radius: 16px;
  padding: 28px 26px 22px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.18);
}
.onb-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 8px;
}
.onb-desc {
  color: #5d636f;
  margin-bottom: 14px;
}
.onb-list {
  margin: 0 0 16px;
  padding-left: 22px;
  display: grid;
  gap: 6px;
}
.onb-check {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #333;
}
.onb-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.onb-btn {
  border: 0;
  border-radius: 10px;
  padding: 10px 16px;
  font-weight: 600;
  cursor: pointer;
}
.onb-cancel {
  background: #eef1f4;
}
.onb-primary {
  background: #12795a;
  color: #fff;
}
.onb-primary:hover { background: #0f6148 }
</style>
