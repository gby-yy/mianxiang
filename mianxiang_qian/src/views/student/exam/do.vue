<template>
  <div class="exam-do-page">
    <div class="exam-header">
      <el-button icon="el-icon-arrow-left" type="text" class="back-btn" @click="goBack">返回</el-button>
      <h1 class="exam-title">章节测试</h1>
      <p class="exam-desc">
        <span v-if="paper">{{ paper.paperName }}</span>
        <span v-if="chapterName" class="course-tag"> · {{ chapterName }}</span>
      </p>
    </div>

    <!-- 阅卷中全屏遮罩 -->
    <div v-if="gradingOverlay" class="grading-overlay">
      <div class="grading-content">
        <i class="el-icon-loading grading-icon" />
        <p class="grading-text">正在阅卷，请稍候…</p>
      </div>
    </div>

    <div v-loading="loading" class="exam-layout">
      <template v-if="!paper && !loading">
        <p class="empty-tip">试卷不存在或未启用</p>
      </template>
      <template v-else-if="paper && questions.length">
        <aside class="exam-sidebar">
          <div class="sidebar-timer" :class="{ overtime: timeLeft <= 0 }">
            <span class="timer-label">剩余时间</span>
            <span class="timer-value">{{ formatTime(timeLeft) }}</span>
          </div>
          <div class="sidebar-title">答题卡</div>
          <div class="question-dots">
            <span
              v-for="(q, idx) in questions"
              :key="q.questionId"
              :class="['dot', { answered: hasAnswer(q), active: currentScrollId === q.questionId }]"
              @click="scrollToQuestion(q.questionId)"
            >
              {{ idx + 1 }}
            </span>
          </div>
        </aside>

        <main class="exam-main">
          <div class="exam-info-bar">
            <span>共 {{ questions.length }} 题</span>
            <span>总分 {{ paper.totalScore }}</span>
            <span>考试时长 {{ paper.durationMinutes }} 分钟</span>
          </div>

          <div ref="questionsContainer" class="questions-container">
            <section
              v-for="(q, index) in questions"
              :key="q.questionId"
              class="question-block"
              :data-question-id="q.questionId"
            >
              <div class="question-meta">
                <span class="question-type-badge">{{ questionTypeLabel(q.questionType) }}</span>
                <span class="question-index">第 {{ index + 1 }} 题</span>
              </div>
              <h3 class="question-title">{{ q.questionTitle }}</h3>

              <!-- 单选、多选：共用选项列表，用 type 区分选中与点击逻辑 -->
              <div
                v-if="questionTypeNum(q) === 1 || questionTypeNum(q) === 2"
                :class="['option-list', { 'checkbox-list': questionTypeNum(q) === 2, disabled: isExamEnded }]"
              >
                <div
                  v-for="(opt, optIdx) in getQuestionOptions(q)"
                  :key="String(q.questionId) + '-' + (opt.id || optIdx)"
                  :class="['option-item', { selected: isOptionSelected(q, opt) }]"
                  @click="!isExamEnded && onOptionClick(q, opt)"
                >
                  <span class="option-key">{{ opt.optionKey || opt.optionLabel || opt.option_key || ['A','B','C','D','E','F'][optIdx] }}.</span>
                  <span class="option-content">{{ opt.optionContent || opt.option_content || '' }}</span>
                </div>
                <p v-if="(questionTypeNum(q) === 2 || questionTypeNum(q) === 1) && getQuestionOptions(q).length === 0" class="option-empty-tip">该题暂无选项数据，请检查题库中是否已配置选项。</p>
              </div>
              <!-- 判断题 -->
              <div v-if="questionTypeNum(q) === 3" class="judge-area" :class="{ disabled: isExamEnded }">
                <button
                  type="button"
                  :class="['judge-btn', 'judge-btn--correct', { active: getAnswer(q.questionId) === '正确' }]"
                  :disabled="isExamEnded"
                  @click="!isExamEnded && setAnswer(q.questionId, '正确')"
                >
                  <i class="el-icon-check" /> 正确
                </button>
                <button
                  type="button"
                  :class="['judge-btn', 'judge-btn--wrong', { active: getAnswer(q.questionId) === '错误' }]"
                  :disabled="isExamEnded"
                  @click="!isExamEnded && setAnswer(q.questionId, '错误')"
                >
                  <i class="el-icon-close" /> 错误
                </button>
              </div>
              <!-- 填空题 -->
              <div v-if="questionTypeNum(q) === 4" class="fill-blank-area" :class="{ disabled: isExamEnded }">
                <div
                  v-for="(_, idx) in getFillCount(q)"
                  :key="idx"
                  class="fill-blank-row"
                >
                  <span class="fill-blank-label">空{{ idx + 1 }}：</span>
                  <el-input
                    :value="getFillAnswer(q.questionId, idx)"
                    placeholder="请填写"
                    :disabled="isExamEnded"
                    clearable
                    class="fill-input"
                    @input="setFillAnswer(q.questionId, idx, $event)"
                  />
                </div>
              </div>
              <!-- 主观题 -->
              <el-input
                v-if="questionTypeNum(q) === 5"
                :value="getAnswer(q.questionId)"
                type="textarea"
                :rows="5"
                placeholder="请输入你的答案"
                class="subjective-input"
                :disabled="isExamEnded"
                @input="setAnswer(q.questionId, $event)"
              />
            </section>
          </div>

          <div class="submit-row">
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              :disabled="gradingOverlay"
              @click="handleSubmit"
            >
              {{ isExamEnded ? '时间已到，提交试卷' : '交卷' }}
            </el-button>
          </div>
        </main>
      </template>
    </div>

    <!-- 结果弹框：及格 / 不及格 分支 -->
    <el-dialog
      :visible.sync="resultVisible"
      :title="result && result.passed ? '恭喜通过！' : '未达到解锁条件'"
      width="480px"
      class="result-dialog"
      append-to-body
    >
      <div v-if="result" class="result-content">
        <p class="result-score">得分：<strong>{{ result.totalScore }}</strong> / {{ result.paperTotalScore }}（及格线 {{ result.passLine }} 分）</p>
        <p class="result-stats">正确 {{ result.correctCount }} 题，错误 {{ result.wrongCount }} 题</p>
        <template v-if="result.passed">
          <p class="result-tip">您已通过本章节测试，可选择解锁下一章或继续巩固本章。</p>
        </template>
        <template v-else>
          <p class="result-tip">请重新学习本章内容或查看错题后再试。</p>
        </template>
      </div>
      <span slot="footer" class="result-dialog-footer">
        <template v-if="result && result.passed">
          <el-button @click="handleUnlockNext">解锁下一章</el-button>
          <el-button type="primary" @click="handleContinueChapter">继续学习本章</el-button>
        </template>
        <template v-else>
          <el-button @click="handleRelearnChapter">重新学习本章</el-button>
          <el-button type="primary" @click="resultVisible = false">关闭</el-button>
        </template>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getExamPaperDetail, submitChapterExam, unlockNextChapter } from '@/api/student'

const QUESTION_TYPE_MAP = {
  1: '单选题',
  2: '多选题',
  3: '判断题',
  4: '填空题',
  5: '主观题'
}

export default {
  name: 'StudentExamDo',
  data() {
    return {
      paperId: null,
      courseId: null,
      chapterId: null,
      courseName: '',
      chapterName: '',
      paper: null,
      questions: [],
      loading: false,
      answers: {},
      multiAnswers: {},
      fillAnswers: {},   // questionId -> string[] 每空答案
      startTime: null,
      timeLeft: 0,
      timerId: null,
      currentScrollId: null,
      submitting: false,
      gradingOverlay: false,
      resultVisible: false,
      result: null
    }
  },
  computed: {
    isExamEnded() {
      return this.timeLeft <= 0
    }
  },
  created() {
    const q = this.$route.query
    this.paperId = q.paperId ? Number(q.paperId) : null
    this.courseId = q.courseId ? Number(q.courseId) : null
    this.chapterId = q.chapterId ? Number(q.chapterId) : null
    this.courseName = q.courseName || ''
    this.chapterName = q.chapterName || ''
    if (this.paperId) this.fetchPaper()
    else this.$message.error('缺少试卷信息')
  },
  beforeDestroy() {
    if (this.timerId) clearInterval(this.timerId)
  },
  methods: {
    /** 统一题目类型为数字，避免接口返回字符串导致 === 2 不匹配、多选题不渲染 */
    questionTypeNum(q) {
      const t = q && q.questionType
      if (t == null || t === '') return 0
      const n = Number(t)
      return Number.isNaN(n) ? 0 : n
    },
    /** 统一返回选项数组，保证单选/多选都能拿到 options（兼容 options / option_list） */
    getQuestionOptions(q) {
      const opts = (q && (q.options || q.option_list)) || []
      return Array.isArray(opts) ? opts : []
    },
    /** 选项的 key（A/B/C/D），兼容多种字段名 */
    getOptionKey(opt) {
      return opt.optionKey || opt.optionLabel || opt.option_key || opt.option_label
    },
    /** 当前选项是否选中（单选看答案是否等于该 key，多选看是否在数组中） */
    isOptionSelected(q, opt) {
      const key = this.getOptionKey(opt)
      if (this.questionTypeNum(q) === 2) {
        return (this.multiAnswers[q.questionId] || []).includes(key)
      }
      return this.getAnswer(q.questionId) === key
    },
    /** 点击选项：单选直接设答案，多选切换该 key */
    onOptionClick(q, opt) {
      const key = this.getOptionKey(opt)
      if (this.questionTypeNum(q) === 2) {
        this.toggleMultiAnswer(q.questionId, key)
      } else {
        this.setAnswer(q.questionId, key)
      }
    },
    questionTypeLabel(type) {
      return QUESTION_TYPE_MAP[Number(type)] || QUESTION_TYPE_MAP[type] || '题目'
    },
    fetchPaper() {
      if (!this.paperId) return
      this.loading = true
      getExamPaperDetail(this.paperId).then(res => {
        const data = res.data
        if (data && data.paper) {
          this.paper = data.paper
          this.questions = data.questions || []
          this.answers = {}
          this.multiAnswers = {}
          this.fillAnswers = {}
          this.startTime = Date.now()
          this.timeLeft = (this.paper.durationMinutes || 60) * 60
          this.startTimer()
        } else {
          this.paper = null
          this.questions = []
        }
      }).finally(() => { this.loading = false })
    },
    startTimer() {
      if (this.timerId) clearInterval(this.timerId)
      this.timerId = setInterval(() => {
        this.timeLeft--
        if (this.timeLeft <= 0) {
          clearInterval(this.timerId)
          this.timerId = null
          this.$message.warning('考试时间到，请点击下方按钮提交试卷')
        }
      }, 1000)
    },
    formatTime(seconds) {
      if (seconds < 0) return '0:00'
      const m = Math.floor(seconds / 60)
      const s = seconds % 60
      return `${m}:${String(s).padStart(2, '0')}`
    },
    hasAnswer(q) {
      if (this.questionTypeNum(q) === 2) {
        const ma = this.multiAnswers[q.questionId]
        return ma && ma.length > 0
      }
      if (this.questionTypeNum(q) === 4) {
        const fa = this.fillAnswers[q.questionId]
        if (!fa) return false
        return fa.some(v => v != null && String(v).trim() !== '')
      }
      const a = this.answers[q.questionId]
      return a != null && String(a).trim() !== ''
    },
    getAnswer(questionId) {
      return this.answers[questionId] != null ? this.answers[questionId] : ''
    },
    setAnswer(questionId, value) {
      this.$set(this.answers, questionId, value)
    },
    getMultiAnswer(questionId) {
      return this.multiAnswers[questionId] || []
    },
    toggleMultiAnswer(questionId, key) {
      const arr = [...(this.multiAnswers[questionId] || [])]
      const i = arr.indexOf(key)
      if (i === -1) arr.push(key)
      else arr.splice(i, 1)
      arr.sort()
      this.$set(this.multiAnswers, questionId, arr)
      this.$set(this.answers, questionId, arr.join(','))
    },
    getFillCount(q) {
      const opts = this.getQuestionOptions(q)
      return opts.length > 0 ? opts.length : 1
    },
    getFillAnswer(questionId, idx) {
      const fa = this.fillAnswers[questionId]
      return (fa && fa[idx] != null) ? fa[idx] : ''
    },
    setFillAnswer(questionId, idx, val) {
      let fa = this.fillAnswers[questionId]
      if (!fa) fa = []
      const len = Math.max(fa.length, idx + 1)
      while (fa.length < len) fa.push('')
      fa[idx] = val == null ? '' : val
      this.$set(this.fillAnswers, questionId, fa)
      this.$set(this.answers, questionId, fa.join(','))
    },
    buildAnswersPayload() {
      const list = []
      this.questions.forEach(q => {
        let answer = this.answers[q.questionId]
        if (this.questionTypeNum(q) === 2) {
          const arr = this.multiAnswers[q.questionId]
          answer = arr && arr.length ? arr.join(',') : (answer || '')
        }
        if (this.questionTypeNum(q) === 4) {
          const fa = this.fillAnswers[q.questionId]
          answer = (fa && fa.length) ? fa.join(',') : (answer || '')
        }
        list.push({ questionId: q.questionId, answer: answer != null ? String(answer).trim() : '' })
      })
      return list
    },
    scrollToQuestion(questionId) {
      this.currentScrollId = questionId
      const container = this.$refs.questionsContainer
      if (!container) return
      const el = container.querySelector(`[data-question-id="${questionId}"]`)
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    },
    handleSubmit() {
      if (this.submitting || this.gradingOverlay) return
      const useTimeSec = Math.floor((Date.now() - this.startTime) / 1000)
      if (this.timeLeft > 0) {
        const unanswered = this.questions.filter(q => !this.hasAnswer(q))
        if (unanswered.length > 0) {
          this.$message.warning(`还有 ${unanswered.length} 题未作答，请完成后再交卷`)
          return
        }
      }
      this.submitting = true
      this.gradingOverlay = true
      submitChapterExam({
        paperId: this.paperId,
        useTimeSec,
        answers: this.buildAnswersPayload()
      }).then(res => {
        const data = res.data
        if (data) {
          this.result = data
          this.resultVisible = true
          if (this.timerId) { clearInterval(this.timerId); this.timerId = null }
        } else {
          this.$message.error(res.msg || '交卷失败')
        }
      }).catch(() => {
        this.$message.error('交卷失败，请重试')
      }).finally(() => {
        this.submitting = false
        this.gradingOverlay = false
      })
    },
    goBack() {
      this.resultVisible = false
      if (this.courseId) {
        this.$router.push({
          path: `/student/courses/learn/${this.courseId}`,
          query: this.courseName ? { courseName: this.courseName } : {}
        })
      } else {
        this.$router.push('/student/courses')
      }
    },
    handleUnlockNext() {
      if (!this.courseId || !this.chapterId) return this.goBack()
      unlockNextChapter(this.courseId, this.chapterId).then(() => {
        this.$message.success('已解锁下一章')
        this.resultVisible = false
        this.goBack()
      }).catch(() => {
        this.$message.error('解锁失败')
      })
    },
    handleContinueChapter() {
      this.resultVisible = false
      if (this.courseId && this.chapterId) {
        this.$router.push({
          path: `/student/courses/learn/${this.courseId}/chapter/${this.chapterId}`,
          query: { chapterName: this.chapterName, courseName: this.courseName }
        })
      } else this.goBack()
    },
    handleRelearnChapter() {
      this.handleContinueChapter()
    }
  }
}
</script>

<style lang="scss" scoped>
.exam-do-page { padding-bottom: 32px; }
.grading-overlay {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(255,255,255,0.9);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  .grading-content {
    text-align: center;
    .grading-icon { font-size: 48px; color: #1677ff; margin-bottom: 16px; display: block; }
    .grading-text { font-size: 16px; color: #374151; margin: 0; }
  }
}
.exam-header {
  margin-bottom: 24px;
  .back-btn { padding: 0; margin-bottom: 12px; color: #6b7280; &:hover { color: #4096ff; } }
  .exam-title { font-size: 22px; font-weight: 700; color: #1a1a2e; margin: 0 0 8px; }
  .exam-desc { font-size: 14px; color: #6b7280; margin: 0; .course-tag { color: #0d9488; } }
}
.exam-layout { display: flex; gap: 24px; align-items: flex-start; min-height: 400px; }
.exam-sidebar {
  flex-shrink: 0;
  width: 200px;
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  position: sticky;
  top: 24px;
}
.sidebar-timer {
  text-align: center;
  padding: 12px;
  background: linear-gradient(135deg, #e6f4ff, #bae0ff);
  border-radius: 10px;
  margin-bottom: 16px;
  .timer-label { display: block; font-size: 12px; color: #1677ff; margin-bottom: 4px; }
  .timer-value { font-size: 20px; font-weight: 700; color: #0c4a6e; }
  &.overtime .timer-value { color: #dc2626; }
}
.sidebar-title { font-size: 14px; font-weight: 600; color: #1a1a2e; margin-bottom: 12px; }
.question-dots {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  .dot {
    width: 36px; height: 36px; line-height: 36px; text-align: center;
    border-radius: 8px; border: 2px solid #e5e7eb; background: #fafafa;
    font-size: 13px; color: #6b7280; cursor: pointer;
    &:hover { border-color: #93c5fd; background: #eff6ff; }
    &.answered { border-color: #4096ff; background: #e6f4ff; color: #1677ff; }
    &.active { border-color: #1677ff; background: #bae0ff; color: #0c4a6e; }
  }
}
.exam-main {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 14px;
  padding: 28px 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.exam-info-bar { display: flex; gap: 24px; margin-bottom: 24px; font-size: 14px; color: #6b7280; }
.questions-container { max-height: none; }
.question-block {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  scroll-margin-top: 16px;
  &:last-of-type { border-bottom: none; }
}
.question-meta {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 12px;
  .question-type-badge {
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 12px;
    background: linear-gradient(135deg, #e6f4ff, #bae0ff);
    color: #1677ff;
  }
  .question-index { font-size: 13px; color: #6b7280; }
}
.question-title { font-size: 16px; font-weight: 600; color: #1a1a2e; margin: 0 0 16px; line-height: 1.5; }
.option-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  .option-item {
    display: flex;
    align-items: baseline;
    padding: 12px 16px;
    border-radius: 10px;
    border: 2px solid #e5e7eb;
    background: #fafafa;
    cursor: pointer;
    transition: all 0.2s;
    &.selected { border-color: #4096ff; background: #e6f4ff; }
    .option-key { flex-shrink: 0; margin-right: 10px; font-weight: 600; color: #4096ff; }
    .option-content { flex: 1; line-height: 1.5; color: #374151; }
  }
  &.disabled .option-item { cursor: not-allowed; pointer-events: none; opacity: 0.9; }
  .option-empty-tip { margin: 12px 0 0; font-size: 13px; color: #f5222d; }
}
.checkbox-list .option-item.selected { border-color: #4096ff; background: #e6f4ff; }
.judge-area {
  display: flex;
  gap: 16px;
  &.disabled { pointer-events: none; opacity: 0.9; }
  .judge-btn {
    padding: 10px 24px;
    border-radius: 10px;
    border: 2px solid #e5e7eb;
    background: #fafafa;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.2s;
    &.judge-btn--correct:hover, &.active.judge-btn--correct { border-color: #52c41a; background: #f0f9eb; color: #52c41a; }
    &.judge-btn--wrong:hover, &.active.judge-btn--wrong { border-color: #f5222d; background: #fff2f0; color: #f5222d; }
  }
}
.fill-blank-area {
  .fill-blank-row { margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
  .fill-blank-label { font-size: 14px; color: #374151; min-width: 48px; }
  .fill-input { flex: 1; max-width: 400px; }
  &.disabled { pointer-events: none; opacity: 0.9; }
}
.subjective-input { width: 100%; }
.submit-row { margin-top: 32px; padding-top: 24px; border-top: 1px solid #eee; }
.empty-tip { text-align: center; padding: 60px 20px; color: #9ca3af; }
.result-dialog .result-content {
  .result-score { font-size: 18px; margin: 0 0 12px; }
  .result-stats { color: #6b7280; margin: 0 0 8px; }
  .result-tip { font-size: 13px; color: #0d9488; margin: 0 0 16px; }
}
</style>
