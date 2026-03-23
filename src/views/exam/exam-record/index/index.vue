<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :inline="true" :model="queryParams" class="search-form" size="small">
        <el-form-item label="提交状态">
          <el-select
            v-model="queryParams.submitStatus"
            placeholder="请选择提交状态"
            clearable
          >
            <el-option
              :key="0"
              :label="'未提交'"
              :value="0"
            />
            <el-option
              :key="1"
              :label="'已提交'"
              :value="1"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="试卷名称">
          <el-input
            v-model="queryParams.paperName"
            placeholder="请输入试卷名称"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input
            v-model="queryParams.realName"
            placeholder="请输入学生姓名"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item class="search-buttons">
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>
    </div>


    <!-- 数据表格 -->
    <div class="table-wrapper">
      <el-table
        v-loading="loading"
        :data="eduStudentExamRecordList"
        class="modern-table"
      >
      <el-table-column label="考试编号" prop="examNo" align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{ formatExamNo(scope.row.examNo) }}
        </template>
      </el-table-column>
      <el-table-column label="学生姓名" prop="realName" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="试卷名称" prop="paperName" align="center" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="提交状态" prop="submitStatus" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.submitStatus === 1 ? 'success' : 'warning'" size="small">
            {{ getEnumLabel(scope.row.submitStatus, submitStatusEnumMap) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" prop="startTime" width="180" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="提交时间" prop="submitTime" width="180" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="客观题得分" prop="objectiveScore" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="主观题得分" prop="subjectiveScore" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="总得分" prop="totalScore" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="正确率" prop="correctRate" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="答题时长秒" prop="durationSeconds" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="100" fixed="right" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button type="text" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <pagination
      :total="total"
      :current-page.sync="queryParams.current"
      :page-size.sync="queryParams.size"
      :page-sizes="[5, 10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <!-- 学生提交记录（答题详情）对话框 -->
    <el-dialog title="学生提交记录" :visible.sync="detailOpen" width="800px" append-to-body>
      <template v-if="detailRecord.id">
        <div class="detail-summary">
          <span>{{ detailRecord.paperName || '—' }}</span>
          <span>学生：{{ detailRecord.realName || '—' }}</span>
          <span>总得分：{{ detailRecord.totalScore != null ? detailRecord.totalScore : '—' }}</span>
          <span>提交时间：{{ detailRecord.submitTime || '—' }}</span>
        </div>
        <el-table :data="detailAnswers" border size="small" max-height="400" class="detail-answers-table">
          <el-table-column type="index" label="序号" width="55" align="center" />
          <el-table-column label="题目" prop="questionTitle" min-width="200" show-overflow-tooltip />
          <el-table-column label="学生答案" prop="studentAnswer" width="120" show-overflow-tooltip />
          <el-table-column label="正确答案" prop="correctAnswer" width="120" show-overflow-tooltip />
          <el-table-column label="是否正确" width="88" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.isCorrect === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.isCorrect === 1 ? '正确' : '错误' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="得分" prop="score" width="80" align="center" />
        </el-table>
        <div v-if="!detailAnswers.length" class="detail-empty">暂无答题记录</div>
      </template>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import {
  pageEduStudentExamRecord,
  getExamRecordDetail
} from '@/api/edu_student_exam_record'
import Pagination from '@/components/Pagination'

export default {
  name: 'EduStudentExamRecord',
  components: { Pagination },
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      eduStudentExamRecordList: [],
      queryParams: {
        current: 1,
        size: 5,
        submitStatus: null,
        paperName: null,
        realName: null
      },
      detailOpen: false,
      detailRecord: {},
      detailAnswers: [],
      submitStatusEnumMap: { 0: '未提交', 1: '已提交' }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询学生考试记录列表（范围查询绑定 xxxRange，传参拆成 xxxStart/xxxEnd） */
    getList() {
      this.loading = true
      const { current, size, ...rest } = this.queryParams
      const entity = { ...rest }
      pageEduStudentExamRecord(current, size, entity).then(response => {
        const pageData = response.data
        this.eduStudentExamRecordList = pageData.records || []
        this.total = Number(pageData.total) || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 枚举列展示：根据 value 取 label，无则显示原值 */
    getEnumLabel(val, map) {
      if (map == null || map[val] === undefined) return val
      return map[val]
    },
    /** 考试编号展示：去除 gen: 或 gen:er 前缀 */
    formatExamNo(no) {
      if (!no) return ''
      return String(no).replace(/^gen:?(er)?/i, '').trim() || no
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.current = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.queryParams.current = 1
      this.queryParams.size = this.queryParams.size || 5
      this.queryParams.submitStatus = null
      this.queryParams.paperName = null
      this.queryParams.realName = null
      this.handleQuery()
    },
    /** 详情：学生提交记录（答题详情） */
    handleDetail(row) {
      const id = row.id
      this.detailRecord = {}
      this.detailAnswers = []
      this.detailOpen = true
      getExamRecordDetail(id).then(response => {
        const data = response.data || {}
        this.detailRecord = data.record || {}
        this.detailAnswers = data.answers || []
      }).catch(() => {
        this.$message.error('获取提交记录失败')
        this.detailOpen = false
      })
    },
    /** 分页大小改变 */
    handleSizeChange(val) {
      this.queryParams.size = val
      this.queryParams.current = 1
      this.getList()
    },
    /** 当前页改变 */
    handleCurrentChange(val) {
      this.queryParams.current = val
      this.getList()
    }
  }
}
</script>

<style scoped lang="scss">
.detail-summary {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.detail-answers-table { margin-bottom: 12px; }
.detail-empty { text-align: center; color: #909399; padding: 24px; }
</style>
