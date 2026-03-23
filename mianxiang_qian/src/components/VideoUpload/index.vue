<template>
  <div class="video-upload-container">
    <el-upload
      :action="uploadVideoUrl"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      :data="data"
      :limit="limit"
      :show-file-list="false"
      accept="video/*"
      ref="videoUpload"
    >
      <el-button size="small" type="primary" :disabled="disabled">
        <i class="el-icon-upload"></i> 选择视频
      </el-button>
      <div class="el-upload__tip" slot="tip" v-if="showTip">
        请上传
        <template v-if="fileSize"> 大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b> </template>
        的视频文件（支持 mp4、avi、mov、wmv 等格式）
      </div>
    </el-upload>

    <!-- 视频预览 -->
    <div v-if="videoUrl" class="video-preview">
      <div class="video-preview-header">
        <span class="video-name">
          <i class="el-icon-video-camera"></i>
          {{ getFileName(videoUrl) }}
        </span>
        <el-button
          v-if="!disabled"
          type="text"
          size="small"
          style="color: #f56c6c;"
          @click="handleDelete"
        >
          <i class="el-icon-delete"></i> 删除
        </el-button>
      </div>
      <video-player :src="videoUrl" :width="previewWidth" :height="previewHeight" />
    </div>
  </div>
</template>

<script>
import VideoPlayer from '@/components/VideoPlayer'

export default {
  name: 'VideoUpload',
  components: {
    VideoPlayer
  },
  props: {
    // 值（视频URL）
    value: {
      type: String,
      default: ''
    },
    // 上传接口地址
    action: {
      type: String,
      default: '/upload/file/url'
    },
    // 上传携带的参数
    data: {
      type: Object
    },
    // 数量限制
    limit: {
      type: Number,
      default: 1
    },
    // 大小限制(MB)
    fileSize: {
      type: Number,
      default: 500
    },
    // 是否显示提示
    isShowTip: {
      type: Boolean,
      default: true
    },
    // 是否禁用
    disabled: {
      type: Boolean,
      default: false
    },
    // 预览宽度
    previewWidth: {
      type: String,
      default: '100%'
    },
    // 预览高度
    previewHeight: {
      type: String,
      default: 'auto'
    }
  },
  data() {
    return {
      baseUrl: process.env.VUE_APP_BASE_API,
      uploadVideoUrl: process.env.VUE_APP_BASE_API + this.action,
      videoUrl: ''
    }
  },
  watch: {
    value: {
      handler(val) {
        this.videoUrl = val || ''
      },
      immediate: true
    }
  },
  computed: {
    showTip() {
      return this.isShowTip && this.fileSize
    }
  },
  methods: {
    // 上传前验证
    handleBeforeUpload(file) {
      // 验证文件类型
      const validTypes = ['video/mp4', 'video/avi', 'video/quicktime', 'video/x-ms-wmv', 'video/webm']
      const fileExtension = file.name.substring(file.name.lastIndexOf('.') + 1).toLowerCase()
      const validExtensions = ['mp4', 'avi', 'mov', 'wmv', 'webm', 'flv', 'mkv']
      
      const isValidType = validTypes.includes(file.type) || validExtensions.includes(fileExtension)
      
      if (!isValidType) {
        this.$message.error('请上传视频文件（支持 mp4、avi、mov、wmv 等格式）!')
        return false
      }

      // 验证文件大小
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 < this.fileSize
        if (!isLt) {
          this.$message.error(`上传视频大小不能超过 ${this.fileSize} MB!`)
          return false
        }
      }

      // 验证文件名
      if (file.name.includes(',')) {
        this.$message.error('文件名不正确，不能包含英文逗号!')
        return false
      }

      this.$modal.loading('正在上传视频，请稍候...')
      return true
    },
    // 上传成功
    handleUploadSuccess(res, file) {
      this.$modal.closeLoading()
      if (res.code === 200) {
        this.videoUrl = res.data
        this.$emit('input', res.data)
        this.$message.success('视频上传成功')
      } else {
        this.$message.error(res.msg || '上传失败')
        this.$refs.videoUpload.clearFiles()
      }
    },
    // 上传失败
    handleUploadError() {
      this.$modal.closeLoading()
      this.$message.error('视频上传失败，请重试')
    },
    // 文件个数超出
    handleExceed() {
      this.$message.error(`上传文件数量不能超过 ${this.limit} 个!`)
    },
    // 删除视频
    handleDelete() {
      this.$confirm('确认删除该视频吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.videoUrl = ''
        this.$emit('input', '')
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    // 获取文件名
    getFileName(url) {
      if (!url) return ''
      const fileName = url.substring(url.lastIndexOf('/') + 1)
      return fileName.length > 30 ? fileName.substring(0, 30) + '...' : fileName
    }
  }
}
</script>

<style scoped lang="scss">
.video-upload-container {
  .video-preview {
    margin-top: 20px;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 4px;

    .video-preview-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;

      .video-name {
        color: #606266;
        font-size: 14px;

        i {
          margin-right: 5px;
          color: #409EFF;
        }
      }
    }
  }
}
</style>
