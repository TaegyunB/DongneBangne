<template>
    <div class="unity-wrapper">
      <iframe
        ref="unityFrame"
        src="/unity/index.html"
        width="2560"
        height="1440"
        frameborder="0"
        allowfullscreen
      ></iframe>
    </div>
</template>

<style scoped>
.unity-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
}
iframe {
  border: none;
  width: 100%;
  height: 100%;
  max-width: 1920px;
  max-height: 1080px;
}
</style>