<template>
  <div class="practice-do-page">
    <div class="do-header">
      <el-button icon="el-icon-arrow-left" type="text" class="back-btn" @click="goBack">返回章节学习</el-button>
      <h1 class="do-title">刷题练习</h1>
      <p class="do-desc">
        <span v-if="courseName">课程：{{ courseName }}</span>
        <span v-if="chapterName" class="chapter-tag">章节：{{ chapterName }}</span>
        <span class="mode-tag">{{ modeLabel }}</span>
      </p>
    </div>

    <div v-loading="loading" class="do-content">
      <!-- 无题目：友好提示 + 返回 -->
      <template v-if="!loading && questionList.length === 0 && !loadError">
        <div class="empty-state">
          <i class="el-icon-document-delete empty-icon" />
          <p class="empty-title">本章节暂无测试题</p>
          <p class="empty-desc">请先学习章节内容，或返回选择其他章节刷题。</p>
          <el-button type="primary" @click="goBack">返回章节学习</el-button>
        </div>
      </template>
      <!-- 加载失败 -->
      <template v-else-if="!loading && loadError">
        <div class="empty-state">
          <i class="el-icon-warning-outline empty-icon" />
          <p class="empty-title">加载失败</p>
          <p class="empty-desc">{{ loadError }}</p>
          <el-button type="primary" @click="fetchQuestions">重新加载</el-button>
        </div>
      </template>
      <!-- 有题目：展示当前题 -->
      <template v-else-if="currentQuestion">
        <div class="question-meta">
          <span class="question-type-badge">{{ questionTypeLabel(currentQuestion.questionType) }}</span>
          <span class="question-index">第 {{ currentIndex + 1 }} / {{ questionList.length }} 题</span>
        </div>
        <h2 class="question-title">{{ currentQuestion.questionTitle }}</h2>

        <div class="answer-area">
          <!-- 单选 -->
          <div v-if="currentQuestion.questionType === 1" class="option-list">
            <div
              v-for="opt in options"
              :key="opt.id"
              :class="['option-item', { selected: userAnswer === opt.optionKey, disabled: submitted }]"
              @click="!submitted && (userAnswer = opt.optionKey)"
            >
              <span class="option-key">{{ opt.optionKey }}.</span>
              <span class="option-content">{{ opt.optionContent }}</span>
            </div>
          </div>
          <!-- 多选 -->
          <div v-if="currentQuestion.questionType === 2" class="option-list checkbox-list">
            <div
              v-for="opt in options"
              :key="opt.id"
              :class="['option-item', { selected: multiAnswer.includes(opt.optionKey), disabled: submitted }]"
              @click="toggleMulti(opt.optionKey)"
            >
              <span class="option-key">{{ opt.optionKey }}.</span>
              <span class="option-content">{{ opt.optionContent }}</span>
            </div>
          </div>
          <!-- 判断题：两个小型按钮 正确 / 错误 -->
          <div v-if="currentQuestion.questionType === 3" class="judge-area">
            <div class="judge-buttons">
              <button
                type="button"
                :class="['judge-btn', 'judge-btn--correct', { active: userAnswer === '正确', disabled: submitted }]"
                :disabled="submitted"
                @click="!submitted && (userAnswer = '正确')"
              >
                <i class="el-icon-check" />
                <span>正确</span>
              </button>
              <button
                type="button"
                :class="['judge-btn', 'judge-btn--wrong', { active: userAnswer === '错误', disabled: submitted }]"
                :disabled="submitted"
                @click="!submitted && (userAnswer = '错误')"
              >
                <i class="el-icon-close" />
                <span>错误</span>
              </button>
            </div>
          </div>
          <!-- 填空题：有选项配置则按选项数量渲染多个输入框，否则一个 -->
          <div v-if="currentQuestion.questionType === 4" class="fill-blank-area">
            <div
              v-for="(_, idx) in fillInputCount"
              :key="idx"
              class="fill-blank-row"
            >
              <span class="fill-blank-label">空{{ idx + 1 }}：</span>
              <el-input
                :value="fillAnswers[idx]"
                placeholder="请填写"
                :disabled="submitted"
                clearable
                class="fill-input"
                @input="setFillAnswer(idx, $event)"
              />
            </div>
          </div>
          <!-- 主观题 -->
          <el-input
            v-if="currentQuestion.questionType === 5"
            v-model="userAnswer"
            type="textarea"
            :rows="6"
            placeholder="请输入你的答案"
            :disabled="submitted"
            class="subjective-input"
          />
        </div>

        <div v-if="!submitted" class="action-row">
          <el-button type="primary" @click="handleSubmit">提交答案</el-button>
        </div>

        <!-- 提交后：客观题对错与解析（含单选、多选、填空） -->
        <template v-if="submitted && currentQuestion.questionType !== 5">
          <div :class="['result-block', isCorrect ? 'result-correct' : 'result-wrong']">
            <i :class="isCorrect ? 'el-icon-success' : 'el-icon-error'" />
            <span>{{ isCorrect ? '回答正确' : '回答错误' }}</span>
          </div>
          <div class="compare-block">
            <p><strong>你的答案：</strong>{{ displayUserAnswer }}</p>
            <p><strong>标准答案：</strong>{{ displayStandardAnswer }}</p>
          </div>
          <div v-if="currentQuestion.answerAnalysis" class="analysis-block">
            <p class="analysis-title">答案解析</p>
            <p class="analysis-content">{{ currentQuestion.answerAnalysis }}</p>
          </div>
        </template>

        <!-- 提交后：主观题 AI 判题结果（得分 + 理由） -->
        <template v-if="submitted && currentQuestion.questionType === 5">
          <div v-if="subjectiveLoading" class="result-block result-loading">
            <i class="el-icon-loading" />
            <span>AI 正在评阅中…</span>
          </div>
          <template v-else>
            <div class="subjective-result">
              <p class="subjective-score">
                <strong>得分：</strong><span class="score-num">{{ subjectiveScore }}</span> 分
              </p>
              <p v-if="subjectiveComment" class="subjective-comment">
                <strong>评语：</strong>{{ subjectiveComment }}
              </p>
              <div class="subjective-compare">
                <p><strong>你的答案：</strong>{{ userAnswer }}</p>
                <p v-if="currentQuestion.standardAnswer"><strong>正确答案：</strong>{{ currentQuestion.standardAnswer }}</p>
              </div>
              <div v-if="currentQuestion.answerAnalysis" class="subjective-analysis">
                <p class="analysis-title">答案解析</p>
                <p class="analysis-content">{{ currentQuestion.answerAnalysis }}</p>
              </div>
            </div>
          </template>
        </template>

        <!-- 上一题 / 下一题 -->
        <div class="nav-row">
          <el-button :disabled="!canPrev" @click="goPrev">
            <i class="el-icon-arrow-left" /> 上一题
          </el-button>
          <el-button :disabled="!canNext" type="primary" @click="goNext">
            下一题 <i class="el-icon-arrow-right" />
          </el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import { getChapterQuestions, submitSubjectiveScore } from '@/api/student'

const QUESTION_TYPE_MAP = {
  1: '单选题',
  2: '多选题',
  3: '判断题',
  4: '填空题',
  5: '主观题'
}

function shuffle(arr) {
  const a = arr.slice()
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

export default {
  name: 'StudentPracticeDo',
  data() {
    return {
      courseId: null,
      chapterId: null,
      courseName: '',
      chapterName: '',
      mode: 'sequential',
      loading: false,
      loadError: '',
      questionList: [],
      currentIndex: 0,
      currentQuestion: null,
      options: [],
      userAnswer: '',
      multiAnswer: [],
      /** 填空题各空答案，长度 = 有选项时 options.length 否则 1 */
      fillAnswers: [],
      submitted: false,
      isCorrect: false,
      /** 主观题 AI 判题：加载中、得分、评语 */
      subjectiveLoading: false,
      subjectiveScore: 0,
      subjectiveComment: ''
    }
  },
  computed: {
    /** 填空题输入框数量：有配置选项则按选项数，否则 1 */
    fillInputCount() {
      if (this.currentQuestion && this.currentQuestion.questionType === 4) {
        if (this.options && this.options.length > 0) return this.options.length
        return 1
      }
      return 0
    },
    modeLabel() {
      const map = { sequential: '顺序练习', random: '随机练习' }
      return map[this.mode] || this.mode
    },
    displayUserAnswer() {
      if (this.currentQuestion && this.currentQuestion.questionType === 2) {
        return this.multiAnswer.length ? this.multiAnswer.sort().join('、') : '—'
      }
      if (this.currentQuestion && this.currentQuestion.questionType === 4) {
        if (this.fillAnswers.length === 0) return '—'
        return this.fillAnswers.map(a => (a != null ? String(a).trim() : '')).filter(Boolean).length
          ? this.fillAnswers.map(a => (a != null ? String(a).trim() : '')).join('、')
          : '—'
      }
      return this.userAnswer || '—'
    },
    /** 标准答案展示：判断题统一为正确/错误，填空题多空时用选项内容拼接 */
    displayStandardAnswer() {
      if (this.currentQuestion && this.currentQuestion.questionType === 3) {
        const n = this.normalizeStandardAnswer(this.currentQuestion.standardAnswer)
        return n || '—'
      }
      if (this.currentQuestion && this.currentQuestion.questionType === 4 && this.options && this.options.length > 0) {
        return this.options.map(o => (o.optionContent != null ? String(o.optionContent).trim() : '')).join('、') || '—'
      }
      return this.currentQuestion ? (this.currentQuestion.standardAnswer || '—') : '—'
    },
    canPrev() {
      return this.currentIndex > 0
    },
    canNext() {
      return this.currentIndex < this.questionList.length - 1
    }
  },
  created() {
    const q = this.$route.query
    this.courseId = q.courseId ? Number(q.courseId) : null
    this.chapterId = q.chapterId ? Number(q.chapterId) : null
    this.courseName = q.courseName || ''
    this.chapterName = q.chapterName || ''
    this.mode = q.mode || 'sequential'
    if (this.courseId && this.chapterId) {
      this.fetchQuestions()
    } else {
      this.loadError = '缺少课程或章节参数'
    }
  },
  methods: {
    questionTypeLabel(type) {
      return QUESTION_TYPE_MAP[type] || '题目'
    },
    fetchQuestions() {
      this.loadError = ''
      this.loading = true
      getChapterQuestions(this.courseId, this.chapterId)
        .then(res => {
          const list = (res.data || []).slice()
          if (this.mode === 'random' && list.length > 0) {
            this.questionList = shuffle(list)
          } else {
            this.questionList = list
          }
          if (this.questionList.length > 0) {
            this.currentIndex = 0
            this.setCurrentQuestion()
          }
        })
        .catch(() => {
          this.loadError = '获取题目失败，请稍后重试'
          this.questionList = []
        })
        .finally(() => {
          this.loading = false
        })
    },
    setCurrentQuestion() {
      const q = this.questionList[this.currentIndex]
      if (!q) {
        this.currentQuestion = null
        this.options = []
        this.fillAnswers = []
        return
      }
      this.currentQuestion = q
      this.options = Array.isArray(q.options) ? q.options : []
      this.userAnswer = ''
      this.multiAnswer = []
      if (q.questionType === 4) {
        this.fillAnswers = this.options.length > 0 ? new Array(this.options.length).fill('') : ['']
      } else {
        this.fillAnswers = []
      }
      this.submitted = false
      this.isCorrect = false
      this.subjectiveLoading = false
      this.subjectiveScore = 0
      this.subjectiveComment = ''
    },
    toggleMulti(key) {
      if (this.submitted) return
      const i = this.multiAnswer.indexOf(key)
      if (i === -1) this.multiAnswer.push(key)
      else this.multiAnswer.splice(i, 1)
    },
    setFillAnswer(idx, val) {
      this.$set(this.fillAnswers, idx, val == null ? '' : val)
    },
    /** 多选题答案标准化：兼容 ABC / AB / BA / A,B，c / A B C 等，忽略顺序与大小写，多种分隔符 */
    normalizeMultiChoiceAnswer(val) {
      if (val == null) return ''
      const s = String(val).trim().toUpperCase()
      const set = new Set()
      // 支持空格、中英文逗号、顿号、分号、点、斜杠等分隔，无分隔的连续字母也会被逐字解析
      s.split(/[\s,，、;；.．/／]+/).forEach(part => {
        const p = part.trim()
        for (let i = 0; i < p.length; i++) {
          if (/[A-Z]/.test(p[i])) set.add(p[i])
        }
      })
      return [...set].sort().join(',')
    },
    normalizeStandardAnswer(val) {
      if (val == null) return ''
      if (this.currentQuestion && this.currentQuestion.questionType === 2) {
        return this.normalizeMultiChoiceAnswer(val)
      }
      let s = String(val).trim().toUpperCase().replace(/\s+/g, '')
      if (this.currentQuestion && this.currentQuestion.questionType === 3) {
        if (s === '正确' || s === '对') return '正确'
        if (s === '错误' || s === '错') return '错误'
      }
      return s
    },
    normalizeUserAnswer() {
      if (this.currentQuestion.questionType === 2) {
        return this.multiAnswer.slice().sort().join(',').toUpperCase()
      }
      return this.normalizeStandardAnswer(this.userAnswer)
    },
    /** 填空题逐空比较：标准化后比较 */
    normalizeBlankVal(val) {
      return (val != null ? String(val).trim() : '').toUpperCase().replace(/\s+/g, ' ')
    },
    handleSubmit() {
      if (this.currentQuestion.questionType === 5) {
        if (!this.userAnswer || !this.userAnswer.trim()) {
          this.$message.warning('请先作答')
          return
        }
        this.submitted = true
        this.subjectiveLoading = true
        this.subjectiveScore = 0
        this.subjectiveComment = ''
        submitSubjectiveScore({
          questionTitle: this.currentQuestion.questionTitle || '',
          userAnswer: this.userAnswer.trim(),
          standardAnswer: this.currentQuestion.standardAnswer != null ? String(this.currentQuestion.standardAnswer) : ''
        })
          .then(res => {
            const data = res.data || {}
            this.subjectiveScore = data.score != null ? Number(data.score) : 0
            this.subjectiveComment = data.comment != null ? String(data.comment) : ''
          })
          .catch(() => {
            this.$message.error('AI 评阅失败，请稍后重试')
            this.subjectiveComment = '评阅请求失败'
          })
          .finally(() => {
            this.subjectiveLoading = false
          })
        return
      }
      if (this.currentQuestion.questionType === 3) {
        this.submitted = true
        const standard = this.normalizeStandardAnswer(this.currentQuestion.standardAnswer)
        const user = this.normalizeStandardAnswer(this.userAnswer)
        this.isCorrect = standard === user
        return
      }
      if (this.currentQuestion.questionType === 4) {
        this.submitted = true
        if (this.options && this.options.length > 0) {
          let allCorrect = true
          for (let i = 0; i < this.options.length; i++) {
            const std = this.normalizeBlankVal(this.options[i].optionContent)
            const user = this.normalizeBlankVal(this.fillAnswers[i])
            if (std !== user) {
              allCorrect = false
              break
            }
          }
          this.isCorrect = allCorrect
        } else {
          const standard = this.normalizeBlankVal(this.currentQuestion.standardAnswer)
          const user = this.normalizeBlankVal(this.fillAnswers[0])
          this.isCorrect = standard === user
        }
        return
      }
      this.submitted = true
      const standard = this.normalizeStandardAnswer(this.currentQuestion.standardAnswer)
      const user = this.normalizeUserAnswer()
      this.isCorrect = standard === user
    },
    goPrev() {
      if (this.currentIndex <= 0) return
      this.currentIndex--
      this.setCurrentQuestion()
    },
    goNext() {
      if (this.currentIndex >= this.questionList.length - 1) return
      this.currentIndex++
      this.setCurrentQuestion()
    },
    goBack() {
      if (this.courseId) {
        this.$router.push({
          path: `/student/courses/learn/${this.courseId}`,
          query: this.courseName ? { courseName: this.courseName } : {}
        })
      } else {
        this.$router.push('/student/courses')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.practice-do-page {
  padding-bottom: 32px;
}

.do-header {
  margin-bottom: 24px;

  .back-btn {
    padding: 0;
    margin-bottom: 12px;
    color: #6b7280;
    &:hover { color: #4096ff; }
  }

  .do-title {
    font-size: 22px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0 0 8px;
  }

  .do-desc {
    font-size: 14px;
    color: #6b7280;
    margin: 0;
    .chapter-tag { margin-left: 12px; color: #0d9488; }
    .mode-tag { margin-left: 12px; color: #1677ff; }
  }
}

.do-content {
  background: #fff;
  border-radius: 14px;
  padding: 32px 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  min-height: 280px;
}

.empty-state {
  text-align: center;
  padding: 48px 24px;

  .empty-icon {
    font-size: 64px;
    color: #c9cdd4;
    margin-bottom: 16px;
    display: block;
  }
  .empty-title {
    font-size: 18px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0 0 8px;
  }
  .empty-desc {
    font-size: 14px;
    color: #6b7280;
    margin: 0 0 24px;
    line-height: 1.6;
  }
}

.question-meta {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;

  .question-type-badge {
    display: inline-block;
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 12px;
    background: linear-gradient(135deg, #e6f4ff, #bae0ff);
    color: #1677ff;
  }
  .question-index {
    font-size: 13px;
    color: #6b7280;
  }
}

.question-title {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 24px;
  line-height: 1.6;
}

.answer-area {
  margin-bottom: 24px;
  font-size: 14px !important;

  .option-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  .option-item {
    display: flex;
    align-items: baseline;
    padding: 12px 16px;
    border-radius: 10px;
    border: 2px solid #e5e7eb;
    background: #fafafa;
    cursor: pointer;
    transition: all 0.2s;

    &.selected {
      border-color: #4096ff;
      background: #e6f4ff;
    }
    &.disabled {
      cursor: default;
    }
    .option-key {
      flex-shrink: 0;
      margin-right: 10px;
      font-weight: 600;
      color: #4096ff;
    }
    .option-content {
      flex: 1;
      line-height: 1.5;
      color: #374151;
    }
  }
  .checkbox-list .option-item.selected {
    border-color: #4096ff;
    background: #e6f4ff;
  }
}

.judge-area {
  margin-bottom: 8px;
}

.judge-buttons {
  display: inline-flex;
  gap: 16px;
  flex-wrap: wrap;
}

.judge-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 120px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
  outline: none;
  background: #f3f4f6;
  color: #6b7280;

  i {
    font-size: 18px;
  }

  &:hover:not(.disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &.judge-btn--correct {
    &:hover:not(.disabled) {
      background: #dcfce7;
      color: #16a34a;
      border-color: #86efac;
    }
    &.active {
      background: linear-gradient(135deg, #22c55e, #16a34a);
      color: #fff;
      border-color: #16a34a;
      box-shadow: 0 4px 14px rgba(34, 197, 94, 0.35);
    }
  }

  &.judge-btn--wrong {
    &:hover:not(.disabled) {
      background: #fee2e2;
      color: #dc2626;
      border-color: #fca5a5;
    }
    &.active {
      background: linear-gradient(135deg, #ef4444, #dc2626);
      color: #fff;
      border-color: #dc2626;
      box-shadow: 0 4px 14px rgba(239, 68, 68, 0.35);
    }
  }

  &.disabled {
    cursor: default;
    opacity: 0.85;
  }
}

.answer-area .subjective-input {
  width: 100%;
}

.fill-blank-area {
  display: flex;
  flex-direction: column;
  gap: 14px;

  .fill-blank-row {
    display: flex;
    align-items: center;
    gap: 10px;

    .fill-blank-label {
      flex-shrink: 0;
      font-size: 14px;
      color: #374151;
      min-width: 48px;
    }
    .fill-input {
      flex: 1;
      max-width: 400px;
    }
  }
}

.action-row {
  margin-bottom: 28px;
}

.result-block {
  padding: 12px 16px;
  border-radius: 10px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;

  &.result-correct {
    background: #f0f9eb;
    border: 1px solid #b3e19d;
    color: #52c41a;
  }
  &.result-wrong {
    background: #fef2f0;
    border: 1px solid #f5b4a8;
    color: #f5222d;
  }
  &.result-loading {
    background: #e6f4ff;
    border: 1px solid #91caff;
    color: #1677ff;
  }
}

.subjective-result {
  margin-bottom: 24px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 10px;

  .subjective-score {
    margin: 0 0 8px;
    font-size: 15px;
    color: #1a1a2e;
    .score-num {
      color: #1677ff;
      font-size: 18px;
      font-weight: 600;
    }
  }
  .subjective-comment {
    margin: 0;
    font-size: 14px;
    color: #4b5563;
    line-height: 1.6;
  }
  .subjective-compare {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #eee;

    p {
      margin: 0 0 8px;
      font-size: 14px;
      color: #374151;
      line-height: 1.6;
    }
  }
  .subjective-analysis {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #eee;

    .analysis-title {
      font-weight: 600;
      color: #1a1a2e;
      margin: 0 0 8px;
    }
    .analysis-content {
      margin: 0;
      font-size: 14px;
      color: #4b5563;
      line-height: 1.6;
    }
  }
}

.compare-block {
  margin-bottom: 16px;
  padding: 12px 0;
  border-top: 1px solid #eee;

  p {
    margin: 0 0 8px;
    font-size: 14px;
    color: #374151;
  }
}

.analysis-block {
  padding: 16px;
  background: #f9fafb;
  border-radius: 10px;
  margin-bottom: 24px;

  .analysis-title {
    font-weight: 600;
    color: #1a1a2e;
    margin: 0 0 8px;
  }
  .analysis-content {
    margin: 0;
    font-size: 14px;
    color: #4b5563;
    line-height: 1.6;
  }
}

.nav-row {
  display: flex;
  justify-content: space-between;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.unsupported-tip {
  padding: 12px 16px;
  color: #909399;
  font-size: 14px;
  background: #f4f4f5;
  border-radius: 8px;
}
</style>
