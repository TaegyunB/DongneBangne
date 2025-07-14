<template>
  <div>
    <h2>Lifecycle Example</h2>
    <p>Count: {{ count }}</p>
    <button @click="count++">Increment</button>
  </div>
</template>

<script setup>
import { ref, onBeforeMount, onMounted, onBeforeUpdate, onUpdated, onBeforeUnmount, onUnmounted } from 'vue'

const count = ref(0)

onBeforeMount(() => {
  console.log('onBeforeMount - DOM에 붙기 직전')
})

onMounted(() => {
  console.log('onMounted - DOM에 마운트 완료')
})

onBeforeUpdate(() => {
  console.log('onBeforeUpdate - DOM 업데이트 직전1')
})

onUpdated(() => {
  console.log('onUpdated - DOM 업데이트 완료')
})

onBeforeUnmount(() => {
  console.log('onBeforeUnmount - 컴포넌트 언마운트 직전')
})

onUnmounted(() => {
  console.log('onUnmounted - 컴포넌트 언마운트 완료')
})
</script>