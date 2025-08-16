<template>
  <div>
    <div class="controls">
      <input 
        v-model="state.videoId" 
        placeholder="YouTube 비디오 ID를 입력하세요"
        @keyup.enter="changeVideo"
        class="video-input"
      />
      <button @click="changeVideo" class="change-btn">비디오 변경</button>
    </div>
    
    <div id="player"></div>
    
    <div class="playback-controls">
      <button @click="playVideo" class="play-btn">재생</button>
      <button @click="pauseVideo" class="pause-btn">일시정지</button>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, reactive, ref, onMounted } from "vue";

declare global {
  interface Window {
    YT: any;
    onYouTubeIframeAPIReady: () => void;
  }
}

export default defineComponent({
  setup() {
    const state = reactive({
      videoId: 'pkc1XoilQIc',
    });
    
    const youtubeIframe = ref<HTMLIFrameElement | null>(null);
    let player: any = null;
    let isPlayerReady = false;

    const changeVideo = () => {
      if (state.videoId && player && isPlayerReady) {
        player.loadVideoById(state.videoId);
      } else {
        const iframe = youtubeIframe.value;
        if (iframe) {
          iframe.src = `https://youtube.com/embed/${state.videoId}?si=8IsRoXmN3OS1AwUH&enablejsapi=1`;
        }
      }
    };

    const playVideo = () => {
      if (player && isPlayerReady) {
        player.playVideo();
      } else {
        const iframe = youtubeIframe.value;
        if (iframe) {
          try {
            iframe.contentWindow?.postMessage('{"event":"command","func":"playVideo","args":""}', '*');
          } catch (error) {
            console.log('재생 명령 전송 중 오류:', error);
          }
        }
      }
    };

    const pauseVideo = () => {
      if (player && isPlayerReady) {
        player.pauseVideo();
      } else {
        const iframe = youtubeIframe.value;
        if (iframe) {
          try {
            iframe.contentWindow?.postMessage('{"event":"command","func":"pauseVideo","args":""}', '*');
          } catch (error) {
            console.log('일시정지 명령 전송 중 오류:', error);
          }
        }
      }
    };

    onMounted(() => {
      // YouTube iframe API 로드
      const tag = document.createElement('script');
      tag.src = 'https://www.youtube.com/iframe_api';
      const firstScriptTag = document.getElementsByTagName('script')[0];
      firstScriptTag.parentNode?.insertBefore(tag, firstScriptTag);

      // YouTube API 준비되면 player 초기화
      window.onYouTubeIframeAPIReady = () => {
        player = new window.YT.Player('player', {
          height: '500',
          width: '500',
          videoId: state.videoId,
          playerVars: {
            'enablejsapi': 1,
            'autoplay': 0,
            'controls': 1,
          },
          events: {
            'onReady': (event: any) => {
              isPlayerReady = true;
              console.log('YouTube Player 준비 완료');
            },
            'onStateChange': (event: any) => {
              console.log('Player 상태 변경:', event.data);
            }
          }
        });
      };
    });

    return {
      state,
      youtubeIframe,
      changeVideo,
      playVideo,
      pauseVideo,
    };
  },
});
</script>

<style scoped>
.controls {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.video-input {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  width: 250px;
}

.change-btn {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.change-btn:hover {
  background-color: #0056b3;
}

#player {
  margin-bottom: 20px;
  text-align: center;
}

.playback-controls {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.play-btn, .pause-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
}

.play-btn {
  background-color: #28a745;
  color: white;
}

.play-btn:hover {
  background-color: #218838;
}

.pause-btn {
  background-color: #dc3545;
  color: white;
}

.pause-btn:hover {
  background-color: #c82333;
}
</style>