<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :inline="true" :model="queryParams" class="search-form" size="small">
        <el-form-item label="课程名称">
          <el-input
            v-model="queryParams.courseName"
            placeholder="请输入课程名称"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="课程难度">
          <el-select
            v-model="queryParams.difficultyLevel"
            placeholder="请选择课程难度"
            clearable
          >
            <el-option
              :key="1"
              :label="'基础'"
              :value="1"
            />
            <el-option
              :key="2"
              :label="'中级'"
              :value="2"
            />
            <el-option
              :key="3"
              :label="'高级'"
              :value="3"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择课程状态"
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
        <el-form-item label="老师姓名">
          <el-input
            v-model="queryParams.realName"
            placeholder="请输入老师姓名"
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
      <el-button v-if="userRole !== 'admin'" type="primary" @click="handleAdd">新建课程</el-button>
      <el-button
        v-if="activeTab === 'draft' && userRole !== 'admin'"
        type="success"
        :disabled="submitAuditDisabled"
        @click="handleSubmitAuditFromSelection"
      >提交审核</el-button>
      <el-button :disabled="multiple" @click="handleDeleteBatch">批量删除</el-button>
    </div>

    <!-- Tab标签页（管理员不显示草稿） -->
    <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="course-tabs">
      <el-tab-pane v-if="userRole !== 'admin'" label="草稿" name="draft">
        <span slot="label">
          <i class="el-icon-edit"></i> 草稿
        </span>
      </el-tab-pane>
      <el-tab-pane label="审核通过" name="approved">
        <span slot="label">
          <i class="el-icon-check"></i> 审核通过
        </span>
      </el-tab-pane>
      <el-tab-pane label="审核中" name="pending">
        <span slot="label">
          <i class="el-icon-time"></i> 审核中
        </span>
      </el-tab-pane>
      <el-tab-pane label="审核不通过" name="rejected">
        <span slot="label">
          <i class="el-icon-close"></i> 审核不通过
        </span>
      </el-tab-pane>
    </el-tabs>

    <!-- 数据表格 -->
    <div class="table-wrapper">
      <el-table
        v-loading="loading"
        :data="eduCourseList"
        @selection-change="handleSelectionChange"
        class="modern-table"
      >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="课程编号" prop="courseCode"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="课程名称" prop="courseName"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="课程封面" prop="cover" width="120" align="center">
        <template slot-scope="scope">
          <image-preview v-if="scope.row.cover" :src="scope.row.cover" width="80px" height="60px" />
          <span v-else style="color: #909399;">暂无图片</span>
        </template>
      </el-table-column>
      <el-table-column label="课程简介" prop="courseDesc"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="课程难度" prop="difficultyLevel"  align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-tag :type="scope.row.difficultyLevel === 1 ? 'info' : scope.row.difficultyLevel === 2 ? 'warning' : 'danger'">
            {{ getEnumLabel(scope.row.difficultyLevel, difficultyLevelEnumMap) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="章节数量" prop="chapterCount"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="审核状态" prop="auditStatus"  align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-tag :type="scope.row.auditStatus === 0 ? 'warning' : scope.row.auditStatus === 1 ? 'success' : scope.row.auditStatus === 2 ? 'danger' : 'info'">
            {{ getEnumLabel(scope.row.auditStatus, auditStatusEnumMap) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核时间" prop="auditTime" width="180" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="课程状态" prop="status"  align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ getEnumLabel(scope.row.status, statusEnumMap) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" :show-overflow-tooltip="true" />
      <el-table-column label="老师姓名" prop="realName"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" width="460" fixed="right" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            type="text"
            @click="handleDetail(scope.row)"
          >详情</el-button>
          <el-button
            v-if="scope.row.auditStatus !== 2 && scope.row.auditStatus !== 0"
            type="text"
            :disabled="scope.row.status === 1"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            v-if="scope.row.auditStatus === 1 || scope.row.auditStatus === 2"
            type="text"
            style="color: #E6A23C;"
            @click="handleReEdit(scope.row)"
          >重新编辑</el-button>
          <el-button
            v-if="scope.row.auditStatus === 1 && scope.row.status === 0"
            type="text"
            style="color: #67C23A;"
            @click="handleEnableCourse(scope.row)"
          >启用</el-button>
          <el-button
            v-if="scope.row.auditStatus === 1 && scope.row.status === 1"
            type="text"
            style="color: #E6A23C;"
            @click="handleDisableCourse(scope.row)"
          >停用</el-button>
          <el-button
            v-if="scope.row.auditStatus === 3 && userRole !== 'admin'"
            type="text"
            style="color: #67C23A;"
            @click="handleSubmitAudit(scope.row)"
          >提交审核</el-button>
          <el-button
            v-if="userRole === 'admin' && scope.row.auditStatus === 0"
            type="text"
            style="color: #409EFF;"
            @click="handleAudit(scope.row)"
          >审核</el-button>
          <el-button
            type="text"
            style="color: #f56c6c;"
            :disabled="scope.row.status === 1 || scope.row.auditStatus === 0"
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

    <!-- 审核对话框 -->
    <el-dialog title="课程审核" :visible.sync="auditOpen" width="500px" append-to-body>
      <el-form ref="auditForm" :model="auditForm" :rules="auditRules" label-width="100px">
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="auditForm.auditStatus === 2"
          label="驳回原因"
          prop="rejectReason"
        >
          <el-input
            v-model="auditForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAudit">确 定</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 课程详情对话框 -->
    <el-dialog title="课程详情" :visible.sync="detailOpen" width="40%" append-to-body class="course-detail-dialog">
      <div v-if="courseDetail.course.id" class="course-detail-container">
        <!-- 基础信息部分 -->
        <div class="detail-section">
          <h3 class="section-title">
            <i class="el-icon-info"></i> 基础信息
          </h3>
          <el-row :gutter="20" class="detail-content">
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">课程编号：</span>
                <span class="value">{{ courseDetail.course.courseCode }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">课程名称：</span>
                <span class="value">{{ courseDetail.course.courseName }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">课程难度：</span>
                <el-tag :type="courseDetail.course.difficultyLevel === 1 ? 'info' : courseDetail.course.difficultyLevel === 2 ? 'warning' : 'danger'">
                  {{ getEnumLabel(courseDetail.course.difficultyLevel, difficultyLevelEnumMap) }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="label">老师姓名：</span>
                <span class="value">{{ courseDetail.course.realName }}</span>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="detail-item">
                <span class="label">课程封面：</span>
                <image-preview v-if="courseDetail.course.cover" :src="courseDetail.course.cover" width="160px" height="120px" />
                <span v-else style="color: #909399;">暂无图片</span>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="detail-item">
                <span class="label">课程简介：</span>
                <span class="value">{{ courseDetail.course.courseDesc || '暂无' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 驳回信息部分（仅审核不通过时显示） -->
        <div v-if="courseDetail.course.auditStatus === 2" class="detail-section reject-section">
          <h3 class="section-title">
            <i class="el-icon-warning"></i> 驳回信息
          </h3>
          <el-row :gutter="20" class="detail-content">
            <el-col :span="24">
              <div class="detail-item">
                <span class="label">驳回原因：</span>
                <div class="reject-reason">
                  <el-alert
                    :title="courseDetail.course.rejectReason || '暂无驳回原因'"
                    type="error"
                    :closable="false"
                    show-icon
                  />
                </div>
              </div>
            </el-col>
            <el-col :span="24" v-if="courseDetail.course.auditTime">
              <div class="detail-item">
                <span class="label">审核时间：</span>
                <span class="value">{{ courseDetail.course.auditTime }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 章节信息部分 -->
        <div class="detail-section">
          <h3 class="section-title">
            <i class="el-icon-menu"></i> 章节信息
          </h3>
          <el-table :data="courseDetail.chapters" border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="章节名称" prop="chapterName" align="center" />
            <el-table-column label="章节类型" prop="chapterType" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.chapterType === 1 ? 'success' : 'warning'">
                  {{ scope.row.chapterType === 1 ? '基础章节' : '有难度章节' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="章节难度" prop="difficultyLevel" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.difficultyLevel === 1 ? 'info' : scope.row.difficultyLevel === 2 ? 'warning' : 'danger'">
                  {{ getEnumLabel(scope.row.difficultyLevel, difficultyLevelEnumMap) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="课件数量" prop="contentCount" align="center" />
            <el-table-column label="测试题数量" prop="questionCount" align="center" />
            <el-table-column label="试卷列表" align="center" min-width="200">
              <template slot-scope="scope">
                <div v-if="scope.row.papers && scope.row.papers.length > 0">
                  <el-tag
                    v-for="paper in scope.row.papers"
                    :key="paper.id"
                    size="small"
                    style="margin-right: 5px; margin-bottom: 5px;"
                  >
                    {{ paper.paperName }}
                  </el-tag>
                </div>
                <span v-else style="color: #909399;">暂无试卷</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 添加或修改对话框（有富文本/MD 时宽度 50%，否则 600px） -->
    <el-dialog :title="title" :visible.sync="open"  width="600px"  append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程编号" prop="courseCode">
          <el-input 
            v-model="form.courseCode" 
            placeholder="新增课程时自动生成" 
            :disabled="true"
          />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程教师" prop="teacherId">
          <el-select 
            v-model="form.teacherId" 
            placeholder="请选择课程教师" 
            clearable 
            style="width: 100%;"
            :disabled="true"
          >
            <el-option
              v-for="(item, idx) in filteredTeacherOptions"
              :key="idx"
              :label="item.realName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程封面" prop="cover">
          <img-upload v-model="form.cover" :limit="1" />
        </el-form-item>
        <el-form-item label="课程简介" prop="courseDesc">
          <el-input
            v-model="form.courseDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入课程简介"
          />
        </el-form-item>
        <el-form-item label="课程难度" prop="difficultyLevel">
          <el-select v-model="form.difficultyLevel" placeholder="请选择课程难度" clearable style="width: 100%;">
            <el-option
              v-for="(item, index) in difficultyLevelOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import {
  pageEduCourse,
  getEduCourseById,
  saveEduCourse,
  updateEduCourse,
  deleteEduCourse,
  deleteBatchEduCourse,
  listEduUser
} from '@/api/edu_course'
import { listEduCourseChapter } from '@/api/edu_course_chapter'
import { pageEduChapterContent } from '@/api/edu_chapter_content'
import { pageEduQuestionBank } from '@/api/edu_question_bank'
import { pageEduChapterExamPaper } from '@/api/edu_chapter_exam_paper'
import { mapGetters } from 'vuex'
import ImgUpload from '@/components/ImgUpload'
import ImagePreview from '@/components/ImagePreview'
import Pagination from '@/components/Pagination'
import BasicEditor from '@/components/BasicEditor'

export default {
  name: 'EduCourse',
  components: {
    ImgUpload,
    ImagePreview,
    Pagination,
    BasicEditor
  },
  computed: {
    /** 过滤后的教师选项（排除管理员） */
    filteredTeacherOptions() {
      return (this.eduUserOptions || []).filter(user => user.roleType !== 'admin')
    },
    /** 提交审核按钮禁用：草稿 tab 下未勾选 exactly 一条课程时禁用 */
    submitAuditDisabled() {
      return this.single
    },
    ...mapGetters(['role', 'id']),
    userRole() {
      return this.role || ''
    },
    userId() {
      return this.id || null
    }
  },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      eduCourseList: [],
      title: '',
      open: false,
      detailOpen: false,
      auditOpen: false,
      auditForm: {
        auditStatus: 1,
        rejectReason: ''
      },
      courseDetail: {
        course: {},
        chapters: []
      },
      activeTab: 'draft', // 当前选中的tab，默认显示草稿
      queryParams: {
        current: 1,
        size: 5,
        courseName:  null,
        difficultyLevel:  null,
        auditStatus:  3, // 默认显示草稿状态的课程
        status:  null,
        realName:  null
      },
      form: {},
      rules: {
        courseName: [
          { required: true, message: '课程名称不能为空', trigger: 'blur' }
        ],
        teacherId: [
          { required: true, message: '课程教师不能为空', trigger: 'change' }
        ],
        cover: [
          { required: true, message: '课程封面不能为空', trigger: 'blur' }
        ],
        courseDesc: [
          { required: true, message: '课程简介不能为空', trigger: 'blur' }
        ],
        difficultyLevel: [
          { required: true, message: '课程难度不能为空', trigger: 'change' }
        ],
      },
      eduUserOptions: []

,
      difficultyLevelOptions: [{value: 1, label: '基础'},{value: 2, label: '中级'},{value: 3, label: '高级'}],
      auditStatusOptions: [{value: 0, label: '待审核'},{value: 1, label: '通过'},{value: 2, label: '驳回'}],
      statusOptions: [{value: 0, label: '停用'},{value: 1, label: '启用'}],

      difficultyLevelEnumMap: { 1: '基础', 2: '中级', 3: '高级' },
      auditStatusEnumMap: { 0: '待审核', 1: '通过', 2: '驳回', 3: '草稿' },
      statusEnumMap: { 0: '停用', 1: '启用' },
      auditRules: {
        auditStatus: [
          { required: true, message: '请选择审核结果', trigger: 'change' }
        ],
        rejectReason: [
          {
            validator: (rule, value, callback) => {
              if (this.auditForm.auditStatus === 2 && (!value || value.trim() === '')) {
                callback(new Error('驳回原因不能为空'))
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ]
      },
      currentAuditCourse: null
    }
  },
  watch: {
    userRole: {
      handler(role) {
        if (role === 'admin' && this.activeTab === 'draft') {
          this.activeTab = 'approved'
          this.queryParams.auditStatus = 1
          this.getList()
        }
      }
    }
  },
  created() {
    // 管理员不显示草稿 tab，默认显示审核通过
    if (this.userRole === 'admin') {
      this.activeTab = 'approved'
      this.queryParams.auditStatus = 1
    } else {
      this.setAuditStatusByTab(this.activeTab)
    }
    this.getList()
    this.getEduUserOptions({})
  },
  methods: {
    /** 查询课程列表（范围查询绑定 xxxRange，传参拆成 xxxStart/xxxEnd） */
    getList() {
      this.loading = true
      const { current, size, ...rest } = this.queryParams
      const entity = { ...rest }
      // 如果是老师角色，只查询该老师的课程
      if (this.userRole === 'teacher' && this.userId) {
        entity.teacherId = this.userId
      }
      pageEduCourse(current, size, entity).then(response => {
        const pageData = response.data
        this.eduCourseList = pageData.records || []
        this.total = Number(pageData.total) || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 获取课程教师下拉选项 */
    getEduUserOptions(params) {
      listEduUser(params || {}).then(response => {
        this.eduUserOptions = response.data || []
      })
    },
    /** 获取当前老师姓名 */
    getCurrentTeacherName() {
      if (this.userRole === 'teacher' && this.userId) {
        const currentTeacher = this.eduUserOptions.find(teacher => teacher.id === this.userId)
        return currentTeacher ? currentTeacher.realName : '当前用户'
      }
      return ''
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
      this.queryParams.courseName = null
      this.queryParams.difficultyLevel = null
      // 保持当前tab对应的审核状态
      this.setAuditStatusByTab(this.activeTab)
      this.queryParams.status = null
      this.queryParams.realName = null
      this.handleQuery()
    },
    /** Tab切换处理 */
    handleTabClick(tab) {
      this.setAuditStatusByTab(tab.name)
      this.queryParams.current = 1
      this.getList()
    },
    /** 根据tab设置审核状态 */
    setAuditStatusByTab(tabName) {
      switch (tabName) {
        case 'draft':
          this.queryParams.auditStatus = 3 // 草稿
          break
        case 'approved':
          this.queryParams.auditStatus = 1 // 审核通过
          break
        case 'pending':
          this.queryParams.auditStatus = 0 // 审核中（待审核）
          break
        case 'rejected':
          this.queryParams.auditStatus = 2 // 审核不通过（驳回）
          break
        default:
          this.queryParams.auditStatus = 3
      }
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
      // 新建课程默认状态为草稿（3）
      this.form.auditStatus = 3
      // 新建课程默认状态为停用（0）
      this.form.status = 0
      // 课程编号不设置，由后端自动生成
      this.form.courseCode = ''
      // 如果是老师，自动设置所属老师为当前登录的老师
      if (this.userRole === 'teacher' && this.userId) {
        this.form.teacherId = this.userId
      }
      this.open = true
      this.title = '添加课程'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      // 检查课程状态，启用状态不能修改
      if (row.status === 1) {
        this.$message.warning('启用状态的课程不能修改')
        return
      }
      this.reset()
      const id = row.id || this.ids[0]
      getEduCourseById(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改课程'
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateEduCourse(this.form).then(response => {
              this.$message.success('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            saveEduCourse(this.form).then(response => {
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
      // 检查课程状态，启用状态不能删除
      if (row.status === 1) {
        this.$message.warning('启用状态的课程不能删除')
        return
      }
      // 检查审核状态，审核中的课程不能删除
      if (row.auditStatus === 0) {
        this.$message.warning('审核中的课程不能删除')
        return
      }
      const ids = row.id || this.ids
      this.$confirm('是否确认删除编号为"' + ids + '"的数据项？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteEduCourse(ids)
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    /** 批量删除按钮操作 */
    handleDeleteBatch() {
      const ids = this.ids
      // 检查选中的课程中是否有启用状态的
      const selectedCourses = this.eduCourseList.filter(course => ids.includes(course.id))
      const enabledCourses = selectedCourses.filter(course => course.status === 1)
      if (enabledCourses.length > 0) {
        this.$message.warning('选中的课程中包含启用状态的课程，不能删除')
        return
      }
      // 检查选中的课程中是否有审核中的
      const auditingCourses = selectedCourses.filter(course => course.auditStatus === 0)
      if (auditingCourses.length > 0) {
        this.$message.warning('选中的课程中包含审核中的课程，不能删除')
        return
      }
      this.$confirm('是否确认删除选中的' + ids.length + '条数据？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteBatchEduCourse(ids)
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
    /** 从表格勾选触发提交审核（草稿 tab 顶部按钮） */
    handleSubmitAuditFromSelection() {
      if (this.ids.length !== 1) {
        this.$message.warning('请先勾选一条草稿课程')
        return
      }
      const row = this.eduCourseList.find(c => c.id === this.ids[0])
      if (row && row.auditStatus === 3) {
        this.handleSubmitAudit(row)
      } else {
        this.$message.warning('请选择草稿状态的课程')
      }
    },
    /** 提交审核按钮操作（校验：有章节、章节下有测试题和试卷） */
    handleSubmitAudit(row) {
      // 先验证课程数据完整性：有章节，且每章节下有测试题和试卷
      this.validateCourseData(row).then(() => {
        // 验证通过，弹出确认对话框
        this.$confirm('确认提交该课程进行审核吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }).then(() => {
          const updateData = {
            id: row.id,
            auditStatus: 0 // 改为待审核状态
          }
          updateEduCourse(updateData).then(response => {
            this.$message.success('提交审核成功')
            // 切换到审核中tab
            this.activeTab = 'pending'
            this.setAuditStatusByTab(this.activeTab)
            this.getList()
          }).catch(() => {
            this.$message.error('提交审核失败')
          })
        }).catch(() => {})
      }).catch(error => {
        // 验证失败，显示错误信息
        this.$message.error(error)
      })
    },
    /** 验证课程数据完整性 */
    validateCourseData(row) {
      return new Promise((resolve, reject) => {
        // 1. 检查是否有章节
        listEduCourseChapter({ courseId: row.id }).then(response => {
          const chapters = response.data || []
          if (chapters.length === 0) {
            reject('课程下没有章节，无法提交审核。请先添加章节。')
            return
          }
          
          // 2. 检查每个章节是否有测试题和试卷
          const validationPromises = chapters.map(chapter => {
            return Promise.all([
              // 检查测试题
              pageEduQuestionBank(1, 1000, { chapterId: chapter.id }).then(res => {
                const questions = res.data && res.data.records ? res.data.records : []
                return { chapter, questionCount: questions.length }
              }).catch(() => ({ chapter, questionCount: 0 })),
              // 检查试卷
              pageEduChapterExamPaper(1, 1000, { chapterId: chapter.id }).then(res => {
                const papers = res.data && res.data.records ? res.data.records : []
                return { chapter, paperCount: papers.length }
              }).catch(() => ({ chapter, paperCount: 0 }))
            ]).then(([questionResult, paperResult]) => {
              return {
                chapter: chapter,
                questionCount: questionResult.questionCount,
                paperCount: paperResult.paperCount
              }
            })
          })
          
          return Promise.all(validationPromises)
        }).then(results => {
          // 检查每个章节：必须同时有测试题和试卷
          const invalidChapters = []
          results.forEach(result => {
            const issues = []
            if (result.questionCount === 0) issues.push('没有测试题')
            if (result.paperCount === 0) issues.push('没有试卷')
            if (issues.length > 0) {
              invalidChapters.push({
                name: result.chapter.chapterName,
                issues: issues
              })
            }
          })
          
          if (invalidChapters.length > 0) {
            const msg = invalidChapters.map(item => `${item.name}：${item.issues.join('、')}`).join('；')
            reject(`以下章节数据不完整，无法提交审核：${msg}。请完善后再提交。`)
            return
          }
          
          // 所有验证通过
          resolve()
        }).catch(error => {
          if (typeof error === 'string') {
            reject(error)
          } else {
            reject('验证课程数据时发生错误，请稍后重试')
          }
        })
      })
    },
    /** 重新编辑按钮操作 */
    handleReEdit(row) {
      this.$confirm('确认将该课程重新编辑吗？重新编辑后课程状态将变为草稿，课程状态将变为停用，可以继续修改。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        const updateData = {
          id: row.id,
          auditStatus: 3, // 改为草稿状态
          status: 0 // 改为停用状态
        }
        updateEduCourse(updateData).then(response => {
          this.$message.success('重新编辑成功，课程已变为草稿状态并停用')
          // 切换到草稿tab
          this.activeTab = 'draft'
          this.setAuditStatusByTab(this.activeTab)
          this.getList()
        }).catch(() => {
          this.$message.error('重新编辑失败')
        })
      }).catch(() => {})
    },
    /** 审核按钮操作 */
    handleAudit(row) {
      this.currentAuditCourse = row
      this.auditForm = {
        auditStatus: 1,
        rejectReason: ''
      }
      this.auditOpen = true
    },
    /** 提交审核 */
    submitAudit() {
      this.$refs['auditForm'].validate(valid => {
        if (valid) {
          const updateData = {
            id: this.currentAuditCourse.id,
            auditStatus: this.auditForm.auditStatus,
            auditTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
          }
          if (this.auditForm.auditStatus === 2) {
            updateData.rejectReason = this.auditForm.rejectReason
          } else {
            updateData.rejectReason = null
          }
          updateEduCourse(updateData).then(response => {
            this.$message.success('审核成功')
            this.auditOpen = false
            // 根据审核结果切换到对应的tab
            if (this.auditForm.auditStatus === 1) {
              this.activeTab = 'approved'
            } else if (this.auditForm.auditStatus === 2) {
              this.activeTab = 'rejected'
            }
            this.setAuditStatusByTab(this.activeTab)
            this.getList()
          })
        }
      })
    },
    /** 启用课程 */
    handleEnableCourse(row) {
      this.$confirm('确认启用该课程吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        const updateData = {
          id: row.id,
          status: 1 // 启用
        }
        updateEduCourse(updateData).then(response => {
          this.$message.success('启用成功')
          this.getList()
        }).catch(() => {
          this.$message.error('启用失败')
        })
      }).catch(() => {})
    },
    /** 停用课程 */
    handleDisableCourse(row) {
      this.$confirm('确认停用该课程吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const updateData = {
          id: row.id,
          status: 0 // 停用
        }
        updateEduCourse(updateData).then(response => {
          this.$message.success('停用成功')
          this.getList()
        }).catch(() => {
          this.$message.error('停用失败')
        })
      }).catch(() => {})
    },
    /** 详情按钮操作 */
    handleDetail(row) {
      this.detailOpen = true
      this.courseDetail = {
        course: {},
        chapters: []
      }
      // 获取课程详情
      getEduCourseById(row.id).then(response => {
        this.courseDetail.course = response.data || {}
        // 获取章节列表
        return listEduCourseChapter({ courseId: row.id })
      }).then(response => {
        if (!response || !response.data) {
          this.courseDetail.chapters = []
          return Promise.resolve([])
        }
        const chapters = response.data || []
        if (chapters.length === 0) {
          this.courseDetail.chapters = []
          return Promise.resolve([])
        }
        // 为每个章节获取详细信息
        const chapterPromises = chapters.map(chapter => {
          return Promise.all([
            // 获取章节内容数量
            pageEduChapterContent(1, 1000, { chapterId: chapter.id }).then(res => {
              return res.data && res.data.records ? res.data.records.length : (chapter.contentCount || 0)
            }).catch(() => chapter.contentCount || 0),
            // 获取题目数量
            pageEduQuestionBank(1, 1000, { chapterId: chapter.id }).then(res => {
              return res.data && res.data.records ? res.data.records.length : (chapter.questionCount || 0)
            }).catch(() => chapter.questionCount || 0),
            // 获取试卷列表
            pageEduChapterExamPaper(1, 1000, { chapterId: chapter.id }).then(res => {
              return res.data && res.data.records ? res.data.records : []
            }).catch(() => [])
          ]).then(([contentCount, questionCount, papers]) => {
            return {
              ...chapter,
              contentCount: contentCount || 0,
              questionCount: questionCount || 0,
              papers: papers || []
            }
          })
        })
        return Promise.all(chapterPromises)
      }).then(chaptersWithDetails => {
        if (chaptersWithDetails) {
          this.courseDetail.chapters = chaptersWithDetails
        }
      }).catch((error) => {
        console.error('获取课程详情失败:', error)
        this.$message.error('获取课程详情失败')
      })
    }
  }
}
</script>

<style scoped lang="scss">
.course-tabs {
  margin: 20px 0;
  background: #fff;
  padding: 0 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  ::v-deep .el-tabs__header {
    margin: 0;
  }

  ::v-deep .el-tabs__item {
    font-size: 14px;
    padding: 0 20px;
    height: 50px;
    line-height: 50px;

    i {
      margin-right: 5px;
    }
  }

  ::v-deep .el-tabs__active-bar {
    height: 3px;
  }
}

.course-detail-container {
  .detail-section {
    margin-bottom: 30px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;

    .section-title {
      margin: 0 0 20px 0;
      padding-bottom: 10px;
      border-bottom: 2px solid #409EFF;
      color: #303133;
      font-size: 18px;
      font-weight: 600;

      i {
        margin-right: 8px;
        color: #409EFF;
      }
    }

    .detail-content {
      .detail-item {
        margin-bottom: 15px;
        line-height: 28px;

        .label {
          display: inline-block;
          min-width: 100px;
          color: #606266;
          font-weight: 500;
        }

        .value {
          color: #303133;
        }
      }
    }

    &.reject-section {
      background: #fef0f0;
      border: 1px solid #fde2e2;

      .section-title {
        border-bottom-color: #f56c6c;
        color: #f56c6c;

        i {
          color: #f56c6c;
        }
      }

      .reject-reason {
        margin-top: 10px;
      }
    }
  }
}

::v-deep .course-detail-dialog {
  .el-dialog__body {
    padding: 20px;
    max-height: 70vh;
    overflow-y: auto;
  }
}
</style>
