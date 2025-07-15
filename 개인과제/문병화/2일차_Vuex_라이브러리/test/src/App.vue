<template>
  <div style="text-align: center; padding: 2rem;">
    <h1>Vuex 숫자 증가 예제</h1>
    <p>현재 숫자: {{ count }}</p>
    <p>2배 숫자: {{ double }}</p>
    <button @click="increment">+1</button>
    <button @click="delayedIncrement">0.5초 후 +1</button>
  </div>
</template>

<script setup>
import { useStore } from 'vuex'
import { computed } from 'vue'

const store = useStore()
const count = computed(() => store.state.count)
const double = computed(() => store.getters.double)

const increment = () => store.commit('increment')
const delayedIncrement = () => store.dispatch('delayedIncrement')
</script>
