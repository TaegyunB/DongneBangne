<template>
  <div class="unity-wrapper">
    <button @click="sendToUnity">Vue → Unity</button>
    <iframe
      ref="unityFrame"
      src="/unity/index.html"
      width="1280"
      height="720"
      frameborder="0"
      allowfullscreen
    ></iframe>
  </div>
</template>

<script>
export default {mounted() {
    // Unity가 보낸 메시지 수신
    window.addEventListener("message", (event) => {
      console.log("✅ Unity → Vue 메시지:", event.data);
    });
  },
  methods: {
    sendToUnity() {
      const unityFrame = this.$refs.unityFrame;
      unityFrame.contentWindow.postMessage("Hello from Vue!", "*");
    }
  },
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