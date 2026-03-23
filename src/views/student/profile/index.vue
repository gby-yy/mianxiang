<template>
  <div class="profile-page">
    <!-- 顶部个人信息卡片 -->
    <div class="profile-hero">
      <div class="hero-bg" />
      <div class="hero-inner">
        <div class="avatar-wrap">
          <img v-if="form.avatar" :src="avatarDisplayUrl" alt="头像" class="avatar-img" />
          <div v-else class="avatar-placeholder">
            <i class="el-icon-user-solid avatar-icon" />
            <span class="avatar-tip">{{ displayName.charAt(0) || '学' }}</span>
          </div>
        </div>
        <h1 class="hero-name">{{ displayName }}</h1>
        <p class="hero-account">
          <i class="el-icon-user" />
          {{ form.username || '—' }}
        </p>
      </div>
    </div>

    <div class="profile-content">
      <!-- 个人信息 -->
      <div class="card card-profile">
        <div class="card-head">
          <i class="el-icon-edit-outline card-icon" />
          <span>个人信息</span>
        </div>
        <el-form
          ref="profileForm"
          :model="form"
          :rules="profileRules"
          label-width="100px"
          class="profile-form"
          @submit.native.prevent
        >
          <el-form-item label="登录账号" prop="username">
            <el-input v-model="form.username" disabled placeholder="—" />
          </el-form-item>
          <el-form-item label="姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入姓名" maxlength="20" show-word-limit clearable />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" clearable />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" clearable />
          </el-form-item>
          <el-form-item label="头像" prop="avatar">
            <img-upload v-model="form.avatar" :limit="1" :file-size="2" />
          </el-form-item>
          <el-form-item label="年级" prop="grade">
            <el-input v-model="form.grade" placeholder="如：2024级" clearable />
          </el-form-item>
          <el-form-item label="专业" prop="major">
            <el-input v-model="form.major" placeholder="请输入专业" clearable />
          </el-form-item>
          <el-form-item label="个人简介" prop="intro">
            <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="选填" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 修改密码 -->
      <div class="card card-password">
        <div class="card-head">
          <i class="el-icon-lock card-icon" />
          <span>修改密码</span>
        </div>
        <el-form
          ref="pwdForm"
          :model="pwdForm"
          :rules="pwdRules"
          label-width="100px"
          class="password-form"
          @submit.native.prevent
        >
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input
              v-model="pwdForm.oldPassword"
              type="password"
              placeholder="请输入当前密码"
              show-password
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="pwdForm.newPassword"
              type="password"
              placeholder="不少于6位"
              show-password
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="pwdForm.confirmPassword"
              type="password"
              placeholder="再次输入新密码"
              show-password
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="pwdSaving" @click="savePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import CryptoJS from 'crypto-js'
import ImgUpload from '@/components/ImgUpload'
import { getStudentProfile, updateStudentProfile, changeStudentPassword } from '@/api/student'

const PASSWORD_SALT = 'intelligent_teaching_system'

export default {
  name: 'StudentProfile',
  components: { ImgUpload },
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.pwdForm.newPassword) {
        callback(new Error('两次输入的新密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: {
        username: '',
        realName: '',
        phone: '',
        email: '',
        avatar: '',
        grade: '',
        major: '',
        intro: ''
      },
      profileRules: {},
      pwdForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      pwdRules: {
        oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码不少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      },
      profileSaving: false,
      pwdSaving: false
    }
  },
  computed: {
    avatarDisplayUrl() {
      const u = this.form.avatar
      if (!u) return ''
      if (u.startsWith('http') || u.startsWith('//')) return u
      const base = process.env.VUE_APP_BASE_API || ''
      return base.replace(/\/$/, '') + (u.startsWith('/') ? u : '/' + u)
    },
    displayName() {
      return this.form.realName && this.form.realName.trim()
        ? this.form.realName.trim()
        : (this.form.username || '未设置姓名')
    }
  },
  created() {
    this.fetchProfile()
  },
  methods: {
    fetchProfile() {
      getStudentProfile().then(res => {
        if (res.code === 200 && res.data) {
          const d = res.data
          this.form = {
            username: d.username || '',
            realName: d.realName || '',
            phone: d.phone || '',
            email: d.email || '',
            avatar: d.avatar || '',
            grade: d.grade || '',
            major: d.major || '',
            intro: d.intro || ''
          }
        }
      }).catch(() => {
        this.$message.error('获取个人信息失败')
      })
    },
    saveProfile() {
      this.$refs.profileForm.validate(valid => {
        if (!valid) return
        this.profileSaving = true
        updateStudentProfile({
          realName: this.form.realName,
          phone: this.form.phone,
          email: this.form.email,
          avatar: this.form.avatar,
          grade: this.form.grade,
          major: this.form.major,
          intro: this.form.intro
        }).then(res => {
          if (res.code === 200) {
            this.$message.success('保存成功')
            this.$store.dispatch('student/getInfo').catch(() => {})
          } else {
            this.$message.error(res.msg || '保存失败')
          }
        }).catch(() => {
          this.$message.error('保存失败')
        }).finally(() => {
          this.profileSaving = false
        })
      })
    },
    savePassword() {
      this.$refs.pwdForm.validate(valid => {
        if (!valid) return
        this.pwdSaving = true
        const oldHash = CryptoJS.SHA256(PASSWORD_SALT + this.pwdForm.oldPassword).toString()
        const newHash = CryptoJS.SHA256(PASSWORD_SALT + this.pwdForm.newPassword).toString()
        changeStudentPassword({
          oldPassword: oldHash,
          newPassword: newHash
        }).then(res => {
          if (res.code === 200) {
            this.$message.success('密码已修改，请重新登录')
            this.pwdForm.oldPassword = ''
            this.pwdForm.newPassword = ''
            this.pwdForm.confirmPassword = ''
            this.$refs.pwdForm.resetFields()
            this.$store.dispatch('student/logout').then(() => {
              this.$router.push('/student/login')
            })
          } else {
            this.$message.error(res.msg || '修改失败')
          }
        }).catch(() => {
          this.$message.error('修改失败')
        }).finally(() => {
          this.pwdSaving = false
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  padding-bottom: 40px;
}

.profile-hero {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 28px;
  min-height: 200px;
  box-shadow: 0 8px 32px rgba(99, 102, 241, 0.2);
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #4338ca 0%, #6366f1 40%, #8b5cf6 100%);
  opacity: 0.96;
}

.hero-inner {
  position: relative;
  padding: 36px 32px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.avatar-wrap {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.25);
  margin-bottom: 16px;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;

  .avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba(255, 255, 255, 0.95);
    .avatar-icon { font-size: 36px; }
    .avatar-tip { font-size: 24px; font-weight: 600; margin-left: 2px; }
  }
}

.hero-name {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.hero-account {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.profile-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 900px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
}

.card {
  background: #fff;
  border-radius: 18px;
  padding: 28px 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.25s ease;
  &:hover { box-shadow: 0 8px 28px rgba(0, 0, 0, 0.08); }
}

.card-head {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 17px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 22px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e0e7ff;

  .card-icon {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
  }
}

.card-profile .card-head {
  border-bottom-color: #e0e7ff;
  .card-icon { background: linear-gradient(135deg, #e0e7ff, #c7d2fe); color: #6366f1; }
}
.card-password .card-head {
  border-bottom-color: #fef3c7;
  .card-icon { background: linear-gradient(135deg, #fef3c7, #fde68a); color: #d97706; }
}

.profile-form,
.password-form {
  ::v-deep .el-form-item { margin-bottom: 18px; }
  ::v-deep .el-form-item:last-child { margin-bottom: 0; }
}
</style>
