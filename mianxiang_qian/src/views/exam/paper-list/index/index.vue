<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :inline="true" :model="queryParams" class="search-form" size="small">
        <el-form-item label="试卷名称">
          <el-input
            v-model="queryParams.paperName"
            placeholder="请输入试卷名称"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="试卷难度">
          <el-select
            v-model="queryParams.difficultyLevel"
            placeholder="请选择试卷难度"
            clearable
          >
            <el-option
              :key="1"
              :label="'简单'"
              :value="1"
            />
            <el-option
              :key="2"
              :label="'中等'"
              :value="2"
            />
            <el-option
              :key="3"
              :label="'困难'"
              :value="3"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="生成方式">
          <el-select
            v-model="queryParams.generateType"
            placeholder="请选择生成方式"
            clearable
          >
            <el-option
              :key="1"
              :label="'AI组卷'"
              :value="1"
            />
            <el-option
              :key="2"
              :label="'手动组卷'"
              :value="2"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="试卷状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择试卷状态"
            clearable
          >
            <el-option
              :key="0"
              :label="'停用'"
              :value="0"
            />
            <el-option
              :key="1"
              :label="'启用'"
              :value="1"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="章节名称">
          <el-input
            v-model="queryParams.chapterName"
            placeholder="请输入章节名称"
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

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button :disabled="multiple" @click="handleDeleteBatch">批量删除</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
      <el-table
        v-loading="loading"
        :data="eduChapterExamPaperList"
        @selection-change="handleSelectionChange"
        class="modern-table"
      >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="章节名称" prop="chapterName" align="center" :show-overflow-tooltip="true" min-width="120" />
      <el-table-column label="试卷编号" prop="paperCode" align="center" :show-overflow-tooltip="true" width="120">
        <template slot-scope="scope">
          {{ formatPaperCode(scope.row.paperCode) }}
        </template>
      </el-table-column>
      <el-table-column label="试卷名称" prop="paperName" align="center" :show-overflow-tooltip="true" min-width="140" />
      <el-table-column label="试卷难度" prop="difficultyLevel" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.difficultyLevel === 1 ? 'success' : scope.row.difficultyLevel === 2 ? 'warning' : 'danger'" size="small">
            {{ getEnumLabel(scope.row.difficultyLevel, difficultyLevelEnumMap) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="题目数量" prop="questionCount" align="center" width="90" />
      <el-table-column label="总分" prop="totalScore" align="center" width="80" />
      <el-table-column label="考试时长" prop="durationMinutes" align="center" width="90" />
      <el-table-column label="生成方式" prop="generateType" align="center" width="100">
        <template slot-scope="scope">
          <span>{{ getEnumLabel(scope.row.generateType, generateTypeEnumMap) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="试卷说明" prop="paperDesc" align="center" :show-overflow-tooltip="true" min-width="120" />
      <el-table-column label="试卷状态" prop="status" align="center" width="80">
        <template slot-scope="scope">
          <span>{{ getEnumLabel(scope.row.status, statusEnumMap) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="200" fixed="right" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button type="text" @click="handleDetail(scope.row)">详情</el-button>
          <el-button type="text" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button type="text" style="color: #f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 添加或修改对话框（有富文本/MD 时宽度 50%，否则 600px） -->
    <el-dialog :title="title" :visible.sync="open"  width="600px"  append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="试卷编号" prop="paperCode">
          <el-input v-model="form.paperCode" placeholder="请输入试卷编号" />
        </el-form-item>
        <el-form-item label="所属章节" prop="chapterId">
          <el-select v-model="form.chapterId" placeholder="请选择所属章节" clearable style="width: 100%;">
            <el-option
              v-for="(item, idx) in eduCourseChapterOptions"
              :key="idx"
              :label="item.chapterName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="试卷名称" prop="paperName">
          <el-input v-model="form.paperName" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="试卷难度" prop="difficultyLevel">
          <el-select v-model="form.difficultyLevel" placeholder="请选择试卷难度" clearable style="width: 100%;">
            <el-option
              v-for="(item, index) in difficultyLevelOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目数量" prop="questionCount">
          <el-input v-model.number="form.questionCount" type="number" placeholder="请输入题目数量" />
        </el-form-item>
        <el-form-item label="总分" prop="totalScore">
          <el-input v-model.number="form.totalScore" type="number" placeholder="请输入总分" />
        </el-form-item>
        <el-form-item label="考试时长" prop="durationMinutes">
          <el-input v-model.number="form.durationMinutes" type="number" placeholder="请输入考试时长" />
        </el-form-item>
        <el-form-item label="生成方式" prop="generateType">
          <el-radio-group v-model="form.generateType">
            <el-radio
              v-for="(item, index) in generateTypeOptions"
              :key="index"
              :label="item.value"
            >{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="试卷说明" prop="paperDesc">
          <el-input
            v-model="form.paperDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入试卷说明"
          />
        </el-form-item>
        <el-form-item label="试卷状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="(item, index) in statusOptions"
              :key="index"
              :label="item.value"
            >{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 试卷详情：只读展示试卷下题目 -->
    <el-dialog :title="detailTitle" :visible.sync="detailOpen" width="35%" append-to-body class="paper-detail-dialog">
      <div v-if="currentDetailPaper" class="paper-detail-container">
        <div class="paper-detail-meta">
          <el-tag size="small">难度：{{ getEnumLabel(currentDetailPaper.difficultyLevel, difficultyLevelEnumMap) }}</el-tag>
          <el-tag size="small" style="margin-left: 8px;">题目数：{{ paperDetailQuestionList.length }}</el-tag>
          <el-tag size="small" type="success" style="margin-left: 8px;">总分：{{ totalDetailScore }}</el-tag>
        </div>
        <el-table
          v-loading="detailLoading"
          :data="paperDetailQuestionList"
          border
          size="small"
          style="margin-top: 12px;"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="题目标题" prop="questionTitle" align="left" min-width="200" :show-overflow-tooltip="true" />
          <el-table-column label="题目类型" prop="questionType" align="center" width="100">
            <template slot-scope="scope">
              {{ getEnumLabel(scope.row.questionType, questionTypeEnumMap) }}
            </template>
          </el-table-column>
          <el-table-column label="难度" prop="difficultyLevel" align="center" width="80">
            <template slot-scope="scope">
              {{ getEnumLabel(scope.row.difficultyLevel, difficultyLevelEnumMap) }}
            </template>
          </el-table-column>
          <el-table-column label="分值" prop="questionScore" align="center" width="80" />
          <el-table-column label="排序" prop="sortOrder" align="center" width="70" />
        </el-table>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import {
  pageEduChapterExamPaper,
  getEduChapterExamPaperById,
  saveEduChapterExamPaper,
  updateEduChapterExamPaper,
  deleteEduChapterExamPaper,
  deleteBatchEduChapterExamPaper,
  listEduCourseChapter
} from '@/api/edu_chapter_exam_paper'
import { listEduExamPaperQuestion } from '@/api/edu_exam_paper_question'
import ImgUpload from '@/components/ImgUpload'
import ImagePreview from '@/components/ImagePreview'
import Pagination from '@/components/Pagination'
import BasicEditor from '@/components/BasicEditor'
import { mapGetters } from 'vuex'

export default {
  name: 'EduChapterExamPaper',
  components: {
    ImgUpload,
    ImagePreview,
    Pagination,
    BasicEditor
  },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      eduChapterExamPaperList: [],
      title: '',
      open: false,
      queryParams: {
        current: 1,
        size: 5,
        paperName:  null,
        difficultyLevel:  null,
        generateType:  null,
        status:  null,
        chapterName:  null
      },
      form: {},
      rules: {
        paperCode: [
          { required: true, message: '试卷编号不能为空', trigger: 'blur' }
        ],
        chapterId: [
          { required: true, message: '所属章节不能为空', trigger: 'change' }
        ],
        paperName: [
          { required: true, message: '试卷名称不能为空', trigger: 'blur' }
        ],
        difficultyLevel: [
          { required: true, message: '试卷难度不能为空', trigger: 'change' }
        ],
        questionCount: [
          { required: true, message: '题目数量不能为空', trigger: 'blur' }
        ],
        totalScore: [
          { required: true, message: '总分不能为空', trigger: 'blur' }
        ],
        durationMinutes: [
          { required: true, message: '考试时长不能为空', trigger: 'blur' }
        ],
        generateType: [
          { required: true, message: '生成方式不能为空', trigger: 'change' }
        ],
        paperDesc: [
          { required: true, message: '试卷说明不能为空', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '试卷状态不能为空', trigger: 'change' }
        ]
      },
      eduCourseChapterOptions: [],

      difficultyLevelOptions: [{value: 1, label: '简单'},{value: 2, label: '中等'},{value: 3, label: '困难'}],
      generateTypeOptions: [{value: 1, label: 'AI组卷'},{value: 2, label: '手动组卷'}],
      statusOptions: [{value: 0, label: '停用'},{value: 1, label: '启用'}],

      difficultyLevelEnumMap: { 1: '简单', 2: '中等', 3: '困难' },
      generateTypeEnumMap: { 1: 'AI组卷', 2: '手动组卷' },
      statusEnumMap: { 0: '停用', 1: '启用' },
      questionTypeEnumMap: { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '主观题' },

      detailOpen: false,
      detailTitle: '试卷详情',
      currentDetailPaper: null,
      paperDetailQuestionList: [],
      detailLoading: false
    }
  },
  computed: {
    ...mapGetters(['role', 'id']),
    userRole() {
      return this.role || ''
    },
    userId() {
      return this.id || null
    },
    totalDetailScore() {
      return (this.paperDetailQuestionList || []).reduce((sum, q) => sum + (Number(q.questionScore) || 0), 0)
    }
  },
  created() {
    this.getList()
    this.getEduCourseChapterOptions({})
  },
  methods: {
    /** 查询章节试卷列表（老师只能查看自己所属课程下的试卷） */
    getList() {
      this.loading = true
      const { current, size, ...rest } = this.queryParams
      const entity = { ...rest }
      if (this.userRole === 'teacher' && this.userId) {
        entity.teacherId = this.userId
      }
      pageEduChapterExamPaper(current, size, entity).then(response => {
        const pageData = response.data
        this.eduChapterExamPaperList = pageData.records || []
        this.total = Number(pageData.total) || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 获取所属章节下拉选项 */
    getEduCourseChapterOptions(params) {
      listEduCourseChapter(params || {}).then(response => {
        this.eduCourseChapterOptions = response.data || []
      })
    },
    /** 枚举列展示：根据 value 取 label，无则显示原值 */
    getEnumLabel(val, map) {
      if (map == null || map[val] === undefined) return val
      return map[val]
    },
    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    },
    /** 表单重置 */
    reset() {
      this.form = {}
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
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
      this.queryParams.paperName = null
      this.queryParams.difficultyLevel = null
      this.queryParams.generateType = null
      this.queryParams.status = null
      this.queryParams.chapterName = null
      this.handleQuery()
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加章节试卷'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids[0]
      getEduChapterExamPaperById(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改章节试卷'
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateEduChapterExamPaper(this.form).then(response => {
              this.$message.success('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            saveEduChapterExamPaper(this.form).then(response => {
              this.$message.success('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$confirm('是否确认删除编号为"' + ids + '"的数据项？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteEduChapterExamPaper(ids)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    /** 批量删除按钮操作 */
    handleDeleteBatch() {
      const ids = this.ids
      this.$confirm('是否确认删除选中的' + ids.length + '条数据？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteBatchEduChapterExamPaper(ids)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      }).catch(() => {})
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
    },
    /** 试卷编号展示：去除 gen 前缀 */
    formatPaperCode(code) {
      if (!code) return ''
      return String(code).replace(/^gen:?/i, '').trim() || code
    },
    /** 试卷详情（只读展示题目） */
    handleDetail(row) {
      this.currentDetailPaper = row
      this.detailTitle = `试卷详情 - ${row.paperName || '未命名'}`
      this.detailOpen = true
      this.paperDetailQuestionList = []
      this.loadPaperDetailQuestions()
    },
    /** 加载试卷下题目列表（只读） */
    loadPaperDetailQuestions() {
      if (!this.currentDetailPaper || !this.currentDetailPaper.id) return
      this.detailLoading = true
      listEduExamPaperQuestion({ paperId: this.currentDetailPaper.id }).then(res => {
        const list = res.data || []
        this.paperDetailQuestionList = list.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
        this.detailLoading = false
      }).catch(() => {
        this.detailLoading = false
      })
    }
  }
}
</script>

<style scoped lang="scss">
.paper-detail-container {
  .paper-detail-meta {
    margin-bottom: 4px;
  }
}
</style>
