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
        <el-form-item label="姓名">
          <el-input
            v-model="queryParams.realName"
            placeholder="请输入姓名"
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
        <el-form-item label="性别">
          <el-select
            v-model="queryParams.gender"
            placeholder="请选择性别"
            clearable
          >
            <el-option
              :key="1"
              :label="'男'"
              :value="1"
            />
            <el-option
              :key="2"
              :label="'女'"
              :value="2"
            />
            <el-option
              :key="3"
              :label="'未知'"
              :value="3"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教师职称">
          <el-select
            v-model="queryParams.titleLevel"
            placeholder="请选择教师职称"
            clearable
          >
            <el-option
              :key="1"
              :label="'助教'"
              :value="1"
            />
            <el-option
              :key="2"
              :label="'讲师'"
              :value="2"
            />
            <el-option
              :key="3"
              :label="'副教授'"
              :value="3"
            />
            <el-option
              :key="4"
              :label="'教授'"
              :value="4"
            />
          </el-select>
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
      <el-button type="primary" @click="handleAdd">新建教师</el-button>
      <el-button :disabled="multiple" @click="handleDeleteBatch">批量删除</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrapper">
      <el-table
        v-loading="loading"
        :data="eduUserList"
        @selection-change="handleSelectionChange"
        class="modern-table"
      >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="登录账号" prop="username"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="姓名" prop="realName"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="用户角色" prop="roleType"  align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-tag :type="scope.row.roleType === 'admin' ? 'danger' : 'primary'">
            {{ getEnumLabel(scope.row.roleType, roleTypeEnumMap) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="手机号" prop="phone"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="邮箱" prop="email"  align="center" :show-overflow-tooltip="true" />
      <el-table-column label="头像" prop="avatar" width="100" align="center">
        <template slot-scope="scope">
          <image-preview v-if="scope.row.avatar" :src="scope.row.avatar" width="60px" height="60px" />
          <span v-else style="color: #909399;">暂无图片</span>
        </template>
      </el-table-column>
      <el-table-column label="性别" prop="gender"  align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-tag :type="scope.row.gender === 1 ? 'primary' : scope.row.gender === 2 ? 'danger' : 'info'">
            {{ getEnumLabel(scope.row.gender, genderEnumMap) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="教师职称" prop="titleLevel"  align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <el-tag :type="scope.row.titleLevel === 4 ? 'success' : scope.row.titleLevel === 3 ? 'warning' : 'info'">
            {{ getEnumLabel(scope.row.titleLevel, titleLevelEnumMap) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="教龄" prop="teachYears"  align="center" :show-overflow-tooltip="true" />
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
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
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
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别" clearable style="width: 100%;">
            <el-option
              v-for="(item, index) in genderOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教师职称" prop="titleLevel">
          <el-select v-model="form.titleLevel" placeholder="请选择教师职称" clearable style="width: 100%;">
            <el-option
              v-for="(item, index) in titleLevelOptions"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教龄" prop="teachYears">
          <el-input v-model.number="form.teachYears" type="number" placeholder="请输入教龄" />
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
  pageEduUser,
  getEduUserById,
  saveEduUser,
  updateEduUser,
  deleteEduUser,
  deleteBatchEduUser
} from '@/api/edu_user'
import ImgUpload from '@/components/ImgUpload'
import ImagePreview from '@/components/ImagePreview'
import Pagination from '@/components/Pagination'
import BasicEditor from '@/components/BasicEditor'

export default {
  name: 'EduUser',
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
      eduUserList: [],
      title: '',
      open: false,
      queryParams: {
        current: 1,
        size: 5,
        username:  null,
        realName:  null,
        roleType:  'teacher', // 固定为教师角色
        phone:  null,
        gender:  null,
        titleLevel:  null,
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
          { required: true, message: '姓名不能为空', trigger: 'blur' }
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
        gender: [
          { required: true, message: '性别不能为空', trigger: 'change' }
        ],
        titleLevel: [
          { required: true, message: '教师职称不能为空', trigger: 'change' }
        ],
        teachYears: [
          { required: true, message: '教龄不能为空', trigger: 'blur' }
        ],
        intro: [
          { required: true, message: '个人简介不能为空', trigger: 'blur' }
        ],
        status: [
          { required: true, message: '账号状态不能为空', trigger: 'change' }
        ]
      }
,
      roleTypeOptions: [{value: 'admin', label: '管理员'},{value: 'teacher', label: '教师'}],
      genderOptions: [{value: 1, label: '男'},{value: 2, label: '女'},{value: 3, label: '未知'}],
      titleLevelOptions: [{value: 1, label: '助教'},{value: 2, label: '讲师'},{value: 3, label: '副教授'},{value: 4, label: '教授'}],
      statusOptions: [{value: 0, label: '停用'},{value: 1, label: '启用'}],

      roleTypeEnumMap: { 'admin': '管理员', 'teacher': '教师' },
      genderEnumMap: { 1: '男', 2: '女', 3: '未知' },
      titleLevelEnumMap: { 1: '助教', 2: '讲师', 3: '副教授', 4: '教授' },
      statusEnumMap: { 0: '停用', 1: '启用' },
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询系统用户列表（范围查询绑定 xxxRange，传参拆成 xxxStart/xxxEnd） */
    getList() {
      this.loading = true
      const { current, size, ...rest } = this.queryParams
      const entity = { ...rest }
      // 强制设置为教师角色，确保只查询教师
      entity.roleType = 'teacher'
      pageEduUser(current, size, entity).then(response => {
        const pageData = response.data
        this.eduUserList = pageData.records || []
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
      this.queryParams.roleType = 'teacher' // 重置时保持为教师角色
      this.queryParams.phone = null
      this.queryParams.gender = null
      this.queryParams.titleLevel = null
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
      // 新增时默认设置为教师角色
      this.form.roleType = 'teacher'
      this.open = true
      this.title = '添加教师'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids[0]
      getEduUserById(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改教师'
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          // 新增时确保角色为教师
          if (this.form.id == null) {
            this.form.roleType = 'teacher'
          }
          if (this.form.id != null) {
            updateEduUser(this.form).then(response => {
              this.$message.success('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            saveEduUser(this.form).then(response => {
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
        return deleteEduUser(ids)
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
        return deleteBatchEduUser(ids)
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
