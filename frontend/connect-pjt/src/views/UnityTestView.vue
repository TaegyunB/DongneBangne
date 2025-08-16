<template>
  <div class="unity-wrapper">
    <div class="col-lg-6">
      <iframe 
        width="560" 
        height="315" 
        src="https://www.youtube.com/embed/reQgeI10TAo?si=xpImqgf1KQIsoydt" 
        title="YouTube video player" 
        frameborder="0" 
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
        allowfullscreen
      ></iframe>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UnityTestView'
}
</script>

<style scoped>
.unity-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
}

.col-lg-6 {
  max-width: 600px;
  width: 100%;
}

iframe {
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
</style>
