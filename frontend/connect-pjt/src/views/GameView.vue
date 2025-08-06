<template>
  <div class="unity-wrapper">
    <button @click="startScreenShare">화면 공유 버튼</button>
    <iframe
      ref="unityFrame"
      src="/unity/index.html"
      width="1280"
      height="720"
      frameborder="0"
      allowfullscreen
    ></iframe>
  </div>
  <div class="localCamera">
    <video ref="localVideo" id="localVideo" autoplay playsinline muted></video>
  </div>
  <canvas id="canvas" style="display: none;"></canvas>
</template>

<script>
export default {
  data() {
    return {
      localStream: null     // LocalMedia
    }
  },
  mounted() {
    // Unity가 보낸 메시지 수신
    window.addEventListener("message", (event) => {
      console.log("✅ Unity → Vue 메시지:", event.data);
    });
  },
  methods: {
    sendToUnity() {
      const unityFrame = this.$refs.unityFrame;
      unityFrame.contentWindow.postMessage("Hello from Vue!", "*");
    },
    async startScreenShare(){
      const screenStream = await navigator.mediaDevices.getUserMedia({
        video: true
      });

      const micStream = await navigator.mediaDevices.getUserMedia({
        audio:true
      });

      this.localStream = new MediaStream([
        ...screenStream.getVideoTracks(),
        ...micStream.getAudioTracks(),
      ]);
      this.$refs.localVideo.srcObject = this.localStream;

      // 원본 해상도 얻기
      const track = screenStream.getVideoTracks()[0];
      const {width, height} = track.getSettings();

      // 반복 전송
      setInterval(this.sendVideoFrameToUnity, 1000/30);
    },
    sendVideoFrameToUnity(){
      const video = document.getElementById('localVideo');
      const canvas = document.getElementById('canvas');
      const ctx = canvas.getContext('2d');
      
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;

      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

      const imageData = canvas.toDataURL('image/jpeg');

      const unityFrame = this.$refs.unityFrame;
      unityFrame.contentWindow.postMessage(JSON.stringify({
        type: 'video-frame',
        data: imageData
      }), "*");
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
.localCamera {
  position: absolute;
  width: 1px;
  height: 1px;
  bottom: 0px;
  right: 0px;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}
</style>