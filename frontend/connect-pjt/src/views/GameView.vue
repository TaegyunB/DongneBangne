<!-- src/views/UnityView.vue -->
<template>
  <div class="unity-wrapper">
    <iframe
      src="/WebGLTest_Build/index.html"
      width="1280"
      height="720"
      frameborder="0"
      allowfullscreen
    ></iframe>
  </div>
</template>

<script>
export default {
  name: "UnityView",
};
</script>

<style scoped>
.unity-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
iframe {
  border: none;
}
</style>
