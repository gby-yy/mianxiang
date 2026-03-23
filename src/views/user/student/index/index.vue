<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <div class="search-wrapper">
      <el-form :inline="true" :model="queryParams" class="search-form" size="small">
        <el-form-item label="登录账号">
          <el-input
            v-model="queryParams.username"
            placeholder="请输入登录账号"
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
        <el-form-item label="手机号">
          <el-input
            v-model="queryParams.phone"
            placeholder="请输入手机号"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="年级">
          <el-input
            v-model="queryParams.grade"
            placeholder="请输入年级"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="专业">
          <el-input
            v-model="queryParams.major"
            placeholder="请输入专业"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="账号状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择账号状态"
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
        <el-form-item class="search-buttons">
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button type="primary" @click="handleAdd">新建学生</el-button>
      <el-button :disabled="multiple" @click="handleDeleteBatch">批量删除</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
      <el-table
        v-loading="loading"
        :data="eduStudentUserList"
        @selection-change="handleSelectionChange"
        class="modern-table"
      >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="登录账号" prop="username"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="学生姓名" prop="realName"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="手机号" prop="phone"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="邮箱" prop="email"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="头像" prop="avatar" width="100" align="center">
        <template slot-scope="scope">
          <image-preview v-if="scope.row.avatar" :src="scope.row.avatar" width="60px" height="60px" />
          <span v-else style="color: #909399;">暂无图片</span>
        </template>
      </el-table-column>
      <el-table-column label="年级" prop="grade"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="专业" prop="major"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="个人简介" prop="intro"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="账号状态" prop="status"  align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ getEnumLabel(scope.row.status, statusEnumMap) }}
          </el-tag>
        </template>
      </el-table-column>
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
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入登录密码" />
        </el-form-item>
        <el-form-item label="学生姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <img-upload v-model="form.avatar" :limit="1" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" placeholder="请输入年级" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="个人简介" prop="intro">
          <el-input
            v-model="form.intro"
            type="textarea"
            :rows="3"
            placeholder="请输入个人简介"
          />
        </el-form-item>
        <el-form-item label="账号状态" prop="status">
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

  </div>
</template>

<script>
import {
  pageEduStudentUser,
  getEduStudentUserById,
  saveEduStudentUser,
  updateEduStudentUser,
  deleteEduStudentUser,
  deleteBatchEduStudentUser
} from '@/api/edu_student_user'
import ImgUpload from '@/components/ImgUpload'
import ImagePreview from '@/components/ImagePreview'
import Pagination from '@/components/Pagination'
import BasicEditor from '@/components/BasicEditor'

export default {
  name: 'EduStudentUser',
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
      eduStudentUserList: [],
      title: '',
      open: false,
      queryParams: {
        current: 1,
        size: 5,
        username:  null,
        realName:  null,
        phone:  null,
        grade:  null,
        major:  null,
        status:  null
      },
      form: {},
      rules: {
        username: [
          { required: true, message: '登录账号不能为空', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '登录密码不能为空', trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '学生姓名不能为空', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '手机号不能为空', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '邮箱不能为空', trigger: 'blur' }
        ],
        avatar: [
          { required: true, message: '头像不能为空', trigger: 'blur' }
        ],
        grade: [
          { required: true, message: '年级不能为空', trigger: 'blur' }
        ],
        major: [
          { required: true, message: '专业不能为空', trigger: 'blur' }
        ],
        intro: [
          { required: true, message: '个人简介不能为空', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '账号状态不能为空', trigger: 'change' }
        ]
      }
,
      statusOptions: [{value: 0, label: '停用'},{value: 1, label: '启用'}],

      statusEnumMap: { 0: '停用', 1: '启用' },
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询学生列表（范围查询绑定 xxxRange，传参拆成 xxxStart/xxxEnd） */
    getList() {
      this.loading = true
      const { current, size, ...rest } = this.queryParams
      const entity = { ...rest }
      pageEduStudentUser(current, size, entity).then(response => {
        const pageData = response.data
        this.eduStudentUserList = pageData.records || []
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
      this.queryParams.username = null
      this.queryParams.realName = null
      this.queryParams.phone = null
      this.queryParams.grade = null
      this.queryParams.major = null
      this.queryParams.status = null
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
      this.title = '添加学生'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids[0]
      getEduStudentUserById(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改学生'
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateEduStudentUser(this.form).then(response => {
              this.$message.success('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            saveEduStudentUser(this.form).then(response => {
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
        return deleteEduStudentUser(ids)
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
        return deleteBatchEduStudentUser(ids)
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
    }
  }
}
</script>

<style scoped lang="scss">
</style>
