# 1일차: Vue3-Lifecycle-Hooks

---

## Lifecycle Hooks

---

- 각 Vue 컴포턴트 인스턴스는 생성될 때 일련의 초기화 단계를 거친다
- 이 과정에서 `Lifecycle Hook` 이라고 불리는 함수들이 실행되며, 사용자는 특정 단계에서 자신만의 코드를 추가할 수 있다.

## Lifecycle Diagram

---

![image.png](image.png)

## Lifecycle Hook API References

---

### `onBeforeMount()`

- 컴포넌트가 마운트되기 직전에 호출될 콜백 함수를 등록한다.
- 아직 DOM 요소가 생성되지 않아, 시각적 요소는 존재하지 않는다.

```jsx
function onBeforeMount(callback: () => void, target?: ComponentInternalInstance | null): void
```

### `onMounted()`

- 컴포넌트가 DOM에 연결된 후 호출될 콜백을 등록한다.
- DOM에 연결되었기에, `ref` 를 통해 `element` 에 접근할 수 있다.

```jsx
<script setup>
import { ref, onMounted } from 'vue'

const el = ref()

onMounted(() => {
  el.value // <div>
})
</script>

<template>
  <div ref="el"></div>
</template>
```

### `onBeforeUpdate()`

- 컴포넌트의 DOM이 업데이트 되기 직전에 호출될 콜백을 등록한다.

### `onUpdated()`

- 컴포넌트의 DOM이 업데이트 된 후 호출될 콜백을 등록한다.

```jsx
<script setup>
import { ref, onUpdated } from 'vue'

const count = ref(0)

onUpdated(() => {
  // 텍스트 내용이 현재 `count.value`와 같아야 합니다.
  console.log(document.getElementById('count').textContent)
})
</script>

<template>
  <button id="count" @click="count++">{{ count }}</button>
</template>
```

### `onUnmounted()`

- 컴포넌트가 언마운트 된 후 호출될 콜백을 등록합니다

```jsx
<script setup>
import { onMounted, onUnmounted } from 'vue'

let intervalId
onMounted(() => {
  intervalId = setInterval(() => {
    // ...
  })
})

onUnmounted(() => clearInterval(intervalId))
</script>
```

---

## 실습

### 예제 코드

```jsx
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
  console.log('onBeforeUpdate - DOM 업데이트 직전')
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
```

### 실습 결과

- 컴포넌트가 마운트되면 다음과 같이 출력됩니다.
    
    ![image.png](image%201.png)
    

- 좌측의 `Increment` 버튼을 클릭하여 DOM이 업데이트되면, 다음과 같이 출력됩니다.
    
    ![image.png](image%202.png)
    

- 컴포넌트를 언마운트하면 다음과 같이 출력됩니다.
    
    ![image.png](image%203.png)