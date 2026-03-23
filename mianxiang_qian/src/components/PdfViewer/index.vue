<template>
  <div class="pdf-viewer-container">
    <div v-if="loading" class="pdf-loading">
      <i class="el-icon-loading"></i>
      <p>正在加载PDF文档...</p>
    </div>
    <div v-else-if="error" class="pdf-error">
      <i class="el-icon-warning"></i>
      <p>{{ error }}</p>
    </div>
    <div v-else class="pdf-wrapper">
      <pdf
        v-for="page in numPages"
        :key="page"
        :src="pdfSrc"
        :page="page"
        style="width: 100%; display: block; margin-bottom: 10px;"
      ></pdf>
    </div>
  </div>
</template>

<script>
import pdf from 'vue-pdf'

export default {
  name: 'PdfViewer',
  components: {
    pdf
  },
  props: {
    // PDF文件地址
    src: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      pdfSrc: '',
      numPages: 0,
      loading: false,
      error: '',
      loadingTask: null // 保存当前的加载任务，用于清理
    }
  },
  watch: {
    src: {
      handler(newVal, oldVal) {
        // 如果URL发生变化，重置状态并重新加载
        if (newVal !== oldVal) {
          // 立即重置状态
          this.resetState()
          // 如果新值存在，延迟加载以确保状态已重置
          if (newVal) {
            // 使用setTimeout确保DOM更新完成
            setTimeout(() => {
              this.loadPdf(newVal)
            }, 50)
          }
        } else if (newVal && !this.pdfSrc) {
          // 如果URL相同但pdfSrc为空，说明需要重新加载
          this.loadPdf(newVal)
        }
      },
      immediate: true
    }
  },
  beforeDestroy() {
    // 组件销毁前清理加载任务
    this.cleanup()
  },
  methods: {
    /** 清理旧的加载任务 */
    cleanup() {
      if (this.loadingTask) {
        try {
          // 尝试销毁旧的加载任务
          if (this.loadingTask.destroy) {
            this.loadingTask.destroy()
          }
        } catch (e) {
          console.warn('清理PDF加载任务失败:', e)
        }
        this.loadingTask = null
      }
    },
    /** 重置状态 */
    resetState() {
      // 先清理旧的加载任务
      this.cleanup()
      this.pdfSrc = ''
      this.numPages = 0
      this.loading = false
      this.error = ''
    },
    loadPdf(url) {
      // 先清理旧的加载任务
      this.cleanup()
      
      // 如果URL为空，直接返回
      if (!url) {
        return
      }
      
      this.loading = true
      this.error = ''
      this.numPages = 0
      this.pdfSrc = ''
      
      // 处理PDF URL
      let pdfUrl = url
      if (!url.startsWith('http://') && !url.startsWith('https://')) {
        const baseUrl = process.env.VUE_APP_BASE_API
        pdfUrl = baseUrl + (url.startsWith('/') ? url : '/' + url)
      }
      
      try {
        // 创建PDF加载任务
        const loadingTask = pdf.createLoadingTask(pdfUrl)
        this.loadingTask = loadingTask
        
        // 获取总页数
        loadingTask.promise.then(pdfDoc => {
          // 检查是否还是当前的加载任务（防止异步回调时已经切换了PDF）
          if (this.loadingTask === loadingTask && this.loading) {
            this.numPages = pdfDoc.numPages
            this.pdfSrc = loadingTask
            this.loading = false
          }
        }).catch(err => {
          // 检查是否还是当前的加载任务
          if (this.loadingTask === loadingTask) {
            console.error('PDF加载失败:', err)
            this.loading = false
            this.error = 'PDF文档加载失败，请检查文件是否存在或网络连接'
            this.pdfSrc = ''
            this.loadingTask = null
          }
        })
      } catch (err) {
        console.error('PDF初始化失败:', err)
        this.loading = false
        this.error = 'PDF文档初始化失败'
        this.pdfSrc = ''
        this.loadingTask = null
      }
    }
  }
}
</script>

<style scoped lang="scss">
.pdf-viewer-container {
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .pdf-loading {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #909399;

    i {
      font-size: 48px;
      margin-bottom: 20px;
      animation: rotating 2s linear infinite;
    }

    p {
      margin: 0;
      font-size: 14px;
    }
  }

  .pdf-error {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #f56c6c;

    i {
      font-size: 48px;
      margin-bottom: 20px;
    }

    p {
      margin: 10px 0;
      font-size: 14px;
    }
  }

  .pdf-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    background: #525252;

    ::v-deep canvas {
      display: block;
      margin: 0 auto 10px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      background: #fff;
    }
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
