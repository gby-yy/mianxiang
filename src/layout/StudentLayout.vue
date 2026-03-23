<template>
  <div class="student-layout">
    <header class="student-header">
      <div class="header-inner">
        <div class="header-left">
          <span
            :class="['logo-text', { clickable: !isExamPage }]"
            @click="goHomeIfNotExam"
          >智能教学系统</span>
        </div>
        <div v-if="studentName" class="header-right">
          <div class="user-block">
            <div class="user-avatar">
              <img v-if="studentAvatar" :src="studentAvatar" alt="头像" class="avatar-img">
              <span v-else class="avatar-fallback">{{ avatarFallback }}</span>
            </div>
            <span class="user-name">{{ studentName }}</span>
            <el-button type="text" class="logout-btn" @click="handleLogout">退出</el-button>
          </div>
        </div>
      </div>
    </header>
    <main class="student-main">
      <div class="content-inner">
        <router-view />
      </div>
    </main>
    <footer class="student-footer">
      <div class="footer-inner">
        <div class="footer-links">
          <router-link to="/student/login">学生登录</router-link>
          <span class="dot">·</span>
          <router-link to="/login">管理后台</router-link>
        </div>
        <p class="footer-copy">面向计算机专业的个性化交互式智能教学系统 · 仅供学习使用</p>
        <p class="footer-icp">© 2026 智能教学系统</p>
      </div>
    </footer>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'StudentLayout',
  computed: {
    ...mapGetters(['studentName', 'studentToken', 'studentAvatar']),
    isExamPage() {
      return this.$route.path.indexOf('/student/exam') === 0
    },
    avatarFallback() {
      if (!this.studentName || !this.studentName.trim()) return '?'
      return this.studentName.trim().charAt(0).toUpperCase()
    }
  },
  mounted() {
    if (this.studentToken && !this.studentName) {
      this.$store.dispatch('student/getInfo').catch(() => {
        this.$store.dispatch('student/resetToken')
        this.$router.replace('/student/login')
      })
    }
  },
  methods: {
    goHomeIfNotExam() {
      if (!this.isExamPage) {
        this.$router.push('/student/index')
      }
    },
    async handleLogout() {
      await this.$store.dispatch('student/logout')
      this.$router.push('/student/login')
    }
  }
}
</script>

<style lang="scss" scoped>
.student-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #e6f4ff 0%, #bae0ff 30%, #91caff 60%, #69b1ff 100%);
  background-attachment: fixed;
}

.student-header {
  width: 100%;
  flex-shrink: 0;
  height: 56px;
  background: transparent;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.header-inner {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.student-header .header-left .logo-text {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.02em;
  background: linear-gradient(135deg, #0c4a6e 0%, #0369a1 35%, #0284c7 60%, #0ea5e9 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  transition: opacity 0.2s ease;

  &.clickable {
    cursor: pointer;
    &:hover {
      opacity: 0.85;
    }
  }
}

.student-header .header-right {
  display: flex;
  align-items: center;
}

.user-block {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 6px 14px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0ea5e9, #0284c7);
  .avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  .avatar-fallback {
    font-size: 16px;
    font-weight: 600;
    color: #fff;
  }
}

.user-name {
  font-size: 14px;
  color: #374151;
  line-height: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
}

.logout-btn {
  color: #6b7280;
  padding: 0 8px;
  height: 32px;
  line-height: 32px;
  &:hover { color: #409eff; }
}

.student-main {
  flex: 1;
  width: 100%;
  padding: 24px;
  min-height: 0;
}

.content-inner {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.student-footer {
  width: 100%;
  flex-shrink: 0;
  background: transparent;
  border-top: 1px solid rgba(255, 255, 255, 0.4);
  padding: 28px 24px 24px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  text-align: left;
}

.student-footer .footer-links {
  margin-bottom: 12px;
  font-size: 13px;
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 0 4px;

  a {
    color: #1e3a5f;
    text-decoration: none;
    &:hover { color: #1677ff; }
  }

  .dot {
    margin: 0 6px;
    color: #3d5a80;
  }
}

.student-footer .footer-copy {
  font-size: 12px;
  color: #2d4a6f;
  margin: 0 0 6px;
}

.student-footer .footer-icp {
  font-size: 12px;
  color: #2d4a6f;
  margin: 0;
}
</style>
