<template>
  <div class="login-container">
    <div class="login-left">
      <div class="project-info">
        <h1 class="project-title">面向计算机专业的个性化交互式智能教学系统</h1>
        <div class="feature-list">
          <div class="feature-item">
            <i class="el-icon-check"></i>
            <div class="feature-text">
              <h3>教学资源管理</h3>
              <p>实时监控教学资源状态，系统化智能管理</p>
            </div>
          </div>
          <div class="feature-item">
            <i class="el-icon-check"></i>
            <div class="feature-text">
              <h3>学习任务处理</h3>
              <p>高效处理学习任务，提升教学体验</p>
            </div>
          </div>
          <div class="feature-item">
            <i class="el-icon-check"></i>
            <div class="feature-text">
              <h3>学习数据分析</h3>
              <p>全面的学习数据分析，助力教学决策</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="login-right">
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" @keyup.enter.native="handleLogin">
        <h2 class="login-title">用户登录</h2>
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            prefix-icon="el-icon-user"
            placeholder="请输入用户名">
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            prefix-icon="el-icon-lock"
            :type="passwordType"
            placeholder="请输入密码"
            show-password>
          </el-input>
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-container">
            <el-input
              v-model="loginForm.captcha"
              prefix-icon="el-icon-picture"
              placeholder="请输入验证码"
              class="captcha-input">
            </el-input>
            <div class="captcha-image" @click="refreshCaptcha">
              <img v-if="captchaImageUrl" :src="captchaImageUrl" alt="验证码" />
              <span v-else class="captcha-placeholder">点击获取验证码</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <div class="remember-password">
            <el-checkbox v-model="rememberPassword">记住密码</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-button">登录</el-button>
        </el-form-item>
        <div class="login-footer">
          <span class="footer-line" />
          <span class="footer-text">或</span>
          <span class="footer-line" />
        </div>
        <div class="student-link-wrap">
          <router-link to="/student/login" class="student-link">
            <i class="el-icon-s-home" />
            去学生端首页
          </router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { getCaptcha } from '@/api/user'

export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入用户名'))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入密码'))
      } else if (value.length < 6) {
        callback(new Error('密码长度不能小于6位'))
      } else {
        callback()
      }
    }
    const validateCaptcha = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入验证码'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
        username: 'admin',
        password: '111111',
        captcha: '',
        captchaKey: ''
      },
      loginRules: {
        username: [{ validator: validateUsername, trigger: 'blur' }],
        password: [{ validator: validatePassword, trigger: 'blur' }],
        captcha: [{ validator: validateCaptcha, trigger: 'blur' }]
      },
      passwordType: 'password',
      loading: false,
      captchaImageUrl: '',
      rememberPassword: false
    }
  },
  mounted() {
    // 页面加载时获取验证码
    this.refreshCaptcha()
    // 加载记住的密码
    this.loadRememberedPassword()
  },
  methods: {
    // 加载记住的密码
    loadRememberedPassword() {
      const remembered = localStorage.getItem('rememberPassword')
      if (remembered === 'true') {
        this.rememberPassword = true
        const savedUsername = localStorage.getItem('savedUsername')
        const savedPassword = localStorage.getItem('savedPassword')
        if (savedUsername) {
          this.loginForm.username = savedUsername
        }
        if (savedPassword) {
          this.loginForm.password = savedPassword
        }
      }
    },
    // 保存记住的密码
    saveRememberedPassword() {
      if (this.rememberPassword) {
        localStorage.setItem('rememberPassword', 'true')
        localStorage.setItem('savedUsername', this.loginForm.username)
        localStorage.setItem('savedPassword', this.loginForm.password)
      } else {
        localStorage.removeItem('rememberPassword')
        localStorage.removeItem('savedUsername')
        localStorage.removeItem('savedPassword')
      }
    },
    // 刷新验证码
    refreshCaptcha() {
      // 生成 cacheKey，使用时间戳
      const cacheKey = Date.now().toString()
      this.loginForm.captchaKey = cacheKey

      // 如果之前有验证码图片 URL，先释放它
      if (this.captchaImageUrl) {
        window.URL.revokeObjectURL(this.captchaImageUrl)
      }

      getCaptcha(cacheKey).then(blob => {
        // response 已经是 blob 对象，直接转换为图片 URL
        const url = window.URL.createObjectURL(blob)
        this.captchaImageUrl = url
      }).catch(error => {
        console.error('获取验证码失败:', error)
        this.$message.error('获取验证码失败，请重试')
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          // 保存记住密码
          this.saveRememberedPassword()

          this.$store.dispatch('user/login', this.loginForm).then(() => {
            this.$router.push('/')
            this.loading = false
          }).catch(() => {
            this.loading = false
            // 登录失败后刷新验证码
            this.refreshCaptcha()
            this.loginForm.captcha = ''
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  width: 100%;

  .login-left {
    flex: 0 0 60%;
    max-width: 60%;
    background: linear-gradient(135deg, #1890ff, #1d39c4);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;

    .project-info {
      color: white;
      max-width: 600px;

      .project-title {
        font-size: 36px;
        margin-bottom: 60px;
        text-align: center;
      }

      .feature-list {
        .feature-item {
          display: flex;
          align-items: center;
          margin-bottom: 40px;

          i {
            font-size: 36px;
            margin-right: 20px;
          }

          .feature-text {
            h3 {
              font-size: 20px;
              margin: 0 0 10px;
            }

            p {
              margin: 0;
              opacity: 0.8;
              font-size: 16px;
            }
          }
        }
      }
    }
  }

  .login-right {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    background: white;

    .login-form {
      width: 450px;
      padding: 50px 40px;
      background: white;
      border-radius: 16px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.08);
      border: 1px solid rgba(0, 0, 0, 0.06);

      .login-title {
        text-align: center;
        margin-bottom: 45px;
        font-size: 28px;
        font-weight: 600;
        color: #333;
        letter-spacing: 1px;
      }

      ::v-deep .el-form-item {
        margin-bottom: 24px;

        .el-form-item__content {
          line-height: normal;
        }
      }

      ::v-deep .el-input {
        .el-input__inner {
          height: 44px;
          line-height: 44px;
          border-radius: 6px;
          border: 1px solid #e0e0e0;
          font-size: 14px;
          transition: all 0.3s;

          &:focus {
            border-color: #409eff;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
          }
        }

        .el-input__prefix {
          left: 12px;

          .el-input__icon {
            line-height: 44px;
            color: #909399;
          }
        }

        .el-input__inner {
          padding-left: 40px;
        }
      }

      .remember-password {
        width: 100%;

        ::v-deep .el-checkbox {
          .el-checkbox__label {
            font-size: 14px;
            color: #606266;
          }
        }
      }

      .captcha-container {
        display: flex;
        align-items: center;
        gap: 12px;

        .captcha-input {
          flex: 1;
        }

        .captcha-image {
          width: 120px;
          height: 44px;
          flex-shrink: 0;
          border: 1px solid #e0e0e0;
          border-radius: 6px;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #f8f9fa;
          overflow: hidden;
          transition: all 0.3s;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          .captcha-placeholder {
            font-size: 12px;
            color: #909399;
            text-align: center;
            padding: 0 5px;
          }

          &:hover {
            border-color: #409eff;
            background: #f0f7ff;
          }

          &:active {
            transform: scale(0.98);
          }
        }
      }

      .login-button {
        width: 100%;
        height: 44px;
        font-size: 16px;
        font-weight: 500;
        border-radius: 6px;
        margin-top: 10px;
        background: linear-gradient(135deg, #409eff, #1890ff);
        border: none;
        transition: all 0.3s;

        &:hover {
          background: linear-gradient(135deg, #66b1ff, #409eff);
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        }

        &:active {
          transform: translateY(0);
        }
      }

      .login-footer {
        display: flex;
        align-items: center;
        margin: 24px 0 16px;
        gap: 12px;

        .footer-line {
          flex: 1;
          height: 1px;
          background: linear-gradient(90deg, transparent, #e0e0e0, transparent);
        }

        .footer-text {
          font-size: 12px;
          color: #909399;
        }
      }

      .student-link-wrap {
        text-align: center;
      }

      .student-link {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        color: #409eff;
        text-decoration: none;
        padding: 8px 16px;
        border-radius: 8px;
        transition: all 0.2s;

        i {
          font-size: 16px;
        }

        &:hover {
          color: #66b1ff;
          background: rgba(64, 158, 255, 0.08);
        }
      }
    }
  }
}
</style>
