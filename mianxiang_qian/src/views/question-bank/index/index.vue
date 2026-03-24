<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :inline="true" :model="queryParams" class="search-form" size="small">
        <el-form-item label="题目标题">
          <el-input
            v-model="queryParams.questionTitle"
            placeholder="请输入题目标题"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select
            v-model="queryParams.questionType"
            placeholder="请选择题目类型"
            clearable
          >
            <el-option
              :key="1"
              :label="'单选题'"
              :value="1"
            />
            <el-option
              :key="2"
              :label="'多选题'"
              :value="2"
            />
            <el-option
              :key="3"
              :label="'判断题'"
              :value="3"
            />
            <el-option
              :key="4"
              :label="'填空题'"
              :value="4"
            />
            <el-option
              :key="5"
              :label="'主观题'"
              :value="5"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级">
          <el-select
            v-model="queryParams.difficultyLevel"
            placeholder="请选择难度等级"
            clearable
          >
            <el-option
              :key="1"
              :label="'低'"
              :value="1"
            />
            <el-option
              :key="2"
              :label="'中'"
              :value="2"
            />
            <el-option
              :key="3"
              :label="'高'"
              :value="3"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择题目状态"
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
        <el-form-item label="课程名称">
          <el-input
            v-model="queryParams.courseName"
            placeholder="请输入课程名称"
            clearable
            @keyup.enter.native="handleQuery"
          />
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
        :data="eduQuestionBankList"
        @selection-change="handleSelectionChange"
        @expand-change="handleExpandChange"
        row-key="id"
        class="modern-table"
      >
      <el-table-column type="expand">
        <template slot-scope="scope">
          <div class="expand-options-wrapper">
            <div v-if="!hasOptions(scope.row)" class="expand-no-options">
              本题无选项（判断题/主观题无选项）
            </div>
            <template v-else>
              <div class="expand-options-header">
                <span class="expand-title">选项信息</span>
                <el-button type="primary" size="mini" icon="el-icon-plus" @click="handleAddOption(scope.row)">添加选项</el-button>
              </div>
              <el-table
                :data="getOptionsForRow(scope.row)"
                border
                size="small"
                class="expand-options-table"
              >
                <el-table-column label="选项标识" prop="optionLabel" width="100" align="center" />
                <el-table-column label="选项内容" prop="optionContent" align="left" min-width="200" :show-overflow-tooltip="true" />
                <el-table-column label="是否正确" width="90" align="center">
                  <template slot-scope="optScope">
                    <el-tag :type="optScope.row.isCorrect === 1 ? 'success' : 'info'" size="small">
                      {{ optScope.row.isCorrect === 1 ? '正确' : '错误' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="排序" prop="sortOrder" width="70" align="center" />
                <el-table-column label="操作" width="140" align="center" fixed="right">
                  <template slot-scope="optScope">
                    <el-button type="text" size="small" @click="handleEditOption(scope.row, optScope.row)">编辑</el-button>
                    <el-button type="text" size="small" style="color: #f56c6c;" @click="handleDeleteOption(scope.row, optScope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </template>
          </div>
        </template>
      </el-table-column>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="课程名称" prop="courseName" align="center" :show-overflow-tooltip="true" min-width="120" />
      <el-table-column label="章节名称" prop="chapterName" align="center" :show-overflow-tooltip="true" min-width="120" />
      <el-table-column label="题目标题" prop="questionTitle" align="center" :show-overflow-tooltip="true" min-width="160" />
      <el-table-column label="题目类型" prop="questionType" align="center" width="90">
        <template slot-scope="scope">
          <span>{{ getEnumLabel(scope.row.questionType, questionTypeEnumMap) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="难度等级" prop="difficultyLevel" align="center" width="90">
        <template slot-scope="scope">
          <span>{{ getEnumLabel(scope.row.difficultyLevel, difficultyLevelEnumMap) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="题目状态" prop="status" align="center" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ getEnumLabel(scope.row.status, statusEnumMap) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="答案内容" prop="answerContent" align="center" :show-overflow-tooltip="true" min-width="120" />
      <el-table-column label="解析" prop="analysisContent" align="center" :show-overflow-tooltip="true" min-width="120" />
      <el-table-column label="题目分值" prop="score" align="center" width="90" />
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="180" fixed="right" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            type="text"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            type="text"
            style="color: #f56c6c;"
            @click="handleDelete(scope.row)"
          >删除</el-button>
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
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择所属课程" clearable style="width: 100%;">
            <el-option
              v-for="(item, idx) in eduCourseOptions"
              :key="idx"
              :label="item.courseName"
              :value="item.id"
            />
          </el-select>
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
        <el-form-item label="题目标题" prop="questionTitle">
          <el-input v-model="form.questionTitle" placeholder="请输入题目标题" />
        </el-form-item>
        <el-form-item label="题目类型" prop="questionType">
          <el-select v-model="form.questionType" placeholder="请选择题目类型" clearable style="width: 100%;">
            <el-option
              v-for="(item, index) in questionTypeOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="难度等级" prop="difficultyLevel">
          <el-select v-model="form.difficultyLevel" placeholder="请选择难度等级" clearable style="width: 100%;">
            <el-option
              v-for="(item, index) in difficultyLevelOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="答案内容" prop="answerContent">
          <el-input
            v-model="form.answerContent"
            type="textarea"
            :rows="3"
            placeholder="请输入答案内容"
          />
        </el-form-item>
        <el-form-item label="解析" prop="analysisContent">
          <el-input
            v-model="form.analysisContent"
            type="textarea"
            :rows="3"
            placeholder="请输入解析"
          />
        </el-form-item>
        <el-form-item label="题目分值" prop="score">
          <el-input v-model.number="form.score" type="number" placeholder="请输入题目分值" />
        </el-form-item>
        <el-form-item label="题目状态" prop="status">
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

    <!-- 选项新增/编辑对话框 -->
    <el-dialog :title="optionDialogTitle" :visible.sync="optionDialogVisible" width="500px" append-to-body>
      <el-form ref="optionFormRef" :model="optionForm" :rules="optionRules" label-width="100px">
        <el-form-item v-if="optionForm.id" label="选项标识" prop="optionLabel">
          <el-input v-model="optionForm.optionLabel" placeholder="如 A、B 或 填空1" />
        </el-form-item>
        <el-form-item v-if="!optionForm.id" label="选项标识" prop="optionLabel">
          <el-input v-model="optionForm.optionLabel" :placeholder="optionLabelPlaceholder" />
        </el-form-item>
        <el-form-item label="选项内容" prop="optionContent">
          <el-input v-model="optionForm.optionContent" type="textarea" :rows="2" placeholder="请输入选项内容" />
        </el-form-item>
        <el-form-item v-if="currentOptionQuestionType !== 4" label="是否正确" prop="isCorrect">
          <el-radio-group v-model="optionForm.isCorrect">
            <el-radio :label="0">错误</el-radio>
            <el-radio :label="1">正确</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="optionForm.sortOrder" :min="1" style="width: 120px;" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitOptionForm">确 定</el-button>
        <el-button @click="optionDialogVisible = false">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import {
  pageEduQuestionBank,
  getEduQuestionBankById,
  saveEduQuestionBank,
  updateEduQuestionBank,
  deleteEduQuestionBank,
  deleteBatchEduQuestionBank,
  listEduCourse,
  listEduCourseChapter,
} from '@/api/edu_question_bank'
import {
  listEduQuestionOption,
  saveEduQuestionOption,
  updateEduQuestionOption,
  deleteEduQuestionOption
} from '@/api/edu_question_option'
import ImgUpload from '@/components/ImgUpload'
import ImagePreview from '@/components/ImagePreview'
import Pagination from '@/components/Pagination'
import BasicEditor from '@/components/BasicEditor'
import { mapGetters } from 'vuex'



export default {
  name: 'EduQuestionBank',
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
      eduQuestionBankList: [],
      title: '',
      open: false,
      queryParams: {
        current: 1,
        size: 10,
        questionTitle:  null,
        questionType:  null,
        difficultyLevel:  null,
        status:  null,
        courseName:  null,
        chapterName:  null
      },
      form: {},
      rules: {
        courseId: [
          { required: true, message: '所属课程不能为空', trigger: 'change' }
        ],
        chapterId: [
          { required: true, message: '所属章节不能为空', trigger: 'change' }
        ],
        questionTitle: [
          { required: true, message: '题目标题不能为空', trigger: 'blur' }
        ],
        questionType: [
          { required: true, message: '题目类型不能为空', trigger: 'change' }
        ],
        difficultyLevel: [
          { required: true, message: '难度等级不能为空', trigger: 'change' }
        ],
        answerContent: [
          { required: true, message: '答案内容不能为空', trigger: 'blur' }
        ],
        analysisContent: [
          { required: true, message: '解析不能为空', trigger: 'blur' }
        ],
        score: [
          { required: true, message: '题目分值不能为空', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '题目状态不能为空', trigger: 'change' }
        ]
      },
      eduCourseOptions: [],
      eduCourseChapterOptions: [],
      /** 展开行：题目ID -> 选项列表 */
      expandOptionsMap: {},
      /** 选项弹窗 */
      optionDialogVisible: false,
      optionDialogTitle: '添加选项',
      optionForm: {
        id: null,
        questionId: null,
        optionLabel: '',
        optionContent: '',
        isCorrect: 0,
        sortOrder: 1
      },
      currentOptionQuestionId: null,
      currentOptionQuestionType: null,
      optionRules: {
        optionLabel: [{ required: true, message: '请输入选项标识', trigger: 'blur' }],
        optionContent: [{ required: true, message: '请输入选项内容', trigger: 'blur' }]
      },

      questionTypeOptions: [{value: 1, label: '单选题'},{value: 2, label: '多选题'},{value: 3, label: '判断题'},{value: 4, label: '填空题'},{value: 5, label: '主观题'}],
      difficultyLevelOptions: [{value: 1, label: '低'},{value: 2, label: '中'},{value: 3, label: '高'}],
      statusOptions: [{value: 0, label: '停用'},{value: 1, label: '启用'}],

      questionTypeEnumMap: { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '主观题' },
      difficultyLevelEnumMap: { 1: '低', 2: '中', 3: '高' },
      statusEnumMap: { 0: '停用', 1: '启用' },

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
    optionLabelPlaceholder() {
      if (this.currentOptionQuestionType === 4) return '如 填空1、填空2'
      return '如 A、B、C、D'
    }
  },
  created() {
    this.getList()
    this.getEduCourseOptions({})
    this.getEduCourseChapterOptions({})
  },
  methods: {
    /** 查询章节题库列表（老师只能查看自己所属课程下的题目） */
    getList() {
      this.loading = true
      const { current, size, ...rest } = this.queryParams
      const entity = { ...rest }
      if (this.userRole === 'teacher' && this.userId) {
        entity.teacherId = this.userId
      }
      pageEduQuestionBank(current, size, entity).then(response => {
        const pageData = response.data
        this.eduQuestionBankList = pageData.records || []
        this.total = Number(pageData.total) || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 获取所属课程下拉选项 */
    getEduCourseOptions(params) {
      listEduCourse(params || {}).then(response => {
        this.eduCourseOptions = response.data || []
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
      this.queryParams.size = this.queryParams.size || 10
      this.queryParams.questionTitle = null
      this.queryParams.questionType = null
      this.queryParams.difficultyLevel = null
      this.queryParams.status = null
      this.queryParams.courseName = null
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
      this.title = '添加章节题库'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids[0]
      getEduQuestionBankById(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改章节题库'
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateEduQuestionBank(this.form).then(response => {
              this.$message.success('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            saveEduQuestionBank(this.form).then(response => {
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
        return deleteEduQuestionBank(ids)
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
        return deleteBatchEduQuestionBank(ids)
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

    /** 是否有选项（单选、多选、填空有选项） */
    hasOptions(row) {
      const t = row.questionType
      return t === 1 || t === 2 || t === 4
    },
    /** 获取某行的选项列表（用于展开行） */
    getOptionsForRow(row) {
      const list = this.expandOptionsMap[row.id]
      if (!list) return []
      return list.slice().sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    },
    /** 展开行变化时加载选项 */
    handleExpandChange(row, expandedRows) {
      const isExpanded = expandedRows.some(r => r.id === row.id)
      if (isExpanded && this.hasOptions(row) && !this.expandOptionsMap[row.id]) {
        this.loadOptionsForQuestion(row.id)
      }
    },
    /** 加载某题的选项并写入 expandOptionsMap */
    loadOptionsForQuestion(questionId) {
      listEduQuestionOption({ questionId }).then(res => {
        const list = (res.data || []).map(opt => ({
          id: opt.id,
          questionId: opt.questionId,
          optionLabel: opt.optionLabel || opt.option_label || '',
          optionContent: opt.optionContent || opt.option_content || '',
          isCorrect: opt.isCorrect !== undefined && opt.isCorrect !== null ? opt.isCorrect : (opt.is_correct || 0),
          sortOrder: opt.sortOrder || opt.sort_order || 0
        }))
        this.$set(this.expandOptionsMap, questionId, list)
      }).catch(() => {
        this.$set(this.expandOptionsMap, questionId, [])
      })
    },
    /** 添加选项 */
    handleAddOption(row) {
      this.currentOptionQuestionId = row.id
      this.currentOptionQuestionType = row.questionType
      const nextSort = (this.getOptionsForRow(row).length + 1)
      this.optionForm = {
        id: null,
        questionId: row.id,
        optionLabel: this.getDefaultOptionLabel(row.questionType, nextSort - 1),
        optionContent: '',
        isCorrect: 0,
        sortOrder: nextSort
      }
      this.optionDialogTitle = '添加选项'
      this.optionDialogVisible = true
      this.$nextTick(() => this.$refs.optionFormRef && this.$refs.optionFormRef.clearValidate())
    },
    /** 编辑选项 */
    handleEditOption(row, option) {
      this.currentOptionQuestionId = row.id
      this.currentOptionQuestionType = row.questionType
      this.optionForm = {
        id: option.id,
        questionId: row.id,
        optionLabel: option.optionLabel,
        optionContent: option.optionContent,
        isCorrect: option.isCorrect === 1 ? 1 : 0,
        sortOrder: option.sortOrder || 1
      }
      this.optionDialogTitle = '编辑选项'
      this.optionDialogVisible = true
      this.$nextTick(() => this.$refs.optionFormRef && this.$refs.optionFormRef.clearValidate())
    },
    /** 删除选项 */
    handleDeleteOption(row, option) {
      this.$confirm('确认删除该选项？', '提示', {
        type: 'warning'
      }).then(() => {
        return deleteEduQuestionOption(option.id)
      }).then(() => {
        this.loadOptionsForQuestion(row.id)
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    /** 提交选项表单（新增/编辑） */
    submitOptionForm() {
      this.$refs.optionFormRef.validate(valid => {
        if (!valid) return
        const payload = {
          questionId: this.optionForm.questionId,
          optionLabel: this.optionForm.optionLabel,
          optionContent: this.optionForm.optionContent,
          isCorrect: this.optionForm.isCorrect,
          sortOrder: this.optionForm.sortOrder
        }
        if (this.optionForm.id) {
          payload.id = this.optionForm.id
          updateEduQuestionOption(payload).then(() => {
            this.$message.success('修改成功')
            this.optionDialogVisible = false
            this.loadOptionsForQuestion(this.currentOptionQuestionId)
          }).catch(() => {})
        } else {
          saveEduQuestionOption(payload).then(() => {
            this.$message.success('添加成功')
            this.optionDialogVisible = false
            this.loadOptionsForQuestion(this.currentOptionQuestionId)
          }).catch(() => {})
        }
      })
    },
    /** 默认选项标识：填空为填空1/填空2，否则 A/B/C */
    getDefaultOptionLabel(questionType, index) {
      if (questionType === 4) return `填空${index + 1}`
      return String.fromCharCode(65 + index)
    },


      // 文件选择变化
      handleFileChange(file) {
        this.importForm.file = file.raw
      },

      // 超出文件数量限制
      handleExceed() {
        this.$message.warning('只能上传一个文件')
      },

      // 下载模板
      downloadTemplate() {
        // 创建一个 Excel 模板下载
        const template = [
          ['题目标题', '题目类型', '难度等级', '答案内容', '解析', '分值'],
          ['Java 的基本数据类型有哪些？', '单选', '简单', 'A', '八种基本类型包括 byte, short, int, long, float, double, char, boolean', '5'],
          ['示例题目2', '多选', '中等', 'A,B', '解析内容', '5']
        ]

        // 使用简单的 CSV 格式下载（实际项目中可以使用 xlsx 库）
        let csvContent = template.map(row => row.join(',')).join('\n')
        const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = 'question_import_template.csv'
        link.click()
      },

      // 执行导入
      async handleImport() {
        if (!this.importForm.courseId) {
          this.$message.warning('请选择所属课程')
          return
        }
        if (!this.importForm.chapterId) {
          this.$message.warning('请选择所属章节')
          return
        }
        if (!this.importForm.file) {
          this.$message.warning('请选择要上传的文件')
          return
        }

        const isExcel = this.importForm.file.name.endsWith('.xlsx') || this.importForm.file.name.endsWith('.xls')
        if (!isExcel) {
          this.$message.warning('请上传 Excel 文件（.xlsx 或 .xls）')
          return
        }

        this.importing = true

        try {
          const res = await importEduQuestionBank(
            this.importForm.file,
            this.importForm.courseId,
            this.importForm.chapterId
          )

          this.$message.success(res.message || '导入成功')
          this.importDialogVisible = false
          this.getList() // 刷新列表

        } catch (error) {
          this.$message.error(error.message || '导入失败')
        } finally {
          this.importing = false
        }
      }

  }
}
</script>

<style scoped lang="scss">
.expand-options-wrapper {
  padding: 12px 20px;
  background: #fafbfc;
  border-left: 3px solid #409EFF;
  margin: 0 0 0 55px;
  .expand-no-options {
    color: #909399;
    font-size: 13px;
  }
  .expand-options-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    .expand-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
  }
  .expand-options-table {
    max-width: 900px;
  }
}
</style>
