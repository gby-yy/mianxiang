<template>
  <div class="student-home">
    <section class="intro-section">
      <h1 class="intro-title">智能教学系统</h1>
      <p class="intro-desc">
        面向计算机专业的个性化交互式智能教学系统，以个性化选择与智能交互为核心，支持课程学习、练习与考试。
      </p>
    </section>

    <!-- 学生数据统计金刚区 -->
    <section v-if="!dashboardLoading" class="stats-diamond">
      <div class="stats-diamond-inner">
        <div class="stat-item" @click="$router.push('/student/courses')">
          <div class="stat-icon-wrap stat-icon--course">
            <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2"/><rect x="8" y="6" width="8" height="4"/></svg>
          </div>
          <div class="stat-value">{{ stats.studyCourseCount || 0 }}</div>
          <div class="stat-label">学习课程数</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon-wrap stat-icon--chapter">
            <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
          </div>
          <div class="stat-value">{{ stats.finishedChapterCount || 0 }}</div>
          <div class="stat-label">完成章节数</div>
        </div>
        <div class="stat-item" @click="$router.push('/student/exam/records')">
          <div class="stat-icon-wrap stat-icon--exam">
            <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
          </div>
          <div class="stat-value">{{ stats.examCount || 0 }}</div>
          <div class="stat-label">考试次数</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon-wrap stat-icon--score">
            <svg class="stat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          </div>
          <div class="stat-value">{{ formatScore(stats.maxScore) }}</div>
          <div class="stat-label">最高分</div>
        </div>
      </div>
    </section>

    <!-- 看板：学习进度与待办同一行，等高、超出滚动 -->
    <section v-if="dashboardLoading" class="dashboard-wrap dashboard-loading">
      <span>加载中…</span>
    </section>
    <section
      v-else-if="dashboardList.length || todoUnlearnedFlat.length || todoUntestedFlat.length"
      class="dashboard-wrap"
    >
      <div class="dashboard-row">
        <!-- 学习进度 -->
        <div class="dashboard-col dashboard-col--progress">
          <h2 class="dashboard-heading">
            <i class="el-icon-data-line" /> 学习进度
          </h2>
          <div class="dashboard-body dashboard-body--scroll">
            <div v-if="dashboardList.length" class="progress-cards">
              <div
                v-for="row in dashboardList"
                :key="row.courseId"
                class="progress-card"
                @click="goLearn(row.courseId)"
              >
                <div class="progress-card-cover">
                  <img v-if="row.cover" :src="resolveCover(row.cover)" :alt="row.courseName" class="cover-img" />
                  <div v-else class="cover-placeholder">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2"/><rect x="8" y="6" width="8" height="4"/></svg>
                  </div>
                </div>
                <div class="progress-card-main">
                  <span class="progress-course-name">{{ row.courseName }}</span>
                  <span class="progress-counts">
                    已解锁 <strong>{{ row.unlockedChapterCount }}</strong> / 共 <strong>{{ row.totalChapterCount }}</strong> 章
                  </span>
                  <el-progress
                    :percentage="Number(row.progressRate) || 0"
                    :stroke-width="8"
                    class="progress-bar"
                  />
                </div>
                <i class="el-icon-arrow-right progress-card-arrow" />
              </div>
            </div>
            <p v-else class="dashboard-empty-tip">暂无学习记录，去 <router-link to="/student/courses">课程列表</router-link> 选课开始学习。</p>
          </div>
        </div>
        <!-- 待办 -->
        <div class="dashboard-col dashboard-col--todos">
          <h2 class="dashboard-heading">
            <i class="el-icon-s-operation" /> 待办
          </h2>
          <div class="dashboard-body dashboard-body--scroll">
            <div v-if="todoUnlearnedFlat.length" class="todo-block">
              <h3 class="todo-label">已解锁未学习</h3>
              <div
                v-for="row in dashboardList"
                v-show="row.todoUnlearned && row.todoUnlearned.length"
                :key="'uc-' + row.courseId"
                class="todo-course-group"
              >
                <div class="todo-course-info" @click="goLearn(row.courseId)">
                  <i class="el-icon-notebook-2 todo-course-icon" />
                  <span class="todo-course-name">{{ row.courseName }}</span>
                  <span class="todo-course-meta">共 {{ (row.todoUnlearned || []).length }} 章待学</span>
                </div>
                <ul class="todo-list">
                  <li
                    v-for="(item, idx) in (row.todoUnlearned || [])"
                    :key="'u-' + row.courseId + '-' + idx"
                    class="todo-item"
                  >
                    <router-link :to="`/student/courses/learn/${item.courseId}/chapter/${item.chapterId}`" class="todo-link">
                      <span class="todo-icon todo-icon--learn" aria-hidden="true">
                        <svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
                      </span>
                      <span class="todo-text">{{ item.chapterName }}</span>
                    </router-link>
                  </li>
                </ul>
              </div>
            </div>
            <div v-if="todoUntestedFlat.length" class="todo-block">
              <h3 class="todo-label">已学完待测试</h3>
              <ul class="todo-list">
                <li v-for="(item, idx) in todoUntestedFlat" :key="'t-' + idx" class="todo-item">
                  <router-link :to="`/student/courses/learn/${item.courseId}`" class="todo-link">
                    <span class="todo-icon todo-icon--test" aria-hidden="true">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                    </span>
                    <span class="todo-text">{{ item.courseName }} - {{ item.chapterName }}</span>
                  </router-link>
                </li>
              </ul>
            </div>
            <p v-if="!todoUnlearnedFlat.length && !todoUntestedFlat.length" class="dashboard-empty-tip">当前没有待办，继续保持～</p>
          </div>
        </div>
      </div>
    </section>

    <section class="nav-section">
      <router-link
        v-for="item in navList"
        :key="item.path"
        :to="item.path"
        :class="['nav-card', 'nav-card--' + item.theme]"
      >
        <div class="nav-card-icon">
          <i :class="item.icon" />
        </div>
        <div class="nav-card-body">
          <h3 class="nav-card-title">{{ item.title }}</h3>
          <p class="nav-card-subtitle">{{ item.subtitle }}</p>
        </div>
        <i class="nav-card-arrow el-icon-arrow-right" />
      </router-link>
    </section>
  </div>
</template>

<script>
import { getStudentDashboard } from '@/api/student'

export default {
  name: 'StudentHome',
  data() {
    return {
      dashboardLoading: false,
      dashboardList: [],
      stats: {},
      navList: [
        {
          path: '/student/courses',
          theme: 'blue',
          icon: 'el-icon-notebook-2',
          title: '课程列表',
          subtitle: '浏览课程，选择课程进行学习'
        },
        {
          path: '/student/exam/records',
          theme: 'cyan',
          icon: 'el-icon-tickets',
          title: '考试记录',
          subtitle: '查看历史考试与作答详情'
        },
        {
          path: '/student/profile',
          theme: 'teal',
          icon: 'el-icon-user',
          title: '个人中心',
          subtitle: '个人信息与学习统计'
        }
      ]
    }
  },
  computed: {
    todoUnlearnedFlat() {
      const list = []
      this.dashboardList.forEach(row => {
        (row.todoUnlearned || []).forEach(item => list.push(item))
      })
      return list
    },
    todoUntestedFlat() {
      const list = []
      this.dashboardList.forEach(row => {
        (row.todoUntested || []).forEach(item => list.push(item))
      })
      return list
    }
  },
  mounted() {
    this.loadDashboard()
  },
  methods: {
    loadDashboard() {
      this.dashboardLoading = true
      getStudentDashboard()
        .then(res => {
          const data = res && res.data
          if (!data) {
            this.dashboardList = []
            this.stats = {}
            return
          }
          if (Array.isArray(data.list)) {
            this.dashboardList = data.list
            this.stats = data.stats && typeof data.stats === 'object' ? data.stats : {}
          } else if (Array.isArray(data)) {
            this.dashboardList = data
            const finished = data.reduce((s, r) => s + (Number(r.finishedChapterCount) || 0), 0)
            this.stats = { studyCourseCount: data.length, finishedChapterCount: finished, examCount: 0, maxScore: 0 }
          } else {
            this.dashboardList = []
            this.stats = {}
          }
        })
        .catch(() => {
          this.dashboardList = []
          this.stats = {}
        })
        .finally(() => {
          this.dashboardLoading = false
        })
    },
    goLearn(courseId) {
      this.$router.push({ path: `/student/courses/learn/${courseId}` })
    },
    formatScore(v) {
      if (v == null || v === '') return '0'
      const n = Number(v)
      if (Number.isNaN(n)) return '0'
      return n % 1 === 0 ? String(Math.round(n)) : n.toFixed(1)
    },
    resolveCover(cover) {
      if (!cover) return ''
      if (typeof cover === 'string' && (cover.startsWith('http://') || cover.startsWith('https://'))) return cover
      const base = process.env.VUE_APP_BASE_API || ''
      return base ? (base.replace(/\/$/, '') + '/' + (cover.replace(/^\//, ''))) : cover
    }
  }
}
</script>

<style lang="scss" scoped>
.student-home {
  padding-bottom: 32px;
}

.intro-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 32px 40px;
  margin-bottom: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);

  .intro-title {
    font-size: 24px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0 0 12px;
  }

  .intro-desc {
    font-size: 15px;
    line-height: 1.7;
    color: #4b5563;
    margin: 0;
  }
}

/* 学生数据统计金刚区 */
.stats-diamond {
  margin-bottom: 24px;
}

.stats-diamond-inner {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-item {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%);
  border-radius: 16px;
  padding: 20px 16px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.6);
  cursor: default;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  &:first-child,
  &:nth-child(3) {
    cursor: pointer;
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 28px rgba(0, 0, 0, 0.1);
    }
  }
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  margin: 0 auto 12px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon {
  width: 24px;
  height: 24px;
  color: #fff;
}

.stat-icon--course {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}
.stat-icon--chapter {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}
.stat-icon--exam {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}
.stat-icon--score {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

/* 看板：玻璃卡片、同一行、等高、超出滚动 */
.dashboard-wrap {
  margin-bottom: 28px;
  padding: 0;
  border-radius: 20px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.92) 0%, rgba(255, 255, 255, 0.88) 100%);
  backdrop-filter: blur(12px);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.6);
  overflow: hidden;
}

.dashboard-loading {
  color: #6b7280;
  text-align: center;
  padding: 40px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.92) 0%, rgba(255, 255, 255, 0.88) 100%);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.dashboard-row {
  display: flex;
  align-items: stretch;
  gap: 0;
  min-height: 320px;
}

.dashboard-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 24px 28px;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  &:last-child {
    border-right: none;
  }
}

.dashboard-col--progress {
  border-right: 1px solid rgba(0, 0, 0, 0.06);
}

.dashboard-heading {
  font-size: 17px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  .el-icon-data-line,
  .el-icon-list {
    font-size: 20px;
    color: #1677ff;
    opacity: 0.9;
  }
}

.dashboard-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.dashboard-body--scroll {
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 6px;
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.04);
    border-radius: 3px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.12);
    border-radius: 3px;
  }
  &::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.2);
  }
}

.progress-cards {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.progress-card {
  display: flex;
  align-items: center;
  padding: 0;
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.22s ease;
  overflow: hidden;
  &:hover {
    border-color: rgba(22, 119, 255, 0.35);
    background: rgba(22, 119, 255, 0.06);
    box-shadow: 0 4px 16px rgba(22, 119, 255, 0.12);
    .progress-card-arrow { color: #1677ff; }
  }
}

.progress-card-cover {
  width: 72px;
  min-width: 72px;
  height: 56px;
  margin-left: 12px;
  margin-top: 8px;
  margin-bottom: 8px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.progress-card-cover .cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.progress-card-cover .cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6366f1;
  opacity: 0.7;
  svg {
    width: 28px;
    height: 28px;
  }
}

.progress-card-main {
  flex: 1;
  min-width: 0;
  padding: 14px 16px 14px 18px;
}

.progress-course-name {
  font-weight: 600;
  color: #111827;
  margin-right: 8px;
  letter-spacing: 0.01em;
}

.progress-counts {
  font-size: 13px;
  color: #6b7280;
  strong {
    color: #1677ff;
    font-weight: 600;
  }
}

.progress-bar {
  margin-top: 12px;
  max-width: 100%;
  ::v-deep .el-progress-bar__outer { border-radius: 4px; }
  ::v-deep .el-progress-bar__inner { border-radius: 4px; }
}

.progress-card-arrow {
  flex-shrink: 0;
  margin-right: 4px;
  color: #9ca3af;
  font-size: 18px;
  transition: color 0.2s ease;
}

.dashboard-empty-tip {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.6;
  a {
    color: #1677ff;
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }
}

.todo-block {
  margin-bottom: 20px;
  &:last-child { margin-bottom: 0; }
}

.todo-course-group {
  margin-bottom: 18px;
  &:last-child { margin-bottom: 0; }
}

.todo-course-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  margin-bottom: 8px;
  border-radius: 10px;
  background: rgba(14, 165, 233, 0.08);
  border: 1px solid rgba(14, 165, 233, 0.18);
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease;
  &:hover {
    background: rgba(14, 165, 233, 0.12);
    border-color: rgba(14, 165, 233, 0.28);
  }
}

.todo-course-icon {
  font-size: 18px;
  color: #0ea5e9;
  flex-shrink: 0;
}

.todo-course-name {
  font-weight: 600;
  font-size: 14px;
  color: #0c4a6e;
  flex: 1;
  min-width: 0;
}

.todo-course-meta {
  font-size: 12px;
  color: #0369a1;
  flex-shrink: 0;
}

.todo-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 10px;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.todo-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.todo-item {
  padding: 0;
  margin-bottom: 2px;
  border-radius: 10px;
  transition: background 0.2s ease;
  &:hover {
    background: rgba(22, 119, 255, 0.06);
  }
}

.todo-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  color: #374151;
  text-decoration: none;
  font-size: 14px;
  line-height: 1.45;
  border-radius: 10px;
  transition: color 0.2s ease, background 0.2s ease;
  &:hover {
    color: #1677ff;
    .todo-icon { opacity: 1; }
  }
}

.todo-icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  opacity: 0.9;
  transition: opacity 0.2s ease;
  svg {
    width: 18px;
    height: 18px;
    display: block;
  }
}
.todo-icon--learn {
  color: #0ea5e9;
}
.todo-icon--test {
  color: #8b5cf6;
}

.todo-text {
  flex: 1;
  min-width: 0;
  word-break: break-word;
}

.nav-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.nav-card {
  display: flex;
  align-items: center;
  border-radius: 14px;
  padding: 24px 28px;
  text-decoration: none;
  color: inherit;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid transparent;
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

    .nav-card-arrow {
      transform: translateX(4px);
    }
  }

  .nav-card-icon {
    width: 56px;
    height: 56px;
    flex-shrink: 0;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20px;
    transition: all 0.25s ease;

    i {
      font-size: 28px;
    }
  }
}

.nav-card--blue {
  background: linear-gradient(135deg, #e6f4ff 0%, #bae0ff 100%);
  border-color: rgba(64, 150, 255, 0.25);
  .nav-card-icon { background: rgba(255, 255, 255, 0.7); color: #1677ff; }
  .nav-card-title { color: #0c4a6e; }
  .nav-card-subtitle { color: #0369a1; }
  .nav-card-arrow { color: #0369a1; }
  &:hover .nav-card-icon { background: rgba(255, 255, 255, 0.95); color: #0369a1; }
  &:hover .nav-card-arrow { color: #0369a1; }
}

.nav-card--green {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  border-color: rgba(34, 197, 94, 0.25);
  .nav-card-icon { background: rgba(255, 255, 255, 0.7); color: #15803d; }
  .nav-card-title { color: #14532d; }
  .nav-card-subtitle { color: #166534; }
  .nav-card-arrow { color: #166534; }
  &:hover .nav-card-icon { background: rgba(255, 255, 255, 0.95); color: #166534; }
  &:hover .nav-card-arrow { color: #166534; }
}

.nav-card--orange {
  background: linear-gradient(135deg, #ffedd5 0%, #fed7aa 100%);
  border-color: rgba(234, 88, 12, 0.25);
  .nav-card-icon { background: rgba(255, 255, 255, 0.7); color: #c2410c; }
  .nav-card-title { color: #7c2d12; }
  .nav-card-subtitle { color: #9a3412; }
  .nav-card-arrow { color: #9a3412; }
  &:hover .nav-card-icon { background: rgba(255, 255, 255, 0.95); color: #9a3412; }
  &:hover .nav-card-arrow { color: #9a3412; }
}

.nav-card--violet {
  background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%);
  border-color: rgba(139, 92, 246, 0.25);
  .nav-card-icon { background: rgba(255, 255, 255, 0.7); color: #6d28d9; }
  .nav-card-title { color: #4c1d95; }
  .nav-card-subtitle { color: #5b21b6; }
  .nav-card-arrow { color: #5b21b6; }
  &:hover .nav-card-icon { background: rgba(255, 255, 255, 0.95); color: #5b21b6; }
  &:hover .nav-card-arrow { color: #5b21b6; }
}

.nav-card--teal {
  background: linear-gradient(135deg, #ccfbf1 0%, #99f6e4 100%);
  border-color: rgba(20, 184, 166, 0.25);
  .nav-card-icon { background: rgba(255, 255, 255, 0.7); color: #0d9488; }
  .nav-card-title { color: #134e4a; }
  .nav-card-subtitle { color: #0f766e; }
  .nav-card-arrow { color: #0f766e; }
  &:hover .nav-card-icon { background: rgba(255, 255, 255, 0.95); color: #0f766e; }
  &:hover .nav-card-arrow { color: #0f766e; }
}

.nav-card--cyan {
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  border-color: rgba(14, 165, 233, 0.25);
  .nav-card-icon { background: rgba(255, 255, 255, 0.7); color: #0284c7; }
  .nav-card-title { color: #0c4a6e; }
  .nav-card-subtitle { color: #0369a1; }
  .nav-card-arrow { color: #0369a1; }
  &:hover .nav-card-icon { background: rgba(255, 255, 255, 0.95); color: #0369a1; }
  &:hover .nav-card-arrow { color: #0369a1; }
}

.nav-card-body {
  flex: 1;
  min-width: 0;
}

.nav-card-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 6px;
}

.nav-card-subtitle {
  font-size: 13px;
  margin: 0;
  line-height: 1.5;
}

.nav-card-arrow {
  flex-shrink: 0;
  font-size: 20px;
  transition: all 0.25s ease;
}
</style>
