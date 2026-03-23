<template>
  <div class="learn-page">
    <div class="page-header">
      <el-button icon="el-icon-arrow-left" type="text" class="back-btn" @click="goBack">返回课程列表</el-button>
      <h1 class="page-title">{{ courseName || '课程学习' }}</h1>
      <p v-if="record" class="page-desc">
        学习进度：{{ finishedChapterCountDisplay }} / {{ record.chapterTotalCount || 0 }} 章
        <span v-if="record.progressRate != null"> · {{ Math.round(Number(record.progressRate)) }}%</span>
      </p>
    </div>

    <div v-loading="loading" class="learn-content">
      <!-- 未选课：弹框选基础 -->
      <template v-if="!loading && !hasRecord && !dialogConfirmed">
        <div class="empty-tip">
          <p>您尚未开始学习本课程，请选择您的基础情况以解锁对应章节。</p>
          <el-button type="primary" size="medium" @click="showFoundationDialog">选择基础并开始</el-button>
        </div>
      </template>

      <!-- 已选课：章节列表 -->
      <template v-else-if="chapterRecords.length">
        <div class="chapter-list">
          <div
            v-for="(ch, index) in chapterRecords"
            :key="ch.id"
            :class="['chapter-item', { unlocked: ch.unlockStatus === 1, locked: ch.unlockStatus !== 1 }]"
          >
            <div class="chapter-index">{{ index + 1 }}</div>
            <div class="chapter-main">
              <div class="chapter-name-row">
                <span class="chapter-name">{{ ch.chapterName }}</span>
                <el-tag :type="ch.chapterType === 1 ? 'success' : 'warning'" size="mini">
                  {{ ch.chapterType === 1 ? '基础' : '难度' }}
                </el-tag>
                <el-tag v-if="ch.unlockStatus !== 1" type="info" size="mini">未解锁</el-tag>
                <el-tag v-else-if="ch.studyStatus === 2" type="success" size="mini">已完成</el-tag>
                <el-tag v-else-if="ch.studyStatus === 1" type="primary" size="mini">学习中</el-tag>
                <el-tag v-else type="info" size="mini">未学习</el-tag>
              </div>
              <div class="chapter-meta">
                <span v-if="ch.studyProgressRate != null">进度 {{ Math.round(Number(ch.studyProgressRate)) }}%</span>
                <span v-if="ch.contentCount != null"> · 内容 {{ ch.contentCount }} 个</span>
                <span v-if="ch.questionCount != null"> · 题目 {{ ch.questionCount }} 道</span>
              </div>
            </div>
            <div class="chapter-action">
              <template v-if="ch.unlockStatus === 1">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleStudyChapter(ch)"
                >
                  {{ ch.studyStatus === 0 ? '开始学习' : ch.studyStatus === 2 ? '复习' : '继续' }}
                </el-button>
                <el-button
                  size="small"
                  class="btn-practice-inline"
                  @click="showPracticeModeDialog(ch)"
                >
                  <i class="el-icon-edit-outline" />
                  刷题
                </el-button>
                <el-button
                  v-if="isChapterProgressFull(ch)"
                  size="small"
                  type="success"
                  class="btn-exam-inline"
                  @click="handleChapterExam(ch)"
                >
                  <i class="el-icon-document-checked" />
                  章节测试
                </el-button>
              </template>
              <span v-else class="locked-hint">请按顺序完成前面章节</span>
            </div>
          </div>
        </div>
      </template>

      <div v-else-if="!loading && hasRecord" class="empty-tip">
        <p>暂无章节记录</p>
      </div>
    </div>

    <!-- 章节测试弹框：自评引导 + 选择难度 -->
    <el-dialog
      :visible.sync="examDialogVisible"
      width="520px"
      class="exam-dialog"
      append-to-body
      :close-on-click-modal="false"
      @close="examDialogStep = 1; examDifficulty = 1; currentExamChapter = null"
    >
      <div slot="title" class="exam-dialog-title">
        {{ examDialogStep === 1 ? '章节测试' : '选择测试难度' }}
      </div>
      <div class="exam-dialog-body">
        <template v-if="examDialogStep === 1">
          <p class="exam-dialog-hint">感觉自己学得怎么样？</p>
          <p class="exam-dialog-desc">完成本章学习后，可以通过章节测试检验掌握程度。请根据自身情况选择测试难度，系统将为您匹配对应试卷。</p>
        </template>
        <template v-else>
          <p class="exam-dialog-hint">请选择本次测试的难度</p>
          <div class="exam-difficulty-options">
            <div
              v-for="item in examDifficultyOptions"
              :key="item.value"
              :class="['exam-difficulty-option', { active: examDifficulty === item.value }]"
              @click="examDifficulty = item.value"
            >
              <span class="option-label">{{ item.label }}</span>
              <span class="option-desc">及格线 {{ item.passLine }} 分</span>
              <i v-if="examDifficulty === item.value" class="el-icon-check option-check" />
            </div>
          </div>
        </template>
      </div>
      <span slot="footer" class="exam-dialog-footer">
        <el-button v-if="examDialogStep === 1" @click="examDialogVisible = false">取 消</el-button>
        <el-button v-if="examDialogStep === 1" type="primary" @click="examDialogStep = 2">下一步</el-button>
        <template v-else>
          <el-button @click="examDialogStep = 1">上一步</el-button>
          <el-button type="primary" :loading="examDialogLoading" @click="handleStartChapterExam">开始测试</el-button>
        </template>
      </span>
    </el-dialog>

    <!-- 刷题模式弹框：选择顺序/随机（针对当前选中章节） -->
    <el-dialog
      :visible.sync="practiceModeDialogVisible"
      width="560px"
      class="practice-mode-dialog"
      append-to-body
      @close="practiceMode = 'sequential'; currentPracticeChapter = null"
    >
      <div slot="title" class="mode-dialog-header">
        <span class="mode-dialog-title">选择练习模式</span>
        <span class="mode-dialog-course">{{ currentPracticeChapter ? currentPracticeChapter.chapterName : '' }} · {{ courseName || '当前课程' }}</span>
      </div>
      <div class="mode-dialog-body">
        <p class="mode-dialog-hint">选择一种方式开始刷题</p>
        <div class="mode-cards">
          <div
            v-for="item in practiceModeOptions"
            :key="item.value"
            :class="['mode-card', { active: practiceMode === item.value }]"
            @click="practiceMode = item.value"
          >
            <div class="mode-card-icon" :class="'mode-card-icon--' + item.value">
              <i :class="item.icon" />
            </div>
            <div class="mode-card-text">
              <span class="mode-card-title">{{ item.label }}</span>
              <span class="mode-card-desc">{{ item.desc }}</span>
            </div>
            <i v-if="practiceMode === item.value" class="el-icon-check mode-card-check" />
          </div>
        </div>
      </div>
      <span slot="footer" class="mode-dialog-footer">
        <el-button @click="practiceModeDialogVisible = false">取 消</el-button>
        <el-button type="primary" class="btn-start" @click="handleStartPractice">
          <i class="el-icon-right" />
          开始练习
        </el-button>
      </span>
    </el-dialog>

    <!-- 选择基础弹框 -->
    <el-dialog
      title="选择您的基础"
      :visible.sync="foundationDialogVisible"
      width="480px"
      append-to-body
      :close-on-click-modal="false"
      class="foundation-dialog"
      @close="foundationType = 0"
    >
      <p class="dialog-desc">请选择您当前的基础情况，系统将为您解锁相应章节。</p>
      <div class="foundation-options">
        <div
          :class="['foundation-option', { active: foundationType === 0 }]"
          @click="foundationType = 0"
        >
          <div class="option-icon"><i class="el-icon-star-off" /></div>
          <div class="option-text">
            <span class="option-title">0基础</span>
            <span class="option-desc">仅解锁第1章（基础章节），适合零基础入门</span>
          </div>
          <i v-if="foundationType === 0" class="el-icon-check option-check" />
        </div>
        <div
          :class="['foundation-option', { active: foundationType === 1 }]"
          @click="foundationType = 1"
        >
          <div class="option-icon"><i class="el-icon-medal" /></div>
          <div class="option-text">
            <span class="option-title">有基础</span>
            <span class="option-desc">解锁所有基础章节 + 第1章难度章节，适合有基础学员</span>
          </div>
          <i v-if="foundationType === 1" class="el-icon-check option-check" />
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="foundationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmFoundation">开始学习</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getCourseLearnInfo, startCourseLearn, getChapterPaper } from '@/api/student'

export default {
  name: 'StudentCourseLearn',
  data() {
    return {
      courseId: null,
      courseName: '',
      loading: false,
      hasRecord: false,
      record: null,
      chapterRecords: [],
      dialogConfirmed: false,
      foundationDialogVisible: false,
      foundationType: 0,
      practiceModeDialogVisible: false,
      /** 当前要刷题的章节（点击某行刷题时设置） */
      currentPracticeChapter: null,
      practiceMode: 'sequential',
      practiceModeOptions: [
        { value: 'sequential', icon: 'el-icon-s-operation', label: '顺序刷题', desc: '按题目顺序依次作答' },
        { value: 'random', icon: 'el-icon-s-data', label: '随机刷题', desc: '随机打乱题目顺序' }
      ],
      /** 章节测试弹框 */
      examDialogVisible: false,
      examDialogStep: 1,
      examDifficulty: 1,
      examDialogLoading: false,
      currentExamChapter: null,
      examDifficultyOptions: [
        { value: 1, label: '简单', passLine: 70 },
        { value: 2, label: '中等', passLine: 60 },
        { value: 3, label: '困难', passLine: 50 }
      ]
    }
  },
  computed: {
    /** 已完成章节数：按章节内容学习进度 100% 实时统计，与列表中的「已完成」一致 */
    finishedChapterCountDisplay() {
      if (!this.chapterRecords || !this.chapterRecords.length) {
        return this.record ? (this.record.finishedChapterCount || 0) : 0
      }
      return this.chapterRecords.filter(ch => {
        const p = ch.studyProgressRate
        return p != null && Number(p) >= 100
      }).length
    }
  },
  created() {
    this.courseId = this.$route.params.courseId ? Number(this.$route.params.courseId) : null
    this.courseName = this.$route.query.courseName || ''
    if (this.courseId) {
      this.fetchLearnInfo()
    } else {
      this.$message.error('课程不存在')
      this.goBack()
    }
  },
  methods: {
    goBack() {
      this.$router.push('/student/courses')
    },
    fetchLearnInfo() {
      this.loading = true
      getCourseLearnInfo(this.courseId).then(res => {
        const data = res.data || {}
        this.hasRecord = !!data.hasRecord
        this.record = data.record || null
        this.chapterRecords = data.chapterRecords || []
      }).catch(() => {}).finally(() => {
        this.loading = false
      })
    },
    showFoundationDialog() {
      this.foundationDialogVisible = true
      this.foundationType = 0
    },
    confirmFoundation() {
      this.foundationDialogVisible = false
      this.loading = true
      startCourseLearn(this.courseId, this.foundationType).then(res => {
        const data = res.data || {}
        this.hasRecord = true
        this.record = data.record || null
        this.chapterRecords = data.chapterRecords || []
        this.dialogConfirmed = true
        this.$message.success('已为您解锁相应章节，开始学习吧')
      }).catch(err => {
        // 接口返回 code!==200 时拦截器已提示 res.msg，此处仅处理无 response 的情况（如网络异常）
        if (!err.response && err.message) {
          this.$message.error(err.message || '网络异常，请稍后重试')
        }
      }).finally(() => {
        this.loading = false
      })
    },
    handleStudyChapter(ch) {
      const courseId = this.$route.params.courseId
      const sourceChapterId = ch.sourceChapterId
      if (!courseId || !sourceChapterId) {
        this.$message.error('参数错误')
        return
      }
      this.$router.push({
        name: 'StudentChapterDetail',
        params: { courseId, chapterId: sourceChapterId },
        query: {
          chapterName: ch.chapterName || '',
          courseName: this.courseName || ''
        }
      })
    },
    showPracticeModeDialog(ch) {
      this.currentPracticeChapter = ch
      this.practiceMode = 'sequential'
      this.practiceModeDialogVisible = true
    },
    handleStartPractice() {
      if (!this.currentPracticeChapter) return
      const chapterId = this.currentPracticeChapter.sourceChapterId
      const chapterName = this.currentPracticeChapter.chapterName || ''
      this.practiceModeDialogVisible = false
      this.$router.push({
        path: '/student/practice/do',
        query: {
          courseId: this.courseId,
          chapterId: chapterId,
          courseName: this.courseName || '',
          chapterName: chapterName,
          mode: this.practiceMode
        }
      })
      this.currentPracticeChapter = null
    },
    /** 章节进度是否已达 100%（用于显示章节测试按钮） */
    isChapterProgressFull(ch) {
      if (!ch) return false
      if (ch.studyStatus === 2) return true
      const rate = ch.studyProgressRate != null ? Number(ch.studyProgressRate) : 0
      return rate >= 100
    },
    /** 点击章节测试：打开自评+选难度弹框 */
    handleChapterExam(ch) {
      if (!ch || !this.courseId) return
      this.currentExamChapter = ch
      this.examDialogStep = 1
      this.examDifficulty = 1
      this.examDialogVisible = true
    },
    /** 开始章节测试：拉取对应难度试卷并跳转考试页 */
    handleStartChapterExam() {
      if (!this.currentExamChapter || !this.courseId) return
      this.examDialogLoading = true
      const chapterId = this.currentExamChapter.sourceChapterId
      getChapterPaper(chapterId, this.examDifficulty)
        .then(res => {
          const data = res.data
          if (!data || !data.paper) {
            this.$message.warning('该章节暂无对应难度的试卷，请选择其他难度或联系老师')
            return
          }
          this.examDialogVisible = false
          this.$router.push({
            path: '/student/exam/do',
            query: {
              paperId: data.paper.id,
              courseId: this.courseId,
              chapterId: chapterId,
              courseName: this.courseName || '',
              chapterName: this.currentExamChapter.chapterName || '',
              difficultyLevel: this.examDifficulty
            }
          })
        })
        .catch(() => {
          this.$message.error('获取试卷失败，请重试')
        })
        .finally(() => {
          this.examDialogLoading = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.learn-page {
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
  }
}

.learn-content {
  min-height: 200px;
}

.empty-tip {
  text-align: center;
  padding: 48px 24px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  p {
    margin: 0 0 20px;
    color: #6b7280;
    font-size: 14px;
  }
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chapter-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);

  &.locked {
    opacity: 0.85;
    background: #fafafa;
  }

  .chapter-index {
    width: 36px;
    height: 36px;
    line-height: 36px;
    text-align: center;
    border-radius: 10px;
    background: #e6f4ff;
    color: #1677ff;
    font-weight: 600;
    margin-right: 16px;
    flex-shrink: 0;
  }

  .chapter-main {
    flex: 1;
    min-width: 0;
  }

  .chapter-name-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;

    .chapter-name {
      font-size: 16px;
      font-weight: 600;
      color: #1a1a2e;
    }
  }

  .chapter-meta {
    font-size: 13px;
    color: #9ca3af;
  }

  .chapter-action {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 10px;

    .btn-practice-inline,
    .btn-exam-inline {
      i {
        margin-right: 4px;
        vertical-align: middle;
      }
    }
  }

  .locked-hint {
    font-size: 13px;
    color: #9ca3af;
  }
}

/* 刷题模式弹框（参考 example 样式） */
.practice-mode-dialog ::v-deep .el-dialog__header {
  padding: 0;
  margin: 0;
}
.practice-mode-dialog ::v-deep .el-dialog__body {
  padding: 0 24px 20px;
}
.practice-mode-dialog ::v-deep .el-dialog__footer {
  padding: 16px 24px 20px;
  border-top: 1px solid #eee;
}

.mode-dialog-header {
  padding: 20px 24px 16px;

  .mode-dialog-title {
    display: block;
    font-size: 18px;
    font-weight: 700;
    color: #1a1a2e;
    margin-bottom: 4px;
  }
  .mode-dialog-course {
    font-size: 13px;
    color: #6b7280;
  }
}

.mode-dialog-body {
  .mode-dialog-hint {
    font-size: 13px;
    color: #9ca3af;
    margin: 0 0 16px;
  }
}

.mode-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mode-card {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;

  &:hover {
    border-color: #bae0ff;
    background: #f0f7ff;
  }

  &.active {
    border-color: #4096ff;
    background: #e6f4ff;
    box-shadow: 0 2px 8px rgba(64, 150, 255, 0.2);

    .mode-card-icon {
      color: #fff;
    }
    .mode-card-icon--sequential { background: linear-gradient(135deg, #4096ff, #1677ff); }
    .mode-card-icon--random { background: linear-gradient(135deg, #52c41a, #389e0d); }
  }

  .mode-card-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 14px;
    flex-shrink: 0;
    background: #e5e7eb;
    color: #6b7280;
    transition: all 0.25s ease;

    i {
      font-size: 22px;
    }
  }

  .mode-card-text {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .mode-card-title {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a2e;
  }

  .mode-card-desc {
    font-size: 12px;
    color: #6b7280;
  }

  .mode-card-check {
    position: absolute;
    right: 16px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 18px;
    color: #4096ff;
  }
}

.mode-dialog-footer {
  .btn-start {
    padding: 10px 24px;
    font-weight: 600;

    i {
      margin-left: 6px;
      vertical-align: middle;
    }
  }
}

/* 选择基础弹框 */
.exam-dialog .exam-dialog-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
}
.exam-dialog-body {
  .exam-dialog-hint {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0 0 12px;
  }
  .exam-dialog-desc {
    font-size: 14px;
    color: #6b7280;
    line-height: 1.6;
    margin: 0;
  }
  .exam-difficulty-options {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  .exam-difficulty-option {
    display: flex;
    align-items: center;
    padding: 14px 16px;
    border: 2px solid #e5e7eb;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;
    position: relative;
    &:hover { border-color: #93c5fd; background: #eff6ff; }
    &.active { border-color: #1677ff; background: #e6f4ff; }
    .option-label { font-weight: 600; color: #1a1a2e; margin-right: 8px; }
    .option-desc { font-size: 13px; color: #6b7280; }
    .option-check { margin-left: auto; color: #1677ff; font-size: 18px; }
  }
}

.foundation-dialog ::v-deep .el-dialog__body {
  padding: 16px 24px 8px;
}

.dialog-desc {
  margin: 0 0 20px;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
}

.foundation-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.foundation-option {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &:hover {
    border-color: #bae0ff;
    background: #f0f7ff;
  }

  &.active {
    border-color: #4096ff;
    background: #e6f4ff;

    .option-icon {
      background: linear-gradient(135deg, #4096ff, #1677ff);
      color: #fff;
    }
  }

  .option-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 14px;
    flex-shrink: 0;
    background: #e5e7eb;
    color: #6b7280;

    i { font-size: 22px; }
  }

  .option-text {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .option-title {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a2e;
  }

  .option-desc {
    font-size: 12px;
    color: #6b7280;
    line-height: 1.4;
  }

  .option-check {
    position: absolute;
    right: 16px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 18px;
    color: #4096ff;
  }
}
</style>
