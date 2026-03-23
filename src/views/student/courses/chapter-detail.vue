<template>
  <div class="chapter-detail-page">
    <div class="page-header">
      <el-button icon="el-icon-arrow-left" type="text" class="back-btn" @click="goBack">返回章节列表</el-button>
      <h1 class="page-title">{{ chapterName || '章节学习' }}</h1>
      <p class="page-desc">
        按顺序学习以下内容
        <span v-if="contentList.length" class="chapter-progress">本章进度：{{ chapterProgressText }}</span>
      </p>
    </div>

    <div v-loading="loading" class="content-by-type">
      <template v-if="contentList.length">
        <!-- 视频分组：一行两个 -->
        <section v-if="videoList.length" class="content-section">
          <h2 class="section-title"><i class="el-icon-video-camera" /> 视频</h2>
          <div class="content-grid">
            <div
              v-for="(item, index) in videoList"
              :key="item.id"
              class="content-card"
            >
              <div class="content-head">
                <span class="content-index">{{ index + 1 }}</span>
                <span class="content-title">{{ item.contentTitle }}</span>
                <span v-if="isContentLearned(item.id)" class="learned-badge"><i class="el-icon-check" /> 已学习</span>
                <el-button
                  v-else
                  type="primary"
                  size="mini"
                  :loading="submittingContentId === item.id"
                  class="finish-learn-btn"
                  @click.stop="markContentLearned(item)"
                >完成学习</el-button>
              </div>
              <div class="content-body">
                <template v-if="item.videoUrl">
                  <div v-if="item.coverImage" class="media-cover-wrap aspect-4-3" @click="openVideoDialog(item)">
                    <img :src="fullUrl(item.coverImage)" class="media-cover-img" alt="封面">
                    <div class="media-cover-mask">
                      <i class="el-icon-video-play play-icon" />
                      <span class="play-text">点击播放</span>
                    </div>
                  </div>
                  <div v-else class="video-placeholder aspect-4-3" @click="openVideoDialog(item)">
                    <div class="video-placeholder-inner">
                      <i class="el-icon-video-play" />
                      <span>点击播放视频</span>
                    </div>
                  </div>
                </template>
                <p v-else class="no-media">暂无视频地址</p>
              </div>
            </div>
          </div>
        </section>

        <!-- 文档分组：一行两个 -->
        <section v-if="docList.length" class="content-section">
          <h2 class="section-title"><i class="el-icon-document" /> 文档</h2>
          <div class="content-grid">
            <div
              v-for="(item, index) in docList"
              :key="item.id"
              class="content-card"
            >
              <div class="content-head">
                <span class="content-index">{{ index + 1 }}</span>
                <span class="content-title">{{ item.contentTitle }}</span>
                <span v-if="isContentLearned(item.id)" class="learned-badge"><i class="el-icon-check" /> 已学习</span>
                <el-button
                  v-else
                  type="primary"
                  size="mini"
                  :loading="submittingContentId === item.id"
                  class="finish-learn-btn"
                  @click.stop="markContentLearned(item)"
                >完成学习</el-button>
              </div>
              <div class="content-body">
                <template v-if="item.docUrl">
                  <div v-if="item.coverImage" class="media-cover-wrap doc-cover aspect-4-3" @click="openPdfPreview(item)">
                    <img :src="fullUrl(item.coverImage)" class="media-cover-img" alt="封面">
                    <div class="media-cover-mask">
                      <i class="el-icon-document preview-icon" />
                      <span class="play-text">点击预览</span>
                    </div>
                  </div>
                  <div v-else class="doc-placeholder" @click="openPdfPreview(item)">
                    <i class="el-icon-document" />
                    <span>点击预览文档</span>
                  </div>
                  <div class="doc-link-row">
                    <a :href="fullUrl(item.docUrl)" target="_blank" rel="noopener" class="doc-link">新窗口打开文档</a>
                  </div>
                </template>
                <p v-else class="no-media">暂无文档地址</p>
              </div>
            </div>
          </div>
        </section>
      </template>
      <div v-else-if="!loading" class="empty-tip">
        <i class="el-icon-document-delete" />
        <p>本章节暂无学习内容</p>
      </div>
    </div>

    <!-- 视频播放弹框 -->
    <el-dialog
      :title="currentVideoTitle || '视频播放'"
      :visible.sync="videoDialogOpen"
      width="40%"
      top="5vh"
      append-to-body
      class="video-play-dialog"
      @close="onVideoDialogClose"
    >
      <video-player
        v-if="currentVideoUrl"
        ref="dialogVideoPlayer"
        :src="currentVideoUrl"
        :poster="currentVideoPoster"
        width="100%"
        height="400px"
      />
    </el-dialog>

    <!-- PDF 预览弹框（与视频播放弹框同宽） -->
    <el-dialog
      title="文档预览"
      :visible.sync="pdfPreviewOpen"
      width="40%"
      top="5vh"
      append-to-body
      class="pdf-preview-dialog"
      @close="pdfPreviewUrl = ''"
    >
      <pdf-viewer v-if="pdfPreviewUrl" :src="pdfPreviewUrl" :key="pdfPreviewUrl" />
    </el-dialog>

    <!-- 悬浮 AI 学习按钮 -->
    <div class="ai-float-btn" @click="aiChatVisible = true">
      <i class="el-icon-chat-dot-round" />
      <span class="ai-float-label">AI 学习</span>
    </div>

    <!-- AI 学习助手对话弹框（流式输出） -->
    <ai-chat-dialog
      :value="aiChatVisible"
      :chapter-context="chapterName"
      @input="aiChatVisible = $event"
      @close="aiChatVisible = false"
    />
  </div>
</template>

<script>
import { getChapterContents, getCourseLearnInfo, reportContentProgress } from '@/api/student'
import VideoPlayer from '@/components/VideoPlayer'
import PdfViewer from '@/components/PdfViewer'
import AiChatDialog from '@/components/AiChatDialog'

export default {
  name: 'StudentChapterDetail',
  components: { VideoPlayer, PdfViewer, AiChatDialog },
  data() {
    return {
      courseId: null,
      chapterId: null,
      chapterName: '',
      loading: false,
      contentList: [],
      /** 内容学习进度：{ [contentId]: 1 } 表示已学习，用于回显与本章进度计算 */
      contentProgressMap: {},
      /** 正在上报完成学习的 contentId，用于按钮 loading */
      submittingContentId: null,
      videoDialogOpen: false,
      currentVideoUrl: '',
      currentVideoPoster: '',
      currentVideoTitle: '',
      pdfPreviewOpen: false,
      pdfPreviewUrl: '',
      /** AI 学习助手弹框 */
      aiChatVisible: false
    }
  },
  computed: {
    videoList() {
      return (this.contentList || []).filter(item => item.contentType === 1)
    },
    docList() {
      return (this.contentList || []).filter(item => item.contentType === 2)
    },
    chapterProgressText() {
      const list = this.contentList || []
      if (list.length === 0) return ''
      const completed = list.filter(item => this.isContentLearned(item.id)).length
      return `${completed}/${list.length} 已学`
    }
  },
  created() {
    this.courseId = this.$route.params.courseId ? Number(this.$route.params.courseId) : null
    this.chapterId = this.$route.params.chapterId ? Number(this.$route.params.chapterId) : null
    this.chapterName = this.$route.query.chapterName || ''
    if (this.courseId && this.chapterId) {
      this.fetchLearnProgress()
      this.fetchContents()
    } else {
      this.$message.error('参数错误')
      this.goBack()
    }
  },
  methods: {
    /** 拉取课程学习记录中的 contentProgress，用于回显已学状态 */
    fetchLearnProgress() {
      if (!this.courseId) return
      getCourseLearnInfo(this.courseId).then(res => {
        const record = (res.data && res.data.record) ? res.data.record : null
        if (record && record.contentProgress) {
          try {
            const raw = record.contentProgress
            this.contentProgressMap = typeof raw === 'string' ? JSON.parse(raw || '{}') : (raw || {})
          } catch (e) {
            this.contentProgressMap = {}
          }
        } else {
          this.contentProgressMap = {}
        }
      }).catch(() => {
        this.contentProgressMap = {}
      })
    },
    isContentLearned(contentId) {
      return this.contentProgressMap[String(contentId)] === 1
    },
    /** 点击「完成学习」：上报并更新本地进度 */
    markContentLearned(item) {
      if (!this.courseId || !item || !item.id) return
      this.submittingContentId = item.id
      reportContentProgress(this.courseId, { contentId: item.id })
        .then(res => {
          const record = res.data || {}
          if (record.contentProgress) {
            try {
              const raw = record.contentProgress
              this.contentProgressMap = typeof raw === 'string' ? JSON.parse(raw || '{}') : (raw || {})
            } catch (e) {
              this.$set(this.contentProgressMap, String(item.id), 1)
            }
          } else {
            this.$set(this.contentProgressMap, String(item.id), 1)
          }
          this.$message.success('已记录学习进度')
        })
        .catch(() => {
          this.$message.error('保存学习记录失败，请重试')
        })
        .finally(() => {
          this.submittingContentId = null
        })
    },
    fullUrl(url) {
      if (!url || typeof url !== 'string') return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url
      const base = process.env.VUE_APP_BASE_API || ''
      return base.replace(/\/$/, '') + (url.startsWith('/') ? url : '/' + url)
    },
    goBack() {
      this.$router.push({
        path: `/student/courses/learn/${this.courseId}`,
        query: this.$route.query.courseName ? { courseName: this.$route.query.courseName } : {}
      })
    },
    fetchContents() {
      this.loading = true
      getChapterContents(this.courseId, this.chapterId).then(res => {
        this.contentList = (res.data || []).slice()
      }).finally(() => {
        this.loading = false
      })
    },
    /** 打开视频播放弹框 */
    openVideoDialog(item) {
      this.currentVideoUrl = this.fullUrl(item.videoUrl)
      this.currentVideoPoster = item.coverImage ? this.fullUrl(item.coverImage) : ''
      this.currentVideoTitle = item.contentTitle || '视频播放'
      this.videoDialogOpen = true
      this.$nextTick(() => {
        if (this.$refs.dialogVideoPlayer && this.$refs.dialogVideoPlayer.$refs.videoEl) {
          this.$refs.dialogVideoPlayer.$refs.videoEl.play().catch(() => {})
        }
      })
    },
    onVideoDialogClose() {
      this.currentVideoUrl = ''
      this.currentVideoPoster = ''
      this.currentVideoTitle = ''
      if (this.$refs.dialogVideoPlayer && this.$refs.dialogVideoPlayer.$refs.videoEl) {
        this.$refs.dialogVideoPlayer.$refs.videoEl.pause()
      }
    },
    /** 打开 PDF 预览弹框 */
    openPdfPreview(item) {
      this.pdfPreviewUrl = this.fullUrl(item.docUrl)
      this.pdfPreviewOpen = true
    }
  }
}
</script>

<style lang="scss" scoped>
.chapter-detail-page {
  padding-bottom: 32px;
}

.back-btn {
  margin-bottom: 12px;
  font-size: 14px;
  color: #6b7280;
  &:hover { color: #1677ff; }
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    font-size: 22px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0 0 8px;
  }

  .page-desc {
    font-size: 14px;
    color: #6b7280;
    margin: 0;

    .chapter-progress {
      margin-left: 16px;
      color: #1677ff;
      font-weight: 500;
    }
  }
}

.content-by-type {
  min-height: 120px;
}

.content-section {
  margin-bottom: 32px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;

  i {
    font-size: 20px;
    color: #1677ff;
  }
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;

  .content-card .aspect-4-3,
  .content-card .media-cover-wrap {
    max-width: none;
  }

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.content-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.content-head {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #fafafa;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  gap: 12px;

  .content-index {
    width: 32px;
    height: 32px;
    line-height: 32px;
    text-align: center;
    border-radius: 8px;
    background: #e6f4ff;
    color: #1677ff;
    font-weight: 600;
    flex-shrink: 0;
  }

  .content-title {
    flex: 1;
    font-size: 16px;
    font-weight: 600;
    color: #1a1a2e;
    min-width: 0;
  }

  .learned-badge {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 12px;
    color: #52c41a;
    background: #f0f9eb;
    border: 1px solid #b7eb8f;
  }

  .finish-learn-btn {
    flex-shrink: 0;
  }

  .content-type-tag {
    flex-shrink: 0;
  }
}

.content-body {
  padding: 20px;
}

/* 4:3 比例容器（封面与视频学习框统一 4:3） */
.aspect-4-3 {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  padding-bottom: 75%; /* 4:3 */
  height: 0;
  overflow: hidden;
  border-radius: 8px;

  .media-cover-img {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .video-inner {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
  }

  .video-placeholder-inner {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #909399;
    cursor: pointer;
    background: #f5f7fa;
    border-radius: 8px;
    transition: background 0.2s, color 0.2s;

    i {
      font-size: 56px;
      margin-bottom: 8px;
    }

    span {
      font-size: 14px;
    }
  }
}

.video-placeholder.aspect-4-3:hover .video-placeholder-inner {
  background: #e6f4ff;
  color: #1677ff;
}

/* 有封面时的封面+播放/预览入口 */
.media-cover-wrap {
  position: relative;
  max-width: 800px;
  margin: 0 auto 0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: #000;

  &:not(.aspect-4-3) .media-cover-img {
    width: 100%;
    display: block;
    max-height: 400px;
    object-fit: contain;
  }

  .media-cover-mask {
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.35);
    color: #fff;
    transition: background 0.2s;

    .play-icon,
    .preview-icon {
      font-size: 64px;
      margin-bottom: 8px;
      opacity: 0.95;
    }

    .preview-icon {
      font-size: 56px;
    }

    .play-text {
      font-size: 14px;
    }
  }

  &:hover .media-cover-mask {
    background: rgba(0, 0, 0, 0.5);
  }
}

.doc-cover {
  margin-bottom: 12px;
}

.doc-placeholder {
  max-width: 800px;
  margin: 0 auto 12px;
  height: 200px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;

  i {
    font-size: 48px;
    margin-bottom: 8px;
  }

  &:hover {
    border-color: #1677ff;
    color: #1677ff;
  }
}

.media-wrap {
  &.video-wrap {
    max-width: 800px;
    margin: 0 auto;

    &.aspect-4-3 .video-inner {
      ::v-deep .video-player-container,
      ::v-deep .video-player {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }
  }

  &.doc-wrap {
    .doc-link-row {
      margin-top: 0;
      margin-bottom: 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}

.doc-link-row {
  margin-top: 8px;
  margin-bottom: 0;
}

.doc-link {
  color: #1677ff;
  text-decoration: none;
  font-size: 14px;
  &:hover { text-decoration: underline; }
}

.no-media {
  margin: 0;
  color: #9ca3af;
  font-size: 14px;
}

.empty-tip {
  text-align: center;
  padding: 60px 24px;
  color: #9ca3af;

  i {
    font-size: 56px;
    margin-bottom: 16px;
    display: block;
  }
  p {
    margin: 0;
    font-size: 14px;
  }
}

::v-deep .video-play-dialog {
  .el-dialog__body {
    padding: 16px;
  }
}

::v-deep .pdf-preview-dialog {
  .el-dialog__body {
    max-height: 80vh;
    overflow-y: auto;
  }
}

/* 悬浮 AI 学习按钮：内容区域右侧、页面垂直居中 */
.ai-float-btn {
  position: fixed;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 999;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #fff;
  border-radius: 50px;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  font-size: 15px;
  font-weight: 600;
  i {
    font-size: 20px;
  }
  .ai-float-label {
    letter-spacing: 0.5px;
  }
  &:hover {
    transform: translateY(calc(-50% - 2px));
    box-shadow: 0 12px 32px rgba(99, 102, 241, 0.45);
  }
}
</style>
