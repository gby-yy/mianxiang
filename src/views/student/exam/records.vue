<template>
  <div class="exam-records-page">
    <div class="page-header">
      <el-button icon="el-icon-arrow-left" type="text" class="back-btn" @click="goHome">返回首页</el-button>
      <h1 class="page-title">考试记录</h1>
      <p class="page-desc">查看本人做过的考试，支持按课程、章节、试卷搜索。</p>
    </div>

    <!-- 搜索 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="课程">
          <el-input v-model="searchForm.courseName" placeholder="课程名称" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="章节">
          <el-input v-model="searchForm.chapterName" placeholder="章节名称" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="试卷">
          <el-input v-model="searchForm.paperName" placeholder="试卷名称" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-loading="loading" class="records-wrap">
      <template v-if="recordList.length">
        <div
          v-for="item in recordList"
          :key="item.id"
          class="record-card"
        >
          <div class="record-card-body">
            <h3 class="record-paper-name">{{ item.paperName || '试卷' }}</h3>
            <p v-if="item.courseName" class="record-meta-line"><i class="el-icon-notebook-2" /> {{ item.courseName }}</p>
            <p v-if="item.chapterName" class="record-meta-line"><i class="el-icon-document-copy" /> {{ item.chapterName }}</p>
            <div class="record-meta">
              <span class="record-time"><i class="el-icon-time" /> {{ formatTime(item.submitTime) }}</span>
              <span class="record-score">得分 <strong>{{ item.totalScore != null ? item.totalScore : '—' }}</strong> / {{ item.paperTotalScore != null ? item.paperTotalScore : '—' }}</span>
              <span v-if="item.durationSeconds != null" class="record-duration">用时 {{ formatDuration(item.durationSeconds) }}</span>
            </div>
          </div>
          <el-button type="primary" size="small" class="btn-detail" @click="openDetail(item)">详情</el-button>
        </div>

        <el-pagination
          v-if="total > pageSize"
          class="pagination"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          :page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </template>
      <div v-else-if="!loading" class="empty-tip">
        <i class="el-icon-document" />
        <p>暂无考试记录</p>
      </div>
    </div>

    <!-- 详情弹框 -->
    <el-dialog
      :visible.sync="detailVisible"
      :title="detailTitle"
      width="720px"
      class="detail-dialog"
      append-to-body
      @close="detailList = []"
    >
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detailList.length">
          <div
            v-for="(r, idx) in detailList"
            :key="r.id"
            class="detail-item"
          >
            <div class="detail-item-head">
              <span class="detail-index">第 {{ idx + 1 }} 题</span>
              <span :class="['detail-correct', r.isCorrect === 1 ? 'correct' : 'wrong']">
                {{ r.isCorrect === 1 ? '正确' : '错误' }}
              </span>
              <span v-if="r.score != null" class="detail-score">得分 {{ r.score }}</span>
            </div>
            <p class="detail-question-title">{{ r.questionTitle || '题目' }}</p>
            <div class="detail-answers">
              <p><strong>你的答案：</strong>{{ r.studentAnswer != null && r.studentAnswer !== '' ? r.studentAnswer : '—' }}</p>
              <p><strong>标准答案：</strong>{{ r.correctAnswer != null && r.correctAnswer !== '' ? r.correctAnswer : '—' }}</p>
            </div>
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMyExamRecords, getMyExamRecordDetail } from '@/api/student'

export default {
  name: 'StudentExamRecords',
  data() {
    return {
      loading: false,
      recordList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      searchForm: {
        courseName: '',
        chapterName: '',
        paperName: ''
      },
      detailVisible: false,
      detailLoading: false,
      detailList: [],
      detailTitle: ''
    }
  },
  created() {
    this.fetchList()
  },
  methods: {
    formatTime(val) {
      if (!val) return '—'
      const s = String(val)
      if (s.indexOf('T') !== -1) return s.replace('T', ' ')
      return s
    },
    formatDuration(sec) {
      if (sec == null) return '—'
      const m = Math.floor(sec / 60)
      const s = sec % 60
      return m > 0 ? `${m} 分 ${s} 秒` : `${s} 秒`
    },
    fetchList() {
      this.loading = true
      const params = {
        current: this.currentPage,
        size: this.pageSize,
        courseName: this.searchForm.courseName || undefined,
        chapterName: this.searchForm.chapterName || undefined,
        paperName: this.searchForm.paperName || undefined
      }
      getMyExamRecords(params).then(res => {
        if (res.code === 200 && res.data) {
          this.recordList = res.data.records || []
          this.total = res.data.total != null ? res.data.total : 0
        }
      }).catch(() => {}).finally(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchList()
    },
    handleReset() {
      this.searchForm.courseName = ''
      this.searchForm.chapterName = ''
      this.searchForm.paperName = ''
      this.currentPage = 1
      this.fetchList()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchList()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchList()
    },
    openDetail(item) {
      if (!item.id) {
        this.$message.warning('无法查看该记录')
        return
      }
      this.detailTitle = `考试详情 · ${item.paperName || '试卷'}`
      this.detailVisible = true
      this.detailLoading = true
      this.detailList = []
      getMyExamRecordDetail(item.id).then(res => {
        if (res.code === 200 && Array.isArray(res.data)) {
          this.detailList = res.data
        }
      }).catch(() => {}).finally(() => {
        this.detailLoading = false
      })
    },
    goHome() {
      this.$router.push('/student/index')
    }
  }
}
</script>

<style lang="scss" scoped>
.exam-records-page {
  padding-bottom: 32px;
}

.page-header {
  margin-bottom: 20px;
  .back-btn { padding: 0; margin-bottom: 12px; color: #6b7280; display: inline-block; }
  .back-btn:hover { color: #4096ff; }
  .page-title { font-size: 22px; font-weight: 700; color: #1a1a2e; margin: 0 0 8px; }
  .page-desc { font-size: 14px; color: #6b7280; margin: 0; }
}

.search-bar {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
  .search-form { margin: 0; }
  ::v-deep .el-form-item { margin-bottom: 0; margin-right: 16px; }
  ::v-deep .el-form-item__label { color: #374151; }
}

.records-wrap {
  min-height: 200px;
}

.record-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  margin-bottom: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
  &:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08); }
  .record-card-body { flex: 1; min-width: 0; }
  .record-paper-name { font-size: 17px; font-weight: 600; color: #1a1a2e; margin: 0 0 8px; }
  .record-meta-line { font-size: 13px; color: #6b7280; margin: 0 0 4px; }
  .record-meta {
    font-size: 13px; color: #9ca3af;
    margin-top: 10px;
    span + span { margin-left: 20px; }
    .record-score strong { color: #1677ff; }
  }
  .btn-detail { flex-shrink: 0; margin-left: 20px; }
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.empty-tip {
  text-align: center;
  padding: 64px 24px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  i { font-size: 56px; color: #d1d5db; display: block; margin-bottom: 16px; }
  p { margin: 0; color: #9ca3af; }
}

.detail-body {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  &:last-child { border-bottom: none; }
  .detail-item-head {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
  }
  .detail-index { font-size: 13px; color: #6b7280; }
  .detail-correct {
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 4px;
    &.correct { background: #f0f9eb; color: #52c41a; }
    &.wrong { background: #fef2f0; color: #f5222d; }
  }
  .detail-score { font-size: 13px; color: #1677ff; }
  .detail-question-title { font-size: 14px; color: #1a1a2e; margin: 0 0 10px; line-height: 1.5; }
  .detail-answers {
    font-size: 13px; color: #374151;
    p { margin: 0 0 4px; }
  }
}
</style>
