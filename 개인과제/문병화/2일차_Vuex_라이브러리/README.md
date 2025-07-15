# 2일차: Vuex 라이브러리

---

## Vuex란?

---

![image.png](image.png)

- Vue.js 어플리케이션에 대한 상태 관리 패턴 + 라이브러리
    
    다시 말해, Vue 어플리케이션의 상태를 중앙에서 관리하기 위한 라이브러리이다.
    
- 공통의 상태를 공유하는 여러 컴포넌트가 있는 경우 이 상태를 컴포넌트를 추출하여 전역 싱글톤으로 관리해야 한다.
    
    이를 통해 컴포넌트 트리가 커다란 ‘뷰’가 되며 모든 컴포넌트는 트리에 상관없이 상태에 엑세스하거나 동작을 트리거 할 수 있어야 한다.
    
- 공유된 상태 관리를 처리하는 데 유용하지만, 개념에 대한 이해와 시작되는 비용도 함께 든다.
    
    앱이 단순하다면 Vuex 없이도 괜찮지만, 중대형 규모의 SPA를 구축하는 경우 외부의 상태를 잘 처리하기 위해 Vuex를 선택하는 것이 좋다.
    

## 예제 코드

다음 예제코드는 ‘1 더하는 버튼’과, ‘0.5초 이후 1 더하는 버튼’ 두개가 존재하는 페이지를 만든다.

---

### `store/index.js`

```jsx
// store/index.js
import { createStore } from 'vuex'

export default createStore({
  state() {
    return {
      count: 0
    }
  },
  getters: {
    double(state) {
      return state.count * 2
    }
  },
  mutations: {
    increment(state) {
      state.count++
    }
  },
  actions: {
    delayedIncrement({ commit }) {
      setTimeout(() => {
        commit('increment')
      }, 500)
    }
  }
})

```

### `App.vue`

```jsx
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

```

- 이 코드에서 표시되는 데이터는 현재 숫자 `count` 와, 이 두배 숫자의 `double` 이다.
    
    이 데이터들은 `App.vue` 에 저장되는 것이 아닌, `store` 에서 불러오는 것을 확인할 수 있다. 
    
- 각 버튼에 연결된 함수는 1을 더해주는 `increment` 와, 0.5초 후 1을 더해주는 `delaayedIncrement` 모두, `App.vue` 에서 처리하는 것이 아닌, `store` 에서 처리하는 것을 볼 수 있다.