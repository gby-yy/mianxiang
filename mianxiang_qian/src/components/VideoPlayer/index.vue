<template>
  <div class="video-player-container">
    <video
      v-if="videoUrl"
      ref="videoEl"
      :src="videoUrl"
      controls
      :poster="poster"
      :width="width"
      :height="height"
      class="video-player"
      preload="metadata"
      :autoplay="autoplay"
      @loadeddata="onLoadedData"
    >
      您的浏览器不支持视频播放
    </video>
    <div v-else class="video-placeholder">
      <i class="el-icon-video-camera"></i>
      <p>暂无视频</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'VideoPlayer',
  props: {
    // 视频地址
    src: {
      type: String,
      default: ''
    },
    // 视频封面
    poster: {
      type: String,
      default: ''
    },
    // 宽度
    width: {
      type: String,
      default: '100%'
    },
    // 高度
    height: {
      type: String,
      default: 'auto'
    },
    // 是否自动播放（如从封面点击进入时）
    autoplay: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    videoUrl() {
      if (!this.src) return ''
      // 如果是完整URL，直接返回
      if (this.src.startsWith('http://') || this.src.startsWith('https://')) {
        return this.src
      }
      // 如果是相对路径，拼接baseUrl
      const baseUrl = process.env.VUE_APP_BASE_API
      return baseUrl + (this.src.startsWith('/') ? this.src : '/' + this.src)
    }
  },
  mounted() {
    if (this.autoplay && this.$refs.videoEl) {
      this.$nextTick(() => {
        this.$refs.videoEl.play().catch(() => {})
      })
    }
  },
  methods: {
    onLoadedData() {
      if (this.autoplay && this.$refs.videoEl) {
        this.$refs.videoEl.play().catch(() => {})
      }
    }
  }
}
</script>

<style scoped lang="scss">
.video-player-container {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;

  .video-player {
    max-width: 100%;
    background: #000;
    border-radius: 4px;
  }

  .video-placeholder {
    width: 100%;
    min-height: 200px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: #f5f7fa;
    border: 1px dashed #dcdfe6;
    border-radius: 4px;
    color: #909399;

    i {
      font-size: 48px;
      margin-bottom: 10px;
    }

    p {
      margin: 0;
      font-size: 14px;
    }
  }
}
</style>
