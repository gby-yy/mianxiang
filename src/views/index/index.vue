<template>
  <div class="home-dashboard" :class="roleClass">
    <!-- 管理员首页 -->
    <template v-if="userRole === 'admin'">
      <div class="dashboard-hero admin-hero">
        <div class="hero-bg"></div>
        <div class="hero-content">
          <h1 class="hero-title">平台管理控制台</h1>
          <p class="hero-subtitle">{{ greeting }}，{{ userName || '管理员' }}</p>
          <p class="hero-desc">管理用户、课程、考试与全局数据</p>
        </div>
      </div>
      <div class="dashboard-cards" v-loading="statsLoading">
        <div class="stat-card" v-for="(item, i) in adminStatsList" :key="i">
          <div class="stat-icon" :style="{ background: item.iconBg }">
            <i :class="item.icon"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ item.value }}</span>
            <span class="stat-label">{{ item.label }}</span>
          </div>
        </div>
      </div>
      <div class="dashboard-section">
        <h3 class="section-title">快捷入口</h3>
        <div class="quick-links">
          <router-link v-for="link in adminLinks" :key="link.path" :to="link.path" class="quick-link-card">
            <i :class="link.icon"></i>
            <span>{{ link.title }}</span>
          </router-link>
        </div>
      </div>
    </template>

    <!-- 教师首页 -->
    <template v-else>
      <div class="dashboard-hero teacher-hero">
        <div class="hero-bg"></div>
        <div class="hero-content">
          <h1 class="hero-title">教师工作台</h1>
          <p class="hero-subtitle">{{ greeting }}，{{ userName || '老师' }}</p>
          <p class="hero-desc">管理课程、题库、试卷与学情分析</p>
        </div>
      </div>
      <div class="dashboard-cards" v-loading="statsLoading">
        <div class="stat-card" v-for="(item, i) in teacherStatsList" :key="i">
          <div class="stat-icon" :style="{ background: item.iconBg }">
            <i :class="item.icon"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ item.value }}</span>
            <span class="stat-label">{{ item.label }}</span>
          </div>
        </div>
      </div>
      <div class="dashboard-section">
        <h3 class="section-title">常用功能</h3>
        <div class="quick-links">
          <router-link v-for="link in teacherLinks" :key="link.path" :to="link.path" class="quick-link-card">
            <i :class="link.icon"></i>
            <span>{{ link.title }}</span>
          </router-link>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getDashboardStats } from '@/api/dashboard'

export default {
  name: 'HomeDashboard',
  data() {
    return {
      statsLoading: false,
      adminStatsList: [
        { label: '用户总数', statKey: 'userCount', value: '—', icon: 'el-icon-user-solid', iconBg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
        { label: '课程总数', statKey: 'courseCount', value: '—', icon: 'el-icon-reading', iconBg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
        { label: '考试记录', statKey: 'examRecordCount', value: '—', icon: 'el-icon-document-copy', iconBg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
        { label: '试卷总数', statKey: 'paperCount', value: '—', icon: 'el-icon-s-data', iconBg: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }
      ],
      teacherStatsList: [
        { label: '我的课程', statKey: 'courseCount', value: '—', icon: 'el-icon-reading', iconBg: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' },
        { label: '题库题目', statKey: 'questionCount', value: '—', icon: 'el-icon-edit-outline', iconBg: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)' },
        { label: '我的试卷', statKey: 'paperCount', value: '—', icon: 'el-icon-tickets', iconBg: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)' },
        { label: '考试记录', statKey: 'examRecordCount', value: '—', icon: 'el-icon-data-analysis', iconBg: 'linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%)' }
      ]
    }
  },
  computed: {
    ...mapGetters(['role', 'name']),
    userRole() {
      return this.role || ''
    },
    userName() {
      return this.name || ''
    },
    roleClass() {
      return this.userRole === 'admin' ? 'role-admin' : 'role-teacher'
    },
    greeting() {
      const h = new Date().getHours()
      if (h < 6) return '夜深了'
      if (h < 9) return '早上好'
      if (h < 12) return '上午好'
      if (h < 14) return '中午好'
      if (h < 18) return '下午好'
      if (h < 22) return '晚上好'
      return '夜深了'
    },
    adminLinks() {
      return [
        { title: '用户管理', path: '/user-manage/student', icon: 'el-icon-user' },
        { title: '课程列表', path: '/course-manage/course-list', icon: 'el-icon-document' },
        { title: '试卷列表', path: '/exam-manage/paper-list', icon: 'el-icon-tickets' },
        { title: '数据总览', path: '/data-analysis/overview', icon: 'el-icon-s-data' }
      ]
    },
    teacherLinks() {
      return [
        { title: '课程列表', path: '/course-manage/course-list', icon: 'el-icon-document' },
        { title: '课程详情', path: '/course-manage/course-chapter', icon: 'el-icon-menu' },
        { title: '题库管理', path: '/question-bank/index', icon: 'el-icon-edit' },
        { title: '试卷列表', path: '/exam-manage/paper-list', icon: 'el-icon-tickets' },
        { title: '考试记录', path: '/exam-manage/exam-record', icon: 'el-icon-edit-outline' },
        { title: '课程数据分析', path: '/data-analysis/course-analysis', icon: 'el-icon-pie-chart' }
      ]
    }
  },
  mounted() {
    this.fetchStats()
  },
  methods: {
    fetchStats() {
      this.statsLoading = true
      getDashboardStats().then(res => {
        const data = res.data || {}
        if (this.userRole === 'admin') {
          this.adminStatsList.forEach(item => {
            const n = data[item.statKey]
            this.$set(item, 'value', n !== undefined && n !== null ? Number(n) : '—')
          })
        } else {
          this.teacherStatsList.forEach(item => {
            const n = data[item.statKey]
            this.$set(item, 'value', n !== undefined && n !== null ? Number(n) : '—')
          })
        }
        this.statsLoading = false
      }).catch(() => {
        this.statsLoading = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.home-dashboard {
  min-height: calc(100vh - 84px);
  padding: 24px 28px;
  background: #f0f2f5;
}

.dashboard-hero {
  position: relative;
  padding: 48px 32px;
  border-radius: 12px;
  margin-bottom: 24px;
  overflow: hidden;
  .hero-bg {
    position: absolute;
    inset: 0;
    opacity: 0.92;
  }
  .hero-content {
    position: relative;
    z-index: 1;
  }
  .hero-title {
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    margin: 0 0 12px 0;
    letter-spacing: 0.5px;
    text-shadow: 0 2px 8px rgba(0,0,0,0.15);
  }
  .hero-subtitle {
    font-size: 16px;
    color: rgba(255,255,255,0.95);
    margin: 0 0 8px 0;
  }
  .hero-desc {
    font-size: 14px;
    color: rgba(255,255,255,0.85);
    margin: 0;
  }
}

.admin-hero .hero-bg {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 50%, #7e22ce 100%);
}

.teacher-hero .hero-bg {
  background: linear-gradient(135deg, #0f766e 0%, #0d9488 40%, #14b8a6 100%);
}

.dashboard-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: transform 0.2s, box-shadow 0.2s;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  }
  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    i {
      font-size: 26px;
      color: #fff;
    }
  }
  .stat-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
  .stat-value {
    font-size: 24px;
    font-weight: 700;
    color: #303133;
  }
  .stat-label {
    font-size: 13px;
    color: #909399;
  }
}

.dashboard-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 20px 0;
    padding-bottom: 12px;
    border-bottom: 1px solid #ebeef5;
  }
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.quick-link-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px 16px;
  border-radius: 10px;
  background: #f5f7fa;
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
  i {
    font-size: 28px;
    color: #409EFF;
  }
  &:hover {
    background: #ecf5ff;
    color: #409EFF;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  }
}

.role-teacher .quick-link-card i {
  color: #0d9488;
}
.role-teacher .quick-link-card:hover {
  background: #ccfbf1;
  color: #0d9488;
  box-shadow: 0 4px 12px rgba(13, 148, 136, 0.2);
}

@media (max-width: 1200px) {
  .dashboard-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 768px) {
  .dashboard-cards {
    grid-template-columns: 1fr;
  }
  .dashboard-hero {
    padding: 32px 20px;
  }
  .hero-title {
    font-size: 22px;
  }
}
</style>
