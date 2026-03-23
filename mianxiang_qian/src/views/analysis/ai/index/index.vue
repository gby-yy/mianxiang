<template>
  <div class="app-container ai-learning-analysis">
    <div class="analysis-header">
      <h2 class="analysis-title">AI 学情分析</h2>
      <p class="analysis-desc">选择课程后，基于课程基本信息、学习人数与每位学生的学习进度与测试成绩，生成智能学情分析总结</p>
    </div>

    <el-row :gutter="24" class="analysis-body">
      <!-- 左侧：我的课程 -->
      <el-col :xs="24" :sm="24" :md="10" :lg="8" class="left-col">
        <div class="panel course-panel">
          <div class="panel-head">
            <i class="el-icon-notebook-2" />
            <span>我的课程</span>
          </div>
          <div v-loading="coursesLoading" class="panel-body course-list">
            <div
              v-for="c in courseList"
              :key="c.id"
              :class="['course-item', { active: selectedCourseId === c.id }]"
              @click="selectCourse(c)"
            >
              <div class="course-item-cover">
                <img v-if="c.cover" :src="resolveCover(c.cover)" :alt="c.courseName" />
                <span v-else class="cover-placeholder">{{ (c.courseName || '').slice(0, 1) }}</span>
              </div>
              <div class="course-item-info">
                <div class="course-item-name">{{ c.courseName }}</div>
                <div class="course-item-meta">共 {{ c.chapterCount != null ? c.chapterCount : 0 }} 章</div>
              </div>
              <i v-if="selectedCourseId === c.id" class="el-icon-check course-item-check" />
            </div>
            <div v-if="!coursesLoading && !courseList.length" class="empty-tip">暂无课程</div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：学情分析 -->
      <el-col :xs="24" :sm="24" :md="14" :lg="16" class="right-col">
        <div class="panel ai-panel">
          <div class="panel-head">
            <i class="el-icon-cpu" />
            <span>学情分析</span>
            <el-button
              type="primary"
              size="small"
              :loading="analyzing"
              :disabled="!selectedCourseId"
              class="btn-start"
              @click="startAnalysis"
            >
              {{ analyzing ? '分析中…' : '开始分析' }}
            </el-button>
          </div>
          <div class="panel-body ai-body">
            <div v-if="!hasResult && !noDataMessage && !analyzing" class="ai-placeholder">
              <i class="el-icon-document" />
              <p>请从左侧选择一门课程，点击「开始分析」生成学情分析报告</p>
            </div>
            <div v-else-if="noDataMessage" class="ai-no-data">
              <i class="el-icon-warning-outline" />
              <p>{{ noDataMessage }}</p>
            </div>
            <div
              v-else
              ref="aiContent"
              class="ai-content"
              v-html="displayContent"
            />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import MarkdownIt from 'markdown-it'
import { getMyCourses } from '@/api/analysis'
import { getToken } from '@/utils/auth'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  breaks: true
})

/** 渲染前规范化标题语法：补空格并尽量保证标题在行首 */
function normalizeMarkdown(text) {
  if (!text || typeof text !== 'string') return ''
  let s = text.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
  // 标题不在行首时，尽量断到下一行（避免被当成普通文本）
  s = s.replace(/([^\n])\s*(#{1,6})(\s*[^\s#\n])/g, '$1\n$2 $3')
  // 行首标题若写成 ####一、（# 后无空格），自动补一个空格
  s = s.replace(/(^|\n)(#{1,6})([^\s#\n])/g, '$1$2 $3')
  return s
}

export default {
  name: 'AiLearningAnalysis',
  data() {
    return {
      coursesLoading: false,
      courseList: [],
      selectedCourseId: null,
      analyzing: false,
      noDataMessage: '',
      aiContent: '',
      hasResult: false
    }
  },
  computed: {
    displayContent() {
      if (!this.aiContent) return ''
      const normalized = normalizeMarkdown(this.aiContent)
      return md.render(normalized)
    }
  },
  created() {
    this.fetchCourses()
  },
  methods: {
    fetchCourses() {
      this.coursesLoading = true
      getMyCourses()
        .then(res => {
          this.courseList = (res && res.data) ? res.data : []
          if (this.courseList.length && !this.selectedCourseId) {
            this.selectedCourseId = this.courseList[0].id
          }
        })
        .finally(() => {
          this.coursesLoading = false
        })
    },
    selectCourse(course) {
      this.selectedCourseId = course.id
      this.noDataMessage = ''
      this.hasResult = false
      this.aiContent = ''
    },
    startAnalysis() {
      if (!this.selectedCourseId || this.analyzing) return
      this.analyzing = true
      this.noDataMessage = ''
      this.aiContent = ''
      this.hasResult = false

      const baseURL = process.env.VUE_APP_BASE_API || ''
      const url = baseURL.replace(/\/$/, '') + '/analysis/ai-learning/stream'
      const token = getToken()
      fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token || ''
        },
        body: JSON.stringify({ courseId: this.selectedCourseId })
      })
        .then(res => {
          if (!res.ok) {
            return res.json().then(data => {
              throw new Error(data.msg || '请求失败')
            }).catch(() => {
              throw new Error('请求失败')
            })
          }
          return res.body.getReader()
        })
        .then(reader => {
          const decoder = new TextDecoder('utf-8')
          let buffer = ''
          const parseEventBlocks = (raw, isFinal = false) => {
            const normalized = raw.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
            const chunks = normalized.split('\n\n')
            if (!isFinal) {
              buffer = chunks.pop() || ''
            } else {
              buffer = ''
            }
            for (const chunk of chunks) {
              if (!chunk) continue
              const lines = chunk.split('\n')
              const dataLines = []
              for (const line of lines) {
                if (!line.startsWith('data:')) continue
                let payload = line.slice(5)
                if (payload.startsWith(' ')) payload = payload.slice(1)
                dataLines.push(payload)
              }
              if (!dataLines.length) continue
              const data = dataLines.join('\n')
              try {
                const obj = JSON.parse(data)
                if (obj && obj.type === 'no_data' && obj.message) {
                  this.noDataMessage = obj.message
                  this.analyzing = false
                  return true
                }
              } catch (e) { /* 非 JSON，为普通 AI 流式内容 */ }
              this.aiContent += data
              this.hasResult = true
            }
            return false
          }
          const processStream = () => {
            reader.read().then(({ done, value }) => {
              if (done) {
                if (buffer) parseEventBlocks(buffer, true)
                this.analyzing = false
                this.$nextTick(() => this.scrollToBottom())
                return
              }
              buffer += decoder.decode(value, { stream: true })
              const stopped = parseEventBlocks(buffer)
              if (stopped) return
              this.$nextTick(() => this.scrollToBottom())
              return processStream()
            })
          }
          return processStream()
        })
        .catch(err => {
          this.analyzing = false
          this.$message.error(err.message || '分析失败，请重试')
        })
    },
    scrollToBottom() {
      const el = this.$refs.aiContent
      if (el) el.scrollTop = el.scrollHeight
    },
    resolveCover(cover) {
      if (!cover) return ''
      if (typeof cover === 'string' && (cover.startsWith('http://') || cover.startsWith('https://'))) return cover
      const base = process.env.VUE_APP_BASE_API || ''
      return base ? (base.replace(/\/$/, '') + '/' + (cover.replace(/^\//, ''))) : cover
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-learning-analysis {
  padding-bottom: 24px;
}

.analysis-header {
  margin-bottom: 24px;
  .analysis-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px;
  }
  .analysis-desc {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
  overflow: hidden;
}

.panel-head {
  padding: 14px 20px;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  i:first-child {
    font-size: 18px;
    color: #409eff;
  }
  .btn-start {
    margin-left: auto;
  }
}

.panel-body {
  padding: 16px 20px;
}

.ai-body {
  min-height: 400px;
  max-height: 70vh;
  overflow-y: auto;
}

.ai-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  color: #909399;
  i {
    font-size: 48px;
    margin-bottom: 16px;
    opacity: 0.6;
  }
  p {
    margin: 0;
    font-size: 14px;
  }
}

.ai-no-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  color: #e6a23c;
  i {
    font-size: 48px;
    margin-bottom: 16px;
  }
  p {
    margin: 0;
    font-size: 15px;
    max-width: 400px;
    text-align: center;
  }
}

/* Markdown 渲染样式 */
.ai-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  word-break: break-word;

  ::v-deep h1, ::v-deep h2, ::v-deep h3, ::v-deep h4 {
    margin: 1em 0 0.5em;
    font-weight: 600;
    color: #303133;
    line-height: 1.4;
  }
  ::v-deep h1 { font-size: 1.35em; }
  ::v-deep h2 { font-size: 1.2em; border-bottom: 1px solid #ebeef5; padding-bottom: 0.3em; }
  ::v-deep h3 { font-size: 1.1em; }
  ::v-deep h4 { font-size: 1em; }

  ::v-deep p {
    margin: 0.6em 0;
  }

  ::v-deep ul, ::v-deep ol {
    margin: 0.6em 0;
    padding-left: 1.5em;
  }
  ::v-deep li {
    margin: 0.25em 0;
  }

  ::v-deep strong {
    font-weight: 600;
    color: #303133;
  }

  ::v-deep code {
    padding: 0.2em 0.4em;
    font-size: 0.9em;
    background: #f5f7fa;
    border-radius: 4px;
    color: #e83e8c;
  }

  ::v-deep blockquote {
    margin: 0.6em 0;
    padding: 0.5em 1em;
    border-left: 4px solid #409eff;
    background: #ecf5ff;
    color: #606266;
  }

  ::v-deep hr {
    border: none;
    border-top: 1px solid #ebeef5;
    margin: 1em 0;
  }

  ::v-deep a {
    color: #409eff;
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }

  ::v-deep table {
    border-collapse: collapse;
    width: 100%;
    margin: 0.6em 0;
  }
  ::v-deep th, ::v-deep td {
    border: 1px solid #ebeef5;
    padding: 8px 12px;
    text-align: left;
  }
  ::v-deep th {
    background: #fafafa;
    font-weight: 600;
  }
}

.course-panel .panel-body {
  padding: 12px;
  max-height: 70vh;
  overflow-y: auto;
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.course-item {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    border-color: #c0c4cc;
    background: #f5f7fa;
  }
  &.active {
    border-color: #409eff;
    background: #ecf5ff;
  }
}

.course-item-cover {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  .cover-placeholder {
    font-size: 18px;
    font-weight: 600;
    color: #909399;
  }
}

.course-item-info {
  flex: 1;
  min-width: 0;
  margin-left: 12px;
}

.course-item-name {
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-item-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.course-item-check {
  flex-shrink: 0;
  color: #409eff;
  font-size: 18px;
  margin-left: 8px;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 24px;
  font-size: 14px;
}
</style>
