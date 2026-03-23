<template>
  <student-auth-bg>
    <div class="auth-card register-card">
      <div class="card-header">
        <div class="header-icon">
          <i class="el-icon-edit-outline" />
        </div>
        <h1 class="title">学生注册</h1>
        <p class="subtitle">填写信息完成注册</p>
        <p class="hint">带 <span class="required-dot">*</span> 为必填项，其余选填</p>
      </div>
      <el-form ref="form" :model="form" :rules="rules" class="form" label-position="top">
        <div class="form-scroll">
          <div class="form-grid">
            <el-form-item label="登录账号" prop="username" class="half">
              <el-input v-model="form.username" placeholder="用于登录，建议字母或数字" size="medium" />
            </el-form-item>
            <el-form-item label="姓名" prop="realName" class="half">
              <el-input v-model="form.realName" placeholder="真实姓名" size="medium" />
            </el-form-item>
            <el-form-item label="密码" prop="password" class="half">
              <el-input v-model="form.password" type="password" placeholder="不少于6位" show-password size="medium" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword" class="half">
              <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" show-password size="medium" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone" class="half">
              <el-input v-model="form.phone" placeholder="选填" size="medium" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email" class="half">
              <el-input v-model="form.email" placeholder="选填" size="medium" />
            </el-form-item>
            <el-form-item label="年级" prop="grade" class="half">
              <el-input v-model="form.grade" placeholder="如：2024级" size="medium" />
            </el-form-item>
            <el-form-item label="专业" prop="major" class="half">
              <el-input v-model="form.major" placeholder="选填" size="medium" />
            </el-form-item>
            <el-form-item label="个人简介" prop="intro" class="full">
              <el-input v-model="form.intro" type="textarea" :rows="2" placeholder="选填" size="medium" />
            </el-form-item>
          </div>
        </div>
        <el-form-item class="btn-wrap">
          <el-button type="primary" :loading="loading" class="submit-btn" @click="handleRegister">
            <i class="el-icon-check" style="margin-right: 6px;" />
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="form-divider">
        <span class="line" />
        <span class="text">或</span>
        <span class="line" />
      </div>
      <div class="footer-links">
        <router-link to="/student/login" class="link-item">
          <i class="el-icon-right" />
          已有账号？去登录
        </router-link>
        <router-link to="/login" class="link-item">
          <i class="el-icon-s-custom" />
          管理后台
        </router-link>
      </div>
    </div>
  </student-auth-bg>
</template>

<script>
import StudentAuthBg from '@/components/StudentAuthBg'
import { studentRegister } from '@/api/student'
import CryptoJS from 'crypto-js'

export default {
  name: 'StudentRegister',
  components: { StudentAuthBg },
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        realName: '',
        phone: '',
        email: '',
        grade: '',
        major: '',
        intro: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入登录账号', trigger: 'blur' },
          { min: 2, max: 32, message: '长度在 2 到 32 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码不少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    handleRegister() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.loading = true
        const encryptedPassword = CryptoJS.SHA256('intelligent_teaching_system' + this.form.password).toString()
        const payload = {
          username: this.form.username.trim(),
          password: encryptedPassword,
          realName: this.form.realName.trim() || undefined,
          phone: this.form.phone.trim() || undefined,
          email: this.form.email.trim() || undefined,
          grade: this.form.grade.trim() || undefined,
          major: this.form.major.trim() || undefined,
          intro: this.form.intro.trim() || undefined
        }
        studentRegister(payload).then(() => {
          this.$message.success('注册成功，请登录')
          this.$router.push('/student/login')
        }).finally(() => {
          this.loading = false
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.auth-card {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(255, 255, 255, 0.5);
  width: 100%;
  max-width: 560px;
}

.register-card {
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  padding: 32px 40px 28px;
  overflow: hidden;
  box-sizing: border-box;
}

.card-header {
  text-align: center;
  margin-bottom: 24px;
  flex-shrink: 0;

  .header-icon {
    width: 52px;
    height: 52px;
    margin: 0 auto 14px;
    border-radius: 14px;
    background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    i {
      font-size: 26px;
      color: #1677ff;
    }
  }

  .title {
    font-size: 22px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0 0 6px;
    letter-spacing: 0.5px;
  }

  .subtitle {
    font-size: 14px;
    color: #374151;
    margin: 0 0 4px;
  }

  .hint {
    font-size: 12px;
    color: #9ca3af;
    margin: 0;

    .required-dot {
      color: #f56c6c;
    }
  }
}

.form {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.form-scroll {
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  min-width: 0;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.15);
    border-radius: 2px;
  }
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 20px;

  .half {
    grid-column: span 1;
  }

  .full {
    grid-column: 1 / -1;
  }

  ::v-deep .el-form-item {
    margin-bottom: 20px;
  }
}

.form {
  ::v-deep .el-form-item__label {
    font-size: 13px;
    color: #4b5563;
    padding-bottom: 8px;
    font-weight: 500;
  }

  ::v-deep .el-input__inner,
  ::v-deep .el-select .el-input__inner {
    height: 42px;
    line-height: 42px;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
    font-size: 14px;

    &:focus {
      border-color: #4096ff;
      box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.15);
    }
  }

  .btn-wrap {
    margin-bottom: 0;
    margin-top: 8px;
    flex-shrink: 0;
  }

  .submit-btn {
    width: 100%;
    height: 46px;
    font-size: 15px;
    font-weight: 600;
    border-radius: 12px;
    border: none;
    background: linear-gradient(135deg, #4096ff 0%, #1677ff 100%);
    box-shadow: 0 4px 14px rgba(64, 150, 255, 0.4);

    &:hover {
      background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
      box-shadow: 0 6px 20px rgba(64, 150, 255, 0.45);
    }
  }
}

.form-divider {
  display: flex;
  align-items: center;
  margin: 20px 0 18px;
  gap: 14px;
  flex-shrink: 0;

  .line {
    flex: 1;
    height: 1px;
    background: linear-gradient(90deg, transparent, #e5e7eb, transparent);
  }

  .text {
    font-size: 12px;
    color: #9ca3af;
  }
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
  flex-shrink: 0;

  .link-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #6b7280;
    text-decoration: none;
    padding: 8px 14px;
    border-radius: 10px;
    transition: all 0.2s;
    border: 1px solid transparent;

    i {
      font-size: 14px;
    }

    &:hover {
      color: #4096ff;
      background: rgba(64, 150, 255, 0.08);
      border-color: rgba(64, 150, 255, 0.2);
    }
  }
}
</style>
