<template>
  <div class="app-container course-analysis">
    <div class="analysis-header">
      <h2 class="analysis-title">课程数据分析</h2>
      <p class="analysis-desc">查看课程下学生学习进度、考试次数、各章节通过率与成绩</p>
    </div>

    <el-row :gutter="24" class="analysis-body">
      <!-- 左侧：我的课程 -->
      <el-col :xs="24" :sm="24" :md="10" :lg="8" class="course-col">
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

      <!-- 右侧：课程统计 -->
      <el-col :xs="24" :sm="24" :md="14" :lg="16" class="stats-col">
        <template v-if="!selectedCourseId">
          <div class="panel stats-placeholder">
            <p>请从左侧选择一门课程查看数据统计</p>
          </div>
        </template>
        <template v-else>
          <div v-loading="statsLoading" class="stats-content">
            <!-- 概览卡片 -->
            <el-row :gutter="16" class="overview-cards">
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-card-icon stat-card--blue"><i class="el-icon-user" /></div>
                  <div class="stat-card-value">{{ stats.studentCount || 0 }}</div>
                  <div class="stat-card-label">选课人数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-card-icon stat-card--green"><i class="el-icon-data-line" /></div>
                  <div class="stat-card-value">{{ formatPercent(stats.avgProgressRate) }}</div>
                  <div class="stat-card-label">平均学习进度</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-card-icon stat-card--orange"><i class="el-icon-document" /></div>
                  <div class="stat-card-value">{{ stats.totalExamCount || 0 }}</div>
                  <div class="stat-card-label">考试次数</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <div class="stat-card-icon stat-card--purple"><i class="el-icon-pie-chart" /></div>
                  <div class="stat-card-value">{{ formatPercent(stats.overallPassRate) }}%</div>
                  <div class="stat-card-label">整体及格率</div>
                </div>
              </el-col>
            </el-row>
            <el-row :gutter="16" class="overview-cards">
              <el-col :span="12">
                <div class="stat-card stat-card--full">
                  <span class="stat-card-label">平均分</span>
                  <span class="stat-card-value">{{ formatScore(stats.avgScore) }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-card stat-card--full">
                  <span class="stat-card-label">及格次数 / 总考试次数</span>
                  <span class="stat-card-value">{{ stats.passCount || 0 }} / {{ stats.totalExamCount || 0 }}</span>
                </div>
              </el-col>
            </el-row>

            <!-- 各章节通过率与成绩 -->
            <div class="panel chapter-panel">
              <div class="panel-head">
                <i class="el-icon-s-data" />
                <span>各章节通过率与成绩</span>
              </div>
              <div class="panel-body">
                <el-table :data="stats.chapterStats || []" border stripe size="small" class="chapter-table">
                  <el-table-column type="index" label="序号" width="56" align="center" />
                  <el-table-column prop="chapterName" label="章节名称" min-width="180" show-overflow-tooltip />
                  <el-table-column prop="examCount" label="考试次数" width="100" align="center" />
                  <el-table-column label="平均分" width="100" align="center">
                    <template slot-scope="scope">
                      {{ formatScore(scope.row.avgScore) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="及格人数" width="100" align="center">
                    <template slot-scope="scope">
                      {{ scope.row.passCount }} / {{ scope.row.examCount }}
                    </template>
                  </el-table-column>
                  <el-table-column label="通过率" width="100" align="center">
                    <template slot-scope="scope">
                      <span :class="{ 'text-success': scope.row.passRate > 0, 'text-muted': scope.row.examCount === 0 }">
                        {{ scope.row.examCount ? (Number(scope.row.passRate) + '%') : '-' }}
                      </span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>

            <!-- 学生学习进度列表（可选折叠） -->
            <div class="panel progress-panel">
              <div class="panel-head" @click="showProgressList = !showProgressList">
                <i class="el-icon-s-custom" />
                <span>学生学习进度</span>
                <i :class="['el-icon-arrow-right', 'toggle-icon', { expanded: showProgressList }]" />
              </div>
              <div v-show="showProgressList" class="panel-body">
                <el-table :data="stats.studentProgress || []" border size="small" max-height="360" class="student-progress-table">
                  <el-table-column type="index" label="序号" width="56" align="center" />
                  <el-table-column label="学生" min-width="180" align="left">
                    <template slot-scope="scope">
                      <div class="student-cell">
                        <el-avatar :size="36" :src="resolveAvatar(scope.row.avatar)" class="student-avatar">
                          {{ (scope.row.realName || scope.row.username || '学').slice(0, 1) }}
                        </el-avatar>
                        <div class="student-info">
                          <div class="student-name">{{ scope.row.realName || scope.row.username || '学生' }}</div>
                          <div v-if="scope.row.username" class="student-username">学号 {{ scope.row.username }}</div>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="已完成章节" width="120" align="center">
                    <template slot-scope="scope">
                      <span class="chapter-count">{{ scope.row.finishedChapterCount }} / {{ scope.row.chapterTotalCount || 0 }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="学习进度" width="120" align="center">
                    <template slot-scope="scope">
                      <div class="progress-cell">
                        <el-progress
                          :percentage="Number(scope.row.progressRate) || 0"
                          :stroke-width="6"
                          :show-text="true"
                        />
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </div>
        </template>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getMyCourses, getCourseStats } from '@/api/analysis'

export default {
  name: 'CourseAnalysis',
  data() {
    return {
      coursesLoading: false,
      courseList: [],
      selectedCourseId: null,
      statsLoading: false,
      stats: {},
      showProgressList: true
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
            this.selectCourse(this.courseList[0])
          }
        })
        .finally(() => {
          this.coursesLoading = false
        })
    },
    selectCourse(course) {
      this.selectedCourseId = course.id
      this.fetchStats()
    },
    fetchStats() {
      if (!this.selectedCourseId) return
      this.statsLoading = true
      getCourseStats(this.selectedCourseId)
        .then(res => {
          this.stats = (res && res.data) ? res.data : {}
        })
        .finally(() => {
          this.statsLoading = false
        })
    },
    formatPercent(v) {
      if (v == null || v === '') return '0'
      const n = Number(v)
      return Number.isNaN(n) ? '0' : (n % 1 === 0 ? n : n.toFixed(1))
    },
    formatScore(v) {
      if (v == null || v === '') return '-'
      const n = Number(v)
      return Number.isNaN(n) ? '-' : (n % 1 === 0 ? String(Math.round(n)) : n.toFixed(1))
    },
    resolveCover(cover) {
      if (!cover) return ''
      if (typeof cover === 'string' && (cover.startsWith('http://') || cover.startsWith('https://'))) return cover
      const base = process.env.VUE_APP_BASE_API || ''
      return base ? (base.replace(/\/$/, '') + '/' + (cover.replace(/^\//, ''))) : cover
    },
    resolveAvatar(avatar) {
      if (!avatar) return ''
      if (typeof avatar === 'string' && (avatar.startsWith('http://') || avatar.startsWith('https://'))) return avatar
      const base = process.env.VUE_APP_BASE_API || ''
      return base ? (base.replace(/\/$/, '') + '/' + (avatar.replace(/^\//, ''))) : avatar
    }
  }
}
</script>

<style lang="scss" scoped>
.course-analysis {
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

.analysis-body {
  min-height: 400px;
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
}

.panel-body {
  padding: 16px 20px;
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

.stats-placeholder {
  padding: 60px 20px;
  text-align: center;
  color: #909399;
  font-size: 15px;
}

.overview-cards {
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
  text-align: center;
  &.stat-card--full {
    display: flex;
    align-items: center;
    justify-content: space-between;
    text-align: left;
  }
}

.stat-card-icon {
  width: 48px;
  height: 48px;
  line-height: 48px;
  border-radius: 10px;
  margin: 0 auto 12px;
  i {
    font-size: 24px;
    color: #fff;
  }
}
.stat-card--blue { background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); }
.stat-card--green { background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%); }
.stat-card--orange { background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%); }
.stat-card--purple { background: linear-gradient(135deg, #909399 0%, #b1b3b8 100%); }

.stat-card-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  display: block;
}

.stat-card-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.stat-card--full .stat-card-value {
  font-size: 18px;
  display: inline;
}

.chapter-panel,
.progress-panel {
  margin-top: 20px;
}

.chapter-table {
  width: 100%;
}

.text-success {
  color: #67c23a;
}
.text-muted {
  color: #c0c4cc;
}

.progress-panel .panel-head {
  cursor: pointer;
  .toggle-icon {
    margin-left: auto;
    transition: transform 0.2s;
    &.expanded {
      transform: rotate(90deg);
    }
  }
}

/* 学生学习进度：头像+姓名 */
.student-progress-table {
  .student-cell {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .student-avatar {
    flex-shrink: 0;
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    color: #fff;
    font-size: 14px;
  }
  .student-info {
    min-width: 0;
  }
  .student-name {
    font-weight: 500;
    color: #303133;
    line-height: 1.4;
  }
  .student-username {
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
  }
  .chapter-count {
    font-weight: 500;
    color: #409eff;
  }
  .progress-cell {
    padding: 4px 0;
    ::v-deep .el-progress__text {
      font-size: 12px;
    }
  }
}
</style>
