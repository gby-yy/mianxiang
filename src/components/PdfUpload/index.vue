<template>
  <div class="pdf-upload-container">
    <el-upload
      :action="uploadPdfUrl"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      :data="data"
      :limit="limit"
      :show-file-list="false"
      accept=".pdf"
      ref="pdfUpload"
    >
      <el-button size="small" type="primary" :disabled="disabled">
        <i class="el-icon-upload"></i> 选择PDF文件
      </el-button>
      <div class="el-upload__tip" slot="tip" v-if="showTip">
        请上传
        <template v-if="fileSize"> 大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b> </template>
        的PDF文件
      </div>
    </el-upload>

    <!-- PDF文件列表 -->
    <div v-if="pdfList.length > 0" class="pdf-list">
      <div v-for="(pdf, index) in pdfList" :key="index" class="pdf-item">
        <div class="pdf-item-content">
          <i class="el-icon-document"></i>
          <span class="pdf-name" :title="getFileName(pdf)">{{ getFileName(pdf) }}</span>
          <div class="pdf-actions">
            <el-button
              type="text"
              size="small"
              @click="handlePreview(pdf)"
            >
              <i class="el-icon-view"></i> 预览
            </el-button>
            <el-button
              v-if="!disabled"
              type="text"
              size="small"
              style="color: #f56c6c;"
              @click="handleDelete(index)"
            >
              <i class="el-icon-delete"></i> 删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- PDF预览对话框 -->
    <el-dialog
      title="PDF预览"
      :visible.sync="previewVisible"
      width="80%"
      append-to-body
      class="pdf-preview-dialog"
      :close-on-click-modal="false"
    >
      <pdf-viewer
        v-if="previewUrl"
        :src="previewUrl"
        :initial-page="1"
        :initial-scale="1"
        style="height: 70vh;"
      />
    </el-dialog>
  </div>
</template>

<script>
import PdfViewer from '@/components/PdfViewer'

export default {
  name: 'PdfUpload',
  components: {
    PdfViewer
  },
  props: {
    // 值（PDF URL，可以是字符串或数组）
    value: {
      type: [String, Array],
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
      default: 5
    },
    // 大小限制(MB)
    fileSize: {
      type: Number,
      default: 50
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
    }
  },
  data() {
    return {
      baseUrl: process.env.VUE_APP_BASE_API,
      uploadPdfUrl: process.env.VUE_APP_BASE_API + this.action,
      pdfList: [],
      previewVisible: false,
      previewUrl: ''
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val) {
          if (Array.isArray(val)) {
            this.pdfList = val
          } else {
            this.pdfList = val.split(',').filter(item => item.trim())
          }
        } else {
          this.pdfList = []
        }
      },
      immediate: true,
      deep: true
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
      const fileExtension = file.name.substring(file.name.lastIndexOf('.') + 1).toLowerCase()
      if (fileExtension !== 'pdf') {
        this.$message.error('请上传PDF文件!')
        return false
      }

      // 验证文件大小
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 < this.fileSize
        if (!isLt) {
          this.$message.error(`上传PDF大小不能超过 ${this.fileSize} MB!`)
          return false
        }
      }

      // 验证文件数量
      if (this.pdfList.length >= this.limit) {
        this.$message.error(`最多只能上传 ${this.limit} 个PDF文件!`)
        return false
      }

      // 验证文件名
      if (file.name.includes(',')) {
        this.$message.error('文件名不正确，不能包含英文逗号!')
        return false
      }

      this.$modal.loading('正在上传PDF，请稍候...')
      return true
    },
    // 上传成功
    handleUploadSuccess(res, file) {
      this.$modal.closeLoading()
      if (res.code === 200) {
        this.pdfList.push(res.data)
        this.emitInput()
        this.$message.success('PDF上传成功')
      } else {
        this.$message.error(res.msg || '上传失败')
        this.$refs.pdfUpload.clearFiles()
      }
    },
    // 上传失败
    handleUploadError() {
      this.$modal.closeLoading()
      this.$message.error('PDF上传失败，请重试')
    },
    // 文件个数超出
    handleExceed() {
      this.$message.error(`上传文件数量不能超过 ${this.limit} 个!`)
    },
    // 删除PDF
    handleDelete(index) {
      this.$confirm('确认删除该PDF文件吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.pdfList.splice(index, 1)
        this.emitInput()
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    // 预览PDF
    handlePreview(pdf) {
      // 直接使用原始URL，PdfViewer组件内部会处理路径
      this.previewUrl = pdf
      this.previewVisible = true
    },
    // 获取文件名
    getFileName(url) {
      if (!url) return ''
      const fileName = url.substring(url.lastIndexOf('/') + 1)
      return fileName.length > 40 ? fileName.substring(0, 40) + '...' : fileName
    },
    // 触发input事件
    emitInput() {
      if (this.limit === 1) {
        // 单个文件，返回字符串
        this.$emit('input', this.pdfList.length > 0 ? this.pdfList[0] : '')
      } else {
        // 多个文件，返回逗号分隔的字符串
        this.$emit('input', this.pdfList.join(','))
      }
    }
  }
}
</script>

<style scoped lang="scss">
.pdf-upload-container {
  .pdf-list {
    margin-top: 15px;

    .pdf-item {
      margin-bottom: 10px;
      padding: 10px;
      background: #f5f7fa;
      border-radius: 4px;
      border: 1px solid #e4e7ed;

      .pdf-item-content {
        display: flex;
        align-items: center;
        justify-content: space-between;

        i {
          font-size: 20px;
          color: #f56c6c;
          margin-right: 10px;
        }

        .pdf-name {
          flex: 1;
          color: #606266;
          font-size: 14px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .pdf-actions {
          margin-left: 10px;
        }
      }
    }
  }
}

::v-deep .pdf-preview-dialog {
  .el-dialog__body {
    padding: 20px;
  }
}
</style>
