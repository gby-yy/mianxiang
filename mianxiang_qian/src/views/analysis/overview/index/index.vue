<template>
  <div class="app-container data-overview">
    <div class="overview-header">
      <h2 class="overview-title">数据总览</h2>
      <p class="overview-desc">平台运营与教学数据统计分析</p>
    </div>

    <!-- 顶部统计卡片：总学生、总教师、课程数量、学习总人次、平均通过率 -->
    <div class="stats-cards" v-loading="overviewLoading">
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="stat-card stat-card--student">
            <div class="stat-icon"><i class="el-icon-user-solid" /></div>
            <div class="stat-body">
              <div class="stat-value">{{ overview.studentCount != null ? overview.studentCount : '—' }}</div>
              <div class="stat-label">总学生人数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="stat-card stat-card--teacher">
            <div class="stat-icon"><i class="el-icon-s-custom" /></div>
            <div class="stat-body">
              <div class="stat-value">{{ overview.teacherCount != null ? overview.teacherCount : '—' }}</div>
              <div class="stat-label">总教师人数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="stat-card stat-card--course">
            <div class="stat-icon"><i class="el-icon-reading" /></div>
            <div class="stat-body">
              <div class="stat-value">{{ overview.courseCount != null ? overview.courseCount : '—' }}</div>
              <div class="stat-label">课程数量</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="stat-card stat-card--study">
            <div class="stat-icon"><i class="el-icon-notebook-2" /></div>
            <div class="stat-body">
              <div class="stat-value">{{ overview.studySessionCount != null ? overview.studySessionCount : '—' }}</div>
              <div class="stat-label">学习总人次</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <div class="stat-card stat-card--pass">
            <div class="stat-icon"><i class="el-icon-data-analysis" /></div>
            <div class="stat-body">
              <div class="stat-value">{{ avgPassRateDisplay }}</div>
              <div class="stat-label">平均通过率</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区：每行两个 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="12" class="chart-col">
        <BasicBarChart :dataList="echarts1" name="试卷难度分布统计（全平台）" />
      </el-col>
      <el-col :xs="24" :md="12" class="chart-col">
        <BasicBarChart :dataList="echarts2" @search="handlecountCourseByDifficultySearch" name="课程难度分布统计">
          <template #queryParams>
            <el-form :inline="true" :model="countCourseByDifficultyQueryParams" size="small" class="query-form">
              <el-form-item label="创建时间">
                <el-date-picker
                  v-model="countCourseByDifficultyQueryParams.timeRange0"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始"
                  end-placeholder="结束"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  @change="handlecountCourseByDifficultyTimeRange0Change"
                  style="width: 220px;"
                />
              </el-form-item>
              <el-form-item label="审核状态">
                <el-select v-model="countCourseByDifficultyQueryParams.auditStatus" placeholder="请选择" style="width: 100px;">
                  <el-option v-for="opt in countCourseByDifficultyparam1Options" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>
            </el-form>
          </template>
        </BasicBarChart>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="12" class="chart-col">
        <FlorenceNightingaleRoseChart :dataList="echarts3" @search="handlecountQuestionByTypeSearch" name="题目类型分布统计">
          <template #queryParams>
            <el-form :inline="true" :model="countQuestionByTypeQueryParams" size="small" class="query-form">
              <el-form-item label="难度">
                <el-select v-model="countQuestionByTypeQueryParams.difficultyLevel" placeholder="请选择" style="width: 100px;">
                  <el-option v-for="opt in countQuestionByTypeparam0Options" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="countQuestionByTypeQueryParams.status" placeholder="请选择" style="width: 100px;">
                  <el-option v-for="opt in countQuestionByTypeparam1Options" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>
            </el-form>
          </template>
        </FlorenceNightingaleRoseChart>
      </el-col>
      <el-col :xs="24" :md="12" class="chart-col">
        <CircularPieChart :dataList="echarts4" @search="handlecountExamRecordBySubmitStatusSearch" name="考试提交状态统计">
          <template #queryParams>
            <el-form :inline="true" :model="countExamRecordBySubmitStatusQueryParams" size="small" class="query-form">
              <el-form-item label="考试开始时间">
                <el-date-picker
                  v-model="countExamRecordBySubmitStatusQueryParams.timeRange0"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始"
                  end-placeholder="结束"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  @change="handlecountExamRecordBySubmitStatusTimeRange0Change"
                  style="width: 220px;"
                />
              </el-form-item>
            </el-form>
          </template>
        </CircularPieChart>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="12" class="chart-col">
        <BasicLineChart :dataList="echarts5" @search="handleavgExamScoreByPaperDifficultySearch" name="不同难度试卷平均分统计">
          <template #queryParams>
            <el-form :inline="true" :model="avgExamScoreByPaperDifficultyQueryParams" size="small" class="query-form">
              <el-form-item label="提交时间">
                <el-date-picker
                  v-model="avgExamScoreByPaperDifficultyQueryParams.timeRange0"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始"
                  end-placeholder="结束"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  @change="handleavgExamScoreByPaperDifficultyTimeRange0Change"
                  style="width: 220px;"
                />
              </el-form-item>
            </el-form>
          </template>
        </BasicLineChart>
      </el-col>
      <el-col :xs="24" :md="12" class="chart-col">
        <BasicPieChart :dataList="echarts6" name="章节内容类型分布统计（全平台）" />
      </el-col>
    </el-row>
  </div>
</template>

<script>
import BasicPieChart from '@/components/BasicPieChart'
import BasicBarChart from '@/components/BasicBarChart'
import FlorenceNightingaleRoseChart from '@/components/FlorenceNightingaleRoseChart'
import CircularPieChart from '@/components/CircularPieChart'
import BasicLineChart from '@/components/BasicLineChart'
import { getDashboardOverview } from '@/api/dashboard'
import { countPaperByDifficulty, countCourseByDifficulty, countQuestionByType, countExamRecordBySubmitStatus, avgExamScoreByPaperDifficulty, countContentByContentType } from '@/api/echarts'

export default {
  name: 'DataOverview',
  components: {
    BasicLineChart,
    BasicPieChart,
    BasicBarChart,
    CircularPieChart,
    FlorenceNightingaleRoseChart
  },
  data() {
    return {
      overviewLoading: false,
      overview: {},
      echarts1: [],
      echarts2: [],
      countCourseByDifficultyQueryParams: {
        timeRange0: null,
        startTime: '',
        endTime: '',
        auditStatus: -1
      },
      countCourseByDifficultyparam1Options: [
        { value: -1, label: '全部' },
        { value: 0, label: '待审核' },
        { value: 1, label: '通过' },
        { value: 2, label: '驳回' }
      ],
      echarts3: [],
      countQuestionByTypeQueryParams: { difficultyLevel: -1, status: -1 },
      countQuestionByTypeparam0Options: [
        { value: -1, label: '全部' },
        { value: 1, label: '低' },
        { value: 2, label: '中' },
        { value: 3, label: '高' }
      ],
      countQuestionByTypeparam1Options: [
        { value: -1, label: '全部' },
        { value: 0, label: '停用' },
        { value: 1, label: '启用' }
      ],
      echarts4: [],
      countExamRecordBySubmitStatusQueryParams: { timeRange0: null, startTime: '', endTime: '' },
      echarts5: [],
      avgExamScoreByPaperDifficultyQueryParams: { timeRange0: null, startTime: '', endTime: '' },
      echarts6: []
    }
  },
  computed: {
    avgPassRateDisplay() {
      const v = this.overview.avgPassRate
      if (v == null) return '—'
      if (typeof v === 'number') return v + '%'
      if (typeof v === 'string') return v + '%'
      return (Number(v) || 0) + '%'
    }
  },
  mounted() {
    this.fetchOverview()
    this.initCharts()
  },
  methods: {
    fetchOverview() {
      this.overviewLoading = true
      getDashboardOverview()
        .then(res => {
          this.overview = (res && res.data) ? res.data : {}
        })
        .finally(() => {
          this.overviewLoading = false
        })
    },
    initCharts() {
      countPaperByDifficulty().then(res => { this.echarts1 = res.data || [] })
      const p2 = {
        startTime: this.countCourseByDifficultyQueryParams.startTime,
        endTime: this.countCourseByDifficultyQueryParams.endTime,
        auditStatus: this.countCourseByDifficultyQueryParams.auditStatus
      }
      countCourseByDifficulty(p2).then(res => { this.echarts2 = res.data || [] })
      const p3 = {
        difficultyLevel: this.countQuestionByTypeQueryParams.difficultyLevel,
        status: this.countQuestionByTypeQueryParams.status
      }
      countQuestionByType(p3).then(res => { this.echarts3 = res.data || [] })
      const p4 = {
        startTime: this.countExamRecordBySubmitStatusQueryParams.startTime,
        endTime: this.countExamRecordBySubmitStatusQueryParams.endTime
      }
      countExamRecordBySubmitStatus(p4).then(res => { this.echarts4 = res.data || [] })
      const p5 = {
        startTime: this.avgExamScoreByPaperDifficultyQueryParams.startTime,
        endTime: this.avgExamScoreByPaperDifficultyQueryParams.endTime
      }
      avgExamScoreByPaperDifficulty(p5).then(res => { this.echarts5 = res.data || [] })
      countContentByContentType({}).then(res => { this.echarts6 = res.data || [] })
    },
    handlecountCourseByDifficultyTimeRange0Change(value) {
      if (value && value.length === 2) {
        this.countCourseByDifficultyQueryParams.startTime = value[0]
        this.countCourseByDifficultyQueryParams.endTime = value[1]
      } else {
        this.countCourseByDifficultyQueryParams.startTime = ''
        this.countCourseByDifficultyQueryParams.endTime = ''
      }
    },
    handlecountCourseByDifficultySearch() {
      const params = {
        startTime: this.countCourseByDifficultyQueryParams.startTime,
        endTime: this.countCourseByDifficultyQueryParams.endTime,
        auditStatus: this.countCourseByDifficultyQueryParams.auditStatus
      }
      countCourseByDifficulty(params).then(res => { this.echarts2 = res.data || [] })
    },
    handlecountQuestionByTypeSearch() {
      const params = {
        difficultyLevel: this.countQuestionByTypeQueryParams.difficultyLevel,
        status: this.countQuestionByTypeQueryParams.status
      }
      countQuestionByType(params).then(res => { this.echarts3 = res.data || [] })
    },
    handlecountExamRecordBySubmitStatusTimeRange0Change(value) {
      if (value && value.length === 2) {
        this.countExamRecordBySubmitStatusQueryParams.startTime = value[0]
        this.countExamRecordBySubmitStatusQueryParams.endTime = value[1]
      } else {
        this.countExamRecordBySubmitStatusQueryParams.startTime = ''
        this.countExamRecordBySubmitStatusQueryParams.endTime = ''
      }
    },
    handlecountExamRecordBySubmitStatusSearch() {
      const params = {
        startTime: this.countExamRecordBySubmitStatusQueryParams.startTime,
        endTime: this.countExamRecordBySubmitStatusQueryParams.endTime
      }
      countExamRecordBySubmitStatus(params).then(res => { this.echarts4 = res.data || [] })
    },
    handleavgExamScoreByPaperDifficultyTimeRange0Change(value) {
      if (value && value.length === 2) {
        this.avgExamScoreByPaperDifficultyQueryParams.startTime = value[0]
        this.avgExamScoreByPaperDifficultyQueryParams.endTime = value[1]
      } else {
        this.avgExamScoreByPaperDifficultyQueryParams.startTime = ''
        this.avgExamScoreByPaperDifficultyQueryParams.endTime = ''
      }
    },
    handleavgExamScoreByPaperDifficultySearch() {
      const params = {
        startTime: this.avgExamScoreByPaperDifficultyQueryParams.startTime,
        endTime: this.avgExamScoreByPaperDifficultyQueryParams.endTime
      }
      avgExamScoreByPaperDifficulty(params).then(res => { this.echarts5 = res.data || [] })
    }
  }
}
</script>

<style lang="scss" scoped>
.data-overview {
  .overview-header {
    margin-bottom: 24px;
    .overview-title {
      font-size: 22px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
    }
    .overview-desc {
      font-size: 14px;
      color: #909399;
      margin: 0;
    }
  }

  .stats-cards {
    margin-bottom: 28px;
    .stat-card {
      display: flex;
      align-items: center;
      padding: 16px 20px;
      border-radius: 8px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
      margin-bottom: 16px;
      background: #fff;
      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        i { font-size: 24px; color: #fff; }
      }
      .stat-body { flex: 1; min-width: 0; }
      .stat-value { font-size: 22px; font-weight: 600; color: #303133; line-height: 1.3; }
      .stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
      &.stat-card--student .stat-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
      &.stat-card--teacher .stat-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
      &.stat-card--course .stat-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
      &.stat-card--study .stat-icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
      &.stat-card--pass .stat-icon { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
    }
  }

  .chart-row { margin-bottom: 20px; }
  .chart-col {
    margin-bottom: 20px;
    display: flex;
    flex-direction: column;
  }
  .query-form {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
    ::v-deep .el-form-item { margin-bottom: 0; margin-right: 0; }
  }
  ::v-deep .el-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    .el-card__body { flex: 1; display: flex; flex-direction: column; }
  }
}
</style>
