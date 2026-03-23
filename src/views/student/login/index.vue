<template>
  <student-auth-bg>
    <div class="auth-card login-card">
      <div class="card-header">
        <div class="header-icon">
          <i class="el-icon-notebook-2" />
        </div>
        <h1 class="title">智能教学系统</h1>
        <p class="subtitle">学生登录</p>
        <p class="hint">使用注册账号与密码登录</p>
      </div>
      <el-form ref="form" :model="form" :rules="rules" class="form" @keyup.enter.native="handleLogin">
        <el-form-item prop="username" label="账号">
          <el-input
            v-model="form.username"
            prefix-icon="el-icon-user"
            placeholder="请输入登录账号"
            size="medium"
          />
        </el-form-item>
        <el-form-item prop="password" label="密码">
          <el-input
            v-model="form.password"
            prefix-icon="el-icon-lock"
            :type="passwordType"
            placeholder="请输入密码（不少于6位）"
            show-password
            size="medium"
          />
        </el-form-item>
        <el-form-item class="options-row">
          <el-checkbox v-model="form.remember">记住账号</el-checkbox>
        </el-form-item>
        <el-form-item class="btn-wrap">
          <el-button type="primary" :loading="loading" class="submit-btn" @click="handleLogin">
            <i class="el-icon-right" style="margin-right: 6px;" />
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="form-divider">
        <span class="line" />
        <span class="text">或</span>
        <span class="line" />
      </div>
      <div class="footer-links">
        <router-link to="/student/register" class="link-item">
          <i class="el-icon-edit-outline" />
          注册账号
        </router-link>
        <span class="link-item" role="link" @click="goAdminLogin">
          <i class="el-icon-s-custom" />
          管理后台
        </span>
      </div>
    </div>
  </student-auth-bg>
</template>

<script>
import StudentAuthBg from '@/components/StudentAuthBg'

export default {
  name: 'StudentLogin',
  components: { StudentAuthBg },
  data() {
    return {
      form: {
        username: '',
        password: '',
        remember: false
      },
      rules: {
        username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码不少于6位', trigger: 'blur' }
        ]
      },
      passwordType: 'password',
      loading: false
    }
  },
  mounted() {
    const saved = localStorage.getItem('student_remember_username')
    if (saved) {
      this.form.username = saved
      this.form.remember = true
    }
  },
  methods: {
    goAdminLogin() {
      if (window.location.hash) {
        window.location.hash = '#/login'
      } else {
        this.$router.push('/login')
      }
    },
    handleLogin() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (this.form.remember) {
          localStorage.setItem('student_remember_username', this.form.username)
        } else {
          localStorage.removeItem('student_remember_username')
        }
        this.loading = true
        this.$store.dispatch('student/login', this.form).then(() => {
          this.$router.push('/student')
          this.loading = false
        }).catch(() => {
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
  max-width: 400px;
}

.login-card {
  padding: 44px 48px 36px;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;

  .header-icon {
    width: 56px;
    height: 56px;
    margin: 0 auto 16px;
    border-radius: 16px;
    background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    i {
      font-size: 28px;
      color: #1677ff;
    }
  }

  .title {
    font-size: 22px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0 0 8px;
    letter-spacing: 0.5px;
  }

  .subtitle {
    font-size: 15px;
    color: #374151;
    margin: 0 0 6px;
  }

  .hint {
    font-size: 12px;
    color: #9ca3af;
    margin: 0;
  }
}

.form {
  ::v-deep .el-form-item {
    margin-bottom: 26px;
  }

  ::v-deep .el-form-item__label {
    font-size: 13px;
    color: #4b5563;
    padding-bottom: 8px;
    font-weight: 500;
  }

  ::v-deep .el-input__inner {
    height: 46px;
    line-height: 46px;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
    font-size: 14px;

    &:focus {
      border-color: #4096ff;
      box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.15);
    }
  }

  .options-row {
    margin-bottom: 20px;

    ::v-deep .el-form-item__content {
      line-height: 1;
    }

    ::v-deep .el-checkbox__label {
      font-size: 13px;
      color: #6b7280;
    }
  }

  .btn-wrap {
    margin-bottom: 0;
    margin-top: 4px;
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
  margin: 28px 0 22px;
  gap: 14px;

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
  gap: 24px;
  flex-wrap: wrap;

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
    cursor: pointer;

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
